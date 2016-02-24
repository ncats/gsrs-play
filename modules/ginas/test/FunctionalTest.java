import ix.ginas.controllers.GinasFactory;
import org.junit.Test;
import play.mvc.Http;
import play.mvc.Result;
import play.test.FakeRequest;
import play.test.WithApplication;
import play.test.FakeApplication;

import static org.fest.assertions.Assertions.assertThat;
import static play.mvc.Http.Status.OK;
import static play.test.Helpers.*;
import static org.mockito.Mockito.*;

public class FunctionalTest extends WithApplication {

     @Test
    public void testRouteGinasHome() {
    	FakeApplication fa=fakeApplication();
        running(fa, new Runnable() {
            public void run() {
                Result result = route(fakeRequest(GET, "/ginas/app"));
                assertThat(result).isNotNull();
                assertThat(status(result)).isEqualTo(OK);
                assertThat(contentType(result)).isEqualTo("text/html");
                assertThat(contentAsString(result)).contains("login");
            }
        });
        stop(fa);
    }

   @Test
    public void testRouteSubstance(){
	    FakeApplication fa=fakeApplication();
        running(fa, new Runnable() {
            public void run() {
                FakeRequest request = new FakeRequest("GET", "/ginas/app/substances");
                Result result = route(request);
                assertThat(status(result)).isEqualTo(OK);
                assertThat(contentType(result)).isEqualTo("text/html");
                assertThat(contentAsString(result)).contains("substances");
            }
        });
        stop(fa);
    }

    @Test
    public void testRouteLogin() {
    	FakeApplication fa=fakeApplication();
        running(fa, new Runnable() {
            public void run() {
                FakeRequest request = new FakeRequest("GET", "/ginas/app/login");
                Result result = route(request);
                assertThat(status(result)).isEqualTo(OK);
                assertThat(contentType(result)).isEqualTo("text/html");
                assertThat(contentAsString(result)).contains("ginas");
            }
        });
        stop(fa);
    }

     @Test
    public void testRouteChemicalWizard() {
    	FakeApplication fa=fakeApplication();
        running(fa, new Runnable() {
            public void run() {
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
        stop(fa);
    }

    @Test
    public void testRouteProteinWizard() {
    	FakeApplication fa=fakeApplication();
        
        running(fa, new Runnable() {
            public void run() {
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
        stop(fa);
    }
    @Test
    public void testRouteStructurallyDiverseWizard(){
    	FakeApplication fa=fakeApplication();
        running(fa, new Runnable() {
            public void run() {
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
        stop(fa);
    }

    @Test
    public void testRoutePolymerWizard(){
    	FakeApplication fa=fakeApplication();
        running(fa, new Runnable(){
            public void run() {
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
        stop(fa);
    }
    @Test
    public void testRouteNucleicAcidWizard(){
    	FakeApplication fa=fakeApplication();
    
        running(fa, new Runnable() {
            public void run() {
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
        stop(fa);
    }

    @Test
    public void testRouteConceptWizard() {
    	FakeApplication fa=fakeApplication();
        
        
        running(fa, new Runnable() {
            public void run() {
                FakeRequest request = new FakeRequest("GET", "/ginas/app/wizard?kind=concept");
                Result result = route(request);
                String content = contentAsString(result);
                assertThat(status(result)).isEqualTo(OK);
                assertThat(contentType(result)).isEqualTo("text/html");
                testCommonWizardElements(content);
            }
        });
        stop(fa);
    }

    public void testCommonWizardElements(String content){
        assertThat(content).contains("name-form");
        assertThat(content).contains("code-form");
        assertThat(content).contains("relationship-form");
        assertThat(content).contains("note-form");
        assertThat(content).contains("property-form");
        assertThat(content).contains("reference-form-only");
    }
     /* @Test
      public void testControllerIndex() {
          running(fakeApplication(), new Runnable() {
              public void run() {
                   Http.Context context = mock(Http.Context.class);
                   Http.Context.current.set(context);
                  Result result = ix.ginas.controllers.GinasFactory.index();
                  assertThat(status(result)).isEqualTo(OK);
                  assertThat(contentType(result)).isEqualTo("text/html");
                  assertThat(charset(result)).isEqualTo("utf-8");
                  assertThat(contentAsString(result)).contains("Welcome");
              }
          });
      }*/
}
