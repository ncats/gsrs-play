package ix.ginas.controllers.v1;

import java.util.*;
import java.io.*;

import play.libs.Json;
import play.*;
import play.db.ebean.*;
import play.data.*;
import play.mvc.*;
import com.avaje.ebean.*;

import ix.core.controllers.EntityFactory;
import ix.ginas.models.*;
import ix.ginas.models.v1.*;
import ix.core.NamedResource;

@NamedResource(name="references",
               type=Reference.class,
               description="Resource for handling of GInAS references")
public class ReferenceFactory extends EntityFactory {
    static public final Model.Finder<UUID, Reference> finder =
        new Model.Finder(UUID.class, Reference.class);

    public static Reference getReference (UUID uuid) {
        return getEntity (uuid, finder);
    }

    public static Reference getReference (String uuid) {
    	 UUID ref = UUID.fromString(uuid);
        return getEntity (ref, finder);
    }
    
    public static List<Reference> getReferences
        (int top, int skip, String filter) {
        return filter (new FetchOptions (top, skip, filter), finder);
    }

    public static Result count () {
        return count (finder);
    }
    
    public static Integer getCount () {
        try {
            return getCount (finder);
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public static Result page (int top, int skip) {
        return page (top, skip, null);
    }

    public static Result page (int top, int skip, String filter) {
        return page (top, skip, filter, finder);
    }

    public static Result edits (UUID uuid) {
        return edits (uuid, Reference.class);
    }

    public static Result get (UUID uuid, String expand) {
        return get (uuid, expand, finder);
    }

    public static Result create () {
        return create (Reference.class, finder);
    }

    public static Result delete (UUID uuid) {
        return delete (uuid, finder);
    }

    public static Result update (UUID uuid, String field) {
        return update (uuid, field, Reference.class, finder);
    }
}
