package ix.core.controllers;

import java.util.List;

import play.*;
import play.db.ebean.*;
import play.data.*;
import play.mvc.*;

import ix.core.NamedResource;
import ix.core.models.Resource;

@NamedResource(name="resources",type=Resource.class)
public class ResourceFactory extends EntityFactory {
    public static final Model.Finder<Long, Resource> finder = 
        new Model.Finder(Long.class, Resource.class);

    public static List<Resource> all () { return all (finder); }
    public static Result count () { return count (finder); }
    public static Result page (int top, int skip, String expand,
                               String filter) {
        return page (top, skip, expand, filter, finder);
    }

    public static Result get (Long id, String select) {
        return get (id, select, finder);
    }

    public static Result field (Long id, String path) {
        return field (id, path, finder);
    }

    public static Result edits (Long id) {
        return edits (id, Resource.class);
    }

    public static Result create () {
        return create (Resource.class, finder);
    }

    public static Result delete (Long id) {
        return delete (id, finder);
    }

    public static Result update (Long id, String field) {
        return update (id, field, Resource.class, finder);
    }
}
