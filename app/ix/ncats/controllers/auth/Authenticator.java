package ix.ncats.controllers.auth;

import ix.core.models.Principal;

public interface Authenticator {
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
}
