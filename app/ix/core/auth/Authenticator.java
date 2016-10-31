package ix.core.auth;

import ix.core.models.UserProfile;
import play.mvc.Http;

/**
 * Created by katzelda on 5/4/16.
 */
public interface Authenticator {
	/**
	 * Authenticate using provided credentials. Returns a UserProfile if authentication
	 * is successful, otherwise returns null.
	 * 
	 * @param credentials
	 * @return
	 */
    UserProfile authenticate(AuthenticationCredentials credentials);
}
