package ix.core.controllers;

import java.util.*;

import play.*;
import play.mvc.Result;
import play.db.ebean.*;

import com.fasterxml.jackson.databind.JsonNode;
import ix.core.models.Journal;
import ix.core.NamedResource;


@NamedResource(name="journals", type=Journal.class)
public class JournalFactory extends EntityFactory {
    public static final Model.Finder<Long, Journal> finder = 
        new Model.Finder(Long.class, Journal.class);

    public static List<Journal> all () { return all (finder); }
    public static Result count () { return count (finder); }
    public static Result page (int top, int skip, String expand,
                               String filter) {
        return page (top, skip, expand, filter, finder);
    }

    public static List<Journal> filter (int top, int skip) {
        return filter (top, skip, null, null);
    }

    public static List<Journal> filter (int top, int skip, 
                                            String expand, String filter) {
        return filter (top, skip, expand, filter, finder);
    }

    public static List<Journal> filter (JsonNode json, int top, int skip) {
        return filter (json, top, skip, finder);
    }

    public static Result get (Long id, String select) {
        return get (id, select, finder);
    }

    public static Result field (Long id, String path) {
        return field (id, path, finder);
    }

    public static Result create () {
        return create (Journal.class, finder);
    }

    public static Result delete (Long id) {
        return delete (id, finder);
    }

    public static Result update (Long id, String field) {
        return update (id, field, Journal.class, finder);
    }
}
