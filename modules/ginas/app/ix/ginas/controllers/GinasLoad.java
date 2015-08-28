package ix.ginas.controllers;

import ix.core.models.Keyword;
import ix.core.models.Payload;
import ix.core.models.ProcessingJob;
import ix.core.models.ProcessingRecord;
import ix.core.models.Structure;
import ix.core.models.Value;
import ix.core.processing.RecordTransformer;
import ix.core.plugins.PayloadPlugin;
import ix.core.plugins.StructureIndexerPlugin;
import ix.core.plugins.GinasRecordProcessorPlugin;
import ix.core.plugins.TextIndexerPlugin;
import ix.core.plugins.StructureProcessorPlugin.PayloadProcessor;
import ix.core.search.TextIndexer;
import ix.core.search.TextIndexer.Facet;
import ix.ginas.controllers.GinasApp;
import ix.ginas.controllers.GinasLegacyUtils;
import ix.ginas.models.Ginas;
import ix.ginas.models.v1.*;
import ix.ncats.controllers.App;
import ix.utils.Util;
import ix.core.chem.Chem;
import ix.core.controllers.PayloadFactory;
import ix.core.controllers.ProcessingJobFactory;
import ix.ginas.models.utils.*;
import ix.ginas.utils.GinasSDFUtils;
import ix.ginas.utils.GinasSDFUtils.GinasSDFExtractor;
import ix.ginas.utils.GinasSDFUtils.GinasSDFExtractor.FieldStatistics;
import ix.ginas.utils.GinasUtils;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.UUID;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.io.FileOutputStream;
import java.util.zip.GZIPInputStream;

import ix.core.stats.Statistics;
import play.Logger;
import play.Play;
import play.data.DynamicForm;
import play.db.ebean.Model;
import play.data.Form;
import play.mvc.Http;
import play.mvc.Result;
import tripod.chem.indexer.StructureIndexer;

import com.avaje.ebean.*;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.DeserializationProblemHandler;
import com.fasterxml.jackson.core.type.TypeReference;

import java.util.Date;


public class GinasLoad extends App {
    public static boolean OLD_LOAD = false;
        
    public static final String[] ALL_FACETS = {
        "Job Status"
    };
        
        
    static final GinasRecordProcessorPlugin ginasRecordProcessorPlugin =
        Play.application().plugin(GinasRecordProcessorPlugin.class);
    static final PayloadPlugin payloadPlugin =
        Play.application().plugin(PayloadPlugin.class);
    
    public static Result error (int code, String mesg) {
        return ok (ix.ginas.views.html.error.render(code, mesg));
    }

    public static Result _notFound (String mesg) {
        return notFound (ix.ginas.views.html.error.render(404, mesg));
    }
    
    public static Result _badRequest (String mesg) {
        return badRequest (ix.ginas.views.html.error.render(400, mesg));
    }
    
    public static Result _internalServerError (Throwable t) {
        t.printStackTrace();
        return internalServerError
            (ix.ginas.views.html.error.render
             (500, "Internal server error: "+t.getMessage()));
    }
    
    static FacetDecorator[] decorate (Facet... facets) {
        List<FacetDecorator> decors = new ArrayList<FacetDecorator>();
        // override decorator as needed here
        for (int i = 0; i < facets.length; ++i) {
            decors.add(new GinasFacetDecorator (facets[i]));
        }       
        GinasFacetDecorator f = new GinasFacetDecorator
            (new TextIndexer.Facet("ChemicalSubstance"));
        f.hidden = true;
        decors.add(f);

        return decors.toArray(new FacetDecorator[0]);
    }

    static class GinasFacetDecorator extends FacetDecorator {
        GinasFacetDecorator (Facet facet) {
            super (facet, true, 6);
        }
        
        @Override
        public String name () {
            return super.name().trim();
        }

        @Override
        public String label (final int i) {
            final String label = super.label(i);
            final String name = super.name();

            return label;
        }
    }

    public static Result load () {
        if (Play.isProd()) {
            return redirect (ix.ginas.controllers.routes.GinasFactory.index());
        }
        return ok (ix.ginas.views.html.admin.load.render());
    }
    
    public static Result loadJSON () {
        
        if (Play.isProd()) {
            return badRequest ("Invalid request!");
        }
        
        DynamicForm requestData = Form.form().bindFromRequest();
        
        try {

            Payload sdpayload = payloadPlugin.parseMultiPart("sd-file",
                                                             request());

            if (sdpayload != null) {
                sdpayload.save();
                Map<String, FieldStatistics> m = GinasSDFExtractor
                    .getFieldStatistics(sdpayload, 100);
                return ok(ix.ginas.views.html.admin.sdfimportmapping.render(
                                                                            sdpayload, new ArrayList<FieldStatistics>(m.values())));
            } else {
                Payload payload = payloadPlugin.parseMultiPart("json-dump",
                                                               request());

                if (payload != null) {
                    // New way:
                    if (!GinasLoad.OLD_LOAD) {
                        String id = ginasRecordProcessorPlugin
                            .submit(payload,
                                    ix.ginas.utils.GinasUtils.GinasDumpExtractor.class,
                                    ix.ginas.utils.GinasUtils.GinasSubstancePersister.class);
                        return redirect(ix.ginas.controllers.routes.GinasLoad
                                        .monitorProcess(id));
                        // return ok("Running job " + id + " payload is " +
                        // payload.name + " also " + payload.id);
                    } else {
                        // Old way
                        return GinasLegacyUtils
                            .processDump(
                                         ix.utils.Util
                                         .getUncompressedInputStreamRecursive(payloadPlugin
                                                                              .getPayloadAsStream(payload)),
                                         false);
                    }
                } else {
                    return badRequest("Neither json-dump nor "
                                      + "sd-file is specified!");
                }
            }
        } catch (Exception ex) {
            return _internalServerError(ex);
        }
    }

    public static Result loadSDF (String payloadUUID) {
        Payload sdpayload=PayloadFactory.getPayload(UUID.fromString(payloadUUID));
        DynamicForm requestData = Form.form().bindFromRequest();
        String mappingsjson = requestData.get("mappings");
        ObjectMapper om = new ObjectMapper();
        System.out.println(mappingsjson);
        List<GinasSDFUtils.PATH_MAPPER> mappers=null;
        try{
            mappers= new ArrayList<GinasSDFUtils.PATH_MAPPER>();
            List<GinasSDFUtils.PATH_MAPPER> mappers2= om.readValue(mappingsjson, new TypeReference<List<GinasSDFUtils.PATH_MAPPER>>(){});
            for(GinasSDFUtils.PATH_MAPPER pm:mappers2){
                if(pm.method!=GinasSDFUtils.PATH_MAPPER.ADD_METHODS.NULL_TYPE){
                    mappers.add(pm);
                }
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        GinasSDFUtils.setPathMappers(payloadUUID, mappers);
        
        System.out.println("##################################");
        System.out.println("mapper rules:" + mappers.size());
        for(GinasSDFUtils.PATH_MAPPER pth:mappers){
            System.out.println("path:" + pth.path);
        }
        
        //      return ok("test");
                
        
        String id = ginasRecordProcessorPlugin.submit(sdpayload, 
                                                      ix.ginas.utils.GinasSDFUtils.GinasSDFExtractor.class,
                                                      ix.ginas.utils.GinasUtils.GinasSubstancePersister.class
                                                      );
        return redirect(ix.ginas.controllers.routes.GinasLoad.monitorProcess(id));
        
    }
   
    
    
    public static String getJobKey(ProcessingJob job){
        return job.getKeyMatching(GinasRecordProcessorPlugin.class.getName());
    }
    public static Result monitorProcess(ProcessingJob job){
        return monitorProcess(getJobKey(job));
    }

    public static Result monitorProcess(Long jobID){
        return monitorProcess(ProcessingJobFactory.getJob(jobID));
    }
    
    public static Result monitorProcess(String processID){
        if(!GinasLoad.OLD_LOAD){
            String msg="";
            ProcessingJob job = ProcessingJobFactory.getJob(processID);
            if(job!=null){
                return ok(ix.ginas.views.html.admin.job.render(job));
            }else{
                msg = "[not yet started]";
            }
            msg +="\n\n refresh page for status";
            return ok("Processing job:" + processID + "\n\n" + msg);
        }else{
            //OLD WAY:
            GinasLegacyUtils.Process p =GinasLegacyUtils.processes.get(processID);
            if(p==null){
                return _internalServerError (new IllegalArgumentException("Process \"" + processID + "\" does not exist."));
            }else{
                return ok(p.statusMessage());
            }
        }
    }
    
    
    
    
    public static Result jobs (final String q, final int rows, final int page)
            throws Exception {
        final int total = Math.max(ProcessingJobFactory.getCount(),1);
        final String key = "jobs/"+Util.sha1(request ());
        if (request().queryString().containsKey("facet") || q != null) {
            final TextIndexer.SearchResult result =
                getSearchResult (ProcessingJob.class, q, total);
            if (result.finished()) {
                final String k = key+"/result";
                return getOrElse
                        (k, new Callable<Result> () {
                            public Result call () throws Exception {
                                Logger.debug("Cache missed: "+k);
                                return createJobResult
                                        (result, rows, page);
                            }
                        });
            }

            return createJobResult (result, rows, page);
        }
        else {
            return getOrElse (key, new Callable<Result> () {
                    public Result call () throws Exception {
                        Logger.debug("Cache missed: "+key);
                        TextIndexer.Facet[] facets =
                            filter(getFacets(ProcessingJob.class, 30),
                                   ALL_FACETS);
                        int nrows = Math.max(Math.min(total, Math.max(1, rows)),1);
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
    public static Result testSubmit(){
        return ok(ix.ginas.views.html.test.testsubmit.render());                
    }
    public static Result submitSubstance(){
        String mappingsjson = null;
        try{
            mappingsjson = request().body().asJson().toString();
        }catch(Exception e){
            DynamicForm requestData = Form.form().bindFromRequest();
            mappingsjson = requestData.get("substance");                        
        }
        Logger.debug("################# got submission");
        Logger.debug(mappingsjson);
                
        Substance sub=null;
        
        try{
            System.out.println(mappingsjson);
            GinasUtils.GinasJSONExtractor ex = new GinasUtils.GinasJSONExtractor(mappingsjson);
            JsonNode jn=ex.getNextRecord();
            GinasUtils.GinasAbstractSubstanceTransformer trans = (GinasUtils.GinasAbstractSubstanceTransformer)ex.getTransformer();
            sub = trans.transformSubstance(jn);
            GinasUtils.GinasAbstractSubstanceTransformer.prepareSubstance(sub);
            List<String> errors = new ArrayList<String>();
            if(!GinasUtils.persistSubstance(sub, _strucIndexer,errors)){
                throw new IllegalStateException(errors.toString());
            }
        }catch(Throwable e){
            return _internalServerError(e);
        }
        return redirect(ix.ginas.controllers.routes.GinasApp.substance(GinasApp.getId(sub)));
    }
        
    public static Result updateSubstance(){
        DynamicForm requestData = Form.form().bindFromRequest();
        String mappingsjson = requestData.get("substance");
        Substance sub=null;
        try{
            System.out.println(mappingsjson);
            GinasUtils.GinasJSONExtractor ex = new GinasUtils.GinasJSONExtractor(mappingsjson);
            JsonNode jn=ex.getNextRecord();
            GinasUtils.GinasAbstractSubstanceTransformer trans = (GinasUtils.GinasAbstractSubstanceTransformer)ex.getTransformer();
            sub = trans.transformSubstance(jn);
            GinasUtils.GinasAbstractSubstanceTransformer.prepareSubstance(sub);
            List<String> errors = new ArrayList<String>();
            if(!GinasUtils.persistSubstance(sub, _strucIndexer,errors)){
                throw new IllegalStateException(errors.toString());
            }
        }catch(Throwable e){
            return _internalServerError(e);
        }
        return ok("worked:" + sub.uuid);
    }
}
