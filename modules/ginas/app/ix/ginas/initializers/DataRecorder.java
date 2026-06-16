package ix.ginas.initializers;

import ix.core.initializers.Initializer;
import ix.core.initializers.ScheduledTaskInitializer;
import ix.core.plugins.CronExpressionBuilder;
import ix.core.plugins.SchedulerPlugin;
import ix.core.util.KeepLastList;
import ix.core.util.TimeUtil;
import ix.utils.Util;
import play.Application;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class DataRecorder implements Initializer {

    private static final int DEFAULT_NUM_RECORDS= 10; // with default cron of very 30 sec this makes last 5 mins snapshots
    private String cron= CronExpressionBuilder.builder()
            .every(30, TimeUnit.SECONDS)
            .build();

    private File logFile = new File("logs/dataRecorder.log");

    private DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;

    private Lock lock = new ReentrantLock();

    private boolean enabled;

    KeepLastList<String> data;
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

        String numRecordsToKeep = (String) m.get("keep_record_count");
        Integer value=null;
        if(numRecordsToKeep !=null){
            try {
                value = Integer.parseInt(numRecordsToKeep);
            }catch(Exception e){
                e.printStackTrace();
                value = DEFAULT_NUM_RECORDS;
            }
        }else{
            value = DEFAULT_NUM_RECORDS;
        }

        data = new KeepLastList<>(value);
        return this;
    }

    @Override
    public void onStart(Application app) {


        SchedulerPlugin.ScheduledTask.of((l) -> {
            lock.lock();

            try {
                try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
                     PrintStream ps = new PrintStream(baos, true, "utf-8");
                ){
                    ps.println(" ==========================================");
                    ps.println(formatter.format(TimeUtil.getCurrentLocalDateTime()));
                    ps.println(" ==========================================");
                    Util.printAllExecutingStackTraces(ps);
                    ps.println("==========================================");

                    data.add(new String(baos.toByteArray(), StandardCharsets.UTF_8));
                }catch(IOException e){

                }
                File tmpFile = new File(logFile.getParentFile(), logFile.getName()+".tmp");

                try (PrintStream out = new PrintStream(new BufferedOutputStream(new FileOutputStream(tmpFile)))) {


                    for(String s: data){
                        out.println(s);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

                try{
                    Files.copy(tmpFile.toPath(), logFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                }catch(IOException e){
                    e.printStackTrace();
                }
            }finally{
                lock.unlock();
            }
        })
                .atCronTab(cron)
                .description("Record Last X stack traces like an airplane's Flight Data Recorder" + logFile.getPath())
                ._to(st -> enabled? st.enable() : st.disable())
                .submit();
    }
}
