package ix.core.auth;

import ix.core.UserFetcher;
import ix.core.models.Principal;
import ix.core.models.UserProfile;
import ix.ncats.controllers.auth.Authentication;
import play.mvc.Http;

/**
 * Created by katzelda on 5/4/16.
 */
public class DefaultAuthenticator implements Authenticator {
    @Override
    public UserProfile authenticate(Http.Context ctx) {
        return Authentication.getUserProfile();
    }
}
