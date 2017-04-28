package ix.test.authentication;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.junit.Test;

import ix.core.controllers.UserProfileFactory;
import ix.core.models.Principal;
import ix.core.models.Role;
import ix.core.models.UserProfile;

/**
 * Created by katzelda on 3/3/17.
 */
public class AddUserTest extends AbstractAddUserTest {

    @Test
    public void listPrincipals() throws Exception{
        //  /admin/users
        List<UserResult> actual = queryAllCurrentUsers();

        assertEquals(DEFAULT_USERS, actual);
    }
    @Test
    public void addNewUserShouldAppearInPrincipalsList() throws Exception{
        ts.createUser("newUser", "mypass", Role.DataEntry);

        List<UserResult> actual = queryAllCurrentUsers();

        List<UserResult> expected = new ArrayList(DEFAULT_USERS);
        expected.add(new UserResult("newUser", true,null));
        assertEquals(expected, actual);

    }

    @Test
    public void createNewUserWithoutAProfile(){
        Principal p = createPrincipalWithoutUserProfile("fakeUser");
        assertNull(p.getUserProfile());
    }


    @Test
    public void callingPrincipalListShouldAutoPopulateMissingUserProfiles() throws Exception{
        createPrincipalWithoutUserProfile("fakeUser");

        List<UserResult> actual = queryAllCurrentUsers();

        List<UserResult> expected = new ArrayList(DEFAULT_USERS);
        expected.add(new UserResult("fakeUser", false,null));
        assertEquals(expected, actual);

        try(Stream<UserProfile> users=UserProfileFactory.userStream()){
        	assertEquals(1, users.filter(up -> up.user.username.equals("fakeUser")).count());
        }

    }


}
