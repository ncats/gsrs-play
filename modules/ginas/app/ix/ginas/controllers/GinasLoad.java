package ix.ginas.controllers;


import be.objectify.deadbolt.java.actions.Dynamic;
import ix.core.controllers.EntityFactory;
import ix.core.controllers.PayloadFactory;
import ix.core.controllers.ProcessingJobFactory;
import ix.core.controllers.v1.GsrsApiUtil;
import ix.core.models.Payload;
import ix.core.models.ProcessingJob;
import ix.core.plugins.GinasRecordProcessorPlugin;
import ix.core.plugins.PayloadPlugin;
import ix.core.plugins.PayloadPlugin.PayloadPersistType;
import ix.core.search.SearchResult;
import ix.core.search.text.TextIndexer;
import ix.core.search.text.TextIndexer.Facet;
import ix.core.util.CachedSupplier;
import ix.ginas.utils.DefaultSdfLoader;
import ix.ginas.utils.GinasSDFUtils;
import ix.ginas.utils.GinasSDFUtils.GinasSDFExtractor;
import ix.ginas.utils.GinasSDFUtils.GinasSDFExtractor.FieldStatistics;
import ix.ginas.utils.GinasUtils;
import ix.ncats.controllers.App;
import ix.ncats.controllers.FacetDecorator;
import ix.ncats.controllers.security.IxDynamicResourceHandler;
import ix.utils.CallableUtil.TypedCallable;
import ix.utils.Util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import play.Application;
import play.Logger;
import play.Play;
import play.data.DynamicForm;
import play.data.Form;
import play.mvc.Result;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import play.mvc.Results;

public class GinasLoad extends App {
	
	public static CachedSupplier<LoadConfiguration> config = 
				CachedSupplier.of(()->new LoadConfiguration(Play.application())); 
	
	public static class LoadConfiguration{
		public boolean OLD_LOAD ;
		public boolean ALLOW_LOAD;
		public boolean ALLOW_REBUILD;
		
		public LoadConfiguration(Application app){
			OLD_LOAD = app.configuration()
					.getString("ix.ginas.loader", "new").equalsIgnoreCase("old");
			ALLOW_LOAD = app.configuration()
					.getBoolean("ix.ginas.allowloading", true);
			ALLOW_REBUILD = app.configuration()
					.getBoolean("ix.ginas.allowindexrebuild", true);
		}
	}
	
	

	public static final String[] ALL_FACETS = { "Job Status" };

	static CachedSupplier<GinasRecordProcessorPlugin> ginasRecordProcessorPlugin = CachedSupplier.of(()->Play
			.application().plugin(GinasRecordProcessorPlugin.class));
	
	static CachedSupplier<PayloadPlugin> payloadPlugin = CachedSupplier.of(()->Play.application().plugin(
			PayloadPlugin.class));
	
	

	public static Result error(int code, String mesg) {
		return ok(ix.ginas.views.html.error.render(code, mesg));
	}

	public static Result _notFound(String mesg) {
		return notFound(ix.ginas.views.html.error.render(404, mesg));
	}

	public static Result _badRequest(String mesg) {
		return badRequest(ix.ginas.views.html.error.render(400, mesg));
	}

	public static Result _internalServerError(Throwable t) {
		t.printStackTrace();
		return internalServerError(ix.ginas.views.html.error.render(500,
				"Internal server error: " + t.getMessage()));
	}

	static FacetDecorator[] decorate(Facet... facets) {
		List<FacetDecorator> decors = new ArrayList<FacetDecorator>();
		// override decorator as needed here
		for (int i = 0; i < facets.length; ++i) {
			decors.add(new GinasFacetDecorator(facets[i]));
		}
		return decors.toArray(new FacetDecorator[0]);
	}

	static class GinasFacetDecorator extends FacetDecorator {
		GinasFacetDecorator(Facet facet) {
			super(facet, true, 6);
		}
	}

	@Dynamic(value = IxDynamicResourceHandler.IS_ADMIN, handler = ix.ncats.controllers.security.IxDeadboltHandler.class)
	public static Result load() {
		if (!GinasLoad.config.get().ALLOW_LOAD) {
			return redirect(ix.ginas.controllers.routes.GinasApp.index());
		}
		return ok(ix.ginas.views.html.admin.load.render());
	}

	@Dynamic(value = IxDynamicResourceHandler.IS_ADMIN, handler = ix.ncats.controllers.security.IxDeadboltHandler.class)
	public static Result loadJsonViaAPI() {
		if (!GinasLoad.config.get().ALLOW_LOAD) {
			return badRequest("Invalid request!");
		}

		DynamicForm requestData = Form.form().bindFromRequest();
		String type = requestData.get("file-type");
		String preserveAudit = requestData.get("preserve-audit");
		Logger.info("type =" + type);
		try {

			Class<?> persister = ix.ginas.utils.GinasUtils.GinasSubstancePersister.class;

			if(preserveAudit!=null){
				persister = ix.ginas.utils.GinasUtils.GinasSubstanceForceAuditPersister.class;
			}

			Payload payload = payloadPlugin.get().parseMultiPart("file-name",
					request(), PayloadPersistType.TEMP);
			switch (type) {
				case "JSON":
					Logger.info("JOS =" + type);

					GinasRecordProcessorPlugin.PayloadProcessor payloadProcessor = ginasRecordProcessorPlugin.get()
							.submit(payload,
									GinasUtils.GinasDumpExtractor.class,
									persister);
					String id = payloadProcessor.key;

					return redirect(ix.core.controllers.v1.routes.LoadController.monitor(id));

//				case "SD-default":
//					Logger.info("SD-default =" + type);
//					payload.save();
//					return ix.ginas.controllers.GinasLoad.loadSDFWithDefaultMappings(payload.id.toString());
//				case "SD": //this is the full custom version
//					Logger.info("SD =" + type);
//
//					payload.save();
//					Map<String, FieldStatistics> m = GinasSDFExtractor
//							.getFieldStatistics(payload, 100);
//					return ok(ix.ginas.views.html.admin.sdfimportmapping.render(
//							payload, new ArrayList<FieldStatistics>(m.values())));
				default:
					return badRequest("only gsrs files currently supported");
			}

		} catch (Exception ex) {
			return _internalServerError(ex);
		}

	}
	@Dynamic(value = IxDynamicResourceHandler.IS_ADMIN, handler = ix.ncats.controllers.security.IxDeadboltHandler.class)
	public static Result loadJSON() {
		if (!GinasLoad.config.get().ALLOW_LOAD) {
			return badRequest("Invalid request!");
		}

		DynamicForm requestData = Form.form().bindFromRequest();
		String type = requestData.get("file-type");
		String preserveAudit = requestData.get("preserve-audit");
		Logger.info("type =" + type);
		try {
			
			Class<?> persister = ix.ginas.utils.GinasUtils.GinasSubstancePersister.class;
			
			if(preserveAudit!=null){
				persister = ix.ginas.utils.GinasUtils.GinasSubstanceForceAuditPersister.class;
			}
			
			Payload payload = payloadPlugin.get().parseMultiPart("file-name",
					request(), PayloadPersistType.TEMP);
			switch (type) {
				case "JSON":
					Logger.info("JOS =" + type);
					
						String id = ginasRecordProcessorPlugin.get()
								.submit(payload,
										ix.ginas.utils.GinasUtils.GinasDumpExtractor.class,
										persister).key;
						return redirect(ix.ginas.controllers.routes.GinasLoad
								.monitorProcess(id));
				case "SD-default":
					Logger.info("SD-default =" + type);
					payload.save();
					return ix.ginas.controllers.GinasLoad.loadSDFWithDefaultMappings(payload.id.toString());
				case "SD": //this is the full custom version
					Logger.info("SD =" + type);
	
					payload.save();
					Map<String, FieldStatistics> m = GinasSDFExtractor
							.getFieldStatistics(payload, 100);
					return ok(ix.ginas.views.html.admin.sdfimportmapping.render(
							payload, new ArrayList<FieldStatistics>(m.values())));
				default:
					return badRequest("Neither json-dump nor "
							+ "sd-file is specified!");
			}

		} catch (Exception ex) {
			return _internalServerError(ex);
		}

	}


	@Dynamic(value = IxDynamicResourceHandler.IS_ADMIN, handler = ix.ncats.controllers.security.IxDeadboltHandler.class)
	public static Result uploadFile() {
		DynamicForm requestData = Form.form().bindFromRequest();
		String type = requestData.get("file-type");
		String nam = requestData.get("file-name");
		Logger.info("type =" + type);
		Logger.info("name =" + nam);
		try {
			Payload payload = payloadPlugin.get().parseMultiPart("file-name",
					request(), PayloadPersistType.PERM);
			if(payload!=null){
				//Need something to persist payloads in the database as a blob
				ObjectMapper om = new ObjectMapper();
				JsonNode jsn= om.valueToTree(payload);
				((com.fasterxml.jackson.databind.node.ObjectNode)jsn).put("url", payloadPlugin.get().getUrlForPayload(payload));
				return ok(jsn);
			}else{
				throw new IllegalStateException("Failed to upload file. Was 'file-name' specified?");
			}
		} catch (Exception ex) {
			return _internalServerError(ex);
		}
	}
	@Dynamic(value = IxDynamicResourceHandler.IS_ADMIN, handler = ix.ncats.controllers.security.IxDeadboltHandler.class)
	public static Result loadSDFWithDefaultMappings(String payloadUUID) {
		Payload sdpayload = PayloadFactory.getPayload(UUID
				.fromString(payloadUUID));
		DynamicForm requestData = Form.form().bindFromRequest();

//		GinasSDFUtils.setPathMappers(payloadUUID, GinasSDFUtils.getDefaultPathMap());

		String id = ginasRecordProcessorPlugin.get().submit(sdpayload,
				DefaultSdfLoader.class,
				ix.ginas.utils.GinasUtils.GinasSubstancePersister.class).key;
		return redirect(ix.ginas.controllers.routes.GinasLoad
				.monitorProcess(id));

	}
	@Dynamic(value = IxDynamicResourceHandler.IS_ADMIN, handler = ix.ncats.controllers.security.IxDeadboltHandler.class)
	public static Result loadSDF(String payloadUUID) {
		Payload sdpayload = PayloadFactory.getPayload(UUID
				.fromString(payloadUUID));
		DynamicForm requestData = Form.form().bindFromRequest();
		String mappingsjson = requestData.get("mappings");
		ObjectMapper om = new ObjectMapper();
//		System.out.println(mappingsjson);
		List<GinasSDFUtils.PATH_MAPPER> mappers = null;
		try {
			mappers = new ArrayList<GinasSDFUtils.PATH_MAPPER>();
			List<GinasSDFUtils.PATH_MAPPER> mappers2 = om.readValue(
					mappingsjson,
					new TypeReference<List<GinasSDFUtils.PATH_MAPPER>>() {
					});
			for (GinasSDFUtils.PATH_MAPPER pm : mappers2) {
				if (pm.method != GinasSDFUtils.PATH_MAPPER.ADD_METHODS.NULL_TYPE) {
					mappers.add(pm);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		GinasSDFUtils.setPathMappers(payloadUUID, mappers);


		// return ok("test");

		String id = ginasRecordProcessorPlugin.get().submit(sdpayload,
				ix.ginas.utils.GinasSDFUtils.GinasSDFExtractor.class,
				ix.ginas.utils.GinasUtils.GinasSubstancePersister.class).key;
		return redirect(ix.ginas.controllers.routes.GinasLoad
				.monitorProcess(id));

	}

	public static String getJobKey(ProcessingJob job) {
		return job.getKeyMatching(GinasRecordProcessorPlugin.class.getName());
	}

	public static Result monitorProcess(ProcessingJob job) {
		return monitorProcess(getJobKey(job));
	}

	public static Result monitorProcess(Long jobID) {
		return monitorProcess(ProcessingJobFactory.getJob(jobID));
	}

	@Dynamic(value = IxDynamicResourceHandler.IS_ADMIN, handler = ix.ncats.controllers.security.IxDeadboltHandler.class)
	public static Result monitorProcess(String processID) {
		
			String msg = "";
			ProcessingJob job = ProcessingJobFactory.getJob(processID);
			if (job != null) {
				return ok(ix.ginas.views.html.admin.job.render(job));
			} else {
				msg = "[not yet started]";
			}
			msg += "\n\n refresh page for status";
			return ok("Processing job:" + processID + "\n\n" + msg);
		
	}
	@Dynamic(value = IxDynamicResourceHandler.IS_ADMIN, handler = ix.ncats.controllers.security.IxDeadboltHandler.class)
	public static Result monitorProcessApi(String processID) {

		ProcessingJob job = ProcessingJobFactory.getJob(processID);
		if(job==null){
			return GsrsApiUtil.notFound("could not find process Id " + processID);
		}
		return Results.ok((JsonNode)EntityFactory.EntityMapper.COMPACT_ENTITY_MAPPER().valueToTree(job))
							.as("application/json");

	}

	@Dynamic(value = IxDynamicResourceHandler.IS_ADMIN, handler = ix.ncats.controllers.security.IxDeadboltHandler.class)
	public static Result jobs(final String q, final int rows, final int page)
			throws Exception {
		final int total = Math.max(ProcessingJobFactory.getCount(), 1);
		final String key = "jobs/" + Util.sha1(request());
		if (request().queryString().containsKey("facet") || q != null) {
			final SearchResult result = getSearchResult(ProcessingJob.class, q, total);
			if (result.finished()) {
				final String k = key + "/result";
				return getOrElse(k, ()->createJobResult(result, rows, page));
			}
			return createJobResult(result, rows, page);
		} else {
			return getOrElse(key, TypedCallable.of(()->{
					Logger.debug("Cache missed: " + key);
					TextIndexer.Facet[] facets = filter(
							getFacets(ProcessingJob.class, 30), ALL_FACETS);
					int nrows = Math.max(Math.min(total, Math.max(1, rows)), 1);
					int[] pages = paging(nrows, page, total);

					List<ProcessingJob> substances = ProcessingJobFactory
							.getProcessingJobs(nrows, (page - 1) * rows, null);

					return ok(ix.ginas.views.html.admin.jobs.render(page,
							nrows, total, pages, decorate(facets), substances));
			}, Result.class));
		}
	}

	static Result createJobResult(SearchResult result, int rows,
			int page) {
		TextIndexer.Facet[] facets = filter(result.getFacets(), ALL_FACETS);

		List<ProcessingJob> jobs = new ArrayList<ProcessingJob>();
		int[] pages = new int[0];
		if (result.getCount() > 0) {
			rows = Math.min(result.getCount(), Math.max(1, rows));
			pages = paging(rows, page, result.getCount());
			for (int i = (page - 1) * rows, j = 0; j < rows
					&& i < result.size(); ++j, ++i) {
				jobs.add((ProcessingJob) result.get(i));
			}
		}

		return ok(ix.ginas.views.html.admin.jobs.render(page, rows,
				result.getCount(), pages, decorate(facets), jobs));

	}


	

}
