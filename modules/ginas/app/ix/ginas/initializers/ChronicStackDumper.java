package ix.ginas.initializers;

import ix.core.initializers.Initializer;
import ix.core.plugins.CronExpressionBuilder;
import ix.core.plugins.SchedulerPlugin;
import ix.core.plugins.SchedulerPlugin.TaskListener;
import ix.core.util.TimeUtil;
import ix.utils.Util;
import play.Application;

import java.io.*;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;

/**
 * Prints all currently running stacktraces to a log file
 * on a regular basis.  If not configured, the log will be invoked
 * every 5 minutes.
 *
 * Created by katzelda on 6/13/17.
 */
public class ChronicStackDumper extends ScheduledTaskInitializer{

    private File logFile = new File("logs/all-running-stacktraces.log");

    private DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;

    private Lock lock = new ReentrantLock();


    @Override
    public Initializer initializeWith(Map<String, ?> m) {
    	super.initializeWith(m);
    	
    	if(this.getCron()==null){
    		this.setCron(CronExpressionBuilder.builder()
            					 .every(5, TimeUnit.MINUTES)
                                 .build());
    	}
        String path = (String)m.get("output.path");
        if(path !=null){
            logFile = new File(path);
        }

        String format = (String)m.get("dateFormat");
        if(format !=null){
            formatter = DateTimeFormatter.ofPattern(format);
        }

        return this;
    }


	@Override
	public void run(TaskListener l) {
		    lock.lock();
            try {
                try (PrintStream out = new PrintStream(new BufferedOutputStream(new FileOutputStream(logFile, true)))) {
                    out.println(" ==========================================");
                    out.println(formatter.format(TimeUtil.getCurrentLocalDateTime()));
                    out.println(" ==========================================");

                    Util.printAllExecutingStackTraces(out);
                    out.println("==========================================");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }finally{
                lock.unlock();
            }
	}

	@Override
	public String getDescription() {
		return "Log all Executing Stack Traces to " + logFile.getPath();
	}
}
