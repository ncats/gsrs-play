package ix.ginas.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.function.Consumer;
import java.util.function.Function;

import ix.core.Experimental;
import ix.core.auth.UserKeyAuthenticator;
import ix.core.auth.UserPasswordAuthenticator;
import ix.core.auth.UserTokenAuthenticator;
import ix.core.factories.AuthenticatorFactory;
import ix.core.models.Payload;
import ix.core.models.UserProfile;
import ix.core.plugins.GinasRecordProcessorPlugin;
import ix.core.plugins.GinasRecordProcessorPlugin.PayloadProcessor;
import ix.core.plugins.PayloadPlugin;
import ix.core.plugins.PayloadPlugin.PayloadPersistType;
import ix.core.stats.Statistics;
import ix.ginas.controllers.GinasApp;
import ix.ginas.controllers.v1.ControlledVocabularyFactory;
import ix.ginas.fda.TrustHeaderAuthenticator;
import ix.ncats.controllers.auth.Authentication;
import ix.ncats.controllers.security.IxDeadboltHandler;
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
	Application app;
	
	
	private boolean showHeaders;
	private boolean loadCV;
	
	static final Logger.ALogger AccessLogger = Logger.of("access");

	private static List<Runnable> startRunners = new ArrayList<Runnable>();
	
	private static boolean isRunning=false;

	
	private void logHeaders(Http.Request req){
		Logger.debug("HEADERS ON REQUEST ===================");
		String allheaders="";
		for(String head:req.headers().keySet()){
			allheaders+=head + "\t" + req.getHeader(head) + "\n";

		}
		Logger.debug(allheaders);
	}
	
	
	public class LoginWrapper extends Action.Simple {
		public LoginWrapper(Action<?> action) {
			this.delegate = action;
		}

		@Override
		public Promise<Result> call(Http.Context ctx) throws java.lang.Throwable {
			requestListener.accept(ctx.request());
			
			Http.Request req = ctx.request();
			if(showHeaders){
				logHeaders(req);
			}

			String real = req.getHeader("X-Real-IP"); //Where does this come from?
													  //IS this due to a proxy?

			UserProfile p = Authentication.getUserProfile();
			
			String uri=req.uri();
			char[] cs = uri.toCharArray();

			for(int i=0;i<cs.length;i++){
				char c=cs[i];
				if(Character.isISOControl(c)){
					return wrapResult(GinasApp.error(500, "Illegal character at position [" +
							i
							+ "]"));
				}
			}


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

	private void loadCV(){
		if (loadCV && !ControlledVocabularyFactory.isloaded()) {
			ControlledVocabularyFactory.loadCVJson(Play.application().resourceAsStream("cv.json"));
			Logger.info("Loaded CV:" + ControlledVocabularyFactory.size());
		}
	}

	private void loadStartFile(){

		String fname = app.configuration().getString("ix.ginas.load.file",null);

		if(fname!=null){
			try{
				System.out.println(fname);
				System.out.println("==================");

				Date start = new Date();
				File f= new File(fname);
				Payload payload=app
						.plugin(PayloadPlugin.class)
						.getPayloadForFile(f, PayloadPersistType.PERM);
				if(payload.created.before(start)){
					System.out.println("Already loaded file:" + f.getAbsolutePath());
				}else{
					PayloadProcessor pp =app
							.plugin(GinasRecordProcessorPlugin.class)
							.submit(payload,
									ix.ginas.utils.GinasUtils.GinasDumpExtractor.class,
									ix.ginas.utils.GinasUtils.GinasSubstanceForceAuditPersister.class);

					Statistics s=GinasRecordProcessorPlugin.getStatisticsForJob(pp.key);
					System.out.println("Loading:" + s.getEstimatedTimeLeft() + " loaded so far:" + s.recordsPersistedSuccess + "\t" + s._isDone());

					while(!s._isDone()){
						Thread.sleep(1000);
						System.out.println("Records loaded:" + s.recordsProcessedSuccess.get() + "\t Seconds left:" + s.getEstimatedTimeLeft()/1000);
						s=GinasRecordProcessorPlugin.getStatisticsForJob(pp.key);
					}
				}
			}catch(Exception e){
				e.printStackTrace();
			}
		}
	}
	
	public void setupAuthenticators(){
		AuthenticatorFactory authFac = AuthenticatorFactory.getInstance(app);
		authFac.registerAuthenticator(new TrustHeaderAuthenticator());
		authFac.registerAuthenticator(new UserPasswordAuthenticator());
		authFac.registerAuthenticator(new UserTokenAuthenticator());
		authFac.registerAuthenticator(new UserKeyAuthenticator());
	}

	public void onStart(Application app) {
		super.onStart(app);
		this.app=app;
		
		showHeaders=app.configuration()
					   .getBoolean("ix.ginas.debug.showheaders", false);
		setupAuthenticators();
		
		loadCV=app.configuration()
				   .getBoolean("ix.ginas.init.loadCV", true);
		loadCV();
		
		IxDeadboltHandler.setErrorResultProvider(GinasApp::error);
		
		for(Runnable r:startRunners){
			r.run();
		}
		startRunners.clear();
		isRunning=true;
		
		loadStartFile();
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
	
	private static Consumer<Http.Request> requestListener =  (r)->{};
	
	
	public static void setRequestListener(Consumer<Http.Request> listen){
		requestListener=listen;
	}
	
}
