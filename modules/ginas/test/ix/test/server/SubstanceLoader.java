package ix.test.server;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gargoylesoftware.htmlunit.FormEncodingType;
import com.gargoylesoftware.htmlunit.WebRequest;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.util.KeyDataPair;
import com.gargoylesoftware.htmlunit.util.NameValuePair;
import com.ning.http.client.FluentCaseInsensitiveStringsMap;
import com.ning.http.client.multipart.StringPart;
import com.ning.http.client.providers.jdk.MultipartRequestEntity;
import ix.core.models.Keyword;
import ix.core.models.ProcessingJob;
import ix.core.util.RestUrlLink;
import org.apache.commons.io.Charsets;
import play.api.mvc.MultipartFormData.*;
import play.libs.F;
import play.libs.ws.WS;
import play.libs.ws.WSRequestHolder;
import play.libs.ws.WSResponse;

import java.io.*;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * Created by katzelda on 4/4/16.
 */
public class SubstanceLoader {

    private static final Pattern LOAD_MONITOR_PATTERN = Pattern.compile(" <a href=\"/(ginas/app/monitor/[a-z0-9]+)\" target=\"_self\">");

    private static final Pattern LOAD_PROGRESS_PATTERN = Pattern.compile("<code>\\s*(COMPLETE|PENDING|RUNNING)\\s*</code>");

    private static ObjectMapper MAPPER = new ObjectMapper();

    private final RestSession session;

    /**
     * Options to give to the loader to load
     * a specific json file.
     */
    public static class LoadOptions{

        public static LoadOptions DEFAULT_OPTIONS = new LoadOptions();

        private boolean preserveAuditInfo;
        private Integer numRecordsToSkip;
        private Integer numRecordsToLoad;

        private File tempDir=null;

        private Integer sleepAmount;

        public LoadOptions preserveAuditInfo(boolean preserveAuditInfo){
            this.preserveAuditInfo = preserveAuditInfo;
            return this;
        }

        public LoadOptions tmpDir(File tmpDir){
            this.tempDir = tmpDir;
            return this;
        }
        public LoadOptions numRecordsToSkip(int skip){
            if(skip < 0){
                throw new IllegalArgumentException("can not skip negative records : " + skip);
            }
            this.numRecordsToSkip = skip;
            return this;
        }

        public LoadOptions sleepAmount(int ms){
            if(ms < 0){
                throw new IllegalArgumentException("can notsleep for negative time : " + ms);
            }
            this.sleepAmount = ms;
            return this;
        }

        public LoadOptions numRecordsToLoad(int numToLoad){
            if(numToLoad < 0){
                throw new IllegalArgumentException("can not load negative records : " + numToLoad);
            }
            this.numRecordsToLoad = numToLoad;
            return this;
        }

        F.Promise<WSResponse> generateParametersFor(File fullInputJsonFile, String postUrl) throws IOException{
//            List<NameValuePair> params = new ArrayList<>();
//            params.add(new NameValuePair("file-type", "JSON"));
//            File json;
//            if(numRecordsToLoad !=null || numRecordsToSkip !=null){
//                json = createParitalTempFile(fullInputJsonFile);
//            }else{
//                json = fullInputJsonFile;
//            }
//            params.add(new KeyDataPair("file-name", json, json.getName(), "application/json", "utf-8"));
//            //preserve-audit // looks like value just has to be non-null
//            if(preserveAuditInfo){
//                params.add(new NameValuePair("preserve-audit", "preserve-audit"));
//            }
            List<com.ning.http.client.multipart.Part> parts = new ArrayList<>();
            try {
                File json;
                if(numRecordsToLoad !=null || numRecordsToSkip !=null){
                    json = createParitalTempFile(fullInputJsonFile);
                }else{
                    json = fullInputJsonFile;
                }
                parts.add(new com.ning.http.client.multipart.FilePart("file-name", json, "application/json", Charsets.UTF_8));
                parts.add(new StringPart("file-type", "JSON"));
                if(preserveAuditInfo) {
                    parts.add(new StringPart("preserve-audit", "preserve-audit"));
                }
                // Add it to the multipart request entity
                MultipartRequestEntity requestEntity = new MultipartRequestEntity(parts, new FluentCaseInsensitiveStringsMap());
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                requestEntity.writeRequest(bos);
                InputStream reqIS = new ByteArrayInputStream(bos.toByteArray());
                return WS.url(postUrl)
                        .setContentType(requestEntity.getContentType())
                        .post(reqIS);

            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }

        private File createParitalTempFile(File json ) throws IOException{
            File tmp = File.createTempFile( "ginas-json",".gsrs", tempDir);
            tmp.deleteOnExit();
            try(BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new GZIPOutputStream(new BufferedOutputStream(new FileOutputStream(tmp)))));
                BufferedReader reader = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(json))));
            ){
                if(numRecordsToSkip !=null) {
                    int skipCount = numRecordsToSkip;
                    for (int i = 0; i < skipCount; i++) {
                        reader.readLine();
                    }
                }

                if(numRecordsToLoad !=null) {
                    int loadCount = numRecordsToLoad;
                    for (int i = 0; i < loadCount; i++) {
                        String line = reader.readLine();
                        if (line == null) {
                            break;
                        }
                        out.write(line);
                        out.newLine();
                    }
                }
            }

            return tmp;
        }
    }

    public SubstanceLoader(RestSession session){
        Objects.requireNonNull(session);
        this.session = session;
    }
    public SubstanceLoader(BrowserSession session){
        Objects.requireNonNull(session);
        this.session = session.getRestSession();
    }



    public void loadJson(File json) throws IOException{
        loadJson(json, LoadOptions.DEFAULT_OPTIONS);
    }

    public void loadJson(File json, LoadOptions options) throws IOException{
        if(!json.exists()){
            throw new FileNotFoundException(json.getAbsolutePath());
        }

        WSResponse response = submitFileForLoading(json, options);
        if(response.getStatus()>=300){
            throw new HttpErrorCode(response);
        }
        waitUntilComplete(response);
        if(options.sleepAmount !=null){

            try {
                Thread.sleep(options.sleepAmount);
            } catch (InterruptedException e) {
                throw new IOException(e);
            }
        }
    }

    private void waitUntilComplete(WSResponse response ) throws IOException {
        waitUntilComplete(parseJob(response.asJson()), 2_000L);
    }
    private void waitUntilComplete(ProcessingJob job) throws IOException {
        waitUntilComplete(job, 2_000L);
    }
    private void waitUntilComplete(ProcessingJob originalJob, long sleeptimeMillis) throws IOException {
        ProcessingJob job = originalJob;

        while(!job.status.isInFinalState()){
            try {
                Thread.sleep(sleeptimeMillis);
            } catch (InterruptedException e) {
                throw new IOException(e);
            }
            WSRequestHolder request = session.getRequest(job.selfUrl().url);
            JsonNode json = session.getAsJson(request);
            job = parseJob( json);

        };
    }


    protected WSResponse submitFileForLoading(File json, LoadOptions options) throws IOException {
        return options.generateParametersFor(json, postLoadUrl(session.getHttpResolver()))
                                    .get(2_000, TimeUnit.MILLISECONDS);
    }

    protected String postLoadUrl(GinasHttpResolver resolver) {
        return resolver.apiV1("/admin/load", true);
    }



    protected ProcessingJob parseJob(JsonNode json) {
        try {
            ProcessingJob job =MAPPER.convertValue(json, ProcessingJob.class);
            return job;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    public static class HttpErrorCode extends IOException{
        private WSResponse response;

        public HttpErrorCode(WSResponse response) {
            super(response.getStatus() + " : " + response.getStatusText());
            this.response = response;
        }

        public WSResponse getResponse() {
            return response;
        }

        public int getStatus(){
            return response.getStatus();
        }
    }

}
