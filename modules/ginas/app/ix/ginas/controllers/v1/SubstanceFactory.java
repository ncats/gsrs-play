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

@NamedResource(name="substances",
               type=Substance.class,
               description="Resource for handling of GInAS substances")
public class SubstanceFactory extends EntityFactory {
    static public final Model.Finder<UUID, Substance> finder =
        new Model.Finder(UUID.class, Substance.class);
    static public final Model.Finder<Long, Substance> subfinder =
            new Model.Finder(Long.class, Substance.class);
    static public final Model.Finder<Long, ChemicalSubstance> chemfinder =
            new Model.Finder(Long.class, ChemicalSubstance.class);
    static public final Model.Finder<Long, ProteinSubstance> protfinder =
            new Model.Finder(Long.class, ProteinSubstance.class);
    
    public static Substance getSubstance (String id) {
        return getSubstance (UUID.fromString(id));
    }

    public static Substance getSubstance (UUID uuid) {
        return getEntity (uuid, finder);
    }
    
    public static Substance getSubstanceByApprovalID (String approvalID) {
        return finder.where().eq("approvalID", approvalID).findUnique();
    }

    public static List<Substance> getSubstances
        (int top, int skip, String filter) {
        return filter (new FetchOptions (top, skip, filter), finder);
    }
    
    public static List<ChemicalSubstance> getChemicals
    (int top, int skip, String filter) {
    return filter (new FetchOptions (top, skip, filter), chemfinder);
}
    public static List<ProteinSubstance> getProteins
    (int top, int skip, String filter) {
    return filter (new FetchOptions (top, skip, filter), protfinder);
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
    
    public static Result count () {
        return count (finder);
    }

    public static Result page (int top, int skip) {
        return page (top, skip, null);
    }

    public static Result page (int top, int skip, String filter) {
        return page (top, skip, filter, finder);
    }

    public static Result edits (UUID uuid) {
        return edits (uuid, Substance.class);
    }

    public static Result get (UUID uuid, String expand) {
        return get (uuid, expand, finder);
    }

    public static Result field (UUID uuid, String path) {
        return field (uuid, path, finder);
    }

    public static Result create () {
        return create (Substance.class, finder);
    }

    public static Result delete (UUID uuid) {
        return delete (uuid, finder);
    }

    public static Result update (UUID uuid, String field) {
        return update (uuid, field, Substance.class, finder);
    }
}
