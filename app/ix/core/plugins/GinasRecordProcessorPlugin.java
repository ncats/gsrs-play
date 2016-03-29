package ix.core.plugins;

import ix.core.UserFetcher;
import ix.core.adapters.EntityPersistAdapter;
import ix.core.controllers.ProcessingJobFactory;
import ix.core.models.Keyword;
import ix.core.models.Payload;
import ix.core.models.ProcessingJob;
import ix.core.models.ProcessingRecord;
import ix.core.models.Structure;
import ix.core.processing.RecordExtractor;
import ix.core.processing.RecordTransformer;
import ix.core.stats.Estimate;
import ix.core.stats.Statistics;
import ix.core.util.TimeUtil;
import ix.utils.Global;
import ix.utils.TimeProfiler;
import ix.utils.Util;

import java.io.Serializable;
import java.util.Collection;
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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;

public class GinasRecordProcessorPlugin extends Plugin {
    private static final int AKKA_TIMEOUT = 60000;
    
//    
    
        
    private static final String KEY_PROCESS_QUEUE_SIZE = "PROCESS_QUEUE_SIZE";
    //Hack variable for resisting buildup
    //of extracted records not yet transformed
    private static Map<String,Long> queueStatistics = new ConcurrentHashMap<String,Long>();
    private static Map<String,Statistics> jobCacheStatistics = new ConcurrentHashMap<String,Statistics>();
    private static final String GINAS_RECORD_PROCESSOR = "GinasRecordProcessor";
    private static int MAX_EXTRACTION_QUEUE = 100;
        
          

        
        
    private final Application app;
    private StructureIndexer indexer;
    private PersistenceQueue PQ;
    private IxContext ctx;
    private ActorSystem system;         // 
    private ActorRef processor;         // Not quite sure, needed
    private Inbox inbox;                        // where things are submitted
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
        public Long jobId;
                
        public PayloadProcessor(Payload payload) {
            this.payload = payload;
            this.key = randomKey(10);
            this.id = payload.id + ":" + this.key;
                        
        }
    }

    
        
    public static class PayloadExtractedRecord<K> implements Serializable {
        public final ProcessingJob job;
        public final K theRecord;
        public final String jobKey;
                
        public PayloadExtractedRecord(ProcessingJob job, K rec) {
            this.job = job;
            this.theRecord = rec;
            String k=job.getKeyMatching(GinasRecordProcessorPlugin.class.getName());
            jobKey=k;
        }
    }

    public static class PersistModel implements Serializable {
        public enum Op {
            SAVE, UPDATE, DELETE
        };

        public final Op oper;
        public final Model[] models;
        public boolean worked=false;

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
                worked=true;
            } catch (Throwable t) {
                t.printStackTrace();
                worked=false;
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
            String k=rec.job.getKeyMatching(GinasRecordProcessorPlugin.class.getName());
            
            //Set the user for use in later persist information
            UserFetcher.setLocalThreadUser(rec.job.owner);
			try {
				try {
					EntityPersistAdapter.persistcount=0;
					long start=System.currentTimeMillis();
					rec.job.getPersister().persist(this);
					Statistics stat = applyStatisticsChangeForJob(k, Statistics.CHANGE.ADD_PE_GOOD);
					long done=System.currentTimeMillis()-start;
					System.out.println(     "Persisted at \t" + 
							System.currentTimeMillis() + "\t" + 
							this.theRecordToPersist.getClass().getName() + "\t" + 
							done + "\t" + 
							EntityPersistAdapter.persistcount);
					
					if(Math.random()>0.9){
						TimeProfiler.getInstance().printResults();
					}
				} catch (Exception e) {
					e.printStackTrace();
					applyStatisticsChangeForJob(k, Statistics.CHANGE.ADD_PE_BAD);
					ObjectMapper om = new ObjectMapper();
					Global.PersistFailLogger.info(rec.name + "\t" + rec.message + "\t"
							+ om.valueToTree(theRecord).toString().replace("\n", ""));
				}
				updateJobIfNecessary(rec.job);
			} finally {
				// unset local user, just in case
				UserFetcher.setLocalThreadUser(null);
			}
        }
    }

    public synchronized static void updateJobIfNecessary(ProcessingJob job2) {
    	ProcessingJob job = job2;
    	try{
    		job = ProcessingJobFactory.getJob(job2.id);
    	}catch(Exception e){
    		Logger.debug("Error refreshing job from database, using local copy");
    	}
    	
    	Statistics stat = getStatisticsForJob(job);
        if (stat != null) {
            if (stat._isDone()) {
            	updateJob(job,stat);
            }
        }
    }
    public synchronized static void updateJob(ProcessingJob job,Statistics stat) {
    	Logger.debug("I think it's done:" + stat.toString());
        ObjectMapper om = new ObjectMapper();
        job.stop = TimeUtil.getCurrentTimeMillis();
        job.status = ProcessingJob.Status.COMPLETE;
        job.statistics = om.valueToTree(stat).toString();
        PersistModel pm = PersistModel.Update(job);
        pm.persists();
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
                    Logger.debug("Job already exists");
                    ProcessingJob job = new ProcessingJob();
                    job.start = job.stop = TimeUtil.getCurrentTimeMillis();
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
                    child = context()
                        .actorOf(
                                 Props.create(Processor.class, indexer)
                                 .withRouter(
                                             new FromConfig()
                                             .withFallback(new SmallestMailboxRouter(
                                                                                     2))),
                                 payload.id);
                    context().watch(child);
                    try {
                        ProcessingJob job = process(reporter, 
                        							child, 
                        							self(),
                                                    payload);
                        Logger.debug("Processed Job");
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                                        
                    child.tell(new Broadcast(PoisonPill.getInstance()), self());
                }
            } else if (mesg instanceof PayloadExtractedRecord) {

//            	TRY_STUFF_TS++;
//                System.out.println("Starting Transform:" + TRY_STUFF_TS);
//                
                PayloadExtractedRecord pr = (PayloadExtractedRecord) mesg;
                                
                ProcessingRecord rec = new ProcessingRecord();
                String k=pr.jobKey;
                try{
                    Payload pay=pr.job.payload;
                    RecordTransformer rt=pr.job.getTransformer();
                    Object trans = rt.transform(pr, rec);
                    
                    if(trans==null){
                        throw new IllegalStateException("Transform error");
                    }
                    
                    applyStatisticsChangeForJob(k,Statistics.CHANGE.ADD_PR_GOOD);
                    reporter.tell(new TransformedRecord(trans, pr.theRecord, rec, indexer),self());
                                        
                }catch(Throwable e){
                	
                    getInstance().decrementExtractionQueue();
                    Logger.error(e.getMessage() + ":" + rec.message);
                    
                    ObjectMapper om = new ObjectMapper();
                    
                    Global.TransformFailLogger.info(rec.name + "\t" + rec.message + "\t" + om.valueToTree(pr.theRecord).toString().replace("\n", ""));
                    applyStatisticsChangeForJob(k,Statistics.CHANGE.ADD_PR_BAD);
                    try{
                		updateJobIfNecessary(pr.job);
                	}catch(Exception e2){
                		e.printStackTrace();
                	}
                }finally{
                	
                }
                //TRY_STUFF_TF++;
                //System.out.println("Ending Transform:" + TRY_STUFF_TF);
                                
            } else if (mesg instanceof Terminated) {
                ActorRef actor = ((Terminated) mesg).actor();

                String id = actor.path().name();
                int pos = id.indexOf(':');
                if (pos > 0) {
                   
                } else {
                   
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
        Config cfg = new Config();
        if (play.Play.application().configuration()
            .getBoolean("ix.ginas.hazelcast", false)) {
            HazelcastInstance instance = Hazelcast.newHazelcastInstance(cfg);
            jobCacheStatistics = instance.getMap("jobStatistics");
            queueStatistics= instance.getMap("queueStatistics");
        }
        
        MAX_EXTRACTION_QUEUE=play.Play.application().configuration()
                .getInt("ix.ginas.maxrecordqueue", MAX_EXTRACTION_QUEUE);
               
        
       
        
        
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
                
        long start= System.currentTimeMillis();
        while(true){
            try{
                inbox = Inbox.create(system);
                break;
            }catch(Exception e){
                Logger.error(e.getMessage() + " retrying");
            }
            if(System.currentTimeMillis()>start+GinasRecordProcessorPlugin.AKKA_TIMEOUT){
                throw new IllegalStateException("Couldn't start akka");
            }
            try{
                Thread.sleep(10);
            }catch(Exception e){
                e.printStackTrace();
            }
        }
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

    //  public String submit(Payload payload) {
    //          return submit(payload, _recordExtractor.getClass());
    //  }
    public String submit(Payload payload, Class extractor, Class persister) {
        // first see if this payload has already processed..
    	
    	
        PayloadProcessor pp = new PayloadProcessor(payload);
                
                
        ProcessingJob job = new ProcessingJob();
        job.start = TimeUtil.getCurrentTimeMillis();
        job.keys.add(new Keyword(GinasRecordProcessorPlugin.class.getName(), pp.key));
        job.setExtractor(extractor);
        job.setPersister(persister);
                
        job.status = ProcessingJob.Status.PENDING;
        job.payload = pp.payload;
        job.message="Preparing payload for processing";
        job.owner=UserFetcher.getActingUser();
        job.save();
        storeStatisticsForJob(pp.key, new Statistics());
        pp.jobId=job.id;
        inbox.send(processor, pp);
                
        
        
        return pp.key;
    }


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
     * 
     * One issue here is that this is designed to only
     * have 1 job per payload. In practice, it might not
     * be done this way.
     * 
     */
    static ProcessingJob process(ActorRef reporter, ActorRef proc,
                                 ActorRef sender, PayloadProcessor pp) throws Exception {
        List<ProcessingJob> jobs = ProcessingJobFactory.getJobsByPayload(pp.payload.id.toString());
        Logger.debug("Okay, where are these jobs?");
        ProcessingJob job = null;
        if (jobs.isEmpty()) {
            job = new ProcessingJob();
            job.start = TimeUtil.getCurrentTimeMillis();
            job.keys.add(new Keyword(
                                     GinasRecordProcessorPlugin.class.getName(), pp.key));
            job.status = ProcessingJob.Status.PENDING;
            job.payload = pp.payload;
        }else{
            job = jobs.iterator().next();
        }
        Logger.debug(job.status.toString());
        //If the job hasn't started yet, then start it
        if (job.status==ProcessingJob.Status.PENDING || job.status==ProcessingJob.Status.COMPLETE) {
            try {
                Logger.debug("Counting records");
                RecordExtractor rec = job.getExtractor();
                Estimate es= rec.estimateRecordCount(pp.payload);
                {
                    Logger.debug("Counted records");
                    Statistics stat = getStatisticsForJob(pp.key);
                    if(stat==null){
                        stat = new Statistics();
                    }
                    stat.totalRecords=es;
                    stat.applyChange(Statistics.CHANGE.EXPLICIT_CHANGE);
                    storeStatisticsForJob(pp.key, stat);
                    Logger.debug(stat.toString());
                }
                job.status = ProcessingJob.Status.RUNNING;
                job.payload = pp.payload;
                                
                Logger.debug("Making extractor");
                RecordExtractor extract = rec.makeNewExtractor(pp.payload);
                                
                Logger.debug("Made extractor:" + extract.getClass().getName());
                for (Object m; ;) {
                	m=null;
                	try{
                		m=extract.getNextRecord();
                		if(m==null)break;
                		Statistics stat = getStatisticsForJob(pp.key);
                        stat.applyChange(Statistics.CHANGE.ADD_EX_GOOD);
                        storeStatisticsForJob(pp.key, stat);
                        getInstance().waitForProcessingRecordsCount(MAX_EXTRACTION_QUEUE);
                        PayloadExtractedRecord prg=new PayloadExtractedRecord(job, m);
                        getInstance().incrementExtractionQueue();
                        proc.tell(prg, sender);
                	}catch(Exception e){
                		Statistics stat = getStatisticsForJob(pp.key);
                        stat.applyChange(Statistics.CHANGE.ADD_EX_BAD);
                        storeStatisticsForJob(pp.key, stat);
                        Global.ExtractFailLogger.info("failed to extract" + "\t" + e.getMessage() + "\t" + "UNKNOWN JSON");
                	}
                	
                    
                }
                extract.close();
                Statistics stat = getStatisticsForJob(pp.key);
                stat.applyChange(Statistics.CHANGE.MARK_EXTRACTION_DONE);
                storeStatisticsForJob(pp.key, stat);
            } catch (Throwable t) {
                job.message = t.getMessage();
                job.status = ProcessingJob.Status.FAILED;
                job.stop = TimeUtil.getCurrentTimeMillis();
                t.printStackTrace();
                Logger.trace("Failed to process payload " + pp.payload.id, t);
            } finally {
                reporter.tell(PersistModel.Save(job), sender);
            }
        } else {
            job.keys.add(new Keyword(
                                     GinasRecordProcessorPlugin.class.getName(), pp.key));
            reporter.tell(PersistModel.Update(job), sender);
        }
        //fail the processing job that was intended by this
        if(job.id!=pp.jobId){
            for(ProcessingJob jb2:jobs){
                if(jb2.id==pp.jobId){
                    Logger.debug("The job already exists, but isn't selected");
                    jb2.status=ProcessingJob.Status.FAILED;
                    jb2.message="Payload job already exists";
                    PersistModel.Update(jb2).persists();
                }
            }
        }
        return job;
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
    public long getRecordsProcessing(){
        Long l=queueStatistics.get(GinasRecordProcessorPlugin.KEY_PROCESS_QUEUE_SIZE);
        if(l==null)return 0;
        return l;
    }
    
    private synchronized void incrementExtractionQueue(){
        
        Long l=queueStatistics.get(GinasRecordProcessorPlugin.KEY_PROCESS_QUEUE_SIZE);
        if(l==null)l=(long) 0;
        l++;
        queueStatistics.put(GinasRecordProcessorPlugin.KEY_PROCESS_QUEUE_SIZE,l);

//        System.out.println("Incrementing Total Records:" + l);
        //return l;
        //Logger.debug("Total Records:" + _extractedButNotTransformed);
    }
    private synchronized void decrementExtractionQueue(){
        
        Long l=queueStatistics.get(GinasRecordProcessorPlugin.KEY_PROCESS_QUEUE_SIZE);
        if(l==null)l=(long) 0;
        l--;
        
        queueStatistics.put(GinasRecordProcessorPlugin.KEY_PROCESS_QUEUE_SIZE,l);
//        System.out.println("Decrementing Total Records:" + l);
    }
    private void waitForProcessingRecordsCount(int max){
    	int t=0;
        while(getRecordsProcessing()>=max){
        	try {
            	Thread.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            t++;
            if(t>8000)break;
        }
    }
    
    public static Statistics getStatisticsForJob(String jobTerm){
        return jobCacheStatistics.get(jobTerm);
    }
    public static Statistics getStatisticsForJob(ProcessingJob pj){
    	String k=pj.getKeyMatching(GinasRecordProcessorPlugin.class.getName());
        return jobCacheStatistics.get(k);
    }
    
    public static synchronized Statistics storeStatisticsForJob(String jobTerm, Statistics s){
        Statistics st=getStatisticsForJob(jobTerm);
        if(st==s)return s;
        //More recent, substitute
        if(st==null || s.isNewer(st)){
            return jobCacheStatistics.put(jobTerm,s);
        }else{
            st.applyChange(s);
            return jobCacheStatistics.put(jobTerm,st);
        }
    }
        
    public static Statistics applyStatisticsChangeForJob(String jobTerm, Statistics.CHANGE change){
        Statistics stat = getStatisticsForJob(jobTerm);
        stat.applyChange(change);
        return storeStatisticsForJob(jobTerm, stat);
    }
    

}
