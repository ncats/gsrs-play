package ix.ginas.utils;

import ix.core.models.UserProfile;
import ix.ginas.controllers.GinasApp;
import ix.ginas.controllers.v1.ControlledVocabularyFactory;
import ix.ginas.models.v1.CodeSystemVocabularyTerm;
import ix.ginas.models.v1.ControlledVocabulary;
import ix.ginas.models.v1.VocabularyTerm;
import ix.ncats.controllers.auth.Authentication;
import ix.utils.Global;
import play.Application;
import play.Play;
import play.libs.F.Function0;
import play.libs.F.Promise;
import play.mvc.Action;
import play.mvc.Http;
import play.mvc.Result;

public class GinasGlobal extends Global {
	public class LoginWrapper extends Action.Simple {
	    public LoginWrapper(Action<?> action) {
	    	this.delegate = action;
	    }
	
	    @Override
	    public Promise<Result> call(Http.Context ctx) throws java.lang.Throwable {
	    	if(Authentication.getUser()==null){
	    		if(!Authentication.loginUserFromHeader(null)){
	    			if(!Authentication.allowNonAuthenticated()){
	    				UserProfile u=Authentication.getAdministratorContact();
	    				if(u!=null){
	    					return wrapResult(GinasApp.error(401, "You are not authorized to see this resource. Please contact " +
	    						u.user.email
	    						+ " to be granted access."));
	    				}else{
	    					return wrapResult(GinasApp.error(401, "You are not authorized to see this resource. Please contact an administrator to be granted access."));
	    				}
	    			}
	    		}
	    	}
	    	Promise<Result> result = this.delegate.call(ctx);
	    	return result;
	    }
    }
	
	@Override
    public Action<?> onRequest(Http.Request request,java.lang.reflect.Method actionMethod) {
        return new LoginWrapper(super.onRequest(request, actionMethod));
    }
	public static Promise<Result> wrapResult(final Result r) {
		  return Promise.promise(
		    new Function0<Result>() {
		      public Result apply() {
		        return r;
		      }
		    }
		  );
	}

	public void onStart(Application app) {
		super.onStart(app);
		if (!ControlledVocabularyFactory.isloaded()) {
		//	ControlledVocabularyFactory.loadSeedCV(Play.application().resourceAsStream("CV.txt"));
			//ControlledVocabularyFactory.loadCVJson(Play.application().resourceAsStream("cv.json"));
			ControlledVocabularyFactory.loadCVJson(Play.application().resourceAsStream("cv2.json"));
			String codeSystem = Play.application().configuration().getString("ix.ginas.generatedcode.codesystem", null);
			if(codeSystem!= null){
				ControlledVocabulary cvv = ControlledVocabularyFactory.getControlledVocabulary("CODE_SYSTEM");
				boolean addNew=true;
				for(VocabularyTerm vt1 : cvv.terms){
					if(vt1.value.equals(codeSystem)){
						addNew=false;
						break;
					}
				}
				if(addNew){
					CodeSystemVocabularyTerm vt = new CodeSystemVocabularyTerm();
					vt.display=codeSystem;
					vt.value=codeSystem;
					vt.hidden=true;
					vt.save();
					cvv.addTerms(vt);
					cvv.save();
				}
			}
			if(!Play.isTest()){
				System.out.println("Loaded CV:" + ControlledVocabularyFactory.size());
			}
		}else{
			//System.out.println("CV already loaded:" + ControlledVocabularyFactory.size());
		}
		
		
    }
	
}
