package ix.ginas.initializers;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;
import java.util.function.Supplier;

import ix.core.initializers.Initializer;
import ix.core.models.Principal;
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
        additionalInitializeWith(m);
        
        return this;
    }
    
    /*
     * (non-Javadoc)
     * @see ix.ginas.initializers.ScheduledTaskInitializer#run(ix.core.plugins.SchedulerPlugin.TaskListener)
     * 

(l) -> {
+            System.out.println("Running export");
+            try {
+                
+                Principal user = new Principal(username, null);
+                String collectionID = getCollectionID();
+
+                String extension = getExtension();
+                boolean publicOnlyBool = publicOnly();
+
+                ExportMetaData emd = new ExportMetaData(collectionID, null, user, publicOnlyBool, extension)
+                        .onTotalChanged((c)->{
+                            l.message("Exported " + c + " records");
+                        });
+                
+
+                LocalDate ld=TimeUtil.getCurrentLocalDate();
+                String date=ld.format(DateTimeFormatter.ISO_LOCAL_DATE);
+                String fname = fileNameGenerator().apply(date) + "." + extension;
+                
+                emd.setDisplayFilename(fname);
+                emd.originalQuery = null;
+                
+                
+
+                ExportProcess p = exportFactorySupplier().get().getProcess(emd,
+                        ProcessExecutionService.CommonStreamSuppliers.allForDeep(Substance.class));
+
+                p.run(out -> Unchecked.uncheck(() -> GinasApp.getSubstanceExporterFor(extension, out, publicOnlyBool)))
+                 .get();
+                
+            } catch (Exception e) {
+                e.printStackTrace();
+            }
+        }
     */

	@Override
	public void run(TaskListener l) {
		
		System.out.println("Running export");
        try {
            
            Principal user = new Principal(username, null);
            String collectionID = getCollectionID();
            String extension = getExtension();
            boolean publicOnlyBool = publicOnly();

            ExportMetaData emd = new ExportMetaData(collectionID, null, user, publicOnlyBool, extension)
                    .onTotalChanged((c)->{
                        l.message("Exported " + c + " records");
                    });
            

            LocalDate ld=TimeUtil.getCurrentLocalDate();
            String date=ld.format(DateTimeFormatter.ISO_LOCAL_DATE);
            String fname = fileNameGenerator().apply(date) + "." + extension;
            
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
	}

	@Override
	public String getDescription() {
		return name + " for "+ username;
	}
	
    protected void additionalInitializeWith(Map<String, ?> m){

    }


    protected String getExtension(){
        return "gsrs";
    }

    protected String getCollectionID(){
        return "export-all-gsrs";
    }

    public boolean publicOnly(){
        return false;
    }

    public Function<String, String> fileNameGenerator(){
        return date ->  "auto-export-" + date;
    }

    protected Supplier<ExportProcessFactory> exportFactorySupplier(){
        return ()-> new ExportProcessFactory();
    }


}
