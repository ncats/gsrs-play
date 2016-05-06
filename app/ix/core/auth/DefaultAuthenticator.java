package ix.core.auth;

import ix.core.UserFetcher;
import ix.core.controllers.AdminFactory;
import ix.core.controllers.UserProfileFactory;
import ix.core.models.Principal;
import ix.core.models.UserProfile;
import ix.ncats.controllers.auth.Authentication;
import play.mvc.Http;

/**
 * Created by katzelda on 5/4/16.
 */
public class DefaultAuthenticator implements Authenticator {
    @Override
    public UserProfile authenticate(AuthenticationCredentials credentials) {
        if(credentials.getContext() ==null){
            //check by username and password
            UserProfile profile = UserProfileFactory.finder.where().eq("user.username", credentials.getUsername()).findUnique();

            if (profile != null && profile.active && AdminFactory.validatePassword(profile, new String(credentials.getPassword()))) {
               return profile;
            }
            return null;
        }else {
            return Authentication.getUserProfile();
        }
    }
}
