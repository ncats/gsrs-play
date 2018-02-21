package ix.core.plugins;

import static org.quartz.JobBuilder.newJob;
import static org.quartz.TriggerBuilder.newTrigger;

import java.sql.Connection;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.FutureTask;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.sql.DataSource;

import org.quartz.CronExpression;
import org.quartz.CronScheduleBuilder;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.impl.StdSchedulerFactory;
import org.quartz.utils.ConnectionProvider;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import ix.core.ResourceReference;
import ix.core.util.CachedSupplier;
import ix.core.util.TimeUtil;
import ix.core.util.Toer;
import ix.core.util.Unchecked;
import ix.utils.Global;
import ix.utils.Tuple;
import play.Application;
import play.Logger;
import play.Play;
import play.Plugin;
import play.db.DB;

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

    private final Application app;
    private Map<String,ScheduledTask> tasks = new ConcurrentHashMap<>();
        
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
            }catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        Logger.info("Plugin "+getClass().getName()+" stopped!");        
    }

    public Scheduler getScheduler () { return scheduler; }
    
    public static class JobRunnable implements Job{
    	public JobRunnable(){}
		@Override
		public void execute(JobExecutionContext arg0) throws JobExecutionException {
			Runnable r = (Runnable)arg0.getJobDetail().getJobDataMap().get("run");
			r.run();
		}
    }
    
    
    private static AtomicLong idmaker = new AtomicLong(1l);
    private static Supplier<Long> idSupplier =()->{
        return idmaker.getAndIncrement();
    };
    
    public static class TaskListener{
        private Double p=null;
        private String msg = null;
        
        public Double getCompletePercentage(){
            return p;
        }
        public String getMessage(){
            return msg;
        }
        
        public TaskListener progress(double p){
            this.p=p;
            return this;
        }
        
        public TaskListener message(String msg){
            this.msg=msg;
            return this;
        }
        
        public TaskListener complete(){
            return progress(100);
        }
        
        public TaskListener start(){
            return progress(0);
        }
        
        
        
    }
    
    @Entity
    public static class ScheduledTask implements Toer<ScheduledTask>{
        
        
        
    	private Consumer<TaskListener> r=(l)->{};
    	
    	private Supplier<Boolean> check = ()->true;
    	
    	private CronScheduleBuilder sched=CronScheduleBuilder.dailyAtHourAndMinute(2, 1);
    	private String key= UUID.randomUUID().toString();
    	private String description = "Unnamed process";
    	
    	private Date lastStarted=null;
        private Date lastFinished=null;
        private int numberOfRuns=0;
        private boolean enabled=true;

        private FutureTask currentTask=null;
        
        private AtomicBoolean isRunning=new AtomicBoolean(false);

        private AtomicBoolean isLocked=new AtomicBoolean(false);
        
        private CronExpression cronExp= null;
        
        private TaskListener listener = new TaskListener();
        
        
        @JsonProperty("running")
        public boolean isRunning(){
            return isRunning.get();
        }
        
        
        public TaskListener getTaskDetails(){
            if(this.isRunning())
            return this.listener;
            return null;
        }
        
        @Id
        public Long id=idSupplier.get();
    	
    	
    	public ScheduledTask(Consumer<TaskListener> r){
    		this.r=r;
    	}
    	
    	public ScheduledTask(Consumer<TaskListener> r, CronScheduleBuilder sched){
    		this.r=r;
    		this.sched=sched;
    	}
    	
    	public ScheduledTask runnable(Runnable r){
    		this.r=(l)->r.run();
    		return this;
    	}
    	
    	public ScheduledTask onlyIf(Supplier<Boolean> onlyIf){
            this.check=onlyIf;
            return this;
        }
    	
    	public ScheduledTask onlyIf(Predicate<ScheduledTask> onlyIf){
            this.check=()->onlyIf.test(this);
            return this;
        }
    	
    	private ScheduledTask schedule(CronScheduleBuilder s){
    	    this.cronExp=null;
    		this.sched=s;
    		return this;
    	}
    	
    	public ScheduledTask key(String key){
    		this.key=key;
    		return this;
    	}
    	
    	
    	@JsonIgnore
    	public CronScheduleBuilder getSchedule(){
    		return this.sched;
    	}
    	
    	
    	@JsonIgnore
    	public Trigger getSubmittedTrigger() throws SchedulerException{
    	    Trigger t=this.getJob().v();
    	    return Play.application().plugin(SchedulerPlugin.class)
                    .scheduler.getTrigger(t.getKey());
    	}
    	
    	public Date getNextRun(){
    	    try {
    	        return getSubmittedTrigger().getNextFireTime();
            } catch (SchedulerException e) {
                e.printStackTrace();
                return null;
            }
    	}
    	
    	public String getCronSchedule(){
    	    if(cronExp==null)return null;
            return cronExp.getCronExpression();
        }
    	
    	
    	@JsonIgnore
    	public Runnable getRunnable(){
    		return ()->{
    		    if(enabled){
        		    if(check.get()){
        		        runNow();
        		    }
    		    }
    		};
    	}
    	
    	public synchronized void runNow(){
    	    numberOfRuns++;
    	    isRunning.set(true);
            lastStarted=TimeUtil.getCurrentDate();
            this.listener.start();
            try {
                Callable<Void> callable = () -> {
                    try {
                        r.accept(this.listener);
                        return null;
                    }catch(Throwable t){
                        t.printStackTrace();
                        throw t;
                }
            };
                currentTask = new FutureTask<>(callable);
                currentTask.run();
            }finally{
                lastFinished=TimeUtil.getCurrentDate();
                isRunning.set(false);
                this.listener.complete();
            }
            isLocked.set(false);
    	}
    	
    	
    	public int getNumberOfRuns(){
    	    return this.numberOfRuns;
    	}
    	
    	public String getDescription(){
    	    return this.description;
    	}

    	public String getKey(){
    		return this.key;
    	}
    	
    	public Date getLastStarted(){
    	    return this.lastStarted;
    	}
    	
    	public Date getLastFinished(){
            return this.lastFinished;
        }
    	
    	@JsonProperty("enabled")
    	public boolean isEnabled(){
            return this.enabled;
        }
    	
    	@JsonProperty("url")
        public String getSelfUrl () {
            return Global.getNamespace()+"/scheduledjobs("+id+")";
        }
    	
    	@JsonProperty("@disable")
        public ResourceReference<ScheduledTask> getDisableAction () {
    	      if(!this.isEnabled())return null;
              String uri = Global.getNamespace()+"/scheduledjobs("+id+")/$@disable";
              return ResourceReference.of(uri, ()->{
                  disable();
                  return ScheduledTask.this;
              });
        }

        @JsonProperty("@cancel")
        public ResourceReference<ScheduledTask> getCancelAction () {
            if(!this.isRunning())return null;

            String uri = Global.getNamespace()+"/scheduledjobs("+id+")/$@cancel";
            return ResourceReference.of(uri, ()->{
                cancel();
                return ScheduledTask.this;
            });
        }
    	
    	@JsonProperty("@enable")
        public ResourceReference<ScheduledTask> getEnableAction () {
    	      if(this.isEnabled())return null;
              String uri = Global.getNamespace()+"/scheduledjobs("+id+")/$@enable";
              return ResourceReference.of(uri, ()->{
                      enable();
                      return ScheduledTask.this;
              });
        }
    	
    	@JsonProperty("@execute")
        public ResourceReference<ScheduledTask> getExecuteAction () {
    	      if(this.isRunning())return null;
              String uri = Global.getNamespace()+"/scheduledjobs("+id+")/$@execute";
              return ResourceReference.of(uri, ()->{
                      if(!isRunning() && !isLocked.get()){
                          isLocked.set(true);
                          ForkJoinPool.commonPool().submit(()->runNow());
                      }
                      return ScheduledTask.this;
              });
        }
    	
    	public ScheduledTask dailyAtHourAndMinute(int hour, int minute){
    		return at(new CronExpressionBuilder()
    		               .everyDay()
    		               .atHourAndMinute(hour, minute));
    	}
    	
    	public ScheduledTask at(CronExpressionBuilder ceb){
            CronExpression cex=Unchecked.uncheck(()->ceb
                                       .buildExpression());
            return this.atCronTab(cex);
        }
    	
    	public ScheduledTask wrap(Consumer<Runnable> wrapper){
    	    Consumer<TaskListener> c=r;
    	    r=(l)->{
    	        wrapper.accept(()->{
    	           c.accept(l); 
    	        });
    	    };
            return this;
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
    		try {
                return atCronTab(new CronExpression(cron));
            } catch (ParseException e) {
                e.printStackTrace();
                throw new IllegalStateException(e);
            }
    	}
    	
    	public ScheduledTask atCronTab(CronExpression cron){
    	    schedule(CronScheduleBuilder.cronSchedule(cron));
    	    cronExp=cron;
            return this;
        }
    	
    	public ScheduledTask atCronTab(CRON_EXAMPLE cron){
    		return atCronTab(cron.getString());
    	}
    	
    	private CachedSupplier<Tuple<JobDetail,Trigger>> submitted=CachedSupplier.of(()->{
    	    JobDataMap jdm = new JobDataMap();
            jdm.put("run", this.getRunnable());

            String key = this.getKey();
            JobDetail job = newJob(JobRunnable.class)
                                .setJobData(jdm)
                                .withIdentity(key)
                                .build();
            Trigger trigger = newTrigger().withIdentity(key)
                                .forJob(job)
                                .withSchedule(this.getSchedule())
                                .build();
            return Tuple.of(job,trigger);
    	});
    	
    	
    	@JsonIgnore
    	public Tuple<JobDetail,Trigger> getJob(){
    	    return submitted.get();
    	}
    	
    	public static ScheduledTask of(Consumer<TaskListener> r){
            return new ScheduledTask(r);
        }
    	
    	public static ScheduledTask of(Runnable r){
            return new ScheduledTask((l)->r.run());
        }
    	
    	public static enum CRON_EXAMPLE{
    		EVERY_SECOND         ("* * * * * ? *"),
    		EVERY_10_SECONDS     ("0/10 * * * * ? *"),
    		EVERY_MINUTE         ("0 * * * * ? *"),
    		EVERY_DAY_AT_2AM     ("0 0 2 * * ? *"),
    		EVERY_SATURDAY_AT_2AM("0 0 2 * * SAT *")
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

        public ScheduledTask description(String description) {
            this.description=description;
            return this;
        }
        
        public ScheduledTask disable(){
            this.enabled=false;
            return this;
            
        }

        public ScheduledTask cancel(){
            System.out.println("in cancel method");
            if(currentTask !=null){
                System.out.println("calling cancel in currentTask");
                currentTask.cancel(true);
            }
            return this;

        }

        public ScheduledTask enable(){
            this.enabled=true;
            return this;
        }
        
        public void submit(SchedulerPlugin plug){
            plug.submit(this);
        }
        
        /**
         * Submits the task to the default SchedulerPlugin
         */
        public void submit(){
            this.submit(Play.application().plugin(SchedulerPlugin.class));
        }
    }
    
    public void submit (ScheduledTask task) {
    	
		try {
		    tasks.put(task.getKey(), task);
		    task.getJob().consume((j,t)->{
		        scheduler.scheduleJob(j, t);
		    });
		} catch (Exception e) {
			e.printStackTrace();
		}
    }
    
    public List<ScheduledTask> getTasks(){
        return this.tasks.values().stream().collect(Collectors.toList());
    }
    
    public ScheduledTask getTask(String key){
        return this.tasks.get(key);
    }
    
    
    
    
}
