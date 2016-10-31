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
		try{
			if(token!=null){
				UserProfile up = Authentication.getUserProfileFromTokenAlone(token);
	        	return up;
	        }
		}catch(Exception e){
			e.printStackTrace();
		}
		return null;
	}
}