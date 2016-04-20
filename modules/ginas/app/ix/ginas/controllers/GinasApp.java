package ix.ginas.controllers;

import be.objectify.deadbolt.java.actions.Dynamic;
import be.objectify.deadbolt.java.actions.SubjectPresent;
import gov.nih.ncgc.chemical.Chemical;
import ix.core.UserFetcher;
import ix.core.adapters.EntityPersistAdapter;
import ix.core.chem.StructureProcessor;
import ix.core.controllers.StructureFactory;
import ix.core.controllers.EntityFactory.FetchOptions;
import ix.core.controllers.search.SearchFactory;
import ix.core.models.*;
import ix.core.plugins.IxCache;
import ix.core.plugins.PayloadPlugin;
import ix.core.search.TextIndexer;
import static ix.core.search.TextIndexer.*;
import ix.core.search.SearchOptions;
import ix.core.search.TextIndexer.Facet;
import ix.ginas.controllers.v1.CV;
import ix.ginas.controllers.v1.ControlledVocabularyFactory;
import ix.ginas.controllers.v1.SubstanceFactory;
import ix.ginas.controllers.v1.SubstanceFactory.SubstanceFilter;
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
import ix.ginas.models.v1.Name;
import ix.ginas.models.v1.NucleicAcid;
import ix.ginas.models.v1.Polymer;
import ix.ginas.models.v1.PolymerSubstance;
import ix.ginas.models.v1.NucleicAcidSubstance;
import ix.ginas.models.v1.Protein;
import ix.ginas.models.v1.ProteinSubstance;
import ix.ginas.models.v1.Relationship;
import ix.ginas.models.v1.Site;
import ix.ginas.models.v1.Sugar;
import ix.ginas.models.v1.Unit;
import ix.ginas.models.v1.SpecifiedSubstanceGroup1Substance;
import ix.ginas.models.v1.StructuralModification;
import ix.ginas.models.v1.StructurallyDiverseSubstance;
import ix.ginas.models.v1.Substance;
import ix.ginas.models.v1.Subunit;
import ix.ginas.models.v1.VocabularyTerm;
import ix.ginas.utils.GinasProcessingMessage;
import ix.ginas.utils.GinasUtils;
import ix.ginas.utils.RebuildIndex;
import ix.ncats.controllers.App;
import ix.ncats.controllers.security.IxDynamicResourceHandler;
import ix.utils.Global;
import ix.utils.Util;


import play.Play;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;


import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.Callable;

import org.springframework.util.StringUtils;

import play.Logger;
import play.Play;
import play.db.ebean.Model;
import play.libs.F;
import play.libs.ws.WS;
import play.libs.ws.WSResponse;
import play.mvc.BodyParser;
import play.mvc.Call;
import play.mvc.Result;
import play.twirl.api.Html;
import tripod.chem.indexer.StructureIndexer;
import chemaxon.struc.MolAtom;
import controllers.Assets;
import ix.seqaln.SequenceIndexer;
import com.fasterxml.jackson.databind.ObjectMapper;

public class GinasApp extends App {
    static final Model.Finder<UUID, Substance> SUBFINDER =
        new Model.Finder<UUID, Substance>(UUID.class, Substance.class);

    // relationship finder
    static final Model.Finder<UUID, Relationship> RELFINDER =
        new Model.Finder<UUID, Relationship>(UUID.class, Relationship.class);
    
    public static final String[] CHEMICAL_FACETS = {
        "Record Status",
        "Substance Class", 
        "SubstanceStereochemistry", 
        "Molecular Weight",
        "LyChI_L4",
        "GInAS Tag"
    };
    
    public static final String[] PROTEIN_FACETS = {
        "Sequence Type",
        "Substance Class", 
        "Status" 
    };
    
    public static final String[] ALL_FACETS = {
        "Record Status",
        "Substance Class", 
        "SubstanceStereochemistry", 
        "Molecular Weight",
        "GInAS Tag", 
        "Molecular Weight",
        "Relationships",
        "Sequence Type", 
        "Material Class", 
        "Material Type",
        "Family", 
        "Parts", 
        "Code System",
        "Last Edited By",
        "modified", //"Last Edited Date",
        "Reference Type",
        "Approved By",
        "approved"         //"Approved Date"
    };

    static final PayloadPlugin _payload =
        Play.application().plugin(PayloadPlugin.class);

    static class SubstanceResultRenderer
        extends DefaultResultRenderer<Substance> {

        final String[] facets;
        SubstanceResultRenderer () {
            this (ALL_FACETS);
        }
        SubstanceResultRenderer (String[] facets) {
            this.facets = facets;
        }
        
        public Result render
            (SearchResultContext context, int page, int rows, int total,
             int[] pages, List<TextIndexer.Facet> facets,
             List<Substance> substances) {
            return ok(ix.ginas.views.html.substances.render
                      (page, rows, total, pages,
                       decorate(filter(facets, this.facets)),
                       substances, context.getId(), null));
        }
    }


    private static SubstanceReIndexListener listener = new SubstanceReIndexListener();


    @SuppressWarnings({ "unchecked", "rawtypes" })
    static <T> List<T> filter(Class<T> cls, List values, int max) {
        List<T> fv = new ArrayList<T>();
        for (Object v : values) {
            if (cls.isAssignableFrom(v.getClass())) {
                fv.add((T) v);
                if (fv.size() >= max)
                    break;
            }
        }
        return fv;
    }

    /**
     * return a field named type to get around scala's template reserved keyword
     */
    public static String getType(Object obj) {
        Class<? extends Object> cls = obj.getClass();
        String type = null;
        try {
            Field f = cls.getField("type");
            if (f != null)
                return (String) f.get(obj);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return type;
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
    
    public static String truncate(String name) {
        String trunc = "";
        if (name.length() <= 20) {
            return name;
        } else {
            String[] spl = name.split("\\s");
            // Logger.info("split "+ Arrays.deepToString(spl));
            for (int i = 0; i < spl.length; i++) {
                if (spl[i].length() <= 20) {
                    trunc += spl[i] + " ";
                } else {
                    trunc += spl[i].substring(0, 10) + "...";
                    // Logger.info("trunc split " +trunc);
                    return trunc;
                }
            }
        }
        // Logger.info("trunc " + trunc);
        return trunc.substring(0, 17) + "...";
    }

    public static CV getCV() {
        return new CV();
    }

    
    static FacetDecorator[] decorate(Facet... facets) {
        List<FacetDecorator> decors = new ArrayList<FacetDecorator>();
        // override decorator as needed here
        for (int i = 0; i < facets.length; ++i) {
            decors.add(new GinasFacetDecorator(facets[i]));
        }

        GinasFacetDecorator f = new GinasFacetDecorator
            (new TextIndexer.Facet("ChemicalSubstance"));
        f.hidden = true;
        decors.add(f);

        return decors.toArray(new FacetDecorator[0]);
    }

    static class GinasFacetDecorator extends FacetDecorator {
        GinasFacetDecorator(Facet facet) {
            super(facet, true, 10);
        }
        
        @Override
        public String name() {
            String n = super.name();
            if ("SubstanceStereoChemistry".equalsIgnoreCase(n))
                return "Stereochemistry";
            if ("modified".equalsIgnoreCase(n))
                return "Last Edited";
            if ("approved".equalsIgnoreCase(n))
                return "Last Approved";
            if("LyChI_L4".equalsIgnoreCase(n)){
                return "Structure Hash";
            }
            if("Substance Class".equalsIgnoreCase(n)){
                return "Substance Type";
            }
            return n.trim();
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
            if (label.contains("->"))
                return label.split("->")[1] + " of " + label.split("->")[0];
            
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

    @BodyParser.Of(value = BodyParser.FormUrlEncoded.class,
                   maxLength = 10_000)
    public static Result sequenceSearch () {

        if (request().body().isMaxSizeExceeded()) {

            return badRequest ("Sequence is too large!");
        }
        
        Map<String, String[]> params = request().body().asFormUrlEncoded();

        String[] values = params.get("sequence");
        if (values != null && values.length > 0) {
            String seq = values[0];
            try {
                Payload payload = _payload.createPayload
                    ("Sequence Search", "text/plain", seq);
                Call call = routes.GinasApp.substances
                (payload.id.toString(), 16, 1);
                return redirect (call.url()+"&type=sequence");
            }
            catch (Exception ex) {
                ex.printStackTrace();
                return _internalServerError (ex);
            }
        }
        
        return badRequest ("Invalid \"sequence\" parameter specified!");
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
        //System.out.println("Test");
        String type = request().getQueryString("type");
        Logger.debug("Substances: rows=" + rows + " page=" + page);

        try {
            if (type == null) {
            }
            else if (type.equalsIgnoreCase("sequence")) {
                // the query is the uuid of the payload
                return sequences (q, rows, page);
            }
            else if (type.toLowerCase().startsWith("sub")
                     || type.toLowerCase().startsWith("sim")) {
                // structure search
                String cutoff = request().getQueryString("cutoff");
                Logger.debug("Search: q=" + q + " type=" + type + " cutoff="
                             + cutoff);
                try {
                    if (type.toLowerCase().startsWith("sub")) {
                        return substructure(q, rows, page);
                    } else {
                        // cap the cutoff at .3.. there is no need to go lower,
                        // otherwise, you're up to no good
                        double thres = Math.max
                            (.3, Math.min(1.,Double.parseDouble(cutoff)));
                        return similarity(q, thres, rows, page);
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }

                return notFound(ix.ginas.views.html.error.render
                        (400, "Invalid search parameters: type=\""
                                + type + "\"; q=\"" + q + "\" cutoff=\""
                                + cutoff + "\"!"));
            }else if (type.equalsIgnoreCase("flex") || type.equalsIgnoreCase("exact")) {
               try {
                   if(type.equalsIgnoreCase("flex")){
                           return lychimatch(q, rows, page, false);
                   }else{
                           return lychimatch(q, rows, page, true);
                   }
               } catch (Exception ex) {
                   ex.printStackTrace();
               }
               return notFound(ix.ginas.views.html.error.render
                       (400, "Invalid search parameters: type=\""
                               + type + "\"; q=\"" + q + "\"!"));
            }

            return _substances (q, rows, page);
        }
        catch (Exception ex) {
            ex.printStackTrace();
            return _internalServerError(ex);
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
        try {
            SearchResultContext context = sequence
                (seq, identity, rows,
                 page, new GinasSequenceResultProcessor ());
            
            return App.fetchResult
                (context, rows, page, new SubstanceResultRenderer ());
        }
        catch (Exception ex) {
            ex.printStackTrace();
            Logger.error("Can't perform sequence search", ex);
        }
        
        return internalServerError
            (ix.ginas.views.html.error.render
             (500, "Unable to perform squence search!"));
    }

    public static void instrumentSubstanceSearchOptions
        (SearchOptions options) {
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
    }

    public static List<Facet> getSubstanceFacets (int fdim) throws IOException {
        SearchOptions options = new SearchOptions (Substance.class);
        options.fdim = fdim;
        instrumentSubstanceSearchOptions (options);
        return _textIndexer.search(options, null, null).getFacets();
    }

    public static SearchResult getSubstanceSearchResult
        (final String q, final int total) {
        final Map<String, String[]> params = App.getRequestQuery();     
        final String sha1 = signature (q, params);

        try {
            long start = System.currentTimeMillis();
            SearchResult result = getOrElse
                (sha1, new Callable<SearchResult>() {
                        public SearchResult call () throws Exception {
                            SearchOptions options = new SearchOptions
                            (Substance.class, total, 0, FACET_DIM);
                            options.parse(params);
                            instrumentSubstanceSearchOptions (options);
                            SearchResult result = _textIndexer.search
                            (options, q, null);
                            return cacheKey (result, sha1);
                        }
                    });
            Logger.debug(sha1+" => "+result);
            
            double elapsed = (System.currentTimeMillis() - start)*1e-3;
            Logger.debug(String.format("Elapsed %1$.3fs to retrieve "
                                       +"search %2$d/%3$d results...",
                                       elapsed, result.size(),
                                       result.count()));
            return result;
        }
        catch (Exception ex) {
            ex.printStackTrace();
            Logger.trace("Unable to perform search", ex);
        }
        return null;    
    }

    static Result _substances(final String q, final int rows, final int page) throws Exception{
        return _substances(q,rows,page,ALL_FACETS);
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
    static Result _substances(final String q, final int rows, final int page, final String[] facets)
        throws Exception {
        final int total = Math.max(SubstanceFactory.getCount(), 1);
        final String user=UserFetcher.getActingUser(true).username;
        final String key = "substances/" + Util.sha1(request());
        
        final String[] searchFacets = facets;
        // if there's a provided query, or there's a facet specified,
        // do a text search
        if (request().queryString().containsKey("facet") || q != null) {
            final TextIndexer.SearchResult result =
                getSubstanceSearchResult (q, total);
            Logger.debug("_substance: q=" + q + " rows=" + rows + " page="
                         + page + " => " + result + " finished? "
                         + result.finished());
            if (result.finished()) {
                final String k = key + "/result";
                
                return getOrElse(k, new Callable<Result>() {
                        public Result call() throws Exception {
                                Logger.debug("Cache missed: " + k);
                            return createSubstanceResult(result, rows, page, facets);
                        }
                    });
            }

            return createSubstanceResult(result, rows, page);
                        
            //otherwise, just show the first substances
        } else {
            return getOrElse(key, new Callable<Result>() {
                    public Result call() throws Exception {
                        Logger.debug("Cache missed: " + key);
                        TextIndexer.Facet[] facets = filter
                            (getSubstanceFacets (30), ALL_FACETS);
                        int nrows = Math.max(Math.min
                                             (total, Math.max(1, rows)), 1);
                        int[] pages = paging(nrows, page, total);

                        List<Substance> substances = SubstanceFactory
                            .getSubstances(nrows, (page - 1) * rows, null);

                        return ok(ix.ginas.views.html.substances.render
                                  (page, nrows, total, pages, decorate(facets),
                                   substances, null, null));
                    }
                });
        }
    }
    static Result createSubstanceResult(TextIndexer.SearchResult result,
            int rows, int page) throws Exception{
        return createSubstanceResult(result,rows,page,ALL_FACETS);
    }
    
    static Result createSubstanceResult(TextIndexer.SearchResult result,
                                        int rows, int page, String[] facets) throws Exception{
        SearchResultContext src= new SearchResultContext(result);
        return fetchResult (src, rows, page, new SubstanceResultRenderer (facets));
    }
    
    public static class SubstanceVersionFetcher extends GetResult<Substance>{
        String version;
        public SubstanceVersionFetcher(String version){
                super(Substance.class, SubstanceFactory.finder);
                this.version=version;
        }
        Result getResult(List<Substance> e) throws Exception{
                List<Substance> slist=new ArrayList<Substance>();
                for(Substance s:e){
                        Substance s2=SubstanceFactory.getSubstanceVersion(s.uuid.toString(),version);
                        slist.add(s2);
                }
                return _getSubstanceResult(slist);
        }
    }



    public static final GetResult<Substance> SubstanceResult =
            new GetResult<Substance>(Substance.class, SubstanceFactory.finder) {
                public Result getResult(List<Substance> substances) throws Exception {
                    return _getSubstanceResult(substances);
                }
            };

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
        else {
            try(TextIndexer indexer = _textIndexer.createEmptyInstance()) {
                for (Substance sub : substances)
                    indexer.add(sub);

                TextIndexer.SearchResult result = SearchFactory.search
                        (indexer, Substance.class, null, null, indexer.size(),
                                0, FACET_DIM, request().queryString());
                if (result.count() < substances.size()) {
                    substances.clear();
                    for (int i = 0; i < result.count(); ++i) {
                        substances.add((Substance) result.get(i));
                    }
                }
                TextIndexer.Facet[] facets = filter(result.getFacets(), ALL_FACETS);


                return ok(ix.ginas.views.html.substances.render
                        (1, result.count(), result.count(), new int[0],
                                decorate(facets), substances, null, null));
            }
        }
    }

    public static Result substance(String name) {
        return SubstanceResult.get(name);
    }

     public static Result substanceVersion(String name, String version) {
        return new SubstanceVersionFetcher(version).get(name);
    }

    static Result createChemicalResult(TextIndexer.SearchResult result,
                                       int rows, int page) {
        TextIndexer.Facet[] facets = filter(result.getFacets(), CHEMICAL_FACETS);

        List<Substance> chemicals = new ArrayList<Substance>();
        int[] pages = new int[0];
        if (result.count() > 0) {
            rows = Math.min(result.count(), Math.max(1, rows));
            pages = paging(rows, page, result.count());
            for (int i = (page - 1) * rows, j = 0; j < rows
                     && i < result.size(); ++j, ++i) {
                chemicals.add((Substance) result.get(i));
            }
        }

        return ok(ix.ginas.views.html.substances.render
                  (page, rows, result.count(), pages,
                   decorate(facets), chemicals, null, null));

    }


    public static Result similarity(final String query, final double threshold,
                                    int rows, int page) {
        try {
            SearchResultContext context = similarity
                (query, threshold, rows,
                 page, new GinasSearchResultProcessor());
            return fetchResult (context, rows, page,
                                new SubstanceResultRenderer (CHEMICAL_FACETS));
        }
        catch (Exception ex) {
            ex.printStackTrace();
            Logger.error("Can't perform similarity search: " + query, ex);
        }
        
        return internalServerError
            (ix.ginas.views.html.error.render
             (500, "Unable to perform similarity search: " + query));
    }
    
        public static Result lychimatch(final String query, int rows, int page, boolean exact) {
                try{
                        Structure struc2 = StructureProcessor.instrument(query, null, true); // don't standardize
                        String hash=null;
                        if(exact){
                                hash=struc2.getLychiv3Hash();
                        }else{
                                hash=struc2.getLychiv4Hash();
                        }
                        return _substances(hash,rows,page, CHEMICAL_FACETS);
                }catch(Exception e){
                        
                }
                return internalServerError
                    (ix.ginas.views.html.error.render
                     (500, "Unable to perform flex search: " + query));
        }

    public static Result substructure(final String query, final int rows,
                                      final int page) {
        try {
            SearchResultContext context = App.substructure
                (query, rows, page, new GinasSearchResultProcessor());
            
            return App.fetchResult
                (context, rows, page, 
                 new SubstanceResultRenderer (CHEMICAL_FACETS));
        } catch (Exception ex) {
            ex.printStackTrace();
            Logger.error("Can't perform substructure search", ex);
        }
        return internalServerError
            (ix.ginas.views.html.error.render
             (500, "Unable to perform substructure search: " + query));
    }



    /**
     * return the canonical/default substance id
     * 
     * This needs to be re-evaluated. It is possible for there to be duplicated
     * names, and there is no check here for this.
     * 
     * While it's not as pretty, I'm defaulting to using the uuid
     * 
     * 
     */
    public static String getId(Substance substance) {

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
         */

        return substance.getUuid().toString().substring(0, 8);
    }

    static abstract class GetResult<T extends Substance> {
        final Model.Finder<UUID, T> finder;
        final Class<T> cls;
        
        GetResult(Class<T> cls, Model.Finder<UUID, T> finder) {
            this.cls = cls;
            this.finder = finder;
        }

        public Result get(final String name) {
            try {
                long start = System.currentTimeMillis();
                final String key = cls.getName() + "/" + name;
                List<T> e = getOrElse(key, new Callable<List<T>>() {
                        public List<T> call() throws Exception {
                            Logger.debug("Cache missed: " + key);
                            return resolve(finder, name);
                        }
                    });
                double ellapsed = (System.currentTimeMillis() - start) * 1e-3;
                Logger.debug("Ellapsed time "
                             + String.format("%1$.3fs", ellapsed) + " to retrieve "
                             + e.size() + " matches for " + name);

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

        public Result result(final List<T> e) {
            try {
                final String key = cls.getName() + "/result/"
                    + Util.sha1(request());
                return getOrElse(key, new Callable<Result>() {
                        public Result call() throws Exception {
                            long start = System.currentTimeMillis();
                            Result r = getResult(e);
                            Logger.debug("Cache missed: " + key + "..."
                                         + (System.currentTimeMillis() - start) + "ms");
                            return r;
                        }
                    });
            } catch (Exception ex) {
                return _internalServerError(ex);
            }
        }

        abstract Result getResult(List<T> e) throws Exception;
    }
    
    public static <T extends Substance> List<T> resolve
        (Model.Finder<UUID, T> finder, String name) {
        if (name == null) {
            return null;
        }else{
                try{
                        UUID uuid = UUID.fromString(name);
                        List<T> slist=new ArrayList<T>();
                        slist.add((T)SubstanceFactory.getSubstance(uuid));
                        return slist;
                }catch(Exception e){
                        //Not a UUID
                }
        }
        List<T> values = new ArrayList<T>();
        if (name.length() == 8) { // might be uuid
            values = finder.where().istartsWith("uuid", name).findList();
            
        }

        if (values.isEmpty()) {
                System.out.println("looking for approvalID");
            values = finder.where().ieq("approvalID", name).findList();
            if (values.isEmpty()) {
                System.out.println("looking for name");
                values = finder.where().ieq("names.name", name).findList(); //this is a problem for oracle
                if (values.isEmpty()){ 
                        System.out.println("looking for codes");
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
        if(s.getReferences()!=null)
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
        ArrayList<Integer> subunit = new ArrayList<Integer>();
        for (StructuralModification sm : mod.structuralModifications) {
            subunit = siteIter(sm.getSites(), index);
        }
        return subunit;
    }

    public static List<Integer> getSites(Glycosylation mod, int index) {
        ArrayList<Integer> subunit = new ArrayList<Integer>();
        subunit.addAll(siteIter(mod.getCGlycosylationSites(), index));
        subunit.addAll(siteIter(mod.getNGlycosylationSites(), index));
        subunit.addAll(siteIter(mod.getOGlycosylationSites(), index));
        return subunit;
    }

    public static List<Integer> getSites(List<DisulfideLink> disulfides,
                                         int index) {
        ArrayList<Integer> subunit = new ArrayList<Integer>();
        for (DisulfideLink sm : disulfides) {
            subunit.addAll(siteIter(sm.getSites(), index));
        }
        return subunit;
    }

    public static ArrayList<Integer> siteIter(List<Site> sites, int index) {
        ArrayList<Integer> subunit = new ArrayList<Integer>();
        for (Site s : sites) {
            if (s.subunitIndex == index) {
                subunit.add(s.residueIndex);
            }
        }
        return subunit;
    }

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
        int count = 0;
        if (obj == null)
            return count;
        try {
                count+=obj.getCGlycosylationSites().size();
                count+=obj.getOGlycosylationSites().size();
                count+=obj.getNGlycosylationSites().size();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        Logger.info("final count = " + count);
        return count;

    }

    // sigh ... this is the best of a bunch of bad options now

    public static class GinasSearchResultProcessor
        extends SearchResultProcessor<StructureIndexer.Result> {
        
        GinasSearchResultProcessor() {
        }

        int index;
        protected Object instrument(StructureIndexer.Result r)
            throws Exception {
            List<ChemicalSubstance> chemicals = SubstanceFactory.chemfinder
                .where().eq("structure.id", r.getId()).findList();
            double similarity=r.getSimilarity();
            Logger.debug(String.format("%1$ 5d: matched %2$s %3$.3f", ++index,
                                       r.getId(), r.getSimilarity()));
                         
            ChemicalSubstance chem = null;
            if (!chemicals.isEmpty()) {
                int[] amap = new int[r.getMol().getAtomCount()];
                int i = 0, nmaps = 0;
                for (MolAtom ma : r.getMol().getAtomArray()) {
                    amap[i] = ma.getAtomMap();
                    if (amap[i] > 0)
                        ++nmaps;
                    ++i;
                }

                chem = chemicals.iterator().next();             
                if (nmaps > 0) {
                    IxCache.set("AtomMaps/"+getContext().getId()+"/" +r.getId(), amap);
                    
                }
                IxCache.set("Similarity/"+getContext().getId()+"/" +r.getId(), similarity);
            }
            return chem;
        }
    }

    public static class GinasSequenceResultProcessor
        extends SearchResultProcessor<SequenceIndexer.Result> {
        GinasSequenceResultProcessor () {}
        
        @Override
        protected Object instrument (SequenceIndexer.Result r)
            throws Exception {
            List<ProteinSubstance> proteins = SubstanceFactory.protfinder
                .where().eq("protein.subunits.uuid", r.id).findList();
            ProteinSubstance protein =
                proteins.isEmpty() ? null : proteins.iterator().next();
            if (protein != null) {
                IxCache.set("Alignment/"+getContext().getId()+"/"+r.id, r);
            }
            else {
                Logger.warn("Can't retrieve protein for subunit "+r.id);
            }
            return protein;
        }
    }

    public static SequenceIndexer.Result
        getSeqAlignment (String context, String id) {
        return (SequenceIndexer.Result)IxCache.get("Alignment/"+context+"/"+id);
    }
    public static Double
        getChemSimilarity (String context, String id) {
        return (Double)IxCache.get("Similarity/"+context+"/"+id);
    }

    static public Substance resolve(Relationship rel) {
        Substance relsub = null;
        try {
            relsub = SUBFINDER.where()
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
            Substance sub = SUBFINDER.where().eq("uuid", uuid).findUnique();
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
        return ok(mapper.valueToTree(rels));
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
        //Logger.debug("Fetching structure");
        String atomMap = "";
        if (context != null) {
            int[] amap = (int[])IxCache.get("AtomMaps/"+context+"/"+id);
            //Logger.debug("AtomMaps/"+context+" => "+amap);
            if (amap != null && amap.length > 0) {
                StringBuilder sb = new StringBuilder ();
                sb.append(amap[0]);
                for (int i = 1; i < amap.length; ++i)
                    sb.append(","+amap[i]);
                atomMap = sb.toString();
            }
            else {
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
                        UUID uuid=UUID.fromString(id);
                        //Unit u=new Unit();
                        Unit u=GinasFactory.unitFinder.byId(uuid);
                        return App.render(u.structure,size);
                }catch(Exception e){
                        e.printStackTrace();
                }
            }
        }
        return r1;
        
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
        }
        
        //Assets.at("public/images/",placeholderFile,true).apply();
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
                c = GinasUtils.structureToChemical(struc, messages);
                
        }else{
                if(s instanceof ProteinSubstance){
                        p=((ProteinSubstance)s).protein;
                }else{
                        c = GinasUtils.substanceToChemical(s, messages);
                }
        }
        
        
        
        ObjectMapper om = new ObjectMapper();
        Logger.debug("SERIALIZED:" + om.valueToTree(messages).toString());
        response().setHeader("EXPORT-WARNINGS",om.valueToTree(messages).toString() +"___");
                try {
                    /*
                     * really?
                     * 
                     */
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
                        }else{
                            return ok(makeFastaFromNA(((NucleicAcidSubstance)s)));
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
        StringBuilder sb=new StringBuilder();
        int i=0;
        for(String line: mol.split("\n")){
                if(i!=0){
                        sb.append("\n");
                }
                if(i==1){
                        line=" G-SRS " + line;
                }
                i++;
                sb.append(line);
        }
        return sb.toString();
    }
    public static String makeFastaFromProtein(ProteinSubstance p){
        String resp = "";
        List<Subunit> subs=p.protein.subunits;
        Collections.sort(subs, new Comparator<Subunit>(){
                @Override
                public int compare(Subunit o1, Subunit o2) {
                    return o1.subunitIndex-o2.subunitIndex;
                }});
        
        for(Subunit s: subs){
            resp+=">" + p.getApprovalIDDisplay() + "|SUBUNIT_" +  s.subunitIndex + "\n";
            for(String seq : splitBuffer(s.sequence,80)){
                resp+=seq+"\n";
            }
        }
        return resp;
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
                resp+=">" + p.getApprovalIDDisplay() + "|SUBUNIT_" +  s.subunitIndex + "\n";
                for(String seq : splitBuffer(s.sequence,80)){
                        resp+=seq+"\n";
                }
        }
        return resp;
    }
    public static int totalSites(NucleicAcidSubstance sub, boolean includeEnds){
        int tot=0;
        for(Subunit s: sub.nucleicAcid.getSubunits()){
                tot+=s.sequence.length();
                if(!includeEnds)tot--;
        }
        return tot;
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
                int elements = (input.length() + maxLength - 1) / maxLength;
                String[] ret = new String[elements];
                for (int i = 0; i < elements; i++) {
                        int start = i * maxLength;
                        ret[i] = input.substring(start,
                                        Math.min(input.length(), start + maxLength));
                }
                return ret;
        }

    
    
    

    public static String getAsJson(Object o){
        ObjectMapper om = new ObjectMapper();
        return om.valueToTree(o).toString();
    }

    private static String updateKey =null;

    @Dynamic(value = IxDynamicResourceHandler.IS_ADMIN, handler = ix.ncats.controllers.security.IxDeadboltHandler.class)
    public static Result updateIndex(String key){
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

            Runnable r= new Runnable(){
                @Override
                public void run() {
                    try {
                        new RebuildIndex(listener).reindex(Substance.class);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            };
            new Thread(r).start();
            updateKey=UUID.randomUUID().toString();

            return ok(new Html("<h1>Updating indexes:</h1><pre>Preprocessing ...</pre><br><a href=\"" + callMonitor.url() + "\">refresh</a>"));
        }
    }




    private static class SubstanceReIndexListener implements RebuildIndex.ReIndexListener {

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
            message.append("\n\nCompleted Substance reindexing.\nTotal Time:").append((System.currentTimeMillis() - startTime)).append("ms");
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
            
            message.append(toAppend);

            lastUpdateTime = currentTime;

            recordsIndexedLastUpdate = currentRecordsIndexed;
        }


        @Override
        public void error(Throwable t) {
            t.printStackTrace();
        }
    }
}
