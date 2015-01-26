package ix.publications.controllers;

import java.io.*;
import java.sql.*;
import java.util.*;
import java.net.*;
import java.util.concurrent.Callable;
import java.util.Iterator;
import controllers.routes;
import play.*;
import play.cache.Cache;
import play.data.*;
import play.mvc.*;
import play.libs.ws.*;
import ix.core.models.*;
import ix.idg.models.*;
import ix.core.controllers.EntityFactory;
import ix.core.controllers.PublicationFactory;
import ix.core.controllers.SearchFactory;
import ix.core.search.TextIndexer;
import ix.ncats.controllers.reach.ProjectFactory;
import ix.ncats.models.Project;
import ix.utils.Global;
import ix.utils.Util;
import com.fasterxml.jackson.databind.ObjectMapper;


public class ReachApp extends Controller {
        
    static public final int CACHE_TIMEOUT = 60*60;
    static public final int MAX_FACETS = 100;
    
    public static final String[] PUBLICATION_FACETS = {
        "Program",
               "Journal Year Published",
               "Author",
               "Category",
               "MeSH",
               "Journal"
    };
    
    public static final String[] PROJECT_FACETS = {
        "Program",
               "Journal Year Published",
               "Author",
               "Category",
               "MeSH"
    };
        
    public static String[] toJsonLabels (TextIndexer.Facet facet) {
          String[] labels = new String[facet.getValues().size()];
          for (int i = 0; i < labels.length; ++i)
            labels[i] = facet.getValues().get(i).getLabel();
          return labels;
            }

    
    public static int[] paging (int rowsPerPage, int page, int total) {
        int max = (total+ rowsPerPage-1)/rowsPerPage;
        if (page < 0 || page > max) {
            throw new IllegalArgumentException ("Bogus page "+page);
        }
        
        int[] pages;
        if (max <= 10) {
            pages = new int[max];
            for (int i = 0; i < pages.length; ++i)
                pages[i] = i+1;
        }
        else if (page >= max-3) {
            pages = new int[10];
            for (int i = pages.length; --i >= 0; )
                pages[i] = max--;
        }
        else {
            pages = new int[10];
            int i = 0;
            for (; i < 7; ++i)
                pages[i] = i+1;
            if (page >= pages[i-1]) {
                // now shift
                pages[--i] = page;
                while (i-- > 0)
                    pages[i] = pages[i+1]-1;
            }
            pages[8] = max-1;
            pages[9] = max;
        }
        return pages;
    }
    
    public static Result index () {
        return ok (ix.ncats.views.html.index.render
                   ("Pharos: Illuminating the Druggable Genome"));
    }

    public static Result error (int code, String mesg) {
        return ok (ix.idg.views.html.error.render(code, mesg));
    }

    public static Result publication (long id) {
        try {
            Publication p = PublicationFactory.getPub(id);
            return ok (ix.publications.views.html.details.render((int) id, "publications",p));
        }
        catch (Exception ex) {
            return internalServerError
                (ix.idg.views.html.error.render(500, "Internal server error"));
        }
    }
    public static Result project (long id) {
        try {
            Project p = ProjectFactory.getProject(id);
            return ok (ix.projects.views.html.details.render("projects",p));
        }
        catch (Exception ex) {
            return internalServerError
                (ix.idg.views.html.error.render(500, "Internal server error"));
        }
        }
    
    public static String sha1 (TextIndexer.Facet facet, int value) {
        return Util.sha1(facet.getName(),
                         facet.getValues().get(value).getLabel());
    }
    
    public static String encode (TextIndexer.Facet facet) {
        try {
            return URLEncoder.encode(facet.getName(), "utf8");
        }
        catch (Exception ex) {
            Logger.trace("Can't encode string "+facet.getName(), ex);
        }
        return null;
    }
    
    public static String encode (TextIndexer.Facet facet, int i) {
        String value = facet.getValues().get(i).getLabel();
        try {
            return URLEncoder.encode(value, "utf8");
        }
        catch (Exception ex) {
            Logger.trace("Can't encode string "+value, ex);
        }
        return null;
    }

    public static String page (int rows, int page) {
        String url = "http"+ (request().secure() ? "s" : "") + "://"
            +request().host()
            +request().uri();
        if (url.charAt(url.length() -1) == '?') {
            url = url.substring(0, url.length()-1);
        }
        //Logger.debug(url);

        Map<String, Collection<String>> params =
            WS.url(url).getQueryParameters();
        
        // remove these
        params.remove("rows");
        params.remove("page");
        StringBuilder uri = new StringBuilder ("?rows="+rows+"&page="+page);
        for (Map.Entry<String, Collection<String>> me : params.entrySet()) {
            for (String v : me.getValue())
                uri.append("&"+me.getKey()+"="+v);
        }
        
        return uri.toString();
    }

    public static String url (String... remove) {
        String url = "http"+ (request().secure() ? "s" : "") + "://"
            +request().host()
            +request().uri();
        if (url.charAt(url.length()-1) == '?') {
            url = url.substring(0, url.length()-1);
        }
        Logger.debug(">> uri="+request().uri());
        
    StringBuilder uri = new StringBuilder ("?");
    Map<String, Collection<String>> params =
        WS.url(url).getQueryParameters();
    for (Map.Entry<String, Collection<String>> me : params.entrySet()) {
        boolean matched = false;
        for (String s : remove)
            if (s.equals(me.getKey())) {
                matched = true;
                break;
            }
        
        if (!matched) {
            for (String v : me.getValue())
                if (v != null)
                    uri.append(me.getKey()+"="+v+"&");
        }
    }
    Logger.debug(">> "+uri);
    return uri.substring(0, uri.length()-1);
    }

    public static boolean hasFacet (TextIndexer.Facet facet, int i) {
        String[] facets = request().queryString().get("facet");
        if (facets != null) {
            for (String f : facets) {
                String[] toks = f.split("/");
                if (toks.length == 2) {
                    try {
                        String name = toks[0];
                        String value = toks[1];
                        /*
                        Logger.debug("Searching facet "+name+"/"+value+"..."
                                     +facet.getName()+"/"
                                     +facet.getValues().get(i).getLabel());
                        */
                        boolean matched = name.equals(facet.getName())
                            && value.equals(facet.getValues()
                                            .get(i).getLabel());
                        
                        if (matched)
                            return matched;
                    }
                    catch (Exception ex) {
                        Logger.trace("Can't URL decode string", ex);
                    }
                }
            }
        }
        
        return false;
    }

    static List<TextIndexer.Facet> getFacets
        (final Class kind, final int fdim) {
        try {
            TextIndexer.SearchResult result =
                SearchFactory.search(kind, null, 0, 0, fdim, null);
            return result.getFacets();
        }
        catch (IOException ex) {
            Logger.trace("Can't retrieve facets for "+kind, ex);
        }
        return new ArrayList<TextIndexer.Facet>();
    }

    static TextIndexer.Facet[] filter (List<TextIndexer.Facet> facets,
                                       String... names) {
        if (names == null || names.length == 0)
            return facets.toArray(new TextIndexer.Facet[0]);
        
        List<TextIndexer.Facet> filtered = new ArrayList<TextIndexer.Facet>();
        for (String n : names) {
            for (TextIndexer.Facet f : facets)
                if (n.equals(f.getName()))
                    filtered.add(f);
        }
        return filtered.toArray(new TextIndexer.Facet[0]);
    }
    
    static TextIndexer.SearchResult getSearchResult
    (final Class kind, final String q, final int total) {
    
    final Map<String, String[]> query =  new HashMap<String, String[]>();
    query.putAll(request().queryString());
            
    List<String> qfacets = new ArrayList<String>();
    if (q != null && q.indexOf('/') > 0) {
        // treat this as facet
        if (query.get("facet") != null) {
            for (String f : query.get("facet"))
                qfacets.add(f);
        }
        qfacets.add("MeSH/"+q);
        query.put("facet", qfacets.toArray(new String[0]));
    }
    
    List<String> args = new ArrayList<String>();
    args.add(request().uri());
    if (q != null)
        args.add(q);
    for (String f : qfacets)
        args.add(f);
Collections.sort(args);
    
    // filtering
    try {
    long start = System.currentTimeMillis();
    String sha1 = Util.sha1(args.toArray(new String[0]));
        TextIndexer.SearchResult result = Cache.getOrElse
            (sha1, new Callable<TextIndexer.SearchResult>() {
                 public TextIndexer.SearchResult call () throws Exception {
                     return SearchFactory.search
                     (kind, q != null
                      && q.indexOf('/') > 0 ? null : q,
                      total, 0, 20, query);
                 }
             }, CACHE_TIMEOUT);
    
    double ellapsed = (System.currentTimeMillis() - start)*1e-3;
    Logger.debug(String.format("Ellapsed %1$.3fs to retrieve "
                               +"results for "
                               +sha1.substring(0, 8)+"...",
                               ellapsed));
    
        return result;
    }
    catch (Exception ex) {
        Logger.trace("Unable to perform search", ex);
    }
    return null;
}

    
    public static Result publications (final String q, int rows, final int page) {
        Logger.debug("Publications: q="+q+" rows="+rows+" page="+page);
        try {
            final int total = PublicationFactory.finder.findRowCount();
                    if (request().queryString().containsKey("facet") || q != null) {
                TextIndexer.SearchResult result =
                    getSearchResult (Publication.class, q, total);
                
                TextIndexer.Facet[] facets = filter
                    (result.getFacets(), PUBLICATION_FACETS);
                List<Publication> publications = new ArrayList<Publication>();
                int[] pages = new int[0];
                if (result.count() > 0) {
                    rows = Math.min(result.count(), Math.max(1, rows));
                    pages = paging (rows, page, result.count());
                    
                    for (int i = (page-1)*rows, j = 0; j < rows
                             && i < result.count(); ++j, ++i) {
                        publications.add((Publication)result.getMatches().get(i));
                    }
                }

                String format = request().getQueryString("format");
                if (format != null && format.equalsIgnoreCase("json")) {
                    ObjectMapper mapper =
                        new EntityFactory.EntityMapper (BeanViews.Full.class);
                    return ok (mapper.valueToTree(publications));
                }
                
                return ok (ix.publications.views.html.publications.render
                           (null, page, rows, result.count(),
                            pages, facets, publications));
            }
            
            else {
                TextIndexer.Facet[] facets = Cache.getOrElse
                    (Publication.class.getName()+".facets",
                     new Callable<TextIndexer.Facet[]>() {
                            public TextIndexer.Facet[] call () {
                                return filter (getFacets (Publication.class, 20),
                                               PUBLICATION_FACETS);
                            }
                        }, 60);
            
                rows = Math.min(total, Math.max(1, rows));
                int[] pages = paging (rows, page, total);               

                List<Publication> publications =
                    PublicationFactory.getPubs(rows, (page-1)*rows, null);

                String format = request().getQueryString("format");
                if (format != null && format.equalsIgnoreCase("json")) {
                    ObjectMapper mapper =
                        new EntityFactory.EntityMapper (BeanViews.Full.class);
                    return ok (mapper.valueToTree(publications));
                }
                
                return ok (ix.publications.views.html.publications.render
                           (null, page, rows, total, pages, facets, publications));
            }
        }
        
        catch (Exception ex) {
            ex.printStackTrace();
            return badRequest (ix.idg.views.html.error.render
                               (404, "Invalid page requested: "+page+ex));
        }
    }
    
    public static Result projects (final String q, int rows, final int page) {
        Logger.debug("Projects: q="+q+" rows="+rows+" page="+page);
        try {
            final int total = ProjectFactory.finder.findRowCount();
                    if (request().queryString().containsKey("facet") || q != null) {
                TextIndexer.SearchResult result =
                    getSearchResult (Project.class, q, total);
                
                TextIndexer.Facet[] facets = filter
                    (result.getFacets(), PROJECT_FACETS);
                List<Project> projects = new ArrayList<Project>();
                int[] pages = new int[0];
                if (result.count() > 0) {
                    rows = Math.min(result.count(), Math.max(1, rows));
                    pages = paging (rows, page, result.count());
                    
                    for (int i = (page-1)*rows, j = 0; j < rows
                             && i < result.count(); ++j, ++i) {
                        projects.add((Project)result.getMatches().get(i));
                    }
                }
                
                return ok (ix.projects.views.html.projects.render
                           (null, page, rows, result.count(),
                            pages, facets, projects));
            }
            
            else {
                TextIndexer.Facet[] facets = Cache.getOrElse
                    (Project.class.getName()+".facets",
                     new Callable<TextIndexer.Facet[]>() {
                            public TextIndexer.Facet[] call () {
                                return filter (getFacets (Project.class, 20),
                                               PROJECT_FACETS);
                            }
                        }, 60);
            
                rows = Math.min(total, Math.max(1, rows));
                int[] pages = paging (rows, page, total);               

                List<Project> projects =
                    ProjectFactory.getProjs(rows, (page-1)*rows, null);
                return ok (ix.projects.views.html.projects.render
                           (null, page, rows, total, pages, facets, projects));
            }
            }
        
        catch (Exception ex) {
            ex.printStackTrace();
            return badRequest (ix.idg.views.html.error.render
                               (404, "Invalid page requested: "+page+ex));
        }
    }
    public static Result search (String kind) {
        Logger.info("KIND=====================" + kind);
        try {
            String q = request().getQueryString("q");
            if (kind != null && !"".equals(kind)) {
                if (Publication.class.isAssignableFrom(Class.forName(kind)))
                    return redirect (ix.publications.controllers.routes.ReachApp.publications(q, 20, 1));
                if (Project.class.isAssignableFrom(Class.forName(kind)))
                    return redirect (ix.publications.controllers.routes.ReachApp.projects(q, 10, 1));
            }
            
            // generic entity search..
            return search (5);
        }
        catch (Exception ex) {
            Logger.debug("Can't resolve class: "+kind, ex);
        }
            
        return badRequest (ix.idg.views.html.error.render
                           (400, "Invalid request: "+request().uri()));
    }

    static <T> List<T> filter (Class<T> cls, List values, int max) {
        List<T> fv = new ArrayList<T>();
        for (Object v : values) {
            if (cls.isAssignableFrom(v.getClass()) && fv.size() < max) {
                fv.add((T)v);
            }
        }
        return fv;
    }

 
    public static Result search (int rows) {
        final String query = request().getQueryString("q");
        Logger.debug("Query: \""+query+"\"");

        String sha1 = Util.sha1(query);
        try {
            TextIndexer.SearchResult result;
            final Map<String, String[]> queryString =
                new HashMap<String, String[]>();
            queryString.putAll(request().queryString());
            
            if (query.indexOf('/') > 0) { // use mesh facet
                result = Cache.getOrElse
                (sha1, new Callable<TextIndexer.SearchResult>() {
                        public TextIndexer.SearchResult
                            call ()  throws Exception {
                            
                            // append this facet to the list 
                            List<String> f = new ArrayList<String>();
                            f.add("MeSH/"+query);
                            String[] ff = queryString.get("facet");
                            if (ff != null) {
                                for (String fv : ff)
                                    f.add(fv);
                            }
                            queryString.put("facet", f.toArray(new String[0]));
                            
                            return SearchFactory.search
                            (null, null, 500, 0, 20, queryString);
                        }
                    }, 60);
            }
            else {
                result = Cache.getOrElse
                    (sha1, new Callable<TextIndexer.SearchResult>() {
                            public TextIndexer.SearchResult
                                call () throws Exception {
                                return SearchFactory.search
                                (null, query, 500, 0, 20, queryString);
                            }
                        }, 60);
            }
            
            TextIndexer.Facet[] facets = filter
                (result.getFacets(), PUBLICATION_FACETS);
            int max = Math.min(rows, Math.max(1,result.count()));
            int [] pages = paging (rows, max, result.count());
            int totalPublications = 0;
            for (TextIndexer.Facet f : result.getFacets()) {
                if (f.getName().equals("ix.Class")) {
                    for (TextIndexer.FV fv : f.getValues()) {
                        if (Publication.class.getName().equals(fv.getLabel()))
                            totalPublications = fv.getCount();
                        }
                }
            }

            List<Publication> publications =
                filter (Publication.class, result.getMatches(), max);
            List<Disease> diseases =
                filter (Disease.class, result.getMatches(), max);
            
            return ok (ix.publications.views.html.publications.render
                       ( query, 1, max, totalPublications, pages, facets,
                        publications ));
        }
        catch (Exception ex) {
            Logger.trace("Can't execute search \""+query+"\"", ex);
        }
        
        return internalServerError (ix.idg.views.html.error.render
                                    (500, "Unable to fullfil request"));
    }


}
