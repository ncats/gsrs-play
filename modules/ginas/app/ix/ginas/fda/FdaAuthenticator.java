package ix.ginas.fda;

import ix.core.auth.DefaultAuthenticator;
import ix.core.models.UserProfile;
import ix.ncats.controllers.auth.Authentication;
import play.mvc.Http;

/**
 * Created by katzelda on 5/4/16.
 */
public class FdaAuthenticator extends DefaultAuthenticator{

    @Override
    public UserProfile authenticate(Http.Context ctx) {
        UserProfile p= super.authenticate(ctx);

        if(p !=null){
            return p;
        }

        if(Authentication.loginUserFromHeader(ctx.request())){
            return Authentication.getUserProfile();
        }
        return null;
    }
}
