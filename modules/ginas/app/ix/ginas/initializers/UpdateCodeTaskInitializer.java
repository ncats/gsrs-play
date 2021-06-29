package ix.ginas.initializers;

import ix.core.EntityProcessor;
import ix.core.adapters.EntityPersistAdapter;
import ix.core.factories.EntityProcessorFactory;
import ix.core.models.Group;
import ix.core.plugins.SchedulerPlugin.TaskListener;
import ix.core.util.CachedSupplier;
import ix.core.utils.executor.ProcessExecutionService;
import ix.core.utils.executor.ProcessExecutionService.CommonStreamSuppliers;
import ix.core.utils.executor.ProcessListener;
import ix.ginas.models.v1.Code;
import java.io.IOException;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import play.Play;

/**
 *
 * @author Egor Puzanov
 */

public class UpdateCodeTaskInitializer extends ScheduledTaskInitializer {

    private CachedSupplier<Set<EntityProcessor>> processors = CachedSupplier.of(()->
            EntityProcessorFactory.getInstance(Play.application())
                    .getRegisteredResourcesFor(Code.class));

    @Override
    public void run(TaskListener l) {
        l.message("Initializing Code Updater");
        ProcessListener listen = ProcessListener.onCountChange((sofar, total) -> {
            if (total != null) {
                l.message("Updated:" + sofar + " of " + total);
            } else {
                l.message("Updated:" + sofar);
            }
        });

        try {
            new ProcessExecutionService(5, 10).buildProcess(Code.class)
                    .streamSupplier(CommonStreamSuppliers.allFor(Code.class))
                    .consumer((Code c) -> {
                        String comments = c.comments;
                        c.comments = "";
                        String url = c.url;
                        c.url = "";
                        Set<Group> access = c.getAccess();
                        processors.get()
                            .forEach(processor ->{
                                try {
                                    processor.preUpdate(c);
                                } catch(Exception ex) {
                                    Logger.getLogger(processor.getClass().getName()).log(Level.SEVERE, null, ex);
                                }
                            });
                        if ((c.comments == null || c.comments.trim().isEmpty()) && !(comments == null || comments.trim().isEmpty())) {
                            c.comments = comments;
                        }
                        if ((c.url == null || c.url.trim().isEmpty()) && !(url == null || url.trim().isEmpty())) {
                            c.url = url;
                        }
                        if (!(comments == c.comments || (comments != null && c.comments != null && comments.equals(c.comments)))
                                || !(url == c.url || (url != null && c.url != null && url.equals(c.url)))
                                || !access.equals(c.getAccess())) {
                            c.modified();
                            c.forceUpdate();
                            EntityPersistAdapter.getInstance().reindex(c);
                        }
                    })
                    .listener(listen)
                    .build()
                    .execute();
        } catch (IOException ex) {
            Logger.getLogger(UpdateCodeTaskInitializer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public String getDescription() {
        return "Update all Codes in the database";
    }
}
