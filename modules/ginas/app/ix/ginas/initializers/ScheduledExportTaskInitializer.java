package ix.ginas.initializers;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import ix.core.initializers.Initializer;
import ix.core.models.Principal;
import ix.core.plugins.CronExpressionBuilder;
import ix.core.plugins.SchedulerPlugin.ScheduledTask;
import ix.core.util.TimeUtil;
import ix.core.util.Unchecked;
import ix.core.utils.executor.ProcessExecutionService;
import ix.ginas.controllers.GinasApp;
import ix.ginas.exporters.ExportMetaData;
import ix.ginas.exporters.ExportProcess;
import ix.ginas.exporters.ExportProcessFactory;
import ix.ginas.models.v1.Substance;
import play.Application;

public class ScheduledExportTaskInitializer implements Initializer{


    private String cron=CronExpressionBuilder.builder()
            .everyDay()
            .atHourAndMinute(2, 04)
            .build();
    
    private String username;

    private String name="Full Data Export";
    
    private boolean enabled;
    
    
    @Override
    public Initializer initializeWith(Map<String, ?> m) {
        username=Optional.ofNullable((String)m.get("username")).orElse("admin");
        enabled=!((String)(m.get("autorun")+"")).equals("false");
        cron=Optional.ofNullable((String)m.get("cron"))
                     .orElse(cron);
        
        name=Optional.ofNullable((String)m.get("name"))
                     .orElse(name);
        return this;
    }

    @Override
    public void onStart(Application app) {

        
        ScheduledTask.of((l) -> {
            System.out.println("Running export");
            try {
                
                Principal user = new Principal(username, null);
                String collectionID = "export-all-gsrs";
                String extension = "gsrs";
                boolean publicOnlyBool = false;

                ExportMetaData emd = new ExportMetaData(collectionID, null, user, publicOnlyBool, extension)
                        .onTotalChanged((c)->{
                            l.message("Exported " + c + " records");
                        });
                

                LocalDate ld=TimeUtil.getCurrentLocalDate();
                String date=ld.format(DateTimeFormatter.ISO_LOCAL_DATE);
                String fname = "auto-export-" + date + ".gsrs";
                
                emd.setDisplayFilename(fname);
                emd.originalQuery = null;
                
                

                ExportProcess p = new ExportProcessFactory().getProcess(emd,
                        ProcessExecutionService.CommonStreamSuppliers.allForDeep(Substance.class));

                Future<?> future = p.run(out -> Unchecked.uncheck(() -> GinasApp.getSubstanceExporterFor(extension, out, publicOnlyBool)));
                boolean stillRunning = true;
                do{
                    try{
                        future.get(3, TimeUnit.SECONDS);
                        stillRunning=false;
                    }catch(TimeoutException ignored){
//                        if(Thread.currentThread().isInterrupted()){
//                            System.out.println("THREAD WAS INTERRUPTED");
//                            emd.cancel();
//                        }
                    }catch(InterruptedException e){
                        e.printStackTrace();
                        System.out.println("got interrupted exception");
                        emd.cancel();
                    }
                }while(stillRunning);
                
            } catch (Exception e) {
                e.printStackTrace();
            }
        })
        .atCronTab(cron)
        ._to(st->(enabled)?st.enable():st.disable())
        .description(name + " for "+ username)
        .submit();          //submit to scheduler
        
    }

}
