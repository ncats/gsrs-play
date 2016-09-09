package ix.ginas.controllers;

import java.io.IOException;
import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.Collectors;

import ix.utils.UUIDUtil;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import be.objectify.deadbolt.java.actions.Dynamic;
import chemaxon.struc.MolAtom;
import gov.nih.ncgc.chemical.Chemical;
import gov.nih.ncgc.chemical.ChemicalFactory;
import ix.core.GinasProcessingMessage;
import ix.core.UserFetcher;
import ix.core.adapters.EntityPersistAdapter;
import ix.core.chem.ChemCleaner;
import ix.core.chem.PolymerDecode;
import ix.core.chem.PolymerDecode.StructuralUnit;
import ix.core.chem.StructureProcessor;
import ix.core.controllers.AdminFactory;
import ix.core.controllers.EntityFactory;
import ix.core.controllers.StructureFactory;
import ix.core.controllers.search.SearchFactory;
import ix.core.models.Keyword;
import ix.core.models.Payload;
import ix.core.models.Principal;
import ix.core.models.Structure;
import ix.core.models.UserProfile;
import ix.core.plugins.IxCache;
import ix.core.plugins.PayloadPlugin;
import ix.core.plugins.TextIndexerPlugin;
import ix.core.search.EntityFetcher;
import ix.core.search.SearchOptions;
import ix.core.search.SearchOptions.TermFilter;
import ix.core.search.SearchResult;
import ix.core.search.SearchResultContext;
import ix.core.search.SearchResultProcessor;
import ix.core.search.text.TextIndexer;
import ix.core.search.text.TextIndexer.FV;
import ix.core.search.text.TextIndexer.Facet;
import ix.core.util.EntityUtils;
import ix.core.util.EntityUtils.EntityInfo;
import ix.core.util.EntityUtils.EntityWrapper;
import ix.core.util.EntityUtils.Key;
import ix.core.util.Java8Util;
import ix.ginas.controllers.plugins.GinasSubstanceExporterFactoryPlugin;
import ix.ginas.controllers.v1.CV;
import ix.ginas.controllers.v1.ControlledVocabularyFactory;
import ix.ginas.controllers.v1.SubstanceFactory;
import ix.ginas.exporters.Exporter;
import ix.ginas.exporters.SubstanceExporterFactory;
import ix.ginas.models.v1.Amount;
import ix.ginas.models.v1.ChemicalSubstance;
import ix.ginas.models.v1.Code;
import ix.ginas.models.v1.Component;
import ix.ginas.models.v1.ControlledVocabulary;
import ix.ginas.models.v1.DisulfideLink;
import ix.ginas.models.v1.GinasChemicalStructure;
import ix.ginas.models.v1.Glycosylation;
import ix.ginas.models.v1.Linkage;
import ix.ginas.models.v1.MixtureSubstance;
import ix.ginas.models.v1.Modifications;
import ix.ginas.models.v1.Moiety;
import ix.ginas.models.v1.Name;
import ix.ginas.models.v1.NucleicAcid;
import ix.ginas.models.v1.NucleicAcidSubstance;
import ix.ginas.models.v1.PolymerSubstance;
import ix.ginas.models.v1.Protein;
import ix.ginas.models.v1.ProteinSubstance;
import ix.ginas.models.v1.Relationship;
import ix.ginas.models.v1.Site;
import ix.ginas.models.v1.SpecifiedSubstanceGroup1Substance;
import ix.ginas.models.v1.StructuralModification;
import ix.ginas.models.v1.StructurallyDiverseSubstance;
import ix.ginas.models.v1.Substance;
import ix.ginas.models.v1.Subunit;
import ix.ginas.models.v1.Sugar;
import ix.ginas.models.v1.Unit;
import ix.ginas.models.v1.VocabularyTerm;
import ix.ginas.utils.reindex.MultiReIndexListener;
import ix.ginas.utils.reindex.ReIndexListener;
import ix.ginas.utils.reindex.ReIndexService;
import ix.ncats.controllers.App;
import ix.ncats.controllers.DefaultResultRenderer;
import ix.ncats.controllers.FacetDecorator;
import ix.ncats.controllers.crud.Administration;
import ix.ncats.controllers.security.IxDynamicResourceHandler;
import ix.seqaln.SequenceIndexer;
import ix.seqaln.SequenceIndexer.CutoffType;
import ix.utils.Util;
import play.Logger;
import play.Play;
import play.data.DynamicForm;
import play.data.Form;
import play.db.ebean.Model;
import play.db.ebean.Model.Finder;
import play.mvc.BodyParser;
import play.mvc.Call;
import play.mvc.Result;
import play.twirl.api.Html;
import tripod.chem.indexer.StructureIndexer;

public class GinasApp extends App {
	
    /**
     * Search types used for UI searches. At this time 
     * these types do not extend to API searches.
     * 
     * @author tyler
     *
     */
    public static enum SearchType{
    	SUBSTRUCTURE,
    	SIMILARITY,
    	EXACT,
    	FLEX,
    	SEQUENCE,
    	TEXT;
    	public boolean isStructureSearch(){
    		switch(this){
    			case SUBSTRUCTURE:
    			case SIMILARITY:
    			case EXACT:
    			case FLEX:
    				return true;
    			default:
    				return false;
    		}
    	}
    	public boolean isSequenceSearch(){
    		return this == SEQUENCE;
    	}
    	public static SearchType valueFor(String type){
    		if(type==null)return TEXT;
    		if(type.toLowerCase().startsWith("sub")){
    			return SUBSTRUCTURE;
    		}else if(type.toLowerCase().startsWith("sim")){
    			return SIMILARITY;
    		}else if(type.toLowerCase().startsWith("exa")){
    			return EXACT;
    		}else if(type.toLowerCase().startsWith("seq")){
    			return SEQUENCE;
    		}else if(type.toLowerCase().startsWith("fle")){
    			return FLEX;
    		}else if(type.toLowerCase().startsWith("text")){
    			return TEXT;
    		}
    		return TEXT;
    	}
    }
    
    
    public static final String[] ALL_FACETS = {
        "Record Status",
        "Substance Class", 
        "SubstanceStereochemistry", 
        "Molecular Weight",
        "GInAS Tag", 
        //"CAS",
        "Relationships",
        "Sequence Type",
        "Modifications",
        "Material Class", 
        "Material Type",
        "Family", 
        "Parts", 
        "Code System",
        "Last Edited By",
        "modified", 	   //"Last Edited Date"
        "Reference Type",
        "Approved By",
        "approved"         //"Approved Date"
    };

    static PayloadPlugin _payload ;

    static class SubstanceResultRenderer extends DefaultResultRenderer<Substance> {
        final String[] facets=ALL_FACETS;
        SubstanceResultRenderer () {}
        public Result render
            (SearchResultContext context, 
            		int page,
            		int rows, 
            		int total,
             int[] pages, 
             List<TextIndexer.Facet> facets,
             List<Substance> substances) {
            return ok(ix.ginas.views.html.substances.render
                      (page, rows, total, pages,
                       decorate(filter(facets, this.facets)),
                       substances, 
                       context));
        }
    }

    static{
        init();
    }


    public static void init(){
        _payload = Play.application().plugin(PayloadPlugin.class);
    }

    private static SubstanceReIndexListener listener = new SubstanceReIndexListener();

    
    @Dynamic(value = IxDynamicResourceHandler.IS_ADMIN, handler = ix.ncats.controllers.security.IxDeadboltHandler.class)
    public static Result listGinasUsers(int page, int rows, String sortBy, String order, String filter) {
        List<UserProfile> profiles = Administration.principalsList();
        return ok(ix.ginas.views.html.admin.userlist.render(profiles, sortBy, order, filter));
    }

    @Dynamic(value = IxDynamicResourceHandler.IS_ADMIN, handler = ix.ncats.controllers.security.IxDeadboltHandler.class)
    public static Result editPrincipal(Long id) {
        UserProfile up = Administration.editUser(id);
        return ok(ix.ginas.views.html.admin.edituser.render(
                id,
                up,
                up.getRoles(),
                AdminFactory.aclNamesByPrincipal(up.user),
                AdminFactory.groupNamesByPrincipal(up.user)
        ));
    }

    @Dynamic(value = IxDynamicResourceHandler.IS_ADMIN, handler = ix.ncats.controllers.security.IxDeadboltHandler.class)
    public static Result updatePrincipal(Long id) {
        Administration.updateUser(id);
        return redirect(ix.ginas.controllers.routes.GinasApp.listGinasUsers(1, 16, "", "", ""));
    }

    @Dynamic(value = IxDynamicResourceHandler.IS_ADMIN, handler = ix.ncats.controllers.security.IxDeadboltHandler.class)
    public static Result createPrincipal() {
        Form<UserProfile> userForm = Form.form(UserProfile.class);
        return ok(ix.ginas.views.html.admin.adduser.render(userForm));
    }

    @Dynamic(value = IxDynamicResourceHandler.IS_ADMIN, handler = ix.ncats.controllers.security.IxDeadboltHandler.class)
    public static Result addPrincipal() {
        DynamicForm requestData = Form.form().bindFromRequest();
        if (requestData.hasErrors()) {
            return ok();//badRequest(createForm.render(userForm));
        }

        Administration.addUser(requestData);
        return redirect(ix.ginas.controllers.routes.GinasApp.listGinasUsers(1, 16, "", "", ""));
    }



    public static Result error(int code, String mesg) {
        return ok(ix.ginas.views.html.error.render(code, mesg));
    }
    
    public static Result lastUnicorn(String name) {
        return notFound(ix.ginas.views.html.error.render
                        (404, "Unknown resource: " + request().uri()));
    }
    
    public static Result _notFound(String mesg) {
        return notFound(ix.ginas.views.html.error.render(404, mesg));
    }
    
    public static Result _badRequest(String mesg) {
        return badRequest(ix.ginas.views.html.error.render(400, mesg));
    }
    
    public static Result _internalServerError(Throwable t) {
        //t.printStackTrace();
        return internalServerError
            (ix.ginas.views.html.error.render
             (500, "Internal server error: " + t.getMessage()));
    }
    
    public static Result authenticate() {
        return ok("You're authenticated!");
    }
    
    public static CV getCV() {
        return new CV();
    }

    public static String translateFacetName(String n){
        if ("SubstanceStereoChemistry".equalsIgnoreCase(n))
            return "Stereochemistry";
        if ("modified".equalsIgnoreCase(n))
            return "Last Edited";
        if ("approved".equalsIgnoreCase(n))
            return "Last Validated";
        if("LyChI_L4".equalsIgnoreCase(n)){
            return "Structure Hash";
        }
        if("Approved By".equalsIgnoreCase(n)){
            return "Validated By";
        }
        if("Substance Class".equalsIgnoreCase(n)){
            return "Substance Type";
        }
        return n.trim();
    }
    
    static FacetDecorator[] decorate(Facet... facets) {
        return Arrays.stream(facets)
        			  .map(GinasFacetDecorator::new)
        			  .toArray(len-> new FacetDecorator[len]);
    }


    static class GinasFacetDecorator extends FacetDecorator {
        GinasFacetDecorator(Facet facet) {
            super(facet, true, 10);
            boolean isrange=true;
            //look for range filter to sort by value
            //uses regex now
            for(FV fv:facet.getValues()){
            	if(!fv.getLabel().matches("^[>]*[<]*[0-9][0-9]*[:]*[0-9]*$")){
            		isrange=false;
            		break;
            	}
            }
            if(isrange){
            	facet.sortLabels(false);
            }
        }
        @Override
        public String name() {
            return translateFacetName( super.name());
        }
        
        @Override
        public String label(final int i) {
            final String label = super.label(i);
            final String name = super.name();
            
            if ("StructurallyDiverse".equalsIgnoreCase(label))
                return "Structurally Diverse";
            if ("protein".equalsIgnoreCase(label))
                return "Protein";
            if ("nucleicAcid".equalsIgnoreCase(label))
                return "Nucleic Acid";
            if ("polymer".equalsIgnoreCase(label))
                return "Polymer";
            if ("mixture".equalsIgnoreCase(label))
                return "Mixture";
            if ("chemical".equalsIgnoreCase(label))
                return "Chemical";
            if ("concept".equalsIgnoreCase(label))
                return "Concept";
            if (label.contains("->")){
                return GinasApp.getCV().getDisplay("RELATIONSHIP_TYPE", label);
            }
            
            if ("EP".equalsIgnoreCase(label))
                return "PH. EUR";
            if(Substance.STATUS_APPROVED.equalsIgnoreCase(label))
                return "Validated (UNII)";
            if("non-approved".equalsIgnoreCase(label))
                return "Non-Validated";
            if (name.equalsIgnoreCase("approved")
                || name.equalsIgnoreCase("modified"))
                return label.substring(1); // skip the prefix character
            
            return label;
        }
    }
    
    // We can do better than this, I think.
    // I think there's a better way to display this information
    public static <T> String namesList(List<Name> list) {
        int size = list.size();
        if (size >= 6) {
            size = 6;
        }
        String[] arr = new String[size];
        for (int i = 0; i < size; i++) {
            Name n = list.get(i);
            String name = n.name;
            arr[i] = name;
        }
        return StringUtils.arrayToDelimitedString(arr, "; ");

    }

    public static <T> String codesList(List<Code> list) {
        int size = list.size();
        if (size >= 6) {
            size = 6;
        }
        String[] arr = new String[size];
        for (int i = 0; i < size; i++) {
            String name = list.get(i).code;
            arr[i] = name;
        }
        return StringUtils.arrayToDelimitedString(arr, "; ");
    }
    
    
    
	public static String getFirstOrElse(String[] s, String def){
		if(s==null || s.length==0)return def;
		return s[0];
	}
	
    @BodyParser.Of(value = BodyParser.FormUrlEncoded.class,
                   maxLength = 50_000)
    public static Result sequenceSearch () {

        if (request().body().isMaxSizeExceeded()) {
            return badRequest ("Sequence is too large!");
        }
        
        Map<String, String[]> params = request().body().asFormUrlEncoded();

        String[] values = params.get("sequence");
        String ident=getFirstOrElse(params.get("identity"),"0.5");
        String identType=getFirstOrElse(params.get("identityType"),"SUB");
        String wait=getFirstOrElse(params.get("wait"),null);
        
        if (values != null && values.length > 0) {
            String seq = values[0];
            try {
                Payload payload = _payload.createPayload
                    ("Sequence Search", "text/plain", seq);
                Call call = routes.GinasApp.substances
                (payload.id.toString(), 16, 1);
                return redirect (call.url()+"&type=sequence" + "&identity=" + ident + "&identityType=" + identType + ((wait!=null)?"&wait=" + wait:""));
            }
            catch (Exception ex) {
                ex.printStackTrace();
                return _internalServerError (ex);
            }
        }
        
        return badRequest ("Invalid \"sequence\" parameter specified!");
    }
    
    
    @BodyParser.Of(value = BodyParser.FormUrlEncoded.class,
            maxLength = 50_000)
	public static Result structureSearchPost() {

		if (request().body().isMaxSizeExceeded()) {
			return badRequest("Structure is too large!");
		}

		Map<String, String[]> params = request().body().asFormUrlEncoded();

		String[] values = params.get("q");
		String[] type = params.get("type");
		if (type == null || type.length == 0) {
			type = new String[] { "flex" };
		}
		String co = getFirstOrElse(params.get("cutoff"), "0.5");
		String wait = getFirstOrElse(params.get("wait"), null);

		if (values != null && values.length > 0) {
			Structure q = getStructureFrom(values[0]);

			try {
				Call call = routes.GinasApp.substances(q.id.toString(), 16, 1);
				return redirect(
						call.url() + "&type=" + type[0] + "&cutoff=" + co + ((wait != null) ? "&wait=" + wait : ""));
			} catch (Exception ex) {
				ex.printStackTrace();
				return _internalServerError(ex);
			}
		}

		return badRequest("Invalid \"q\" parameter specified!");
	}

    @Dynamic(value = IxDynamicResourceHandler.IS_ADMIN, handler = ix.ncats.controllers.security.IxDeadboltHandler.class)
    public static Result admin () {
        return ok (ix.ginas.views.html.admin.admin.render());
    }

    @Dynamic(value = IxDynamicResourceHandler.IS_USER_PRESENT, handler = ix.ncats.controllers.security.IxDeadboltHandler.class)
    public static Result profile () {

        Principal user = ix.ncats.controllers.auth.Authentication.getUser();

        return ok (ix.ginas.views.html.admin.profile.render(user));
    }
        
   
    @Dynamic(value = IxDynamicResourceHandler.CAN_SEARCH, handler = ix.ncats.controllers.security.IxDeadboltHandler.class)
    public static Result substances(final String q, final int rows,
                                    final int page) {
        String type = request().getQueryString("type");
        Logger.debug("Substances: rows=" + rows + " page=" + page);
        SearchType stype = SearchType.valueFor(type);
        try {
        	if(stype.isStructureSearch()){
        		String cutoff = request().getQueryString("cutoff");
        		try {
	        		Structure qStructure=getStructureFrom(q);
	        		flash("qStructureID", qStructure.id.toString());
	        		switch(stype){
		        		case SUBSTRUCTURE:
		        			return substructure(qStructure.smiles, rows, page);
		        		case SIMILARITY:
		        			
		        			double thres = Math.max
		                    (.3, Math.min(1.,Double.parseDouble(cutoff)));
		        			return similarity(qStructure.smiles, thres, rows, page);
		        		case FLEX:
		        			return lychimatch(qStructure.smiles, rows, page, false);
		        		case EXACT:
		        			return lychimatch(qStructure.smiles, rows, page, true);
		        		default:
		        			return substructure(qStructure.smiles,  rows, page);
		        	}
        		}catch(Exception e){
        			e.printStackTrace();
        		}
        		return notFound(ix.ginas.views.html.error.render
                        (400, "Invalid search parameters: type=\""
                                + type + "\"; q=\"" + q + "\" cutoff=\""
                                + cutoff + "\"!"));
        	}else if(stype.isSequenceSearch()){
        		 return sequences (q, rows, page);
        	}else{
        		return _substances (q, rows, page);
        	}
        }catch (Exception ex) {
            ex.printStackTrace();
            return _internalServerError(ex);
        }
    }
   
   
    public static Result export (String collectionID, String extension) {
    	final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    	SearchResultContext src = SearchResultContext.getSearchResultContextForKey(collectionID);
    	
    	try{
	    		 
	    	final PipedInputStream pis =               new PipedInputStream ();
	    	final PipedOutputStream pos = new PipedOutputStream (pis);

            GinasSubstanceExporterFactoryPlugin factoryPlugin = Play.application().plugin(GinasSubstanceExporterFactoryPlugin.class);

            if(factoryPlugin ==null){
                System.out.println("FACTORY PLUGIN NULL!!!!!!");
            }

            SubstanceExporterFactory.Parameters params = new SubstanceParameters(factoryPlugin.getFormatFor(extension));

            SubstanceExporterFactory factory= factoryPlugin.getExporterFor(params);
            if(factory ==null){
                //TODO handle null couldn't find factory for params
                throw new IllegalArgumentException("could not find suitable factory for " + params);
            }

            Exporter<Substance> exporter = factory.createNewExporter(pos, params);

            String fname = "export-" +sdf.format(new Date()) + "." + extension;
            response().setContentType("application/x-download");
            response().setHeader("Content-disposition","attachment; filename=" + fname);

            Executors.newSingleThreadExecutor().submit(()->{
                    try {
                        exporter.exportForEachAndClose(src.getResults());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });

	    	return ok(pis);
    	}catch(Exception e){
    		e.printStackTrace();
    		throw new IllegalStateException(e);
    	}
    }


    private static class SubstanceParameters implements SubstanceExporterFactory.Parameters{
        private final SubstanceExporterFactory.OutputFormat format;
        SubstanceParameters(SubstanceExporterFactory.OutputFormat format){
            Objects.requireNonNull(format);
            this.format = format;
        }
        @Override
        public SubstanceExporterFactory.OutputFormat getFormat() {
            return format;
        }
    }

    public static Result sequences (final String q,
                                    final int rows, final int page) {
        String param = request().getQueryString("identity");
        double identity = 0.5;
        if (param != null) {
            try {
                identity = Double.parseDouble(param);
            }
            catch (NumberFormatException ex) {
                Logger.error("Bogus identity value: "+param);
            }
        }
        
        String seq = GinasFactory.getSequence(q);
        if (seq != null) {
            Logger.debug("sequence: "
                         +seq.substring(0, Math.min(seq.length(), 20))
                         +"; identity="+identity);
            return _sequences (seq, identity, rows, page);
        }
        
        return internalServerError ("Unable to retrieve sequence for "+q);
    }

    public static Result _sequences (final String seq, final double identity,
                                     final int rows, final int page) {
    	CutoffType ct= CutoffType.GLOBAL;
    	try{
    		ct=CutoffType.valueOf(request().getQueryString("identityType"));
    	}catch(Exception e){
    		e.printStackTrace();
    	}
        try {
            SearchResultContext context = sequence
                (seq, identity, rows,
                 page, ct, new GinasSequenceResultProcessor ());
            
            return App.fetchResult (context, rows, page, new SubstanceResultRenderer ());
        }
        catch (Exception ex) {
            ex.printStackTrace();
            Logger.error("Can't perform sequence search", ex);
        }
        
        return internalServerError
            (ix.ginas.views.html.error.render
             (500, "Unable to perform squence search!"));
    }

    public static void instrumentSubstanceSearchOptions(SearchOptions options, Map<String, String[]> params) {
        SearchOptions.FacetLongRange editedRange =
            new SearchOptions.FacetLongRange ("modified");
        SearchOptions.FacetLongRange approvedRange =
            new SearchOptions.FacetLongRange ("approved");

        Calendar now = Calendar.getInstance();
        Calendar cal = (Calendar)now.clone();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        // add a single character prefix so as to keep the map sorted; the
        // decorator strips this out
        long[] range = new long[]{cal.getTimeInMillis(),
                                  now.getTimeInMillis()};
        editedRange.add("aToday", range);
        approvedRange.add("aToday", range);
        now = (Calendar)cal.clone();

        cal.set(Calendar.DAY_OF_WEEK, cal.getFirstDayOfWeek());
        range = new long[]{cal.getTimeInMillis(),
                           now.getTimeInMillis()};
        editedRange.add("bThis week", range);
        approvedRange.add("bThis week", range);
        now = (Calendar)cal.clone();

        cal.set(Calendar.WEEK_OF_MONTH, 1);
        range = new long[]{cal.getTimeInMillis(),
                           now.getTimeInMillis()};
        editedRange.add("cThis month", range);
        approvedRange.add("cThis month", range);
        now = (Calendar)cal.clone();

        cal = (Calendar)now.clone();
        cal.add(Calendar.MONTH, -6);
        range = new long[]{cal.getTimeInMillis(),
                           now.getTimeInMillis()};
        editedRange.add("dPast 6 months", range);
        approvedRange.add("dPast 6 months", range);
        now = (Calendar)cal.clone();

        cal = (Calendar)now.clone();
        cal.add(Calendar.YEAR, -1);
        range = new long[]{cal.getTimeInMillis(),
                           now.getTimeInMillis()};
        editedRange.add("ePast 1 year", range);
        approvedRange.add("ePast 1 year", range);
        now = (Calendar)cal.clone();
        
        cal = (Calendar)now.clone();
        cal.add(Calendar.YEAR, -2);
        range = new long[]{cal.getTimeInMillis(),
                           now.getTimeInMillis()};
        editedRange.add("fPast 2 years", range);
        approvedRange.add("fPast 2 years", range);

        options.longRangeFacets.add(editedRange);
        options.longRangeFacets.add(approvedRange);
        
        if(params!=null){
        	String[] dep =params.get("showDeprecated");
        	if(dep==null || dep.length<=0 || dep[0].equalsIgnoreCase("false")){
        		options.termFilters.add(new TermFilter("SubstanceDeprecated","false"));
        	}
        }
    }

    public static List<Facet> getSubstanceFacets (int fdim, Map<String, String[]> map) throws IOException {
        SearchOptions options = new SearchOptions (Substance.class);
        options.fdim = fdim;
        instrumentSubstanceSearchOptions (options, map);
        return getTextIndexer().search(options, null, null).getFacets();
    }

    public static SearchResult getSubstanceSearchResult(final String q, final int total) {
        final Map<String, String[]> params = App.getRequestQuery();
        
        final String sha1 = App.getKeyForCurrentRequest();
        String[] order = params.get("order");
        
        
        //default to "lastEdited" as sort order
        if(order==null || order.length<=0){
        	order=new String[]{"$lastEdited"};
        	params.put("order", order);
        }
        
        try {
            long start = System.currentTimeMillis();
            SearchResult result = getOrElse(sha1, ()->{
                            SearchOptions options = 
                            		new SearchOptions(Substance.class, total, 0, FACET_DIM)
                            			 .parse(params);
                            instrumentSubstanceSearchOptions (options, params);
                            return cacheKey(getTextIndexer().search(options, q), sha1);
                    });
            Logger.debug(sha1 + " => " + result);
            
            double elapsed = (System.currentTimeMillis() - start)*1e-3;
            Logger.debug(String.format("Elapsed %1$.3fs to retrieve "
                                     + "search %2$d/%3$d results...",
                                       elapsed, result.size(),
                                       result.count()));
            return result;
        }catch (Exception ex) {
            ex.printStackTrace();
            Logger.trace("Unable to perform search", ex);
        }
        return null;    
    }

    /**
     * Returns substances based in provided query string and/or request
     * parameters.
     * 
     * @param q
     * @param rows
     * @param page
     * @return
     * @throws Exception
     */
    static Result _substances(String q, final int rows, final int page)
        throws Exception {
        final int total = Math.max(SubstanceFactory.getCount(), 1);
        final String user=UserFetcher.getActingUser(true).username;
        final String key = "substances/" + Util.sha1(request());
        
        //final String[] searchFacets = facets;
        
        //Special piece to show deprecated records
        boolean forceq = (request().getQueryString("showDeprecated")==null);
        
        // if there's a provided query, or there's a facet specified,
        // do a text search
        if (forceq || q != null || request().queryString().containsKey("facet")) {
            final SearchResult result = getSubstanceSearchResult (q, total);
            Logger.debug("_substance: q=" + q + " rows=" + rows + " page="
                         + page + " => " + result + " finished? "
                         + result.finished());
            if (result.finished()) {
                final String k = key + "/result"; 
                return getOrElse(k, ()->createSubstanceResult(result, rows, page));
            }
            return createSubstanceResult(result, rows, page);
            //otherwise, just show the first substances
        } else {
            return getOrElse(key, () -> {
            	SubstanceResultRenderer srr=new SubstanceResultRenderer();
                        Logger.debug("Cache missed: " + key);
                        List<Facet> defFacets=getSubstanceFacets (30,request().queryString());
                        int nrows   = Math.max(Math.min(total,  rows), 1);
                        int[] pages = paging(nrows, page, total);
                        List<Substance> substances = SubstanceFactory
                        		.getSubstances(nrows, (page - 1) * rows, null);
                        return srr.render(null, page, nrows, total, pages, defFacets, substances);
                });
        }
    }
    
    
    static Result createSubstanceResult(SearchResult result,
                                        int rows, int page) throws Exception{
        return fetchResultImmediate (result, rows, page, new SubstanceResultRenderer ());
    }
    
    
    
    public static class SubstanceVersionFetcher extends GetResult{
        String version;
        public SubstanceVersionFetcher(String version){
                this.version=version;
        }
        public Result getResult(List<Substance> e) throws Exception{
                List<Substance> slist=new ArrayList<Substance>();
                for(Substance s:e){
                        Substance s2=SubstanceFactory.getSubstanceVersion(s.uuid.toString(),version);
                        slist.add(s2);
                }
                return _getSubstanceResult(slist);
        }
    }



    public static final GetResult SubstanceResult = new GetResult();

            
  
    @Dynamic(value = IxDynamicResourceHandler.CAN_SEARCH, handler = ix.ncats.controllers.security.IxDeadboltHandler.class)
    static Result _getSubstanceResult(List<Substance> substances)
        throws Exception {
        // force it to show only one since it's possible that the provided
        // name isn't unique
        if (substances.size() == 1) {
            Substance substance = substances.iterator().next();
            Substance.SubstanceClass type = substance.substanceClass;
            switch (type) {
            case chemical:
                return ok(ix.ginas.views.html.details.chemicaldetails
                          .render((ChemicalSubstance) substance));
            case protein:
                return ok(ix.ginas.views.html.details.proteindetails
                          .render((ProteinSubstance) substance));
            case mixture:
                return ok(ix.ginas.views.html.details.mixturedetails
                          .render((MixtureSubstance) substance));
            case polymer:
                return ok(ix.ginas.views.html.details.polymerdetails
                          .render((PolymerSubstance) substance));
            case structurallyDiverse:
                return ok(ix.ginas.views.html.details.diversedetails
                          .render((StructurallyDiverseSubstance) substance));
            case specifiedSubstanceG1:
                return ok(ix.ginas.views.html.details.group1details
                          .render((SpecifiedSubstanceGroup1Substance) substance));
            case concept:
                return ok(ix.ginas.views.html.details.conceptdetails
                          .render((Substance) substance));
            case nucleicAcid:
                return ok(ix.ginas.views.html.details.nucleicaciddetails
                          .render((NucleicAcidSubstance) substance));
            default:
                return _badRequest("type not found");
            }
        }
        else {  //rarely used
            SearchResult result;
            try(TextIndexer indexer = getTextIndexer().createEmptyInstance()) {
                for (Substance sub : substances) {
                    indexer.add(EntityWrapper.of(sub));
                }
                result = SearchFactory.search
                        (indexer, Substance.class, null, null, indexer.size(),
                                0, FACET_DIM, request().queryString());
            }
            if (result.count() < substances.size()) {
                substances.clear();
                result.copyTo(substances, 0, result.count());
            }
            TextIndexer.Facet[] facets = filter(result.getFacets(), ALL_FACETS);
            System.out.println("Filtered to:" + facets.length + " facets");

            return ok(ix.ginas.views.html.substances.render
                    (1, result.count(), result.count(), new int[0],
                            decorate(facets), substances, null));

        }
    }

    public static Result substance(String name) {
        return SubstanceResult.get(name);
    }

     public static Result substanceVersion(String name, String version) {
        return new SubstanceVersionFetcher(version).get(name);
    }
     
    public static Result similarity(final String query, final double threshold,
                                    int rows, int page) {
        try {
            SearchResultContext context = similarity
                (query, threshold, rows,
                 page, new StructureSearchResultProcessor(isWaitSet()));
            return fetchResult (context, rows, page,
                                new SubstanceResultRenderer ());
        }catch (Exception ex) {
            ex.printStackTrace();
            Logger.error("Can't perform similarity search: " + query, ex);
        }
        
        return internalServerError
            (ix.ginas.views.html.error.render
             (500, "Unable to perform similarity search: " + query));
    }

    
    
	public static Result lychimatch(final String query, int rows, int page, boolean exact) {
		try {
			Structure struc2 = StructureProcessor.instrument(query, null, true); // don't
																					// standardize
			String hash = struc2.getLychiv3Hash();
			if (exact) {
				hash = "root_structure_properties_term:" + struc2.getLychiv4Hash();
			}
			return _substances(hash, rows, page);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return internalServerError(ix.ginas.views.html.error.render(500, "Unable to perform flex search: " + query));
	}

    public static Result substructure(final String query, final int rows,
                                      final int page) {
        try {
        	
            SearchResultContext context = App.substructure
                (query, rows, page, new StructureSearchResultProcessor(isWaitSet()));
            return App.fetchResult
                (context, rows, page, 
                 new SubstanceResultRenderer ());
        } catch (BogusPageException ex) {
            return internalServerError
                    (ix.ginas.views.html.error.render
                     (500, ex.getMessage()));
        } catch (Exception ex){
        	ex.printStackTrace();
            Logger.error("Can't perform substructure search", ex);
        }
        return internalServerError
            (ix.ginas.views.html.error.render
             (500, "Unable to perform substructure search: " + query));
    }



    /**
     * proper permission should be checked here
     * 
     * TP:
     * 
     * This needs to be re-evaluated. It is possible for there to be
     * duplicated names, and there is no check here for this.
     * 
     * While it's not as pretty, I'm defaulting to using the uuid or
     * approvalID.
     * 
     * Update: When is this called now? In constructing a URL probably?
     * 
     */
    public static String getId(Substance substance) {
        return substance.getUuid().toString().substring(0, 8);
    }

    static class GetResult{
    	
    	//Get a rendered thing for a name
        public Result get(final String name) {
            try {
                final String key = Substance.class.getName() + "/" + name;
                List<Substance> e = getOrElse(key, ()->resolveName(name));
                if (e.isEmpty()) {
                    return _notFound("Unknown name: " + name);
                }
                return result(e);
            } catch (Exception ex) {
                Logger.error("Unable to generate Result for \"" + name + "\"",
                             ex);
                return _internalServerError(ex);
            }
        }

        public Result result(final List<Substance> e) {
            try {
                final String key = Substance.class.getName() + "/result/" + Util.sha1(request());
                return getOrElse(key, ()->getResult(e));
            } catch (Exception ex) {
                throw new IllegalStateException(ex);
            }
        }
        
        public Result getResult(List<Substance> substances) throws Exception {
        	try{
        		return _getSubstanceResult(substances);
        	}catch(Exception e){
        		e.printStackTrace();
        		throw e;
        	}
        }

       // abstract Result getResult(List<Substance> e) throws Exception;
    }
    
    public static List<Substance> resolveName(String name) {
    	Finder<UUID,Substance> finder= SubstanceFactory.finder;
        if (name == null) {
            return null;
        }else{
                try{
                        UUID uuid = UUID.fromString(name);
                        List<Substance> slist=new ArrayList<Substance>();
                        slist.add(SubstanceFactory.getSubstance(uuid));
                        return slist;
                }catch(Exception e){
                        //Not a UUID
                }
        }
        List<Substance> values = new ArrayList<Substance>();
        if (name.length() == 8) { // might be uuid
            values = finder.where().istartsWith("uuid", name).findList();
        }
        if (values.isEmpty()) {
            values = finder.where().ieq("approvalID", name).findList();
            if (values.isEmpty()) {
                values = finder.where().ieq("names.name", name).findList(); //this is a problem for oracle
                if (values.isEmpty()){
                    values = finder.where().ieq("codes.code", name).findList();// last resort..
                }
            }
        }
        if (values.size() > 1) {
            Logger.warn("\"" + name + "\" yields " + values.size()
                        + " matches!");
        }
        return values;
    }

    public static Set<Keyword> getStructureReferences(GinasChemicalStructure s) {
        if(s.getReferences()!=null) return s.getReferences();
        return new LinkedHashSet<Keyword>();
    }

    
    /**
     * return the canonical/default chemical id
     */
    public static String siteCheck(Protein prot, int subunit, int index) {
        String desc = prot.getSiteModificationIfExists(subunit, index);
        if (desc == null)
            return "";
        return desc;
    }
    
    public static String siteCheckNA(NucleicAcid na, int subunit, int index) {
        String desc = na.getSiteModificationIfExists(subunit, index);
        if (desc == null)
            return "";
        return desc;
    }

    public static List<Integer> getSites(Modifications mod, int index) {
        List<Integer> subunit = new ArrayList<Integer>();
        for (StructuralModification sm : mod.structuralModifications) {
            subunit = sitesInSubunit(sm.getSites(), index);
        }
        return subunit;
    }

    public static List<Integer> getSites(Glycosylation mod, int index) {
        ArrayList<Integer> subunit = new ArrayList<Integer>();
        subunit.addAll(sitesInSubunit(mod.getCGlycosylationSites(), index));
        subunit.addAll(sitesInSubunit(mod.getNGlycosylationSites(), index));
        subunit.addAll(sitesInSubunit(mod.getOGlycosylationSites(), index));
        return subunit;
    }

    public static List<Integer> getSites(List<DisulfideLink> disulfides,
                                         int index) {
        return disulfides.stream()
        		  .map(ds -> ds.getSites())
        		  .flatMap(s->s.stream())
        		  .filter(s->s.subunitIndex == index)
        		  .map(s->s.residueIndex)
        		  .collect(Collectors.toList());
    }

    public static List<Integer> sitesInSubunit(List<Site> sites, int index) {
        return sites.stream()
        		.filter(s->s.subunitIndex == index)
        		.map(s->s.residueIndex)
        		.collect(Collectors.toList());
    }
    

    //These should be somewhere else

    public static String getAAName(char aa) {
        ControlledVocabulary cv = ControlledVocabularyFactory.getControlledVocabulary("AMINO_ACID_RESIDUE");
        for(VocabularyTerm t : cv.terms){
                if(t.value.equals(aa+"")){
                        return t.display;
                }
        }
        return "UNKNOWN";

    }
    
    public static String getNAName(char aa) {
        ControlledVocabulary cv = ControlledVocabularyFactory.getControlledVocabulary("NUCLEIC_ACID_BASE");
        for(VocabularyTerm t : cv.terms){
                if(t.value.equals(aa+"")){
                        return t.display;
                }
        }
        return "UNKNOWN";

    }

    @SuppressWarnings("rawtypes")
    public static int getCount(Glycosylation obj) {
        if (obj == null)
            return 0;
        return obj.getSiteCount();
    }
    
    
    
    @BodyParser.Of(value = BodyParser.Text.class, maxLength = 1024 * 1024)
    public static Result interpretMolfile() {
        ObjectMapper mapper = EntityFactory.EntityMapper.FULL_ENTITY_MAPPER();
        ObjectNode node = mapper.createObjectNode();
        try {
            String opayload = request().body().asText();
            String payload = ChemCleaner.getCleanMolfile(opayload);
            if (payload != null) {
                List<Structure> moieties = new ArrayList<Structure>();
                
                try {
                    Structure struc = StructureProcessor.instrument
                        (payload, moieties, false); // don't standardize!
                    // we should be really use the PersistenceQueue to do this
                    // so that it doesn't block
                    
                    // in fact, it probably shouldn't be saving this at all
                    if(payload.contains("\n") && payload.contains("M  END")){
                    	struc.molfile=payload;
                    }
                    
                    StructureFactory.saveTempStructure(struc);
                    
                    ArrayNode an = mapper.createArrayNode();
                    for (Structure m : moieties){
                        //m.save();
                    	StructureFactory.saveTempStructure(m);
                        ObjectNode on = mapper.valueToTree(m);
                        Amount c1=Moiety.intToAmount(m.count);
                        JsonNode amt=mapper.valueToTree(c1);
                        on.set("countAmount", amt);
                        an.add(on);
                    }
                    node.put("structure", mapper.valueToTree(struc));
                    node.put("moieties", an);
                } catch (Exception e) {
                        e.printStackTrace();
                }
                try {
                    Chemical c = ChemicalFactory.DEFAULT_CHEMICAL_FACTORY()
                        .createChemical(payload, Chemical.FORMAT_AUTO);

                    Collection<StructuralUnit> o = PolymerDecode
                        .DecomposePolymerSU(c, true);
                    for (StructuralUnit su : o) {
                        Structure struc = StructureProcessor.instrument
                            (su.structure, null, false);
                        //struc.save();
                        StructureFactory.saveTempStructure(struc);
                        su._structure = struc;
                    }
                    node.put("structuralUnits", mapper.valueToTree(o));
                } catch (Exception e) {
                    Logger.error("Can't enumerate polymer", e);
                }
            }
        } catch (Exception ex) {
                ex.printStackTrace();
            Logger.error("Can't process payload", ex);
            return internalServerError("Can't process mol payload");
        }
        return ok(node);
    }
    
	public static Structure getStructureFrom(String str) {
		if(str==null) return null;
		if(UUIDUtil.isUUID(str)){
    		Structure s = StructureFactory.getStructure(str);
    		if(s!=null){
    			return s;
    		}
    	}
		try {
			List<Structure> moieties = new ArrayList<Structure>();
			String payload = ChemCleaner.getCleanMolfile(str);
			Structure struc = StructureProcessor.instrument(payload, moieties, false); // don't
																						// standardize!
			if (payload.contains("\n") && payload.contains("M  END")) {
				struc.molfile = payload;
			}

			StructureFactory.saveTempStructure(struc);
			return struc;
		} catch (Exception e) {
			e.printStackTrace();
			throw new IllegalStateException("Can not parse structure from:" +str);
		}
	}
    
    
	

    public static class StructureSearchResultProcessor
        extends SearchResultProcessor<StructureIndexer.Result, ChemicalSubstance> {
    	int index;
    	public static EntityInfo<ChemicalSubstance> chemMeta=EntityUtils.getEntityInfoFor(ChemicalSubstance.class);
    	
        StructureSearchResultProcessor(boolean wait) {
        	this.setWait(wait);
        }

        
        protected ChemicalSubstance instrument(StructureIndexer.Result r)
            throws Exception {
        	
        	
        	Key k=Key.of(chemMeta,UUID.fromString(r.getId()));
        	EntityFetcher<ChemicalSubstance> efetch = EntityFetcher.of(k, chemMeta.getClazz());
        	
        	ChemicalSubstance chem = efetch.call(); 
            
            if (chem!=null) {
            	UUID structureID = chem.structure.id;
            	double similarity=r.getSimilarity();
                Logger.debug(String.format("%1$ 5d: matched %2$s %3$.3f", ++index,
                                           r.getId(), r.getSimilarity()));
                int[] amap = new int[r.getMol().getAtomCount()];
                int i = 0, nmaps = 0;
                for (MolAtom ma : r.getMol().getAtomArray()) {
                    amap[i] = ma.getAtomMap();
                    if (amap[i] > 0){
                        ++nmaps;
                    }
                    ++i;
                }
                if (nmaps > 0) {
                	String cachekey="AtomMaps/"+getContext().getId()+"/" +structureID;
                    IxCache.setTemp(cachekey, amap);
                }
                IxCache.setTemp("Similarity/"+getContext().getId()+"/" +structureID, similarity);
            }
            return chem;
        }
    }

    public static class GinasSequenceResultProcessor
        extends SearchResultProcessor<SequenceIndexer.Result, ProteinSubstance> {
        GinasSequenceResultProcessor () {}
        
        @Override
        protected ProteinSubstance instrument (SequenceIndexer.Result r)
            throws Exception {
            List<ProteinSubstance> proteins = SubstanceFactory.protfinder
                .where().eq("protein.subunits.uuid", r.id).findList(); //also slow
            ProteinSubstance protein = proteins.isEmpty() ? null : proteins.get(0);
            if (protein != null) {
                IxCache.setTemp("Alignment/"+getContext().getId()+"/"+r.id, r);
            }
            else {
                Logger.warn("Can't retrieve protein for subunit "+r.id);
            }
            return protein;
        }
    }

    public static SequenceIndexer.Result
        getSeqAlignment (String context, String id) {
        return (SequenceIndexer.Result)IxCache.getTemp("Alignment/"+context+"/"+id);
    }
    public static Double
        getChemSimilarity (String context, String id) {
        return (Double)IxCache.getTemp("Similarity/"+context+"/"+id);
    }

    //This is wrong
    static public Substance resolve(Relationship rel) {
        Substance relsub = null;
        try {
            relsub = SubstanceFactory.finder
            		.where()
            		.eq("approvalID", rel.relatedSubstance.approvalID)
            		.findUnique();
        } catch (Exception ex) {
            Logger.warn("Can't retrieve related substance "
                        + "from relationship " + rel.getUuid());
        }
        return relsub;
    }

    static public List<Relationship> resolveRelationships(String uuid) {
        List<Relationship> resolved = new ArrayList<Relationship>();
        try {
            Substance sub = SubstanceFactory.finder.where().where().eq("uuid", uuid).findUnique();
            if (sub != null) {
                for (Relationship rel : sub.relationships) {
                    if (null != resolve(rel))
                        resolved.add(rel);
                }
            } else {
                Logger.warn("Unknown substance: " + uuid);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            Logger.error("Can't resolve relationship for substance " + uuid, ex);
        }
        return resolved;
    }

    @Dynamic(value = IxDynamicResourceHandler.CAN_SEARCH, handler = ix.ncats.controllers.security.IxDeadboltHandler.class)
    static public Result relationships(String uuid) {
        List<Relationship> rels = resolveRelationships(uuid);
        ObjectMapper mapper = new ObjectMapper();
        return Java8Util.ok(mapper.valueToTree(rels));
    }

    /******************* MIXTURES *************************************************/

    public static List<Component> getComponentsByType
        (List<Component> components, String type) {
        List<Component> comp = new ArrayList<Component>();
        for (Component c : components) {
            if (c.type.equals(type)) {
                comp.add(c);
            }
        }
        return comp;
    }

    @Dynamic(value = IxDynamicResourceHandler.CAN_SEARCH, handler = ix.ncats.controllers.security.IxDeadboltHandler.class)
    public static Result getCVField(String field) {
        String terms = CV.getCV(field);
        return ok(terms);
    }


    /**
     * Renders a chemical structure from structure ID
     * atom map can be provided for highlighting
     * 
     * @param id
     * @param format
     * @param size
     * @param context
     * @return
     */
    @Dynamic(value = IxDynamicResourceHandler.CAN_SEARCH, handler = ix.ncats.controllers.security.IxDeadboltHandler.class)
    public static Result structure (final String id,
                                    final String format, 
                                    final int size,
                                    final String context) {
    	if(!UUIDUtil.isUUID(id)){
    		return App.render(id,size);
    	}
        //Logger.debug("Fetching structure");
        String atomMap = "";
        if (context != null) {
            int[] amap = (int[])IxCache.getTemp("AtomMaps/"+context+"/"+id);
            //Logger.debug("AtomMaps/"+context+" => "+amap);
            if (amap != null && amap.length > 0) {
                StringBuilder sb = new StringBuilder ();
                sb.append(amap[0]);
                for (int i = 1; i < amap.length; ++i){
                    sb.append(","+amap[i]);
                }
                atomMap = sb.toString();
            }else{
                atomMap = context;
            }
        }
        //Logger.debug("structure: id="+id+" context="+context+" amap="+atomMap); 
        
        Result r1 = App.structure(id, format, size, atomMap);
        int httpStat =  r1.toScala().header().status();
        if(httpStat == NOT_FOUND){
            Substance s = SubstanceFactory.getSubstance(id);
            if(s!=null){
                if(s instanceof ChemicalSubstance){
                        String sid1= ((ChemicalSubstance) s).structure.id.toString();
                        return App.structure(sid1, format, size, atomMap);
                    }else{
                        return placeHolderImage(s);
                    }
            }else{
                try{
                        Unit u=GinasFactory.findUnitById(id);
                        if(u!=null){
                        	return App.render(u.structure,size);
                        }
                }catch(Exception e){
                        e.printStackTrace();
                }
                return placeHolderImage(null);
            }
        }
        return r1;
        
    }

	public static Result structure2(final String id) {
		return structure(id,"svg",150,null);

	}
    
    public static Result placeHolderImage(Substance s){
        String placeholderFile="polymer.svg";
        if(s!=null){
                switch (s.substanceClass) {
                case chemical:
                        placeholderFile="chemical.svg";break;
                case protein:
                        placeholderFile="protein.svg";break;
                case mixture:
                        placeholderFile="mixture.svg";break;
                case polymer:
                        placeholderFile="polymer.svg";break;
                case structurallyDiverse:
                        placeholderFile="structurally-diverse.svg";break;
                case concept:
                        placeholderFile="concept.svg";break;
                case nucleicAcid:
                        placeholderFile="nucleic-acid.svg";break;
                case specifiedSubstanceG1:
                default:
                        placeholderFile="polymer.svg";
                }
        }else{
        	placeholderFile="noimage.svg";
        }
        
        try{    
                InputStream is=Util.getFile(placeholderFile, "public/images/");
                response().setContentType("image/svg+xml");
                return ok(is);
        }catch(Exception e){
                return _internalServerError(e);
        }
        
    }
    
    
    
    
    
    /**
     * Converts a structure of substance to a chemical structure
     * format. Warnings are put into the header at "EXPORT-WARNINGS"
     * 
     * @param id
     * @param format
     * @param context
     * @return
     */
    @Dynamic(value = IxDynamicResourceHandler.CAN_SEARCH, handler = ix.ncats.controllers.security.IxDeadboltHandler.class)
    public static Result structureExport (final String id,
                                    final String format, final String context) {
    	List<GinasProcessingMessage> messages = new ArrayList<GinasProcessingMessage>();
        
        
        
        Chemical c=null;
        Protein p=null;
        
        Substance s = SubstanceFactory.getSubstance(id);
        
        if(s==null){
                Structure struc = StructureFactory.getStructure(id);
                c = struc.toChemical(messages);
                
        }else{
                c = s.toChemical(messages);
        }
        
        
        
        ObjectMapper om = new ObjectMapper();
        Logger.debug("SERIALIZED:" + om.valueToTree(messages).toString());
        response().setHeader("EXPORT-WARNINGS",om.valueToTree(messages).toString() +"___");
                try {
                    if (format.equalsIgnoreCase("mol")){
                        return ok(formatMolfile(c,Chemical.FORMAT_MOL));
                    }else if (format.equalsIgnoreCase("sdf")){
                        return ok(formatMolfile(c,Chemical.FORMAT_SDF));
                    }else if (format.equalsIgnoreCase("smiles")){
                        return ok(c.export(Chemical.FORMAT_SMILES));
                    }else if (format.equalsIgnoreCase("cdx")){
                        return ok(c.export(Chemical.FORMAT_CDX));
                    }else if (format.equalsIgnoreCase("fas")){
                        if(s instanceof ProteinSubstance){   
                            return ok(makeFastaFromProtein(((ProteinSubstance)s)));
                        }else if(s instanceof NucleicAcidSubstance){
                            return ok(makeFastaFromNA(((NucleicAcidSubstance)s)));
                        }else{
                        	throw new IllegalStateException("object can not be exported to FASTA");
                        }
                    }else{
                        return _badRequest("unknown format:" + format);
                    }
                } catch (Exception e) {
                        e.printStackTrace();
                        return _badRequest(e.getMessage());
                } 
        
    }
    
    
    
    public static String formatMolfile(Chemical c, int format) throws Exception{
        String mol=c.export(format);
        String[] lines = mol.split("\n");
        lines[1] = " G-SRS " + lines[1];
        return String.join("\n", lines);
    }
    public static String makeFastaFromProtein(ProteinSubstance p){
        StringBuilder sb= new StringBuilder();
        
        List<Subunit> subs=p.protein.getSubunits();
        Collections.sort(subs, new Comparator<Subunit>(){
                @Override
                public int compare(Subunit o1, Subunit o2) {
                    return o1.subunitIndex-o2.subunitIndex;
                }});
        for(Subunit s: subs){
        	
        	sb.append(">" + p.getBestId().replace(" ", "_") + "|SUBUNIT_" +  s.subunitIndex + "\n");
            for(String seq : splitBuffer(s.sequence,80)){
                sb.append(seq+"\n");
            }
        }
        return sb.toString();
    }
    public static String makeFastaFromNA(NucleicAcidSubstance p){
        String resp = "";
        List<Subunit> subs=p.nucleicAcid.getSubunits();
        Collections.sort(subs, new Comparator<Subunit>(){
                        @Override
                        public int compare(Subunit o1, Subunit o2) {
                                return o1.subunitIndex-o2.subunitIndex;
                        }});
        
        for(Subunit s: subs){
                resp+=">" + p.getBestId().replace(" ", "_") + "|SUBUNIT_" +  s.subunitIndex + "\n";
                for(String seq : splitBuffer(s.sequence,80)){
                        resp+=seq+"\n";
                }
        }
        return resp;
    }
    public static int totalSites(NucleicAcidSubstance sub, boolean includeEnds){
        return sub.getTotalSites(includeEnds);
    }
    public static int totalSites(NucleicAcidSubstance sub){
        return totalSites(sub, true);
    }
    
    public static Long getDateTime(Date d){
        if(d==null)return null;
        return d.getTime();
    }
    public static String getSugarName(Sugar s){
        return ControlledVocabularyFactory.getDisplayFor("NUCLEIC_ACID_SUGAR",s.getSugar());
        
    }
    public static String getLinkageName(Linkage s){
        return ControlledVocabularyFactory.getDisplayFor("NUCLEIC_ACID_LINKAGE",s.getLinkage());
    }

	public static String[] splitBuffer(String input, int maxLength) {
		int elements = (input.length() - 1) / maxLength + 1; 
		String[] ret = new String[elements];
		for (int i = 0; i < elements; i++) {
			int start = i * maxLength;
			ret[i] = input.substring(start, Math.min(input.length(), start + maxLength));
		}
		return ret;
	}

    
    
    public static Set<SubstanceExporterFactory.OutputFormat> getAllSubstanceExportFormats(){
        return Play.application().plugin(GinasSubstanceExporterFactoryPlugin.class).getAllSupportedFormats();
    }

    public static String getAsJson(Object o){
        if(o == null){
            return null;
        }
        ObjectMapper om = new ObjectMapper();
        return om.valueToTree(o).toString();
    }

    
    
    
    
    private static String updateKey =null;

    @Dynamic(value = IxDynamicResourceHandler.IS_ADMIN, handler = ix.ncats.controllers.security.IxDeadboltHandler.class)
    public synchronized static Result updateIndex(String key){
        if(!GinasLoad.ALLOW_REBUILD){
            return _badRequest("Cannot rebuild text index. Please ensure \"ix.ginas.allowindexrebuild\" is set to true");
        }
        if(updateKey==null){
            updateKey=UUID.randomUUID().toString();
        }
        Call callMonitor = routes.GinasApp.updateIndex("_monitor");

        if(listener.isCurrentlyRunning()) {
            return ok(new Html( new StringBuilder("<h1>Updating indexes:</h1><pre>").append(listener.getMessage()).append("</pre><br><a href=\"").append(callMonitor.url()).append("\">refresh</a>").toString()));
        }else{
            if(key==null || !updateKey.equals(key)){
                Call call = routes.GinasApp.updateIndex(updateKey);
                return ok(new Html(new StringBuilder("<h1>Updated indexes:</h1><pre>").append(listener.getMessage()).append("</pre><br><a href=\"").append(call.url()).append("\">Rebuild Index (warning: will take some time)</a>").toString()));
            }

            Runnable r= ()->{
                    try {
                        new ReIndexService(5, 10)
                                .reindexAll(new MultiReIndexListener(listener,
                                                                    Play.application().plugin(TextIndexerPlugin.class).getIndexer()));

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                };

            ForkJoinPool.commonPool().submit(r);
            updateKey=UUID.randomUUID().toString();

            return ok(new Html("<h1>Updating indexes:</h1><pre>Preprocessing ...</pre><br><a href=\"" + callMonitor.url() + "\">refresh</a>"));
        }
    }




    private static class SubstanceReIndexListener implements ReIndexListener {

        /**
         * Log of index messages so they are saved to file for later examination
         * in case of failure.  Previously logs only written to browser.
         */
        private static final Logger.ALogger LOG = Logger.of("index-rebuild");

        private long startTime;
        private StringBuilder message = new StringBuilder();

        private int totalIndexed = 0;

        private String recordsToIndex = "?";

        private long lastUpdateTime;

        private boolean currentlyRunning = false;

        private int currentRecordsIndexed=0;

        private int recordsIndexedLastUpdate=0;
        @Override
        public void newReindex() {
            lastUpdateTime = startTime = System.currentTimeMillis();
            message = new StringBuilder(10_000);
            totalIndexed = 0;
            recordsToIndex = "?";
            currentlyRunning = true;
            currentRecordsIndexed=0;

            recordsIndexedLastUpdate=0;
        }

        public StringBuilder getMessage() {
            return message;
        }

        public boolean isCurrentlyRunning() {
            return currentlyRunning;
        }

        @Override
        public void doneReindex() {
            if(!currentlyRunning){
                return;
            }
            updateMessage();
            currentlyRunning = false;

            EntityPersistAdapter.doneReindexing();

            StringBuilder doneMessage = new StringBuilder(100);

            if(currentRecordsIndexed >= totalIndexed) {
                doneMessage.append("\n\nCompleted Substance reindexing.");
            }else{
                doneMessage.append("\n\nError : did not finish indexing all records, only re-indexed ").append(currentRecordsIndexed);
            }
            doneMessage.append("\nTotal Time:").append((System.currentTimeMillis() - startTime)).append("ms");
            message.append(doneMessage);
            LOG.info(doneMessage.toString());
        }

        @Override
        public void recordReIndexed(Object o) {
            currentRecordsIndexed++;
            if(currentRecordsIndexed %50 ==0){
                updateMessage();
            }
        }

        @Override
        public void totalRecordsToIndex(int total) {
            recordsToIndex = Integer.toString(total);
        }



        private void updateMessage() {
            int numProcessedThisTime = currentRecordsIndexed - recordsIndexedLastUpdate;
            if(numProcessedThisTime < 1){
                return;
            }
            long currentTime = System.currentTimeMillis();

            long totalTimeSerializing = currentTime - startTime;

            String toAppend="\n" + numProcessedThisTime + " more records Processed: " + currentRecordsIndexed + " of " + recordsToIndex + " in " + ((currentTime - lastUpdateTime))+ "ms (" +totalTimeSerializing + "ms serializing)";
            Logger.debug("REINDEXING:" + toAppend);
            LOG.info(toAppend);

            message.append(toAppend);

            lastUpdateTime = currentTime;

            recordsIndexedLastUpdate = currentRecordsIndexed;
        }

        @Override
        public void countSkipped(int numSkipped) {
            totalIndexed -= numSkipped;
        }

        @Override
        public void error(Throwable t) {

            t.printStackTrace();

            LOG.error("error reindexing", t);
        }
    }
}
