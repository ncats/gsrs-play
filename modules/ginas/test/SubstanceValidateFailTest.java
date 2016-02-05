import static org.fest.assertions.Assertions.assertThat;
import static org.junit.Assert.assertTrue;
import static play.mvc.Http.Status.OK;
import static play.test.Helpers.running;
import static play.test.Helpers.stop;
import static play.test.Helpers.testServer;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import org.junit.AfterClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import play.libs.ws.WS;
import play.libs.ws.WSResponse;
import play.test.WithApplication;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

//@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(Parameterized.class)
public class SubstanceValidateFailTest extends WithApplication {

    private static final String VALIDATE_URL = "http://localhost:9001/ginas/app/api/v1/substances/@validate";
	private static long timeout= 10000L;;
    
    @Parameters(name="{1}")
    static public Collection<Object[]> findstuff(){
    	List<Object[]> mylist  =  new ArrayList<Object[]>();
    	
    	File folder=null;
    	try{
    		for(String s:ResourceList.getResources(Pattern.compile(".*testJSON.*json"))){
    			System.out.println("Hey! I found this:" + s);
    		}
    		
    		folder = new File(SubstanceValidateFailTest.class.getResource("testJSON/fail").getFile());
    		
    	}catch(Exception e){
    		
    		e.printStackTrace();
    		throw new IllegalStateException(e);
    	}
    	assertTrue(folder.exists());
    	for(File s:folder.listFiles()){
    		if(s.getName().endsWith(".json")){
    			mylist.add(new Object[]{s, s.getName()});
    		}
    	}
    	return mylist;
    }

    File resource;
    public SubstanceValidateFailTest(File f, String dummy){
    	this.resource=f;
    }
        
    @Test
    public void testAPIValidateSubstance() {
    	    	
        running(testServer(9001), new Runnable() {
            public void run() {
				try (InputStream is=new FileInputStream(resource);){
					JsonNode js=null;
					js = (new ObjectMapper()).readTree(is);
	            	System.out.println("Running: " + resource);
	                WSResponse wsResponse1 = WS.url(SubstanceValidateFailTest.VALIDATE_URL).post(js).get(timeout);
	                JsonNode jsonNode1 = wsResponse1.asJson();
	                assertThat(wsResponse1.getStatus()).isEqualTo(OK);
	                assertThat(!jsonNode1.isNull()).isEqualTo(true);
	                assertThat(jsonNode1.get("valid").asBoolean()).isEqualTo(false);

				} catch (Exception e1) {
					throw new IllegalStateException(e1);
				}             	
            }
        });
        
        stop(testServer(9001));
    }
    


    

 /*   @Test
    public void testBrowserConcept() throws Exception {
        running(testServer(9001), HTMLUNIT , new F.Callback<TestBrowser>() {

            @Override
            public void invoke(TestBrowser browser) throws Throwable{
                browser.goTo("http://localhost:9001/ginas/app");
                assertThat(!browser.$("title").isEmpty());
*//*
                WebClient webClient = new WebClient(BrowserVersion.FIREFOX_24);
                final HtmlPage page =
                        (HtmlPage)webClient.getPage("http://localhost:9001/ginas/app/wizard?kind=concept");
                webClient.getOptions().setThrowExceptionOnScriptError(false);
*//*

               //assertThat(page.getBody()..contains("geetha"));
            }
        });
    }*/

  /*  @Test
    public void testJsonLoad() throws Exception {

    running(fakeApplication(), new Runnable() {
        public void run() {

            PayloadPlugin payloadPlugin = Play.application().plugin(
                    PayloadPlugin.class);

            FileInputStream fs = null;
            try {
                fs = new FileInputStream("test/testdumps/aspirinsetsmall.txt.gz");
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

            Payload  sdpayload = null;
            try {
                sdpayload = payloadPlugin.createPayload("aspirinsetsmall.txt.gz",
                            "application/gzip", fs.toString() );
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (sdpayload != null) {
                sdpayload.save();
                Map<String, GinasSDFUtils.GinasSDFExtractor.FieldStatistics> m = GinasSDFUtils.GinasSDFExtractor
                        .getFieldStatistics(sdpayload, 100);
                assertThat(m.values() != null);
            }
        }
    });
    }*/
    public static class ResourceList{

        /**
         * for all elements of java.class.path get a Collection of resources Pattern
         * pattern = Pattern.compile(".*"); gets all resources
         * 
         * @param pattern
         *            the pattern to match
         * @return the resources in the order they are found
         */
        public static Collection<String> getResources(
            final Pattern pattern){
            final ArrayList<String> retval = new ArrayList<String>();
            final String classPath = System.getProperty("java.class.path", ".");
            final String[] classPathElements = classPath.split(System.getProperty("path.separator"));
            for(final String element : classPathElements){
                retval.addAll(getResources(element, pattern));
            }
            return retval;
        }

        private static Collection<String> getResources(
            final String element,
            final Pattern pattern){
            final ArrayList<String> retval = new ArrayList<String>();
            final File file = new File(element);
            if(file.isDirectory()){
                retval.addAll(getResourcesFromDirectory(file, pattern));
            } else{
                retval.addAll(getResourcesFromJarFile(file, pattern));
            }
            return retval;
        }

        private static Collection<String> getResourcesFromJarFile(
            final File file,
            final Pattern pattern){
            final ArrayList<String> retval = new ArrayList<String>();
            ZipFile zf;
            try{
                zf = new ZipFile(file);
            } catch(final ZipException e){
                throw new Error(e);
            } catch(final IOException e){
                throw new Error(e);
            }
            final Enumeration e = zf.entries();
            while(e.hasMoreElements()){
                final ZipEntry ze = (ZipEntry) e.nextElement();
                final String fileName = ze.getName();
                final boolean accept = pattern.matcher(fileName).matches();
                if(accept){
                    retval.add(fileName);
                }
            }
            try{
                zf.close();
            } catch(final IOException e1){
                throw new Error(e1);
            }
            return retval;
        }

        private static Collection<String> getResourcesFromDirectory(
            final File directory,
            final Pattern pattern){
            final ArrayList<String> retval = new ArrayList<String>();
            final File[] fileList = directory.listFiles();
            for(final File file : fileList){
                if(file.isDirectory()){
                    retval.addAll(getResourcesFromDirectory(file, pattern));
                } else{
                    try{
                        final String fileName = file.getCanonicalPath();
                        final boolean accept = pattern.matcher(fileName).matches();
                        if(accept){
                            retval.add(fileName);
                        }
                    } catch(final IOException e){
                        throw new Error(e);
                    }
                }
            }
            return retval;
        }

        /**
         * list the resources that match args[0]
         * 
         * @param args
         *            args[0] is the pattern to match, or list all resources if
         *            there are no args
         */
        public static void main(final String[] args){
            Pattern pattern;
            if(args.length < 1){
                pattern = Pattern.compile(".*");
            } else{
                pattern = Pattern.compile(args[0]);
            }
            final Collection<String> list = ResourceList.getResources(pattern);
            for(final String name : list){
                System.out.println(name);
            }
        }
    }  
    @AfterClass
    public static void tearDown(){
        // Stop the server
       // stop(testServer);
    }
}
