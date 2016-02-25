import ix.ginas.controllers.GinasFactory;
import org.junit.After;
import org.junit.Before;
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

    private FakeApplication fa;

    @Before
    public void startApp(){
        fa=fakeApplication();
    }

    @After
    public void stopApp(){
        stop(fa);
    }
     @Test
    public void testRouteGinasHome() {

        running(fa, new Runnable() {
            public void run() {
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
        running(fa, new Runnable() {
            public void run() {
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
        running(fa, new Runnable() {
            public void run() {
                FakeRequest request = new FakeRequest("GET", "/ginas/app/login");
                Result result = route(request);
                assertThat(status(result)).isEqualTo(OK);
                assertThat(contentType(result)).isEqualTo("text/html");
                assertThat(contentAsString(result)).contains("ginas");
            }
        });
    }

     @Test
    public void testRouteChemicalWizard() {
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
    }

    @Test
    public void testRouteProteinWizard() {
        
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
    }
    @Test
    public void testRouteStructurallyDiverseWizard(){
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
    }

    @Test
    public void testRoutePolymerWizard(){
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
    }
    @Test
    public void testRouteNucleicAcidWizard(){

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
    }

    @Test
    public void testRouteConceptWizard() {
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
