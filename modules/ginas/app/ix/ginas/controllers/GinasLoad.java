package ix.ginas.controllers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import ix.core.controllers.PayloadFactory;
import ix.core.controllers.ProcessingJobFactory;
import ix.core.models.Payload;
import ix.core.models.ProcessingJob;
import ix.core.plugins.GinasRecordProcessorPlugin;
import ix.core.plugins.PayloadPlugin;
import ix.core.search.TextIndexer;
import ix.core.search.TextIndexer.Facet;
import ix.ginas.models.utils.GinasSDFUtils;
import ix.ginas.models.utils.GinasSDFUtils.GinasSDFExtractor;
import ix.ginas.models.utils.GinasSDFUtils.GinasSDFExtractor.FieldStatistics;
import ix.ginas.models.utils.GinasUtils;
import ix.ginas.models.v1.Substance;
import ix.ncats.controllers.App;
import ix.utils.Util;
import play.Logger;
import play.Play;
import play.data.DynamicForm;
import play.data.Form;
import play.mvc.Result;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.*;
import java.util.concurrent.Callable;


public class GinasLoad extends App {
    public static boolean OLD_LOAD = false;

    public static final String[] ALL_FACETS = {
            "Job Status"
    };


    static final GinasRecordProcessorPlugin ginasRecordProcessorPlugin =
            Play.application().plugin(GinasRecordProcessorPlugin.class);
    static final PayloadPlugin payloadPlugin =
            Play.application().plugin(PayloadPlugin.class);


    public static java.util.Map<String, Process> processes = new java.util.concurrent.ConcurrentHashMap<String, Process>();


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
        return internalServerError
                (ix.ginas.views.html.error.render
                        (500, "Internal server error: " + t.getMessage()));
    }

    static FacetDecorator[] decorate(Facet... facets) {
        List<FacetDecorator> decors = new ArrayList<FacetDecorator>();
        // override decorator as needed here
        for (int i = 0; i < facets.length; ++i) {
            decors.add(new GinasFacetDecorator(facets[i]));
        }
        // now add hidden facet so as to not have them shown in the alert
        // box
        //        for (int i = 1; i <= 8; ++i) {
        //            GinasFacetDecorator f = new GinasFacetDecorator
        //                (new TextIndexer.Facet
        //                 (ChemblRegistry.ChEMBL_PROTEIN_CLASS+" ("+i+")"));
        //            f.hidden = true;
        //            decors.add(f);
        //        }

        GinasFacetDecorator f = new GinasFacetDecorator
                (new TextIndexer.Facet("ChemicalSubstance"));
        f.hidden = true;
        decors.add(f);

        return decors.toArray(new FacetDecorator[0]);
    }

    static class GinasFacetDecorator extends FacetDecorator {
        GinasFacetDecorator(Facet facet) {
            super(facet, true, 6);
        }

        @Override
        public String name() {
            return super.name().trim();
        }

        @Override
        public String label(final int i) {
            final String label = super.label(i);
            final String name = super.name();

            return label;
        }
    }


    public static Substance persistJSON
            (InputStream is, Class<? extends Substance> cls) throws Exception {
        Substance sub = GinasUtils.makeSubstance(is);

        if (sub != null) {
            GinasUtils.persistSubstance(sub, _strucIndexer);
        }
        return sub;
    }

    public static Result load() {
        if (Play.isProd()) {
            return redirect(ix.ginas.controllers.routes.GinasFactory.index());
        }
        return ok(ix.ginas.views.html.admin.load.render());
    }

    public static Result loadSubstance() {
        Substance sub = null;
        Logger.info(request().body().asJson().toString());
        JsonNode tree = request().body().asJson();
        Logger.debug(tree.toString());
        try {
            sub = GinasUtils.makeSubstance(tree);

        } catch (Exception ex) {
            return _internalServerError(ex);
        }
        if (sub != null) {
            GinasUtils.persistSubstance(sub, _strucIndexer);
        }
        ObjectMapper mapper = new ObjectMapper();
        return ok(mapper.valueToTree(sub));
    }

    public static Result loadJSON() {

        if (Play.isProd()) {
            return badRequest("Invalid request!");
        }

        DynamicForm requestData = Form.form().bindFromRequest();
        String type = requestData.get("substance-type");
        Logger.debug("substance-type: " + type);

        Substance sub = null;
        try {
            InputStream is = null;
            String url = requestData.get("json-url");
            Logger.debug("json-url: " + url);
            if (url != null && url.length() > 0) {
                URL u = new URL(url);
                is = u.openStream();
            } else {

                Payload sdpayload = payloadPlugin.parseMultiPart
                        ("sd-file", request());

                if (sdpayload != null) {
//					String id = ginasRecordProcessorPlugin.submit(sdpayload,
//							ix.ginas.models.utils.GinasSDFUtils.GinasSDFExtractor.class);
//					return redirect(ix.ginas.controllers.routes.GinasLoad.monitorProcess(id));

//                  Statistics / breakdown for later use  
//					====================================
                    sdpayload.save();
                    Map<String, FieldStatistics> m = GinasSDFExtractor.getFieldStatistics(sdpayload, 100);
                    return ok(ix.ginas.views.html.admin.sdfimportmapping.render(sdpayload, new ArrayList<FieldStatistics>(m.values())));
                } else {
                    Payload payload = payloadPlugin.parseMultiPart
                            ("json-dump", request());

                    if (payload != null) {
                        // New way:
                        if (!GinasLoad.OLD_LOAD) {
                            String id = ginasRecordProcessorPlugin
                                    .submit(payload,
                                            ix.ginas.models.utils.GinasUtils.GinasDumpExtractor.class);
                            return redirect(ix.ginas.controllers.routes.GinasLoad.monitorProcess(id));
                            //return ok("Running job " + id + " payload is " + payload.name + " also " + payload.id);
                        } else {
                            // Old way
                            return processDump(ix.utils.Util.getUncompressedInputStreamRecursive(payloadPlugin.getPayloadAsStream(payload)), false);
                        }
                    } else {
                        return badRequest
                                ("Neither json-url nor json-file nor json-dump "
                                        + "parameter is specified!");
                    }
                }
            }

            sub = persistJSON(is, null);
        } catch (Exception ex) {
            return _internalServerError(ex);
        }

        ObjectMapper mapper = new ObjectMapper();
        return ok(mapper.valueToTree(sub));
    }

    public static Result loadSDF(String payloadUUID) {
        Payload sdpayload = PayloadFactory.getPayload(UUID.fromString(payloadUUID));
        DynamicForm requestData = Form.form().bindFromRequest();
        String mappingsjson = requestData.get("mappings");
        ObjectMapper om = new ObjectMapper();
        System.out.println(mappingsjson);
        List<GinasSDFUtils.PATH_MAPPER> mappers = null;
        try {
            mappers = new ArrayList<GinasSDFUtils.PATH_MAPPER>();
            List<GinasSDFUtils.PATH_MAPPER> mappers2 = om.readValue(mappingsjson, new TypeReference<List<GinasSDFUtils.PATH_MAPPER>>() {
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

        System.out.println("#########################");
        System.out.println(mappers);

//    	return ok("test");


        String id = ginasRecordProcessorPlugin.submit(sdpayload, ix.ginas.models.utils.GinasSDFUtils.GinasSDFExtractor.class);
        return redirect(ix.ginas.controllers.routes.GinasLoad.monitorProcess(id));

    }


    /**
     * Processes an inputstream of jsonDump format.
     *
     * @param is
     * @param sync
     * @return
     * @throws Exception
     */
    public static Result processDump(final InputStream is, boolean sync) throws Exception {
        final String requestID = UUID.randomUUID().toString().replace("-", "").substring(0, 10);

        JsonDumpImportProcess jdip = new JsonDumpImportProcess(is);
        processes.put(jdip.processID(), jdip);

        if (sync) {
            jdip.getRunnable().run();
            return ok(jdip.statusMessage());
        } else {
            (new Thread(jdip.getRunnable())).start();
            return redirect(ix.ginas.controllers.routes.GinasLoad.monitorProcess(jdip.processID()));
        }
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

    public static Result monitorProcess(String processID) {
        if (!GinasLoad.OLD_LOAD) {
            String msg = "";
            ProcessingJob job = ProcessingJobFactory.getJob(processID);
            if (job != null) {
                return ok(ix.ginas.views.html.admin.job.render(job));
            } else {
                msg = "[not yet started]";
            }
            msg += "\n\n refresh page for status";


            return ok("Processing job:" + processID + "\n\n" + msg);
        } else {
            //OLD WAY:
            Process p = processes.get(processID);
            if (p == null) {
                return _internalServerError(new IllegalArgumentException("Process \"" + processID + "\" does not exist."));
            } else {
                return ok(p.statusMessage());
            }
        }
    }

    static abstract class Process {
        public abstract String processID();

        public abstract boolean isComplete();

        public abstract Date startTime();

        public abstract Date completeTime();

        public abstract Date estimatedCompleteTime();

        public abstract String statusMessage();

        public String submittedBy() {
            return "system";
        }
    }

    static class JsonDumpImportProcess extends Process {
        String requestID = UUID.randomUUID().toString().replace("-", "").substring(0, 10);
        String statusMessage = "initializing";
        Date startTime;
        Date endTime = null;
        Date lastTime = null;
        boolean done = false;
        boolean canceled = false;

        int importCount = 0;
        int failedCount = 0;

        Runnable r;

        public JsonDumpImportProcess(final InputStream is) {
            r = new Runnable() {
                @Override
                public void run() {
                    startTime = new Date();
                    lastTime = startTime;

                    try {
                        BufferedReader br = new BufferedReader(new InputStreamReader(is));

                        for (String line; (line = br.readLine()) != null; ) {
                            String[] toks = line.split("\t");
                            Logger.debug("processing " + toks[0] + " " + toks[1] + "..." + importCount);
                            try {
                                ByteArrayInputStream bis = new ByteArrayInputStream
                                        (toks[2].getBytes("utf8"));
                                Substance sub = persistJSON(bis, null);
                                if (sub == null) {
                                    Logger.warn("Can't persist record " + toks[1]);
                                    failedCount++;
                                } else {
                                    importCount++;
                                }
                            } catch (Exception ex) {
                                Logger.warn("Can't persist record " + toks[1]);
                                failedCount++;
                                ex.printStackTrace();
                            }
                            lastTime = new Date();
                        }
                        br.close();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        canceled = true;
                    }

                    endTime = new Date();
                    lastTime = new Date();
                    done = true;
                }
            };

        }

        @Override
        public String processID() {
            return requestID;
        }

        @Override
        public boolean isComplete() {
            return done;
        }

        @Override
        public Date startTime() {
            return startTime;
        }

        @Override
        public Date completeTime() {
            return endTime;
        }

        @Override
        public Date estimatedCompleteTime() {
            return null;
        }

        @Override
        public String statusMessage() {
            String msg = "Process is " + getStatusType() + ".\n";
            msg += "Imported records:\t" + importCount + "\n";
            msg += "Failed records:\t" + failedCount + "\n";
            msg += "Start time:\t" + startTime + "\n";
            msg += "End time:\t" + endTime + "\n";
            msg += "Processing Time:\t" + getTotalImportTimems() + "ms\n";
            msg += "Average time per record:\t" + getAverageImportTimems() + "ms\n";
            return msg;
        }

        public long getAverageImportTimems() {
            long dt = lastTime.getTime() - startTime.getTime();
            return dt / (importCount + 1);
        }

        public long getTotalImportTimems() {
            return lastTime.getTime() - startTime.getTime();
        }

        public String getStatusType() {
            if (done) return "complete";
            return "running";
        }

        public Runnable getRunnable() {
            return r;
        }


    }


    public static Result jobs(final String q, final int rows, final int page)
            throws Exception {
        final int total = Math.max(ProcessingJobFactory.getCount(), 1);
        final String key = "jobs/" + Util.sha1(request());
        if (request().queryString().containsKey("facet") || q != null) {
            final TextIndexer.SearchResult result =
                    getSearchResult(ProcessingJob.class, q, total);
            if (result.finished()) {
                final String k = key + "/result";
                return getOrElse
                        (k, new Callable<Result>() {
                            public Result call() throws Exception {
                                Logger.debug("Cache missed: " + k);
                                return createJobResult
                                        (result, rows, page);
                            }
                        });
            }

            return createJobResult(result, rows, page);
        } else {
            return getOrElse(key, new Callable<Result>() {
                public Result call() throws Exception {
                    Logger.debug("Cache missed: " + key);
                    TextIndexer.Facet[] facets =
                            filter(getFacets(ProcessingJob.class, 30),
                                    ALL_FACETS);
                    int nrows = Math.max(Math.min(total, Math.max(1, rows)), 1);
                    int[] pages = paging(nrows, page, total);

                    List<ProcessingJob> substances =
                            ProcessingJobFactory.getProcessingJobs(nrows, (page - 1) * rows, null);

                    return ok(ix.ginas.views.html.admin.jobs.render
                            (page, nrows, total, pages,
                                    decorate(facets), substances));
                }
            });
        }
    }

    static Result createJobResult(TextIndexer.SearchResult result,
                                  int rows, int page) {
        TextIndexer.Facet[] facets = filter(result.getFacets(), ALL_FACETS);

        List<ProcessingJob> substances = new ArrayList<ProcessingJob>();
        int[] pages = new int[0];
        if (result.count() > 0) {
            rows = Math.min(result.count(), Math.max(1, rows));
            pages = paging(rows, page, result.count());
            for (int i = (page - 1) * rows, j = 0; j < rows
                    && i < result.size(); ++j, ++i) {
                substances.add((ProcessingJob) result.get(i));
            }
        }

        return ok(ix.ginas.views.html.admin.jobs.render(page, rows,
                result.count(), pages, decorate(facets), substances));

    }
}