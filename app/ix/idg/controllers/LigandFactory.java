package ix.idg.controllers;

import java.util.*;

import play.*;
import play.db.ebean.*;
import play.data.*;
import play.mvc.*;

import ix.core.NamedResource;
import ix.idg.models.Ligand;
import ix.core.controllers.EntityFactory;

@NamedResource(name="ligands",type=Ligand.class)
public class LigandFactory extends EntityFactory {
    static final public Model.Finder<Long, Ligand> finder = 
        new Model.Finder(Long.class, Ligand.class);

    public static Ligand getLigand (Long id) {
        return getEntity (id, finder);
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
        return edits (id, Ligand.class);
    }

    public static Result get (Long id, String expand) {
        return get (id, expand, finder);
    }

    public static Result field (Long id, String path) {
        return field (id, path, finder);
    }

    public static Result create () {
        return create (Ligand.class, finder);
    }

    public static Result delete (Long id) {
        return delete (id, finder);
    }

    public static Result update (Long id, String field) {
        return update (id, field, Ligand.class, finder);
    }
}
