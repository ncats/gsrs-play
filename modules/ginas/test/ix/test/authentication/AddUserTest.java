package ix.test.authentication;

import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlTableRow;
import ix.AbstractGinasServerTest;
import ix.AbstractGinasTest;
import ix.core.adapters.EntityPersistAdapter;
import ix.core.controllers.UserProfileFactory;
import ix.core.factories.EntityProcessorFactory;
import ix.core.models.Principal;
import ix.core.models.Role;
import ix.core.util.StreamUtil;
import ix.test.server.BrowserSession;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.stream.StreamSupport;

import static org.junit.Assert.*;

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

        assertEquals(1, StreamUtil.forIterator(UserProfileFactory.users()).filter(up -> up.user.username.equals("fakeUser"))
                                                    .count());

    }


}
