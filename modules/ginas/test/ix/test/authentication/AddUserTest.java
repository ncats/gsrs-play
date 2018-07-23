package ix.test.authentication;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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

        assertEquals(new HashSet<>(DEFAULT_USERS), new HashSet<>(actual));
    }
    @Test
    public void addNewUserShouldAppearInPrincipalsList() throws Exception{
        ts.createUser("newUser", "mypass", Role.DataEntry);

        Set<UserResult> actual = new HashSet<>(queryAllCurrentUsers());

        Set<UserResult> expected = new HashSet<>(DEFAULT_USERS);
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

        Set<UserResult> actual = new HashSet<>(queryAllCurrentUsers());

        Set<UserResult> expected = new HashSet<>(DEFAULT_USERS);
        expected.add(new UserResult("fakeUser", false,null));
        assertEquals(expected, actual);

        try(Stream<UserProfile> users=UserProfileFactory.userStream()){
        	assertEquals(1, users.filter(up -> up.user.username.equals("fakeUser")).count());
        }

    }


}
