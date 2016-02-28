import ix.core.models.Group;
import ix.core.models.Principal;
import ix.core.models.Role;
import ix.core.models.UserProfile;
import ix.ginas.controllers.GinasFactory;

import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import play.api.mvc.Content;
import play.libs.ws.WS;
import play.libs.ws.WSResponse;
import play.mvc.Result;
import play.test.Helpers;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static org.fest.assertions.Assertions.assertThat;
import static play.mvc.Http.Status.OK;
import static play.test.Helpers.*;



/**
*
* Simple (JUnit) tests that can call all parts of a play app.
* If you are interested in mocking a whole application, see the wiki for more details.
*
*/
public class ApplicationTest  {

    
    @Test
    public void simpleCheck() {
        int a = 1 + 1;
        assertEquals(2, a);
    }
    

    @Test
    public void testString() {
        String str = "Hello world";
        assertFalse(str.isEmpty());
    }

    @Test
    public void mockTest() {
        // Create and train mock
        List<String> mockedList = mock(List.class);
        when(mockedList.get(0)).thenReturn("first");
        assertEquals("first", mockedList.get(0));
    }

    @Test
    public void createPrincipal() {
          Principal pri = new Principal("testuser", "");
                assertThat(pri.username).isNotNull();
                assertThat(pri.username).isEqualTo("testuser");
    }


    @Test
    public void isAdmin(){
        Principal user = new Principal("TestUser", "");
        assertFalse(user.isAdmin());
    }

    @Test
    public void hasRoles(){
        // Create and train mock repository
        UserProfile mockProfile = mock(UserProfile.class);
        List<Role> roles = new ArrayList<Role>();
        roles.add(new Role(Role.Kind.Admin));
        when(mockProfile.getRoles()).thenReturn(roles);
        assertTrue(!mockProfile.getRoles().isEmpty());
        verify(mockProfile).getRoles();
    }

    @Test
    public void addGroup(){
        UserProfile mockProfile = mock(UserProfile.class);
        List<Group> groups = new ArrayList<>();
        groups.add(new Group("testgroup"));
        when(mockProfile.getGroups()).thenReturn(groups);
        assertTrue(mockProfile.getGroups().size() == 1);
    }
    
   
}
