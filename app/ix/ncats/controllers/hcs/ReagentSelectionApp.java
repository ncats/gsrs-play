package ix.ncats.controllers.hcs;

import java.util.*;
import java.io.*;
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
import ix.ncats.models.hcs.*;
import ix.ncats.controllers.App;
import ix.utils.Util;

public class ReagentSelectionApp extends App {
    public static Result loader () {
        return ok (ix.ncats.views.html.hcs.load.render("HCS Reagent Loader"));
    }

    public static Result index () {
        return redirect (routes.ReagentSelectionApp.reagents(null, 20, 1));
    }

    public static Result load () {
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

        String data = requestData.get("cell-type");
        Logger.debug("Cell Type: "+data);
        Reagent.CellType type = Reagent.CellType.valueOf(data);

        Http.MultipartFormData body = request().body().asMultipartFormData();
        Http.MultipartFormData.FilePart part = body.getFile("reagent-file");
        int count = 0;
        if (part != null) {
            String name = part.getFilename();
            String content = part.getContentType();
            Logger.debug("file="+name+" content="+content);
            try {
                File file = part.getFile();
                BufferedReader br = new BufferedReader (new FileReader (file));
                String[] header = br.readLine().split("\t");
                for (String line; (line = br.readLine()) != null; ) {
                    String[] toks = line.split("\t");
                    if (toks.length > 3) {
                        Logger.debug("--");
                        for (int i = 0; i < header.length; ++i) {
                            if (i < toks.length) {
                                Logger.debug(header[i]+": \""+toks[i]+"\"");
                            }
                        }

                        try {
                            Reagent reagent = new Reagent ();
                            reagent.celltype = type;
                            reagent.apptype = toks[0];
                            reagent.color = toks[1];
                            reagent.application = toks[2];
                            reagent.name = toks[3];
                            if (toks[4].length() > 0) 
                                reagent.excitation = Integer.parseInt(toks[4]);
                            if (toks[5].length() > 0)
                                reagent.emission = Integer.parseInt(toks[5]);
                            reagent.barcode = toks[6];
                            reagent.save();
                            ++count;
                        }
                        catch (NumberFormatException ex) {
                            Logger.warn("Bogus wavelength: "+line);
                        }
                        catch (Exception ex) {
                            ex.printStackTrace();
                            Logger.error("Not enough fields: "+line);
                            //return internalServerError (ex.getMessage());
                        }
                    }
                    else {
                        Logger.warn("Skipping line: "+line);
                    }
                }
                br.close();
            }
            catch (Exception ex) {
                return internalServerError (ex.getMessage());
            }
        }
        else {
            return badRequest ("No reagent-file specified!");
        }
        
        return ok (count+" reagent(s) loaded!");
    }

    public static final String[] REAGENT_FACETS = {
        "Cell Type",
        "Application Type",
        "Color",
        "Application",
        "Excitation",
        "Emission"
    };

    public static FacetDecorator[] decorate (TextIndexer.Facet... facets) {
        List<FacetDecorator> decors = new ArrayList<FacetDecorator>();
        // override decorator as needed here
        for (int i = 0; i < facets.length; ++i) {
            decors.add(new FacetDecorator (facets[i]));
        }
        return decors.toArray(new FacetDecorator[0]);
    }

    public static Result _internalServerError (Throwable t) {
        t.printStackTrace();
        return internalServerError
            (ix.ncats.views.html.hcs.error.render
             (500, "Internal server error: "+t.getMessage()));
    }

    static Result _reagents (final String q, int rows, final int page)
        throws Exception {
        Logger.debug("Reagents: q="+q+" rows="+rows+" page="+page);
        final int total = ReagentFactory.finder.findRowCount();

        if (request().queryString().containsKey("facet") || q != null) {
            TextIndexer.SearchResult result =
                getSearchResult (Reagent.class, q, total);
            
            TextIndexer.Facet[] facets = filter
                (result.getFacets(), REAGENT_FACETS);
            List<Reagent> reagents = new ArrayList<Reagent>();
            int[] pages = new int[0];

            if (result.count() > 0) {
                rows = Math.min(result.count(), Math.max(1, rows));
                pages = paging(rows, page, result.count());

                for (int i = (page - 1) * rows, j = 0; j < rows
                        && i < result.count(); ++j, ++i) {
                    reagents.add((Reagent) result.getMatches().get(i));
                }
            }
            return ok(ix.ncats.views.html.hcs.reagents.render
                      (page, rows, result.count(),
                       pages, decorate(facets), reagents));
        }
        else {
            TextIndexer.Facet[] facets =
                getFacets(Reagent.class, REAGENT_FACETS);
            rows = Math.min(total, Math.max(1, rows));
            int[] pages = paging (rows, page, total);               
            
            List<Reagent> reagents =
                ReagentFactory.filter(rows, (page-1)*rows);
            
            return ok (ix.ncats.views.html.hcs.reagents.render
                       (page, rows, total, pages, decorate (facets),
                        reagents));
        }
    }
    
    public static Result reagents (final String q,
                                   final int rows, final int page) {
        try {
            final String key = "reagents/"+ Util.sha1(request ());
            return getOrElse (key, new Callable<Result>() {
                    public Result call () throws Exception {
                        Logger.debug("Cache missed: "+key);
                        return _reagents (q, rows, page);
                    }
                });
        }
        catch (Exception ex) {
            return _internalServerError (ex);
        }
    }
}
