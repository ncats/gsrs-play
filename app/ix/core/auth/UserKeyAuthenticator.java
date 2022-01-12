package ix.core.auth;

import ix.core.models.UserProfile;
import ix.ncats.controllers.auth.Authentication;
import play.mvc.Http.Context;
import play.mvc.Http.Request;

import java.lang.reflect.Method;

public class UserKeyAuthenticator implements Authenticator{

	@Override
	public UserProfile authenticate(AuthenticationCredentials credentials) {
		Context ctx = credentials.getContext();
		if(ctx==null)return null;
		Request r = ctx.request();
		System.out.println("play request object methods:");
		try {
			Class thisClass = play.mvc.Http.Request.class;
			Method[] methods = thisClass.getDeclaredMethods();

			for (int i = 0; i < methods.length; i++) {
				System.out.println(methods[i].toString());
			}
		} catch (Throwable e) {
			System.err.println(e);
		}

		String user=r.getHeader("auth-username");
        String key=r.getHeader("auth-key");
        if(user!=null && key!=null){
        	return Authentication.getUserProfileFromKey(user,key);
        }
		return null;
	}
}