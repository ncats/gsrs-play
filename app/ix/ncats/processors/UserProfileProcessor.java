package ix.ncats.processors;


import ix.core.EntityProcessor;
import ix.core.models.UserProfile;
import ix.ncats.controllers.auth.Authentication;

public class UserProfileProcessor implements EntityProcessor<UserProfile>{
	@Override
	public void postUpdate(UserProfile obj) {
		Authentication.tokens.get().updateUserCache(obj);
	}
	

}
