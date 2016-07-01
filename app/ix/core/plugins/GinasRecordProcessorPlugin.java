package ix.core.plugins;

import ix.core.UserFetcher;
import ix.core.adapters.EntityPersistAdapter;
import ix.core.controllers.ProcessingJobFactory;
import ix.core.models.Keyword;
import ix.core.models.Payload;
import ix.core.models.ProcessingJob;
import ix.core.models.ProcessingRecord;
import ix.core.models.Structure;
import ix.core.plugins.StructureProcessorPlugin.PersistRecord;
import ix.core.plugins.StructureProcessorPlugin.PersistRecordWorker;
import ix.core.processing.RecordExtractor;
import ix.core.processing.RecordTransformer;
import ix.core.stats.Estimate;
import ix.core.stats.Statistics;
import ix.core.util.BlockingSubmitExecutor;
import ix.core.util.TimeUtil;
import ix.ginas.models.v1.ChemicalSubstance;
import ix.utils.Global;
import ix.utils.TimeProfiler;
import ix.utils.Util;

import java.io.IOException;
import java.io.Serializable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;

import play.Application;
import play.Logger;
import play.Plugin;
import play.db.ebean.Model;
import play.db.ebean.Transactional;
import scala.collection.JavaConverters;
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

import com.avaje.ebean.Ebean;
import com.avaje.ebean.Transaction;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;

public class GinasRecordProcessorPlugin extends Plugin {
    private static final int AKKA_TIMEOUT = 60000;
    
//    
    /**
     * Lock object to synchronize persistance calls
     * so only 1 object is persisted at a time.
     * Not using this causes problems with MySQL.
     */
    private final Object persistanceLock = new Object();
        
    private static final String KEY_PROCESS_QUEUE_SIZE = "PROCESS_QUEUE_SIZE";
    //Hack variable for resisting buildup
    //of extracted records not yet transformed
    private static Map<String,Long> queueStatistics = new ConcurrentHashMap<String,Long>();
    private static Map<String,Statistics> jobCacheStatistics = new ConcurrentHashMap<String,Statistics>();

    private static ObjectMapper om = new ObjectMapper();

    private static int MAX_EXTRACTION_QUEUE = 100;
        

    private Set<ExecutorService> executorServices = new HashSet<>();


        
    private final Application app;

    private IxContext ctx;

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
            Objects.requireNonNull(payload);
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

        public TransformedRecord(V persistRecord, K record,
                                 ProcessingRecord rec) {
            this.theRecordToPersist = persistRecord;
            this.rec = rec;
            this.theRecord = record;
        }
                
        @Transactional
        public void persists() {
        	//System.out.println("Trying to persist");
        	
            String k=rec.job.getKeyMatching(GinasRecordProcessorPlugin.class.getName());
            
            //Set the user for use in later persist information
            UserFetcher.setLocalThreadUser(rec.job.owner);
			try {
				try {
					
					TimeProfiler.addGlobalTime("persist");
					
					long start=TimeUtil.getCurrentTimeMillis();
					rec.job.getPersister().persist(this);
					applyStatisticsChangeForJob(k, Statistics.CHANGE.ADD_PE_GOOD);
					long done=TimeUtil.getCurrentTimeMillis()-start;
//					System.out.println(     "Persisted at \t" + 
//							System.currentTimeMillis() + "\t" + 
//							this.theRecordToPersist.getClass().getName() + "\t" + 
//							done);
					
					
					TimeProfiler.stopGlobalTime("persist");
					TimeProfiler.stopGlobalTime("full submit");
					
					if(Math.random()>0.9){
						TimeProfiler.getInstance().printResults();
					}
					
					TimeProfiler.addGlobalTime("full submit");
				} catch (Exception e) {
					e.printStackTrace();
					applyStatisticsChangeForJob(k, Statistics.CHANGE.ADD_PE_BAD);

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

    public static void updateJobIfNecessary(ProcessingJob job2) {
    	ProcessingJob job;
    	try{
    		job = ProcessingJobFactory.getJob(job2.id);
    	}catch(Exception e){
    		Logger.debug("Error refreshing job from database, using local copy");
            job = job2;
    	}
    	
    	Statistics stat = getStatisticsForJob(job);
        if (stat != null && stat._isDone()) {
            updateJob(job,stat);
        }
    }
    public static void updateJob(ProcessingJob job,Statistics stat) {

        job.stop = TimeUtil.getCurrentTimeMillis();
        job.status = ProcessingJob.Status.COMPLETE;
        job.statistics = om.valueToTree(stat).toString();
        job.message="Job complete";

        job.update();
        TimeProfiler.stopGlobalTime("full submit");
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





                
        long start= System.currentTimeMillis();

        _instance=this;
    }

    @Override
    public void onStop() {
        //TODO shutdownNow() which will kill currently running threads??
        for(ExecutorService s : executorServices){
            s.shutdownNow();
        }
        executorServices.clear();
      //  executorService.shutdown();
        Logger.info("Plugin " + getClass().getName() + " stopped!");
    }

    public boolean enabled() {
        return true;
    }


    public String submit(final Payload payload, Class extractor, Class persister) {
        // first see if this payload has already processed..
    	
    	
        final PayloadProcessor pp = new PayloadProcessor(payload);
                
                
        final ProcessingJob job = new ProcessingJob();
        job.start = TimeUtil.getCurrentTimeMillis();
        job.addKeyword(new Keyword(GinasRecordProcessorPlugin.class.getName(), pp.key));
        job.setExtractor(extractor);
        job.setPersister(persister);
                
        job.status = ProcessingJob.Status.PENDING;
        job.payload = pp.payload;
        job.message="Preparing payload for processing";
        job.owner=UserFetcher.getActingUser();
        job.save();
        storeStatisticsForJob(pp.key, new Statistics());
        pp.jobId=job.id;

        final ExecutorService executorService = BlockingSubmitExecutor.newFixedThreadPool(3, MAX_EXTRACTION_QUEUE);

        final PersistRecordWorkerFactory factory = getPersistRecordWorkerFactory();

        executorServices.add(executorService);
        Runnable r= new Runnable() {
            @Override
            public void run() {

                try (RecordExtractor extractorInstance = job.getExtractor().makeNewExtractor(payload)) {

                    Estimate es = extractorInstance.estimateRecordCount(pp.payload);
                    {
                        Logger.debug("Counted records");
                        Statistics stat = getStatisticsForJob(pp.key);
                        if (stat == null) {
                            stat = new Statistics();
                        }
                        stat.totalRecords = es;
                        stat.applyChange(Statistics.CHANGE.EXPLICIT_CHANGE);
                        storeStatisticsForJob(pp.key, stat);
                        Logger.debug(stat.toString());
                    }
                    job.status = ProcessingJob.Status.RUNNING;
                    job.payload = pp.payload;
                    job.message = "Loading data";
                    job.save();

                    Object record;
                    do {
                        try {
                            record = extractorInstance.getNextRecord();
                            final PayloadExtractedRecord prg = new PayloadExtractedRecord(job, record);

                            if (record != null) {

                                executorService.submit(factory.newWorkerFor(prg));

                            }
                        } catch (Exception e) {
                            Statistics stat = getStatisticsForJob(pp.key);
                            stat.applyChange(Statistics.CHANGE.ADD_EX_BAD);
                            storeStatisticsForJob(pp.key, stat);
                            Global.ExtractFailLogger
                                    .info("failed to extract" + "\t" + e.getMessage() + "\t" + "UNKNOWN JSON");
                            // hack to keep iterator going...
                            record = new Object();
                        }
                    } while (record != null);
                    executorService.shutdown();

                }
                try {
                    executorService.awaitTermination(2, TimeUnit.DAYS);
                    Statistics stat = getStatisticsForJob(pp.key);
                    stat.applyChange(Statistics.CHANGE.MARK_EXTRACTION_DONE);
                    executorServices.remove(executorService);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }


            }
        };

        ForkJoinPool.commonPool().submit(r);


        return pp.key;
    }

    private PersistRecordWorkerFactory getPersistRecordWorkerFactory() {
      try {
          Class<?> clazz = Class.forName(app.configuration().getString("ix.ginas.PersistRecordWorkerFactoryImpl"));
          return (PersistRecordWorkerFactory) clazz.newInstance();
      }catch (IllegalAccessException | InstantiationException |ClassNotFoundException e) {
          throw new IllegalStateException("error creating PersistRecordWorkerFactory instance", e);
      }
    }


    public static abstract class PersistRecordWorker implements Runnable{

        private final PayloadExtractedRecord prg;

        public PersistRecordWorker(PayloadExtractedRecord prg){
            Objects.requireNonNull(prg);
            this.prg = prg;
        }
        @Override
        public void run() {
        	TransformedRecord tr=null;
        	ProcessingRecord rec = new ProcessingRecord();
            ProcessingJob job = prg.job;
            Statistics statistics = job.getStatistics();
            try{
	            
	            statistics.applyChange(Statistics.CHANGE.ADD_EX_GOOD);
	            
	            Object trans = job.getTransformer().transform(prg, rec);
	
	            if (trans == null) {
	                throw new IllegalStateException("Transform error");
	            }
	            statistics.applyChange(Statistics.CHANGE.ADD_PR_GOOD);
	            tr= new TransformedRecord(trans, prg.theRecord, rec);
	           
        	}catch(Throwable t){
        		statistics.applyChange(Statistics.CHANGE.ADD_PR_BAD);
        		//t.printStackTrace();
        		
        		Global.TransformFailLogger.info(rec.name + "\t" + t.getMessage().replace("\n", "") + "\t"
						+ prg.theRecord.toString().replace("\n", ""));
        		updateJobIfNecessary(rec.job);
        	}
        	if(tr!=null){
        		doPersist(tr);
        	}
        }

        protected abstract void doPersist(TransformedRecord tr);
    }
        





    public interface PersistRecordWorkerFactory{
        PersistRecordWorker newWorkerFor(PayloadExtractedRecord prg);
    }




    
    public static Statistics getStatisticsForJob(String jobTerm){
        return jobCacheStatistics.get(jobTerm);
    }
    public static Statistics getStatisticsForJob(ProcessingJob pj){
    	String k=pj.getKeyMatching(GinasRecordProcessorPlugin.class.getName());
        //the Map interface says we should be able to call get(null)
        //but Concurrenthashmap will throw a null pointer when
        //computing the hash value, at least in Java 7...
        if(k ==null){
            return null;
        }
        return jobCacheStatistics.get(k);
    }
    
    public static Statistics storeStatisticsForJob(String jobTerm, Statistics s){

        return jobCacheStatistics.compute(jobTerm, (k, v) ->{
            if(v ==null || s.isNewer(v)){
                return s;
            }
            v.applyChange(s);
            return v;
        });

    }
        
    public static Statistics applyStatisticsChangeForJob(String jobTerm, Statistics.CHANGE change){
        Statistics stat = getStatisticsForJob(jobTerm);
        stat.applyChange(change);
        return stat;
    }
    

}