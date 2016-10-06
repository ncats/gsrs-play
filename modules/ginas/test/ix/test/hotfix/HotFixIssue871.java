package ix.test.hotfix;

import static org.junit.Assert.assertEquals;
import ix.test.server.BrowserSession;
import ix.test.server.SubstanceLoader;
import ix.test.load.AbstractLoadDataSetTest;

import java.io.File;
import java.io.IOException;

import org.junit.Test;

public class HotFixIssue871 extends AbstractLoadDataSetTest{
	 	@Test
	    public void viewingSubstanceWithNonExistentRelatedSubstanceInDatabaseWithConceptShouldNotFail()throws IOException {
	            try(BrowserSession session = ts.newBrowserSession(admin)) {

	                SubstanceLoader loader = new SubstanceLoader(session);

	                File f = new File("test/testdumps/smallMention.txt");

	                loader.loadJson(f);
	                
	                assertEquals(200,session.get("ginas/app/substance/8a184573").getStatus());
	                
	            }

	    }
}
