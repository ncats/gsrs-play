package ix.ginas.initializers;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.function.Consumer;

import play.Application;

import ix.core.initializers.Initializer;
import ix.core.plugins.CronExpressionBuilder;
import ix.core.plugins.SchedulerPlugin;
import ix.core.plugins.SchedulerPlugin.ScheduledTask;
import ix.core.plugins.SchedulerPlugin.TaskListener;
import ix.core.util.TimeUtil;
import ix.utils.Util;

public abstract class ScheduledTaskInitializer implements Initializer {

    private String cron=CronExpressionBuilder.builder()
            .everyDay()
            .atHourAndMinute(2, 04)
            .build();
        
    private boolean enabled;
    
    
    public String getCron(){
    	return cron;
    }
    
    public void setCron(String cron){
    	this.cron=cron;
    }
    
    public boolean getEnabled(){
    	return enabled;
    }
    
    public void setEnabled(boolean en){
    	this.enabled=en;
    }
    
    
    @Override
    public Initializer initializeWith(Map<String, ?> m) {

        String suppliedCron = (String)m.get("cron");
        if(suppliedCron !=null){
            this.cron = suppliedCron;
        }
        Object autoRun = m.get("autorun");
        if(autoRun ==null){
            enabled = false;
        }else if(autoRun instanceof Boolean){
            enabled = (Boolean) autoRun;
        }else {
            enabled = Boolean.parseBoolean(autoRun.toString());
        }
        return this;
    }
    
    public Consumer<TaskListener> getRunner(){
    	return this::run;
    }
    
    public abstract void run(TaskListener l);
    
    public abstract String getDescription();
    
    @Override
    public void onStart(Application app) {
    	createTask()
        ._to(st -> enabled? st.enable() : st.disable())
        .submit();
    }
    
    public ScheduledTask createTask(){
    	return SchedulerPlugin.ScheduledTask.of(getRunner())
    	        .atCronTab(cron)
    	        .description(getDescription());
    }
    
}
