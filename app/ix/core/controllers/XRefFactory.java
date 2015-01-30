package ix.core.controllers;

import java.util.List;

import play.*;
import play.db.ebean.*;
import play.data.*;
import play.mvc.*;

import ix.core.NamedResource;
import ix.core.models.XRef;

@NamedResource(name="xrefs",
               type=XRef.class,
               description="Cross references are handled by this resource. An XRef is also be used as an indirect way (i.e., pointer) to reference a particular entity.")
public class XRefFactory extends EntityFactory {
    public static final Model.Finder<Long, XRef> finder = 
        new Model.Finder(Long.class, XRef.class);

    public static Result count () { return count (finder); }
    public static Result page (int top, int skip, String filter) {
        return page (top, skip, filter, finder);
    }
    public static Result get (Long id, String select) {
        return get (id, select, finder);
    }

    public static Result field (Long id, String path) {
        return field (id, path, finder);
    }

    public static Result edits (Long id) {
        return edits (id, XRef.class);
    }

    public static Result create () {
        return create (XRef.class, finder);
    }

    public static Result delete (Long id) {
        return delete (id, finder);
    }

    public static Result update (Long id, String field) {
        return update (id, field, XRef.class, finder);
    }
}
