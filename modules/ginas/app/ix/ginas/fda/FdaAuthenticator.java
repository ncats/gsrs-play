package ix.ginas.fda;

import ix.core.auth.AuthenticationCredentials;
import ix.core.auth.Authenticator;
import ix.core.auth.DefaultAuthenticator;
import ix.core.models.UserProfile;
import ix.ncats.controllers.auth.Authentication;
import play.mvc.Http;

/**
 * Created by katzelda on 5/4/16.
 */
public class FdaAuthenticator implements Authenticator {
    @Override
    public UserProfile authenticate(AuthenticationCredentials credentials) {

        Http.Context context = credentials.getContext();
        if(context !=null && Authentication.loginUserFromHeader(context.request())){
            return Authentication.getUserProfile();
        }
        return null;
    }
}
