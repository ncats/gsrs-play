package ix.ginas.initializers;

import ix.core.initializers.Initializer;
import ix.core.plugins.CronExpressionBuilder;
import ix.core.plugins.SchedulerPlugin;
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

/**
 * Prints all currently running stacktraces to a log file
 * on a regular basis.  If not configured, the log will be invoked
 * every 5 minutes.
 *
 * Created by katzelda on 6/13/17.
 */
public class ChronicStackDumper implements Initializer{

    private String cron= CronExpressionBuilder.builder()
            .every(5, TimeUnit.MINUTES)
            .build();

    private File logFile = new File("logs/all-running-stacktraces.log");

    private DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;

    private Lock lock = new ReentrantLock();

    private boolean enabled;

    @Override
    public Initializer initializeWith(Map<String, ?> m) {

        String suppliedCron = (String)m.get("cron");
        if(suppliedCron !=null){
            this.cron = suppliedCron;
        }
        String path = (String)m.get("output.path");
        if(path !=null){
            logFile = new File(path);
        }

        String format = (String)m.get("dateFormat");
        if(format !=null){
            formatter = DateTimeFormatter.ofPattern(format);
        }

        Object autoRun = m.get("autorun");
        if(autoRun instanceof Boolean){
            enabled = (Boolean) autoRun;
        }else {
            enabled = Boolean.parseBoolean((String) m.get("autorun"));
        }
        return this;
    }

    @Override
    public void onStart(Application app) {


        SchedulerPlugin.ScheduledTask.of((l) -> {
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
        })
        .atCronTab(cron)
                .description("Log all Executing Stack Traces to " + logFile.getPath())
                ._to(st -> enabled? st.enable() : st.disable())
        .submit();
    }
}
