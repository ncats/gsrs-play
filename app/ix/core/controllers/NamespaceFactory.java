package ix.core.controllers;

import java.util.List;

import play.*;
import play.db.ebean.*;
import play.data.*;
import play.mvc.*;

import ix.core.NamedResource;
import ix.core.models.Namespace;

@NamedResource(name="namespaces",
               type=Namespace.class,
               description="This resource provides meta information about a namespace or data source")
public class NamespaceFactory extends EntityFactory {
    public static final Model.Finder<Long, Namespace> finder = 
        new Model.Finder(Long.class, Namespace.class);

    public static List<Namespace> all () { return all (finder); }
    public static Result count () { return count (finder); }
    
    public static Integer getCount () {
        try {
            return getCount (finder);
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }
    
    public static Result page (int top, int skip, String filter) {
        return page (top, skip, filter, finder);
    }

    public static Namespace get (String name) {
        try {
            Namespace ns = finder.where()
                .eq("name", name)
                .findUnique();
            return ns;
        }
        catch (Exception ex) {
            Logger.trace("Can't query namespace", ex);
        }
        return null;
    }

    public static Namespace registerIfAbsent (String name, String location) {
        Namespace ns = get (name);
        if (ns == null) {
            ns = Namespace.newPublic(name);
            ns.location = location;
            ns.save();
        }
        return ns;
    }
    
    public static Result get (Long id, String select) {
        return get (id, select, finder);
    }

    public static Result field (Long id, String path) {
        return field (id, path, finder);
    }

    public static Result edits (Long id) {
        return edits (id, Namespace.class);
    }

    public static Result create () {
        return create (Namespace.class, finder);
    }

    public static Result delete (Long id) {
        return delete (id, finder);
    }

    public static Result update (Long id, String field) {
        return update (id, field, Namespace.class, finder);
    }
}
