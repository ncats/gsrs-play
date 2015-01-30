package ix.core.controllers.version;

import play.mvc.Controller;
import play.mvc.Result;

public class Api extends Controller {
    public static Result index () {
        return ok (ix.core.views.html.apiversions.render());
    }
}
