package ix.core.util.scheduler;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import ix.utils.Tuple;

public class Scheduler {
	private static final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
	private static final List<ScheduledTask> tasks = new ArrayList<>();
	
	
	
	public static void addTask(ScheduledTask task){
		
		tasks.add(task);
		
		scheduler.scheduleWithFixedDelay(task, task.getOffset(),task.getEvery(),  task.getTimeUnit());
		
	}
	
	
	public static class ScheduledTask implements Runnable{
		private String name = "unnamed task";
		private Runnable delegate;
		private int everyAmount;
		private TimeUnit everyUnit;
		private int offsetAmount=0;
		private boolean enabled=true;
		
		public ScheduledTask(Runnable r, int every, int start, TimeUnit unit){
			this.delegate=r;
			this.everyAmount=every;
			this.offsetAmount=start;
			this.everyUnit=unit;
		}
		
		public void disable(){
			this.enabled=false;
		}
		public void enable(){
			this.enabled=true;
		}
		
		
		public String getName(){
			return name;
		}
		public Runnable getRunner(){
			return delegate;
		}
		
		public Tuple<Integer,TimeUnit> every(){
			return Tuple.of(everyAmount,everyUnit);
		}
		@Override
		public void run() {
			if(enabled){
				getRunner().run();
			}
		}
		
		public int getOffset(){
			return this.offsetAmount;
		}
		
		public int getEvery(){
			return this.everyAmount;
		}
		
		public TimeUnit getTimeUnit(){
			System.out.println(this.everyUnit);
			return this.everyUnit;
		}
		
		public static class Builder{
			private String name = "unnamed task";
			private Runnable delegate;
			private int everyAmount;
			private TimeUnit everyUnit;
			private int offsetAmount=0;
			
			public Builder every(int every, TimeUnit tu){
				this.everyAmount=every;
				this.everyUnit=tu;
				return this;
			}
			
			public Builder offset(int offset){
				this.offsetAmount=offset;
				return this;
			}
			public Builder runnable(Runnable r){
				this.delegate=r;
				return this;
			}
			public Builder name(String name){
				this.name=name;
				return this;
			}
			
			public ScheduledTask build(){
				ScheduledTask task= new ScheduledTask(delegate, everyAmount,offsetAmount, everyUnit);
				if(this.name!=null){
					task.name=this.name;
				}
				return task;
			}
			
		}
	}

}
