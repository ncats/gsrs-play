package ix.ginas.utils;

import java.util.ArrayList;
import java.util.List;

import ix.core.UserFetcher;
import ix.core.auth.AuthenticationCredentials;
import ix.core.auth.Authenticator;
import ix.core.models.Principal;
import ix.core.models.UserProfile;
import ix.ginas.controllers.GinasApp;
import ix.ginas.controllers.v1.ControlledVocabularyFactory;
import ix.ginas.fda.FdaAuthenticator;
import ix.ncats.controllers.auth.Authentication;
import ix.utils.Global;
import play.Application;
import play.Logger;
import play.Play;
import play.libs.F.Function0;
import play.libs.F.Promise;
import play.mvc.Action;
import play.mvc.Http;
import play.mvc.Result;

public class GinasGlobal extends Global {

	static final Logger.ALogger AccessLogger = Logger.of("access");

	private static List<Runnable> startRunners = new ArrayList<Runnable>();
	private static boolean isRunning=false;

    private Authenticator authenticator = new FdaAuthenticator();
	public class LoginWrapper extends Action.Simple {
	    public LoginWrapper(Action<?> action) {
	    	this.delegate = action;
	    }
	
	    @Override
	    public Promise<Result> call(Http.Context ctx) throws java.lang.Throwable {

			Http.Request req = ctx.request();
			if(Play.application().configuration()
					.getBoolean("ix.ginas.debug.showheaders", false)){
				Logger.debug("HEADERS ON REQUEST ===================");
				String allheaders="";
				for(String head:req.headers().keySet()){
					allheaders+=head + "\t" + req.getHeader(head) + "\n";

				}
				Logger.debug(allheaders);
			}

			String real = req.getHeader("X-Real-IP");

            UserProfile p = authenticator.authenticate(AuthenticationCredentials.create(ctx));


			if(p ==null && !Authentication.allowNonAuthenticated()){

				UserProfile u=Authentication.getAdministratorContact();
				AccessLogger.info("NOT_AUTHENTICATED {} {} {} \"{}\"", req.remoteAddress(),
						real != null ? real : "", req.method(), req.uri());
				if(u!=null){
					return wrapResult(GinasApp.error(401, "You are not authorized to see this resource. Please contact " +
						u.user.email
						+ " to be granted access."));
				}else{
					return wrapResult(GinasApp.error(401, "You are not authorized to see this resource. Please contact an administrator to be granted access."));
				}

	    	}

			String username = p ==null ? "GUEST" : p.user.username;

			AccessLogger.info("{} {} {} {} \"{}\"", username, req.remoteAddress(),
					real != null ? real : "", req.method(), req.uri());
	    	return this.delegate.call(ctx);
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
			ControlledVocabularyFactory.loadCVJson(Play.application().resourceAsStream("cv.json"));
			//ControlledVocabularyFactory.loadCVJson(Play.application().resourceAsStream("cv2.json"));
//			String codeSystem = Play.application().configuration().getString("ix.ginas.generatedcode.codesystem", null);
//			if(codeSystem!= null){
//				ControlledVocabulary cvv = ControlledVocabularyFactory.getControlledVocabulary("CODE_SYSTEM");
//				boolean addNew=true;
//				for(VocabularyTerm vt1 : cvv.terms){
//					if(vt1.value.equals(codeSystem)){
//						addNew=false;
//						break;
//					}
//				}
//				if(addNew){
//					CodeSystemVocabularyTerm vt = new CodeSystemVocabularyTerm();
//					vt.display=codeSystem;
//					vt.value=codeSystem;
//					vt.hidden=true;
//					vt.save();
//					cvv.addTerms(vt);
//					cvv.save();
//				}
//			}
			if(!Play.isTest()){
				System.out.println("Loaded CV:" + ControlledVocabularyFactory.size());
			}
		}else{
			//System.out.println("CV already loaded:" + ControlledVocabularyFactory.size());
		}
		for(Runnable r:startRunners){
			r.run();
		}
		startRunners.clear();
		isRunning=true;
		
    }
	@Override
	public void onStop(Application app){
		startRunners.clear();
		isRunning=false;
	}
	public static void runAfterStart(Runnable r){
		if(isRunning){
			r.run();
		}else{
			startRunners.add(r);
		}
		
	}
}
