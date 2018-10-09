package ix.test.login;

import static org.junit.Assert.*;

import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;

import ix.AbstractGinasServerTest;
import ix.core.models.Session;
import ix.test.server.BrowserSession;
import ix.test.server.RestSession;

/**
 * Simple tests to make sure that sessions
 * are handled correctly.
 * 
 * @author tyler
 *
 */
public final class SessionHistoryTest extends AbstractGinasServerTest {

    
    /**
     * Confirms that a user, after logging in and out 3 times, does not have the
     * resulting {@link Session} object stored in the edits resource.
     * @throws Exception
     */
	@Test
	public void testSessionEditsNotStoredInEditResource() throws Exception{
		
		//Log in and out 3 times
        try( BrowserSession session = ts.newBrowserSession(ts.getFakeUser1())) {}
        try( BrowserSession session = ts.newBrowserSession(ts.getFakeUser1())) {}
        try( BrowserSession session = ts.newBrowserSession(ts.getFakeUser1())) {}
        
        
        try( RestSession session = ts.newRestSession(ts.getFakeUser1())) {
        	JsonNode jsn = session.getAsJson(ts.getHttpResolver().apiV1("edits"));
        	JsonNode edits = jsn.at("/content");
        	assertEquals(0, edits.size());
        }
        
	}

}
