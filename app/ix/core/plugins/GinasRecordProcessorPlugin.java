package ix.core.plugins;

import ix.core.controllers.PayloadFactory;
import ix.core.controllers.ProcessingJobFactory;
import ix.core.models.Keyword;
import ix.core.models.Payload;
import ix.core.models.ProcessingJob;
import ix.core.models.ProcessingRecord;
import ix.core.models.Structure;
import ix.utils.Util;

import java.io.InputStream;
import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import play.Application;
import play.Logger;
import play.Plugin;
import play.db.ebean.Model;
import play.db.ebean.Transactional;
import scala.collection.JavaConverters;
import scala.concurrent.stm.ccstm.Stats;
import tripod.chem.indexer.StructureIndexer;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Inbox;
import akka.actor.PoisonPill;
import akka.actor.Props;
import akka.actor.Terminated;
import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.routing.Broadcast;
import akka.routing.FromConfig;
import akka.routing.RouterConfig;
import akka.routing.SmallestMailboxRouter;
//import chemaxon.formats.MolImporter;
//import chemaxon.struc.Molecule;

public class GinasRecordProcessorPlugin extends Plugin {
	
	private static final Map<String,Statistics> jobCacheStatistics = new ConcurrentHashMap<String,Statistics>();
	private static final String GINAS_RECORD_PROCESSOR = "GinasRecordProcessor";
	private static final int MAX_EXTRACTION_QUEUE = 200;
	
	
	//Replace with the methods you want.
	private static RecordPersister _recordPersister = new ix.ginas.models.utils.GinasUtils.GinasSubstancePersister();
	private static RecordExtractor _recordExtractor = new ix.ginas.models.utils.GinasUtils.GinasDumpExtractor(null);
	private static RecordTransformer _recordTransformer= new ix.ginas.models.utils.GinasUtils.GinasSubstanceTransformer();
	
	
	//Hack variable for resisting buildup
	//of extracted records not yet transformed
	private int _extractedButNotTransformed=0;
	
	
	private final Application app;
	private StructureIndexer indexer;
	private PersistenceQueue PQ;
	private IxContext ctx;
	private ActorSystem system;		// 
	private ActorRef processor;		// Not quite sure, needed
	private Inbox inbox; 			// where things are submitted
	static final Random rand = new Random();
	
	private static GinasRecordProcessorPlugin _instance;
	
	public static GinasRecordProcessorPlugin getInstance(){
		return _instance;
	}

	static String randomKey(int size) {
		byte[] b = new byte[size];
		rand.nextBytes(b);
		return Util.toHex(b);
	}

	public static class PayloadProcessor implements Serializable {
		public final Payload payload;
		public final String id;
		public final String key;

		public PayloadProcessor(Payload payload) {
			this.payload = payload;
			this.key = randomKey(10);
			this.id = payload.id + ":" + this.key;
		}
	}

	/**
	 * Instead of creating seperate classes for performing the different stages,
	 * we're going to do a bad thing by following the convention that each actor
	 * knows which method to call. Effectively we have the same instance that
	 * get passed through the actor pipeline. This goes against the
	 * recommendation that a message shouldn't have any state information!
	 */
	
	public static class ReceiverProcessor implements Serializable {
		enum Stage {
			Routing, Instrumentation, Persisting, Done
		}

		final StructureReceiver receiver;
		//final Molecule mol;
		final String key;
		final StructureIndexer indexer;

		Stage stage = Stage.Routing;

		Structure struc;
		StructureReceiver.Status status = StructureReceiver.Status.OK;
		String mesg;

		public ReceiverProcessor(
				//Molecule mol, 
				
				StructureReceiver receiver,
				StructureIndexer indexer) {
			//this.mol = mol;
			this.receiver = receiver;
			this.indexer = indexer;
			this.key = randomKey(10);
		}

		Stage stage() {
			return stage;
		}

		void routes() {
			assert stage == Stage.Routing : "Not a valid stage (" + stage
					+ ") for routing!";
			// next stage
			stage = Stage.Instrumentation;
		}

		void instruments() {
			assert stage == Stage.Instrumentation : "Not a valid stage ("
					+ stage + ") for instrumentation!";
			Logger.debug("Instruments?");
			try {
//				if (mol != null) {
//					struc = StructureProcessor.instrument(mol);
//				}
				stage = Stage.Persisting;
			} catch (Exception ex) {
				error(ex);
			}
		}

		void persists() {
			assert stage == Stage.Persisting : "Not a valid stage (" + stage
					+ ") for persisting!";
			try {
				if (struc != null) {
					struc.save();
//					indexer.add(receiver.getSource(), struc.id.toString(), mol);
				}
				stage = Stage.Done;
			} catch (Exception ex) {
				ex.printStackTrace();
				error(ex);
			}
		}

		void done() {
			receiver.receive(status, mesg, struc);
		}

		void error(Throwable t) {
			status = StructureReceiver.Status.FAILED;
			mesg = t.getMessage();
			stage = Stage.Done;
		}
	}
/*
	public static class PayloadRecord implements Serializable {
		public final ProcessingJob job;
		public final Molecule mol;

		public PayloadRecord(ProcessingJob job, Molecule mol) {
			this.job = job;
			this.mol = mol;
		}
	}*/
	
	public static class PayloadExtractedRecord<K> implements Serializable {
		public final ProcessingJob job;
		public final K theRecord;
		
		public PayloadExtractedRecord(ProcessingJob job, K mol) {
			this.job = job;
			this.theRecord = mol;
		}
	}

	public static class PersistModel implements Serializable {
		public enum Op {
			SAVE, UPDATE, DELETE
		};

		public final Op oper;
		public final Model[] models;

		public PersistModel(Op oper, Model... models) {
			this.oper = oper;
			this.models = models;
		}

		@Transactional
		public void persists() {
			try {
				switch (oper) {
				case SAVE:
					for (Model m : models) {
						m.save();
					}
					break;
				case UPDATE:
					for (Model m : models) {
						m.update();
					}
					break;
				case DELETE:
					for (Model m : models) {
						m.delete();
					}
					break;
				}
			} catch (Throwable t) {
				t.printStackTrace();
			}
		}

		public static PersistModel Update(Model... models) {
			return new PersistModel(Op.UPDATE, models);
		}

		public static PersistModel Save(Model... models) {
			return new PersistModel(Op.SAVE, models);
		}

		public static PersistModel Delete(Model... models) {
			return new PersistModel(Op.DELETE, models);
		}
	}

	public static class TransformedRecord<K,V> implements Serializable {
		public final V theRecordToPersist;
		public final ProcessingRecord rec;
		final K theRecord;
		public final StructureIndexer indexer;

		public TransformedRecord(V persistRecord, K record,
				ProcessingRecord rec, StructureIndexer indexer) {
			this.theRecordToPersist = persistRecord;
			this.rec = rec;
			this.theRecord = record;
			this.indexer = indexer;
		}
		
		@Transactional
		public void persists() {
			
			getInstance().decrementExtractionQueue();
			try{
				_recordPersister.persist(this);
				System.out.println("Last persist:" + System.currentTimeMillis());
				String k=rec.job.getKeyMatching(GinasRecordProcessorPlugin.class.getName());
				Statistics stat = jobCacheStatistics.get(k);
				if(stat!=null){
					stat.recordsPersistedSuccess++;
				}
				jobCacheStatistics.put(k, stat);
				Logger.debug(stat.toString());
			}catch(Exception e){
				String k=rec.job.getKeyMatching(GinasRecordProcessorPlugin.class.getName());
				Statistics stat = jobCacheStatistics.get(k);
				if(stat!=null){
					stat.recordsPersistedFailed++;
				}
				jobCacheStatistics.put(k, stat);
				Logger.debug(stat.toString());
			}
			
		}
	}

	/**
	 * This actor runs in a bounded queue to ensure we don't have issues with
	 * locking due to database persistence
	 */
	public static class Reporter extends UntypedActor {
		LoggingAdapter log = Logging.getLogger(getContext().system(), this);
		final PersistenceQueue PQ;

		public Reporter(PersistenceQueue PQ) {
			this.PQ = PQ;
		}

		public void onReceive(Object mesg) {
			if (mesg instanceof PersistModel) {
				PQ.submit(new PersistModelWorker((PersistModel) mesg));
			} else if (mesg instanceof TransformedRecord) {
				PQ.submit(new PersistRecordWorker((TransformedRecord) mesg));
			} else if (mesg instanceof ReceiverProcessor) {
				PQ.submit(new ReceiverProcessorWorker((ReceiverProcessor) mesg));
			} else {
				log.info("unhandled mesg: sender=" + sender() + " mesg=" + mesg);
				unhandled(mesg);
			}
		}
	}

	static class PersistModelWorker implements
			PersistenceQueue.PersistenceContext {
		PersistModel model;

		PersistModelWorker(PersistModel model) {
			this.model = model;
		}

		public void persists() throws Exception {
			model.persists();
		}

		public Priority priority() {
			return Priority.MEDIUM;
		}
	}

	static class PersistRecordWorker implements
			PersistenceQueue.PersistenceContext {
		TransformedRecord record;

		PersistRecordWorker(TransformedRecord record) {
			this.record = record;
		}

		public void persists() throws Exception {
			record.persists();
		}

		public Priority priority() {
			return Priority.MEDIUM;
		}
	}

	static class ReceiverProcessorWorker implements
			PersistenceQueue.PersistenceContext {
		ReceiverProcessor receiver;

		ReceiverProcessorWorker(ReceiverProcessor receiver) {
			this.receiver = receiver;
		}

		public void persists() throws Exception {
			switch (receiver.stage()) {
			case Persisting:
				receiver.persists();
				// fall through

			case Done:
				receiver.done();
				break;

			default:
				assert false : "Stage " + receiver.stage() + " shouldn't be "
						+ "run in " + getClass().getName() + "!";
			}
		}

		public Priority priority() {
			return Priority.MEDIUM;
		}
	}

	public static class Processor extends UntypedActor {
		LoggingAdapter log = Logging.getLogger(getContext().system(), this);
		ActorRef reporter = context().actorFor("/user/reporter");
		StructureIndexer indexer;

		public Processor(StructureIndexer indexer) {
			this.indexer = indexer;
		}

		public void onReceive(Object mesg) {
			if (mesg instanceof PayloadProcessor) {
				PayloadProcessor payload = (PayloadProcessor) mesg;
				log.info("Received payload " + payload.id);
				

				// now spawn child processor to process the payload stream
				ActorRef child = null;
				Collection<ActorRef> children = JavaConverters
						.asJavaCollectionConverter(context().children())
						.asJavaCollection();
				for (ActorRef ref : children) {
					if (ref.path().name()
							.indexOf(payload.payload.id.toString()) >= 0) {
						// this child is current running
						child = ref;
					}
				}

				if (child != null) {
					// the given payload is currently processing
					// at the moment!
					Logger.debug("Job already exists, dumbo.");
					ProcessingJob job = new ProcessingJob();
					job.start = job.stop = System.currentTimeMillis();
					job.keys.add(new Keyword(GinasRecordProcessorPlugin.class
							.getName(), payload.key));
					job.status = ProcessingJob.Status.NOT_RUN;
					job.message = "Payload " + payload.payload.id + " is "
							+ "currently being processed.";
					job.payload = payload.payload;
					log.info("About to persist payload job, I think:" + payload.id);
					Logger.debug("Job already exists, still gonna persist.");
					reporter.tell(PersistModel.Save(job), self());
				} else {
					Logger.debug("Brand new job!");
					child = context()
							.actorOf(
									Props.create(Processor.class, indexer)
											.withRouter(
													new FromConfig()
															.withFallback(new SmallestMailboxRouter(
																	2))),
									payload.id);
					context().watch(child);
					Logger.debug("About to process ... here it comes ... wait for it ...");
					try {
						ProcessingJob job = process(reporter, child, self(),
								payload);
						Logger.debug("YAY!");
					} catch (Exception ex) {
						ex.printStackTrace();
					}
					
					child.tell(new Broadcast(PoisonPill.getInstance()), self());
				}
				log.info("Persisted payload job, I think:" + payload.id);
			} else if (mesg instanceof ReceiverProcessor) {
				Logger.debug("###############OMG IM NOT GOOD WITH COMPUTER");
				Logger.debug("###############OMG IM NOT GOOD WITH COMPUTER");
				Logger.debug("###############OMG IM NOT GOOD WITH COMPUTER");
				Logger.debug("###############OMG IM NOT GOOD WITH COMPUTER");
				
				ReceiverProcessor receiver = (ReceiverProcessor) mesg;
				switch (receiver.stage()) {
				case Routing: {
					ActorRef actor = context()
							.actorOf(
									Props.create(Processor.class, indexer)
											.withRouter(
													new FromConfig()
															.withFallback(new SmallestMailboxRouter(
																	1))),
									receiver.key);
					context().watch(actor);
					try {
						// submit for
						receiver.routes();
						actor.tell(mesg, self()); // forward to next stage
					} catch (Exception ex) {
						ex.printStackTrace();
						// notify the receiver we can't process the input
						receiver.error(ex);
						reporter.tell(mesg, self());
					}
					actor.tell(new Broadcast(PoisonPill.getInstance()), self());
				}
					break;

				case Instrumentation:
					receiver.instruments();
					reporter.tell(mesg, self());
					break;

				default:
					assert false : "Stage " + receiver.stage() + " shouldn't "
							+ "be running in " + getClass().getName() + "!";
				}
			} else if (mesg instanceof PayloadExtractedRecord) {
				PayloadExtractedRecord pr = (PayloadExtractedRecord) mesg;
				log.info("processing " + pr.job);
				ProcessingRecord rec = new ProcessingRecord();
				try{
					Object trans = _recordTransformer.transform(pr, rec);
					reporter.tell(new TransformedRecord(trans, pr.theRecord, rec, indexer),self());
					String k=pr.job.getKeyMatching(GinasRecordProcessorPlugin.class.getName());
					Statistics stat = jobCacheStatistics.get(k);
					if(stat!=null){
						stat.recordsProcessedSuccess++;
					}
					jobCacheStatistics.put(k, stat);
					Logger.debug(stat.toString());
				}catch(Exception e){
					
					getInstance().decrementExtractionQueue();
					String k=pr.job.getKeyMatching(GinasRecordProcessorPlugin.class.getName());
					Statistics stat = jobCacheStatistics.get(k);
					if(stat!=null){
						stat.recordsProcessedFailed++;
					}
					jobCacheStatistics.put(k, stat);
					Logger.debug(stat.toString());
				}
				
			} else if (mesg instanceof Terminated) {
				ActorRef actor = ((Terminated) mesg).actor();

				String id = actor.path().name();
				int pos = id.indexOf(':');
				if (pos > 0) {
					String jid = id.substring(pos + 1);
					try {
						ProcessingJob job = ProcessingJobFactory.getJob(jid);
						if (job != null) {
							job.stop = System.currentTimeMillis();
							job.status = ProcessingJob.Status.COMPLETE;
							reporter.tell(PersistModel.Update(job), self());
							log.info("done processing job {}!", jid);
						} else {
							log.error("Failed to retrieve job " + jid);
						}
					} catch (Exception ex) {
						ex.printStackTrace();
						log.error("Failed to retrieve job " + jid + "; "
								+ ex.getMessage());
					}
				} else {
					// receiver job
					// log.error("Invalid job id: "+id);
				}
				context().unwatch(actor);
			} else {
				unhandled(mesg);
			}
		}
	}

	public GinasRecordProcessorPlugin(Application app) {
		this.app = app;
	}

	@Override
	public void onStart() {
		ctx = app.plugin(IxContext.class);
		if (ctx == null)
			throw new IllegalStateException("IxContext plugin is not loaded!");

		StructureIndexerPlugin plugin = app
				.plugin(StructureIndexerPlugin.class);
		if (plugin == null)
			throw new IllegalStateException(
					"StructureIndexerPlugin is not loaded!");
		indexer = plugin.getIndexer();

		PQ = app.plugin(PersistenceQueue.class);
		if (PQ == null)
			throw new IllegalStateException(
					"Plugin PersistenceQueue is not laoded!");

		system = ActorSystem.create(GinasRecordProcessorPlugin.GINAS_RECORD_PROCESSOR);
		Logger.info("Plugin " + getClass().getName()
				+ " initialized; Akka version " + system.Version());
		RouterConfig config = new SmallestMailboxRouter(2);
		processor = system
				.actorOf(
						Props.create(Processor.class, indexer).withRouter(
								new FromConfig().withFallback(config)),
						"processor");
		system.actorOf(Props.create(Reporter.class, PQ), "reporter");
		inbox = Inbox.create(system);
		_instance=this;
	}

	@Override
	public void onStop() {
		if (system != null)
			system.shutdown();
		Logger.info("Plugin " + getClass().getName() + " stopped!");
	}

	public boolean enabled() {
		return true;
	}

	
	public String submit(Payload payload) {
		// first see if this payload has already processed..
		PayloadProcessor pp = new PayloadProcessor(payload);
		inbox.send(processor, pp);
		
		jobCacheStatistics.put(pp.key, new Statistics());
		//jobCacheStatistics.put(pp.key, value);
		return pp.key;
	}
	
	public static class Statistics{
		public int recordsExtractedSuccess=0;
		public int recordsProcessedSuccess=0;
		public int recordsPersistedSuccess=0;
		
		public int recordsExtractedFailed=0;
		public int recordsProcessedFailed=0;
		public int recordsPersistedFailed=0;
		
		public Estimate totalRecords=null;
		
		public String toString(){
			String msg = "Extracted: " + recordsExtractedSuccess + " (" + recordsExtractedFailed + " failed)\n";
			msg += "Processed: " + recordsProcessedSuccess + " (" + recordsProcessedFailed + " failed)\n";
			msg += "Persisted: " + recordsPersistedSuccess + " (" + recordsPersistedFailed + " failed)\n";
			if(totalRecords!=null)
				msg += "Total:" + totalRecords.count + "(" + totalRecords.type.toString() + ")";
			return msg;			
		}
		
	}
	
/*
	public void submit(String struc, StructureReceiver receiver) {
		try {
			MolHandler mh = new MolHandler(struc);
			submit(mh.getMolecule(), receiver);
		} catch (Exception ex) {
			ex.printStackTrace();
			throw new IllegalArgumentException(
					"Unable to parse input structure: " + struc);
		}
	}

	public void submit(Molecule mol, StructureReceiver receiver) {
		inbox.send(processor, new ReceiverProcessor(mol, receiver, indexer));
	}
*/

	/**
	 * This is so as to give the user access to the same persistence queue as
	 * the processor to prevent deadlocks
	 */
/*
	public void submit(StructureReceiver receiver) {
		inbox.send(processor, new ReceiverProcessor(null, receiver, indexer));
	}
*/

	/**
	 * batch processing
	 */
	static ProcessingJob process(ActorRef reporter, ActorRef proc,
			ActorRef sender, PayloadProcessor pp) throws Exception {
		List<ProcessingJob> jobs = ProcessingJobFactory
				.getJobsByPayload(pp.payload.id.toString());
		//Logger.debug("Okay, where are these jobs?");
		ProcessingJob job = null;
		
		if (jobs.isEmpty()) {
			//Logger.debug("Sweet, I found some!");
			job = new ProcessingJob();
			job.start = System.currentTimeMillis();
			job.keys.add(new Keyword(
					GinasRecordProcessorPlugin.class.getName(), pp.key));
			job.status = ProcessingJob.Status.RUNNING;
			job.payload = pp.payload;
			//Logger.debug("Lemme try to process one...");
			try {
				Logger.debug("Counting records");
				Estimate es= _recordExtractor.estimateRecordCount(pp.payload);
				{
					Logger.debug("Counted records");
					Statistics stat = jobCacheStatistics.get(pp.key);
					if(stat==null){
						stat.totalRecords=es;
					}else{
						stat = new Statistics();
					}
					stat.totalRecords=es;
					jobCacheStatistics.put(pp.key, stat);
					Logger.debug(stat.toString());
				}
				
				Logger.debug("Making extractor");
				RecordExtractor extract = _recordExtractor.makeNewExtractor(pp.payload);
				
				Logger.debug("Made extractor:" + extract.getClass().getName());
				for (Object m; (m = extract.getNextRecord()) != null;) {
					//Logger.debug("Extracting");
					Statistics stat = jobCacheStatistics.get(pp.key);
					if(stat!=null){
						stat.recordsExtractedSuccess++;
					}
					jobCacheStatistics.put(pp.key, stat);
					Logger.debug(stat.toString());
					getInstance().waitForProcessingRecordsCount(MAX_EXTRACTION_QUEUE);
					PayloadExtractedRecord prg=new PayloadExtractedRecord(job, m);
					getInstance().incrementExtractionQueue();
					proc.tell(prg, sender);
				}
				extract.close();
			} catch (Throwable t) {
				job.message = t.getMessage();
				job.status = ProcessingJob.Status.FAILED;
				job.stop = System.currentTimeMillis();
				t.printStackTrace();
				Logger.trace("Failed to process payload " + pp.payload.id, t);
			} finally {
				reporter.tell(PersistModel.Save(job), sender);
			}
		} else {
			//Logger.debug("No jobs? What is this, this economy? MIRITE?");
			job = jobs.iterator().next();
			job.keys.add(new Keyword(
					GinasRecordProcessorPlugin.class.getName(), pp.key));
			reporter.tell(PersistModel.Update(job), sender);
			
		}
		return job;
	}
	
	
	
	
	
	public static abstract class RecordTransformer<K,T>{
		public abstract T transform(PayloadExtractedRecord<K> pr,ProcessingRecord rec);		
	}
	public static abstract class RecordPersister<K,T>{
		public abstract void persist(TransformedRecord<K,T> prec) throws Exception;		
	}
	
	public static abstract class RecordExtractor<K>{
		InputStream is;
		public RecordExtractor(InputStream is){
			this.is=is;
		}
		
		abstract public K getNextRecord();
		abstract public void close(); 
		
		public Iterator<K> getRecordIterator(){
			return new Iterator<K>(){
				private K cached;
				private boolean done=false;
				@Override
				public boolean hasNext() {
					if(done)return false;
					if(cached!=null)
						return true;
					cached = getNextRecord();
					return (cached!=null);
				}

				@Override
				public K next() {
					if(cached!=null){
						K ret=cached;
						cached=null;
						return ret;
					}
					return getNextRecord();
				}

				@Override
				public void remove() {}
				
			};
		}		
		
		public abstract RecordExtractor<K> makeNewExtractor(InputStream is);
		
		public RecordExtractor<K> makeNewExtractor(Payload p){
			return makeNewExtractor(PayloadFactory.getStream(p));
		}
		
		/**
		 * Count records extracted from payload.
		 * 
		 * By default, this is implemented naively by iteration.
		 * 
		 * Should be overridden to take advantage of better assumptions.
		 * @param p
		 * @return
		 */
		public Estimate estimateRecordCount(Payload p){
			int count=0;
			RecordExtractor extract = makeNewExtractor(p);
			Logger.debug("counting");
			for (Object m; (m = extract.getNextRecord()) != null;count++) {
				Logger.debug("got:" + count);
			}
			Logger.debug("counted:" + count);
			extract.close();
			Logger.debug("closed");
			return new Estimate(count, Estimate.TYPE.EXACT);
		}
		
		
	}
	
	
	public static class Estimate{
		enum TYPE{EXACT, APPROXIMATE, UNKNOWN};
		long count;
		TYPE type;
		public Estimate(long count, TYPE t){
			this.count=count;
			this.type=t;				
		}
	}
	
	

	
	
	/*
	 * The following is a hack to guard against out-of-memory errors.
	 * 
	 * Essentially, if the extraction step happens much faster than the
	 * transform step, there will be a pile up of new extracted records
	 * due to the non-blocking nature of the "tell" command in AKKA.
	 * 
	 * Instead, we keep a count of what's been processed,
	 * and call waitForProcessingRecordsCount in order to block for 
	 * some upper-bound before continuing a process.
	 * 
	 * 
	 */
	/**
	 * Gets the total number of records extracted, but not
	 * transformed yet.
	 * @return
	 */
    public int getRecordsProcessing(){
    	return _extractedButNotTransformed;
    }
    
    private synchronized void incrementExtractionQueue(){
    	_extractedButNotTransformed++;
    	//Logger.debug("Total Records:" + _extractedButNotTransformed);
    }
    private synchronized void decrementExtractionQueue(){
    	_extractedButNotTransformed--;
    	//Logger.debug("Total Records:" + _extractedButNotTransformed);
    }
    private void waitForProcessingRecordsCount(int max){
    	while(getRecordsProcessing()>=max){
    		try {
				Thread.sleep(1);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
    	}
    }
    
    public static Statistics getStatisticsForJob(String jobTerm){
    	return jobCacheStatistics.get(jobTerm);
    }
	

	/*********************************************
	 * Molecule bits for 
	 * 	1. extracting from InputStream
	 *  2. transforming to Structure
	 *  3. persisting
	 * 
	 * @author peryeata
	 *
	 */
    
    /*
	public static class MoleculePersister extends RecordPersister<Molecule,Structure>{
		@Override
		public void persist(PersistRecord<Molecule, Structure> prec) {
			try {
				if (prec.theRecordToPersist != null) {
					prec.theRecordToPersist.save();
					prec.indexer.add(prec.rec.job.payload.name, prec.theRecordToPersist.id.toString(), prec.theRecord);
					prec.rec.xref = new XRef(prec.theRecordToPersist);
					prec.rec.xref.save();
				}
				prec.rec.save();
				Logger.debug("Saved struc " + (prec.theRecordToPersist != null ? prec.theRecordToPersist.id : null)
						+ " record " + prec.rec.id);
			} catch (Throwable t) {
				t.printStackTrace();
			}
		}
	}
	public static class MoleculeTransformer extends RecordTransformer<Molecule,Structure>{
		public Structure transform(PayloadRecordGeneric<Molecule> pr, ProcessingRecord rec){
			rec.name = pr.theRecord.getName();
			rec.job = pr.job;
			rec.start = System.currentTimeMillis();
			Structure struc = null;
			try {
				struc = StructureProcessor.instrument(pr.theRecord);
				rec.stop = System.currentTimeMillis();
				rec.status = ProcessingRecord.Status.OK;
			} catch (Throwable t) {
				rec.stop = System.currentTimeMillis();
				rec.status = ProcessingRecord.Status.FAILED;
				rec.message = t.getMessage();
				t.printStackTrace();
			}
			return struc;
		}
	}
	public static class MoleculeExtractor extends RecordExtractor<Molecule>{
		MolImporter mi;
		public MoleculeExtractor(InputStream is) {
			super(is);
			try{
				mi = new MolImporter(is);
			}catch(Exception e){
				
			}
		}

		@Override
		public Molecule getNextRecord() {
			if(mi==null)return null;
			try {
				return mi.read();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return null;
		}

		@Override
		public void close() {
			try{
			if(mi!=null)
				mi.close();
			}catch(Exception e){
				e.printStackTrace();
			}
		}

		@Override
		public RecordExtractor<Molecule> makeNewExtractor(InputStream is) {
			return new MoleculeExtractor(is);
		}
		
	}
	*/
}
