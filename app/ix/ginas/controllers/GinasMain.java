package ix.ginas.controllers;

import ix.core.controllers.search.SearchFactory;
import ix.core.search.TextIndexer.FV;
import ix.core.search.TextIndexer.Facet;
import ix.core.search.TextIndexer.SearchResult;
import ix.ginas.models.v1.ChemicalSubstance;
import ix.ginas.models.v1.Substance;
import ix.utils.Util;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import play.Logger;
import play.cache.Cache;
import play.data.DynamicForm;
import play.data.Form;
import play.libs.ws.WS;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class GinasMain extends Controller {

	static public final int CACHE_TIMEOUT = 60*60;
	static public final int MAX_FACETS = 100;
	static final String YEAR_FACET = "Journal Year Published";

	//2003-12-13T18:30:02Z
	static final DateFormat DATE_FORMAT = new SimpleDateFormat
			("yyy-MM-dd'T'HH:mm:ss'Z'");
	static final String TIMESTAMP = DATE_FORMAT.format(new java.util.Date ());

	public static final String[] PROJECT_FACETS = {
		"Program",
		YEAR_FACET,
		"Author",
		"Category",
		"MeSH"
	};

	public static Result login () {
		return ok (ix.ginas.views.html.login.render());
	}

	public static Result authenticate () {
		DynamicForm requestData = Form.form().bindFromRequest();
		String region = requestData.get("region");
		String username = requestData.get("username");
		String password = requestData.get("password");
		Logger.info("Logged in as " + username+ " from "+ region + ". Your top secret password is: " + password);
		return ok (ix.ginas.views.html.index.render());
	}

	public static Result namesList () {
		ChemicalSubstance c = new ChemicalSubstance();
		DynamicForm requestData = Form.form().bindFromRequest();
		String names = requestData.get("names");
		String [] chemNames = names.split("\\n");
		Logger.info("names: " + chemNames.length);
		for (String s : chemNames){
//			Name n = new Name(s);
//			c.names.add(n);
		}
//		c.save();
		return ok (ix.ginas.views.html.wizard.render("Chemical"));
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

	static Facet[] filter (List<Facet> facets, String... names) {
		if (names == null || names.length == 0)
			return facets.toArray(new Facet[0]);

		List<Facet> filtered = new ArrayList<Facet>();
		for (String n : names) {
			for (Facet f : facets)
				if (n.equals(f.getName()))
					filtered.add(f);
		}
		for (Facet f : filtered)
			// treat year special...
			if (f.getName().equals(YEAR_FACET))
				f.sortLabels(true);

		return filtered.toArray(new Facet[0]);
	}

	static List<Facet> getFacets (final Class kind, final int fdim) {
		try {
			SearchResult result =
					SearchFactory.search(kind, null, 0, 0, fdim, null);
			return result.getFacets();
		}
		catch (IOException ex) {
			Logger.trace("Can't retrieve facets for "+kind, ex);
		}
		return new ArrayList<Facet>();
	}

	static SearchResult getSearchResult
	(final Class kind, final String q, final int total) {

		final Map<String, String[]> query =  new HashMap<String, String[]>();
		query.putAll(request().queryString());

		List<String> qfacets = new ArrayList<String>();
		final boolean hasMesh = q != null && q.indexOf('/') > 0;        
		if (hasMesh) {
			// treat this as facet
			if (query.get("facet") != null) {
				for (String f : query.get("facet"))
					qfacets.add(f);
			}
			qfacets.add("MeSH/"+q);
			query.put("facet", qfacets.toArray(new String[0]));
		}

		query.put("drill", new String[]{"down"});

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
			SearchResult result = Cache.getOrElse
					(sha1, new Callable<SearchResult>() {
						public SearchResult call ()
								throws Exception {
							return SearchFactory.search
									(kind, hasMesh ? null : q,  total, 0, 20, query);
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

	public static Integer getSubstanceCount () {
		return SubstanceFactory.getCount();
	}

	public static Result substance (long id) {
				Logger.info("looking for " + id);
				try {
					Substance s = SubstanceFactory.getSubstance(id);
					return ok (ix.ginas.views.html.details.render(s));
				}
				catch (Exception ex) {
					return internalServerError
							(ix.idg.views.html.error.render(500, "Internal server error"));
				}
			}

			public static Result chemicalsubstance (long id) {
				Logger.info("looking for " + id);
				try {
					Substance s = SubstanceFactory.getSubstance(id);
					ChemicalSubstance cs = (ChemicalSubstance)s;
					ObjectMapper mapper = new ObjectMapper();
					//			String pretty = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(cs);
					//			Logger.info(pretty);
					Logger.info("HHHHHHHHHHHHHH");
					return ok (ix.ginas.views.html.chemicaldetails.render(cs));
				}
				catch (Exception ex) {
					ex.printStackTrace();
					return internalServerError
							(ix.idg.views.html.error.render(500, "Internal server error"));
				}
			}

	public static Result results (final String q, int rows, final int page) {
		Logger.debug("Substances: q="+q+" rows="+rows+" page="+page);
		try {
			final int total = SubstanceFactory.finder.findRowCount();
			if (request().queryString().containsKey("facet") || q != null) {
				SearchResult result = getSearchResult (Substance.class, q, total);

				Facet[] facets = filter
						(result.getFacets(), PROJECT_FACETS);
				List<Substance> results = new ArrayList<Substance>();
				int[] pages = new int[0];
				if (result.count() > 0) {
					rows = Math.min(result.count(), Math.max(1, rows));
					pages = paging (rows, page, result.count());

					for (int i = (page-1)*rows, j = 0; j < rows
							&& i < result.count(); ++j, ++i) {
						results.add((Substance)result.getMatches().get(i));
					}
				}

				return ok (ix.ginas.views.html.results.render
						(null, page, rows, result.count(),
								pages, facets, results));
			}

			else {
				Facet[] facets = Cache.getOrElse
						(Substance.class.getName()+".facets",
								new Callable<Facet[]>() {
									public Facet[] call () {
										return filter (getFacets (Substance.class, 20),
												PROJECT_FACETS);
									}
								}, CACHE_TIMEOUT);

				rows = Math.min(total, Math.max(1, rows));
				int[] pages = paging (rows, page, total);               

				List<Substance> results =
						SubstanceFactory.filter(rows, (page-1)*rows, null);
				return ok (ix.ginas.views.html.results.render
						(null, page, rows, total, pages, facets, results));
			}
		}

		catch (Exception ex) {
			ex.printStackTrace();
			return badRequest (ix.idg.views.html.error.render
					(404, "Invalid page requested: "+page+ex));
		}
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

	public static Result search (String kind) {
		Logger.info("KIND=====================" + kind);
		try {
			String q = request().getQueryString("q");
			if (kind != null && !"".equals(kind)) {
				if (Substance.class.isAssignableFrom(Class.forName(kind)))
					return redirect (ix.ginas.controllers.routes.GinasMain.results(q, 10, 1));
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
			SearchResult result;
			final Map<String, String[]> queryString =
					new HashMap<String, String[]>();
					queryString.putAll(request().queryString());

					if (query.indexOf('/') > 0) { // use mesh facet
						result = Cache.getOrElse
								(sha1, new Callable<SearchResult>() {
									public SearchResult
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
								}, CACHE_TIMEOUT);
					}
					else {
						result = Cache.getOrElse
								(sha1, new Callable<SearchResult>() {
									public SearchResult
									call () throws Exception {
										return SearchFactory.search
												(null, query, 500, 0, 20, queryString);
									}
								}, CACHE_TIMEOUT);
					}

					Facet[] facets = filter
							(result.getFacets(), PROJECT_FACETS);
					int max = Math.min(rows, Math.max(1,result.count()));
					int [] pages = paging (rows, max, result.count());
					int totalSubstances = 0;
					for (Facet f : result.getFacets()) {
						if (f.getName().equals("ix.Class")) {
							for (FV fv : f.getValues()) {
								if (Substance.class.getName().equals(fv.getLabel()))
									totalSubstances = fv.getCount();
							}
						}
					}

					List<Substance> substances =
							filter (Substance.class, result.getMatches(), max);

					return ok (ix.ginas.views.html.results.render
							(null, 1, max, totalSubstances, pages, facets,
									substances ));
		}

		catch (Exception ex) {
			Logger.trace("Can't execute search \""+query+"\"", ex);
		}

		return internalServerError (ix.idg.views.html.error.render
				(500, "Unable to fullfil request"));
	}

	public static Result migration(){

		return ok (ix.ginas.views.html.migration.render
				("ginas Migration"));
	}

	public static Result migrate(){
		Http.MultipartFormData body = request().body().asMultipartFormData();
		Http.MultipartFormData.FilePart part = body.getFile("file");
		if (part != null) {
			String name = part.getFilename();
			String content = part.getContentType();
			Logger.debug("file="+name+" content="+content);
			File file = part.getFile();
			Logger.info("trying something");   

			ObjectMapper mapper = new ObjectMapper();
			try {
			        ChemicalSubstance cs = mapper.readValue(file, ChemicalSubstance.class);
				Logger.info("erasing set uuid");	
				cs.save();
				Logger.info("ok");
			} catch (JsonParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (JsonMappingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return ok (ix.ginas.views.html.index.render());

		}
	URL u;
	DynamicForm requestData = Form.form().bindFromRequest();
	String url;
	String ginasUrl = requestData.get("ginasUrl");
	if(ginasUrl==null){
		Logger.info("numbers!");
		int count = Integer.parseInt(requestData.get("record-number"));
		int skip = Integer.parseInt(requestData.get("skip-number"));
		url = "https://tripod.nih.gov/ginas/v8/?max="+count+"&skip="+skip;
		Logger.info(url);
	}else{
		url = ginasUrl;
	}
	try {
		u = new URL(url);
		getRecord(u);
	} catch (MalformedURLException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}

return ok (ix.ginas.views.html.index.render());
}

public static Result getRecord(URL url){
	try {
		Logger.info("parsing");
		ObjectMapper mapper = new ObjectMapper();
		JsonNode json = mapper.readTree(url);		
		for(int i = 0; i< json.size(); i++){
			Logger.info("parsing number "+ i);
			String check = json.get(i).get("uuid").asText();
			Logger.info(check +" uuid");
			ChemicalSubstance cs;
//			ChemicalSubstance cs = SubstanceFactory.registerIfAbsent(check);
//			if(cs == null){
				cs =  mapper.treeToValue(json.get(i), ChemicalSubstance.class);
					cs.save();
				Logger.info("saved");
//			}else{
//				Logger.info(cs.uuid + "uuid needs to be updated");
//			}
		}
		
	}
	catch (Exception ex) {
		ex.printStackTrace();
		Logger.trace("Can't load file " + ex);
	}
	return ok (ix.ginas.views.html.index.render());

}

}
