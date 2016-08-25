package ix.ncats.controllers.security;

import be.objectify.deadbolt.core.models.Subject;
import be.objectify.deadbolt.java.AbstractDeadboltHandler;
import be.objectify.deadbolt.java.DynamicResourceHandler;
import ix.core.models.UserProfile;
import ix.ncats.controllers.auth.Authentication;
import play.Logger;
import play.libs.F;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;


public class IxDeadboltHandler extends AbstractDeadboltHandler {
    public F.Promise<Result> beforeAuthCheck(final Http.Context context) {
        // returning null means that everything is OK.  Return a real result if you want a redirect to a login page or
        // somewhere else try to set up the target or substance id to context
        return F.Promise.pure(null);
    }

    public Subject getSubject(final Http.Context context) {
        return Authentication.getUserProfile();

    }

    //will be invoked for Dynamic restrictions
    public DynamicResourceHandler getDynamicResourceHandler(final Http.Context context) {
        return new IxDynamicResourceHandler();
    }
    
    /**
     * This is a convenience method to test if a session meets one of the dynamic permission rules.
     * 
     * This method assumed that there is an accessible Http.Context from Play!'s controller class,
     * if that is not the case, the behavior is not defined.
     * 
     * @param permission
     * @return
     */
    public static boolean activeSessionHasPermission(String permission){
    	IxDeadboltHandler inst=new ix.ncats.controllers.security.IxDeadboltHandler();
    	IxDynamicResourceHandler idrh=(IxDynamicResourceHandler)inst.getDynamicResourceHandler(Controller.ctx());
    	return idrh.isAllowed(permission, null,inst, Controller.ctx());
    }
    

    @Override
    public F.Promise<Result> onAuthFailure(final Http.Context context,
                                           final String content) {
        return F.Promise.promise(new F.Function0<Result>() {
            @Override
            public Result apply() throws Throwable {
                String message = Authentication.getUserProfile() != null ? "Not Authorized" : "No User Present";

    	      // return play.mvc.Results.unauthorized(message);
                return ok(ix.core.views.html.response.render(message));
            }
        });
    }
}
