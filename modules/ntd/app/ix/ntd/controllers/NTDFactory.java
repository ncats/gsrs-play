package ix.ntd.controllers;

import ix.core.controllers.EntityFactory;
import play.mvc.Result;

public class NTDFactory extends EntityFactory {
    public static Result index() {return ok(ix.ntd.views.html.index.render());}


}
