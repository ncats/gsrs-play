package ix.srs.controllers;

import java.util.List;
import play.mvc.Result;
import play.db.ebean.Model;
import com.fasterxml.jackson.databind.JsonNode;

import ix.core.NamedResource;
import ix.core.controllers.EntityFactory;
import ix.srs.models.Application;

@NamedResource(name="applications",
               type=Application.class,
               description="Resource for handling of SRS applications")
public class ApplicationFactory extends EntityFactory {
    public static final Model.Finder<Long, Application> finder = 
        new Model.Finder(Long.class, Application.class);

    public static List<Application> all () { return all (finder); }
    public static Result count () { return count (finder); }
    public static Result page (int top, int skip, String filter) {
        return page (top, skip, filter, finder);
    }

    public static List<Application> filter (int top, int skip) {
        return filter (top, skip, null);
    }

    public static List<Application> filter (int top, int skip, String filter) {
        return filter (new FetchOptions (top, skip, filter), finder);
    }

    public static List<Application> filter (FetchOptions options) {
        return filter (options, finder);
    }

    public static List<Application> filter (JsonNode json, int top, int skip) {
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
        return edits (id, Application.class);
    }

    public static Result field (Long id, String path) {
        return field (id, path, finder);
    }

    public static Result create () {
        return create (Application.class, finder);
    }

    public static Result delete (Long id) {
        return delete (id, finder);
    }

    public static Result update (Long id, String field) {
        return update (id, field, Application.class, finder);
    }
}
