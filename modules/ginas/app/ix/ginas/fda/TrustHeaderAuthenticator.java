package ix.ginas.fda;

import ix.core.auth.AuthenticationCredentials;
import ix.core.auth.Authenticator;
import ix.core.auth.DefaultAuthenticator;
import ix.core.models.UserProfile;
import ix.core.util.CachedSupplier;
import ix.ncats.controllers.auth.Authentication;
import play.Logger;
import play.Play;
import play.mvc.Http;

import java.util.Enumeration;
import java.util.Map;
import java.lang.reflect.*;

/**
 * Created by katzelda on 5/4/16.
 */
public class TrustHeaderAuthenticator implements Authenticator {
	
	CachedSupplier<Boolean> trustheader = CachedSupplier.of(() -> {
		return Play.application()
				.configuration()
				.getBoolean("ix.authentication.trustheader",false);
	});
	
	
	CachedSupplier<String> usernameheader = CachedSupplier.of(() -> {
		return Play.application()
				.configuration()
				.getString("ix.authentication.usernameheader");
	});
	CachedSupplier<String> useremailheader = CachedSupplier.of(() -> {
		return Play.application()
				.configuration()
				.getString("ix.authentication.useremailheader");
	});

	@Override
	public UserProfile authenticate(AuthenticationCredentials credentials) {
		if(!trustheader.get())return null;
		if(usernameheader.get()==null)return null;
		
		Http.Context context = credentials.getContext();
		if(context == null)return null;
		return loginUserFromHeader(context.request());	
	}

	private UserProfile loginUserFromHeader(Http.Request r) {
		try {
			UserInfo ui=getUserInfoFromHeaders(r);
			if (ui.username != null) {
				return Authentication.setUserProfileSessionUsing(ui.username, ui.email);
			}
		} catch (Exception e) {
			Logger.warn("Error authenticating", e);
		}
		return null;
	}
	
	private class UserInfo{
		String username;
		String email;
		
		UserInfo(String username, String email){
			this.username=username;
			this.email=email;
		}
	}
	
	
	private UserInfo getUserInfoFromHeaders(Http.Request r){
		System.out.println("in TrustHeaderAuthenticator.getUserInfoFromHeaders");
		try {
			Class thisClass = Http.Request.class;
			Method[] methods = thisClass.getDeclaredMethods();

			for (int i = 0; i < methods.length; i++) {
				System.out.println(methods[i].toString());
			}
		} catch (Throwable e) {
			System.err.println(e);
		}
/*		Map<String, String> headers = r.getHeaders().asMap();
		for(String key : headers.keySet()) {
			System.out.println("header key: " + key + "; value: " + headers.get(key));
		}*/
		String username = r.getHeader(usernameheader.get());
		String useremail = r.getHeader(useremailheader.get());
		return new UserInfo(username, useremail);
	}
}
