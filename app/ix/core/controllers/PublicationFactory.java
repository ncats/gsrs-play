package ix.core.controllers;

import java.io.*;
import java.security.*;
import java.util.*;

import play.*;
import play.db.ebean.*;
import play.data.*;
import play.mvc.*;
import com.avaje.ebean.Expr;

import com.fasterxml.jackson.databind.JsonNode;
import ix.core.models.Publication;
import ix.core.models.Author;
import ix.core.models.PubAuthor;
import ix.core.NamedResource;
import ix.utils.Eutils;


@NamedResource(name="publications", type=Publication.class)
public class PublicationFactory extends EntityFactory {
    public static final Model.Finder<Long, Publication> finder = 
        new Model.Finder(Long.class, Publication.class);

    public static List<Publication> all () { return all (finder); }
    public static Result count () { return count (finder); }
    public static Result page (int top, int skip, String expand,
                               String filter) {
        return page (top, skip, expand, filter, finder);
    }

    public static List<Publication> filter (int top, int skip) {
        return filter (top, skip, null, null);
    }

    public static List<Publication> filter (int top, int skip, 
                                            String expand, String filter) {
        return filter (top, skip, expand, filter, finder);
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

    public static Publication fetchIfAbsent (long pmid) {
        Publication pub = byPMID (pmid);
        if (pub == null) {
            pub = Eutils.fetchPublication(pmid);
            if (pub != null) {
                for (PubAuthor p : pub.authors) {
                    p.author = instrument (p.author);
                }
                pub.save();
            }
        }
        return pub;
    }

    static final Model.Finder<Long, Author> authorDb = 
        new Model.Finder(Long.class, Author.class);
    static Author instrument (Author a) {
        List<Author> authors = authorDb
            .where(Expr.and(Expr.eq("lastname", a.lastname),
                            Expr.eq("forename", a.forename)))
            .findList();

        Author author = a;
        if (!authors.isEmpty()) {
            author = authors.iterator().next();
        }

        return author;
    }
}
