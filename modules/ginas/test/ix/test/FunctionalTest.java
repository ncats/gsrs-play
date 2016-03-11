package ix.test;


import org.junit.Rule;
import org.junit.Test;
import static org.junit.Assert.*;

import play.test.WithApplication;

public class FunctionalTest extends WithApplication {

    @Rule
    public GinasTestServer ts = new GinasTestServer(9001);

    

    @Test
    public void testRouteGinasHome()   throws Exception {
         try(GinasTestServer.UserSession session =  ts.loginFakeUser1()){
             String content = session.getAsString("ginas/app");

             assertTrue(content.contains("login"));

         }

    }

   @Test
    public void testRouteSubstance() throws Exception {
       try (GinasTestServer.UserSession session = ts.loginFakeUser1()) {
           String content = session.getAsString("ginas/app/substances");

           assertTrue(content.contains("substances"));

       }
    }

    @Test
    public void testRouteLogin() throws Exception {
        try(GinasTestServer.UserSession session =  ts.loginFakeUser1()) {
            String content = session.getAsString("ginas/app/login");

            assertTrue(content.contains("ginas"));
        }
    }

    @Test
    public void testRouteChemicalWizard() throws Exception {
        try (GinasTestServer.UserSession session = ts.loginFakeUser1()) {
            String content = session.getAsString("ginas/app/wizard?kind=chemical");

            assertTrue(content.contains("Structure"));
            assertTrue(content.contains("moiety-form"));
            testCommonWizardElements(content);
        }
    }
    @Test
    public void testRouteProteinWizard() throws Exception {
        try (GinasTestServer.UserSession session = ts.loginFakeUser1()) {
            String content = session.getAsString("ginas/app/wizard?kind=protein");
            assertTrue(content.contains("other-links-form"));
            assertTrue(content.contains("protein-details-form"));
            assertTrue(content.contains("subunit-form"));
            assertTrue(content.contains("disulfide-link-form"));
            assertTrue(content.contains("glycosylation-form"));
            assertTrue(content.contains("agent-modification-form"));
            assertTrue(content.contains("structural-modification-form"));
            assertTrue(content.contains("physical-modification-form"));
            testCommonWizardElements(content);
        }
    }
    
    @Test
    public void testRouteStructurallyDiverseWizard() throws Exception {
        try (GinasTestServer.UserSession session = ts.loginFakeUser1()) {

            String content = session.getAsString("ginas/app/wizard?kind=structurallyDiverse");
            assertTrue(content.contains("diverse-type-form"));
            assertTrue(content.contains("diverse-source-form"));
            assertTrue(content.contains("diverse-organism-form"));
            assertTrue(content.contains("diverse-details-form"));
            assertTrue(content.contains("parent-form"));
            assertTrue(content.contains("part-form"));
            testCommonWizardElements(content);
        }
    }

    //@Ignore("waiting on login rewrite")
    @Test
    public void testRoutePolymerWizard()  throws Exception {
        try(GinasTestServer.UserSession session =  ts.loginFakeUser1()){
            String content = session.getAsString("ginas/app/wizard?kind=polymer");
            assertTrue(content.contains("polymer-classification-form"));
            assertTrue(content.contains("polymer-monomer-form"));
            assertTrue(content.contains("polymer-sru-form"));
            //assertThat(content).contains("Structural Units");
            testCommonWizardElements(content);
       }
    }
    @Test
    public void testRouteNucleicAcidWizard() throws Exception {
        try (GinasTestServer.UserSession session = ts.loginFakeUser1()) {
            String content = session.getAsString("ginas/app/wizard?kind=nucleicAcid");
            assertTrue(content.contains("nucleic-acid-details-form"));
            assertTrue(content.contains("subunit-form"));
            assertTrue(content.contains("nucleic-acid-sugar-form"));
            assertTrue(content.contains("nucleic-acid-linkage-form"));
            testCommonWizardElements(content);
        }
    }

    @Test
    public void testRouteConceptWizard() throws Exception {
        try (GinasTestServer.UserSession session = ts.loginFakeUser1()) {
            String content = session.getAsString("ginas/app/wizard?kind=concept");

            testCommonWizardElements(content);
        }
    }

    public void testCommonWizardElements(String content){
        assertTrue(content.contains("name-form"));
        assertTrue(content.contains("code-form"));
        assertTrue(content.contains("relationship-form"));
        assertTrue(content.contains("note-form"));
        assertTrue(content.contains("property-form"));
        assertTrue(content.contains("reference-form-only"));
    }

}
