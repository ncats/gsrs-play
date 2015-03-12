package ix.tox21.controllers;

import java.io.*;
import java.security.*;
import java.util.*;
import java.sql.*;
import java.net.*;
import java.util.concurrent.Callable;

import play.*;
import play.cache.Cache;
import play.db.ebean.*;
import play.data.*;
import play.mvc.*;
import com.avaje.ebean.*;

import ix.utils.Global;
import ix.utils.Util;
import ix.core.search.TextIndexer;
import ix.core.models.*;
import ix.ncats.controllers.App;

import ix.tox21.models.*;

public class Tox21App extends App {

    public static Result load () {
        return ok (ix.tox21.views.html.load.render("Load Tox21 QC database"));
    }

    public static Result loader () {
        DynamicForm requestData = Form.form().bindFromRequest();
        if (Play.isProd()) { // production..
            String secret = requestData.get("secret-code");
            if (secret == null || secret.length() == 0
                || !secret.equals(Play.application()
                                  .configuration().getString("ix.secret"))) {
                return unauthorized
                    ("You do not have permission to access resource!");
            }
        }
        
        String jdbcUrl = requestData.get("jdbcUrl");
        String jdbcUsername = requestData.get("jdbc-username");
        String jdbcPassword = requestData.get("jdbc-password");
        Logger.debug("JDBC: "+jdbcUrl);

        if (jdbcUrl == null || jdbcUrl.equals("")) {
            return badRequest ("No JDBC URL specified!");
        }

        Connection con = null;
        try {
            con = DriverManager.getConnection
                (jdbcUrl, jdbcUsername, jdbcPassword);
            load (con);
            return redirect (ix.tox21.controllers
                             .routes.Tox21App.samples(null, 30, 1));
        }
        catch (Exception ex) {
            ex.printStackTrace();
            return internalServerError
                (ix.ncats.views.html.error.render
                 (500, "Internal server error: "+ex));
        }
        finally {
            if (con != null) {
                try { con.close(); }
                catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    static void load (Connection con) throws Exception {
        
    }

    public static Result index () {
        return redirect (ix.tox21.controllers
                         .routes.Tox21App.samples(null, 30, 1));
    }

    public static Result samples (final String q,
                                  final int rows, final int page) {
        try {
            String sha1 = Util.sha1(request ());
            return Cache.getOrElse(sha1, new Callable<Result>() {
                    public Result call () throws Exception {
                        return _samples (q, rows, page);
                    }
                }, CACHE_TIMEOUT);
        }
        catch (Exception ex) {
            ex.printStackTrace();
            return badRequest (ix.ncats.views.html.error.render
                               (404, "Invalid page requested: "+page+ex));
        }
    }

    static Result _samples (final String q, int rows, final int page)
        throws Exception {
        Logger.debug("Samples: q="+q+" rows="+rows+" page="+page);
        
        final int total = Tox21Factory.finder.findRowCount();
        if (request().queryString().containsKey("facet") || q != null) {
            TextIndexer.SearchResult result =
                getSearchResult (Sample.class, q, total);
            
            TextIndexer.Facet[] facets = filter (result.getFacets());
            List<Sample> samples = new ArrayList<Sample>();
            int[] pages = new int[0];
            if (result.count() > 0) {
                rows = Math.min(result.count(), Math.max(1, rows));
                pages = paging (rows, page, result.count());
                
                for (int i = (page-1)*rows, j = 0; j < rows
                         && i < result.count(); ++j, ++i) {
                    samples.add((Sample)result.getMatches().get(i));
                }
            }
            
            return ok (ix.tox21.views.html.samples.render
                       (page, rows, result.count(),
                        pages, facets, samples));
        }
        else {
            String cache = Sample.class.getName()+".facets";
            if (System.currentTimeMillis() - CACHE_TIMEOUT
                <= indexer.lastModified())
                Cache.remove(cache);
            TextIndexer.Facet[] facets = Cache.getOrElse
                (cache, new Callable<TextIndexer.Facet[]>() {
                        public TextIndexer.Facet[] call () {
                            return filter (getFacets (Sample.class, FACET_DIM));
                        }
                    }, CACHE_TIMEOUT);
            
            rows = Math.min(total, Math.max(1, rows));
            int[] pages = {};
            List<Sample> samples = new ArrayList<Sample>();
            if (rows > 0) {
                pages = paging (rows, page, total);
                samples = Tox21Factory.getSamples(rows, (page-1)*rows, null);
            }
            return ok (ix.tox21.views.html.samples.render
                       (page, rows, total, pages, facets, samples));
        }
    }
    
    public static Result sample (final String id) {
        try {
            /*
            String sha1 = Util.sha1(request ());
            return Cache.getOrElse(sha1, new Callable<Result> () {
                    public Result call () throws Exception {
                        return _sample (id);
                    }
                }, CACHE_TIMEOUT);
            */
            return ok ("Sample "+id);
        }
        catch (Exception ex) {
            ex.printStackTrace();
            return internalServerError
                (ix.ncats.views.html.error.render(400, "Invalid sample: "+id));
        }
    }
}
