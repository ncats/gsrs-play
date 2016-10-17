package ix.core.auth;

import ix.core.models.UserProfile;
import ix.ncats.controllers.auth.Authentication;
import play.mvc.Http.Context;
import play.mvc.Http.Request;

public class UserTokenAuthenticator implements Authenticator{
	@Override
	public UserProfile authenticate(AuthenticationCredentials credentials) {
		Context ctx = credentials.getContext();
		if(ctx==null)return null;
		Request r = ctx.request();
		String token=r.getHeader("auth-token");
		if(token!=null){
			System.out.println("Found token:" + token);
        	return Authentication.getUserProfileFromTokenAlone(token);
        }
		return null;
	}
}