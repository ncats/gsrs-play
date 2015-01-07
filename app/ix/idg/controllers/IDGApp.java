package ix.idg.controllers;


import ix.core.controllers.SearchFactory;
import ix.core.models.Text;
import ix.core.models.Value;
import ix.core.models.XRef;
import ix.core.search.TextIndexer;
import ix.idg.models.Disease;
import ix.idg.models.Target;
import ix.utils.Util;
import play.Logger;
import play.cache.Cache;
import play.libs.ws.WS;
import play.mvc.Controller;
import play.mvc.Result;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;


public class IDGApp extends Controller {
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
	return ok (ix.idg.views.html.index.render
		   ("Pharos: Illuminating the Druggable Genome"));
    }

    public static Result error (int code, String mesg) {
	return ok (ix.idg.views.html.error.render(code, mesg));
    }

    public static Result target (long id) {
	try {
	    Target t = TargetFactory.getTarget(id);
	    return ok (ix.idg.views.html.targetdetails.render(t));
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
	
	Map<String, Collection<String>> params =
	    WS.url(url).getQueryParameters();
	for (String p : remove)
	    params.remove(p);
	
	StringBuilder uri = new StringBuilder ("?");
	for (Map.Entry<String, Collection<String>> me : params.entrySet()) {
	    if (me.getKey() != null)
		for (String v : me.getValue())
		    if (v != null)
			uri.append(me.getKey()+"="+v+"&");
	}
	Logger.debug(">> "+uri);
	return uri.substring(0, uri.length() - 1);
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
    
    public static Result targets (int rows, final int page) {
	Logger.debug("Targets: rows="+rows+" page="+page);
	try {
	    final int total = TargetFactory.finder.findRowCount();
	    if (request().queryString().containsKey("facet")) {
		// filtering
		TextIndexer.SearchResult result = Cache.getOrElse
		    (Util.sha1Request(request(), "facet"),
		     new Callable<TextIndexer.SearchResult>() {
			 public TextIndexer.SearchResult call () {
			     try {
				 return SearchFactory.search
				 (Target.class, null, total, 0,
				  20, request().queryString());
			     }
			     catch (IOException ex) {
				 Logger.trace("Can't perform search", ex);
			     }
			     return null;
			 }
		     }, 60);
		rows = Math.min(result.count(), Math.max(1, rows));
		int[] pages = paging (rows, page, result.count());

		TextIndexer.Facet[] facets = filter
		    (result.getFacets(), "IDG Classification",
		     "IDG Target Family",
		     "TCRD Disease",
		     "TCRD Drug");
		
		List<Target> targets = new ArrayList<Target>();
		for (int i = (page-1)*rows, j = 0; j < rows
			 && i < result.count(); ++j, ++i) {
		    targets.add((Target)result.getMatches().get(i));
		}
		
		return ok (ix.idg.views.html.targets.render
			   (page, rows, result.count(),
			    pages, facets, targets));
	    }
	    else {
		TextIndexer.Facet[] facets = Cache.getOrElse
		    ("TargetFacets", new Callable<TextIndexer.Facet[]>() {
			    public TextIndexer.Facet[] call () {
				return filter (getFacets (Target.class, 20),
					       "IDG Classification",
					       "IDG Target Family",
					       "TCRD Disease",
					       "TCRD Drug"
					       //"MeSH",
					       //"Keyword"
					       );
			    }
			}, 3600);
		
		rows = Math.min(total, Math.max(1, rows));
		int[] pages = paging (rows, page, total);		

		List<Target> targets =
		    TargetFactory.getTargets(rows, (page-1)*rows, null);
		return ok (ix.idg.views.html.targets.render
			   (page, rows, total, pages, facets, targets));
	    }
	}
	catch (Exception ex) {
	    ex.printStackTrace();
	    return badRequest (ix.idg.views.html.error.render
			       (404, "Invalid page requested: "+page+ex));
	}
    }

	public static Result disease(long id) {
		try {
			Disease d = DiseaseFactory.getDisease(id);
			for (XRef xref : d.links) {
				System.out.println(xref.refid + "/" + xref.kind + "/" + xref.deRef());
				List<Value> props = xref.properties;
				for (Value prop: props) {
					System.out.println("prop = " + prop);
					if (prop.getValue() instanceof Text) {
						Text text = (Text) prop.getValue();
						System.out.println("\t"+text.getText()+"/"+text.getValue());
					}
				}
				System.out.println();
			}
			return DiseaseFactory.create();
//			return ok(ix.idg.views.html.diseasedetails.render(t));
		} catch (Exception ex) {
			return internalServerError
					(ix.idg.views.html.error.render(500, "Internal server error"));
		}
	}

	public static Result diseases(int rows, int page) throws Exception {
		TextIndexer.Facet[] facets = new TextIndexer.Facet[]{};
//		TextIndexer.Facet[] facets = Cache.getOrElse("DiseaseFacets", new Callable<TextIndexer.Facet[]>() {
//			public TextIndexer.Facet[] call() {
//				return filter(getFacets(Disease.class, 20),
//						"IDG Classification",
//						"IDG Target Family"
//						//"MeSH",
//						//"Keyword"
//				);
//			}
//		}, 3600);
		int total = DiseaseFactory.finder.findRowCount();
		rows = Math.min(total, Math.max(1, rows));
		int[] pages = paging(rows, page, total);

		List<Disease> diseases =
				DiseaseFactory.getDiseases(rows, (page - 1) * rows, null);

		return ok(ix.idg.views.html.diseases.render(page, rows, pages, facets, diseases));
	}
}
