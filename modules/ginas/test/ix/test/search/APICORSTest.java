package ix.test.search;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.BeforeClass;
import org.junit.Test;


import ix.AbstractGinasClassServerTest;
import ix.core.models.Role;

import ix.test.server.GinasTestServer.User;
import ix.test.server.RestSession;
import ix.test.server.SubstanceAPI;
import play.libs.ws.WSResponse;

public class APICORSTest extends AbstractGinasClassServerTest{

    public static RestSession session;
    public static SubstanceAPI api;
    
    
    @BeforeClass
    public static void LoadData(){
        User u = ts.createUser(Role.Admin);
        session = ts.newRestSession(u);
        api = new SubstanceAPI(session);
        
        
    }
    
    /*
     * Access-Control-Allow-Headers: Origin, X-Requested-With, Content-Type, Accept
Access-Control-Allow-Methods: POST, PUT, GET, PATCH
Access-Control-Allow-Origin: *
Access-Control-Max-Age: 300

     */
    @Test
    public void httpAPIGetRequestHasAccessControlAllowHeaders(){
    	WSResponse ws=api.fetchStructureBrowse();
    	
    	assertTrue("'Access-Control-Allow-Headers' should contain 'Origin' for CORS",ws.getHeader("Access-Control-Allow-Headers").contains("Origin"));
    	assertEquals(Stream.of("POST", "PUT", "GET", "PATCH").collect(Collectors.toSet()),
    				Arrays.stream(ws.getHeader("Access-Control-Allow-Methods").split(","))
    			         .map(f->f.trim())
    			         .collect(Collectors.toSet()));
    	assertTrue("'Access-Control-Allow-Origin' should contain a '*' for CORS",ws.getHeader("Access-Control-Allow-Origin").contains("*"));
    	
    	
    }
    
    @Test
    public void httpAPIOptionsRequestHasAccessControlAllowHeaders(){
    	WSResponse ws=session.createRequestHolder(SubstanceAPI.API_URL_STRUCTURE_BROWSE).options().get(10_000L);
    	
    	assertTrue("'Access-Control-Allow-Headers' should contain 'Origin' for CORS",ws.getHeader("Access-Control-Allow-Headers").contains("Origin"));
    	assertEquals(Stream.of("POST", "PUT", "GET", "PATCH").collect(Collectors.toSet()),
    				Arrays.stream(ws.getHeader("Access-Control-Allow-Methods").split(","))
    			         .map(f->f.trim())
    			         .collect(Collectors.toSet()));
    	assertTrue("'Access-Control-Allow-Origin' should contain a '*' for CORS",ws.getHeader("Access-Control-Allow-Origin").contains("*"));
    	
    	
    }
    
    
    
}

