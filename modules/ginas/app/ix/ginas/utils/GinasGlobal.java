package ix.ginas.utils;

import ix.ginas.controllers.GinasApp;
import ix.ncats.controllers.auth.Authentication;
import ix.utils.Global;
import play.api.Play;
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
	    				return wrapResult(GinasApp.error(401, "You are not authorized to see this resource"));
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

}