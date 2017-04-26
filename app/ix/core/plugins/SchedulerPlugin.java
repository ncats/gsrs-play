package ix.core.plugins;

import java.util.*;
import java.util.function.Consumer;
import java.sql.*;
import javax.sql.DataSource;

import play.Logger;
import play.Plugin;
import play.Application;
import play.db.DB;
import play.db.ebean.Model;
import play.db.ebean.Transactional;

import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import org.quartz.utils.DBConnectionManager;
import org.quartz.utils.ConnectionProvider;
import static org.quartz.JobBuilder.*;
import static org.quartz.TriggerBuilder.*;

import ix.core.chem.StructureProcessor;

public class SchedulerPlugin extends Plugin {

    static class IxConnectionProvider implements ConnectionProvider {
        final DataSource ds;

        IxConnectionProvider () throws SQLException {
            ds = DB.getDataSource("quartz");
            if (ds == null)
                throw new IllegalArgumentException
                    ("No data source 'quartz' defined!");
        }
        
        public Connection getConnection () throws SQLException {
            return ds.getConnection();
        }
        
        public void initialize () throws SQLException {
        }
        
        public void shutdown () throws SQLException {
        }
    }

    static public class StrucProcJob implements Job {
        public StrucProcJob () {}

        public void execute (JobExecutionContext context)
            throws JobExecutionException {
            Logger.debug(Thread.currentThread()+": Executing trigger {}",
                         context.getJobDetail().getKey());
        }
    }
    
    static public class TestLoggingJob implements Job {
        public TestLoggingJob () {}

        public void execute (JobExecutionContext context)
            throws JobExecutionException {
            System.out.println("Running:" + context.getJobDetail().getKey());
        }
    }
    
    private final Application app;
    private Scheduler scheduler;

    public SchedulerPlugin (Application app) {
        this.app = app;
    }

    @Override
    public void onStart () {
        try {
//            DBConnectionManager dbman = DBConnectionManager.getInstance();
//            dbman.addConnectionProvider
//                ("QuartzDS", new IxConnectionProvider ());
            
            StdSchedulerFactory factory = new StdSchedulerFactory();
            scheduler = factory.getScheduler();
            scheduler.start();
            
            Logger.info("Plugin "+getClass().getName()+" initialized");
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void onStop () {
        if (scheduler != null) {
            try {
                scheduler.shutdown();
            }
            catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        Logger.info("Plugin "+getClass().getName()+" stopped!");        
    }

    public Scheduler getScheduler () { return scheduler; }
    
    public static class JobRunnable implements Job{
    	
    	public JobRunnable(){
    	}
		@Override
		public void execute(JobExecutionContext arg0) throws JobExecutionException {
			Runnable r = (Runnable)arg0.getJobDetail().getJobDataMap().get("run");
			r.run();
		}
    	
    }
    
    public void submit (Runnable r, ScheduleBuilder sched) {
    	
		try {
			JobDataMap jdm = new JobDataMap();
			jdm.put("run", r);

			String key = UUID.randomUUID().toString();
			JobDetail job = newJob(JobRunnable.class).setJobData(jdm).withIdentity(key).build();
			Trigger trigger = newTrigger().withIdentity(key).forJob(job).withSchedule(sched).build();
			
			System.out.println("job" + job);
			System.out.println("trigger" + trigger);
			scheduler.scheduleJob(job, trigger);
		} catch (Exception e) {
			e.printStackTrace();
		}
    	
    }
    
    public static class ScheduledTask{
    	private Runnable r;
    	private ScheduleBuilder sched=SimpleScheduleBuilder.repeatSecondlyForTotalCount(1);
    	private String key= UUID.randomUUID().toString();
    	
    	
    	public ScheduledTask(Runnable r){
    		this.r=r;
    	}
    	
    	public ScheduledTask(Runnable r, ScheduleBuilder sched){
    		this.r=r;
    		this.sched=sched;
    	}
    	
    	public ScheduledTask runnable(Runnable r){
    		this.r=r;
    		return this;
    	}
    	
    	public ScheduledTask schedule(ScheduleBuilder s){
    		this.sched=s;
    		return this;
    	}
    	
    	public ScheduledTask key(String key){
    		this.key=key;
    		return this;
    	}
    	
    	public ScheduleBuilder getSchedule(){
    		return this.sched;
    	}
    	
    	public Runnable getRunnable(){
    		return this.r;
    	}
    	

    	public String getKey(){
    		return this.key;
    	}
    	
    	public ScheduledTask dailyAtHourAndMinute(int hour, int minute){
    		return schedule(CronScheduleBuilder.dailyAtHourAndMinute(hour, minute));
    	}
    	
    	/**
    	 * See here for examples:
    	 * 
    	 * 
    	 * http://www.quartz-scheduler.org/documentation/quartz-2.x/tutorials/crontrigger.html
    	 * @param cron
    	 * @return
    	 */
    	public ScheduledTask atCronTab(String cron){
    		try{
    		CronExpression cr=new CronExpression(cron);
    		
    		return schedule(CronScheduleBuilder.cronSchedule(cr));
    		}catch(Exception e){
    			e.printStackTrace();
    			throw new IllegalStateException(e);
    		}
    	}
    	
    	public ScheduledTask atCronTab(CRON_EXAMPLE cron){
    		return atCronTab(cron.getString());
    	}
    	
    	public static ScheduledTask of(Runnable r){
    		return new ScheduledTask(r);
    	}
    	
    	
    	public static enum CRON_EXAMPLE{
    		EVERY_SECOND         ("* * * * * ? *"),
    		EVERY_10_SECONDS     ("0/10 * * * * ? *"),
    		EVERY_MINUTE         ("0 * * * * ? *"),
    		EVERY_DAY_AT_2AM     ("0 0 2 * * ? *"),
    		EVERY_SATURDAY_AT_2AM("0 0 2 * * SAT *"),
    		;
    		
    		private String c;
    		private CRON_EXAMPLE(String d){
    			c=d;
    		}
    		public String getString(){
    			return c;
    		}
    		
    		public CronScheduleBuilder getSchedule(){
    			return CronScheduleBuilder.cronSchedule(c);
    		}
    	}
    	
    }
    
    public void submit (ScheduledTask task) {
    	
		try {
			JobDataMap jdm = new JobDataMap();
			jdm.put("run", task.getRunnable());

			String key = task.getKey();
			JobDetail job = newJob(JobRunnable.class).setJobData(jdm).withIdentity(key).build();
			Trigger trigger = newTrigger().withIdentity(key).forJob(job).withSchedule(task.getSchedule()).build();
			scheduler.scheduleJob(job, trigger);
		} catch (Exception e) {
			e.printStackTrace();
		}
    	
    }
    
}
