package ix.test.server;

import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

import java.io.IOException;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by katzelda on 4/12/16.
 */
public class SubstanceReIndexer {

    private static final Pattern REBUILD_LINK_PATTERN = Pattern.compile("(ginas/app/_updateIndex/\\S+)");
    private final BrowserSession session;

    public SubstanceReIndexer(BrowserSession session) {
        Objects.requireNonNull(session);
        this.session = session;
    }

    public void reindex() throws IOException{
        HtmlPage page = session.submit(session.newGetRequest("ginas/app/_updateIndex/_monitor").get());

        String relativeUrl = parseRebuildIndexURL(page);
        System.out.println("relativeURL = " + relativeUrl);
        HtmlPage reindexPage = session.submit(session.newGetRequest(relativeUrl).get());

        waitForReindexToComplete();
    }

    private void waitForReindexToComplete() throws IOException{
        HtmlPage page = session.submit(session.newGetRequest("ginas/app/_updateIndex/_monitor").get());

        while(!page.asXml().contains("Completed Substance reindexing.")){
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            page = session.submit(session.newGetRequest("ginas/app/_updateIndex/_monitor").get());
        }
    }

    private String parseRebuildIndexURL(HtmlPage page) throws IOException{
        HtmlAnchor a = page.getFirstByXPath("//a[text()='Rebuild Index (warning: will take some time)']");

        String url = a.getHrefAttribute();

        Matcher matcher = REBUILD_LINK_PATTERN.matcher(url);
        if(matcher.find()){
            return matcher.group(1);
        }
        throw new IOException("could not parse rebuild link from " + page.asXml());
    }
}
