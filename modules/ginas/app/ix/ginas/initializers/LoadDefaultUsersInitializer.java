package ix.ginas.initializers;

import ix.core.controllers.PrincipalFactory;
import ix.core.controllers.UserProfileFactory;
import ix.core.initializers.Initializer;
import ix.core.models.Principal;
import ix.core.models.UserProfile;
import play.Application;
import play.Logger;

import java.util.List;
import java.util.Map;

/**
 * Created by katzelda on 4/30/18.
 */
public class LoadDefaultUsersInitializer implements Initializer {

    @Override
    public void onStart(Application app) {

        List<Object> ls = app.configuration().getList("ix.core.users", null);

        if (ls != null) {
            for (Object o : ls) {
                if (o instanceof Map) {
                    Map m = (Map) o;
                    String username = (String) m.get("username");
                    String email = (String) m.get("email");
                    String password = (String) m.get("password");
                    List roles = (List) m.get("roles");
                    List groups = (List) m.get("groups");

                    Principal p = new Principal(username, email);

                    Principal p2 = PrincipalFactory.byUserName(username);
                    if (p2 == null) {
                        try {
                            UserProfile up = UserProfileFactory.addActiveUser(p, password, roles, groups);
                        } catch (Exception e) {
                            Logger.error(username + "failed");
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }
}
