package ix.ginas.controllers;

import ix.core.controllers.search.SearchFactory;
import ix.core.models.Keyword;
import ix.core.models.Structure;
import ix.core.models.Value;
import ix.core.plugins.StructureIndexerPlugin;
import ix.core.plugins.TextIndexerPlugin;
import ix.core.search.TextIndexer;
import ix.core.search.TextIndexer.Facet;
import ix.ginas.controllers.v1.SubstanceFactory;
import ix.ginas.models.v1.ChemicalSubstance;
import ix.ginas.models.v1.Code;
import ix.ginas.models.v1.DisulfideLink;
import ix.ginas.models.v1.Glycosylation;
import ix.ginas.models.v1.Modifications;
import ix.ginas.models.v1.Polymer;
import ix.ginas.models.v1.Protein;
import ix.ginas.models.v1.ProteinSubstance;
import ix.ginas.models.v1.Relationship;
import ix.ginas.models.v1.Site;
import ix.ginas.models.v1.Name;
import ix.ginas.models.v1.StructuralModification;
import ix.ginas.models.v1.Substance;
import ix.ncats.controllers.App;
import ix.utils.Util;

import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Callable;

import org.springframework.util.StringUtils;

import play.Logger;
import play.Play;
import play.cache.Cache;
import play.db.ebean.Model;
import play.mvc.Result;
import tripod.chem.indexer.StructureIndexer;

import com.fasterxml.jackson.databind.ObjectMapper;

public class GinasApp extends App {
    static final TextIndexer TEXT_INDEXER = 
        Play.application().plugin(TextIndexerPlugin.class).getIndexer();
    static final StructureIndexer STRUC_INDEXER =
        Play.application().plugin(StructureIndexerPlugin.class).getIndexer();

    // substance finder
    static final Model.Finder<UUID, Substance> SUBFINDER =
        new Model.Finder(UUID.class, Substance.class);
    
    // relationship finder
    static final Model.Finder<UUID, Relationship> RELFINDER =
        new Model.Finder(UUID.class, Relationship.class);

    
    public static final String[] CHEMICAL_FACETS = {
        "Status",
        "Substance Class",
        "SubstanceStereoChemistry",
        "Molecular Weight",
        "GInAS Tag"
    };
    
    public static final String[] PROTEIN_FACETS = {
        "Sequence Type",
        "Substance Class",
        "Status"
    };

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

    /**
     * return a field named type to get around scala's template reserved 
     * keyword
     */
    public static String getType (Object obj) {
        Class cls = obj.getClass();
        String type = null;
        try {
            Field f = cls.getField("type");
            if (f != null)
                return (String)f.get(obj);
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
        return type;
    }
    
    public static Result error (int code, String mesg) {
        return ok (ix.ginas.views.html.error.render(code, mesg));
    }
    
    public static Result _notFound (String mesg) {
        return notFound (ix.ginas.views.html.error.render(404, mesg));
    }
    
    public static Result _badRequest (String mesg) {
        return badRequest (ix.ginas.views.html.error.render(400, mesg));
    }
    
    public static Result _internalServerError (Throwable t) {
        t.printStackTrace();
        return internalServerError
            (ix.ginas.views.html.error.render
             (500, "Internal server error: "+t.getMessage()));
    }
    
    public static Result authenticate () {
        return ok ("You're authenticated!");
    }

    public static String truncate (String name) {
        String trunc = "";
        if (name.length()<=20){
            return name;
        } else {
            String[] spl = name.split("\\s");
            //Logger.info("split "+ Arrays.deepToString(spl));
            for(int i=0; i<spl.length; i++){
                if(spl[i].length()<=20){
                    trunc+= spl[i] +" ";
                }else{
                    trunc+=spl[i].substring(0, 10) + "...";
                    //Logger.info("trunc split " +trunc);
                    return trunc;
                }
            }
        }
        //Logger.info("trunc " + trunc);
        return trunc.substring(0, 17)+ "...";
    }
        
    static FacetDecorator[] decorate (Facet... facets) {
        List<FacetDecorator> decors = new ArrayList<FacetDecorator>();
        // override decorator as needed here
        for (int i = 0; i < facets.length; ++i) {
            decors.add(new GinasFacetDecorator (facets[i]));
        }

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
            String n = super.name();
            if ("SubstanceStereoChemistry".equals(n))
                return "Stereo Chemistry";
            return n.trim();
        }
        
        @Override
        public String label (final int i) {
            final String label = super.label(i);
            final String name = super.name();
            return label;
        }
    }
    
    public static <T> String namesList(List<Name> list){
    	int size = list.size();
    	if(size >= 6){
    		size=6;
    	}
    	String[] arr = new String [size];
    	for(int i=0; i<size; i++){
    		Name n = list.get(i);
    		String name = n.name;
    		arr[i]= name;
    	}
    	return StringUtils.arrayToDelimitedString(arr, "; ");
    	
    }
    
    public static <T> String codesList(List<Code> list){
    	int size = list.size();
    	if(size>=6){
    		size=6;
    	}
    	String[] arr = new String [size];
    	for(int i=0; i<size; i++){
    		String name = list.get(i).code;
    		arr[i]= name;
    	}
    	return StringUtils.arrayToDelimitedString(arr, "; ");
     }
    
    public static Result chemicals (final String q,
                                    final int rows, final int page) {
        String type = request().getQueryString("type");
        Logger.debug("Chemicals: rows=" + rows + " page=" + page);
        try {
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
                
                return notFound (ix.ginas.views.html.error.render
                                 (400, "Invalid search parameters: type=\""+type
                                  +"\"; q=\""+q+"\" cutoff=\""+cutoff+"\"!"));
            }
            else {
                return _chemicals (q, rows, page);
            }
        }
        catch (Exception ex) {
            return _internalServerError (ex);
        }
    }

    static Result createChemicalResult
        (TextIndexer.SearchResult result, int rows, int page) {
        TextIndexer.Facet[] facets = filter
            (result.getFacets(), CHEMICAL_FACETS);
        
        List<ChemicalSubstance> chemicals = new ArrayList<ChemicalSubstance>();
        int[] pages = new int[0];
        if (result.count() > 0) {
            rows = Math.min(result.count(), Math.max(1, rows));
            pages = paging (rows, page, result.count());
            for (int i = (page - 1) * rows, j = 0; j < rows
                     && i < result.size(); ++j, ++i) {
                chemicals.add((ChemicalSubstance) result.get(i));
            }
        }

        return ok(ix.ginas.views.html.chemicals.render
                  (page, rows, result.count(),
                   pages, decorate (facets), chemicals));
        
    }

    static Result _chemicals (final String q, final int rows, final int page)
        throws Exception {
        final int total = SubstanceFactory.finder.findRowCount();
        final String key = "chemicals/"+Util.sha1(request ());
        
        if (request().queryString().containsKey("facet") || q != null) {
            final TextIndexer.SearchResult result = getOrElse
                (key, new Callable<TextIndexer.SearchResult> () {
                        public TextIndexer.SearchResult call ()
                            throws Exception {
                            Logger.debug("Cache missed: "+key);
                            return getSearchResult
                            (ChemicalSubstance.class, q, total);
                        }
                    });

            if (result.finished()) {
                return getOrElse
                    (key+"/result", new Callable<Result> () {
                            public Result call () throws Exception {
                                return createChemicalResult
                                    (result, rows, page);
                            }
                        });
            }
            
            return createChemicalResult (result, rows, page);
        }
        else {
            return getOrElse (key, new Callable<Result> () {
                    public Result call () throws Exception {
                        Logger.debug("Cache missed: "+key);                     
                        TextIndexer.Facet[] facets = 
                            filter(getFacets(ChemicalSubstance.class, 30),
                                   CHEMICAL_FACETS);
                        int nrows = Math.min(total, Math.max(1, rows));
                        int[] pages = paging(nrows, page, total);
                        
                        List<ChemicalSubstance> chemicals =
                            SubstanceFactory.getChemicals
                            (nrows, (page - 1) * rows, null);

                        return ok(ix.ginas.views.html.chemicals.render
                                  (page, nrows, total, pages,
                                   decorate (facets), chemicals));
                    }
                });
        }
    }

    public static Result similarity (final String query,
                                     final double threshold,
                                     int rows, int page) {
        try {
            SearchResultContext context = similarity
                (query, threshold, rows, page,
                 new GinasSearchResultProcessor ());
            return structureResult (context, rows, page);
        }
        catch (Exception ex) {
            ex.printStackTrace();
            Logger.error("Can't perform similarity search: "+query, ex);
        }
        return internalServerError
            (ix.ginas.views.html.error.render
             (500, "Unable to perform similarity search: "+query));
    }

    public static Result substructure
        (final String query, final int rows, final int page) {
        try {
            SearchResultContext context = substructure
                (query, rows, page, new GinasSearchResultProcessor ());

            return structureResult (context, rows, page);
        }
        catch (Exception ex) {
            ex.printStackTrace();
            Logger.error("Can't perform substructure search", ex);
        }
        return internalServerError
            (ix.ginas.views.html.error.render
             (500, "Unable to perform substructure search: "+query));
    }

    public static Result structureResult
        (final SearchResultContext context, int rows, int page)
        throws Exception {

        final String key = "structureResult/"+context.getId()
            +"/"+Util.sha1(request (), "facet");
        
        TextIndexer.SearchResult result = getOrElse
            (key, new Callable<TextIndexer.SearchResult> () {
                    public TextIndexer.SearchResult call () throws Exception {
                        Logger.debug("Cache missed: "+key);
                        List results = context.getResults();
                        return results.isEmpty() ? null : SearchFactory.search
                        (results, null, results.size(), 0,
                         FACET_DIM, request().queryString());
                    }
                });

        TextIndexer.Facet[] facets = new TextIndexer.Facet[0];
        List<ChemicalSubstance> substances =
            new ArrayList<ChemicalSubstance>();
        int[] pages = new int[0];
        int count = 0;
        if (result != null) {
            Long stop = context.getStop();
            if (!context.finished() || 
                (stop != null && stop >= result.getTimestamp()))
                Cache.remove(key);
            
            count = result.count();
            Logger.debug(key+": "+count);
            
            rows = Math.min(count, Math.max(1, rows));
            int i = (page - 1) * rows;
            if (i < 0 || i >= count) {
                page = 1;
                i = 0;
            }
            pages = paging (rows, page, count);
            facets = filter (result.getFacets(), CHEMICAL_FACETS);

            for (int j = 0; j < rows && i < count; ++j, ++i)
                substances.add((ChemicalSubstance)result.get(i));
        }
        
        return ok (ix.ginas.views.html.chemicals.render
                   (page, rows, count, pages, decorate (facets), substances));
    }

    static final GetResult<ChemicalSubstance> ChemicalResult =
        new GetResult<ChemicalSubstance>(ChemicalSubstance.class,
                                         SubstanceFactory.chemfinder) {
            public Result getResult (List<ChemicalSubstance> chemicals)
                throws Exception {
                return _getChemicalResult (chemicals);
            }
        };

    static Result _getChemicalResult (List<ChemicalSubstance> chemicals)
        throws Exception {
        // force it to show only one since it's possible that the provided
        // name isn't unique
        Logger.info("7");
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
                (indexer, ChemicalSubstance.class, null, null,
                 indexer.size(), 0, FACET_DIM, request().queryString());
            if (result.count() < chemicals.size()) {
                chemicals.clear();
                for (int i = 0; i < result.count(); ++i) {
                    chemicals.add((ChemicalSubstance)result.get(i));
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
    public static String getId (Substance substance) {
        if (substance.approvalID != null)
            return substance.approvalID;

        /**
         * proper permission should be checked here
         */
        String official = null;
        for (Name n : substance.names) {
            if (n.preferred)
                return n.name;
            else if ("Official Name".equalsIgnoreCase(n.type))
                official = n.name;
        }
        
        return official != null ? official
            : substance.uuid.toString().substring(0, 8);
    }

    static abstract class GetResult<T extends Substance> {
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
                                return resolve (finder, name);
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
                final String key =
                    cls.getName()+"/result/"+Util.sha1(request ());
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

    static <T extends Substance> List<T> resolve
        (Model.Finder<Long, T> finder, String name) {
        List<T> values = new ArrayList<T>();    
        if (name.length() == 8) { // might be uuid
            values = finder.where().istartsWith("uuid", name).findList();
        }
        
        if (values.isEmpty()) {
            values = finder.where()
                .ieq("approvalID", name).findList();
            if (values.isEmpty()){
                values = finder.where()
                    .ieq("names.name", name).findList();
                if (values.isEmpty())               // last resort..
                    values = finder.where()
                        .ieq("codes.code", name).findList();
            }
        }
        
        if (values.size() > 1) {
            Logger.warn("\""+name+"\" yields "
                        +values.size()+" matches!");
        }
        return values;
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
        
    /******************* PROTEINS *************************************************/
        
    public static Result proteins (final String q,
                                   final int rows, final int page) {
        try {
            final String key = "proteins/"+Util.sha1(request ());
            return getOrElse(key, new Callable<Result>() {
                    public Result call () throws Exception {
                        Logger.debug("Cache missed: "+key);
                        return _proteins (q, rows, page);
                    }
                });
        }
        catch (Exception ex) {
            return _internalServerError (ex);
        }
    }

    static Result _proteins (String q, int rows, int page) throws Exception {
        String type = request().getQueryString("type");
        Logger.debug("Proteins: rows=" + rows + " page=" + page);
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
                getSearchResult (ProteinSubstance.class, q, total);

            TextIndexer.Facet[] facets = filter
                (result.getFacets(), PROTEIN_FACETS);

            List<ProteinSubstance> proteins = new ArrayList<ProteinSubstance>();
            int[] pages = new int[0];
            if (result.count() > 0) {
                rows = Math.min(result.count(), Math.max(1, rows));
                pages = paging (rows, page, result.count());
                for (int i = (page - 1) * rows, j = 0; j < rows
                         && i < result.count(); ++j, ++i) {
                    proteins.add((ProteinSubstance) result.get(i));
                }
            }

            return ok(ix.ginas.views.html.proteins.render
                      (page, rows, result.count(),
                       pages, decorate (facets), proteins));
        }
        else {
            final String key = ProteinSubstance.class.getName()+".facets";
            TextIndexer.Facet[] facets = getOrElse
                (key, new Callable<TextIndexer.Facet[]>() {
                        public TextIndexer.Facet[] call() {
                            Logger.debug("Cache missed: "+key);
                            return filter(getFacets(ProteinSubstance.class, 30),
                                          PROTEIN_FACETS);
                        }
                    });
            rows = Math.min(total, Math.max(1, rows));
            int[] pages = paging(rows, page, total);

            List<ProteinSubstance> proteins =
                SubstanceFactory.getProteins(rows, (page - 1) * rows, null);
            Logger.info("protein list length: " + proteins.size());
            return ok(ix.ginas.views.html.proteins.render
                      (page, rows, total, pages, decorate (facets), proteins));
        }
    }

    static final GetResult<ProteinSubstance> ProteinResult =
        new GetResult<ProteinSubstance>(ProteinSubstance.class, SubstanceFactory.protfinder) {
            public Result getResult (List<ProteinSubstance> proteins) throws Exception {
                return _getProteinResult (proteins);
            }
        };
    
    static Result _getProteinResult (List<ProteinSubstance> proteins) throws Exception {
        // force it to show only one since it's possible that the provided
        // name isn't unique
        if (true || proteins.size() == 1) {
            ProteinSubstance protein = proteins.iterator().next();
            return ok (ix.ginas.views.html
                       .proteindetails.render(protein));
        }
        else {
            TextIndexer indexer = textIndexer.createEmptyInstance();
            for (ProteinSubstance prot : proteins)
                indexer.add(prot);

            TextIndexer.SearchResult result = SearchFactory.search
                (indexer, ProteinSubstance.class, null, null,
                 indexer.size(), 0, FACET_DIM,
                 request().queryString());
            if (result.count() < proteins.size()) {
                proteins.clear();
                for (int i = 0; i < result.count(); ++i) {
                    proteins.add((ProteinSubstance)result.get(i));
                }
            }
            TextIndexer.Facet[] facets = filter
                (result.getFacets(), PROTEIN_FACETS);
            indexer.shutdown();

            return ok (ix.ginas.views.html.proteins.render
                       (1, result.count(), result.count(),
                        new int[0], decorate (facets), proteins));
        }
    }

    public static Result protein (String name) {
        return ProteinResult.get(name);
    }
    
    /**
     * return the canonical/default chemical id
     */

    
    public static String siteCheck(Protein prot, int subunit, int index){
        String desc=prot.getSiteModificationIfExists(subunit, index);
        if(desc==null)return "";
        return desc;
    }
    
    
    public static List<Integer> getSites (Modifications mod, int index){
        ArrayList<Integer> subunit= new ArrayList<Integer>();
        for(StructuralModification sm: mod.structuralModifications){
            subunit = siteIter(sm.sites, index);
        }
        return subunit; 
    }
    
    
    public static List<Integer> getSites (Glycosylation mod, int index){
        ArrayList<Integer> subunit= new ArrayList<Integer>();
        subunit.addAll(siteIter(mod.CGlycosylationSites, index));
        subunit.addAll(siteIter(mod.NGlycosylationSites, index));
        subunit.addAll(siteIter(mod.OGlycosylationSites, index));
        return subunit; 
    }

    public static List<Integer> getSites (List<DisulfideLink> disulfides, int index){
        ArrayList<Integer> subunit= new ArrayList<Integer>();
        for(DisulfideLink sm: disulfides){
            subunit.addAll( siteIter(sm.sites, index));
        }
        return subunit; 
    }
        
    public static ArrayList<Integer> siteIter (List<Site> sites, int index){
        ArrayList<Integer> subunit= new ArrayList<Integer>();
        for(Site s : sites){
            if(s.subunitIndex==index){
                subunit.add(s.residueIndex);
            }
        }
        return subunit;
    }
        
    public static String getAAName (char aa) {

        String amino;

        switch (aa) {
        case 'A': amino = "Alanine";
            break;
            //              case 'B' : amino ="Asparagine/Aspartic acid";
            //              break;
        case 'C': amino = "Cysteine";
            break;
        case 'D': amino = "Aspartic acid";
            break;
        case 'E': amino = "Glutamic acid";
            break;
        case 'F': amino = "Phenylalanine";
            break;
        case 'G': amino = "Glycine";
            break;
        case 'H': amino = "Histidine";
            break;
        case 'I': amino = "Isoleucine";
            break;
        case 'K': amino = "Lysine";
            break;
        case 'L': amino = "Leucine";
            break;
        case 'M': amino = "Methionine";
            break;
        case 'N': amino = "Asparagine";
            break;
        case 'P': amino = "Proline";
            break;
        case 'Q': amino = "Glutamine";
            break;
        case 'R': amino = "Arginine";
            break;
        case 'S': amino = "Serine";
            break;
        case 'T': amino = "Threonine";
            break;
        case 'V': amino = "Valine";
            break;
        case 'W': amino = "Tryptophan";
            break;
        case 'Y': amino = "Tyrosine";
            break;
            //              case 'Z': amino = "Glutamine/Glutamic acid";
            //              break;
        default: 
            amino = "Tim forgot one";
            break;
        }
        return amino;

    }

    @SuppressWarnings("rawtypes")
    public static int getCount (Object obj){
        int count=0;
        try {
            for(Field l: obj.getClass().getFields()){
//                                              Logger.info(l.getName().toString());
                Class type = l.getType();
                if(type.isArray()){
                    count += Array.getLength(l.get(obj));
                }
                else if (Collection.class.isAssignableFrom(type)) {
                    count += ((Collection)l.get(obj)).size();
                                                          Logger.info("collection"+ count);

                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        Logger.info("final count = " + count);
        return count;
   
    }

    public static class GinasSearchResultProcessor
        extends SearchResultProcessor {
        final public Set<String> processed = new HashSet<String>();
        int count;

        GinasSearchResultProcessor () {
        }

        protected Object instrument (StructureIndexer.Result r)
            throws Exception {
            List<ChemicalSubstance> chemicals =
                SubstanceFactory.chemfinder
                .where().eq("structure.id", r.getId()).findList();
            return chemicals.isEmpty() ? null : chemicals.iterator().next();
        }
    }
    
    public static Result search (String kind) {
        try {
            String q = request().getQueryString("q");
            String t = request().getQueryString("type");
            if (kind != null && !"".equals(kind)) {
                if (ChemicalSubstance.class.getName().equals(kind)){
                    return redirect (routes.GinasApp.chemicals(q, 32, 1));
                }
                else if (ProteinSubstance.class.getName().equals(kind)){
                    return redirect (routes.GinasApp.proteins(q, 32, 1));
                }
                else if ("substructure".equalsIgnoreCase(t)) {
                    String url = routes.GinasApp.chemicals(q, 16, 1).url()
                        +"&type="+t;
                    return redirect (url);
                }
                else if ("similarity".equalsIgnoreCase(t)) {
                    String cutoff = request().getQueryString("cutoff");
                    if (cutoff == null) {
                        cutoff = "0.8";
                    }
                    String url = routes.GinasApp.chemicals(q, 16, 1).url()
                        +"&type="+t+"&cutoff="+cutoff;
                    return redirect (url);
                }
            }
            // generic entity search..
            return search (8);
        }
        catch (Exception ex) {
            Logger.debug("Can't resolve class: "+kind, ex);
        }

        return _badRequest ("Invalid request: "+request().uri());
    }

    public static Result search (final int rows) {
        Logger.info("generic search");
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
            Logger.info("1");
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
                            (MAX_SEARCH_RESULTS, 0, FACET_DIM, queryString);
                        }
                    });

            double ellapsed = (System.currentTimeMillis()-start)*1e-3;
            Logger.debug
                ("1. Elapsed time "+String.format("%1$.3fs", ellapsed));
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
                            (quote (query), MAX_SEARCH_RESULTS, 0,
                             FACET_DIM, request().queryString());
                        }
                    });
            double ellapsed = (System.currentTimeMillis()-start)*1e-3;
            Logger.debug
                ("2. Elapsed time "+String.format("%1$.3fs", ellapsed));
        }
        TextIndexer.Facet[] facets = filter
            (result.getFacets(), CHEMICAL_FACETS);

        int max = Math.min(rows, Math.max(1,result.count()));
        int total = 0, totalChemicalSubstances = 0, totalProteinSubstances = 0, totalLigands = 0;
        for (TextIndexer.Facet f : result.getFacets()) {
            if (f.getName().equals("ix.Class")) {
                for (TextIndexer.FV fv : f.getValues()) {
                    if (ChemicalSubstance.class.getName().equals(fv.getLabel())) {
                        totalChemicalSubstances = fv.getCount();
                        total += totalChemicalSubstances;
                    }
                    else if (ProteinSubstance.class.getName()
                             .equals(fv.getLabel())) {
                        totalProteinSubstances = fv.getCount();  
                        total += totalProteinSubstances;
                    }
                    else if (Polymer.class.getName().equals(fv.getLabel())) {
                        totalLigands = fv.getCount();
                        total += totalLigands;
                    }
                }
            }
        }

        List<ChemicalSubstance> chemicalSubstances =
            filter (ChemicalSubstance.class, result.getMatches(), max);

        List<ProteinSubstance> proteinSubstances =
            filter (ProteinSubstance.class, result.getMatches(), max);

        return ok (ix.ginas.views.html.search.render
                   (query, total, GinasApp.decorate (facets),
                    chemicalSubstances, totalChemicalSubstances,
                    proteinSubstances, totalLigands,
                    null, totalProteinSubstances));
    }

    static public Substance resolve (Relationship rel) {
        Substance relsub = null;        
        try {
            relsub = SUBFINDER.where()
                .eq("approvalID", rel.relatedSubstance.approvalID)
                .findUnique();
        }
        catch (Exception ex) {
            Logger.warn("Can't retrieve related substance "
                        +"from relationship "+rel.uuid);
        }
        return relsub;
    }
    
    static public List<Relationship> resolveRelationships (String uuid) {
        List<Relationship> resolved = new ArrayList<Relationship>();
        try {
            Substance sub = SUBFINDER.where().eq("uuid", uuid).findUnique();
            if (sub != null) {
                for (Relationship rel : sub.relationships) {
                    if (null != resolve (rel))
                        resolved.add(rel);
                }
            }
            else {
                Logger.warn("Unknown substance: "+uuid);
            }
        }
        catch (Exception ex) {
            ex.printStackTrace();
            Logger.error("Can't resolve relationship for substance "+uuid, ex);
        }
        return resolved;
    }

    static public Result relationships (String uuid) {
        List<Relationship> rels = resolveRelationships (uuid);
        ObjectMapper mapper = new ObjectMapper ();
        return ok (mapper.valueToTree(rels));
    }
}

