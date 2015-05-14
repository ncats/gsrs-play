package ix.srs.controllers;

import java.util.*;
import java.io.*;
import java.sql.*;
import java.util.concurrent.Callable;

import play.*;
import play.mvc.*;
import play.db.ebean.*;
import play.cache.Cache;
import play.data.*;
import play.db.DB;

import ix.core.search.TextIndexer;
import ix.core.search.SearchOptions;
import ix.core.models.*;
import ix.srs.models.*;
import ix.ncats.controllers.App;
import ix.utils.Util;
import com.jolbox.bonecp.BoneCPDataSource;

import ix.srs.views.html.*;


public class SRSApp extends App {
    public static final String[] APPLICATION_FACETS = {
        "Application Type",
        "Application Number",
        "BDNUM"
    };

    public static FacetDecorator[] decorate (TextIndexer.Facet... facets) {
        List<FacetDecorator> decors = new ArrayList<FacetDecorator>();
        for (TextIndexer.Facet f : facets) {
            decors.add(new FacetDecorator (f));
        }
        return decors.toArray(new FacetDecorator[0]);
    }
    
    public static Result bulkAppLoaderForm () {
        return ok (bulk.render());
    }

    /**
     * landing page
     */
    public static Result index () {
        // for now just redirect it to applications
        return redirect (routes.SRSApp.applications(null, 20, 1));
    }

    public static Result _internalServerError (Throwable t) {
        t.printStackTrace();
        return internalServerError
            (error.render(500, "Internal server error: "+t.getMessage()));
    }

    static Result _applications (final String q, int rows, final int page)
        throws Exception {
        Logger.debug("Reagents: q="+q+" rows="+rows+" page="+page);
        final int total = ApplicationFactory.finder.findRowCount();

        if (request().queryString().containsKey("facet") || q != null) {
            TextIndexer.SearchResult result =
                getSearchResult (ix.srs.models.Application.class, q, total);
            
            TextIndexer.Facet[] facets = filter
                (result.getFacets(), APPLICATION_FACETS);
            List<ix.srs.models.Application> applications =
                new ArrayList<ix.srs.models.Application>();
            int[] pages = new int[0];

            if (result.count() > 0) {
                rows = Math.min(result.count(), Math.max(1, rows));
                pages = paging(rows, page, result.count());

                for (int i = (page - 1) * rows, j = 0; j < rows
                        && i < result.count(); ++j, ++i) {
                    applications.add((ix.srs.models.Application)
                                     result.getMatches().get(i));
                }
            }
            return ok(ix.srs.views.html.applications.render
                      (page, rows, result.count(),
                       pages, decorate(facets), applications));
        }
        else {
            TextIndexer.Facet[] facets =
                getFacets(ix.srs.models.Application.class, APPLICATION_FACETS);
            rows = Math.min(total, Math.max(1, rows));
            int[] pages = paging (rows, page, total);               
            
            List<ix.srs.models.Application> applications =
                ApplicationFactory.filter(rows, (page-1)*rows);
            
            return ok (ix.srs.views.html.applications.render
                       (page, rows, total, pages, decorate (facets),
                        applications));
        }
    }
    
    public static Result applications (final String q,
                                       final int rows, final int page) {
        try {
            final String key = "applications/"+ Util.sha1(request ());
            return getOrElse (key, new Callable<Result>() {
                    public Result call () throws Exception {
                        Logger.debug("Cache missed: "+key);
                        return _applications (q, rows, page);
                    }
                });
        }
        catch (Exception ex) {
            return _internalServerError (ex);
        }
    }

    public static Result bulkAppLoader () {
        DynamicForm requestData = Form.form().bindFromRequest();
        String jdbcUrl = requestData.get("jdbcUrl");
        String jdbcUsername = requestData.get("jdbc-username");
        String jdbcPassword = requestData.get("jdbc-password");
        Logger.debug("JDBC: "+jdbcUrl);

        int count = 0;
        Http.MultipartFormData body = request().body().asMultipartFormData();
        Http.MultipartFormData.FilePart part = body.getFile("csv-file");
        if (part != null) {
            String name = part.getFilename();
            String content = part.getContentType();
            Logger.debug("file="+name+" content="+content);
            File file = part.getFile();
            try {
                count = bulkAppLoaderCSV (new FileInputStream (file));
                Logger.debug(count+" record(s) loaded!");
            }
            catch (Exception ex) {
                ex.printStackTrace();
                return internalServerError (ex.getMessage());
            }
        }

        if (jdbcUrl == null || jdbcUrl.equals("")) {
            if (count == 0)
                return badRequest ("No jdbc url is specified!");
        }
        else {
            Connection con = null;
            try {
                BoneCPDataSource bone = new BoneCPDataSource ();
                bone.setJdbcUrl(jdbcUrl);
                bone.setUsername(jdbcUsername);
                bone.setPassword(jdbcPassword);
                con = bone.getConnection();
                count += bulkAppLoader (con);
            }
            catch (Exception ex) {
                ex.printStackTrace();
                return internalServerError (ex.getMessage());
            }
            finally {
                try {
                    if (con != null)
                        con.close();
                }
                catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
        }
        Logger.debug(count+" total record(s) loaded!");
        
        return redirect (routes.SRSApp.applications(null, 20, 1));
    }

    static int bulkAppLoader (Connection con) throws SQLException {
        Statement stm = con.createStatement();
        ResultSet rset = stm.executeQuery
            ("select * from SRSCID.SRSCID_APPLICATION_TYPE_SRS "
             //+"where APP_TYPE='NDA'"
             );
        int count = 0;
        while (rset.next()) {
            ix.srs.models.Application app = new ix.srs.models.Application();
            app.apptype = rset.getString("app_type");
            app.appnumber = rset.getString("app_number");
            
            // there should be more meat to Ingredient here!
            Ingredient ingre = new Ingredient ();
            ingre.name = rset.getString("applicant_ingred_name");
            app.ingredient = ingre;
            
            // ditto for Product
            Product prod = new Product ();
            prod.name = "Product instance of "+rset.getString("product_id");
            app.product = prod;
            
            app.bdnum = rset.getString("bdnum");
            try {
                prod.save();
                ingre.save();
                app.save();
                if (++count % 1000 == 0) {
                    Logger.debug(app.appnumber+": "+count);
                }
            }
            catch (Exception ex) {
                ex.printStackTrace();
                Logger.error("Can't persist application "+app.appnumber, ex);
            }
        }
        rset.close();
        stm.close();
        return count;
    }

    static int bulkAppLoaderCSV (InputStream is) throws Exception {
        BufferedReader br = new BufferedReader (new InputStreamReader (is));
        String[] header = br.readLine().split(",");
        int count = 0;
        for (String line; (line = br.readLine()) != null; ) {
            String[] toks = line.split(",");
            ix.srs.models.Application app = new ix.srs.models.Application();
            for (int i = 0; i < toks.length && i < header.length; ++i) {
                String h = header[i];
                if (h.equalsIgnoreCase("app_type")) {
                    app.apptype = toks[i];
                }
                else if (h.equalsIgnoreCase("app_number")) {
                    app.appnumber = toks[i];
                }
                else if (h.equalsIgnoreCase("product_id")) {
                    Product p = new Product ();
                    p.name = "Product instance of "+toks[i];
                    app.product = p;
                }
                else if (h.equalsIgnoreCase("applicant_ingred_name")) {
                    Ingredient ing = new Ingredient ();
                    ing.name = toks[i];
                    app.ingredient = ing;
                }
                else if (h.equalsIgnoreCase("bdnum")) {
                    app.bdnum = toks[i];
                }
            }
            try {
                app.product.save();
                app.ingredient.save();
                app.save();
                ++count;
            }
            catch (Exception ex) {
                ex.printStackTrace();
                Logger.error("Can't persist application "+app.appnumber, ex);
            }
        }
        br.close();
        return count;
    }
}
