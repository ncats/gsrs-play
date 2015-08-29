package ix.ginas.controllers;

import ix.core.controllers.PayloadFactory;
import ix.core.controllers.StructureFactory;
import ix.core.controllers.search.SearchFactory;
import ix.core.models.Keyword;
import ix.core.models.Structure;
import ix.core.models.Value;
import ix.core.models.Payload;
import ix.core.plugins.StructureIndexerPlugin;
import ix.core.plugins.PayloadPlugin;
import ix.core.plugins.TextIndexerPlugin;
import ix.core.plugins.IxCache;
import ix.core.search.TextIndexer;
import ix.core.search.TextIndexer.Facet;
import ix.ginas.controllers.v1.*;
import ix.ginas.models.v1.*;
import ix.ncats.controllers.App;
import ix.ncats.controllers.auth.Authentication;
import ix.utils.Util;

import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.util.StringUtils;

import play.Logger;
import play.Play;
import play.db.ebean.Model;
import play.mvc.*;
import play.libs.ws.*;
import play.libs.F;
import tripod.chem.indexer.StructureIndexer;
import ix.seqaln.SequenceIndexer;

import chemaxon.struc.MolAtom;
import chemaxon.util.MolHandler;

import com.fasterxml.jackson.databind.ObjectMapper;

public class GinasApp extends App {
    // substance finder
    static final Model.Finder<UUID, Substance> SUBFINDER =
        new Model.Finder(UUID.class, Substance.class);
    
    // relationship finder
    static final Model.Finder<UUID, Relationship> RELFINDER =
        new Model.Finder(UUID.class, Relationship.class);
    
    public static final String[] CHEMICAL_FACETS = {
        "Record Status",
        "Substance Class", "SubstanceStereoChemistry", "Molecular Weight",
        "GInAS Tag" };
    
    public static final String[] PROTEIN_FACETS = {
        "Sequence Type",
        "Substance Class", "Status" };
    
    public static final String[] ALL_FACETS = {
        "Record Status",
        "Substance Class", "SubstanceStereoChemistry", "Molecular Weight",
        "GInAS Tag", "Sequence Type", "Material Class", "Material Type",
        "Family", "Parts", "Code System" };

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
            (int page, int rows, int total,
             int[] pages, List<TextIndexer.Facet> facets,
             List<Substance> substances) {

            process (substances);
            return ok(ix.ginas.views.html.substances.render
                      (page, rows, total, pages,
                       decorate(filter(facets, this.facets)),
                       substances,null));
        }

        // override to process before rendering
        protected void process (List<Substance> substances) {
        }
    }
    
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
        Class cls = obj.getClass();
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
        return notFound(ix.ginas.views.html.error.render(404,
                                                         "Unknown resource: " + request().uri()));
    }
    
    public static Result _notFound(String mesg) {
        return notFound(ix.ginas.views.html.error.render(404, mesg));
    }
    
    public static Result _badRequest(String mesg) {
        return badRequest(ix.ginas.views.html.error.render(400, mesg));
    }
    
    public static Result _internalServerError(Throwable t) {
        t.printStackTrace();
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
        try {
            return IxCache.getOrElse("ginasCV", new Callable<CV>() {
                    public CV call() throws Exception {
                        if (!ControlledVocabularyFactory.isloaded()) {
                            Call call = controllers.routes.Assets
                                .at("ginas/CV.txt");
                            F.Promise<WSResponse> ws = WS.url(
                                                              call.absoluteURL(request())).get();
                            ControlledVocabularyFactory.loadSeedCV(ws.get(1000)
                                                                   .getBodyAsStream());
                        }

                        CV cv = new CV();
                        Logger.debug("CV loaded: size=" + cv.size());
                        return cv;
                    }
                }, 0);
        } catch (Exception ex) {
            Logger.error("Can't load CV", ex);
        }
        return null;
    }

    static FacetDecorator[] decorate(Facet... facets) {
        List<FacetDecorator> decors = new ArrayList<FacetDecorator>();
        // override decorator as needed here
        for (int i = 0; i < facets.length; ++i) {
            decors.add(new GinasFacetDecorator(facets[i]));
        }

        GinasFacetDecorator f = new GinasFacetDecorator(new TextIndexer.Facet(
                                                                              "ChemicalSubstance"));
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
            return n.trim();
        }
        
        @Override
        public String label(final int i) {
            final String label = super.label(i);
            final String name = super.name();
            if ("StructurallyDiverse".equalsIgnoreCase(label))
                return "Structurally Diverse";
            
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
                   maxLength = 100000)
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
            else if (type.equalsIgnoreCase("substructure")
                     || type.equalsIgnoreCase("similarity")) {
                // structure search
                String cutoff = request().getQueryString("cutoff");
                Logger.debug("Search: q=" + q + " type=" + type + " cutoff="
                             + cutoff);
                try {
                    if (type.equalsIgnoreCase("substructure")) {
                        return substructure(q, rows, page);
                    } else {
                        return similarity(q, Double.parseDouble(cutoff), rows,
                                          page);
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }

                return notFound(ix.ginas.views.html.error.render
                                (400,"Invalid search parameters: type=\""
                                 + type+ "\"; q=\"" + q + "\" cutoff=\""
                                 + cutoff + "\"!"));
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
            
            // TODO: rename structureResult to something less specific!
            return App.structureResult
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
    static Result _substances(final String q, final int rows, final int page)
        throws Exception {
        final int total = Math.max(SubstanceFactory.getCount(), 1);
        final String key = "substances/" + Util.sha1(request());

        // if there's a provided query, or there's a facet specified,
        // do a text search
        if (request().queryString().containsKey("facet") || q != null) {
            final TextIndexer.SearchResult result =
                getSearchResult (Substance.class, q, total);
            // if(true)throw new IllegalStateException("one two");
            Logger.debug("_substance: q=" + q + " rows=" + rows + " page="
                         + page + " => " + result + " finished? "
                         + result.finished());
            if (result.finished()) {
                final String k = key + "/result";
                return getOrElse(k, new Callable<Result>() {
                        public Result call() throws Exception {
                            Logger.debug("Cache missed: " + k);
                            return createSubstanceResult(result, rows, page);
                        }
                    });
            }

            return createSubstanceResult(result, rows, page);
                        
            //otherwise, just show the first substances
        } else {
            return getOrElse(key, new Callable<Result>() {
                    public Result call() throws Exception {
                        Logger.debug("Cache missed: " + key);
                        TextIndexer.Facet[] facets = filter(
                                                            getFacets(Substance.class, 30), ALL_FACETS);
                        int nrows = Math.max(Math.min(total, Math.max(1, rows)), 1);
                        int[] pages = paging(nrows, page, total);

                        List<Substance> substances = SubstanceFactory
                            .getSubstances(nrows, (page - 1) * rows, null);

                        return ok(ix.ginas.views.html.substances.render(page,
                                                                        nrows, total, pages, decorate(facets), substances,null));
                    }
                });
        }
    }

    static Result createSubstanceResult(TextIndexer.SearchResult result,
                                        int rows, int page) {
        TextIndexer.Facet[] facets = filter(result.getFacets(), ALL_FACETS);

        List<Substance> substances = new ArrayList<Substance>();
        int[] pages = new int[0];
        if (result.count() > 0) {
            rows = Math.min(result.count(), Math.max(1, rows));
            pages = paging(rows, page, result.count());
            for (int i = (page - 1) * rows, j = 0; j < rows
                     && i < result.size(); ++j, ++i) {
                substances.add((Substance) result.get(i));
            }
        }
        long starttime = System.currentTimeMillis();
                
                
        //              ObjectMapper om = new ObjectMapper();
        //              om.valueToTree(substances);
        //              int k=0;
        //              for(Substance s:substances){
        //                      for(Name n:s.getAllNames()){
        //                              k+=n.name.hashCode();
        //                      }
        //              }
                
        String tt=(-(starttime-System.currentTimeMillis())/1000.)  + "s";
                
        Logger.debug("############## serialization time:" + tt);
                

        return ok(ix.ginas.views.html.substances.render(page, rows,
                                                        result.count(), pages, decorate(facets), substances, result.getSearchContextAnalyzer().getFieldFacets()));

    }

    public static final GetResult<Substance> SubstanceResult =
        new GetResult<Substance>(
                                 Substance.class, SubstanceFactory.finder) {
            public Result getResult(List<Substance> substances) throws Exception {
                return _getSubstanceResult(substances);
            }
        };
    
    static Result _getSubstanceResult(List<Substance> substances)
        throws Exception {
        // force it to show only one since it's possible that the provided
        // name isn't unique
        if (true || substances.size() == 1) {
            Substance substance = substances.iterator().next();
            Substance.SubstanceClass type = substance.substanceClass;
            switch (type) {
            case chemical:
                return ok(ix.ginas.views.html.chemicaldetails
                          .render((ChemicalSubstance) substance));
            case protein:
                return ok(ix.ginas.views.html.proteindetails
                          .render((ProteinSubstance) substance));
            case mixture:
                return ok(ix.ginas.views.html.mixturedetails
                          .render((MixtureSubstance) substance));
            case polymer:
                return ok(ix.ginas.views.html.polymerdetails
                          .render((PolymerSubstance) substance));
            case structurallyDiverse:
                return ok(ix.ginas.views.html.diversedetails
                          .render((StructurallyDiverseSubstance) substance));
            case specifiedSubstanceG1:
                return ok(ix.ginas.views.html.group1details
                          .render((SpecifiedSubstanceGroup1) substance));
            case concept:
                return ok(ix.ginas.views.html.conceptdetails
                          .render((Substance) substance));
            default:
                return _badRequest("type not found");
            }
        } else {
            TextIndexer indexer = _textIndexer.createEmptyInstance();
            for (Substance sub : substances)
                indexer.add(sub);

            TextIndexer.SearchResult result = SearchFactory.search(indexer,
                                                                   Substance.class, null, null, indexer.size(), 0, FACET_DIM,
                                                                   request().queryString());
            if (result.count() < substances.size()) {
                substances.clear();
                for (int i = 0; i < result.count(); ++i) {
                    substances.add((Substance) result.get(i));
                }
            }
            TextIndexer.Facet[] facets = filter(result.getFacets(), ALL_FACETS);
            indexer.shutdown();

            return ok(ix.ginas.views.html.substances.render(1, result.count(),
                                                            result.count(), new int[0], decorate(facets), substances,null));
        }
    }

    public static Result substance(String name) {
        return SubstanceResult.get(name);
    }

    public static Result chemicals(final String q, final int rows,
                                   final int page) {
        String type = request().getQueryString("type");
        Logger.debug("Chemicals: rows=" + rows + " page=" + page);
        try {
            if (type != null
                && (type.equalsIgnoreCase("substructure") || type
                    .equalsIgnoreCase("similarity"))) {
                // structure search
                String cutoff = request().getQueryString("cutoff");
                Logger.debug("Search: q=" + q + " type=" + type + " cutoff="
                             + cutoff);
                try {
                    if (type.equalsIgnoreCase("substructure")) {
                        return substructure(q, rows, page);
                    } else {
                        return similarity(q, Double.parseDouble(cutoff), rows,
                                          page);
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }

                return notFound(ix.ginas.views.html.error.render(400,
                                                                 "Invalid search parameters: type=\"" + type
                                                                 + "\"; q=\"" + q + "\" cutoff=\"" + cutoff
                                                                 + "\"!"));
            } else {
                return _chemicals(q, rows, page);
            }
        } catch (Exception ex) {
            return _internalServerError(ex);
        }
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

        return ok(ix.ginas.views.html.substances.render(page, rows,
                                                        result.count(), pages, decorate(facets), chemicals,null));

    }

    static Result _chemicals(final String q, final int rows, final int page)
        throws Exception {
        final int total = SubstanceFactory.finder.findRowCount();
        final String key = "chemicals/" + Util.sha1(request());

        if (request().queryString().containsKey("facet") || q != null) {
            final TextIndexer.SearchResult result = getOrElse
                (key, new Callable<TextIndexer.SearchResult>() {
                        public TextIndexer.SearchResult call()
                            throws Exception {
                            Logger.debug("Cache missed: " + key);
                            return getSearchResult(ChemicalSubstance.class, q,
                                                   total);
                        }
                    });
            
            if (result.finished()) {
                return getOrElse(key + "/result", new Callable<Result>() {
                        public Result call() throws Exception {
                            return createChemicalResult(result, rows, page);
                        }
                    });
            }

            return createChemicalResult(result, rows, page);
        } else {
            return getOrElse(key, new Callable<Result>() {
                    public Result call() throws Exception {
                        Logger.debug("Cache missed: " + key);
                        TextIndexer.Facet[] facets = filter(
                                                            getFacets(ChemicalSubstance.class, 30),
                                                            CHEMICAL_FACETS);
                        int nrows = Math.min(total, Math.max(1, rows));
                        int[] pages = paging(nrows, page, total);

                        List<Substance> chemicals = SubstanceFactory.getSubstances(
                                                                                   nrows, (page - 1) * rows, null);

                        return ok(ix.ginas.views.html.substances.render(page,
                                                                        nrows, total, pages, decorate(facets), chemicals,null));
                    }
                });
        }
    }

    public static Result similarity(final String query, final double threshold,
                                    int rows, int page) {
        try {
            SearchResultContext context = similarity
                (query, threshold, rows,
                 page, new GinasSearchResultProcessor());
            return structureResult(context, rows, page);
        }
        catch (Exception ex) {
            ex.printStackTrace();
            Logger.error("Can't perform similarity search: " + query, ex);
        }
        
        return internalServerError
            (ix.ginas.views.html.error.render
             (500, "Unable to perform similarity search: " + query));
    }

    public static Result substructure(final String query, final int rows,
                                      final int page) {
        final GinasSearchResultProcessor processor =
            new GinasSearchResultProcessor();
        try {
            SearchResultContext context = substructure
                (query, rows, page, processor);

            return structureResultAndProcess(context, rows, page, processor);
        } catch (Exception ex) {
            ex.printStackTrace();
            Logger.error("Can't perform substructure search", ex);
        }
        return internalServerError
            (ix.ginas.views.html.error.render
             (500, "Unable to perform substructure search: " + query));
    }

    public static Result structureResultAndProcess
        (final SearchResultContext context, int rows, int page,
         final GinasSearchResultProcessor processor) throws Exception {

        return structureResult
            (context, rows, page, 
             new SubstanceResultRenderer (CHEMICAL_FACETS) {
                 @Override
                 protected void process (List<Substance> substances) {
                     if (processor != null)
                         processor.postProcess(substances);
                 }
             });
             
    }

    public static Result structureResult(final SearchResultContext context,
                                         int rows, int page) throws Exception {
        return structureResultAndProcess(context, rows, page, null);
    }

    static final GetResult<Substance> ChemicalResult = new GetResult<Substance>(
                                                                                Substance.class, SubstanceFactory.finder) {
            public Result getResult(List<Substance> chemicals) throws Exception {
                return _getChemicalResult(chemicals);
            }
        };

    static Result _getChemicalResult(List<Substance> chemicals)
        throws Exception {
        // force it to show only one since it's possible that the provided
        // name isn't unique
        if (true || chemicals.size() == 1) {
            Substance chemical = chemicals.iterator().next();
            return ok(ix.ginas.views.html.chemicaldetails
                      .render((ChemicalSubstance) chemical));
        } else {
            TextIndexer indexer = _textIndexer.createEmptyInstance();
            for (Substance chem : chemicals)
                indexer.add(chem);

            TextIndexer.SearchResult result = SearchFactory.search(indexer,
                                                                   Substance.class, null, null, indexer.size(), 0, FACET_DIM,
                                                                   request().queryString());
            if (result.count() < chemicals.size()) {
                chemicals.clear();
                for (int i = 0; i < result.count(); ++i) {
                    chemicals.add((Substance) result.get(i));
                }
            }
            TextIndexer.Facet[] facets = filter(result.getFacets(),
                                                CHEMICAL_FACETS);
            indexer.shutdown();

            return ok(ix.ginas.views.html.substances.render(1, result.count(),
                                                            result.count(), new int[0], decorate(facets), chemicals,null));
        }
    }

    public static Result chemical(String name) {
        return ChemicalResult.get(name);
    }

    /**
     * return the canonical/default chemical id
     * 
     * This needs to be re-evaluated. It is possible for there to be duplicated
     * names, and there is no check here for this.
     * 
     * While it's not as pretty, I'm defaulting to using the uuid or approvalID.
     * 
     * 
     */
    public static String getId(Substance substance) {
        if (substance.approvalID != null)
            return substance.approvalID;

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
        // String official = null;
        // for (Name n : substance.names) {
        // if (n.preferred)
        // return n.name;
        // else if ("of".equalsIgnoreCase(n.type))
        // official = n.name;
        // }
        //
        // return official != null ? official
        // : substance.uuid.toString().substring(0, 8);

        return substance.uuid.toString().substring(0, 8);
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
        }
        List<T> values = new ArrayList<T>();
        if (name.length() == 8) { // might be uuid
            values = finder.where().istartsWith("uuid", name).findList();
        }

        if (values.isEmpty()) {
            values = finder.where().ieq("approvalID", name).findList();
            if (values.isEmpty()) {
                values = finder.where().ieq("names.name", name).findList();
                if (values.isEmpty()) // last resort..
                    values = finder.where().ieq("codes.code", name).findList();
            }
        }

        if (values.size() > 1) {
            Logger.warn("\"" + name + "\" yields " + values.size()
                        + " matches!");
        }
        return values;
    }

    public static List<Keyword> getStructureReferences(Structure s) {
        List<Keyword> references = new ArrayList<Keyword>();
        for (Value v : s.properties) {
            Logger.info(v.label);
            if (v.label.equals("GInAS Reference")) {
                Keyword k = new Keyword(v.getValue().toString());
                Logger.info(k.term);
                references.add(k);
            }
        }

        return references;
    }

        // ******************* PROTEINS
        // *************************************************//*

    public static Result proteins(final String q, final int rows, final int page) {
        try {
            final String key = "proteins/" + Util.sha1(request());
            return getOrElse(key, new Callable<Result>() {
                    public Result call() throws Exception {
                        Logger.debug("Cache missed: " + key);
                        return _proteins(q, rows, page);
                    }
                });
        } catch (Exception ex) {
            return _internalServerError(ex);
        }
    }

    static Result _proteins(String q, int rows, int page) throws Exception {
        String type = request().getQueryString("type");
        Logger.debug("Proteins: rows=" + rows + " page=" + page);
        if (type != null
            && (type.equalsIgnoreCase("substructure") || type
                .equalsIgnoreCase("similarity"))) {
            // structure search
            String cutoff = request().getQueryString("cutoff");
            Logger.debug("Search: q=" + q + " type=" + type + " cutoff="
                         + cutoff);
            try {
                if (type.equalsIgnoreCase("substructure")) {
                    return substructure(q, rows, page);
                } else {
                    return similarity(q, Double.parseDouble(cutoff), rows, page);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            return notFound(ix.ginas.views.html.error.render(400,
                                                             "Invalid search parameters: type=\"" + type + "\"; q=\""
                                                             + q + "\" cutoff=\"" + cutoff + "\"!"));
        }

        final int total = SubstanceFactory.finder.findRowCount();
        if (request().queryString().containsKey("facet") || q != null) {
            TextIndexer.SearchResult result = getSearchResult(
                                                              ProteinSubstance.class, q, total);

            TextIndexer.Facet[] facets = filter(result.getFacets(),
                                                PROTEIN_FACETS);

            List<Substance> proteins = new ArrayList<Substance>();
            int[] pages = new int[0];
            if (result.count() > 0) {
                rows = Math.min(result.count(), Math.max(1, rows));
                pages = paging(rows, page, result.count());
                for (int i = (page - 1) * rows, j = 0; j < rows
                         && i < result.count(); ++j, ++i) {
                    proteins.add((Substance) result.get(i));
                }
            }

            return ok(ix.ginas.views.html.substances.render(page, rows,
                                                            result.count(), pages, decorate(facets), proteins,null));
        } else {
            final String key = ProteinSubstance.class.getName() + ".facets";
            TextIndexer.Facet[] facets = getOrElse(key,
                                                   new Callable<TextIndexer.Facet[]>() {
                                                       public TextIndexer.Facet[] call() {
                                                           Logger.debug("Cache missed: " + key);
                                                           return filter(
                                                                         getFacets(ProteinSubstance.class, 30),
                                                                         PROTEIN_FACETS);
                                                       }
                                                   });
            rows = Math.min(total, Math.max(1, rows));
            int[] pages = paging(rows, page, total);

            List<Substance> proteins = SubstanceFactory.getSubstances(rows,
                                                                      (page - 1) * rows, null);
            Logger.info("protein list length: " + proteins.size());
            return ok(ix.ginas.views.html.substances.render(page, rows, total,
                                                            pages, decorate(facets), proteins,null));
        }
    }

    static final GetResult<Substance> ProteinResult =
        new GetResult<Substance>(
                                 Substance.class, SubstanceFactory.finder) {
            public Result getResult(List<Substance> proteins) throws Exception {
                return _getProteinResult(proteins);
            }
        };
    
    static Result _getProteinResult(List<Substance> proteins) throws Exception {
        // force it to show only one since it's possible that the provided
        // name isn't unique
        if (true || proteins.size() == 1) {
            Substance protein = proteins.iterator().next();
            return ok(ix.ginas.views.html.proteindetails
                      .render((ProteinSubstance) protein));
        } else {
            TextIndexer indexer = _textIndexer.createEmptyInstance();
            for (Substance prot : proteins)
                indexer.add(prot);
            
            TextIndexer.SearchResult result = SearchFactory.search
                (indexer, ProteinSubstance.class, null, null, indexer.size(),
                 0,  FACET_DIM, request().queryString());
            if (result.count() < proteins.size()) {
                proteins.clear();
                for (int i = 0; i < result.count(); ++i) {
                    proteins.add((ProteinSubstance) result.get(i));
                    
                }
            }
            TextIndexer.Facet[] facets = filter(result.getFacets(),
                                                PROTEIN_FACETS);
            indexer.shutdown();
            
            return ok(ix.ginas.views.html.substances.render(1, result.count(),
                                                            result.count(), new int[0], decorate(facets), proteins,null));
        }
    }
    
    public static Result protein(String name) {
        return ProteinResult.get(name);
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

    public static List<Integer> getSites(Modifications mod, int index) {
        ArrayList<Integer> subunit = new ArrayList<Integer>();
        for (StructuralModification sm : mod.structuralModifications) {
            subunit = siteIter(sm.sites, index);
        }
        return subunit;
    }

    public static List<Integer> getSites(Glycosylation mod, int index) {
        ArrayList<Integer> subunit = new ArrayList<Integer>();
        subunit.addAll(siteIter(mod.CGlycosylationSites, index));
        subunit.addAll(siteIter(mod.NGlycosylationSites, index));
        subunit.addAll(siteIter(mod.OGlycosylationSites, index));
        return subunit;
    }

    public static List<Integer> getSites(List<DisulfideLink> disulfides,
                                         int index) {
        ArrayList<Integer> subunit = new ArrayList<Integer>();
        for (DisulfideLink sm : disulfides) {
            subunit.addAll(siteIter(sm.sites, index));
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

        String amino;

        switch (aa) {
        case 'A':
            amino = "Alanine";
            break;
            // case 'B' : amino ="Asparagine/Aspartic acid";
            // break;
        case 'C':
            amino = "Cysteine";
            break;
        case 'D':
            amino = "Aspartic acid";
            break;
        case 'E':
            amino = "Glutamic acid";
            break;
        case 'F':
            amino = "Phenylalanine";
            break;
        case 'G':
            amino = "Glycine";
            break;
        case 'H':
            amino = "Histidine";
            break;
        case 'I':
            amino = "Isoleucine";
            break;
        case 'K':
            amino = "Lysine";
            break;
        case 'L':
            amino = "Leucine";
            break;
        case 'M':
            amino = "Methionine";
            break;
        case 'N':
            amino = "Asparagine";
            break;
        case 'P':
            amino = "Proline";
            break;
        case 'Q':
            amino = "Glutamine";
            break;
        case 'R':
            amino = "Arginine";
            break;
        case 'S':
            amino = "Serine";
            break;
        case 'T':
            amino = "Threonine";
            break;
        case 'V':
            amino = "Valine";
            break;
        case 'W':
            amino = "Tryptophan";
            break;
        case 'Y':
            amino = "Tyrosine";
            break;
            // case 'Z': amino = "Glutamine/Glutamic acid";
            // break;
        default:
            amino = "Tim forgot one";
            break;
        }
        return amino;

    }

    @SuppressWarnings("rawtypes")
    public static int getCount(Object obj) {
        int count = 0;
        if (obj == null)
            return count;
        try {

            for (Field l : obj.getClass().getFields()) {
                // Logger.info(l.getName().toString());
                Class type = l.getType();
                if (type.isArray()) {
                    count += Array.getLength(l.get(obj));
                } else if (Collection.class.isAssignableFrom(type)) {
                    count += ((Collection) l.get(obj)).size();
                    Logger.info("collection" + count);

                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        Logger.info("final count = " + count);
        return count;

    }

        // sigh ... this is the best of a bunch of bad options now

    public static class GinasSearchResultProcessor
        extends SearchResultProcessor<StructureIndexer.Result> {
        final public Set<String> processed = new HashSet<String>();
        final public Map<String, int[]> atomMaps = new ConcurrentHashMap<String, int[]>();
        int count;
        
        GinasSearchResultProcessor() {
        }
        
        public void postProcess(List<Substance> lsub) {
            for (Substance s : lsub) {
                int[] am = atomMaps.get(s.uuid + "");
                if (am != null) {
                    ((ChemicalSubstance) s).setAtomMaps(am);
                }
            }
        }
            
        protected Object instrument(StructureIndexer.Result r) throws Exception {
            List<ChemicalSubstance> chemicals = SubstanceFactory.chemfinder
                .where().eq("structure.id", r.getId()).findList();
            if (!chemicals.isEmpty()) {
                int[] amap = new int[r.getMol().getAtomCount()];
                int i = 0;
                for (MolAtom ma : r.getMol().getAtomArray()) {
                    amap[i++] = ma.getAtomMap();
                }
                atomMaps.put(chemicals.get(0).uuid + "", amap);
            }
            return chemicals.isEmpty() ? null : chemicals.iterator().next();
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
            return proteins.isEmpty() ? null : proteins.iterator().next();
        }
    }

    public static Result search(String kind) {
        try {
            String q = request().getQueryString("q");
            String t = request().getQueryString("type");
            if (kind != null && !"".equals(kind)) {
                if (ChemicalSubstance.class.getName().equals(kind)) {
                    return redirect(routes.GinasApp.substances(q, 32, 1));
                } else if (ProteinSubstance.class.getName().equals(kind)) {
                    return redirect(routes.GinasApp.substances(q, 32, 1));
                } else if ("substructure".equalsIgnoreCase(t)) {
                    String url = routes.GinasApp.substances(q, 16, 1).url()
                        + "&type=" + t;
                    return redirect(url);
                } else if ("similarity".equalsIgnoreCase(t)) {
                    String cutoff = request().getQueryString("cutoff");
                    if (cutoff == null) {
                        cutoff = "0.8";
                    }
                    String url = routes.GinasApp.chemicals(q, 16, 1).url()
                        + "&type=" + t + "&cutoff=" + cutoff;
                    return redirect(url);
                }
            }
            // generic entity search..
            return search(8);
        } catch (Exception ex) {
            Logger.debug("Can't resolve class: " + kind, ex);
        }

        return _badRequest("Invalid request: " + request().uri());
    }

    public static Result search(final int rows) {
        Logger.info("generic search");
        try {

            final String key = "search/" + Util.sha1(request());
            return getOrElse(key, new Callable<Result>() {
                    public Result call() throws Exception {
                        Logger.debug("Cache missed: " + key);
                        return _search(rows);
                    }
                });
        } catch (Exception ex) {
            return _internalServerError(ex);
        }
    }

    static Result _search(int rows) throws Exception {
        final String query = request().getQueryString("q");
        Logger.debug("Query: \"" + query + "\"");

        TextIndexer.SearchResult result = null;
        if (query.indexOf('/') > 0) { // use mesh facet
            final Map<String, String[]> queryString = new HashMap<String, String[]>();
            queryString.putAll(request().queryString());
            // append this facet to the list
            List<String> f = new ArrayList<String>();
            f.add("MeSH/" + query);
            String[] ff = queryString.get("facet");
            if (ff != null) {
                for (String fv : ff)
                    f.add(fv);
            }
            Logger.info("1");
            queryString.put("facet", f.toArray(new String[0]));
            long start = System.currentTimeMillis();
            final String key = "search/facet/"
                + Util.sha1(queryString.get("facet"));
            result = getOrElse(key, new Callable<TextIndexer.SearchResult>() {
                    public TextIndexer.SearchResult call() throws Exception {
                        Logger.debug("Cache missed: " + key);
                        return SearchFactory.search(MAX_SEARCH_RESULTS, 0,
                                                    FACET_DIM, queryString);
                    }
                });

            double ellapsed = (System.currentTimeMillis() - start) * 1e-3;
            Logger.debug("1. Elapsed time "
                         + String.format("%1$.3fs", ellapsed));
        }

        if (result == null || result.count() == 0) {
            long start = System.currentTimeMillis();
            final String key = "search/facet/q/"
                + Util.sha1(request(), "facet", "q");
            result = getOrElse(key, new Callable<TextIndexer.SearchResult>() {
                    public TextIndexer.SearchResult call() throws Exception {
                        Logger.debug("Cache missed: " + key);
                        return SearchFactory.search(quote(query),
                                                    MAX_SEARCH_RESULTS, 0, FACET_DIM, request()
                                                    .queryString());
                    }
                });
            double ellapsed = (System.currentTimeMillis() - start) * 1e-3;
            Logger.debug("2. Elapsed time "
                         + String.format("%1$.3fs", ellapsed));
        }
        TextIndexer.Facet[] facets = filter(result.getFacets(), CHEMICAL_FACETS);

        int max = Math.min(rows, Math.max(1, result.count()));
        int total = 0, totalChemicalSubstances = 0, totalProteinSubstances = 0, totalLigands = 0;
        for (TextIndexer.Facet f : result.getFacets()) {
            if (f.getName().equals("ix.Class")) {
                for (TextIndexer.FV fv : f.getValues()) {
                    if (ChemicalSubstance.class.getName().equals(fv.getLabel())) {
                        totalChemicalSubstances = fv.getCount();
                        total += totalChemicalSubstances;
                    } else if (ProteinSubstance.class.getName().equals(
                                                                       fv.getLabel())) {
                        totalProteinSubstances = fv.getCount();
                        total += totalProteinSubstances;
                    } else if (Polymer.class.getName().equals(fv.getLabel())) {
                        totalLigands = fv.getCount();
                        total += totalLigands;
                    }
                }
            }
        }

        List<ChemicalSubstance> chemicalSubstances = filter(
                                                            ChemicalSubstance.class, result.getMatches(), max);

        List<ProteinSubstance> proteinSubstances = filter(
                                                          ProteinSubstance.class, result.getMatches(), max);

        return ok(ix.ginas.views.html.search.render(query, total,
                                                    GinasApp.decorate(facets), chemicalSubstances,
                                                    totalChemicalSubstances, proteinSubstances,
                                                    totalProteinSubstances, null, totalProteinSubstances));
    }

    static public Substance resolve(Relationship rel) {
        Substance relsub = null;
        try {
            relsub = SUBFINDER.where()
                .eq("approvalID", rel.relatedSubstance.approvalID)
                .findUnique();
        } catch (Exception ex) {
            Logger.warn("Can't retrieve related substance "
                        + "from relationship " + rel.uuid);
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
     * @param atomMap
     * @return
     */
    public static Result structure (final String id,
                                    final String format, final int size,
                                    final String atomMap) {
        //Logger.debug("Fetching structure");
        Result r1 = App.structure(id, format, size, atomMap);
        int httpStat =  r1.toScala().header().status();
        if(httpStat == NOT_FOUND){
            Substance s = SubstanceFactory.getSubstance(id);
            if(s instanceof ChemicalSubstance){
                String sid1= ((ChemicalSubstance) s).structure.id.toString();
                return App.structure(sid1, format, size, atomMap);
            }
        }
        return r1;
        
    }
}
