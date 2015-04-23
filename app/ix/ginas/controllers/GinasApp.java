package ix.ginas.controllers;

import ix.core.controllers.search.SearchFactory;
import ix.core.models.Keyword;
import ix.core.models.Structure;
import ix.core.models.Value;
import ix.core.plugins.TextIndexerPlugin;
import ix.core.search.TextIndexer;
import ix.core.search.TextIndexer.Facet;
import ix.ginas.controllers.v1.SubstanceFactory;
import ix.ginas.models.Ginas;
import ix.ginas.models.v1.ChemicalSubstance;
import ix.ginas.models.v1.Moiety;
import ix.ginas.models.v1.Substance;
import ix.idg.controllers.LigandFactory;
import ix.idg.models.Disease;
import ix.idg.models.Ligand;
import ix.ncats.controllers.App;
import ix.ncats.controllers.App.FacetDecorator;
import ix.utils.Util;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import play.Logger;
import play.Play;
import play.data.DynamicForm;
import play.data.Form;
import play.db.ebean.Model;
import play.mvc.Http;
import play.mvc.Result;
import tripod.chem.indexer.StructureIndexer;
import tripod.chem.indexer.StructureIndexer.ResultEnumeration;

import com.avaje.ebean.Expr;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.DeserializationProblemHandler;


public class GinasApp extends App {
	static final TextIndexer INDEXER = 
			play.Play.application().plugin(TextIndexerPlugin.class).getIndexer();

	public static final String[] CHEMICAL_FACETS = {
		"Status",
		"Substance Class",
		"StereoChemistry"
	};

	public static Result error (int code, String mesg) {
		return ok (ix.idg.views.html.error.render(code, mesg));
	}

	public static Result _notFound (String mesg) {
		return notFound (ix.idg.views.html.error.render(404, mesg));
	}

	public static Result _badRequest (String mesg) {
		return badRequest (ix.idg.views.html.error.render(400, mesg));
	}

	public static Result _internalServerError (Throwable t) {
		t.printStackTrace();
		return internalServerError
				(ix.idg.views.html.error.render
						(500, "Internal server error: "+t.getMessage()));
	}

	static FacetDecorator[] decorate (Facet... facets) {
		List<FacetDecorator> decors = new ArrayList<FacetDecorator>();
		// override decorator as needed here
		for (int i = 0; i < facets.length; ++i) {
			decors.add(new GinasFacetDecorator (facets[i]));
		}
		// now add hidden facet so as to not have them shown in the alert
		// box
		//        for (int i = 1; i <= 8; ++i) {
		//            GinasFacetDecorator f = new GinasFacetDecorator
		//                (new TextIndexer.Facet
		//                 (ChemblRegistry.ChEMBL_PROTEIN_CLASS+" ("+i+")"));
		//            f.hidden = true;
		//            decors.add(f);
		//        }

		GinasFacetDecorator f = new GinasFacetDecorator
				(new TextIndexer.Facet("ChemicalSubstance"));
		f.hidden = true;
		decors.add(f);

		return decors.toArray(new FacetDecorator[0]);
	}

	static class GinasFacetDecorator extends FacetDecorator {
		GinasFacetDecorator (Facet facet) {
			super (facet, true, 6);
		}

		@Override
		public String name () {
			return super.name().trim();
		}

		@Override
		public String label (final int i) {
			final String label = super.label(i);
			final String name = super.name();

			return label;
		}
	}


	static class GinasV1ProblemHandler
	extends  DeserializationProblemHandler {
		GinasV1ProblemHandler () {
		}

		public boolean handleUnknownProperty
		(DeserializationContext ctx, JsonParser parser,
				JsonDeserializer deser, Object bean, String property) {

			try {
				boolean parsed = true;
				if ("hash".equals(property)) {
					Structure struc = (Structure)bean;
					//Logger.debug("value: "+parser.getText());
					struc.properties.add(new Keyword
							(Structure.H_LyChI_L4,
									parser.getText()));
				}
				else if ("references".equals(property)) {
					if (bean instanceof Structure) {
						Structure struc = (Structure)bean;
						if (parser.getCurrentToken() == JsonToken.START_ARRAY) {
							while (JsonToken.END_ARRAY != parser.nextToken()) {
								String ref = parser.getValueAsString();
								struc.properties.add
								(new Keyword (Ginas.REFERENCE, ref));
							}
						}
						else {
							return false;
						}
					}
					else {
						parsed = false;
					}
				}
				else if ("count".equals(property)) {
					if (bean instanceof Structure) {
						// need to handle this.
						parser.skipChildren();
					}
				}
				else {
					parsed = false;
				}

				if (!parsed) {
					Logger.warn("Unknown property \""
							+property+"\" while parsing "
							+bean+"; skipping it..");
					Logger.debug("Token: "+parser.getCurrentToken());
					parser.skipChildren();                  
				}
			}
			catch (Exception ex) {
				ex.printStackTrace();
			}
			return true;
		}
	}

	public static <T extends Substance> T parseJSON
	(InputStream is, Class<T> cls) throws IOException {
		ObjectMapper mapper = new ObjectMapper ();
		mapper.addHandler(new GinasV1ProblemHandler ());
		JsonNode tree = mapper.readTree(is);
		JsonNode subclass = tree.get("substanceClass");
		if (subclass != null && !subclass.isNull()) {
			Substance.SubstanceClass type =
					Substance.SubstanceClass.valueOf(subclass.asText());
			switch (type) {
			case chemical:
				if (cls.isAssignableFrom(ChemicalSubstance.class)) {
					return mapper.treeToValue(tree, cls);
				}
				else {
					Logger.warn(tree.get("uuid").asText()+" is not of type "
							+cls.getName());
				}
				break;
			default:
				Logger.warn("Skipping substance class "+type);
			}
		}
		else {
			Logger.error("Not a valid JSON substance!");
		}
		return null;
	}

	public static Result load () {
		return ok (ix.ginas.views.html.load.render());
	}

	public static Result loadJSON () {
		DynamicForm requestData = Form.form().bindFromRequest();
		String type = requestData.get("substance-type");
		Logger.debug("substance-type: "+type);

		Substance sub = null;
		try {
			InputStream is = null;
			String url = requestData.get("json-url");
			Logger.debug("json-url: "+url);
			if (url != null && url.length() > 0) {
				URL u = new URL (url);
				is = u.openStream();
			}
			else {
				// now try json-file
				Http.MultipartFormData body =
						request().body().asMultipartFormData();
				Http.MultipartFormData.FilePart part =
						body.getFile("json-file");
				if (part != null) {
					File file = part.getFile();
					Logger.debug("json-file: "+file);
					is = new FileInputStream (file);
				}
				else {
					part = body.getFile("json-dump");
					if (part != null) {
						File file = part.getFile();
						try {
							// see if it's a zip file
							ZipFile zip = new ZipFile (file);
							for (Enumeration<? extends ZipEntry> en = zip.entries();
									en.hasMoreElements(); ) {
								ZipEntry ze = en.nextElement();
								Logger.debug("processing "+ze.getName());
								is = zip.getInputStream(ze);
								break;
							}
						}
						catch (Exception ex) {
							Logger.warn("Not a zip file \""+file+"\"!");
							// try as plain txt file
							is = new FileInputStream (file);
						}
						return processDump (is);
					}
					else {
						return badRequest
								("Neither json-url nor json-file nor json-dump "
										+"parameter is specified!");
					}
				}
			}

			if (type.equalsIgnoreCase("chemical")) {
				ChemicalSubstance chem =
						parseJSON(is, ChemicalSubstance.class);
				sub = persist (chem);
			}
			else if (type.equalsIgnoreCase("protein")) {
			}
			else if (type.equalsIgnoreCase("nucleic acid")) {
			}
			else if (type.equalsIgnoreCase("polymer")) {
			}
			else if (type.equalsIgnoreCase("Structurally Diverse")) {
			}
			else if (type.equalsIgnoreCase("mixture")) {
			}
			else {
				return badRequest ("Unknown substance type: "+type);
			}
		}
		catch (Exception ex) {
			ex.printStackTrace();
			return internalServerError
					("Can't parse json: "+ex.getMessage());
		}

		ObjectMapper mapper = new ObjectMapper ();
		return ok (mapper.valueToTree(sub));
	}

	public static Result processDump (InputStream is) throws Exception {
		BufferedReader br = new BufferedReader (new InputStreamReader (is));
		int count = 0;
		for (String line; (line = br.readLine()) != null; ) {
			String[] toks = line.split("\t");
			Logger.debug("processing "+toks[0]+" "+toks[1]+"...");
			ByteArrayInputStream bis = new ByteArrayInputStream
					(toks[2].getBytes("utf8"));
			ChemicalSubstance chem = parseJSON (bis, ChemicalSubstance.class);
			if (chem != null) {
				try {
					persist (chem);
					++count;
				}
				catch (Exception ex) {
					Logger.error("Can't persist record "+toks[1], ex);
				}
			}
		}
		br.close();
		return ok (count+" record(s) processed!");
	}

	// there is some thing in how jackson's object creation
	// doesn't get registered with ebean for it to realize that
	// the bean's state has changed.
	static Substance persist (ChemicalSubstance chem) throws Exception {
		chem.structure.save();
		strucIndexer.add(chem.structure.id.toString(), chem.structure.molfile);
		for (Moiety m : chem.moieties)
			m.structure.save();
		chem.save();
		return chem;
	}

	public static Result search (String kind) {
		try {
			String q = request().getQueryString("q");
			String t = request().getQueryString("type");
			if (kind != null && !"".equals(kind)) {
				Logger.info(ChemicalSubstance.class.getName());
				if (ChemicalSubstance.class.getName().equals(kind)){
					return redirect (routes.GinasApp.chemicals(q, 32, 1));
				}
				else if ("substructure".equalsIgnoreCase(t)) {
					String url = routes.GinasApp.chemicals(q, 8, 1).url()
							+"&type="+t;
					return redirect (url);
				}
				else if ("similarity".equalsIgnoreCase(t)) {
					String cutoff = request().getQueryString("cutoff");
					if (cutoff == null) {
						cutoff = "0.8";
					}
					String url = routes.GinasApp.chemicals(q, 8, 1).url()
							+"&type="+t+"&cutoff="+cutoff;
					return redirect (url);
				}
			}
			// generic entity search..
			return search (5);
		}
		catch (Exception ex) {
			Logger.debug("Can't resolve class: "+kind, ex);
		}

		return _badRequest ("Invalid request: "+request().uri());
	}

	public static Result search (final int rows) {
		try {

			final String key = "search/"+Util.sha1(request ());
			return getOrElse(key, new Callable<Result> () {
				public Result call () throws Exception {
					Logger.debug("Cache missed: "+key);
					return _search (rows);
				}
			});
		}
		catch (Exception ex) {
			return _internalServerError (ex);
		}
	}

	static Result _search (int rows) throws Exception {
		Logger.info("inside private search");
		final String query = request().getQueryString("q");
		Logger.debug("Query: \""+query+"\"");

		TextIndexer.SearchResult result = null;            
		if (query.indexOf('/') > 0) { // use mesh facet
			final Map<String, String[]> queryString =
					new HashMap<String, String[]>();
					queryString.putAll(request().queryString());

					// append this facet to the list 
					List<String> f = new ArrayList<String>();
					f.add("MeSH/"+query);
					String[] ff = queryString.get("facet");
					if (ff != null) {
						for (String fv : ff)
							f.add(fv);
					}
					queryString.put("facet", f.toArray(new String[0]));
					long start = System.currentTimeMillis();
					final String key =
							"search/facet/"+Util.sha1(queryString.get("facet")); 
					result = getOrElse
							(key, new Callable<TextIndexer.SearchResult>() {
								public TextIndexer.SearchResult
								call ()  throws Exception {
									Logger.debug("Cache missed: "+key);
									return SearchFactory.search
											(null, null, MAX_SEARCH_RESULTS,
													0, FACET_DIM, queryString);
								}
							});
					double ellapsed = (System.currentTimeMillis()-start)*1e-3;
					Logger.debug
					("1. Ellapsed time "+String.format("%1$.3fs", ellapsed));
		}

		if (result == null || result.count() == 0) {
			long start = System.currentTimeMillis();
			final String key =
					"search/facet/q/"+Util.sha1(request(), "facet", "q");
			result = getOrElse
					(key, new Callable<TextIndexer.SearchResult>() {
						public TextIndexer.SearchResult
						call () throws Exception {
							Logger.debug("Cache missed: "+key);
							return SearchFactory.search
									(null, quote (query), MAX_SEARCH_RESULTS, 0,
											FACET_DIM, request().queryString());
						}
					});
			double ellapsed = (System.currentTimeMillis()-start)*1e-3;
			Logger.debug
			("2. Ellapsed time "+String.format("%1$.3fs", ellapsed));
		}

		TextIndexer.Facet[] facets = filter
				(result.getFacets(), CHEMICAL_FACETS);

		int max = Math.min(rows, Math.max(1,result.count()));
		int total = 0, totalChemicalSubstances = 0, totalDiseases = 0, totalLigands = 0;
		for (TextIndexer.Facet f : result.getFacets()) {
			if (f.getName().equals("ix.Class")) {
				for (TextIndexer.FV fv : f.getValues()) {
					if (ChemicalSubstance.class.getName().equals(fv.getLabel())) {
						totalChemicalSubstances = fv.getCount();
						total += totalChemicalSubstances;
					}
					else if (Disease.class.getName()
							.equals(fv.getLabel())) {
						totalDiseases = fv.getCount();  
						total += totalDiseases;
					}
					else if (Ligand.class.getName().equals(fv.getLabel())) {
						totalLigands = fv.getCount();
						total += totalLigands;
					}
				}
			}
		}

		List<ChemicalSubstance> chemicalSubstances =
				filter (ChemicalSubstance.class, result.getMatches(), max);


		return ok (ix.ginas.views.html.search.render
				(query, total, decorate (facets),
						chemicalSubstances, totalChemicalSubstances,
						null, totalLigands,
						null, totalDiseases));
	}

	static <T> List<T> filter (Class<T> cls, List values, int max) {
		List<T> fv = new ArrayList<T>();
		for (Object v : values) {
			if (cls.isAssignableFrom(v.getClass())) {
				fv.add((T)v);
				if (fv.size() >= max)
					break;
			}
		}
		return fv;
	}    

	public static Result authenticate () {
		return ok ("You're authenticated!");
	}

	public static Result chemicals (final String q,
			final int rows, final int page) {
		try {
			final String key = "chemicals/"+Util.sha1(request ());
			return getOrElse(key, new Callable<Result>() {
				public Result call () throws Exception {
					Logger.debug("Cache missed: "+key);
					return _chemicals (q, rows, page);
				}
			});
		}
		catch (Exception ex) {
			return _internalServerError (ex);
		}
	}

	static Result _chemicals (String q, int rows, int page) throws Exception {
		String type = request().getQueryString("type");
		Logger.debug("Chemicals: rows=" + rows + " page=" + page);
		if (type != null && (type.equalsIgnoreCase("substructure")
				|| type.equalsIgnoreCase("similarity"))) {
			// structure search
			String cutoff = request().getQueryString("cutoff");
			Logger.debug("Search: q="+q+" type="+type+" cutoff="+cutoff);
			try {
				if (type.equalsIgnoreCase("substructure")) {
					return substructure (q, rows, page);
				}
				else {
					return similarity
							(q, Double.parseDouble(cutoff), rows, page);
				}
			}
			catch (Exception ex) {
				ex.printStackTrace();
			}

			return notFound (ix.idg.views.html.error.render
					(400, "Invalid search parameters: type=\""+type
							+"\"; q=\""+q+"\" cutoff=\""+cutoff+"\"!"));
		}


		final int total = SubstanceFactory.finder.findRowCount();
		if (request().queryString().containsKey("facet") || q != null) {
			TextIndexer.SearchResult result =
					getSearchResult (ChemicalSubstance.class, q, total);

			TextIndexer.Facet[] facets = filter
					(result.getFacets(), CHEMICAL_FACETS);

			List<ChemicalSubstance> chemicals = new ArrayList<ChemicalSubstance>();
			int[] pages = new int[0];
			if (result.count() > 0) {
				rows = Math.min(result.count(), Math.max(1, rows));
				pages = paging (rows, page, result.count());
				for (int i = (page - 1) * rows, j = 0; j < rows
						&& i < result.count(); ++j, ++i) {
					chemicals.add((ChemicalSubstance) result.getMatches().get(i));
				}
			}

			return ok(ix.ginas.views.html.chemicals.render
					(page, rows, result.count(),
							pages, decorate (facets), chemicals));
		}
		else {
			final String key = ChemicalSubstance.class.getName()+".facets";
			TextIndexer.Facet[] facets = getOrElse
					(key, new Callable<TextIndexer.Facet[]>() {
						public TextIndexer.Facet[] call() {
							Logger.debug("Cache missed: "+key);
							return filter(getFacets(ChemicalSubstance.class, 30),
									CHEMICAL_FACETS);
						}
					});
			rows = Math.min(total, Math.max(1, rows));
			int[] pages = paging(rows, page, total);

			List<ChemicalSubstance> chemicals =
					SubstanceFactory.getChemicals(rows, (page - 1) * rows, null);

			return ok(ix.ginas.views.html.chemicals.render
					(page, rows, total, pages, decorate (facets), chemicals));
		}
	}

	public static Result similarity (final String query,
			final double threshold,
			int rows, int page) {
		try {
			final String key = "similarity/"+Util.sha1(query)
					+"/"+String.format("%1$d", (int)(1000*threshold+.5));
			TextIndexer indexer = getOrElse
					(strucIndexer.lastModified(),
							key, new Callable<TextIndexer> () {
						public TextIndexer call () throws Exception {
							Logger.debug("Cache missed: "+key);
							ResultEnumeration results =
									strucIndexer.similarity
									(query, threshold,
											Play.application().configuration()
											.getInt("ix.structure.max", 100));
							return createIndexer (results);
						}
					});

			return structureResult (indexer, rows, page);
		}
		catch (Exception ex) {
			ex.printStackTrace();
			Logger.error("Can't execute similarity search", ex);
		}

		return internalServerError
				(ix.idg.views.html.error.render
						(500, "Unable to perform similarity search: "+query));
	}

	public static Result substructure
	(final String query, int rows, int page) {
		try {
			final String key = "substructure/"+Util.sha1(query);
			Logger.debug("substructure: query="+query
					+" rows="+rows+" page="+page+" key="+key);
			TextIndexer indexer = getOrElse
					(strucIndexer.lastModified(),
							key, new Callable<TextIndexer> () {
						public TextIndexer call () throws Exception {
							Logger.debug("Cache missed: "+key);
							ResultEnumeration results =
									strucIndexer.substructure
									(query, Play.application().configuration()
											.getInt("ix.structure.max", 100));
							return createIndexer (results);
						}
					});

			return structureResult (indexer, rows, page);
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}

		return internalServerError
				(ix.idg.views.html.error.render
						(500, "Unable to perform substructure search: "+query));
	}

	public static Result structureResult
    (TextIndexer indexer, int rows, int page) throws Exception {
    try {
        TextIndexer.SearchResult result = SearchFactory.search
            (indexer, Substance.class, null, indexer.size(), 0, FACET_DIM,
             request().queryString());
        
        TextIndexer.Facet[] facets =
            filter (result.getFacets(), CHEMICAL_FACETS);
        List<ChemicalSubstance> substances = new ArrayList<ChemicalSubstance>();
        int[] pages = new int[0];
        if (result.count() > 0) {
            rows = Math.min(result.count(), Math.max(1, rows));
            pages = paging (rows, page, result.count());
            
            for (int i = (page-1)*rows, j = 0; j < rows
                     && i < result.count(); ++j, ++i) {
                substances.add((ChemicalSubstance)result.getMatches().get(i));
            }
        }
        
        return ok (ix.ginas.views.html.chemicals.render
                   (page, rows, result.count(),
                    pages, decorate (facets), substances));
    }
    finally {
        //indexer.shutdown();
    }
}

    static TextIndexer createIndexer (ResultEnumeration results)
            throws Exception {
            long start = System.currentTimeMillis();        
            TextIndexer indexer = textIndexer.createEmptyInstance();
            int count = 0;
            Set<String> unique = new HashSet<String>();
            while (results.hasMoreElements()) {
                StructureIndexer.Result r = results.nextElement();

                Logger.debug(r.getId()+" "+r.getSource()+" "
                             +r.getMol().toFormat("smiles"));

                List<Substance> chemicals = SubstanceFactory.finder
                    .where().eq("structure.id", r.getId()).findList();
                for (Substance chemical : chemicals) {
                    if (!unique.contains(chemical.uuid.toString())) {
                        indexer.add(chemical);
                        unique.add(chemical.uuid.toString());
                    }
                }
                ++count;
            }
            
            double ellapsed = (System.currentTimeMillis() - start)*1e-3;
            Logger.debug(String.format("Ellapsed %1$.3fs to retrieve "
                                       +"%2$d structures...",
                                       ellapsed, count));
            return indexer;
        }

	static final GetResult<ChemicalSubstance> ChemicalResult =
			new GetResult<ChemicalSubstance>(ChemicalSubstance.class, SubstanceFactory.chemfinder) {
		public Result getResult (List<ChemicalSubstance> chemicals) throws Exception {
			return _getChemicalResult (chemicals);
		}
	};

	static Result _getChemicalResult (List<ChemicalSubstance> chemicals) throws Exception {
		// force it to show only one since it's possible that the provided
		// name isn't unique
		if (true || chemicals.size() == 1) {
			ChemicalSubstance chemical = chemicals.iterator().next();
			return ok (ix.ginas.views.html
					.chemicaldetails.render(chemical));
		}
		else {
			TextIndexer indexer = textIndexer.createEmptyInstance();
			for (ChemicalSubstance chem : chemicals)
				indexer.add(chem);

			TextIndexer.SearchResult result = SearchFactory.search
					(indexer, ChemicalSubstance.class, null, indexer.size(), 0, FACET_DIM,
							request().queryString());
			if (result.count() < chemicals.size()) {
				chemicals.clear();
				for (int i = 0; i < result.count(); ++i) {
					chemicals.add((ChemicalSubstance)result.getMatches().get(i));
				}
			}
			TextIndexer.Facet[] facets = filter
					(result.getFacets(), CHEMICAL_FACETS);
			indexer.shutdown();

			return ok (ix.ginas.views.html.chemicals.render
					(1, result.count(), result.count(),
							new int[0], decorate (facets), chemicals));
		}
	}

	public static Result chemical (String name) {
		return ChemicalResult.get(name);
	}

	/**
	 * return the canonical/default chemical id
	 */
	public static String getId (ChemicalSubstance chemical) {
		return chemical.getName();
	}

	static abstract class GetResult<T> {
		final Model.Finder<Long, T> finder;
		final Class<T> cls;
		GetResult (Class<T> cls, Model.Finder<Long, T> finder) {
			this.cls = cls;
			this.finder = finder;
		}

		public Result get (final String name) {
			try {
				long start = System.currentTimeMillis();
				final String key = cls.getName()+"/"+name;
				List<T> e = getOrElse
						(key, new Callable<List<T>> () {
							public List<T> call () throws Exception {
								Logger.debug("Cache missed: "+key);
								List<T> values = finder.where()
										.eq("names.name", name).findList();
								if (values.size() > 1) {
									Logger.warn("\""+name+"\" yields "
											+values.size()+" matches!");
								}
								return values;
							}
						});
				double ellapsed = (System.currentTimeMillis()-start)*1e-3;
				Logger.debug("Ellapsed time "+String.format("%1$.3fs", ellapsed)
						+" to retrieve "+e.size()+" matches for "+name);

				if (e.isEmpty()) {
					return _notFound ("Unknown name: "+name);
				}
				return result (e);
			}
			catch (Exception ex) {
				Logger.error("Unable to generate Result for \""+name+"\"", ex);
				return _internalServerError (ex);
			}
		}

		public Result result (final List<T> e) {
			try {
				final String key = cls.getName()+"/result/"+Util.sha1(request ());
				return getOrElse(key, new Callable<Result> () {
					public Result call () throws Exception {
						long start = System.currentTimeMillis();
						Result r = getResult (e);
						Logger.debug("Cache missed: "+key+"..."
								+(System.currentTimeMillis()-start)
								+"ms");
						return r;
					}
				}
						);
			}
			catch (Exception ex) {
				return _internalServerError (ex);
			}
		}

		abstract Result getResult (List<T> e) throws Exception;
	}

	public static List<Keyword> getStructureReferences(Structure s){
		List<Keyword> references = new ArrayList<Keyword>();
		for(Value v : s.properties){
			Logger.info(v.label);
			if(v.label.equals("GInAS Reference")){
				Keyword k = new Keyword(v.getValue().toString());
				Logger.info(k.term);
				references.add(k);
			}
		}

		return references;
	}
}
