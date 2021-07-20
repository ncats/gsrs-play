package ix.ginas.initializers;

import ix.core.EntityProcessor;
import ix.core.adapters.EntityPersistAdapter;
import ix.core.factories.EntityProcessorFactory;
import ix.core.plugins.SchedulerPlugin.TaskListener;
import ix.core.util.CachedSupplier;
import ix.core.utils.executor.ProcessExecutionService;
import ix.core.utils.executor.ProcessExecutionService.CommonStreamSuppliers;
import ix.core.utils.executor.ProcessListener;
import ix.ginas.controllers.v1.SubstanceFactory;
import ix.ginas.models.v1.SubstanceReference;
import ix.ginas.models.v1.Substance;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import play.Play;

/**
 *
 * @author Egor Puzanov
 */

public class UpdateSubstanceReferenceTaskInitializer extends ScheduledTaskInitializer {

    private CachedSupplier<Set<EntityProcessor>> processors = CachedSupplier.of(()->
            EntityProcessorFactory.getInstance(Play.application())
                    .getRegisteredResourcesFor(SubstanceReference.class));

    @Override
    public void run(TaskListener l) {
        l.message("Initializing SubstanceReference Updater");
        ProcessListener listen = ProcessListener.onCountChange((sofar, total) -> {
            if (total != null) {
                l.message("Updated:" + sofar + " of " + total);
            } else {
                l.message("Updated:" + sofar);
            }
        });

        try {
            new ProcessExecutionService(5, 10).buildProcess(SubstanceReference.class)
                    .streamSupplier(CommonStreamSuppliers.allFor(SubstanceReference.class))
                    .consumer((SubstanceReference sr) -> {
                        String refuuid = sr.refuuid;
                        String approvalID = sr.approvalID;
                        String refPname = sr.refPname;
                        processors.get()
                            .forEach(processor ->{
                                try {
                                    processor.preUpdate(sr);
                                } catch(Exception ex) {
                                    Logger.getLogger(processor.getClass().getName()).log(Level.SEVERE, null, ex);
                                }
                            });
                        if (!(refuuid == sr.refuuid || (refuuid != null && sr.refuuid != null && refuuid.equals(sr.refuuid)))
                                || !(approvalID == sr.approvalID || (approvalID != null && sr.approvalID != null && approvalID.equals(sr.approvalID)))
                                || !(refPname == sr.refPname || (refPname != null && sr.refPname != null && refPname.equals(sr.refPname)))) {
                            sr.modified();
                            sr.forceUpdate();
                            EntityPersistAdapter.getInstance().reindex(sr);
                        }
                    })
                    .listener(listen)
                    .build()
                    .execute();
        } catch (IOException ex) {
            Logger.getLogger(UpdateSubstanceReferenceTaskInitializer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public String getDescription() {
        return "Update all SubstanceReferences in the database";
    }
}
