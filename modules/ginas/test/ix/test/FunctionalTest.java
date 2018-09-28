package ix.test;


import static ix.test.util.TestUtil.assertContains;

import org.junit.Test;

import ix.AbstractGinasServerTest;
import ix.test.server.RestSession;

public class FunctionalTest  extends AbstractGinasServerTest {

    @Test
    public void loggedInUserHasLogout()   throws Exception {
        try(RestSession session = ts.newRestSession(ts.getFakeUser1())){
            String content = session.get(ts.getHttpResolver().get("")).getBody();
            assertContains(content,"logout");
        }
    }

    @Test
    public void testRouteSubstance() throws Exception {
        try(RestSession session = ts.newRestSession(ts.getFakeUser1())){
            String content = session.get(ts.getHttpResolver().get("substances")).getBody();

            assertContains(content,"substances");

        }
    }
    @Test
    public void testRouteLogin() throws Exception {
        try(RestSession session = ts.newRestSession(ts.getFakeUser1())){
            String content = session.get(ts.getHttpResolver().get("login")).getBody();

            assertContains(content,"ginas");
        }
    }
    @Test
    public void testRouteChemicalWizard() throws Exception {
        try(RestSession session = ts.newRestSession(ts.getFakeUser1())){
            String content = session.get(ts.getHttpResolver().get("wizard?kind=chemical")).getBody();
            assertContains(content,"structure-form");
            assertContains(content,"moiety-form");
            testCommonWizardElements(content);
        }catch(Throwable t){
            t.printStackTrace();
            throw t;
        }
    }
    @Test
    public void testRouteProteinWizard() throws Exception {
        try(RestSession session = ts.newRestSession(ts.getFakeUser1())){
            String content = session.get(ts.getHttpResolver().get("/wizard?kind=protein")).getBody();

            assertContains(content,"other-links-form");
            assertContains(content,"protein-details-form");
            assertContains(content,"subunit-form");
            assertContains(content,"disulfide-link-form");
            assertContains(content,"glycosylation-form");
            assertContains(content,"agent-modification-form");
            assertContains(content,"structural-modification-form");
            assertContains(content,"physical-modification-form");
            testCommonWizardElements(content);
        }
    }
    @Test
    public void testRouteStructurallyDiverseWizard() throws Exception {
        try(RestSession session = ts.newRestSession(ts.getFakeUser1())){

            String content = session.get(ts.getHttpResolver().get("wizard?kind=structurallyDiverse")).getBody();
            assertContains(content,"diverse-type-form");
            assertContains(content,"diverse-source-form");
            assertContains(content,"diverse-organism-form");
            assertContains(content,"parent-form");
            assertContains(content,"part-form");
            testCommonWizardElements(content);
        }
    }

    @Test
    public void testRoutePolymerWizard()  throws Exception {
        try(RestSession session = ts.newRestSession(ts.getFakeUser1())){
            String content = session.get(ts.getHttpResolver().get("wizard?kind=polymer")).getBody();
            assertContains(content,"polymer-classification-form");
            assertContains(content,"polymer-monomer-form");
            assertContains(content,"polymer-sru-form");
            //assertThat(content).contains("Structural Units");
            testCommonWizardElements(content);
        }
    }

    @Test
    public void testRouteNucleicAcidWizard() throws Exception {
        try(RestSession session = ts.newRestSession(ts.getFakeUser1())){

            String content = session.get(ts.getHttpResolver().get("wizard?kind=nucleicAcid")).getBody();
            assertContains(content,"nucleic-acid-details-form");
            assertContains(content,"subunit-form");
            assertContains(content,"nucleic-acid-sugar-form");
            assertContains(content,"nucleic-acid-linkage-form");
            testCommonWizardElements(content);
        }
    }

    @Test
    public void testRouteConceptWizard() throws Exception {

        try(RestSession session = ts.newRestSession(ts.getFakeUser1())){
            String content = session.get(ts.getHttpResolver().get("wizard?kind=concept")).getBody();

            testCommonWizardElements(content);
        }
    }

    public void testCommonWizardElements(String content){
        assertContains(content,"name-form");
        assertContains(content,"code-form");
        assertContains(content,"relationship-form");
        assertContains(content,"note-form");
        assertContains(content,"property-form");
        assertContains(content,"reference-form");
    }


}
