package ix.ginas.controllers;

import ix.core.controllers.EntityFactory;
import ix.core.models.Principal;
import play.Logger;
import play.db.ebean.Model;
import play.mvc.Result;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.Transaction;

public class GinasFactory extends EntityFactory {
    public static final Model.Finder<Long, Principal> finder = 
        new Model.Finder(Long.class, Principal.class);

    public static Result index () {
        return ok (ix.ginas.views.html.index.render());
    }

    public static Result register () {
        return ok (ix.ginas.views.html.register.render());
    }

    public static Result chemical () {
        return ok (ix.ginas.views.html.chemical.render());
    }

    public static Result sequence (String kind) {
        return ok (ix.ginas.views.html.sequence.render(kind));
    }

    public static Result report () {
        return ok (ix.ginas.views.html.report.render());
    }

    public static Result wizard (String kind) {
        Logger.info(kind);
        return ok (ix.ginas.views.html.wizard.render(kind));
    }

    public static Principal byUsername (String user) {
        return finder.where().eq("username", user).findUnique();
    } 

    public static Principal registerIfAbsent (String user) {
        Principal p = byUsername(user);
        if (p == null) {
            p = new Principal();
            p.username = user;
            p.save();
        }
        return p;
    }

}
