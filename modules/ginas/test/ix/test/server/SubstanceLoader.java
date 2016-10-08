package ix.test.server;

import com.gargoylesoftware.htmlunit.FormEncodingType;
import com.gargoylesoftware.htmlunit.WebRequest;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.util.KeyDataPair;
import com.gargoylesoftware.htmlunit.util.NameValuePair;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by katzelda on 4/4/16.
 */
public class SubstanceLoader {

    private static final Pattern LOAD_MONITOR_PATTERN = Pattern.compile(" <a href=\"/(ginas/app/monitor/[a-z0-9]+)\" target=\"_self\">");

    private static final Pattern LOAD_PROGRESS_PATTERN = Pattern.compile("<code>\\s*(COMPLETE|PENDING|RUNNING)\\s*</code>");
    private final BrowserSession session;



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
            System.out.println(status);

        }while(!"COMPLETE".equals(status));
    }

    private String submitFileForLoading(File json) throws IOException {
        String url;


        List<NameValuePair> params = new ArrayList<>();
        params.add(new NameValuePair("file-type", "JSON"));
        params.add(new KeyDataPair("file-name", json, json.getName(), "application/json", "utf-8"));
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
