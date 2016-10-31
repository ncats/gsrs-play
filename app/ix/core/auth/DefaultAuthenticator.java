package ix.core.auth;

import ix.core.controllers.AdminFactory;
import ix.core.controllers.UserProfileFactory;
import ix.core.models.UserProfile;

/**
 * Created by katzelda on 5/4/16.
 */
public class DefaultAuthenticator implements Authenticator {
    @Override
    public UserProfile authenticate(AuthenticationCredentials credentials) {
    	
    	//If there's no HTTP context, attempt to authenticate with username/password 
    	//
        if(credentials.getContext()==null){
            //check by username and password
            UserProfile profile = UserProfileFactory
            		.getUserProfileForUsername(credentials.getUsername());

            if (profile != null && profile.active && AdminFactory.validatePassword(profile, new String(credentials.getPassword()))) {
               return profile;
            }
            return null;
        }else {
            return null;
        }
    }
}
