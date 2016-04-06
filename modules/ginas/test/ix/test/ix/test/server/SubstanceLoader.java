package ix.test.ix.test.server;

import com.gargoylesoftware.htmlunit.FormEncodingType;
import com.gargoylesoftware.htmlunit.WebRequest;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.util.KeyDataPair;
import com.gargoylesoftware.htmlunit.util.NameValuePair;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Created by katzelda on 4/4/16.
 */
public class SubstanceLoader {

    private final BrowserSession session;

    private static int jobCount;

    static{
        init();
    }

    /**
     * Reset the jobCount whenever we (re)start the test server.
     */
    public static synchronized void init(){
        jobCount=1;
    }

    public SubstanceLoader(BrowserSession session){
        Objects.requireNonNull(session);
        this.session = session;
    }


    public void loadJson(File json) throws IOException{
        if(!json.exists()){
            throw new FileNotFoundException(json.getAbsolutePath());
        }
        String url;
        String status;
        //syncrhonize here in the critical section
        //where we start an upload.
        //this should prevent all other
        //loaders from loading a file at the same time
        //so our jobCount valable stays in sync
        synchronized(SubstanceLoader.class) {
            List<NameValuePair> params = new ArrayList<>();
            params.add(new NameValuePair("file-type", "JSON"));
            params.add(new KeyDataPair("file-name", json, json.getName(), "application/json", "utf-8"));
            WebRequest request = session.newPostRequest("ginas/app/load");
            request.setEncodingType(FormEncodingType.MULTIPART);
            request.setRequestParameters(params);

            HtmlPage result = session.submit(request);

            //instead of trying to figureout when the javascript compeltes
            //we can do rest requests to query the job status

            //http://localhost:$port/dev/ginas/app/api/v1/jobs/1"

            //compute status url once which we will use over and over
            //in the do-while loop below
            url = "ginas/app/api/v1/jobs/" + (jobCount++);
        }
        do {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                throw new IOException(e);
            }
            status = session.get(url)
                    .asJson()
                    .get("status").asText();

            System.out.println(status);

        }while(!"COMPLETE".equals(status));
    }
}
