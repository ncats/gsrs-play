package ix.ncats.controllers;

import play.*;
import play.mvc.*;

public class Secured extends Security.Authenticator {
    @Override
    public String getUsername (Http.Context ctx) {
        return ctx.session().get("username");
    }
    
    @Override
    public Result onUnauthorized (Http.Context ctx) {
        return redirect (routes.Authentication.login());
    }
}
