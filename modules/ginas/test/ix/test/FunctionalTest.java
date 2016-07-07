package ix.test;


import ix.test.ix.test.server.GinasTestServer;
import ix.test.ix.test.server.RestSession;
import ix.test.util.TestNamePrinter;

import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import static org.junit.Assert.*;

import org.junit.rules.TestRule;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import play.test.WithApplication;

public class FunctionalTest {

    @Rule
    public TestNamePrinter printer = new TestNamePrinter();

    @Rule
    public GinasTestServer ts = new GinasTestServer();



    @Test
    public void loggedInUserHasLogout()   throws Exception {
        try(RestSession session = ts.newRestSession(ts.getFakeUser1())){
             String content = session.get("ginas/app").getBody();

             assertContains(content,"logout");

         }

    }

   @Test
    public void testRouteSubstance() throws Exception {
       try(RestSession session = ts.newRestSession(ts.getFakeUser1())){
           String content = session.get("ginas/app/substances").getBody();

           assertContains(content,"substances");

       }
    }
    @Test
    public void testRouteLogin() throws Exception {
        try(RestSession session = ts.newRestSession(ts.getFakeUser1())){
            String content = session.get("ginas/app/login").getBody();

            assertContains(content,"ginas");
        }
    }
    @Test
    public void testRouteChemicalWizard() throws Exception {
        try(RestSession session = ts.newRestSession(ts.getFakeUser1())){
            String content = session.get("ginas/app/wizard?kind=chemical").getBody();
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
            String content = session.get("ginas/app/wizard?kind=protein").getBody();

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

            String content = session.get("ginas/app/wizard?kind=structurallyDiverse").getBody();
            assertContains(content,"diverse-type-form");
            assertContains(content,"diverse-source-form");
            assertContains(content,"diverse-organism-form");
            assertContains(content,"parent-form");
            assertContains(content,"part-form");
            testCommonWizardElements(content);
        }
    }
    
    //@Ignore("waiting on login rewrite")
    @Test
    public void testRoutePolymerWizard()  throws Exception {
        try(RestSession session = ts.newRestSession(ts.getFakeUser1())){
            String content = session.get("ginas/app/wizard?kind=polymer").getBody();
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

            String content = session.get("ginas/app/wizard?kind=nucleicAcid").getBody();
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
            String content = session.get("ginas/app/wizard?kind=concept").getBody();
            
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
    
    public static void assertContains(String within,String find){
    	String rep=within;
    	if(rep.length()>20){
    		rep=rep.substring(0, 20) + " ... {" + (within.length()-20) +"}" ;
    	}
    	assertTrue("Should have found:'" + find + "' in '" + rep + "'" ,within.contains(find));
    }

}
