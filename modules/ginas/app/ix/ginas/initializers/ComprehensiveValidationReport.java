package ix.ginas.initializers;

import ix.core.UserFetcher;
import ix.core.controllers.PrincipalFactory;
import ix.core.initializers.Initializer;
import ix.core.models.Principal;
import ix.core.plugins.SchedulerPlugin;
import ix.core.util.TimeUtil;
import ix.core.utils.executor.ProcessExecutionService;
import ix.core.utils.executor.ProcessListener;
import ix.core.validator.ValidationMessage;
import ix.core.validator.ValidationResponse;
import ix.ginas.models.v1.Substance;
import ix.ginas.utils.GinasProcessingStrategy;
import ix.ginas.utils.validation.DefaultSubstanceValidator;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import play.Logger;

public class ComprehensiveValidationReport extends ReportTaskInitializer {

	private String reportCharSet;
	private Boolean includeInfoLevelMessages = false;

	public ComprehensiveValidationReport() {
		reportSubject = "comprehensive_validation";
	}

	@Override
	public void run(SchedulerPlugin.TaskListener l) {
		l.message("Initializing comprehensive validation report");
		ProcessListener listen = ProcessListener.onCountChange((sofar, total)
				-> {
			if (total != null) {
				l.message("Processed: " + sofar + " of " + total);
			}
			else {
				l.message("Processed: " + sofar);
			}
		});

		File reportFile = getWriteFile();
		try {
			Logger.trace(String.format("Going to write validation report to file %s using charset %s",
					reportFile.getAbsolutePath(), reportCharSet));
			final int recordLimit = 2000;
			final AtomicInteger record = new AtomicInteger(0);
			Principal admin = PrincipalFactory.byUserName("admin");//  UserFetcher.getActingUser();
			Logger.trace("admin.username: " + admin.username);
			try (final PrintWriter writer = new PrintWriter(Files.newBufferedWriter(reportFile.toPath(), Charset.forName(reportCharSet)))) {
				new ProcessExecutionService(5, 10).buildProcess(Object.class)
						//.streamSupplier(ProcessExecutionService.CommonStreamSuppliers.allFor(Substance.class))
						.streamSupplier(ProcessExecutionService.CommonStreamSuppliers.allBackups())
						.filter(s -> {return s instanceof Substance ;} )
						.filter(s -> record.getAndIncrement()< recordLimit )
						.consumer( (Object o)
								-> {
							try {
								Substance s = (Substance ) o;
								UserFetcher.setLocalThreadUser(admin);
								record.incrementAndGet();
								
								DefaultSubstanceValidator sv = DefaultSubstanceValidator
										.NEW_SUBSTANCE_VALIDATOR(GinasProcessingStrategy.ACCEPT_APPLY_ALL_WARNINGS().markFailed());
								ValidationResponse<Substance> response = sv.validate(s, s);
								writer.println("Substance: " + s.getUuid());
								response.getValidationMessages().forEach(vm -> {
									if (includeInfoLevelMessages || !vm.getMessageType().equals(ValidationMessage.MESSAGE_TYPE.INFO)) {
										writer.println(String.format("    level: %s; message: %s", vm.getMessageType().toString(), vm.getMessage()));
									}
								});

								writer.flush();
							} catch (Throwable other) {
								Logger.error("Error during report generation: " + other.getMessage());
								other.printStackTrace();
							}
						})
						//.before(ProcessExecutionService::nukeEverything)
						.listener(listen)
						.build()
						.execute();
			}
		} catch (IOException ex) {
			ex.printStackTrace();
			l.message("Error: " + ex.getMessage());
			Logger.error("IO error in validation report: " + ex.getMessage());
		} catch (Exception ex) {
			Logger.error("error in validation report: " + ex.getMessage());
			ex.printStackTrace();
			l.message("Error: " + ex.getMessage());
		}
	}

	@Override
	public String getDescription() {
		return "Creates a report of validation errors and warnings for all substances";
	}

	@Override
	public Initializer initializeWith(Map<String, ?> m) {
		super.initializeWith(m);
		reportCharSet = (String) m.get("reportCharSet");
		path = (String) m.get("output.path");

		if (path == null) {
			path = "reports/" + "all_validations" + "-%DATE%-%TIME%.txt";
		}
		if (m.containsKey("IncludeInfo")) {
			includeInfoLevelMessages = (Boolean) m.get("IncludeInfoLevelMessages");
		}

		return this;
	}

	/**
	 * Returns the File used to output the report
	 *
	 * @return
	 */
	@Override
	public File getWriteFile() {
		String date = formatter.format(TimeUtil.getCurrentLocalDateTime());
		String time = formatterTime.format(TimeUtil.getCurrentLocalDateTime());

		String fpath = path.replace("%DATE%", date)
				.replace("%TIME%", time);

		File f = new File(fpath);
		File p = f.getParentFile();
		p.mkdirs();
		return f;
	}

}
