package ix.ginas.initializers;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

import ix.core.initializers.Initializer;
import ix.core.models.Principal;
import ix.core.plugins.CronExpressionBuilder;
import ix.core.plugins.SchedulerPlugin.ScheduledTask;
import ix.core.plugins.SchedulerPlugin.TaskListener;
import ix.core.util.TimeUtil;
import ix.core.util.Unchecked;
import ix.core.utils.executor.ProcessExecutionService;
import ix.ginas.controllers.GinasApp;
import ix.ginas.exporters.ExportMetaData;
import ix.ginas.exporters.ExportProcess;
import ix.ginas.exporters.ExportProcessFactory;
import ix.ginas.models.v1.Substance;

public class ScheduledExportTaskInitializer extends ScheduledTaskInitializer{


    
    private String username;

    private String name="Full Data Export";
    
    
    
    @Override
    public Initializer initializeWith(Map<String, ?> m) {
    	super.initializeWith(m);
        username=Optional.ofNullable((String)m.get("username")).orElse("admin");
        
        name=Optional.ofNullable((String)m.get("name"))
                     .orElse(name);
        return this;
    }

	@Override
	public Consumer<TaskListener> getRunner() {
		
		return (l)->{
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
//                    if(Thread.currentThread().isInterrupted()){
//                        System.out.println("THREAD WAS INTERRUPTED");
//                        emd.cancel();
//                    }
                }catch(InterruptedException e){
                    e.printStackTrace();
                    System.out.println("got interrupted exception");
                    emd.cancel();
                }
            }while(stillRunning);
            
        } catch (Exception e) {
            e.printStackTrace();
        }
		};
	}

	@Override
	public String getDescription() {
		return name + " for "+ username;
	}

}
