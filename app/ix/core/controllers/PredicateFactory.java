package ix.core.controllers;

import java.io.*;
import java.util.*;

import play.*;
import play.db.ebean.*;
import play.data.*;
import play.mvc.*;
import com.avaje.ebean.Query;
import com.avaje.ebean.Expr;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import ix.core.NamedResource;
import ix.core.models.Predicate;

@NamedResource(name="predicates", type=Predicate.class)
public class PredicateFactory extends EntityFactory {
    public static final Model.Finder<Long, Predicate> finder = 
        new Model.Finder(Long.class, Predicate.class);

    public static Integer getCount () { 
        try {
            return getCount (finder); 
        }
        catch (Exception ex) {
            Logger.trace("Can't get row count", ex);
        }
        return null;
    }

    public static Result count () { return count (finder); }
    public static Result page (int top, int skip, String expand,
                               String filter) {
        return page (top, skip, expand, filter, finder);
    }

    public static List<Predicate> filter (int top, int skip) {
        return filter (top, skip, null, null);
    }

    public static List<Predicate> filter (int top, int skip, 
                                          String expand, String filter) {
        return filter (top, skip, expand, filter, finder);
    }

    public static List<Predicate> filter (JsonNode json, int top, int skip) {
        return filter (json, top, skip, finder);
    }

    public static Result get (Long id, String expand) {
        return get (id, expand, finder);
    }

    public static Result field (Long id, String path) {
        return field (id, path, finder);
    }

    public static Result edits (Long id) {
        return edits (id, Predicate.class);
    }

    public static Result create () {
        return create (Predicate.class, finder);
    }

    public static Result delete (Long id) {
        return delete (id, finder);
    }

    public static Result update (Long id, String field) {
        return update (id, field, Predicate.class, finder);
    }
}

