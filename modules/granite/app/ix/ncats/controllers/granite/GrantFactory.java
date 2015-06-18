package ix.ncats.controllers.granite;

import java.io.*;
import java.util.*;

import play.*;
import play.db.ebean.*;
import play.data.*;
import play.mvc.*;

import com.fasterxml.jackson.databind.JsonNode;

import ix.core.NamedResource;
import ix.ncats.models.Grant;
import ix.core.controllers.EntityFactory;

@NamedResource(name="grants", type=Grant.class)
public class GrantFactory extends EntityFactory {
    static final public Model.Finder<Long, Grant> finder = 
        new Model.Finder(Long.class, Grant.class);

    //static final public Class getType () { return Grant.class; }

    public static Integer getCount () { 
        try {
            return getCount (finder); 
        }
        catch (Exception ex) {
            Logger.trace("Can't get row count", ex);
        }
        return null;
    }
    public static List<Grant> all () { return all (finder); }
    public static List<Grant> filter (int top, int skip) {
        return filter (new FetchOptions (top, skip, null), finder);
    }
    public static List<Grant> filter (int top, int skip, String filter) {
        return filter (new FetchOptions (top, skip, filter), finder);
    }
    public static Grant getEntity (Long id) {
        return getEntity (id, finder);
    }

    public static Result count () {
        return count (finder);
    }

    public static Result page (int top, int skip) {
        return GrantFactory.page (top, skip, null);
    }

    public static Result page (int top, int skip, String filter) {
        return page (top, skip, filter, finder);
    }

    public static List<Grant> filter (JsonNode json, int top, int skip) {
        return filter (json, top, skip, finder);
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
