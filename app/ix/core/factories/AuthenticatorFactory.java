package ix.core.factories;

import ix.core.auth.Authenticator;
import ix.core.auth.DefaultAuthenticator;
import ix.core.models.UserProfile;
import ix.core.util.EntityUtils.EntityInfo;
import play.Application;
import play.Logger;

public class AuthenticatorFactory  extends AccumlatingInternalMapEntityResourceFactory<Authenticator>{

	public static final Class<?> RESOURCE_CLASS = Authenticator.class;
	
	private static AuthenticatorFactory _instance;
	
	public AuthenticatorFactory(Application app) {
		super(app);
		_instance=this;
	}

	@Override
	public Authenticator accumulate(Authenticator t1, Authenticator t2) {
		return (ac)->{
			UserProfile up = t1.authenticate(ac);
			if(up==null){
				return t2.authenticate(ac);
			}
			return up;
		};
	}

	@Override
	public void initialize(Application app) {
		this.getStandardResourceStream(app, "ix.core.authenticators")
			.forEach(m->{
				String authClass=(String) m.get("authenticator");
    			String debug="Setting up authenticator [" + authClass + "] ... ";
    			try {
					Class entityCls = Class.forName(authClass);
					Authenticator auth=(Authenticator) entityCls.newInstance();
					this.register(RESOURCE_CLASS, auth, false);
				} catch (Exception e) {
					Logger.info(debug + "failed");
					e.printStackTrace();
				}
			});
	}
	
	@Override
	public Authenticator getDefaultResourceFor(EntityInfo<?> emeta){
		return new DefaultAuthenticator();
	}
	
	
	public Authenticator getAuthenticator(){
		return getSingleResourceFor(RESOURCE_CLASS);
	}
	
	public void registerAuthenticator(Authenticator auth){
		this.register(RESOURCE_CLASS, auth, false);
	}
	
	public static AuthenticatorFactory getInstance(Application app){
		if(_instance==null){
			return new AuthenticatorFactory(app);
		}
		return _instance;
	}

}
