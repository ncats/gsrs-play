package crosstalk.ncats.controllers;

import java.io.*;
import java.security.*;
import java.util.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import play.*;
import play.db.ebean.*;
import play.data.*;
import play.mvc.*;

import crosstalk.ncats.models.Grant;
import crosstalk.core.controllers.EntityFactory;

public class GrantFactory extends EntityFactory {
    static final public Model.Finder<Long, Grant> finder = 
        new Model.Finder(Long.class, Grant.class);

    public static List<Grant> all () { return all (finder); }
    public static Grant getEntity (Long id) {
        return getEntity (id, finder);
    }
    public static Result count () {
        return count (finder);
    }
    public static Result page (int top, int skip) {
        return GrantFactory.page (top, skip, null, null);
    }
    public static Result page (int top, int skip, 
                               String expand, String filter) {
        return page (top, skip, expand, filter, finder);
    }

    public static Result edits (Long id) {
        return edits (id, Grant.class);
    }

    public static Result get (Long id, String expand) {
        return get (id, expand, finder);
    }

    public static Result field (Long id, String path) {
        return field (id, path, finder);
    }

    public static Result create () {
        return create (Grant.class, finder);
    }

    public static Result delete (Long id) {
        return delete (id, finder);
    }

    public static Result update (Long id, String field) {
        return update (id, field, Grant.class, finder);
    }
}
