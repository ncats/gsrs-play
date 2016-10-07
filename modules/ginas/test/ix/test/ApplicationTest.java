package ix.test;
import ix.core.models.Role;
import ix.core.models.Group;
import ix.core.models.Principal;
import ix.core.models.UserProfile;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static org.fest.assertions.Assertions.assertThat;


/**
*
* Simple (JUnit) tests that can call all parts of a play app.
* If you are interested in mocking a whole application, see the wiki for more details.
*
*/
public class ApplicationTest extends AbstractGinasTest{

    


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
        roles.add(Role.Admin);
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
