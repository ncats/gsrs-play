package ix.test;
import static org.fest.assertions.Assertions.assertThat;
import static play.mvc.Http.Status.OK;
import static play.test.Helpers.GET;
import static play.test.Helpers.contentAsString;
import static play.test.Helpers.contentType;
import static play.test.Helpers.fakeRequest;
import static play.test.Helpers.route;
import static play.test.Helpers.status;

import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;

import ix.core.controllers.search.SearchFactory;
import play.mvc.Result;
import play.test.FakeRequest;
import play.test.WithApplication;

public class FunctionalTest extends WithApplication {

    @Rule
    public GinasTestServer ts = new GinasTestServer(9001);

    

    @Test
    public void testRouteGinasHome() {
    	 ts.run(new GinasTestServer.ServerWorker() {
             public void doWork() throws Exception {
            	 ts.loginFakeUser1();
            	 Result result = route(fakeRequest(GET, "/ginas/app"));
                 assertThat(result).isNotNull();
                 assertThat(status(result)).isEqualTo(OK);
                 assertThat(contentType(result)).isEqualTo("text/html");
                 assertThat(contentAsString(result)).contains("login");

                }
            });

    }

   @Test
    public void testRouteSubstance(){
	   SearchFactory.init();
	   ts.run(new GinasTestServer.ServerWorker() {
           public void doWork() throws Exception {
        	   ts.loginFakeUser1();
	        	   FakeRequest request = new FakeRequest("GET", "/ginas/app/substances");
	               Result result = route(request);
	               assertThat(status(result)).isEqualTo(OK);
	               assertThat(contentType(result)).isEqualTo("text/html");
	               assertThat(contentAsString(result)).contains("substances");

              }
          });
    }

    @Test
    public void testRouteLogin() {
    	 ts.run(new GinasTestServer.ServerWorker() {
             public void doWork() throws Exception {
            	 ts.loginFakeUser1();
            	 FakeRequest request = new FakeRequest("GET", "/ginas/app/login");
                 Result result = route(request);
                 assertThat(status(result)).isEqualTo(OK);
                 assertThat(contentType(result)).isEqualTo("text/html");
                 assertThat(contentAsString(result)).contains("ginas");
                }
            });
    }

    @Ignore("waiting on login rewrite")
    @Test
    public void testRouteChemicalWizard() {
    	 ts.run(new GinasTestServer.ServerWorker() {
             public void doWork() throws Exception {
            	 ts.loginFakeUser1();
            	 FakeRequest request = new FakeRequest("GET", "/ginas/app/wizard?kind=chemical");
                 Result result = route(request);
                 String content = contentAsString(result);
                 assertThat(status(result)).isEqualTo(OK);
                 assertThat(contentType(result)).isEqualTo("text/html");
                 assertThat(content).contains("Structure");
                 assertThat(content).contains("moiety-form");
                 testCommonWizardElements(content);
                }
            });
    }
    @Ignore("waiting on login rewrite")
    @Test
    public void testRouteProteinWizard() {
    	ts.run(new GinasTestServer.ServerWorker() {
            public void doWork() throws Exception {
            	 ts.loginFakeUser1();
            	 FakeRequest request = new FakeRequest("GET", "/ginas/app/wizard?kind=protein");
                 Result result = route(request);
                 String content = contentAsString(result);
                 assertThat(status(result)).isEqualTo(OK);
                 assertThat(contentType(result)).isEqualTo("text/html");
                 assertThat(content).contains("other-links-form");
                 assertThat(content).contains("protein-details-form");
                 assertThat(content).contains("subunit-form");
                 assertThat(content).contains("disulfide-link-form");
                 assertThat(content).contains("glycosylation-form");
                 assertThat(content).contains("agent-modification-form");
                 assertThat(content).contains("structural-modification-form");
                 assertThat(content).contains("physical-modification-form");
                 testCommonWizardElements(content);
               }
           });
    }
    @Ignore("waiting on login rewrite")
    @Test
    public void testRouteStructurallyDiverseWizard(){
    	ts.run(new GinasTestServer.ServerWorker() {
            public void doWork() throws Exception {
            	ts.loginFakeUser1();
            	FakeRequest request = new FakeRequest("GET", "/ginas/app/wizard?kind=structurallyDiverse");
                Result result = route(request);
                String content = contentAsString(result);
                assertThat(status(result)).isEqualTo(OK);
                assertThat(contentType(result)).isEqualTo("text/html");
                assertThat(content).contains("diverse-type-form");
                assertThat(content).contains("diverse-source-form");
                assertThat(content).contains("diverse-organism-form");
                assertThat(content).contains("diverse-details-form");
                assertThat(content).contains("parent-form");
                assertThat(content).contains("part-form");
                testCommonWizardElements(content);
               }
           });
    }

    @Ignore("waiting on login rewrite")
    @Test
    public void testRoutePolymerWizard(){
    	ts.run(new GinasTestServer.ServerWorker() {
            public void doWork() throws Exception {
            	ts.loginFakeUser1();
            	FakeRequest request = new FakeRequest("GET", "/ginas/app/wizard?kind=polymer");
                Result result = route(request);
                String content = contentAsString(result);
                assertThat(status(result)).isEqualTo(OK);
                assertThat(contentType(result)).isEqualTo("text/html");
                assertThat(content).contains("polymer-classification-form");
                assertThat(content).contains("polymer-monomer-form");
                assertThat(content).contains("polymer-sru-form");
                //assertThat(content).contains("Structural Units");
                testCommonWizardElements(content);
               }
           });
    }
    @Ignore("waiting on login rewrite")
    @Test
    public void testRouteNucleicAcidWizard(){
    	ts.run(new GinasTestServer.ServerWorker() {
            public void doWork() throws Exception {
            	ts.loginFakeUser1();
            	FakeRequest request = new FakeRequest("GET", "/ginas/app/wizard?kind=nucleicAcid");
                Result result = route(request);
                String content = contentAsString(result);
                assertThat(status(result)).isEqualTo(OK);
                assertThat(contentType(result)).isEqualTo("text/html");
                assertThat(content).contains("nucleic-acid-details-form");
                assertThat(content).contains("subunit-form");
                assertThat(content).contains("nucleic-acid-sugar-form");
                assertThat(content).contains("nucleic-acid-linkage-form");
                testCommonWizardElements(content);
               }
           });
    }

    @Ignore("waiting on login rewrite")
    @Test
    public void testRouteConceptWizard() {
    	ts.run(new GinasTestServer.ServerWorker() {
            public void doWork() throws Exception {
            	ts.loginFakeUser1();
            	FakeRequest request = new FakeRequest("GET", "/ginas/app/wizard?kind=concept");
                Result result = route(request);
                String content = contentAsString(result);
                assertThat(status(result)).isEqualTo(OK);
                assertThat(contentType(result)).isEqualTo("text/html");
                testCommonWizardElements(content);
               }
           });
    }

    public void testCommonWizardElements(String content){
        assertThat(content).contains("name-form");
        assertThat(content).contains("code-form");
        assertThat(content).contains("relationship-form");
        assertThat(content).contains("note-form");
        assertThat(content).contains("property-form");
        assertThat(content).contains("reference-form-only");
    }

}
