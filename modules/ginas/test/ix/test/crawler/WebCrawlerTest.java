package ix.test.crawler;

import com.gargoylesoftware.htmlunit.WebResponse;
import ix.core.util.StopWatch;
import ix.test.ix.test.server.BrowserSession;
import ix.test.ix.test.server.GinasTestServer;
import ix.test.ix.test.server.SubstanceLoader;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import play.libs.ws.WSResponse;

import java.io.File;
import java.io.IOError;
import java.io.IOException;
import java.net.URL;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.Assert.*;

/**
 * Created by katzelda on 6/29/16.
 */
public class WebCrawlerTest {

    @Rule
    public GinasTestServer ts = new GinasTestServer();

    private GinasTestServer.User admin;

    @Before
    public void createAdminAndLoadData() throws Exception{
        admin = ts.createAdmin("admin2", "adminPass");
        loadRep90();
    }

    private void loadRep90() throws Exception {
        try (BrowserSession session = ts.newBrowserSession(admin)) {

            SubstanceLoader loader = new SubstanceLoader(session);

            File f = new File("test/testdumps/rep90.ginas");

            loader.loadJson(f);

        }
    }


    @Test
    public void crawl() throws Exception{


        WebCrawlerSpy spy = new WebCrawlerSpy();
        WebCrawler crawler = new WebCrawler.Builder (spy).build();
        URL url = ts.getHomeUrl();

        long elapsed = StopWatch.timeElapsed(() -> crawler.crawl(url));

        System.out.println("done... in " + elapsed + "ms");

        assertEquals(90, spy.getSubstancesVisited().size());
    }


    @Test
    public void restrictedLinksAreForbiddenWhenUnAuthenticated() throws IOException {
        WebCrawlerSpy spy = new WebCrawlerSpy();
        WebCrawler crawler = new WebCrawler.Builder (spy).build();
        URL url = ts.getHomeUrl();

        crawler.crawl(url);

        assertFalse(spy.get401Links().isEmpty());

    }

    @Test
    public void restrictedLinksAreAccessibleFromAdmin() throws IOException {
        WebCrawlerSpy spy = new WebCrawlerSpy();
        WebCrawler crawler = new WebCrawler.Builder (spy).build();

        crawler.crawl(ts.getHomeUrl());

        Set<URL> links = spy.get401Links();
        try(BrowserSession session = ts.newBrowserSession(admin)){
            for(URL url : links) {
                WSResponse response = session.get(url);
                assertEquals(200, response.getStatus());
            }
        }

    }

    private static final class WebCrawlerSpy extends AbstractWebCrawlerSpy{

        private static final Pattern SUBSTANCES_ROOT_PATTERN = Pattern.compile("/ginas/app/substance/([a-z0-9]+)$");
        private final Set<String> substancesVisited = new LinkedHashSet<>();

        private final Set<URL> _401Links = new LinkedHashSet<>();
        @Override
        public boolean shouldVisit(URL url) {
            return true;
        }

        @Override
        public void visitValidURL(URL url) {

            Matcher matcher = SUBSTANCES_ROOT_PATTERN.matcher(url.toString());
            if(matcher.find()){
                String substanceId = matcher.group(1);
                substancesVisited.add(substanceId);
            }
        }

        @Override
        protected void visitErrorURL(URL url, int statusCode, String statusMessage) {
            if(statusCode ==401){
                _401Links.add(url);
            }
        }

        public Set<String> getSubstancesVisited() {
            return substancesVisited;
        }

        public Set<URL> get401Links() {
            return _401Links;
        }
    }

    private static abstract class AbstractWebCrawlerSpy implements WebCrawler.WebCrawlerVisitor{
        @Override
        public boolean shouldVisit(URL url) {
            return true;
        }

        public void visited(URL url, int statusCode, String statusMessage) {
            if(statusCode == 200 || statusCode == 201){
                visitValidURL(url);
            }else{
                visitErrorURL(url, statusCode, statusMessage);
            }
        }

        protected void visitValidURL(URL url){

        }

        protected void visitErrorURL(URL url, int statusCode, String statusMessage){

        }

    }
}
