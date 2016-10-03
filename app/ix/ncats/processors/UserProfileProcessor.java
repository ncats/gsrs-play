package ix.ncats.processors;


import ix.core.EntityProcessor;
import ix.core.models.UserProfile;
import ix.ncats.controllers.auth.Authentication;

public class UserProfileProcessor implements EntityProcessor<UserProfile>{
	@Override
	public void preUpdate(UserProfile obj) {
		System.out.println("Updating users");
		Authentication.updateUserProfileToken(obj);
	}
	

}
