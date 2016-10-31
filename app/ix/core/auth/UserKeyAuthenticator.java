package ix.core.auth;

import ix.core.models.UserProfile;
import ix.ncats.controllers.auth.Authentication;
import play.mvc.Http.Context;
import play.mvc.Http.Request;

public class UserKeyAuthenticator implements Authenticator{

	@Override
	public UserProfile authenticate(AuthenticationCredentials credentials) {
		Context ctx = credentials.getContext();
		if(ctx==null)return null;
		Request r = ctx.request();
		String user=r.getHeader("auth-username");
        String key=r.getHeader("auth-key");
        if(user!=null && key!=null){
        	return Authentication.getUserProfileFromKey(user,key);
        }
		return null;
	}
}