package ix.test.hotfix;

import static org.junit.Assert.assertEquals;
import ix.test.ix.test.server.BrowserSession;
import ix.test.ix.test.server.ConfigUtil;
import ix.test.ix.test.server.SubstanceLoader;
import ix.test.ix.test.server.SubstanceReIndexer;
import ix.test.ix.test.server.SubstanceSearch;
import ix.test.load.AbstractLoadDataSetTest;
import ix.test.load.TestFacetUtil;
import ix.test.util.TestUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Test;

import com.gargoylesoftware.htmlunit.FormEncodingType;
import com.gargoylesoftware.htmlunit.WebRequest;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.util.KeyDataPair;
import com.gargoylesoftware.htmlunit.util.NameValuePair;

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
