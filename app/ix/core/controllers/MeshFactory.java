package ix.core.controllers;

import java.io.*;
import java.security.*;
import java.util.*;

import play.*;
import play.db.ebean.*;
import play.data.*;
import play.mvc.*;

import ix.core.models.Mesh;

public class MeshFactory extends EntityFactory {
    public static final Model.Finder<Long, Mesh> finder = 
        new Model.Finder(Long.class, Mesh.class);

    public static List<Mesh> all () { return all (finder); }
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
        return edits (id, Mesh.class);
    }

    public static Result create () {
        return create (Mesh.class, finder);
    }

    public static Result delete (Long id) {
        return delete (id, finder);
    }

    public static Result update (Long id, String field) {
        return update (id, field, Mesh.class, finder);
    }
}
