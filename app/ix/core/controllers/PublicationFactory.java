package ix.core.controllers;

import java.io.*;
import java.security.*;
import java.util.*;

import ix.core.util.Java8Util;
import play.*;
import play.db.ebean.*;
import play.data.*;
import play.mvc.*;

import com.avaje.ebean.Query;
import com.avaje.ebean.annotation.Transactional;
import com.avaje.ebean.Expr;
import com.avaje.ebean.Transaction;
import com.avaje.ebean.Ebean;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import ix.core.models.Publication;
import ix.core.models.Author;
import ix.core.models.PubAuthor;
import ix.core.NamedResource;
import ix.utils.Eutils;
import ix.core.plugins.EutilsPlugin;

@NamedResource(name="publications",
               type=Publication.class,
               description="Resource for handling of publications")
public class PublicationFactory extends EntityFactory {
    public static final Model.Finder<Long, Publication> finder = 
        new Model.Finder(Long.class, Publication.class);
    public static final EutilsPlugin eutils =
        Play.application().plugin(EutilsPlugin.class);

    public static List<Publication> all () { return all (finder); }
    public static Result count () { return count (finder); }
    public static Result page (int top, int skip, String filter) {
        return page (top, skip, filter, finder);
    }

    public static List<Publication> filter (int top, int skip) {
        return filter (top, skip, null);
    }

    public static List<Publication> filter (int top, int skip, String filter) {
        return filter (new FetchOptions (top, skip, filter), finder);
    }

    public static List<Publication> filter (FetchOptions options) {
        return filter (options, finder);
    }

    public static List<Publication> filter (JsonNode json, int top, int skip) {
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
        String type = request().getQueryString("type");
        if (type != null) {
            if ("pmid".equalsIgnoreCase(type)) {
                Query<Publication> q = finder.query();
                if (expand != null) {
                    q = q.fetch(expand);
                }
                Publication pub = q.where().eq("pmid", id).findUnique();
                if (pub != null) {
                    ObjectMapper mapper = getEntityMapper ();
                    return Java8Util.ok (mapper.valueToTree(pub));
                }

                return notFound ("Not found: "+request().uri());
            }

            return badRequest ("Unknown type: "+type);
        }
        return get (id, expand, finder);
    }

    public static Result edits (Long id) {
        return edits (id, Publication.class);
    }

    public static Publication getPub (Long id) {
        return getEntity (id, finder);
    }
    
    public static Result relatedByPMID (long pmid) {
        ObjectMapper mapper = getEntityMapper ();
        return Java8Util.ok (mapper.valueToTree(getRelated (pmid)));
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

    public static List<Publication> getRelated (long pmid) {
        List<Long> pmids = Eutils.fetchRelated(pmid);
        List<Publication> pubs = new ArrayList<Publication>();
        for (Long id : pmids) {
            Publication pub = registerIfAbsent (id);
            if (pub != null)
                pubs.add(pub);
        }
        return pubs;
    }

    public static Publication registerIfAbsent (long pmid) {
        Publication pub = byPMID (pmid);
        if (pub == null) {
            pub = eutils.getPublication(pmid);
            if (pub != null) {
                for (PubAuthor p : pub.authors) {
                    p.author = instrument (p.author);
                }
                Transaction tx = Ebean.beginTransaction();
                try {
                    pub.save();
                    tx.commit();
                }
                catch (Exception ex) {
                    Logger.trace("Can't persist publication "+pub.pmid, ex);
                }
                finally {
                    Ebean.endTransaction();
                }
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
    
    public static Publication getEntity( long id){
        return getEntity (id, finder);
    }
}
