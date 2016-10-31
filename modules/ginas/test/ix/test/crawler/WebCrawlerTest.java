package ix.test.crawler;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import ix.AbstractGinasClassServerTest;
import ix.core.plugins.IxCache;
import ix.core.util.RunOnly;
import ix.core.util.StopWatch;
import ix.test.server.BrowserSession;
import ix.test.server.GinasTestServer;
import ix.test.server.SubstanceLoader;
import play.libs.ws.WSResponse;

/**
 * Created by katzelda on 6/29/16.
 */
public class WebCrawlerTest  extends AbstractGinasClassServerTest {

    private static GinasTestServer.User admin;

    @BeforeClass
    public static void createAdminAndLoadData() throws Exception{
        admin = ts.createAdmin("admin2", "adminPass");
        loadRep90();
    }

    @Before
    public void clearCache(){
        IxCache.clearCache();
    }

    private static void loadRep90() throws Exception {
        try (BrowserSession session = ts.newBrowserSession(admin)) {
            SubstanceLoader loader = new SubstanceLoader(session);
            File f = new File("test/testdumps/rep90.ginas");
            loader.loadJson(f);
        }
    }


    @Test
    public void crawl() throws Exception{


        WebCrawlerSpy spy = new WebCrawlerSpy();
        
        try(BrowserSession session =  ts.notLoggedInBrowserSession()){
            
            WebCrawler crawler = new WebCrawler.Builder(session, spy).build();
            URL url = ts.getHomeUrl();

            long elapsed = StopWatch.timeElapsed(() -> crawler.crawl(url));

            System.out.println("done... in " + elapsed + "ms");

            assertEquals(90, spy.getSubstancesVisited().size());
        }
    }


    @Test
    public void restrictedForbiddenLinksNotDiscoverableWhenUnAuthenticated() throws Exception {
        WebCrawlerSpy spy = new WebCrawlerSpy();
        try(BrowserSession session =  ts.notLoggedInBrowserSession()) {
            WebCrawler crawler = new WebCrawler.Builder(session, spy).build();
            URL url = ts.getHomeUrl();
            crawler.crawl(url);
            assertTrue(spy.get401Links().isEmpty());
        }
    }

    @Test
    public void nothingRestrictedForAdmin() throws Exception {
        final WebCrawlerSpy spy = new WebCrawlerSpy();
        
        try(BrowserSession session =  ts.newBrowserSession(admin)) {
            WebCrawler crawler = new WebCrawler.Builder(session, spy).build();
            URL url = ts.getHomeUrl();
            
            crawler.crawl(url);

            assertTrue("Admins should not find any restricted links, found:" + spy.get401Links().toString(),spy.get401Links().isEmpty());
        }

    }
    @Test
    public void findNo404sOnCrawl() throws Exception {
        WebCrawlerSpy spy = new WebCrawlerSpy();
        try(BrowserSession session =  ts.notLoggedInBrowserSession()) {
            WebCrawler crawler = new WebCrawler.Builder(session, spy).build();
            URL url = ts.getHomeUrl();

            crawler.crawl(url);


            if(!spy.get404Paths().isEmpty()){
            	System.err.println("404 links:");
            	spy.get404Paths().stream().forEach(l->{
            		System.err.println("Path to 404:");
            		l.stream().forEach(href->{
            			System.err.println("\t+" + href.toString());
            		});
            	});
            }

            assertTrue(spy.get404Paths().isEmpty());
            
        }

    }
    
    @Test
    public void findNoInternalServerErrorsOnCrawl() throws Exception {
        WebCrawlerSpy spy = new WebCrawlerSpy();
        try(BrowserSession session =  ts.notLoggedInBrowserSession()) {
            WebCrawler crawler = new WebCrawler.Builder(session, spy).build();
            URL url = ts.getHomeUrl();

            crawler.crawl(url);
            if(!spy.getErrorPaths().isEmpty()){
            	System.err.println("Internal Error links:");
            	System.err.println(spy.get404Paths().toString());
            }
            assertTrue(spy.getErrorPaths().isEmpty());
            
        }

    }

    @Test
    public void restrictedLinksAreAccessibleFromAdmin() throws Exception {
        WebCrawlerSpy spy = new WebCrawlerSpy();
        try(BrowserSession session =  ts.notLoggedInBrowserSession()) {
            WebCrawler crawler = new WebCrawler.Builder(session, spy).build();

            crawler.crawl(ts.getHomeUrl());

            Set<URL> links = spy.get401Links();
            try (BrowserSession adminSession = ts.newBrowserSession(admin)) {
                for (URL url : links) {
                    WSResponse response = adminSession.get(url);
                    assertEquals(200, response.getStatus());
                }
            }
        }

    }

    private static final class WebCrawlerSpy extends AbstractWebCrawlerSpy{

        private static final Pattern SUBSTANCES_ROOT_PATTERN = Pattern.compile("/ginas/app/substance/([a-z0-9]+)$");
        private final Set<String> substancesVisited = new LinkedHashSet<>();

        private final Set<URL> _401Links = new LinkedHashSet<>();
        private final Set<List<URL>> _404Paths = new LinkedHashSet<List<URL>>();
        private final Set<List<URL>> _InternalErrorPaths = new LinkedHashSet<List<URL>>();
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
        protected void visitErrorURL(URL url, int statusCode, String statusMessage, List<URL> path) {
            if(statusCode ==401){
                
                _401Links.add(url);
            }else if(statusCode == 404){
            	System.out.println("404\t" + url + "\t" + path.size());
            	_404Paths.add(new ArrayList<URL>(path));
            }else if(statusCode >= 500){
            	_InternalErrorPaths.add(new ArrayList<URL>(path));
            }
        }

        public Set<String> getSubstancesVisited() {
            return substancesVisited;
        }

        public Set<URL> get401Links() {
            return _401Links;
        }
        public Set<List<URL>> get404Paths() {
			return _404Paths;
		}
        public Set<List<URL>> getErrorPaths() {
			return _InternalErrorPaths;
		}
    }

    private static abstract class AbstractWebCrawlerSpy implements WebCrawler.WebCrawlerVisitor{
        @Override
        public boolean shouldVisit(URL url) {
            return true;
        }
        public void visited(URL url, int statusCode, String statusMessage, List<URL> path) {
            if(statusCode == 200 || statusCode == 201){
                visitValidURL(url);
            }else{
                visitErrorURL(url, statusCode, statusMessage, path);
            }
        }
        
        protected void visitValidURL(URL url){
        }

        protected void visitErrorURL(URL url, int statusCode, String statusMessage, List<URL> path){

        }

    }
}
