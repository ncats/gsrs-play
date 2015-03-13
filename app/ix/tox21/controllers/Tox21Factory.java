package ix.tox21.controllers;

import java.util.*;

import play.*;
import play.db.ebean.*;
import play.data.*;
import play.mvc.*;
import com.avaje.ebean.Query;
import com.avaje.ebean.Expr;

import ix.tox21.models.QCSample;
import ix.core.controllers.EntityFactory;

public class Tox21Factory extends EntityFactory {
    static final public Model.Finder<Long, QCSample> finder = 
        new Model.Finder(Long.class, QCSample.class);

    public static QCSample getQCSample (Long id) {
        return getEntity (id, finder);
    }

    public static List<QCSample> getQCSamples
        (int top, int skip, String filter) {
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
        return edits (id, QCSample.class);
    }

    public static Result get (Long id, String expand) {
        return get (id, expand, finder);
    }

    public static Result field (Long id, String path) {
        return field (id, path, finder);
    }

    public static Result create () {
        return create (QCSample.class, finder);
    }

    public static Result delete (Long id) {
        return delete (id, finder);
    }

    public static Result update (Long id, String field) {
        return update (id, field, QCSample.class, finder);
    }
}
