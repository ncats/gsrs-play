package ix.test.crawler;

import ix.test.server.BrowserSession;
import play.libs.ws.WSResponse;

import java.util.*;
import java.util.concurrent.*;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.io.*;
import java.net.*;

import javax.swing.text.*;
import javax.swing.text.html.*;
import javax.swing.text.html.parser.DocumentParser;
import javax.swing.text.html.parser.DTD;

/**
 * A simple web crawler that does depth first traversal of a given seed
 * url. This is useful for testing web app.
 */
public class WebCrawler {
    static final Logger logger = Logger.getLogger(WebCrawler.class.getName());
   

    public enum UserAgent {

        WINDOWS_IE ("Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.1; .NET CLR 2.0.50727; .NET CLR 3.0.04506.30; .NET CLR 3.0.4506.2152; .NET CLR 3.5.30729; .NET CLR 1.1.4322)"),
        WINDOWS_MOZILLA("Mozilla/5.0 (Windows; U; Windows NT 5.0; it-IT; rv:1.7.12) Gecko/20050915"),
        LINUX_MOZILLA("Mozilla/5.0 (X11; U; Linux i686; en-US; rv:1.0.1) Gecko/20020919"),
        WINDOWS_CHROME("Mozilla/5.0 (Windows; U; Windows NT 5.1; en-US) AppleWebKit/525.19 (KHTML, like Gecko) Chrome/1.0.154.53 Safari/525.19");

        private final String header;

        private UserAgent(String header){
            this.header = header;
        }

        void setUserAgentHeader(HttpURLConnection connection){
            connection.setRequestProperty("User-Agent", header);
        }

    }
//    static final String[] AGENTS = {
//        "Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.1; .NET CLR 2.0.50727; .NET CLR 3.0.04506.30; .NET CLR 3.0.4506.2152; .NET CLR 3.5.30729; .NET CLR 1.1.4322)",
//        "Mozilla/5.0 (X11; U; Linux i686; en-US) AppleWebKit/534.1 SUSE/6.0.428.0 (KHTML, like Gecko) Chrome/6.0.428.0 Safari/534.1",
//        "Mozilla/5.0 (Windows; U; Windows NT 6.1; en-US) AppleWebKit/534.1 (KHTML, like Gecko) Chrome/6.0.428.0 Safari/534.1",
//        "Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10_6_3; en-US) AppleWebKit/534.1 (KHTML, like Gecko) Chrome/6.0.428.0 Safari/534.1",
//        "Mozilla/5.0 (Windows; U; Windows NT 5.1; en-US; rv:1.9.2.3) Gecko/20100401"
//    };

    public interface WebCrawlerVisitor{
        boolean shouldVisit(URL url);

        void visited(URL url, int statusCode, String statusMessage, List<URL> path);
    }

    static class HtmlParser extends HTMLEditorKit.ParserCallback {
        URL url;
        List<URL> hrefs = new ArrayList<URL>();
        int status;
        
        private BrowserSession session;

        HtmlParser (URL url, BrowserSession session,  WebCrawlerVisitor visitor, List<URL> path) throws Exception {
            this.url = url;

            WSResponse resp = session.get(url);
            status = resp.getStatus();
            
            String message = resp.getStatusText();
            visitor.visited(url, status, resp.getStatusText(),path);
            if(status == 200){
                DocumentParser doc = new DocumentParser (DTD.getDTD("html"));
                doc.parse(new StringReader(resp.getBody()), this, true);
            }else{
                try{
                    //System.out.println("GOT:######" + status + "\n\n\n" + resp.getBody());
                }catch(Exception e){
                    e.printStackTrace();
                }
            }
            
        }

        @Override
        public void handleStartTag (HTML.Tag t,
                                    MutableAttributeSet attr, int pos) {
        }
        
        @Override
        public void handleEndTag (HTML.Tag t, int pos) {
        }
        
        @Override
        public void handleSimpleTag 
            (HTML.Tag t, MutableAttributeSet a, int pos) {
            MutableAttributeSet attr = new SimpleAttributeSet (a);
            //logger.info("simpletag: "+t);
            if (t == HTML.Tag.A) {
                String url = (String)attr.getAttribute(HTML.Attribute.HREF);
                if (url != null) {
                    try {
                        URL u;
                        if (url.startsWith("?")) {
                            url = url.replaceAll("\\s", "%20");
                            
                            pos = this.url.toString().indexOf('?');
                            if (pos > 0) {
                                u = new URL (this.url.toString()
                                             .substring(0, pos)+url);
                            }
                            else {
                                u = new URL (this.url.toString()+url);
                            }
                        }
                        else {
                            u =new URL (this.url, url);
                        }
                        
                        // no linkout.. keep with within the same site
                        if (u.getHost().equals(this.url.getHost()))
                            hrefs.add(u);
                    }
                    catch (Exception ex) {
                        logger.log(Level.SEVERE, "Bogus url: "+url, ex);
                    }
                }
            }                   
        }

        public List<URL> getLinks () { return hrefs; }
    }
    
    static int MAXDEPTH = Integer.getInteger("maxdepth", 20);

    private int maxdepth;
    private Set<URL> visited = new HashSet<>();
    private final WebCrawlerVisitor visitor;

    private UserAgent agent;

    private final BrowserSession session;
    //ExecutorService executor = Executors.newFixedThreadPool(1);
    public static class Builder{
        UserAgent agent = UserAgent.LINUX_MOZILLA;

        private int maxDepth = MAXDEPTH;

        private final  WebCrawlerVisitor visitor;

        private final BrowserSession session;

        public Builder(BrowserSession session, WebCrawlerVisitor visitor){
            Objects.requireNonNull(session);
            Objects.requireNonNull(visitor);
            this.visitor = visitor;
            this.session = session;
        }

        public Builder userAgent(UserAgent agent){
            Objects.requireNonNull(agent);
            this.agent = agent;
            return this;
        }

        public Builder maxDepth( int max){
            if(max < 1){
                throw new IllegalArgumentException("max depth can not be less than 1 : "+ max);
            }
            maxDepth = max;
            return this;
        }

        public WebCrawler build(){
            return new WebCrawler(this);
        }
    }


    private WebCrawler (Builder builder) {

        this.visitor = builder.visitor;
        this.maxdepth = builder.maxDepth;
        this.agent = builder.agent;
        this.session = builder.session;

    }

    public void crawl (URL url) {
    	try{
    		depthFirstCrawl (0, url, new ArrayList<URL>());
    	}catch(Exception e){
    		throw new IllegalStateException(e);
    	}
    }

    void depthFirstCrawl (int depth, final URL url, final List<URL> path) throws TimeoutException{
    	path.add(url);
        if (depth >= maxdepth) {
            /*
            logger.warning("Max depth ("+maxdepth
                           +") reached; backtracking "+url+"!");
            */
        }
        else {
            visited.add(url);
            
            try {
                long start = System.currentTimeMillis();
                String s = url.toString().replaceAll("\\s", "+");
                String u = s.substring(s.indexOf(url.getPath()));
              //  System.out.println(u);

                if(visitor.shouldVisit(url)){
	                    
	                Callable<HtmlParser> c = new Callable<HtmlParser>(){
	
						@Override
						public HtmlParser call() throws Exception {
							HtmlParser parser = new HtmlParser (url, session, visitor, path);
							
							return parser;
						}
	                	
	                };

	                Future<HtmlParser> futureParse =  ForkJoinPool.commonPool().submit(c);
	                //fail after 60 seconds
	                HtmlParser parser=futureParse.get(60, TimeUnit.SECONDS);
	                
	                for (URL uu : parser.getLinks()) {
	                    if (!visited.contains(uu)) {
	                        depthFirstCrawl (depth+1, uu,path);
	                    }
	                }
                }
            }
            catch (TimeoutException ex) {
            	
                logger.severe(path.toString()+": "+ex.getMessage());
                System.out.println("Timeout in path: " + path.toString()+": "+ex.getMessage());

                path.remove(path.size()-1);
                throw ex;
            }
            catch (Exception ex) {
                logger.warning(url+": "+ex.getMessage());
            }
           // visited.remove(url);
        }
        path.remove(path.size()-1);
    }

//    public static void main (String[] argv) throws Exception {
//        if (argv.length == 0) {
//            System.err.println("Usage: WebCrawler URL...");
//            System.exit(1);
//        }
//
//        WebCrawler crawler = new WebCrawler.Builder (DefaultWebCrawlerVisitor.INSTANCE).build();
//        for (String u : argv) {
//            crawler.crawl(new URL (u));
//        }
//    }

    public enum DefaultWebCrawlerVisitor implements WebCrawlerVisitor{
        INSTANCE;


        @Override
        public boolean shouldVisit(URL url) {
            return true;
        }

        @Override
        public void visited(URL url, int statusCode, String statusMessage, List<URL> path) {
            //no-op
        }


    }
}
