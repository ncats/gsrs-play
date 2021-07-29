/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ix.ginas.initializers;

import java.io.IOException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import ix.core.initializers.Initializer;
import ix.core.models.BaseModel;
import ix.core.plugins.SchedulerPlugin.TaskListener;
import ix.core.processors.BackupProcessor;
import ix.core.util.EntityUtils;
import ix.core.util.EntityUtils.EntityInfo;
import ix.core.utils.executor.ProcessExecutionService;
import ix.core.utils.executor.ProcessExecutionService.CommonStreamSuppliers;
import ix.core.utils.executor.ProcessListener;

/**
 *
 * @author Tyler
 */
public class ResaveBackupsTaskInitializer extends ScheduledTaskInitializer
{

	EntityInfo entityToResaveBackups = null;

	@Override
	public void run(TaskListener l)
	{
		resaveBackups(entityToResaveBackups.getEntityClass(), l);
	}

	private <T extends BaseModel> void resaveBackups(Class<T> cls, TaskListener l)
	{
		l.message("Initializing creating new backups");
		ProcessListener listen = ProcessListener.onCountChange((sofar, total) ->
		{

			if (total != null)
			{
				l.message("Resaving backups:" + sofar + " of " + total);
			} else
			{
				l.message("Resaving backups:" + sofar);
			}
		});

		try
		{
			new ProcessExecutionService(5, 10).buildProcess(cls)
					.streamSupplier(CommonStreamSuppliers.allFor(cls))
					.consumer((T s) ->
					{
						try{
							if( s instanceof ix.ginas.models.v1.Substance){
								String id = ((ix.ginas.models.v1.Substance) s).uuid.toString();
								play.Logger.debug("going to process substance for backup: " + id);
							}
						BackupProcessor.getInstance()
								.postUpdate(s);
						}
						catch(Exception ex){
							play.Logger.error("Error mustering substance for backup: ", ex);
						}
					})
					.listener(listen)
					.build()
					.execute();
		} catch (IOException ex)
		{
			Logger.getLogger(ResaveBackupsTaskInitializer.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	@Override
	public String getDescription()
	{
		return "Resave all backups of type " + entityToResaveBackups.getName() + " to the database backups.";
	}

	@Override
	public Initializer initializeWith(Map<String, ?> m)
	{
		super.initializeWith(m);
		String entityClass = (String) m.get("entityClass");
		try
		{
			entityToResaveBackups = EntityUtils.getEntityInfoFor(entityClass);
		} catch (ClassNotFoundException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return this;
	}

}