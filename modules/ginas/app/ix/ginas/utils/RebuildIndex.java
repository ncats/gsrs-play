package ix.ginas.utils;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.persistence.Entity;

import org.reflections.Reflections;

import com.avaje.ebean.Query;
import com.avaje.ebean.QueryIterator;

import ix.core.adapters.EntityPersistAdapter;
import ix.core.controllers.EntityFactory.EntityMapper;
import ix.core.models.BackupEntity;
import ix.utils.EntityUtils;
import play.Logger;
import play.db.ebean.Model;

public class RebuildIndex  {
	public static String UPDATE_MESSAGE = "";
	public static int PAGE_SIZE=10;
	
	public static String getUpdateMessage(){
		return UPDATE_MESSAGE;
	}
	
	
	private static void updateIndexesFromBackup(Class<?> type, Date since){
		
		long start = System.currentTimeMillis();
		Model.Finder<Long,BackupEntity> finder = new Model.Finder(Long.class, BackupEntity.class);
		int page = 0;
		int pageSize = PAGE_SIZE;
		int rcount = finder.findRowCount();
		
		UPDATE_MESSAGE = "Fetching first " + pageSize + " of " + rcount + " records in " + (System.currentTimeMillis() - start) + "ms";
		long totalTimeSerializing=0;
		
		BlockingQueue<Runnable> blockingQueue = new ArrayBlockingQueue<Runnable>(pageSize);
	    RejectedExecutionHandler rejectedExecutionHandler = new ThreadPoolExecutor.CallerRunsPolicy();
	    ExecutorService executor =  new ThreadPoolExecutor(5, 5, 
	        1L, TimeUnit.MINUTES, blockingQueue, rejectedExecutionHandler);

		while (true) {
			Query q = finder.query()
					.setFirstRow(pageSize * page)
					.setMaxRows(pageSize);
			
			List<BackupEntity> l = q.findList();
			
			for (BackupEntity o : l) {
				//TODO: make part of query
				if(!o.isOfType(type))continue;
				if(since!=null){
					if(!o.modified.after(since)){
						continue;
					}
				}
				long serialTime=System.currentTimeMillis();
				executor.submit(new Worker(o));
				serialTime=System.currentTimeMillis()-serialTime;
				totalTimeSerializing+=serialTime;
			}
			
			long timesofar=(System.currentTimeMillis() - start);
			double serialFraction = totalTimeSerializing/(timesofar+0.0);
			
			UPDATE_MESSAGE += "\nRecords Processed:" + (page + 1) * pageSize + " of " + rcount + " in " +timesofar + "ms (" +totalTimeSerializing + "ms serializing, " +serialFraction + ")";
			if (l.isEmpty() || (page + 1) * pageSize > rcount) break;
			page++;
		}
		executor.shutdown();
		while(!executor.isTerminated()){}

		UPDATE_MESSAGE += "\n\nCompleted " +type.getName() + " reindexing.\nTotal Time:" + (System.currentTimeMillis() - start) + "ms";
	}
	
	public static void updateLuceneIndex(String models) throws Exception{
		//if(true){
		//	updateLuceneIndexLegacy(models);
		//	return;
		//}
		try{
			UPDATE_MESSAGE = "Preprocessing ...";
			
			Collection<Class<?>> classes = getEntityClasses(models.split(","));
			long start = System.currentTimeMillis();
			EntityPersistAdapter.setUpdatingIndex(true);
			
			for (Class<?> eclass : classes) {
				updateIndexesFromBackup(eclass,null);
			}
			UPDATE_MESSAGE += "\n\n====================\n\nComplete.\nTotal Time:" + (System.currentTimeMillis() - start) + "ms";
		}catch(Exception e){
			e.printStackTrace();
			UPDATE_MESSAGE = e.getMessage();
		}finally {
			EntityPersistAdapter.setUpdatingIndex(false);
		}
	}
	
	public static class Worker implements Runnable{
		BackupEntity oreal;
		public Worker(BackupEntity bm){
			this.oreal=bm;
		}
		@Override
		public void run() {
			try{
				EntityPersistAdapter.getInstance().deepreindex(oreal.getInstantiated());
			}catch(Exception e){
				e.printStackTrace();
			}
		}
		
	}
	
    public static void updateLuceneIndexLegacy(String models) throws Exception{
		try {
			UPDATE_MESSAGE = "Preprocessing ...";
			Collection<Class<?>> classes = getEntityClasses(models.split(","));

			final long start = System.currentTimeMillis();
			//EntityPersistAdapter.setUpdatingIndex(true);
			for (Class<?> eclass : classes) {

				Class idClass = Long.class;
				Field idf = EntityUtils.getIdFieldForClass(eclass);
				if (idf != null) {
					idClass = idf.getType();
				}
				//System.out.println(eclass + "\t" + idClass);
				Model.Finder finder = new Model.Finder(idClass, eclass);
				final int[] page = new int[]{ 0};
				final int pageSize = 10;
				final int[] fetchcount=new int[]{0};
				final int rcount = finder.findRowCount();
				UPDATE_MESSAGE = "Fetching first " + pageSize + " of " + rcount + " records in " + (System.currentTimeMillis() - start) + "ms";
				final long[] totalTimeSerializing=new long[]{0};
				final EntityMapper em = EntityMapper.FULL_ENTITY_MAPPER();
				
					QueryIterator qi =finder.findIterate();
					Consumer c=new Consumer(){
						@Override
						public void accept(Object o) {
							long serialTime=System.currentTimeMillis();
							try {
								String v = em.valueToTree(o).toString();
							} catch (Exception e) {
								e.printStackTrace();
								Logger.info("Error serializing entity:" + o);
							}
							serialTime=System.currentTimeMillis()-serialTime;
							
							synchronized(fetchcount){
								fetchcount[0]++;
								totalTimeSerializing[0]+=serialTime;
								System.out.println("fetched:" + fetchcount[0]);
								
								
								
								if(fetchcount[0]%pageSize==0){
									long timesofar=(System.currentTimeMillis() - start);
									double serialFraction = totalTimeSerializing[0]/(timesofar+0.0);
									UPDATE_MESSAGE += "\nRecords Processed:" + (page[0] + 1) * pageSize + " of " + rcount
										+ " in " + timesofar + "ms (" + totalTimeSerializing[0] + "ms serializing, "
										+ serialFraction + ")";
									page[0]++;
								}
							}
						}
					};
					int i=0;
					while(qi.hasNext()){
						Object o=qi.next();
						//c.accept(o);
						System.out.println(i);
						i++;
					}
					qi.close();
			}
			UPDATE_MESSAGE += "\n\nComplete.\nTotal Time:" + (System.currentTimeMillis() - start) + "ms";
		}catch(Exception e){
			e.printStackTrace();
			UPDATE_MESSAGE = e.getMessage();
		}finally {
			EntityPersistAdapter.setUpdatingIndex(false);
		}
    }
    
    public static interface Consumer{
    	public void accept(Object o);
    }
    public static Set<Class<?>> getEntityClasses (String[] models) throws Exception {
        Set<Class<?>> classes = new HashSet<Class<?>>();
        for(String load: models) {
            load = load.trim();
            if (load.endsWith(".*")) {
                Reflections reflections = new Reflections
                    (load.substring(0, load.length()-2));
                Set<Class<?>> resources =
                    reflections.getTypesAnnotatedWith(Entity.class);
                for (Class<?> c : resources) {
                	classes.add((Class<? extends Entity>) c);
                }
            }
            else {
            	Reflections reflections = new Reflections
                        (load.substring(0, load.lastIndexOf(".")));
            	Set<Class<?>> resources =
                        reflections.getTypesAnnotatedWith(Entity.class);
                    for (Class<?> c : resources) {
                    	if(c.getName().equalsIgnoreCase(load))
                    		classes.add(c);
                    }
            }
        }
        return classes;
        
    }
}