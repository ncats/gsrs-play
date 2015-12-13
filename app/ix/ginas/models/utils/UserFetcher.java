package ix.ginas.models.utils;

import java.lang.reflect.Method;

import ix.core.controllers.PrincipalFactory;
import ix.core.models.Principal;

public class UserFetcher {
	private static final String DEFAULT_USERNAME = "AUTO_IMPORTER";
	private static final String USER_FETCH_METHOD = "getUser";
	private static final String USER_FETCH_CLASS = "ix.ncats.controllers.auth.Authentication";
	private static Method userMethod = null;
	public static Principal getActingUser(){
		try {
			setupMethod();
			Principal p = ((Principal) userMethod.invoke(null));
		    if(p!=null)
		    	return p;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return PrincipalFactory.registerIfAbsent(new Principal(DEFAULT_USERNAME,null));
	}
	private static void setupMethod(){
		if(userMethod!=null)return;
		Class<?> act;
		try {
			act = Class.forName(USER_FETCH_CLASS);
			userMethod = act.getMethod(USER_FETCH_METHOD);
		}catch(Exception e){
			
		}
	}
}
