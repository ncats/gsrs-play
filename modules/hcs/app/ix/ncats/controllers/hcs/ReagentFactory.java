package ix.ncats.controllers.hcs;

import java.io.*;
import java.util.*;

import play.*;
import play.db.ebean.*;
import play.data.*;
import play.mvc.*;

import com.avaje.ebean.Query;
import com.avaje.ebean.Expr;
import com.avaje.ebean.Ebean;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import ix.core.NamedResource;
import ix.ncats.models.hcs.*;
import ix.core.controllers.EntityFactory;

@NamedResource(name="reagents",
               type=Reagent.class,
               description="Resource for handling of HCS reagent")
public class ReagentFactory extends EntityFactory {
    public static final Model.Finder<Long, Reagent> finder = 
        new Model.Finder(Long.class, Reagent.class);
    
    public static List<Reagent> all () { return all (finder); }
    public static Result count () { return count (finder); }
    public static Result page (int top, int skip, String filter) {
        return page (top, skip, filter, finder);
    }

    public static List<Reagent> filter (int top, int skip) {
        return filter (top, skip, null);
    }

    public static List<Reagent> filter (int top, int skip, String filter) {
        return filter (new FetchOptions (top, skip, filter), finder);
    }

    public static List<Reagent> filter (FetchOptions options) {
        return filter (options, finder);
    }

    public static List<Reagent> filter (JsonNode json, int top, int skip) {
        return filter (json, top, skip, finder);
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

    public static Result get (Long id, String expand) {
        return get (id, expand, finder);
    }

    public static Result edits (Long id) {
        return edits (id, Reagent.class);
    }

    public static Reagent getReagent (Long id) {
        return getEntity (id, finder);
    }
    
    public static Result field (Long id, String path) {
        return field (id, path, finder);
    }

    public static Result create () {
        return create (Reagent.class, finder);
    }

    public static Result delete (Long id) {
        return delete (id, finder);
    }

    public static Result update (Long id, String field) {
        return update (id, field, Reagent.class, finder);
    }
}
