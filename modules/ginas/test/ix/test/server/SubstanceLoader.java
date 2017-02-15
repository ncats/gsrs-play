package ix.test.server;

import com.gargoylesoftware.htmlunit.FormEncodingType;
import com.gargoylesoftware.htmlunit.WebRequest;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.util.KeyDataPair;
import com.gargoylesoftware.htmlunit.util.NameValuePair;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
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
    private final BrowserSession session;

    /**
     * Options to give to the loader to load
     * a specific json file.
     */
    public static class LoadOptions{
        private boolean preserveAuditInfo;
        private Integer numRecordsToSkip;
        private Integer numRecordsToLoad;

        private File tempDir=null;

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

        public LoadOptions numRecordsToLoad(int numToLoad){
            if(numToLoad < 0){
                throw new IllegalArgumentException("can not load negative records : " + numToLoad);
            }
            this.numRecordsToLoad = numToLoad;
            return this;
        }

        List<NameValuePair> generateParametersFor(File fullInputJsonFile) throws IOException{
            List<NameValuePair> params = new ArrayList<>();
            params.add(new NameValuePair("file-type", "JSON"));
            File json;
            if(numRecordsToLoad !=null || numRecordsToSkip !=null){
                json = createParitalTempFile(fullInputJsonFile);
            }else{
                json = fullInputJsonFile;
            }
            params.add(new KeyDataPair("file-name", json, json.getName(), "application/json", "utf-8"));
            //preserve-audit // looks like value just has to be non-null
            if(preserveAuditInfo){
                params.add(new NameValuePair("preserve-audit", "preserve-audit"));
            }

            return params;
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


    public SubstanceLoader(BrowserSession session){
        Objects.requireNonNull(session);
        this.session = session;
    }


    public void loadJson(File json) throws IOException{
        if(!json.exists()){
            throw new FileNotFoundException(json.getAbsolutePath());
        }
        String url = submitFileForLoading(json);

        waitUntilComplete(url);
    }

    public void loadJson(File json, LoadOptions options) throws IOException{
        if(!json.exists()){
            throw new FileNotFoundException(json.getAbsolutePath());
        }

        String url = submitFileForLoading(json, options);

        waitUntilComplete(url);
    }


    private void waitUntilComplete(String monitorUrl) throws IOException {
        waitUntilComplete(monitorUrl, 2_000L);
    }
    private void waitUntilComplete(String monitorUrl, long sleeptimeMillis) throws IOException {
        String status=null;
        do {
            try {
                Thread.sleep(sleeptimeMillis);
            } catch (InterruptedException e) {
                throw new IOException(e);
            }
            WebRequest request = session.newGetRequest(monitorUrl).get();
            HtmlPage monitorPage = session.submit(request);

           // System.out.println(monitorPage.asXml());

            Matcher matcher = LOAD_PROGRESS_PATTERN.matcher(monitorPage.asXml());
            if(matcher.find()){
                status = matcher.group(1);
            }

        }while(!"COMPLETE".equals(status));
    }

    private String submitFileForLoading(File json) throws IOException {
        String url;


        List<NameValuePair> params = new ArrayList<>();
        params.add(new NameValuePair("file-type", "JSON"));
        params.add(new KeyDataPair("file-name", json, json.getName(), "application/json", "utf-8"));
        //preserve-audit // looks like value just has to be non-null
        WebRequest request = session.newPostRequest("ginas/app/load");
        request.setEncodingType(FormEncodingType.MULTIPART);
        request.setRequestParameters(params);

        HtmlPage result = session.submit(request);

        Matcher matcher = LOAD_MONITOR_PATTERN.matcher(result.asXml());
        if(!matcher.find()){
            throw new IOException("could not parse monitor URL for load");
        }

        url = matcher.group(1);
        return url;
    }

    private String submitFileForLoading(File json, LoadOptions options) throws IOException {
        String url;


        List<NameValuePair> params = options.generateParametersFor(json);
        WebRequest request = session.newPostRequest("ginas/app/load");
        request.setEncodingType(FormEncodingType.MULTIPART);
        request.setRequestParameters(params);

        HtmlPage result = session.submit(request);

        Matcher matcher = LOAD_MONITOR_PATTERN.matcher(result.asXml());
        if(!matcher.find()){
            throw new IOException("could not parse monitor URL for load");
        }

        url = matcher.group(1);
        System.out.println("monitor URL = " + url);
        return url;
    }


}
