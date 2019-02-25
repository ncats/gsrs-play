package ix.core.controllers.cors;

import ix.core.controllers.v1.RouteFactory;
import play.mvc.Controller;
import play.mvc.Result;

public class ApiHTTPHelper extends Controller {
    public static Result checkPreFlight (String p) {
        return RouteFactory.checkPreFlight(p);
    }
}
