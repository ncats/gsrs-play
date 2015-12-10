import ix.core.models.Group;
import ix.core.models.Principal;
import ix.core.models.Role;
import ix.core.models.UserProfile;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.fest.assertions.Assertions.assertThat;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;



/**
*
* Simple (JUnit) tests that can call all parts of a play app.
* If you are interested in mocking a whole application, see the wiki for more details.
*
*/
public class ApplicationTest {

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
   /* @Test
    public void renderTemplate() {

        Content html = ix.ginas.views.html.login.render();
        //Content html = views.html.index.render("Your new application is ready.");
        assertEquals("text/html", contentType(html));
        assertTrue(contentAsString(html).contains("Your new application is ready."));
    }*/

    @Test
    public void createPrincipal() {
          Principal pri = new Principal("testuser", "");
                assertThat(pri.username).isNotNull();
                assertThat(pri.username).isEqualTo("testuser");
    }


    @Test
    public void isAdmin(){
        Principal user = new Principal("John Test", "");
        assertFalse(user.isAdmin());
    }

    @Test
    public void hasRoles(){
        // Create and train mock repository
        UserProfile mockProfile = mock(UserProfile.class);
        List<Role> roles = new ArrayList<Role>();
        roles.add(Role.newAdmin());
        when(mockProfile.getRoles()).thenReturn(roles);

        Principal user = new Principal("John Test", "");
        mockProfile.user = user;
        assertTrue(!mockProfile.getRoles().isEmpty() && mockProfile.user.username.equals("John Test"));

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

/*

    @Test
    public void testRestEndpoint() throws Exception {
        FakeRequest request = new FakeRequest("GET", "/ginas/app/substances ");
        Result result = route(request);
        assertThat(status(result)).isEqualTo(OK);
        assertThat(contentType(result)).isEqualTo("application/html");
        assertThat(contentAsString(result)).contains("substances");
    }


*/

}
