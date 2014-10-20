package ix.core.controllers;

import java.io.*;
import java.security.*;
import java.util.*;

import play.*;
import play.db.ebean.*;
import play.data.*;
import play.mvc.*;

import com.fasterxml.jackson.databind.JsonNode;
import ix.core.models.Publication;

public class PublicationFactory extends EntityFactory {
    public static final Model.Finder<Long, Publication> finder = 
        new Model.Finder(Long.class, Publication.class);

    public static List<Publication> all () { return all (finder); }
    public static Result count () { return count (finder); }
    public static Result page (int top, int skip, String expand,
                               String filter) {
        return page (top, skip, expand, filter, finder);
    }

    public static List<Publication> filter (JsonNode json, int top, int skip) {
        return filter (json, top, skip, finder);
    }

    public static Result get (Long id, String select) {
        return get (id, select, finder);
    }

    public static Result field (Long id, String path) {
        return field (id, path, finder);
    }

    public static Result create () {
        return create (Publication.class, finder);
    }

    public static Result delete (Long id) {
        return delete (id, finder);
    }

    public static Result update (Long id, String field) {
        return update (id, field, Publication.class, finder);
    }

    public static Publication byPMID (long pmid) {
        return finder.where().eq("pmid", pmid).findUnique();
    }
}
