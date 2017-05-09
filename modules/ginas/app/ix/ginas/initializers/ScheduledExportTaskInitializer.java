package ix.ginas.initializers;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import ix.core.adapters.EntityPersistAdapter;
import ix.core.initializers.Initializer;
import ix.core.models.Principal;
import ix.core.plugins.CronExpressionBuilder;
import ix.core.plugins.SchedulerPlugin.ScheduledTask;
import ix.core.util.TimeUtil;
import ix.core.util.Unchecked;
import ix.core.plugins.TextIndexerPlugin;
import ix.core.utils.executor.ProcessExecutionService;
import ix.core.utils.executor.ProcessExecutionService.Before;
import ix.core.utils.executor.ProcessExecutionService.CommonConsumers;
import ix.core.utils.executor.ProcessExecutionService.CommonStreamSuppliers;
import ix.core.utils.executor.ProcessListener;
import ix.ginas.controllers.GinasApp;
import ix.ginas.exporters.ExportMetaData;
import ix.ginas.exporters.ExportProcess;
import ix.ginas.exporters.ExportProcessFactory;
import ix.ginas.models.v1.Substance;
import play.Application;
import play.Play;

public class ScheduledExportTaskInitializer implements Initializer{

    @Override
    public void onStart(Application app) {

        
        ScheduledTask.of((l) -> {
            System.out.println("Running export");
            try {
                
                Principal user = new Principal("admin", null);
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

                p.run(out -> Unchecked.uncheck(() -> GinasApp.getSubstanceExporterFor(extension, out, publicOnlyBool)))
                 .get();
                
            } catch (Exception e) {
                e.printStackTrace();
            }
        })
        .dailyAtHourAndMinute(3, 10)
        .description("Full Data Export")
        .submit();                      //submit to scheduler
        
    }

}
