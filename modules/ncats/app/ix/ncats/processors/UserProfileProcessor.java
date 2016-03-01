package ix.ncats.processors;


import ix.core.EntityProcessor;
import ix.core.models.UserProfile;
import ix.ncats.controllers.auth.Authentication;

public class UserProfileProcessor implements EntityProcessor<UserProfile>{

	@Override
	public void prePersist(UserProfile obj) {
		
	}
	
	@Override
	public void postPersist(UserProfile obj) {
		//Authentication.updateUserProfileToken(obj);
	}
	@Override
	public void preRemove(UserProfile obj) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void postRemove(UserProfile obj) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void preUpdate(UserProfile obj) {
		System.out.println("Updating users");
		Authentication.updateUserProfileToken(obj);
	}
	@Override
	public void postUpdate(UserProfile obj) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void postLoad(UserProfile obj) {
		// TODO Auto-generated method stub
		
	}
	

}
