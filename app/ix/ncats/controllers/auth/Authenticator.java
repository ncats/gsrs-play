package ix.ncats.controllers.auth;

import ix.core.auth.AuthenticationCredentials;
import ix.core.models.Principal;
import ix.core.models.UserProfile;

@Deprecated
public interface Authenticator extends ix.core.auth.Authenticator {
	/**
	 * Returns the Principal authenticated via the given username and password.
	 * 
	 * If authentication fails, return null.
	 * 
	 * @param username
	 * @param password
	 * @return
	 */
	public Principal getUser(String username, String password);
	
	default UserProfile authenticate(AuthenticationCredentials credentials){
		String password= String.copyValueOf(credentials.getPassword());
		String uname=credentials.getUsername();
		Principal pp = this.getUser(uname, password);
		if(pp==null)return null;
		return pp.getUserProfile();
	}
}
