/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ix.ginas.initializers;

import ix.core.UserFetcher;
import ix.core.initializers.Initializer;
import ix.core.plugins.SchedulerPlugin;
import ix.core.util.ConfigHelper;
import ix.core.util.TimeUtil;
import java.io.File;
import java.time.format.DateTimeFormatter;
import java.util.Map;

/**
 *
 * @author Mitch
 */
public class ReportTaskInitializer extends ScheduledTaskInitializer
{

	String baseName = "Base Report Name";
	String baseDescription = "Base Report Description";
	protected DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE;
	protected DateTimeFormatter formatterTime = DateTimeFormatter.ofPattern("HHmmss");
	protected String reportSubject = "validation of all substances";
	String path;

	protected static File getExportDirFor(String username)
	{
		return new File((String) ConfigHelper.getOrDefault("export.path.root", "exports"), username);
	}

	@Override
	public void run(SchedulerPlugin.TaskListener l)
	{
	}

	@Override
	public String getDescription()
	{
		return baseDescription;
	}

	@Override
	public Initializer initializeWith(Map<String, ?> m)
	{
		super.initializeWith(m);
//		m.keySet().forEach((key) ->
//		{
//			System.out.println(String.format("key: %s; value: %s", key, m.get(key).toString()));
//		});
		path = (String) m.get("output.path");

		return this;
	}

	/**
	 * Returns the File used to output the report
	 *
	 * @return
	 */
	public File getWriteFile()
	{
		if (path == null)
		{
			//path = "reports/" + reportSubject + "-%DATE%-%TIME%.txt";
			String userName = UserFetcher.getActingUser(true).username;
			if (userName == null || userName.length() == 0)
			{
				userName = "admin";
			}
			System.out.println("set userName: " + userName);
			path = getExportDirFor(userName).getAbsolutePath() + "/" + reportSubject + "-%DATE%-%TIME%.txt";
		}

		String date = formatter.format(TimeUtil.getCurrentLocalDateTime());
		String time = formatterTime.format(TimeUtil.getCurrentLocalDateTime());

		String fpath = path.replace("%DATE%", date)
				.replace("%TIME%", time);

		File f = new File(fpath);
		File p = f.getParentFile();
		System.out.println("calling mkdirs on " + p.getAbsolutePath());
		p.mkdirs();
		return f;
	}

}
