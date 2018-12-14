/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ix.ginas.initializers;

import ix.core.chem.StructureProcessorTask;
import ix.core.models.Structure;
import ix.core.plugins.SchedulerPlugin.TaskListener;
import ix.core.utils.executor.ProcessExecutionService;
import ix.core.utils.executor.ProcessExecutionService.CommonStreamSuppliers;
import ix.core.utils.executor.ProcessListener;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Mitch Miller
 */
public class StructureRecalcTaskInitializer extends ScheduledTaskInitializer
{

	@Override
	public void run(TaskListener l)
	{
		l.message("Initializing rehashing");
		ProcessListener listen = ProcessListener.onCountChange((sofar, total) ->
		{
			if (total != null)
			{
				l.message("Rehashed:" + sofar + " of " + total);
			} else
			{
				l.message("Rehashed:" + sofar);
			}
		});

		try
		{
			new ProcessExecutionService(5, 10).buildProcess(Structure.class)
					.streamSupplier(CommonStreamSuppliers.allFor(Structure.class))
					.consumer((Structure s) ->
					{
						s.properties.stream().forEach(k ->
						{
							k.delete(); //delete old stored properties
						});

						String molfileString = s.molfile;
						Structure newStructure;
						try
						{
							newStructure = new StructureProcessorTask.Builder()
									.mol(molfileString)
									.build()
									.instrument(); //compute new properties
							s.properties.addAll(newStructure.properties); //add properties
							s.save(); //save

						} catch (Exception ex)
						{
							System.err.println("Error reinitializing structure: " + ex.getMessage());
							ex.printStackTrace();
						}

					})
					//.before(ProcessExecutionService::nukeEverything)

					.listener(listen)
					.build()
					.execute();
		} catch (IOException ex)
		{
			Logger.getLogger(StructureRecalcTaskInitializer.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	@Override
	public String getDescription()
	{
		return "Regenerate the Lychi structure hash for all chemicals in the database";
	}

}
