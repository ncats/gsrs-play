package ix.idg.security;

import be.objectify.deadbolt.core.models.Subject;
import be.objectify.deadbolt.java.AbstractDeadboltHandler;
import be.objectify.deadbolt.java.DynamicResourceHandler;
import ix.core.models.UserProfile;
import ix.ncats.controllers.auth.Authentication;
import play.libs.F;
import play.mvc.Http;
import play.mvc.Result;


public class IdgDeadboltHandler extends AbstractDeadboltHandler {
    public F.Promise<Result> beforeAuthCheck(final Http.Context context) {
        // returning null means that everything is OK.  Return a real result if you want a redirect to a login page or
        // somewhere else
        return F.Promise.pure(null);
    }

    public Subject getSubject(final Http.Context context) {
        UserProfile profile = Authentication.getUserProfile();
        return profile;
    }

    //will be invoked for Dynamic restrictions
    public DynamicResourceHandler getDynamicResourceHandler(final Http.Context context) {
        return new IdgDynamicResourceHandler();
    }

    @Override
    public F.Promise<Result> onAuthFailure(final Http.Context context,
                                           final String content) {
        return F.Promise.promise(new F.Function0<Result>() {
            @Override
            public Result apply() throws Throwable {
                String message = Authentication.getUserProfile() != null ? "Not Authorized" : "No User Present";
                return unauthorized(ix.idg.views.html.error.render(401, message ));
            }
        });
    }
}
