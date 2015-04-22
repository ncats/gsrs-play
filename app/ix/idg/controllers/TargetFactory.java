package ix.idg.controllers;

import java.util.*;

import play.*;
import play.db.ebean.*;
import play.data.*;
import play.mvc.*;
import com.avaje.ebean.Query;
import com.avaje.ebean.Expr;

import ix.core.NamedResource;
import ix.idg.models.Target;
import ix.core.controllers.EntityFactory;

@NamedResource(name="targets",type=Target.class)
public class TargetFactory extends EntityFactory implements Commons {
    static final public Model.Finder<Long, Target> finder = 
        new Model.Finder(Long.class, Target.class);

    public static Target getTarget (Long id) {
        return getEntity (id, finder);
    }

    public static List<Target> getTargets (int top, int skip, String filter) {
        return filter (new FetchOptions (top, skip, filter), finder);
    }

    public static Result count () {
        return count (finder);
    }
    public static Result page (int top, int skip) {
        return page (top, skip, null);
    }
    public static Result page (int top, int skip, String filter) {
        return page (top, skip, filter, finder);
    }

    public static Result edits (Long id) {
        return edits (id, Target.class);
    }

    public static Result get (Long id, String expand) {
        return get (id, expand, finder);
    }

    public static Result field (Long id, String path) {
        return field (id, path, finder);
    }

    public static Result create () {
        return create (Target.class, finder);
    }

    public static Result delete (Long id) {
        return delete (id, finder);
    }

    public static Result update (Long id, String field) {
        return update (id, field, Target.class, finder);
    }

    public static Target registerIfAbsent (String accession) {
        List<Target> targets =
            finder.where(Expr.and(Expr.eq("synonyms.label",
                                          UNIPROT_ACCESSION),
                                  Expr.eq("synonyms.term", accession)))
                .findList();
        
        if (!targets.isEmpty()) {
            if (targets.size() > 1) {
                Logger.warn("Accession "+accession+" maps to "+targets.size()
                            +" targets!");
            }
            return targets.iterator().next();
        }
        
        UniprotRegistry uni = new UniprotRegistry ();
        try {
            uni.register(accession);
            return uni.getTarget();
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }
}
