package ix.ginas.controllers;

import ix.core.models.Keyword;
import ix.core.models.Payload;
import ix.core.models.ProcessingJob;
import ix.core.models.ProcessingRecord;
import ix.core.models.Structure;
import ix.core.models.Value;
import ix.core.plugins.PayloadPlugin;
import ix.core.plugins.StructureIndexerPlugin;
import ix.core.plugins.GinasRecordProcessorPlugin;
import ix.core.plugins.TextIndexerPlugin;
import ix.core.plugins.StructureProcessorPlugin.PayloadProcessor;
import ix.core.search.TextIndexer;
import ix.core.search.TextIndexer.Facet;
import ix.ginas.models.GinasCommonSubData;
import ix.ginas.models.v1.*;
import ix.ncats.controllers.App;
import ix.utils.Util;
import ix.core.adapters.EntityPersistAdapter;
import ix.core.chem.Chem;
import ix.core.controllers.PayloadFactory;
import ix.core.controllers.ProcessingJobFactory;
import ix.ginas.models.utils.*;
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


public class GinasLegacyUtils extends App {
        public static java.util.Map<String,Process> processes = new java.util.concurrent.ConcurrentHashMap<String,Process>();            
         
        public static abstract class Process{
        public abstract String processID();
        public abstract boolean isComplete();
        public abstract Date startTime();
        public abstract Date completeTime();
        public abstract Date estimatedCompleteTime();
        public abstract String statusMessage();
        public String submittedBy(){
                return "system";
        }
    }
        public static class JsonDumpImportProcess extends Process{
        String requestID = UUID.randomUUID().toString().replace("-", "").substring(0, 10);
        String statusMessage="initializing";
        Date startTime;
        Date endTime=null;
        Date lastTime=null;
        boolean done=false;
        boolean canceled=false;
        
        int importCount=0;
        int failedCount=0;
        
        Runnable r;
        public JsonDumpImportProcess(final InputStream is){
                r = new Runnable(){
                        @Override
                        public void run(){
                                startTime=new Date();
                                lastTime=startTime;
                                
                                try{
                                        BufferedReader br = new BufferedReader (new InputStreamReader (is));
                                
                                for (String line; (line = br.readLine()) != null; ) {
                                    String[] toks = line.split("\t");
                                    Logger.debug("processing "+toks[0]+" "+toks[1]+"..."+importCount);
                                    try {
                                        ByteArrayInputStream bis = new ByteArrayInputStream
                                            (toks[2].getBytes("utf8"));
                                        Substance sub = persistJSON (bis, null);
                                        if (sub == null) {
                                            Logger.warn("Can't persist record "+toks[1]);
                                            failedCount++;
                                        }
                                        else {
                                            importCount++;
                                        }
                                    }
                                    catch (Exception ex) {
                                        Logger.warn("Can't persist record "+toks[1]);
                                    failedCount++;
                                        ex.printStackTrace();
                                    }
                                lastTime=new Date();
                                }
                                br.close();
                                }
                        catch (Exception ex) {
                        ex.printStackTrace();
                        canceled=true;
                    }
                       
                        endTime=new Date();
                        lastTime=new Date();
                        done=true;
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
                public String statusMessage(){
                        String msg="Process is " +getStatusType() + ".\n";
                        msg+="Imported records:\t" +importCount + "\n";
                        msg+="Failed records:\t" +failedCount + "\n";
                        msg+="Start time:\t" +startTime + "\n";
                        msg+="End time:\t" +endTime + "\n";
                        msg+="Processing Time:\t" + getTotalImportTimems() + "ms\n";
                        msg+="Average time per record:\t" + getAverageImportTimems() + "ms\n";
                        return msg;
                }
                
                public long getAverageImportTimems(){
                        long dt=lastTime.getTime()-startTime.getTime();
                        return dt/(importCount+1);
                }
                public long getTotalImportTimems(){
                        return lastTime.getTime()-startTime.getTime();
                }
                
                public String getStatusType(){
                        if(done)return "complete";
                        return "running";
                }
                
                public Runnable getRunnable(){
                        return r;
                }
    }
    
    public static Substance persistJSON(InputStream is, Class<? extends Substance> cls) throws Exception {
        Substance sub = GinasUtils.makeSubstance(is);
        
        if(sub!=null){
            GinasUtils.persistSubstance(sub, EntityPersistAdapter.getStructureIndexer(), null);
        }
        return sub;
    }
    
    /**
     * Processes an inputstream of jsonDump format.
     * @param is
     * @param sync
     * @return
     * @throws Exception
     */
    public static Result processDump (final InputStream is, boolean sync) throws Exception {
        final String requestID = UUID.randomUUID().toString().replace("-", "").substring(0, 10);
        
        JsonDumpImportProcess jdip= new JsonDumpImportProcess(is);
        processes.put(jdip.processID(),jdip);
        
        if(sync){
                jdip.getRunnable().run();
                return ok (jdip.statusMessage());
        }else{
                (new Thread(jdip.getRunnable())).start();
                return redirect(ix.ginas.controllers.routes.GinasLoad.monitorProcess(jdip.processID()));
        }
    }
}
