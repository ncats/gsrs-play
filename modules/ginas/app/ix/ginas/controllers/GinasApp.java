package ix.ginas.controllers;

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.ForkJoinPool;
import java.util.function.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.concurrent.ConcurrentHashMap;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import be.objectify.deadbolt.java.actions.Dynamic;
import gov.nih.ncats.molwitch.Atom;
import gov.nih.ncats.molwitch.Chemical;
import gov.nih.ncats.molwitch.io.ChemFormat;
import ix.core.controllers.v1.GsrsApiUtil;
import ix.core.exporters.OutputFormat;
import ix.core.validator.GinasProcessingMessage;
import ix.core.UserFetcher;
import ix.core.adapters.EntityPersistAdapter;
import ix.core.chem.ChemCleaner;
import ix.core.chem.PolymerDecode;
import ix.core.chem.PolymerDecode.StructuralUnit;
import ix.core.chem.StructureProcessor;
import ix.core.controllers.AdminFactory;
import ix.core.controllers.EntityFactory;
import ix.core.controllers.EntityFactory.EntityMapper;
import ix.core.controllers.StructureFactory;
import ix.core.controllers.search.SearchFactory;
import ix.core.controllers.search.SearchRequest;
import ix.core.models.*;
import ix.core.plugins.IxCache;
import ix.core.plugins.PayloadPlugin;
import ix.core.plugins.TextIndexerPlugin;
import ix.core.search.*;
import ix.core.search.text.TextIndexer;
import ix.core.search.text.TextIndexer.FV;
import ix.core.search.text.TextIndexer.Facet;
import ix.core.util.*;
import ix.core.util.EntityUtils.EntityInfo;
import ix.core.util.EntityUtils.EntityWrapper;
import ix.core.util.EntityUtils.Key;
import ix.core.util.StreamUtil.StreamConcatter;
import ix.core.plugins.Workers;
import ix.ginas.controllers.plugins.GinasSubstanceExporterFactoryPlugin;
import ix.ginas.controllers.v1.CV;
import ix.ginas.controllers.v1.ControlledVocabularyFactory;
import ix.ginas.controllers.v1.SubstanceFactory;
import ix.ginas.controllers.viewfinders.ListViewFinder;
import ix.ginas.controllers.viewfinders.ThumbViewFinder;
import ix.ginas.exporters.*;
import ix.ginas.models.v1.*;
import ix.ginas.utils.GinasUtils;
import ix.core.utils.executor.MultiProcessListener;
import ix.core.utils.executor.ProcessExecutionService;
import ix.ncats.controllers.App;
import ix.ncats.controllers.DefaultResultRenderer;
import ix.ncats.controllers.FacetDecorator;
import ix.ncats.controllers.crud.Administration;
import ix.ncats.controllers.security.IxDynamicResourceHandler;
import ix.seqaln.SequenceIndexer;
import ix.seqaln.SequenceIndexer.CutoffType;
import ix.utils.CallableUtil.TypedCallable;
import ix.utils.UUIDUtil;
import ix.utils.Util;
import org.jcvi.jillion.core.Range;
import org.jcvi.jillion.core.Ranges;
import play.Logger;
import play.Play;
import play.data.DynamicForm;
import play.data.Form;
import play.db.ebean.Model.Finder;
import play.libs.F;
import play.libs.Json;
import play.mvc.BodyParser;
import play.mvc.Call;
import play.mvc.Result;
import play.mvc.Results;
import play.twirl.api.Html;
import gov.nih.ncats.structureIndexer.StructureIndexer;
import com.wordnik.swagger.annotations.*;

/**
 * GinasApp is mostly a utility class for UI-related elements of the ginas
 * project. Typical user-driven routes direct to here. This includes browsing,
 * searching, and viewing record details. This class also includes some
 * convenience functions used by the Twirl templates for displaying certain
 * information.
 *
 * @author tyler
 *
 */
@Api(value = "/api/v1/substances", description = "Operations with substances")
public class GinasApp extends App {

    private static final String CAN_T_DISPLAY_RECORD = "Can't display record:";

    // This is the default search order.
    // Currently, this is "Newest change first"
    private static final String DEFAULT_SEARCH_ORDER = "$lastEdited";


    private static CachedSupplier<GinasSubstanceExporterFactoryPlugin> factoryPlugin = CachedSupplier
            .of(() -> Play.application().plugin(GinasSubstanceExporterFactoryPlugin.class));

    /**
     * Search types used for UI searches. At this time these types do not extend
     * to API searches.
     *
     * @author tyler
     *
     */
    public static enum SearchType {
        SUBSTRUCTURE, SIMILARITY, EXACT, FLEX, SEQUENCE, TEXT;
        public boolean isStructureSearch() {
            switch (this) {
                case SUBSTRUCTURE:
                case SIMILARITY:
                case EXACT:
                case FLEX:
                    return true;
                default:
                    return false;
            }
        }

        public boolean isSequenceSearch() {
            return this == SEQUENCE;
        }

        public static SearchType valueFor(String type) {
            if (type == null)
                return TEXT;
            if (type.toLowerCase().startsWith("sub")) {
                return SUBSTRUCTURE;
            } else if (type.toLowerCase().startsWith("sim")) {
                return SIMILARITY;
            } else if (type.toLowerCase().startsWith("exa")) {
                return EXACT;
            } else if (type.toLowerCase().startsWith("seq")) {
                return SEQUENCE;
            } else if (type.toLowerCase().startsWith("fle")) {
                return FLEX;
            } else if (type.toLowerCase().startsWith("text")) {
                return TEXT;
            }
            return TEXT;
        }
    }

    // ===============
    // Record Status
    // Validation
    // Stereo Chemistry
    // Modifications
    // Last Edited
    // Last Edited By
    // Material Class
    // Reference Type
    // Validated By
    // Last Validated

    
    
    private static Map<String,String[]> facetsForRole = new HashMap<String,String[]>();
    private static Map<String,String[]> facetsForUsername = new HashMap<String,String[]>();
    
    private static List<String> getSubstanceFacetsFor(String role){
    	return Play.application().configuration()
        				  .getStringList("ix.ginas.facets.substance." + role.toLowerCase(), new ArrayList<String>());
    }
    
    
    static CachedSupplier<PayloadPlugin> _payload = CachedSupplier.of(() -> {
        return Play.application().plugin(PayloadPlugin.class);
    });

    /**
     * Fetch the facets which will be displayed for the active user. This is accomplished by looking
     * at the active user's roles and groups, and then looking in the config for anything related to them.
     * 
     * <p>
     * All facets marked in the config under <pre>ix.ginas.facets.substance.[ROLE_OR_GROUP]</pre> will be
     * added to the list of facets to display for that user, after looking at all of that user's
     * roles and groups.
     * </p>
     * 
     * <p>
     * Each user also receives all facets under the <pre>ix.ginas.facets.substance.default</pre> config
     * variable.
     * </p>
     * 
     * 
     * @return
     */
    public static String[] getSubstanceFacetsForActiveUser() {
    	
    	try{
    		UserProfile up = UserFetcher.getActingUserProfile(true);
    		
    		String username = Optional.ofNullable(up).map(u->u.user.username).orElse("GUEST");
    		
    		return facetsForUsername.computeIfAbsent(username, (uk)->{

        		List<String> userMembership = new ArrayList<String>();
        	
        		if(up!=null){
        			if(up.getRoles()!=null){
        				userMembership.addAll(up.getRoles().stream().map(r->r.getName()).collect(Collectors.toList()));
        			}   
        			if(up.getGroups()!=null){
        				userMembership.addAll(up.getGroups().stream().map(g->g.name).collect(Collectors.toList()));
        			}    			
        		}
        		
        		
    	    	List<String> roles = userMembership
    					    	 .stream()
    					    	 .sorted()
    					    	 .collect(Collectors.toList());
    	    	
    	    	String roleconcat = roles.stream().collect(Collectors.joining());
    	    	
    	    	return facetsForRole.computeIfAbsent(roleconcat, k->{
    	    		List<String> facets=getSubstanceFacetsFor("default");
    	    		
    	    		return StreamUtil.with(facets.stream())
    			    		  .and(roles.stream()
    			    		            .flatMap(r->getSubstanceFacetsFor(r).stream()))
    			    		  .stream()
    			    		  .distinct()
    			    		  .toArray(i->new String[i]);
    	    	});	
    		});
    		
    	}catch(Exception e){
    		e.printStackTrace();
    		throw e;
    	}
    }

    static class SubstanceResultRenderer extends DefaultResultRenderer<Substance> {
        final String[] facets = getSubstanceFacetsForActiveUser();

        SubstanceResultRenderer() {
        }

        public Result render(SearchResultContext context, int page, int rows, int total, int[] pages,
                             List<TextIndexer.Facet> facets, List<Substance> substances) {
            return ok(ix.ginas.views.html.substances.render(page, rows, total, pages,
                    decorate(filter(facets, this.facets)), substances, context));
        }
    }

    private static SubstanceReIndexListener listener = new SubstanceReIndexListener();


    public static SubstanceReIndexListener getReindexListener(){
        return listener;
    }


    @Dynamic(value = IxDynamicResourceHandler.IS_ADMIN, handler = ix.ncats.controllers.security.IxDeadboltHandler.class)
    public static Result listGinasUsers(int page, int rows, String sortBy, String order, String filter) {
        List<UserProfile> profiles = Administration.principalsList();
        return ok(ix.ginas.views.html.admin.userlist.render(profiles, sortBy, order, filter));
    }

    @Dynamic(value = IxDynamicResourceHandler.IS_ADMIN, handler = ix.ncats.controllers.security.IxDeadboltHandler.class)
    public static Result editPrincipal(Long id) {
        UserProfile up = Administration.editUser(id);
        return ok(ix.ginas.views.html.admin.edituser.render(id, up, up.getRoles(),
                AdminFactory.aclNamesByPrincipal(up.user), AdminFactory.groupNamesByPrincipal(up.user)));
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


        try{
        DynamicForm requestData = Form.form().bindFromRequest();
        if (requestData.hasErrors()) {
               throw new IllegalStateException("User form has errors");
        }
        Administration.addUser(requestData);

        }catch(Exception e){
        	flash("error", e.getMessage());
        	return createPrincipal();
        }
        return redirect(ix.ginas.controllers.routes.GinasApp.listGinasUsers(1, 16, "", "", ""));
    }

    public static Result error(int code, String mesg) {
        return status(code, ix.ginas.views.html.error.render(code, mesg));
    }
    public static Result warn(int code, String mesg) {
        return status(code, ix.ginas.views.html.message.render(code, mesg));
    }

    public static Result lastUnicorn(String name) {
        return notFound(ix.ginas.views.html.error.render(404, "Unknown resource: " + request().uri()));
    }

    public static Result _notFound(String mesg) {
        return notFound(ix.ginas.views.html.error.render(404, mesg));
    }

    public static Result _badRequest(String mesg) {
        return badRequest(ix.ginas.views.html.error.render(400, mesg));
    }

    public static Result _internalServerError(Throwable t) {
        // t.printStackTrace();
        return internalServerError(ix.ginas.views.html.error.render(500, "Internal server error: " + t.getMessage()));
    }

    public static Result authenticate() {
        return ok("You're authenticated!");
    }

    public static CV getCV() {
        return new CV();
    }

    public static String translateFacetName(String n) {
        if ("SubstanceStereoChemistry".equalsIgnoreCase(n))
            return "Stereochemistry";
        if ("root_lastEdited".equalsIgnoreCase(n))
            return "Last Edited Period";
        if ("root_approved".equalsIgnoreCase(n))
            return "Last Validated";
        if ("LyChI_L4".equalsIgnoreCase(n)) {
            return "Structure Hash";
        }
        if ("GInAS Tag".equalsIgnoreCase(n)) {
            return "Source Tag";
        }
        if ("Approved By".equalsIgnoreCase(n)) {
            return "Validated By";
        }
        if ("Substance Class".equalsIgnoreCase(n)) {
            return "Substance Type";
        }
        if("root_lastEditedBy".equalsIgnoreCase(n)){
            return "Last Edited By";
        }
        return n.trim();
    }

    static FacetDecorator[] decorate(Facet... facets) {
        return Arrays.stream(facets).map(GinasFacetDecorator::new).filter(fd -> !fd.isHidden())
        		.sorted((f1,f2)->{
        			
        			return f2.getFacet().getSelectedLabels().size()-f1.getFacet().getSelectedLabels().size();
        		})
                .toArray(len -> new FacetDecorator[len]);
    }

    static class GinasFacetDecorator extends FacetDecorator {

        private static final Pattern RANGE_PATTERN = Pattern.compile("^[>]*[<]*[0-9][0-9]*[:]*[0-9]*$");
        private GinasFacetDecorator(Facet facet) {
            super(facet, true, 10);

            boolean isrange = true;
            // look for range filter to sort by value
            // uses regex now
            for (FV fv : facet.getValues()) {
//                if (!fv.getLabel().matches("^[>]*[<]*[0-9][0-9]*[:]*[0-9]*$")) {
                if(!RANGE_PATTERN.matcher(fv.getLabel()).matches()){
                    isrange = false;
                    break;
                }
            }
            if (isrange) {
                facet.sortLabels(false);
            }

        }

        /**
         * Return the Display Name for this facet
         *
         * @return
         */
        @Override
        public String name() {
            return translateFacetName(super.name());
        }

        /**
         * Return the label for the facet value at index i. Returning null
         * signals the UI not to show this facet.
         *
         * @param i
         * @return
         */
        @Override
        public String label(final int i) {
            final String label = super.label(i);
            final String name = super.name();

            if ("StructurallyDiverse".equalsIgnoreCase(label)) {
                return "Structurally Diverse";
            }
            if ("specifiedSubstanceG1".equalsIgnoreCase(label)) {
                return "Group 1 Specified Substance";
            }
            if ("protein".equalsIgnoreCase(label)) {
                return "Protein";
            }
            if ("nucleicAcid".equalsIgnoreCase(label)) {
                return "Nucleic Acid";
            }
            if ("polymer".equalsIgnoreCase(label)) {
                return "Polymer";
            }
            if ("mixture".equalsIgnoreCase(label)) {
                return "Mixture";
            }
            if ("chemical".equalsIgnoreCase(label)) {
                return "Chemical";
            }
            if ("concept".equalsIgnoreCase(label)) {
                return "Concept";
            }
            if (this.name().equalsIgnoreCase("Relationships")) {
                if (label.equals(Relationship.ACTIVE_MOIETY_RELATIONSHIP_TYPE)) {
                    return null;
                }
            }
            // TODO: Need to figure out CV mechanism in general
            if (label.contains("->")) {

                String s1 = GinasApp.getCV().getDisplay("RELATIONSHIP_TYPE", label);
                if (s1.contains("->")) {
                    return s1.split("->")[1] + " of " + s1.split("->")[0];
                } else {
                    return s1;
                }
            }

            if ("EP".equalsIgnoreCase(label)) {
                return "PH. EUR";
            }
            if("Record Status".equals(name())){
	            if (Substance.STATUS_APPROVED.equalsIgnoreCase(label)) {
	                return "Validated (" + GinasUtils.getApprovalIdGenerator().getName() + ")";
	            }
	            if ("non-approved".equalsIgnoreCase(label)) {
	                return "Non-Validated";
	            }
            }
            if (name.equalsIgnoreCase("root_approved") || name.equalsIgnoreCase("root_lastEdited")){
                return label.substring(1); // skip the prefix character
            }

            return label;
        }

        public boolean isHidden() {
            for (int i = 0; i < this.size(); i++) {
                if (this.label(i) != null)
                    return false;
            }
            return true;
        }
    }

    public static <T> Collection<T> limitList(Collection<T> list, int max) {
        return list.stream().limit(max).collect(Collectors.toList());
    }

    public static Collection<List<Code>> getGroupedCodes(List<Code> codes, int max) {
        LinkedHashMap<String, List<Code>> cmap = codes.stream()
                .collect(Collectors.groupingBy(c -> c.codeSystem, LinkedHashMap::new, Collectors.toList()));
        if (max > 0) {
            return limitList(cmap.values(), max);
        }

        return cmap.values();
    }

    /**
     * Get a Collection of lists of codes, ordered by preference set in
     * configuration.
     *
     * @param s
     * @param max
     * @return
     */
    public static Collection<List<Code>> getOrderedGroupedCodes(Substance s, int max) {
        return getGroupedCodes(s.getOrderedCodes(), max);
    }

    public static String getFirstOrElse(String[] s, String def) {
        if (s == null || s.length == 0)
            return def;
        return s[0];
    }

    @BodyParser.Of(value = BodyParser.FormUrlEncoded.class, maxLength = 50_000)
    public static Result sequenceSearch() {

        if (request().body().isMaxSizeExceeded()) {
            return badRequest("Sequence is too large!");
        }

        Map<String, String[]> params = request().body().asFormUrlEncoded();

        String[] values = params.get("sequence");
        String ident = getFirstOrElse(params.get("identity"), "0.5");
        String identType = getFirstOrElse(params.get("identityType"), "SUB");
        String wait = getFirstOrElse(params.get("wait"), null);
        String seqType = getFirstOrElse(params.get("seqType"), "Protein");

        if (values != null && values.length > 0) {
            String seq = values[0];
            try {
                Payload payload = _payload.get().createPayload("Sequence Search", "text/plain", seq);
                Call call = routes.GinasApp.substances(payload.id.toString(), 16, 1);
                return redirect(call.url() + "&type=sequence" + "&identity=" + ident + "&identityType=" + identType
                        + "&seqType="+seqType
                        + ((wait != null) ? "&wait=" + wait : ""));
            } catch (Exception ex) {
                Logger.error("Sequence search failed", ex);
                return _internalServerError(ex);
            }
        }

        return badRequest("Invalid \"sequence\" parameter specified!");
    }

    @BodyParser.Of(value = BodyParser.FormUrlEncoded.class, maxLength = 50_000)
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
                Logger.error("Structure search call error", ex);
                return _internalServerError(ex);
            }
        }

        return badRequest("Invalid \"q\" parameter specified!");
    }





    @Dynamic(value = IxDynamicResourceHandler.IS_ADMIN, handler = ix.ncats.controllers.security.IxDeadboltHandler.class)
    public static Result admin() {
        return ok(ix.ginas.views.html.admin.admin.render());
    }

    @Dynamic(value = IxDynamicResourceHandler.IS_USER_PRESENT, handler = ix.ncats.controllers.security.IxDeadboltHandler.class)
    public static Result profile() {

        Principal user = ix.ncats.controllers.auth.Authentication.getUser();

        return ok(ix.ginas.views.html.admin.profile.render(user));
    }


    @ApiOperation(value = "get All Subsatnces",
            notes = "Returns List of all Substances",
            response = Substance.class,
            httpMethod = "GET")
    @Dynamic(value = IxDynamicResourceHandler.CAN_SEARCH, handler = ix.ncats.controllers.security.IxDeadboltHandler.class)
    public static F.Promise<Result> substances(final String q, final int rows, final int page) {
        String type = request().getQueryString("type");
        Logger.debug("Substances: rows=" + rows + " page=" + page);
        SearchType stype = SearchType.valueFor(type);


//        return F.Promise.promise( ()-> {
        try {
            if (stype.isStructureSearch()) {
                String cutoff = request().getQueryString("cutoff");
                try {
                    Structure qStructure = getStructureFrom(q);
                    flash("qStructureID", qStructure.id.toString());
                    switch (stype) {
                        case SUBSTRUCTURE:
                            return substructure(qStructure.molfile, rows, page);
                        case SIMILARITY:
                            double thres = Math.max(.3, Math.min(1., Double.parseDouble(cutoff)));
                            return similarity(qStructure.molfile, thres, rows, page);
                        case FLEX:
                            return structureHashMatch(qStructure.molfile, rows, page, false);
                        case EXACT:
                            return structureHashMatch(qStructure.molfile, rows, page, true);
                        default:
                            return substructure(qStructure.molfile, rows, page);
                    }
                } catch (Exception e) {
                    Logger.error(e.getMessage(), e);
                }
                return  F.Promise.promise(() -> (Result) notFound(ix.ginas.views.html.error.render(400, "Invalid search parameters: type=\"" + type
                        + "\"; q=\"" + q + "\" cutoff=\"" + cutoff + "\"!")));
            } else if (stype.isSequenceSearch()) {
                return sequences(q, rows, page);
            } else {
                return _substances(q, rows, page);
            }
        } catch (Exception ex) {
            Logger.error(ex.getMessage(), ex);
            return F.Promise.promise( () -> _internalServerError(ex));
        }
//        });
    }

    /**
     * May contain special streams for use in tests / caching or example
     * downloads
     *
     *
     */
    private static Map<String, Supplier<Stream<Substance>>> specialStreams = new ConcurrentHashMap<>();

    /**
     * Register a supplier to a stream for a given collection ID. This can be
     * used to specify an explicit collection for reporting,
     *
     *
     * @param collectionId
     *            "key" value to be stored to fetch this stream
     * @param supplier
     *            a supplier of the stream
     */
    public static void registerSpecialStream(String collectionId, Supplier<Stream<Substance>> supplier) {
        specialStreams.put(collectionId, supplier);
    }

    /**
     * Differed method for generating a export. This returns a JSONode of the
     * meta data surrounding a would-be export request.
     *
     * @param collectionID
     * @param extension
     * @return
     */

    public static Result generateExportFileUrl(String collectionID, String extension, int publicOnly) {
        try{
            ObjectNode on = generateExportMetaDataUrlForApi(collectionID, extension, publicOnly);
            return ok(on);
        }catch(Exception e){
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public static ObjectNode generateExportMetaDataUrlForApi(String collectionID, String extension, int publicOnly) {
            ObjectNode on = EntityMapper.FULL_ENTITY_MAPPER().createObjectNode();
            String url = routes.GinasApp.export(collectionID, extension, publicOnly).url();
            on.put("url", url);

            on.put("isReady", factoryPlugin.get().isReady());


            boolean publicOnlyBool = publicOnly == 1;
            Principal prof = UserFetcher.getActingUser(true);
//            System.out.println("Getting url for:" + prof.username);
            ExportMetaData emd=new ExportMetaData(collectionID, null, prof, publicOnlyBool, extension);

            String username=emd.username;
            Optional<ExportMetaData> existing= new ExportProcessFactory().getMetaForLatestKey(username, emd.getKey());

            if(existing.isPresent()){
                on.put("isCached", true);
                on.put("cached", EntityWrapper.of(existing.get()).toFullJsonNode());
            }else{
                on.put("isCached", false);
            }

            try {
                // make sure it's present
                getExportStream(collectionID);
                on.put("isPresent", true);
            } catch (Exception e) {
                Logger.error(e.getMessage(), e);
            e.printStackTrace();
                on.put("isPresent", false);
                on.put("isReady", false);
            }
        return on;
    }

    /**
     * Return a stream for the given collectionID, or throw a
     * NoSuchElementException if none is found
     *
     * @param collectionID
     * @return
     */
    public static Stream<Substance> getExportStream(String collectionID) throws NoSuchElementException {
        Supplier<Stream<Substance>> sstream = specialStreams.getOrDefault(collectionID, () -> {
            SearchResultContext src = SearchResultContext.getSearchResultContextForKey(collectionID);
            if (src == null) {
                throw new NoSuchElementException("No search result context by that key");
            }
            return src.getResults().stream();
        });
        return sstream.get();
    }

    /**
     * Direct method, given collection ID, to return a File-Download result.
     *
     * @param collectionID
     *            The id of the collection (typically the SearchResultContext
     *            key)
     * @param extension
     *            The format extension (e.g. sdf, csv, etc)
     * @return
     */
    public static F.Promise<Result> export(String collectionID, String extension, int publicOnlyFlag) {
        return export(collectionID, extension, publicOnlyFlag, ()->getExportStream(collectionID));
    }
    /**
     * Direct method, given collection ID, to return a File-Download result.
     *
     * @param collectionID
     *            The id of the collection (typically the SearchResultContext
     *            key)
     * @param extension
     *            The format extension (e.g. sdf, csv, etc)
     * @return
     */
    public static F.Promise<Result> export(String collectionID, String extension, int publicOnlyFlag, Supplier<Stream<Substance>> streamSupplier) {

        return F.Promise.promise(() -> {
            String fname= request().getQueryString("filename");
            String qgen = request().getQueryString("genUrl");
            return exportDirect(collectionID, extension, publicOnlyFlag, streamSupplier, fname, qgen);
        });
    }

    public static Result exportDirect(String collectionID, String extension, int publicOnlyFlag,
                                      Supplier<Stream<Substance>> streamSupplier, String fileName, String generatorUrl) {
            try {
                boolean publicOnlyBool = publicOnlyFlag == 1;


//                SearchResultContext src = SearchResultContext.getSearchResultContextForKey(collectionID);
//                if(src!=null){
//                   System.out.println(src.getAdapted(opt));
//                }

                //Dummy version for query

                Principal prof = UserFetcher.getActingUser(true);
            ExportMetaData emd=new ExportMetaData(collectionID, generatorUrl, prof, publicOnlyBool, extension);


                //Not ideal, but gets around user problem
            Stream<Substance> mstream = streamSupplier.get();

                //GSRS-699 REALLY filter out anything that isn't public unless we are looking at private data
                if(publicOnlyBool){
                    mstream = mstream.filter(s-> s.getAccess().isEmpty());
                }

                Stream<Substance> effectivelyFinalStream = mstream;


            if(fileName!=null){
                emd.setDisplayFilename(fileName);
            }

            ExportProcess<Substance> p = new ExportProcessFactory().getProcess(emd,
                        () -> effectivelyFinalStream);

                p.run(out -> Unchecked.uncheck(() -> getSubstanceExporterFor(extension, out, publicOnlyBool)));

                return ok(EntityWrapper.of(p.getMetaData()).toFullJsonNode());
            } catch (Exception e) {
                e.printStackTrace();
                Logger.error(e.getMessage(), e);
            return GsrsApiUtil.notFound( e.getMessage());
            }
    }


    //public static InputStream download(String username, String collectionId, String extension, boolean publicOnly) throws IOException{
    @Dynamic(value = IxDynamicResourceHandler.IS_USER_PRESENT, handler = ix.ncats.controllers.security.IxDeadboltHandler.class)
    public static F.Promise<Result> downloadExport(String downloadID){
        return F.Promise.promise(() -> {
            try {
                String username=UserFetcher.getActingUser(true).username;
                String filename=request().getQueryString("filename");
//                System.out.println("here");
                Optional<ExportMetaData>emeta = ExportProcessFactory.getStatusFor(username, downloadID);
                ExportMetaData data=emeta.get();

                if(filename==null){
                    filename=data.getDisplayFilename();
                }

                InputStream in= ExportProcessFactory.download(username, data.getFilename());
                response().setContentType("application/x-download");
                response().setHeader("Content-disposition", "attachment; filename=" + filename);
                return ok(in);
            } catch (Exception e) {
                Logger.error(e.getMessage(), e);
                e.printStackTrace();
                return GsrsApiUtil.notFound( e.getMessage());
            }
        });
    }

    @Dynamic(value = IxDynamicResourceHandler.IS_USER_PRESENT, handler = ix.ncats.controllers.security.IxDeadboltHandler.class)
    public static F.Promise<Result> cancelExport(String downloadID){
        return F.Promise.promise(() -> {
            try {
                String username=UserFetcher.getActingUser(true).username;
                Optional<ExportMetaData>emeta = ExportProcessFactory.getStatusFor(username, downloadID);
                ExportMetaData data=emeta.get();
                if(data.isComplete()){
                    throw new IllegalStateException("Can not cancel a completed export.");
                }
                data.cancel();
                return ok(EntityWrapper.of(data).toFullJsonNode());
            } catch (Exception e) {
                Logger.error(e.getMessage(), e);
                return GsrsApiUtil.notFound( e.getMessage());
            }
        });
    }

    @Dynamic(value = IxDynamicResourceHandler.IS_USER_PRESENT, handler = ix.ncats.controllers.security.IxDeadboltHandler.class)
    public static Result deleteDownload(String downloadID){
            try {
                String username = UserFetcher.getActingUser(true).username;
                Optional<ExportMetaData> emeta = ExportProcessFactory.getStatusFor(username, downloadID);
                if(!emeta.isPresent()){
                    return GsrsApiUtil.notFound("could not find download " + downloadID);
                }
                ExportMetaData data = emeta.get();
                if (!data.isComplete()) {
                    return GsrsApiUtil.badRequest("Can not delete an unfinished export.");
                }

                ExportProcessFactory.remove(data);
                return GsrsApiUtil.deleted("download deleted");
            }catch(Exception e){
                Logger.error(e.getMessage(), e);
                return error(500, e.getMessage());
            }
    }
    @Dynamic(value = IxDynamicResourceHandler.IS_USER_PRESENT, handler = ix.ncats.controllers.security.IxDeadboltHandler.class)
    public static F.Promise<Result> removeExport(String downloadID){
        return F.Promise.promise(() -> {
            try {
                String username=UserFetcher.getActingUser(true).username;
                Optional<ExportMetaData>emeta = ExportProcessFactory.getStatusFor(username, downloadID);
                ExportMetaData data=emeta.get();
                if(!data.isComplete()){
                    throw new IllegalStateException("Can not delete an unfinished export.");
                }

                ExportProcessFactory.remove(data);

                ObjectMapper mapper = EntityFactory.EntityMapper.FULL_ENTITY_MAPPER();
                ObjectNode node = mapper.createObjectNode();
                node.put("message", "removed export");
                return ok(node);
            } catch (Exception e) {
                Logger.error(e.getMessage(), e);
                return GsrsApiUtil.notFound( e.getMessage());
            }
        });
    }
    @Dynamic(value = IxDynamicResourceHandler.IS_USER_PRESENT, handler = ix.ncats.controllers.security.IxDeadboltHandler.class)
    public static Result getDownloadRecordAsJson(String downloadId) {
        String username=UserFetcher.getActingUser(true).username;
        Optional<ExportMetaData>emeta = ExportProcessFactory.getStatusFor(username, downloadId);
        if(emeta.isPresent()){
            return Results.ok(EntityMapper.FULL_ENTITY_MAPPER().toJson(emeta.get()))
                            .as("application/json");
        }
        return GsrsApiUtil.notFound("could not find download " + downloadId);
    }
    @Dynamic(value = IxDynamicResourceHandler.IS_USER_PRESENT, handler = ix.ncats.controllers.security.IxDeadboltHandler.class)
    public static JsonNode getDownloadsAsJson(int rows,int page, String key) {


                String username=UserFetcher.getActingUser(true).username;

                List<ExportMetaData>list = ExportProcessFactory.getExplicitExportMetaData(username);

                if(key!=null){
                    list=list.stream()
                            .filter(m->m.getKey().equals(key))
                            .sorted(Comparator.comparing(m -> m.finished))
                            .collect(Collectors.toList());
                }

                List<ExportMetaData> data =  getPagedDownloads(list,rows,page);
        DownloadResultPage result = new DownloadResultPage();
        result.downloads = data;
        result.page =page;
        result.row = rows = Math.min(list.size(), Math.max(1, rows));

                return EntityMapper.FULL_ENTITY_MAPPER().valueToTree(result);

    }

    public static class DownloadResultPage{
       public List<ExportMetaData> downloads;
       public int page;
       public int row;

    }


    @Dynamic(value = IxDynamicResourceHandler.IS_USER_PRESENT, handler = ix.ncats.controllers.security.IxDeadboltHandler.class)
    public static F.Promise<Result> downloadsView(int rows,int page){
        return F.Promise.promise(() -> {
            try {
                String key=request().getQueryString("q");

                String username=UserFetcher.getActingUser(true).username;

                List<ExportMetaData>list = ExportProcessFactory.getExplicitExportMetaData(username);

                if(key!=null){
                    list=list.stream()
                            .filter(m->m.getKey().equals(key))
                            .sorted(Comparator.comparing(m -> m.finished))
                            .collect(Collectors.toList());
                }

                return createDownloadsResult(list,rows,page);

            } catch (Exception e) {
                Logger.error(e.getMessage(), e);
                return error(404, e.getMessage());
            }
        });
    }
    static Result createDownloadsResult(List<ExportMetaData> result, int rows,
                                        int page) {

        int[] pages = new int[0];
        if(!result.isEmpty()){
            pages = paging(rows, page, result.size());
        }
        List<ExportMetaData> jobs = getPagedDownloads(result, rows, page);

        return ok(ix.ginas.views.html.downloads.jobs.render(page, rows,
                result.size(), pages, new FacetDecorator[]{}, jobs));

    }
    public static List<ExportMetaData> getPagedDownloads(List<ExportMetaData> result, int rows,
                                        int page) {

        List<ExportMetaData> jobs = new ArrayList<ExportMetaData>();

        if (result.size() > 0) {
            rows = Math.min(result.size(), Math.max(1, rows));
            for (int i = (page - 1) * rows, j = 0; j < rows
                    && i < result.size(); ++j, ++i) {
                jobs.add(result.get(i));
            }
        }

        return jobs;

    }

    @Dynamic(value = IxDynamicResourceHandler.IS_USER_PRESENT, handler = ix.ncats.controllers.security.IxDeadboltHandler.class)
    public static F.Promise<Result> downloadView(String downloadID){
        return F.Promise.promise(() -> {
            String username=UserFetcher.getActingUser(true).username;
            Optional<ExportMetaData>emeta = ExportProcessFactory.getStatusFor(username, downloadID);

            if(emeta.isPresent()){

                return ok(ix.ginas.views.html.downloads.job.render(emeta.get()));
            }
            return error(404, "download file not found");
        });
    }

    @Dynamic(value = IxDynamicResourceHandler.IS_USER_PRESENT, handler = ix.ncats.controllers.security.IxDeadboltHandler.class)
    public static F.Promise<Result> getStatusFor(String downloadID){
        return F.Promise.promise(() -> {
            try {
                String username=UserFetcher.getActingUser(true).username;

                Optional<ExportMetaData>emeta = ExportProcessFactory.getStatusFor(username, downloadID);

                if(emeta.isPresent()){
                    return ok(EntityWrapper.of(emeta.get()).toFullJsonNode());
                }
                return GsrsApiUtil.notFound( "download file not found");
            } catch (Exception e) {
                Logger.error(e.getMessage(), e);
                return GsrsApiUtil.notFound(e.getMessage());
            }
        });
    }

    @Dynamic(value = IxDynamicResourceHandler.IS_USER_PRESENT, handler = ix.ncats.controllers.security.IxDeadboltHandler.class)
    public static F.Promise<Result> listDownloads(){
        return F.Promise.promise(() -> {
            try {
                String username=UserFetcher.getActingUser(true).username;

                List<ExportMetaData>list = ExportProcessFactory.getExplicitExportMetaData(username);

                return ok(EntityWrapper.of(list).toFullJsonNode());

            } catch (Exception e) {
                Logger.error(e.getMessage(), e);
                return error(404, e.getMessage());
            }
        });
    }

    /**
     * PipiedInputStream that allows probing into whether it's closed yet or
     * not.
     *
     * @author tyler
     *
     */
    public static class VisiblePipedInputStream extends PipedInputStream {
        private boolean isClosed = false;

        @Override
        public void close() throws IOException {
            isClosed = true;
            super.close();
        }

        public boolean isClosed() {
            return isClosed;
        }
    }

    /**
     * PipiedOutputStream that allows probing into whether it's closed yet or
     * not.
     *
     * @author tyler
     *
     */
    public static class VisiblePipedOutputStream extends PipedOutputStream {
        private boolean isClosed = false;

        public VisiblePipedOutputStream(PipedInputStream pis) throws IOException {
            super(pis);
        }

        @Override
        public void close() throws IOException {
            isClosed = true;
            super.close();
        }

        public boolean isClosed() {
            return isClosed;
        }
    }

    static OutputStream createOutputStreamForExport(String extension) throws IOException{
        String username =getUser().username;

        File userExportArea = new File(ConfigHelper.getOrDefault("path.to.export.root", ""), username);
        final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HHmm");
        File outputFile = File.createTempFile("export-" + sdf.format(new Date()) , extension,  userExportArea);
        //TODO should we buffer?
        return new FileOutputStream(outputFile);
    }


    /**
     * Directly export the provided stream, using an exporter that matches
     * extension
     *
     * @param tstream
     * @param extension
     * @return
     * @throws Exception
     */
    public static Result export(Stream<Substance> tstream, String extension, boolean publicOnly) throws Exception {

        Objects.requireNonNull(tstream, "Invalid stream");

        if (!factoryPlugin.get().isReady()) {
            throw new IllegalStateException("export thread pool is full");
        }

        final VisiblePipedInputStream pis = new VisiblePipedInputStream();
        final VisiblePipedOutputStream pos = new VisiblePipedOutputStream(pis);



        //OutputStream out = createOutputStreamForExport(extension);
        ExportListener listener = new ExportListener() {
            @Override
            public void exportStarted() {

            }

            @Override
            public void exportHalted(String reason) {

            }

            @Override
            public void exportHalted(Throwable reason) {

            }

            @Override
            public void exportCompleted() {

            }
        };
        Exporter<Substance> exporter = getSubstanceExporterFor(extension, pos, publicOnly);

//        LocalExportWriter writer = new LocalExportWriter(out, listener);
        final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HHmm");
        String fname = "export-"
                + sdf.format(new Date())
                + "." + extension;

        factoryPlugin.get().submit(() -> {
            try {
                listener.exportStarted();
                tstream.forEach(s -> {
                    try {
                        exporter.export(s);
                    } catch (Exception e) {
                        Logger.error("Erorr exporting:" + e.getMessage(), e);
                    }
                });
                // exporter.exportForEachAndClose(tstream.iterator());
            } catch (Exception e) {
                Logger.error("Erorr exporting:" + e.getMessage(), e);
            } finally {
                try {
                    exporter.close();
                } catch (Exception e) {
                    Logger.error("Error closing exporter:" + e.getMessage(), e);
                }
                try {
                    pos.close();
                } catch (Exception e) {
                    Logger.error("Error closing POS:" + e.getMessage(), e);
                }
            }
        });

        response().setContentType("application/x-download");
        response().setHeader("Content-disposition", "attachment; filename=" + fname);
        return ok(pis);
    }

    public static Exporter<Substance> getSubstanceExporterFor(String extension, OutputStream pos, boolean publicOnly)
            throws IOException {

        if (factoryPlugin.get() == null) {
            throw new NullPointerException("could not find a factory plugin");
        }

        SubstanceExporterFactory.Parameters params = new SubstanceParameters(
                factoryPlugin.get().getFormatFor(extension),publicOnly);

        SubstanceExporterFactory factory = factoryPlugin.get().getExporterFor(params);
        if (factory == null) {
            // TODO handle null couldn't find factory for params
            throw new IllegalArgumentException("could not find suitable factory for " + params);
        }
        return factory.createNewExporter(pos, params);
    }

    private static class SubstanceParameters implements SubstanceExporterFactory.Parameters {
        private final OutputFormat format;

        private final boolean publicOnly;
        SubstanceParameters(OutputFormat format, boolean publicOnly) {
            Objects.requireNonNull(format);
            this.format = format;
            this.publicOnly = publicOnly;
        }

        @Override
        public OutputFormat getFormat() {
            return format;
        }

        @Override
        public boolean publicOnly() {
            return publicOnly;
        }
    }

    private static double parseDoubleOrElse(String value, double defaultValue){
        if(value !=null){
            try {
                return Double.parseDouble(value);
            }catch(NumberFormatException ex){
                Logger.error("Bogus identity value: " + value);
            }
        }
        //if we get here either we didn't have a value or it errord out
        return defaultValue;
    }
    public static F.Promise<Result> sequences(final String q, final int rows, final int page) {
        double identity = parseDoubleOrElse(request().getQueryString("identity"),  0.5);

        return Workers.WorkerPool.DB_SIMPLE_READ_ONLY.newJob( () -> {
            String seq = GinasFactory.getSequence(q);
            if (seq != null) {
                Logger.debug("sequence: " + seq.substring(0, Math.min(seq.length(), 20)) + "; identity=" + identity);
                return _sequences(seq, identity, rows, page);
            }

            return internalServerError("Unable to retrieve sequence for " + q);
        }).toPromise();

    }

    public static Result _sequences(final String seq, final double identity, final int rows, final int page) {
        CutoffType ct = CutoffType.valueOfOrDefault(request().getQueryString("identityType"));
        String seqType = Optional.ofNullable(request().getQueryString("seqType")).orElse("Protein");
        ResultProcessor processor;
        if("Protein".equals(seqType)){
            processor = new GinasSequenceResultProcessor();
        }else{
            processor = new GinasNucleicSequenceResultProcessor();
        }
        try {
            SearchResultContext context = sequence(seq, identity, rows, page, ct,seqType , processor);

            return App.fetchResult(context, rows, page, new SubstanceResultRenderer());
        } catch (Exception ex) {
            Logger.error("Can't perform sequence search", ex);
        }

        return internalServerError(ix.ginas.views.html.error.render(500, "Unable to perform sequence search!"));
    }

    private static void instrumentSearchOptions(SearchOptions options, Map<String, String[]> params,
                                                Map<String, Function<LocalDateTime, LocalDateTime>> orderedMap){


        SearchOptions.FacetLongRange editedRange =
                new SearchOptions.FacetLongRange ("root_lastEdited");
        SearchOptions.FacetLongRange approvedRange =
                new SearchOptions.FacetLongRange ("root_approved");


        List<SearchOptions.FacetLongRange> facetRanges = new ArrayList<>();

        facetRanges.add(editedRange);
        facetRanges.add(approvedRange);

        LocalDateTime now = TimeUtil.getCurrentLocalDateTime();


        char leadingChar = 'A';
//        long start = TimeUtil.toMillis(midnightThisMorning);
//        long end = todayMillis;

        //1 second in future
        long end = TimeUtil.toMillis(now) + 1000L;


        LocalDateTime last = now;

        for(Map.Entry<String, Function<LocalDateTime, LocalDateTime>> entry : orderedMap.entrySet()){
            // add a single character prefix so as to keep the map sorted; the
            // decorator strips this out
            String name = leadingChar + entry.getKey();


            LocalDateTime startDate = entry.getValue().apply(now);
            long start = TimeUtil.toMillis(startDate);


            //This is terrible, and I hate it, but this is a
            //quick fix for the calendar problem.
            if(start>end){
                startDate = entry.getValue().apply(last);
                start = TimeUtil.toMillis(startDate);
            }


            long[] range = new long[]{start, end};



            if(end<start){
                System.out.println("How is this possible?");
            }

            for(SearchOptions.FacetLongRange facet : facetRanges){
                facet.add(name, range);
            }

            end = start;
            leadingChar++;
            last=startDate;
        }


        options.addLongRangeFacets(facetRanges);


        if(params!=null){

            String[] dep =params.get("showDeprecated");
            if(dep==null || dep.length<=0 || dep[0].equalsIgnoreCase("false")){
                options.addTermFilter("SubstanceDeprecated","false");
            }
        }
    }

    public static void instrumentSubstanceSearchOptions(SearchOptions options, Map<String, String[]> params) {

        //Note, this is not really the right terminology.
        //durations of time are better than actual cal. references,
        //as they don't behave quite as well, and may cause more confusion
        //due to their non-overlapping nature. This makes it easier
        //to have a bug, and harder for a user to understand.


        Map<String, Function<LocalDateTime, LocalDateTime>> map = new LinkedHashMap<>();
        map.put("Today", now -> LocalDateTime.of(now.toLocalDate(), LocalTime.MIDNIGHT));


        //(Last 7 days)
        map.put("This week", now -> {
            return now.minusDays(7);
//            TemporalField dayOfWeek = weekFields.dayOfWeek();
//            return LocalDateTime.of(now.toLocalDate(), LocalTime.MIDNIGHT)
//                    .with(dayOfWeek, 1);
        });

        //(Last 30 days)
        map.put("This month", now ->{
                    return now.minusDays(30);
//            LocalDateTime ldt=LocalDateTime.of(now.toLocalDate(), LocalTime.MIDNIGHT)
//                         .withDayOfMonth(1);
//            return ldt;
                }
        );

        //(Last 6 months)
        map.put("Past 6 months", now ->{
            return now.minusMonths(6);
//                LocalDateTime.of(now.toLocalDate(), LocalTime.MIDNIGHT)
//                        .minusMonths(6)
        });

        //(Last 1 year)
        map.put("Past 1 year", now ->{
            return now.minusYears(1);
//                return LocalDateTime.of(now.toLocalDate(), LocalTime.MIDNIGHT)
//                        .minusYears(1);
        });

        map.put("Past 2 years", now ->{
            return now.minusYears(2);
//            return LocalDateTime.of(now.toLocalDate(), LocalTime.MIDNIGHT)
//                        .minusYears(2);
        });

        //Older than 2 Years
        map.put("Older than 2 years", now ->now.minusYears(6000));

        instrumentSearchOptions(options, params, map);


    }

    public static List<Facet> getSubstanceFacets(int fdim, Map<String, String[]> map) throws IOException {
        SearchOptions options = new SearchOptions(Substance.class);
        options.setFdim(fdim);
        instrumentSubstanceSearchOptions (options, map);

        return getTextIndexer().search(options, null, null).getFacets();
    }

    public static SearchResult getSubstanceSearchResult(final String q, final int total) {
        final Map<String, String[]> params = App.getRequestQuery();

        //final String sha1 = App.getKeyForCurrentRequest();

        String[] order = params.get("order");

        // default to "lastEdited" as sort order
        if (order == null || order.length <= 0) {
            order = new String[] { DEFAULT_SEARCH_ORDER };
            params.put("order", order);
        }

        try {
            long start = System.currentTimeMillis();
            SearchRequest sr = new SearchRequest.Builder()
                                          .fdim(App.FACET_DIM)
                                          .withParameters(params)
                                          .top(total)
                                          .kind(Substance.class)
                                          .query(q)
                                          .build();
            
            final String sha1 = App.getBestKeyForCurrentRequest() + ((sr.getOptions().getFdim()!=App.FACET_DIM)?sr.getOptions().getFdim():"");
            
            

            SearchResult result = getOrElse(sha1, TypedCallable.of(() -> {
                try{
                	SearchOptions options = sr.getOptions();
                    instrumentSubstanceSearchOptions(options, params);
                    return cacheKey(sr.execute(), sha1);
                }catch(Exception e){
                    e.printStackTrace();
                    throw e;
                }
            } , SearchResult.class));
            Logger.debug(sha1 + " => " + result);

            double elapsed = (System.currentTimeMillis() - start) * 1e-3;
            //result could be null!
            if(result !=null){
            Logger.debug(String.format("Elapsed %1$.3fs to retrieve " + "search %2$d/%3$d results...", elapsed,
                    result.size(), result.count()));
            }
            return result;
        }catch (Exception ex) {
            Logger.error("Unable to perform search", ex);
            throw new IllegalStateException(ex);
        }
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
    static F.Promise<Result> _substances(String q, final int rows, final int page) throws Exception {
        final int total = Math.max(SubstanceFactory.getCount(), 1);
        final String key = "substances/" + Util.sha1(request());

        
        // final String[] searchFacets = facets;
        boolean forcesql = "true".equals(request().getQueryString("sqlOnly"));

        // if there's a provided query, or there's a facet specified,
        // do a text search

        if (!forcesql) {
            return  Workers.WorkerPool.CPU_INTENSIVE.newJob(() -> getSubstanceSearchResult(q, total))
                    .andThen(Workers.WorkerPool.DB_EXPENSIVE_READ_ONLY, (r ->  {
                        Logger.debug("_substance: q=" + q + " rows=" + rows + " page=" + page + " => " + r + " finished? "
                                + r.finished());
                        if (r.finished()) {
                            final String k = key + "/result";

                            return getOrElse(k, TypedCallable.of(() -> createSubstanceResult(r, rows, page), Result.class));
                        }
                        return createSubstanceResult(r, rows, page);
                    }))
                    .toPromise();


            // otherwise, just show the first substances
        } else {
            return Workers.WorkerPool.DB_EXPENSIVE_READ_ONLY.newJob( () ->
                            getOrElse(key, () -> {
                                SubstanceResultRenderer srr = new SubstanceResultRenderer();
                                List<Facet> defFacets = getSubstanceFacets(30, request().queryString());
                                int nrows = Math.max(Math.min(total, rows), 1);
                                int[] pages = paging(nrows, page, total);
                                List<Substance> substances = SubstanceFactory.getSubstances(nrows, (page - 1) * rows, null);
                                return srr.render(null, page, nrows, total, pages, defFacets, substances);
                            })
            ).toPromise();
        }

    }

    static Result createSubstanceResult(SearchResult result, int rows, int page) throws Exception {
        return fetchResultImmediate(result, rows, page, new SubstanceResultRenderer());
    }

    public static class SubstanceVersionFetcher extends GetResult {
        String version;

        public SubstanceVersionFetcher(String version) {
            this.version = version;
        }

        public Result getResult(List<Substance> e) throws Exception {
            List<Substance> slist = new ArrayList<Substance>();
            for (Substance s : e) {
                Substance s2 = SubstanceFactory.getSubstanceVersion(s.uuid.toString(), version);
                slist.add(s2);
            }
            return _getSubstanceResult(slist);
        }
    }

    public static final GetResult SubstanceResult = new GetResult();

    @Dynamic(value = IxDynamicResourceHandler.CAN_SEARCH, handler = ix.ncats.controllers.security.IxDeadboltHandler.class)
    static Result _getSubstanceResult(List<Substance> substances) throws Exception {
        // force it to show only one since it's possible that the provided
        // name isn't unique
        if (substances.size() == 1) {
            Substance substance = substances.iterator().next();
            Substance.SubstanceClass type = substance.substanceClass;
            switch (type) {
                case chemical:
                    return ok(ix.ginas.views.html.details.chemicaldetails.render((ChemicalSubstance) substance));
                case protein:
                    return ok(ix.ginas.views.html.details.proteindetails.render((ProteinSubstance) substance));
                case mixture:
                    return ok(ix.ginas.views.html.details.mixturedetails.render((MixtureSubstance) substance));
                case polymer:
                    return ok(ix.ginas.views.html.details.polymerdetails.render((PolymerSubstance) substance));
                case structurallyDiverse:
                    return ok(ix.ginas.views.html.details.diversedetails.render((StructurallyDiverseSubstance) substance));
                case specifiedSubstanceG1:
                    return ok(ix.ginas.views.html.details.group1details
                            .render((SpecifiedSubstanceGroup1Substance) substance));
                case concept:
                    return ok(ix.ginas.views.html.details.conceptdetails.render((Substance) substance));
                case nucleicAcid:
                    return ok(ix.ginas.views.html.details.nucleicaciddetails.render((NucleicAcidSubstance) substance));
                default:
                    return _badRequest("type not found");
            }
        } else { // rarely used
            SearchResult result;
            try (TextIndexer indexer = getTextIndexer().createEmptyInstance()) {
                for (Substance sub : substances) {
                    indexer.add(EntityWrapper.of(sub));
                }

                SearchRequest request = new SearchRequest.Builder().top(indexer.size()).kind(Substance.class)
                        .fdim(FACET_DIM).withRequest(request()).build();

                result = SearchFactory.search(indexer, request);
            }
            if (result.count() < substances.size()) {
                substances.clear();
                result.copyTo(substances, 0, result.count());
            }
            TextIndexer.Facet[] facets = filter(result.getFacets(), getSubstanceFacetsForActiveUser());

            return ok(ix.ginas.views.html.substances.render(1, result.count(), result.count(), new int[0],
                    decorate(facets), substances, null));

        }
    }

    public static Result substance(String name) {
        return SubstanceResult.get(name);
    }

    public static Result substanceVersion(String name, String version) {
        return new SubstanceVersionFetcher(version).get(name);
    }

    public static F.Promise<Result> similarity(final String query, final double threshold, int rows, int page) {
        return Workers.WorkerPool.CPU_INTENSIVE.newJob( () -> {
            try {
                SearchResultContext context = similarity(query, threshold, rows, page,
                        new StructureSearchResultProcessor());
                return fetchResult(context, rows, page, new SubstanceResultRenderer());
            } catch (Exception ex) {
                ex.printStackTrace();
                Logger.error("Can't perform similarity search: " + query, ex);
            }


            return internalServerError(
                    ix.ginas.views.html.error.render(500, "Unable to perform similarity search: " + query));
        }).toPromise();

    }

    public static F.Promise<Result> structureHashMatch(final String query, int rows, int page, boolean exact) {
        try {
            Structure struc2 = StructureProcessor.instrument(query, null, true); // don't
            // standardize
            String hash = struc2.getStereoInsensitiveHash();
            if (exact) {
                hash = "root_structure_properties_term:" + struc2.getExactHash();
            }
            return _substances(hash, rows, page);
        } catch (Exception e) {
            Logger.error("lychi match error", e);
        }
        return F.Promise.promise( () ->internalServerError(ix.ginas.views.html.error.render(500, "Unable to perform flex search: " + query)));
    }

    public static F.Promise<Result> substructure(final String query, final int rows, final int page) {
        return F.Promise.<Result>promise( () -> {
            try {
                SearchResultContext context = App.substructure(query, rows, page,
                        new StructureSearchResultProcessor());
                return App.fetchResult(context, rows, page, new SubstanceResultRenderer());
            } catch (BogusPageException ex) {
                return internalServerError(ix.ginas.views.html.error.render(500, ex.getMessage()));
            } catch (Exception ex) {
                ex.printStackTrace();
                Logger.error("Can't perform substructure search", ex);
            }
            return internalServerError(
                    ix.ginas.views.html.error.render(500, "Unable to perform substructure search: " + query));
        });
    }

    /**
     * proper permission should be checked here
     *
     * TP:
     *
     * This needs to be re-evaluated. It is possible for there to be duplicated
     * names, and there is no check here for this.
     *
     * While it's not as pretty, I'm defaulting to using the uuid or approvalID.
     *
     * Update: When is this called now? In constructing a URL probably?
     *
     */
    public static String getId(Substance substance) {
        return substance.getUuid().toString();
    }

    static class GetResult {

        // Get a rendered thing for a name
        public Result get(final String name) {
            try {
                final String key = Substance.class.getName() + "/" + name;
                List<Substance> e = getOrElse(key, () -> resolveName(name));
                if (e.isEmpty()) {
                    return _notFound("Unknown name: " + name);
                }
                return result(e);
            } catch (Exception ex) {
                Logger.error("Unable to generate Result for \"" + name + "\"", ex);
                return _internalServerError(ex);
            }
        }

        public Result result(final List<Substance> e) {
            try {
                final String key = Substance.class.getName() + "/result/" + Util.sha1(request());
                return getOrElse(key, () -> getResult(e));
            } catch (Exception ex) {
                throw new IllegalStateException(ex);
            }
        }

        public Result getResult(List<Substance> substances) throws Exception {
            try {
                return _getSubstanceResult(substances);
            } catch (Exception e) {
                e.printStackTrace();
                throw e;
            }
        }

        // abstract Result getResult(List<Substance> e) throws Exception;
    }

    // TODO: possible duplicate of substance factory method
    public static List<Substance> resolveName(String name) {
        Finder<UUID, Substance> finder = SubstanceFactory.finder.get();
        if (name == null) {
            return null;
        } else {
            try {
                UUID uuid = UUID.fromString(name);
                List<Substance> slist = new ArrayList<Substance>();
                slist.add(SubstanceFactory.getSubstance(uuid));
                return slist;
            } catch (Exception e) {
                // Not a UUID
            }
        }
        List<Substance> values = new ArrayList<Substance>();
        if (name.length() == 8) { // might be uuid
            values = finder.where().istartsWith("uuid", name).findList();
        }
        if (values.isEmpty()) {
            values = finder.where().ieq("approvalID", name).findList();
            if (values.isEmpty()) {
                values = finder.where().ieq("names.name", name).findList(); // this
                // is
                // a
                // problem
                // for
                // oracle
                if (values.isEmpty()) {
                    values = finder.where().ieq("codes.code", name).findList();// last
                    // resort..
                    
                    //lookup in ad-hoc list
                    if(values.isEmpty()){
                       values=AdHocNameResolver.getAdaptedRecordKey(name)
                                        .map(nk->resolveName(nk))
                                        .orElse(values);
                    }

                }
            }
        }
        if (values.size() > 1) {
            Logger.warn("\"" + name + "\" yields " + values.size() + " matches!");
        }
        return values;
    }

    public static Set<Keyword> getStructureReferences(GinasChemicalStructure s) {
        if (s.getReferences() != null)
            return s.getReferences();
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

    public static List<Integer> getSites(List<DisulfideLink> disulfides, int index) {
        return disulfides.stream().map(ds -> ds.getSites()).flatMap(s -> s.stream())
                .filter(s -> s.subunitIndex == index).map(s -> s.residueIndex).collect(Collectors.toList());
    }

    public static List<Integer> sitesInSubunit(List<Site> sites, int index) {
        return sites.stream().filter(s -> s.subunitIndex == index).map(s -> s.residueIndex)
                .collect(Collectors.toList());
    }

    // These should be somewhere else

    public static String getAAName(char aa) {
        ControlledVocabulary cv = ControlledVocabularyFactory.getControlledVocabulary("AMINO_ACID_RESIDUE");
        for (VocabularyTerm t : cv.terms) {
            if (t.value.equals(aa + "")) {
                return t.display;
            }
        }
        return "UNKNOWN";

    }

    public static String getNAName(char aa) {
        ControlledVocabulary cv = ControlledVocabularyFactory.getControlledVocabulary("NUCLEIC_ACID_BASE");
        for (VocabularyTerm t : cv.terms) {
            if (t.value.equals(aa + "")) {
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
                    Structure struc = StructureProcessor.instrument(payload, moieties, false); // don't
                    // standardize!
                    // we should be really use the PersistenceQueue to do this
                    // so that it doesn't block

                    // in fact, it probably shouldn't be saving this at all
                    if (payload.contains("\n") && payload.contains("M  END")) {
                        struc.molfile = payload;
                    }

                    StructureFactory.saveTempStructure(struc);

                    ArrayNode an = mapper.createArrayNode();
                    for (Structure m : moieties) {
                        // m.save();
                        StructureFactory.saveTempStructure(m);
                        ObjectNode on = mapper.valueToTree(m);
                        Amount c1 = Moiety.intToAmount(m.count);
                        JsonNode amt = mapper.valueToTree(c1);
                        on.set("countAmount", amt);
                        an.add(on);
                    }
                    node.put("structure", mapper.valueToTree(struc));
                    node.put("moieties", an);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                try {

                    Collection<StructuralUnit> o = PolymerDecode.DecomposePolymerSU(payload, true);
                    for (StructuralUnit su : o) {
                        Structure struc = StructureProcessor.instrument(su.structure, null, false);
                        // struc.save();
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

    /**
     * Simply delegates to {@link StructureFactory#getStructureFrom(String, boolean)}.
     * @param str
     * @return
     */
    public static Structure getStructureFrom(String str) {
        if(str==null)return null;
        return StructureFactory.getStructureFrom(str, true);
    }

    public static class StructureSearchResultProcessor
    extends SearchResultProcessor<StructureIndexer.Result, Substance> {
		int index;
		public static EntityInfo<ChemicalSubstance> chemMeta = EntityUtils.getEntityInfoFor(ChemicalSubstance.class);
		public static EntityInfo<MixtureSubstance> mixMeta = EntityUtils.getEntityInfoFor(MixtureSubstance.class);
		public static EntityInfo<Substance> subMeta = EntityUtils.getEntityInfoFor(Substance.class);
		public static EntityInfo<Modifications> modMeta = EntityUtils.getEntityInfoFor(Modifications.class);

		public StructureSearchResultProcessor() {
		    
		}
		
		@Override
		public Stream<Substance> map(StructureIndexer.Result result) {
			try{
				Substance r=instrument(result);
				if(r==null)return Stream.empty();
				boolean includePolymers = ConfigHelper.getBoolean("ix.ginas.structure.search.includePolymers", false);

				StreamConcatter<Substance> sstream = StreamUtil.with(Stream.of(r));

				if(!includePolymers){
					if(r instanceof PolymerSubstance){
						return Stream.empty();
					}
				}

				if(ConfigHelper.getBoolean("ix.ginas.structure.search.includeModifications", false)){
					//add modifications results as well
					//This is likely to be a source of slow-down
					//due to possibly missing indexes
					List<Modifications> modlist = (List<Modifications>)modMeta.getFinder()
							                                          .where()
							                                          .eq("structuralModifications.molecularFragment.refuuid", result.getId())
							                                          .findList();

					sstream = sstream.and(modlist.stream().map(m->{
								Substance ff=(Substance)subMeta.getFinder()
										.where()
										.eq("modifications_uuid", m.uuid)
										.findUnique();
								return ff;
								}
							   ));
				}


				if(ConfigHelper.getBoolean("ix.ginas.structure.search.includeMixtures", false)){
					//add mixture results as well
					List<Substance> mixlist = (List<Substance>)mixMeta.getFinder()
							                                          .where()
							                                          .eq("mixture.components.substance.refuuid", result.getId())
							                            .findList();
					sstream = sstream.and(mixlist.stream());
				}
				return sstream.stream();
			}catch(Exception e){
				Logger.error("error processing record", e);
				return Stream.empty();
			}
		}
		
		protected Substance instrument(StructureIndexer.Result r) throws Exception {
		
		    Key k = Key.of(subMeta, UUID.fromString(r.getId()));

		    EntityFetcher<Substance> efetch = k.getFetcher();
		    
		    Substance chem = efetch.call();

		    
		    
		    
		
		    if (chem != null) {
		        Map<String, Object> matchingContext = new HashMap<String,Object>();
		        
		        double similarity = r.getSimilarity();
		        Logger.debug(String.format("%1$ 5d: matched %2$s %3$.3f", ++index, r.getId(), r.getSimilarity()));
                Chemical mol = r.getMol();


//                int[] amap = r.getHits();
                int[] amap = new int[mol.getAtomCount()];
		        int i = 0, nmaps = 0;
		        for (Atom ma : mol.getAtoms()) {
		            amap[i] = ma.getAtomToAtomMap().orElse(0);
		            if (amap[i] > 0) {
		                ++nmaps;
		            }
		            ++i;
		        }
		        if (nmaps > 0) {
		            matchingContext.put("atomMaps", amap);
		        }
		        matchingContext.put("similarity", similarity);
		        //Util.debugSpin(100);
		        EntityWrapper<?> ew = EntityWrapper.of(chem);
		        IxCache.setMatchingContext(this.getContext(), ew.getKey(), matchingContext);
		    }
		    return chem;
		}
	}

    private static Pattern FASTA_FILE_PATTERN = Pattern.compile(">(.+)\\|.+");
    public static class GinasSequenceResultProcessor
            extends SearchResultProcessor<SequenceIndexer.Result, ProteinSubstance> {
        public GinasSequenceResultProcessor() {
        }

        @Override
        protected ProteinSubstance instrument(SequenceIndexer.Result r) throws Exception {
            ProteinSubstance protein=null;

            //I don't understand the logic here ...
            //I don't think this does what it's supposed to.
            //katzelda - needed for large fasta sequences
            //but need tests to confirm
            if(r.id.startsWith(">")){
                Matcher m = FASTA_FILE_PATTERN.matcher(r.id);
                if(m.find()){
                    String parentId = m.group(1);
                    ProteinSubstance sub = SubstanceFactory.protfinder.get().byId(UUID.fromString(parentId));
                    return sub;

                }
            }else {

                Optional<ProteinSubstance> proteinMatch = SubstanceFactory.getProteinSubstancesFromSubunitID(r.id);
                protein = !proteinMatch.isPresent() ? null : proteinMatch.get();
            }

            if (protein != null) {
                Key key=EntityWrapper.of(protein).getKey();
                Map<String,Object> added = IxCache.getMatchingContext(this.getContext(), key);
                if(added==null){
                    added=new HashMap<String,Object>();
                }
                List<SequenceIndexer.Result> alignments = (List<SequenceIndexer.Result>)
                        added.computeIfAbsent("alignments", f->new ArrayList<SequenceIndexer.Result>());
                alignments.add(r);
                //GSRS 1512 add target site info
                // this is the only place in the alignment code we have the aligned sequence
                //AND we know what subunit it belongs to
                UUID subunitUUID = UUID.fromString(r.id);
//                System.out.println("looking for subunit id = " + subunitUUID );
                Optional<Subunit> foundSubunit = protein.protein.getSubunits().stream()

                        .filter(sub-> Objects.equals(subunitUUID,sub.getUuid())).findAny();
//                System.out.println("found subunit ? ="+ foundSubunit);
                if(foundSubunit.isPresent()){
                    Subunit subunit = foundSubunit.get();
                    int index = subunit.subunitIndex==null? 0: subunit.subunitIndex;
//                    System.out.println("index = " + index);
                    Range.RangeAndCoordinateSystemToStringFunction function = (begin,end, ignored)-> index+"_"+begin + "-" +index+"_"+end;
                    r.alignments.stream().forEach(a->{
                            String shorthand = Ranges.asRanges(a.targetSites())
                                                                    .stream()
                                                                    .map(range-> range.toString(function, Range.CoordinateSystem.RESIDUE_BASED))
                                                .collect(Collectors.joining(";","Target Sites: ","\n\n"));
//                            System.out.println("short hand -  " + shorthand);
                        //this check is because sometimes we get here twice?
                        if(a.alignment!=null && !a.alignment.startsWith("Target")) {
                            a.alignment = shorthand + a.alignment;
                        }
//                            System.out.println("new alignment =\n"+a.alignment);
                    });



                }

                IxCache.setMatchingContext(this.getContext(), key, added);
            } else {
                Logger.warn("Can't retrieve protein for subunit " + r.id);
            }
            return protein;
        }
    }

    public static SequenceIndexer.Result getSeqAlignment(String context, Substance sub, String subunitID) {
        sub.setMatchContextFromID(context);
        List<SequenceIndexer.Result> alignment= sub.getMatchContextPropertyOr("alignments", new ArrayList<SequenceIndexer.Result>());

        return alignment.stream()
                .filter(a->subunitID.equals(a.id))
                .findFirst()
                .orElse(null);

    }

    public static Double getChemSimilarity(String context, Substance sub) {
        sub.setMatchContextFromID(context);
        return sub.getMatchContextPropertyOr("similarity", null);
    }

    static public Substance resolve(Relationship rel) {
        Substance relsub = null;
        try {
            relsub = SubstanceFactory.getFullSubstance(rel.relatedSubstance);
        } catch (Exception ex) {
            Logger.warn("Can't retrieve related substance " + "from relationship " + rel.getUuid());
        }
        return relsub;
    }

    static public List<Relationship> resolveRelationships(String uuid) {
        List<Relationship> resolved = new ArrayList<Relationship>();
        try {
            Substance sub = SubstanceFactory.finder.get().where().where().eq("uuid", uuid).findUnique();
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


    //TODO: When is this used?
    @Dynamic(value = IxDynamicResourceHandler.CAN_SEARCH, handler = ix.ncats.controllers.security.IxDeadboltHandler.class)
    static public Result relationships(String uuid) {
        List<Relationship> rels = resolveRelationships(uuid);
        ObjectMapper mapper = new ObjectMapper();
        return Java8Util.ok(mapper.valueToTree(rels));
    }

    /******************* MIXTURES *************************************************/

    public static List<Component> getComponentsByType(List<Component> components, String type) {
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


    private static Substance getSubstanceForSubstanceOrStructureID(UUID id){
        EntityInfo<Substance> subMeta =
                EntityUtils.getEntityInfoFor(Substance.class);
        try{
            Key skey=Key.of(subMeta, id);
            Substance s = (Substance) skey.getFetcher().call();
            if(s!=null)return s;
        }catch(Exception e){//nothing found
            e.printStackTrace();
        }

        Key skey=Key.of(Structure.class, id);
        try{
            Structure s = (Structure) skey.getFetcher().call();

            if(s!=null){
                if(s instanceof GinasChemicalStructure){
                    Substance csub =subMeta.getNativeSpecificFinder()
                            .where()
                            .eq("structure.id", id)
                            .findUnique();
                    if(csub!=null){
                        return csub;
                    }
                }
            }
        }catch(Exception e){
            e.printStackTrace();
            return null;
        }

        return null;
    }


    /**
     * Renders a chemical structure from structure ID atom map can be provided
     * for highlighting
     *
     * @param id
     * @param format
     * @param size
     * @param contextId
     * @return
     */
    @Dynamic(value = IxDynamicResourceHandler.CAN_SEARCH, handler = ix.ncats.controllers.security.IxDeadboltHandler.class)
    public static Result structure(final String id, final String format, final int size, final String contextId, final String version) {
    	UUID uuid;
        play.Logger.trace("in GinasApp.structure");
    	
    	if (!UUIDUtil.isUUID(id)) {
        	Optional<UUID> t= SubstanceFactory.resolveID(id);
        	if(t.isPresent()){
        		uuid=t.get();
        	}else{
        		return App.render(id, size);
        	}
        }else{
        	uuid = UUID.fromString(id);
        }

        String structureID = id;
        String atomMap = contextId;


        boolean history = (version!=null);

        Substance cs = getSubstanceForSubstanceOrStructureID(uuid);




        if(cs!=null){ //Can get a unique substance from the ID


            //History view
            if(history){
            	if(!cs.version.equals(version)){
	                cs=SubstanceFactory.getSubstanceVersion(cs.getUuid().toString(), version);
	                if(cs==null){
	                    return placeHolderImage(cs);
	                }
            	}else{
            		history=false;
            	}
            }


            //Highlighting
            cs.setMatchContextFromID(contextId);
            int[] amaps=cs.getMatchContextPropertyOr("atomMaps", null);
            if(amaps!=null){
                atomMap=Arrays
                        .stream(amaps)
                        .mapToObj(i->i+"")
                        .collect(Collectors.joining(","));
            }



            if(cs instanceof ChemicalSubstance){
                Structure struc = ((ChemicalSubstance)cs).structure;
                if(!history){
                    structureID=struc.id.toString();
                    return App.structure(structureID, format, size, atomMap);
                }else{
                    return App.structure(struc, format, size, atomMap);
                }
            }else{
            	if(cs instanceof PolymerSubstance){
            		Structure struc = ((PolymerSubstance)cs).polymer.displayStructure;
                    //System.out.println("Looking at structure:" + struc.id.toString());
                    //System.out.println("Found structure:" + struc.molfile);
                    if(!history){
                        structureID=struc.id.toString();
                        return App.structure(structureID, format, size, atomMap);
                    }else{
                        return App.structure(struc, format, size, atomMap);
                    }
            	}else{
            		return placeHolderImage(cs);
            	}
            }
        }else{  //Can't get a unique substance from the ID
            Result r1 = App.structure(id, format, size, atomMap);
            int httpStat = r1.toScala().header().status();
            if (httpStat == NOT_FOUND) {
                try {
                    Unit u = GinasFactory.findUnitById(id);
                    if (u != null) {
                        return App.render(u.structure, size);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return placeHolderImage(null);
            }else{
                return r1;
            }
        }
    }


    public static Result placeHolderImage(Substance s) {
        String placeholderFile = "polymer.svg";
        if (s != null) {
            switch (s.substanceClass) {
                case chemical:
                    placeholderFile = "chemical.svg";
                    break;
                case protein:
                    placeholderFile = "protein.svg";
                    break;
                case mixture:
                    placeholderFile = "mixture.svg";
                    break;
                case polymer:
                    placeholderFile = "polymer.svg";
                    break;
                case structurallyDiverse:
                    placeholderFile = "structurally-diverse.svg";
                    break;
                case concept:
                    placeholderFile = "concept.svg";
                    break;
                case nucleicAcid:
                    placeholderFile = "nucleic-acid.svg";
                    break;
                case specifiedSubstanceG1:
                    placeholderFile = "g1ss.svg";
                    break;
                default:
                    placeholderFile = "polymer.svg";
            }
        } else {
            placeholderFile = "noimage.svg";
        }

        try {
            InputStream is = Util.getFile(placeholderFile, "public/images/");
            response().setContentType("image/svg+xml");
            return ok(is);
        } catch (Exception e) {
            return _internalServerError(e);
        }

    }

    /**
     * Converts a structure of substance to a chemical structure format.
     * Warnings are put into the header at "EXPORT-WARNINGS"
     *
     * @param id
     * @param format
     * @param context
     * @return
     */
    @Dynamic(value = IxDynamicResourceHandler.CAN_SEARCH, handler = ix.ncats.controllers.security.IxDeadboltHandler.class)
    public static Result structureExport(final String id, final String format, final String context) {
        List<GinasProcessingMessage> messages = new ArrayList<GinasProcessingMessage>();

        Chemical c = null;
        Protein p = null;

        Substance s = SubstanceFactory.getSubstance(id);

        if (s == null) {
            Structure struc = StructureFactory.getStructure(id);
            c = struc.toChemical(messages);

        } else {
            c = s.toChemical(messages);
        }

        ObjectMapper om = new ObjectMapper();
        Logger.debug("SERIALIZED:" + om.valueToTree(messages).toString());
        response().setHeader("EXPORT-WARNINGS", om.valueToTree(messages).toString() + "___");
        try {
            if (format.equalsIgnoreCase("mol")) {
                return ok(formatMolfile(c.toMol()));
            } else if (format.equalsIgnoreCase("sdf")) {
                return ok(formatMolfile(c.toSd()));
            } else if (format.equalsIgnoreCase("smiles")) {
                return ok(c.toSmiles(new ChemFormat.SmilesFormatWriterSpecification()
                                            .setKekulization(ChemFormat.KekulizationEncoding.KEKULE)));
                //TODO add back CDX format?

//            } else if (format.equalsIgnoreCase("cdx")) {
//                return ok(c.export(Chemical.FORMAT_CDX));
            } else if (format.equalsIgnoreCase("fas")) {
                if (s instanceof ProteinSubstance) {
                    return ok(makeFastaFromProtein(((ProteinSubstance) s)));
                } else if (s instanceof NucleicAcidSubstance) {
                    return ok(makeFastaFromNA(((NucleicAcidSubstance) s)));
                } else {
                    throw new IllegalStateException("object can not be exported to FASTA");
                }
            } else {
                return _badRequest("unknown format:" + format);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return _badRequest(e.getMessage());
        }

    }

    private static String formatMolfile(String mol) throws Exception {
        String[] lines = mol.split("\n");
        lines[1] = " G-SRS " + lines[1];
        return String.join("\n", lines);
    }

    public static String makeFastaFromProtein(ProteinSubstance p) {
        StringBuilder sb = new StringBuilder();

        List<Subunit> subs = p.protein.getSubunits();
        Collections.sort(subs, new Comparator<Subunit>() {
            @Override
            public int compare(Subunit o1, Subunit o2) {
                return o1.subunitIndex - o2.subunitIndex;
            }
        });
        for (Subunit s : subs) {

            sb.append(">" + p.getBestId().replace(" ", "_") + "|SUBUNIT_" + s.subunitIndex + "\n");
            for (String seq : splitBuffer(s.sequence, 80)) {
                sb.append(seq + "\n");
            }
        }
        return sb.toString();
    }

    public static String makeFastaFromNA(NucleicAcidSubstance p) {
        String resp = "";
        List<Subunit> subs = p.nucleicAcid.getSubunits();
        Collections.sort(subs, new Comparator<Subunit>() {
            @Override
            public int compare(Subunit o1, Subunit o2) {
                return o1.subunitIndex - o2.subunitIndex;
            }
        });

        for (Subunit s : subs) {
            resp += ">" + p.getBestId().replace(" ", "_") + "|SUBUNIT_" + s.subunitIndex + "\n";
            for (String seq : splitBuffer(s.sequence, 80)) {
                resp += seq + "\n";
            }
        }
        return resp;
    }

    public static int totalSites(NucleicAcidSubstance sub, boolean includeEnds) {
        return sub.getTotalSites(includeEnds);
    }

    public static int totalSites(NucleicAcidSubstance sub) {
        return totalSites(sub, true);
    }

    public static Long getDateTime(Date d) {
        if (d == null)
            return null;
        return d.getTime();
    }

    public static String getSugarName(Sugar s) {
        return ControlledVocabularyFactory.getDisplayFor("NUCLEIC_ACID_SUGAR", s.getSugar());

    }

    public static String getLinkageName(Linkage s) {
        return ControlledVocabularyFactory.getDisplayFor("NUCLEIC_ACID_LINKAGE", s.getLinkage());
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

    public static Set<OutputFormat> getAllSubstanceExportFormats() {
        return Play.application().plugin(GinasSubstanceExporterFactoryPlugin.class).getAllSupportedFormats();
    }

    public static String getAsJson(Object o) {
        if (o == null) {
            return null;
        }
        ObjectMapper om = new ObjectMapper();
        return om.valueToTree(o).toString();
    }

    private static String updateKey = null;

    @Dynamic(value = IxDynamicResourceHandler.IS_ADMIN, handler = ix.ncats.controllers.security.IxDeadboltHandler.class)
    public synchronized static Result updateIndex(String key) {
        if (!GinasLoad.config.get().ALLOW_REBUILD) {
            return _badRequest(
                    "Cannot rebuild text index. Please ensure \"ix.ginas.allowindexrebuild\" is set to true");
        }
        if (updateKey == null) {
            updateKey = UUID.randomUUID().toString();
        }
        Call callMonitor = routes.GinasApp.updateIndex("_monitor");

        if (listener.isCurrentlyRunning()) {
            return ok(new Html(new StringBuilder("<h1>Updating indexes:</h1><pre>").append(listener.getMessage())
                    .append("</pre><br><a href=\"").append(callMonitor.url()).append("\">refresh</a>").toString()));
        } else {
            if (key == null || !updateKey.equals(key)) {
                Call call = routes.GinasApp.updateIndex(updateKey);
                return ok(new Html(new StringBuilder("<h1>Updated indexes:</h1><pre>").append(listener.getMessage())
                        .append("</pre><br><a href=\"").append(call.url())
                        .append("\">Rebuild Index (warning: will take some time)</a>").toString()));
            }

            Runnable r = () -> {
                try {
                    new ProcessExecutionService(5, 10).reindexAll(new MultiProcessListener(listener,
                            Play.application().plugin(TextIndexerPlugin.class).getIndexer(),
                            EntityPersistAdapter.getInstance().getProcessListener()));

                } catch (Exception e) {
                    e.printStackTrace();
                }
            };

            ForkJoinPool.commonPool().submit(r);
            updateKey = UUID.randomUUID().toString();

            return ok(new Html("<h1>Updating indexes:</h1><pre>Preprocessing ...</pre><br><a href=\""
                    + callMonitor.url() + "\">refresh</a>"));
        }
    }

    private static CachedSupplier<Boolean> isSingleSignOn = CachedSupplier.of(() -> {
        boolean isTrustHeaders = ConfigHelper.getBoolean("ix.authentication.trustheader", false);
        boolean allowNonAuth = ConfigHelper.getBoolean("ix.authentication.allownonauthenticated", true);
        return isTrustHeaders && !allowNonAuth;
    });

    public static boolean isSingleSignOn() {
        return isSingleSignOn.get();
    }

    public  interface ExportListener{
        void exportStarted();
        void exportHalted(String reason);
        void exportHalted(Throwable reason);
        void exportCompleted();
    }


    /**
     * Get the HTML for the list view of a given type.
     *
     * @param o
     * @param ctx
     * @return
     */
    public static <T> Html getListContentFor(T o, SearchResultContext ctx) {
        ResultRenderer<T> render = (ResultRenderer<T>) ListViewFinder.getRendererOrDefault(o.getClass(),
                (t, ct) -> {
                    return ix.ginas.views.html.list.conceptlist.render((Substance) t);
                });
        try {
            return render.render((T) o, ctx);
        } catch (Throwable e) {
        	e.printStackTrace();
            Logger.error(e.getMessage(), e);
            return Html.apply("<div class=\"col-md-3 thumb-col\">"
                    + ix.ginas.views.html.errormessage.render(CAN_T_DISPLAY_RECORD + e.getMessage()).body() + "</div>");
        }

    }

    /**
     * Get the HTML for the grid view of a given type.
     *
     * @param o
     * @param ctx
     * @return
     */
    public static <T> Html getGridContentFor(T o, SearchResultContext ctx) {
        ResultRenderer<T> render = (ResultRenderer<T>) ThumbViewFinder.getRendererOrDefault(o.getClass(),
                (t, ct) -> {
                    // default to substance concept
                    return ix.ginas.views.html.thumbs.conceptthumb.render((Substance) t);
                });
        try {
            return render.render((T) o, ctx);
        } catch (Throwable e) {
            Logger.error(e.getMessage(), e);
            return Html.apply("<div class=\"col-md-3 thumb-col\">"
                    + ix.ginas.views.html.errormessage.render(CAN_T_DISPLAY_RECORD + e.getMessage()).body() + "</div>");
        }
    }

    public static String siteShorthand(int subunitIndex, BitSet residues) {
        return residues.stream()
                .mapToObj(i -> new Site(subunitIndex, i + 1))
                .collect(ModelUtils.toShorthand());
    }


    public static Result index() {
        //sillyTest();
        return ok(ix.ginas.views.html.index.render());
    }

    public static Result app() {
        return index();
    }

    @Dynamic(value = IxDynamicResourceHandler.CAN_REGISTER, handler = ix.ncats.controllers.security.IxDeadboltHandler.class)
    public static Result register() {
        return ok(ix.ginas.views.html.register.render());
    }
    // ***************

    @Dynamic(value = IxDynamicResourceHandler.CAN_SEARCH, handler = ix.ncats.controllers.security.IxDeadboltHandler.class)
    public static Result sequence(String id, String seqType) {
        return ok(ix.ginas.views.html.sequence.render(id, seqType));
    }

    public static Result structuresearch(String q) {
        Structure s = getStructureFrom(q);
        String smol = null;
        if (s != null) {
            smol = s.molfile;
        }
        return ok(ix.ginas.views.html.structuresearch.render(smol));
    }

    @Dynamic(value = IxDynamicResourceHandler.CAN_SEARCH, handler = ix.ncats.controllers.security.IxDeadboltHandler.class)
    public static Result report() {
        return ok(ix.ginas.views.html.report.render());
    }

    @Dynamic(value = IxDynamicResourceHandler.CAN_REGISTER, handler = ix.ncats.controllers.security.IxDeadboltHandler.class)
    public static Result wizard(String kind) {
        Logger.info(kind);
        return ok(ix.ginas.views.html.wizard.render(kind, "{}", null));
    }

    @Dynamic(value = IxDynamicResourceHandler.CAN_APPROVE, handler = ix.ncats.controllers.security.IxDeadboltHandler.class)
    public static Result approve(String substanceId) {
        List<Substance> substances = SubstanceFactory.resolve(substanceId);

        try {
            if (substances.size() == 1) {
                Substance s = substances.get(0);
                s=SubstanceFactory.approve(s);
                String resp = "Substance approved with Approval ID: " + s.approvalID;
                return ok(ix.ginas.views.html.response.render(resp));
            }
            throw new IllegalStateException("More than one substance matches that term");
        } catch (Exception ex) {
            return _internalServerError(ex);
        }
    }

    // This won't typically work, as it will collide with existing CV
    public static Result loadCV() {
        ControlledVocabularyFactory.loadCVFile();
        return ok(ix.ginas.views.html.index.render());
    }

    @Dynamic(value = IxDynamicResourceHandler.IS_ADMIN, handler = ix.ncats.controllers.security.IxDeadboltHandler.class)
    public static JsonNode getLogFileListAsJsonNode() throws IOException{
        Path directory = Paths.get(".");

        LogFileWalker visitor = new LogFileWalker(directory);

        Files.walkFileTree(directory, visitor);

        Collections.sort(visitor.fileInfoList);
        return Json.toJson(visitor.fileInfoList);
    }
    @Dynamic(value = IxDynamicResourceHandler.IS_ADMIN, handler = ix.ncats.controllers.security.IxDeadboltHandler.class)
    public static String getLogFileListAsJson() throws IOException{
        return getLogFileListAsJsonNode().toString();
    }
    @Dynamic(value = IxDynamicResourceHandler.IS_ADMIN, handler = ix.ncats.controllers.security.IxDeadboltHandler.class)
    public static JsonNode getLogFileListAsJsonNode(String path) throws IOException{
        Path directory = Paths.get(path);

        LogFileWalker visitor = new LogFileWalker(directory);

        Files.walkFileTree(directory, visitor);

        Collections.sort(visitor.fileInfoList);
        return Json.toJson(visitor.fileInfoList);
    }
    @Dynamic(value = IxDynamicResourceHandler.IS_ADMIN, handler = ix.ncats.controllers.security.IxDeadboltHandler.class)
    public static String getLogFileListAsJson(String path) throws IOException{
        return getLogFileListAsJsonNode(path).toString();
    }

    private static class LogFileWalker extends SimpleFileVisitor<Path> {


        private final Path directory;

        List<LogFileInfo> fileInfoList = new ArrayList<>();

        private static CachedSupplier<java.util.function.Predicate<Path>> filter = CachedSupplier.of(()->{
            Set<String> blackList = new HashSet<String>(Play.application().configuration().getStringList("admin.panel.download.folderBlackList", Collections.emptyList()));
//            Set<String> whiteList = new HashSet<String>(Play.application().configuration().getStringList("admin.panel.download.folderWhiteList", Collections.emptyList()));


            return p -> {
                String relativePath = GINAS_ROOT.get().relativize(p.normalize().toAbsolutePath()).toString();
                return !blackList.contains(relativePath);


            };
        });

        public LogFileWalker(Path directory) {
            this.directory = directory;
        }

        @Override
        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {

            if(filter.get().test(dir)) {
                addToFileInfoList(dir);
                return FileVisitResult.CONTINUE;
            }
            return FileVisitResult.SKIP_SUBTREE;
        }

        public void addToFileInfoList(Path p) {
            File f = p.toFile();
            String relativePath = directory.relativize(p).toString();

            if(relativePath.isEmpty()){
                return;
            }
            LogFileInfo info = new LogFileInfo();
            info.id = relativePath;
            info.isDir = f.isDirectory();
            if(f.isDirectory()) {
                info.text = relativePath;
            }else{
                info.text = relativePath + " ( " + f.length() + " B)";
            }

            String parentRelativePath = directory.relativize(p.getParent()).toString();

            info.parent = parentRelativePath.isEmpty()? "#" : parentRelativePath;

            fileInfoList.add(info);
        }

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
            addToFileInfoList(file);
            return FileVisitResult.CONTINUE;
        }


    }

    public static class LogFileInfo implements Comparable<LogFileInfo>{
        public String id;
        public String parent;
        public String text;
        public boolean isDir;

        @Override
        public int compareTo(LogFileInfo o) {
            if(isDir && !o.isDir){
                return -1;
            }
            if(o.isDir && !isDir){
                return 1;
            }

            return  id.compareTo(o.id);

        }
    }


    @Dynamic(value = IxDynamicResourceHandler.IS_ADMIN, handler = ix.ncats.controllers.security.IxDeadboltHandler.class)
    public static Map ListLogFiles() {
        Path directory = Paths.get("./logs");
        Map<String, String> fileList = new TreeMap();

        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

            Files.list(directory).forEach(file -> {
                String modifiedDate = sdf.format(file.toFile().lastModified());
                String fileSize = Long.toString(file.toFile().length());
                String dateSize = modifiedDate + "size" + fileSize;
                fileList.put(file.toFile().getName(), dateSize );
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
        return fileList;
    }

    private static final CachedSupplier<Path> GINAS_ROOT = CachedSupplier.of(()->Paths.get(".").toAbsolutePath().normalize());

    @Dynamic(value = IxDynamicResourceHandler.IS_ADMIN, handler = ix.ncats.controllers.security.IxDeadboltHandler.class)
    public static Result downloadFile(String fName){

        Path root = new File(".").toPath().toAbsolutePath();
        try {
        File file = new File(fName);
            boolean exists = file.exists();
            if (!exists) {
                return GsrsApiUtil.notFound("could not find download " + fName);
            }
        //must be a subdirectory of ginas
        if(file.toPath().toAbsolutePath().startsWith(GINAS_ROOT.get())) {
                response().setContentType("application/x-download");
                response().setHeader("Content-disposition","attachment; filename=" + fName);

            return ok(file);
        }
        return forbidden("not allowed to access file : " + fName);
        }catch(Exception e){
            e.printStackTrace();
            throw e;
        }
    }

    public static Result createTestChemical(int count){
        String structure="C1CCCCC1";
        ChemicalSubstance chem = new ChemicalSubstance();
        chem.getOrGenerateUUID();
        //  chem.structure = (GinasChemicalStructure)getStructureFrom(structure);


        List<Reference> refList = new ArrayList<>();
        List<Name> nameList = new ArrayList<>();
        for(int i=0;i<count;i++){
            Reference r = new Reference();
            r.citation="Reference " + i;
            r.getOrGenerateUUID();
            r.docType = "SRS";
            refList.add(r);
        }

        for(int i=0; i<count; i++){
            Name n = new Name();
            n.name = "Test Name" +i;
            n.addReference(refList.get(i));
            nameList.add(n);
        }

        chem.names = nameList;
        chem.references = refList;

        JsonNode node = new ObjectMapper().valueToTree(chem);
        response().setContentType("application/x-download");
        response().setHeader("Content-disposition","attachment; filename=chemicaljson");
        //System.out.println("node:" + node);
        return ok(node);
    }
    public static class GinasNucleicSequenceResultProcessor
            extends SearchResultProcessor<SequenceIndexer.Result, NucleicAcidSubstance> {

        @Override
        protected NucleicAcidSubstance instrument(SequenceIndexer.Result r) throws Exception {
            NucleicAcidSubstance nuc= null;
//            if(r.id.startsWith(">")){
//                Matcher m = FASTA_FILE_PATTERN.matcher(r.id);
//                if(m.find()){
//                    String parentId = m.group(1);
//                    nuc = SubstanceFactory.nucfinder.get().byId(UUID.fromString(parentId));
//                }
//            }else {
                Optional<NucleicAcidSubstance> nucSubstance = SubstanceFactory.getNucleicAcidSubstancesFromSubunitID(r.id); // also slow

                nuc = !nucSubstance.isPresent() ? null : nucSubstance.get();
//            }

            if (nuc != null) {
                Key key=EntityWrapper.of(nuc).getKey();
                Map<String,Object> added = IxCache.getMatchingContext(this.getContext(), key);
                if(added==null){
                    added=new HashMap<>();
                }
                List<SequenceIndexer.Result> alignments = (List<SequenceIndexer.Result>)
                        added.computeIfAbsent("alignments", f->new ArrayList<SequenceIndexer.Result>());
                alignments.add(r);
                //GSRS 1512 add target site info
                // this is the only place in the alignment code we have the aligned sequence
                //AND we know what subunit it belongs to
                UUID subunitUUID = UUID.fromString(r.id);
//                System.out.println("looking for subunit id = " + subunitUUID );
                Optional<Subunit> foundSubunit = nuc.nucleicAcid.getSubunits().stream()
                                                .filter(sub-> Objects.equals(subunitUUID,sub.getUuid())).findAny();
                if(foundSubunit.isPresent()) {
                    Subunit subunit = foundSubunit.get();
                    int index = subunit.subunitIndex == null ? 0 : subunit.subunitIndex;
//                    System.out.println("index = " + index);
                    Range.RangeAndCoordinateSystemToStringFunction function = (begin, end, ignored) -> index + "_" + begin + "-" + index + "_" + end;
                    r.alignments.stream().forEach(a -> {
                        String shorthand = Ranges.asRanges(a.targetSites())
                                .stream()
                                .map(range -> range.toString(function, Range.CoordinateSystem.RESIDUE_BASED))
                                .collect(Collectors.joining(";", "Target Sites: ", "\n\n"));
//                            System.out.println("short hand -  " + shorthand);
                        //this check is because sometimes we get here twice?
                        if (a.alignment != null && !a.alignment.startsWith("Target")) {
                            a.alignment = shorthand + a.alignment;
                        }
//                            System.out.println("new alignment =\n"+a.alignment);
                    });
                }
                IxCache.setMatchingContext(this.getContext(), key, added);
            } else {
                Logger.warn("Can't retrieve nucleic for subunit " + r.id);
            }
            return nuc;
        }
    }
    public static Result getStaticIndexFile() {
    	if(Play.isProd()){
    		File staticFile = new File("conf/beta/index.html");
            return ok(staticFile, true).as("text/html");
    	}else{
    		File staticFile = new File("modules/ginas/conf/beta/index.html");
            return ok(staticFile, true).as("text/html");
    	}
    }

    public static Result getStaticClientAssets(String file) {
    	if(Play.isProd()){
    		File staticFile = new File("conf/beta/" + file);
	    if(staticFile.exists()){
		    if(file.contains(".html")){
		    	return ok(staticFile, true).as("text/html");
		    }else{
                return ok(staticFile);
		    }
            } else {
                return getStaticIndexFile();
            }
    	}else{
    		File staticFile = new File("modules/ginas/conf/beta/" + file);
            if(staticFile.exists()){
		    if(file.contains(".html")){
		    	return ok(staticFile, true).as("text/html");
		    }else{
                return ok(staticFile);
		    }
            } else {
                return getStaticIndexFile();
            }
    	}

    }
}
