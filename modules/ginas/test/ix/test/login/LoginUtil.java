package ix.test.login;

import ix.test.server.GinasTestServer;
import play.libs.ws.WSResponse;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.Assert.assertEquals;

/**
 * Created by katzelda on 3/23/16.
 */
public final class LoginUtil {

    private static Pattern LOGGED_IN_AS_PATTERN = Pattern.compile("username:\\s*(\\S+)?");

    private LoginUtil(){
        //can not instantiate
    }

    public static void ensureNotLoggedIn(WSResponse response){
    	ensureLoggedInAs(response, "null");
    }
    public static void ensureLoggedInAs(WSResponse response, GinasTestServer.User user){
        ensureLoggedInAs(response, user.getUserName());

    }

    public static void ensureLoggedInAs(WSResponse response, String username) {
        String body = response.getBody();

        Matcher matcher = LOGGED_IN_AS_PATTERN.matcher(body);
        if(!matcher.find()){
            throw new IllegalStateException("could not parse username from session:" + body);
        }
        String foundName = unquote(matcher.group(1));
        assertEquals(username, foundName);
    }

    private static String unquote(String s){
        return s.replaceAll("\"", "");
    }

}
