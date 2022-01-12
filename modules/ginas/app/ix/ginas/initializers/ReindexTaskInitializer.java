package ix.ginas.initializers;

import java.util.function.Consumer;

import ix.core.adapters.EntityPersistAdapter;
import ix.core.initializers.Initializer;
import ix.core.models.UserProfile;
import ix.core.plugins.CronExpressionBuilder;
import ix.core.plugins.SchedulerPlugin.ScheduledTask;
import ix.core.plugins.SchedulerPlugin.TaskListener;
import ix.core.plugins.TextIndexerPlugin;
import ix.core.utils.executor.ProcessExecutionService;
import ix.core.utils.executor.ProcessExecutionService.CommonConsumers;
import ix.core.utils.executor.ProcessExecutionService.CommonStreamSuppliers;
import ix.core.utils.executor.ProcessListener;
import ix.ginas.controllers.GinasApp;
import ix.ginas.controllers.GinasAppAdmin;
import ix.ginas.utils.GinasGlobal;
import ix.ncats.controllers.auth.Authentication;
import play.Application;
import play.Play;

public class ReindexTaskInitializer extends ScheduledTaskInitializer{


	@Override
	public void run(TaskListener l){
		play.Logger.debug("reindex task run");
            try {
                l.message("Initializing reindexing");
                ProcessListener listen = ProcessListener.onCountChange((sofar,total)->{
                    if(total!=null){
                        l.message("Indexed:" + sofar + " of " + total);
                    }else{
                        l.message("Indexed:" + sofar);
                    }
                })
                .and(GinasApp.getReindexListener())
                .and(Play.application().plugin(TextIndexerPlugin.class).getIndexer())
                .and(EntityPersistAdapter.getInstance().getProcessListener());
                
                new ProcessExecutionService(5, 10).buildProcess(Object.class)
                        .consumer(CommonConsumers.REINDEX_FAST)
                        .streamSupplier(CommonStreamSuppliers.allBackups())
                        .before(ProcessExecutionService::nukeEverything)
                        .listener(listen)
                        .build()
                        .execute();
                
                
                
            } catch (Exception e) {
                e.printStackTrace();
            }
	}

	@Override
	public String getDescription() {		
		return "Reindex all core entities from backup tables";
	}
	
	@Override
	public ScheduledTask createTask(){
		return super.createTask()
			.wrap((r)->{
	            GinasGlobal.runWithIntercept(r, (req)->{
	                UserProfile p = Authentication.getUserProfile();
	                return !GinasAppAdmin.isAdminRequest(req, p);
	            });
	        });
	}

}
