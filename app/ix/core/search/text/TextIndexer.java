package ix.core.search.text;

import static org.apache.lucene.document.Field.Store.NO;
import static org.apache.lucene.document.Field.Store.YES;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.nio.file.Files;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import ix.core.search.*;
import ix.core.util.*;
import net.sf.ehcache.search.impl.SearchManager;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.KeywordAnalyzer;
import org.apache.lucene.analysis.miscellaneous.PerFieldAnalyzerWrapper;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.util.CharArraySet;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.DoubleField;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.document.FloatField;
import org.apache.lucene.document.IntField;
import org.apache.lucene.document.LongField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.facet.DrillDownQuery;
import org.apache.lucene.facet.DrillSideways;
import org.apache.lucene.facet.FacetField;
import org.apache.lucene.facet.FacetResult;
import org.apache.lucene.facet.Facets;
import org.apache.lucene.facet.FacetsCollector;
import org.apache.lucene.facet.FacetsConfig;
import org.apache.lucene.facet.LabelAndValue;
import org.apache.lucene.facet.range.LongRange;
import org.apache.lucene.facet.range.LongRangeFacetCounts;
import org.apache.lucene.facet.taxonomy.FastTaxonomyFacetCounts;
import org.apache.lucene.facet.taxonomy.TaxonomyReader;
import org.apache.lucene.facet.taxonomy.directory.DirectoryTaxonomyReader;
import org.apache.lucene.facet.taxonomy.directory.DirectoryTaxonomyWriter;
import org.apache.lucene.index.AtomicReaderContext;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.FieldInfo.IndexOptions;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.index.IndexableFieldType;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.Terms;
import org.apache.lucene.queries.ChainedFilter;
import org.apache.lucene.queries.TermsFilter;
import org.apache.lucene.queryparser.classic.CharStream;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Collector;
import org.apache.lucene.search.FieldCacheRangeFilter;
import org.apache.lucene.search.FieldCacheTermsFilter;
import org.apache.lucene.search.Filter;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.NumericRangeQuery;
import org.apache.lucene.search.PhraseQuery;
import org.apache.lucene.search.PrefixQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Scorer;
import org.apache.lucene.search.SearcherManager;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TermRangeQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.WildcardQuery;
import org.apache.lucene.search.suggest.DocumentDictionary;
import org.apache.lucene.search.suggest.analyzing.AnalyzingInfixSuggester;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.NIOFSDirectory;
import org.apache.lucene.store.NoLockFactory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.Version;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import ix.core.FieldNameDecorator;
import ix.core.factories.FieldNameDecoratorFactory;
import ix.core.factories.IndexValueMakerFactory;
import ix.core.plugins.IxCache;
import ix.core.search.FieldedQueryFacet.MATCH_TYPE;
import ix.core.search.SearchOptions.DrillAndPath;
import ix.core.search.text.TextIndexer.IxQueryParser;
import ix.core.util.EntityUtils.EntityInfo;
import ix.core.util.EntityUtils.EntityWrapper;
import ix.core.util.EntityUtils.Key;
import ix.core.utils.executor.ProcessListener;
import ix.utils.Global;
import ix.utils.Tuple;
import ix.utils.Util;
import play.Logger;
import play.Play;

/**
 * Singleton class that responsible for all entity indexing
 */
public class TextIndexer implements Closeable, ProcessListener {
	private static final String TERM_VEC_PREFIX = "F";

    public static final String IX_BASE_PACKAGE = "ix";
	
	public static final boolean INDEXING_ENABLED = ConfigHelper.getBoolean("ix.textindex.enabled",true);
	private static final boolean USE_ANALYSIS =    ConfigHelper.getBoolean("ix.textindex.fieldsuggest",true);
	
	private static final String ANALYZER_FIELD = "M_FIELD";
	private static final String ANALYZER_VAL_PREFIX = "ANALYZER_";
	
	
	private static final char SORT_DESCENDING_CHAR = '$';
	private static final char SORT_ASCENDING_CHAR = '^';
	private static final int EXTRA_PADDING = 2;
	private static final String FULL_TEXT_FIELD = "text";
	private static final String SORT_PREFIX = "SORT_";
	protected static final String STOP_WORD = " THE_STOP";
	protected static final String START_WORD = "THE_START ";
	public static final String GIVEN_STOP_WORD = "$";
	public static final String GIVEN_START_WORD = "^";
	static final String ROOT = "root";
	
	
	public static String FULL_TEXT_FIELD(){
		return FULL_TEXT_FIELD;
	}

	public void deleteAll() {
		try {
			indexWriter.deleteAll();
			indexWriter.commit();
		} catch (Exception e) {
			// e.printStackTrace();
		}
	}

	
	/**
	 * well known fields
	 */
	public static final String FIELD_KIND = "__kind";
	public static final String FIELD_ID = "id";

	/**
	 * these default parameters should be configurable!
	 */
	public static final int CACHE_TIMEOUT = 60 * 60 * 24; // 24 hours
	private static int FETCH_WORKERS;

	/**
	 * Make sure to properly update the code when upgrading version
	 */
	static final Version LUCENE_VERSION = Version.LATEST;
	static final String FACETS_CONFIG_FILE = "facet_conf.json";
	static final String SUGGEST_CONFIG_FILE = "suggest_conf.json";
	static final String SORTER_CONFIG_FILE = "sorter_conf.json";
	public static final String DIM_CLASS = "ix.Class";

	static final ThreadLocal<DateFormat> YEAR_DATE_FORMAT = new ThreadLocal<DateFormat>() {
		@Override
		protected DateFormat initialValue() {
			return new SimpleDateFormat("yyyy");
		}
	};

	private static final Pattern SUGGESTION_WHITESPACE_PATTERN = Pattern.compile("[\\s/]");

	private static CachedSupplier<AtomicBoolean> ALREADY_INITIALIZED = CachedSupplier.of(()->new AtomicBoolean(false));

	
	private static class TermVectorField extends org.apache.lucene.document.Field {
        static final FieldType TermVectorFieldType = new FieldType ();
        static {
            TermVectorFieldType.setIndexed(true);
            TermVectorFieldType.setTokenized(false);
            TermVectorFieldType.setStoreTermVectors(true);
            TermVectorFieldType.setStoreTermVectorPositions(false);
            TermVectorFieldType.freeze();
        }
        
        public TermVectorField (String field, String value) {
            super (field, value, TermVectorFieldType);
        }
    }

    public static class TermVectors implements java.io.Serializable {
        static private final long serialVersionUID = 0x192464a5d08ea528l;
        
        private Class kind;     
        private String field;
        private int numDocs;
        private List<TermList> docs = new ArrayList<TermList>();
        private Map<String, DocumentSet> terms = new TreeMap<String,DocumentSet>();
        private Map<String, String> filters = new TreeMap<String, String>();
        
        TermVectors (Class kind, String field) {
            this.kind = kind;
            this.field = field;
        }

        public Class getKind () { return kind; }
        public String getField () { return field.substring(TERM_VEC_PREFIX.length()); }

        public Map<String, String> getFilters () { return filters; }
        public Map<String, DocumentSet> getTerms () { return terms; }
        public List<TermList> getDocs () { return docs; }    
        public int getNumDocs () { return numDocs; }
        public int getNumDocsWithTerms () { return docs.size(); }
        public int getNumTerms () { return terms.size(); }
        public Integer getTermCount (String term) {
            DocumentSet map = terms.get(term);
            Integer count = null;
            if (map != null) {
                count = map.getNDocs();
            }
            return count;
        }
        
        public Predicate<Tuple<String, Integer>> getPredicate(Query q){
            String find = "";
            if(q instanceof PhraseQuery){
                find=Stream.of(((PhraseQuery)q).getTerms())
                        .map(t->t.text())
                        .collect(Collectors.joining(" "));
            }else if(q instanceof TermQuery){
                find=Stream.of(((TermQuery)q).getTerm())
                        .map(t->t.text())
                        .collect(Collectors.joining(" "));
            }else if(q instanceof TermRangeQuery){
                TermRangeQuery trq= (TermRangeQuery)q;
                Integer max = Integer.parseInt(trq.getUpperTerm().utf8ToString())+ ((trq.includesUpper())?1:0);
                Integer min = Integer.parseInt(trq.getLowerTerm().utf8ToString())- ((trq.includesLower())?1:0);;
                
                return (t)->{
                    return (t.v()< max) && (t.v()>min);   
                  };
                
            }else if(q instanceof NumericRangeQuery){
                NumericRangeQuery nq = (NumericRangeQuery)q;
                nq.includesMax();
                nq.includesMin();
                
                Integer max=((NumericRangeQuery)q)
                                .getMax()
                                .intValue() + ((nq.includesMax())?1:0);
                Integer min=((NumericRangeQuery)q)
                                .getMin()
                                .intValue() - ((nq.includesMin())?1:0);
                
                return (t)->{
                  return (t.v()< max) && (t.v()>min);   
                };
            }else if(q instanceof PrefixQuery){
                PrefixQuery pq = (PrefixQuery)q;
                find=pq.getPrefix().text();
                String finalField = find.toLowerCase();
                return (t)->{
                    return Stream
                            .of(t.k().toLowerCase().split(" "))
                            .filter(f->f.startsWith(finalField))
                            .findAny().isPresent();
                  };
            }else if(q instanceof BooleanQuery){
                BooleanQuery bq = (BooleanQuery)q;
                Set<Term> terms = new HashSet<Term>();
                bq.extractTerms(terms);
                
                List<String> findterms=terms.stream().map(t->t.text().toLowerCase()).collect(Collectors.toList());
                
                return (t)->{
                    return findterms.stream().allMatch(s->t.k().toLowerCase().contains(s));
                };
                
            }else{
                throw new IllegalStateException(q.getClass() + " not supported " + ":" + q.toString());
                
            }
            String finalField = find.toLowerCase();
            return (t)->{
              return t.k().toLowerCase().contains(finalField);  
            };
        }
        
        public FacetMeta getFacet(int top, int skip, String filter, String uri) throws ParseException{
            Facet fac = new Facet(getField(), null);
            
            Predicate<Tuple<String,Integer>> filt=(t)->true;
            
            if(filter!=null && !filter.equals("")){
                QueryParser parser = new IxQueryParser("label");
                filt = getPredicate(parser.parse(filter));
            }
            
            
            terms.entrySet()
                .stream()
                .map(es->Tuple.of(es.getKey(), es.getValue().getNDocs()))
                .filter(filt)
                .map(t->new FV(fac,t.k(),t.v()))
                .collect(StreamUtil.maxElements(top+skip, Facet.Comparators.COUNT_SORTER_DESC))
                .skip(skip)
                .limit(top)
                .forEach(fv->{
                    fac.add(fv);
                });
        
            return new FacetMeta.Builder()
            .facets(fac)
            .ffilter(filter)
            .fdim(top)
            .fskip(skip)
            .ftotal(terms.size())
            .uri(uri)
            .build();
        }
        
    }
    private static class DocumentSet implements Serializable{
        private Set s;
        public DocumentSet(Set set){
            this.s=set;
        }
        public Set getDocs(){
            return this.s;
        }
        public int getNDocs(){
            return this.s.size();
        }
        public static DocumentSet of(Set s){
            return new DocumentSet(s);
        }
    }
    
    private static class TermList implements Comparable<TermList>, Serializable{
        private String id;
        private List s;
        public TermList(String id,List list){
            this.id=id;
            this.s=list;
        }
        
        public String getDoc(){
            return id;
        }
        
        public int getNTerms(){
            return this.s.size();
        }
        
        public List getTerms(){
            return this.s;
        }
        public static TermList of(String id, List s){
            return new TermList(id,s);
        }

        @Override
        public int compareTo(TermList o) {
            return o.getNTerms()-this.getNTerms();
        }
    }

    static class TermVectorsCollector<T> extends Collector {
        private int docBase;
        private IndexReader reader;     
        private EntityInfo<T> entityMeta;
        private TermVectors tvec;
        private Map<String, Set<Object>> counts;
        private final Set<String> fieldSet;

        private TermVectorsCollector (Class<T> kind, String originalField, IndexSearcher searcher, Filter extrafilter, Query q)
            throws IOException {
            String adaptedField = TERM_VEC_PREFIX + originalField;
            
            tvec = new TermVectors (kind, adaptedField);
            counts = new TreeMap<String, Set<Object>>();
            
            entityMeta = EntityUtils.getEntityInfoFor(kind);
            
            fieldSet = entityMeta.getTypeAndSubTypes()
                      .stream()
                      .map(em->em.getInternalIdField())
                      .collect(Collectors.toSet());
            fieldSet.add(FIELD_KIND);
            
                
            this.reader = searcher.getIndexReader();
            
            Filter filter = filterForKinds(kind);
            
            if(q==null){
                q = new MatchAllDocsQuery();
            }
            
            if(extrafilter!=null){
                filter=new ChainedFilter(new Filter[]{filter,extrafilter},ChainedFilter.AND);
            }
            
            searcher.search(q, filter, this);

            Collections.sort(tvec.docs);

            tvec.terms= counts.entrySet()
                    .stream()
                    .map(Tuple::of)
                    .map(Tuple.vmap(DocumentSet::of))
                    .collect(Tuple.toMap());
            counts = null;
            
        }
       
        
        public void setScorer (Scorer scorer) {}
        
        public boolean acceptsDocsOutOfOrder () { return true; }
        
        public void collect (int doc) {
            int docId = docBase + doc;
            try {
                
                //TODO: It IS possible to get all fields
                //available here. That could be useful
                //for having a "facets" resource in the
                //context of a collection
                
//                StreamUtil
//                        .forIterable(reader.getTermVectors(docId))
//                        .forEach(c->{
//                            System.out.println("GOT Facet:" + c);
//                        });
                
                
                Terms docterms = reader.getTermVector(docId, tvec.field);
                if (docterms != null) {
                    Document d = reader.document(docId, fieldSet);                    
                    String kind=d.get(FIELD_KIND).toString();
                    
                    EntityInfo einfo=EntityUtils.getEntityInfoFor(kind);
                    String idstring = d.get(einfo.getInternalIdField()).toString();
                    Object nativeID= einfo.formatIdToNative(idstring.toString());
                    
                    
                    List<String> terms = StreamUtil
                          .from(docterms.iterator(null)) //Not sure what termsEnum is here
                          .streamNullable(en->en.next())
                          .map(t->t.utf8ToString())
                          .peek(term->{
                                    counts.computeIfAbsent(term, t->new HashSet<>())
                                          .add(nativeID);
                           })
                          .collect(Collectors.toList());
                                
                    tvec.docs.add(TermList.of(idstring, terms));
                }else {
                    //Logger.debug("No term vector for field \""+field+"\"!");
                }
                ++tvec.numDocs;
            }catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        
        public void setNextReader (AtomicReaderContext ctx) {
            docBase = ctx.docBase;
        }

        public TermVectors termVectors () { return tvec; }
        
        public static <T> TermVectorsCollector<T> make(Class<T> kind, String originalField, IndexSearcher searcher, Filter filter, Query q) throws IOException{
            return new TermVectorsCollector<T>(kind,originalField, searcher, filter,q);
        }
    }
	
	
	
	
	public static class FV {
		
		private Facet container;
		String label;
		Integer count;

		FV(Facet container, String label, Integer count) {
			this.container=container;
			this.label = label;
			this.count = count;
		}

		public String getLabel() {
			return label;
		}

		public Integer getCount() {
			return count;
		}
		
		@JsonIgnore
		public String getToggleUrl(){
			return container.sr.getFacetToggleURI(container.name, this.label);
		}
	}

	public interface FacetFilter extends Predicate<FV>{
		boolean accepted(FV fv);
		
		default boolean test(FV fv){
			return accepted(fv);
		}
	}

	public static class Facet {
		String name;
		private List<FV> values = new ArrayList<FV>();
		private Set<String> selectedLabel=new HashSet<>();
		private SearchResult sr;
		
		private CachedSupplier<List<FV>> selectedFVfetch = 
		        CachedSupplier.of(()->{
		           return this.values.stream()
		               .filter(fv->selectedLabel.contains(fv.getLabel()))
		               .collect(Collectors.toList());
		        });
        
		
		/**
		 * Set the labeled facet(s) which were intentionally 
		 * selected. 
		 * 
		 * @param label
		 */
		public void setSelectedLabel(String ... label){
			this.setSelectedLabels(Arrays.stream(label).collect(Collectors.toList()));   
		}
		
		@JsonInclude(Include.NON_EMPTY)
		public Set<String> getSelectedLabels(){
			return this.selectedLabel;
		}
		
		@JsonIgnore
		public List<FV> getSelectedFV(){
		    return this.selectedFVfetch.get();
		}
		
		@JsonProperty("_self")
		public String getSelfUri(){
		    if(sr!=null){
		        return sr.getFacetURI(name);
		    }else{
		        return null;
		    }
		}
		
		@JsonIgnore
		public Set<String> getMissingSelections(){
		    Set<String> containedSet=this.getSelectedFV().stream()
		        .map(FV::getLabel)
		        .collect(Collectors.toSet());
		    
		    Set<String> totalSet = new HashSet<>(this.selectedLabel);
		    totalSet.removeAll(containedSet);
		    return totalSet;
		}
		
		
		public Facet(String name, SearchResult sr) {
			this.name = name;
			this.sr=sr;
		}

		public String getName() {
			return name;
		}

		public List<FV> getValues() {
			return values;
		}

		
		/**
		 * Get the number of {@link FV} values currently present
		 * in this facet. This is equivalent to:
		 * <pre>
		 * <code>
		 *    return this.getValues().size();
		 * </code>
		 * </pre>
		 * @return
		 */
		public int size() {
			return values.size();
		}

		public FV getValue(int index) {
			return values.get(index);
		}

		public String getLabel(int index) {
			return values.get(index).getLabel();
		}

		public Integer getCount(int index) {
			return values.get(index).getCount();
		}

		public Integer getCount(String label) {
			for (FV fv : values)
				if (fv.label.equalsIgnoreCase(label))
					return fv.count;
			return null;
		}

		public void sort() {
			sortCounts(true);
		}

		public Facet filter(FacetFilter filter) {
			Facet filtered = new Facet(name,sr);
			for (FV fv : values)
				if (filter.accepted(fv))
					filtered.values.add(fv);
			return filtered;
		}

		public void sortLabels(final boolean desc) {
			Collections.sort(values, new Comparator<FV>() {
				public int compare(FV v1, FV v2) {
					return desc ? v2.label.compareTo(v1.label) : v1.label.compareTo(v2.label);
				}
			});
		}

		public void sortCounts(final boolean desc) {
			Collections.sort(values, (v1,v2)->{
					int d = desc ? (v2.count - v1.count) : (v1.count - v2.count);
					if (d == 0)
						d = v1.label.compareTo(v2.label);
					return d;
			});
		}
		
		public static enum Comparators implements Comparator<FV>{
            LABEL_SORTER_ASC{
                @Override
                public int compare(FV v1, FV v2) {
                    return v1.label.compareTo(v2.label);
                }
            },
            COUNT_SORTER_ASC{
                @Override
                public int compare(FV v1, FV v2) {
                    int d = (v1.count - v2.count);
                    if (d == 0)
                        d = -v1.label.compareTo(v2.label);
                    return d;
                }
            },
            LABEL_SORTER_DESC{
                @Override
                public int compare(FV v1, FV v2) {
                    return -LABEL_SORTER_ASC.compare(v1, v2);
                }
            },
            COUNT_SORTER_DESC{
                @Override
                public int compare(FV v1, FV v2) {
                    return -COUNT_SORTER_ASC.compare(v1, v2);
                }
            };
		}

		@JsonIgnore
		public ArrayList<String> getLabelString() {
			ArrayList<String> strings = new ArrayList<String>();
			for (int i = 0; i < values.size(); i++) {
				String label = values.get(i).getLabel();
				strings.add(label);
			}
			return strings;
		}

		@JsonIgnore
		public ArrayList<Integer> getLabelCount() {
			ArrayList<Integer> counts = new ArrayList<Integer>();
			for (int i = 0; i < values.size(); i++) {
				int count = values.get(i).getCount();
				counts.add(count);
			}
			return counts;
		}
		
		/**
		 * Creates and adds a {@link FV} to the list of values.
		 * 
		 * @param label
		 * @param count
		 * @return The {@link FV} that was added
		 */
		public FV add(String label, Integer count){
			FV val=new FV(this, label, count);
			add(val);
			return val;
		}
		
		private void add(FV fv){
			this.values.add(fv);
			this.selectedFVfetch.resetCache();
		}

        public void setSelectedLabels(List<String> collect) {

            this.selectedLabel.clear();
            this.selectedLabel.addAll(collect);
            this.selectedFVfetch.resetCache();
        }
	}

	class SuggestLookup implements Closeable {
		String name;
		File dir;
		AtomicBoolean dirty = new AtomicBoolean(false);
        ExactMatchSuggesterDecorator lookup;
		long lastRefresh;

		ConcurrentHashMap<String, Addition> additions = new ConcurrentHashMap<String, Addition>();

		class Addition {
			String text;
			AtomicLong weight;

			public Addition(String text, long weight) {
				this.text = text;
				this.weight = new AtomicLong(weight);	
			}
			public void incrementWeight() {
				weight.incrementAndGet();
			}

			public void addToWeight(long value) {
				weight.getAndAdd(value);

			}
		}

		SuggestLookup(File dir) throws IOException {
			boolean isNew = false;
			if (!dir.exists()) {
				dir.mkdirs();
				isNew = true;
			} else if (!dir.isDirectory())
				throw new IllegalArgumentException("Not a directory: " + dir);


            AnalyzingInfixSuggester suggester = new AnalyzingInfixSuggester(LUCENE_VERSION,
                    new NIOFSDirectory(dir, NoLockFactory.getNoLockFactory()), indexAnalyzer);


//			lookup = new InxightInfixSuggester(LUCENE_VERSION,
//					new NIOFSDirectory(dir, NoLockFactory.getNoLockFactory()), indexAnalyzer);

            lookup = new ExactMatchSuggesterDecorator(suggester,()-> ReflectionUtil.getFieldValue(suggester, "searcherMgr"));

			// If there's an error getting the index count, it probably wasn't
			// saved properly. Treat it as new if an error is thrown.
			if (!isNew) {
				try {
					lookup.getCount();
				} catch (Exception e) {
					isNew = true;
					Logger.warn("Error building lookup " + dir.getName() + " will reinitialize");
				}
			}

			if (isNew) {
				Logger.debug("Initializing lookup " + dir.getName());
				build();
			} else {
				Logger.debug(lookup.getCount() + " entries loaded for " + dir.getName());
			}

			this.dir = dir;
			this.name = dir.getName();
		}

		SuggestLookup(String name) throws IOException {
			this(new File(suggestDir, name));
		}


		void add(String text) throws IOException {
			Addition add = additions.computeIfAbsent(text, t -> new Addition(t, 0));
			add.incrementWeight();
			incr();
		}

		void incr() {
			dirty.compareAndSet(false, true);
		}

		public void refreshIfDirty() {
			if (dirty.get()) {
				try {
					refresh();
				} catch (IOException ex) {
					ex.printStackTrace();
					Logger.trace("Can't refresh suggest index!", ex);
				}
			}
		}

		private synchronized void refresh() throws IOException {
			Iterator<Addition> additionIterator = additions.values().iterator();
			
			while (additionIterator.hasNext()) {
				Addition add = additionIterator.next();
				BytesRef ref = new BytesRef(add.text);
				add.addToWeight(lookup.getWeightFor(ref));
				//lookup.
                ((AnalyzingInfixSuggester)lookup.getDelegate()).update(ref, null, add.weight.get(), ref);
				additionIterator.remove();
			}

			long start = System.currentTimeMillis();
            ((AnalyzingInfixSuggester)lookup.getDelegate()).refresh();
			lastRefresh = System.currentTimeMillis();
			Logger.debug(lookup.getClass().getName() + " refreshs " + lookup.getCount() + " entries in "
					+ String.format("%1$.2fs", 1e-3 * (lastRefresh - start)));
			dirty.set(false);

		}

		@Override
		public void close() throws IOException {
			refreshIfDirty();
            ((AnalyzingInfixSuggester)lookup.getDelegate()).close();
		}

		long build() throws IOException {
			IndexReader reader = DirectoryReader.open(indexWriter, true);
			// now weight field
			long start = System.currentTimeMillis();
			lookup.build(new DocumentDictionary(reader, name, null));
			long count = lookup.getCount();
			Logger.debug(lookup.getClass().getName() + " builds " + count + " entries in "
					+ String.format("%1$.2fs", 1e-3 * (System.currentTimeMillis() - start)));
			return count;
		}

		List<SuggestResult> suggest(CharSequence key, int max) throws IOException {
			refreshIfDirty();
            return lookup.lookup(key, null, false, max).stream()
//                    .peek(l -> System.out.println(l.key))
					.map(r -> new SuggestResult(r.payload.utf8ToString(), r.key, r.value))
					.collect(Collectors.toList());


		}
	}

	class FlushDaemon implements Runnable {

		private ReentrantLock latch = new ReentrantLock();
		FlushDaemon() {
		}

		protected void lockFlush(){
			latch.lock();
		}

		protected void unLockFlush(){
			latch.unlock();
		}

		public void run() {

            if(!latch.tryLock()){
                //someone else has the lock
                //we won't wait the schedule deamon
                //will re-run us soon anyay
                return;
            }
			try {
                // Don't execute if already shutdown
				if(isShutDown || isReindexing){
					return;
				}
				execute();
			}finally {
				latch.unlock();
			}
		}

		/**
		 * Execute the flush, with debugging statistics, without looking at the
		 * shutdown state
		 */
		public void execute() {
			StopWatch.timeElapsed(this::flush);
		}

		private void flush() {
			File configFile = getFacetsConfigFile();
			if (TextIndexer.this.hasBeenModifiedSince(configFile.lastModified())) {
				Logger.debug(
						Thread.currentThread() + ": " + getClass().getName() + " writing FacetsConfig " + new Date());
				saveFacetsConfig(configFile, facetsConfig);
			}

			File sortFile = getSorterConfigFile();
			if (TextIndexer.this.hasBeenModifiedSince(sortFile.lastModified())) {
				saveSorters(sortFile, sorters);
			}

			if (indexWriter.hasUncommittedChanges()) {
				Logger.debug("Committing index changes...");
				try {
					indexWriter.commit();
					taxonWriter.commit();
				} catch (IOException ex) {
					ex.printStackTrace();
					try {
						indexWriter.rollback();
						taxonWriter.rollback();
					} catch (IOException exx) {
						exx.printStackTrace();
					}
				}

				for (SuggestLookup lookup : lookups.values())
					lookup.refreshIfDirty();
			}

		}
	}

	private File baseDir;
	private File suggestDir;
	private Directory indexDir;
	private Directory taxonDir;
	private IndexWriter indexWriter;
	// private DirectoryReader indexReader;
	private Analyzer indexAnalyzer;
	private DirectoryTaxonomyWriter taxonWriter;
	private FacetsConfig facetsConfig;
	
	public FacetsConfig getFacetsConfig() {
        return facetsConfig;
    }


    private ConcurrentMap<String, SuggestLookup> lookups;
	private ConcurrentMap<String, SortField.Type> sorters;
	
	
	private AtomicLong lastModified = new AtomicLong();

	private ExecutorService threadPool;
	private ScheduledExecutorService scheduler;

	private boolean isEmptyPool;

	// private Future[] fetchWorkers;
	// private BlockingQueue<SearchResultPayload> fetchQueue =
	// new LinkedBlockingQueue<SearchResultPayload>();

	static ConcurrentMap<File, TextIndexer> indexers;

	private File indexFileDir, facetFileDir;

	private boolean isShutDown = false;

	private boolean isReindexing = false;

	private FlushDaemon flushDaemon;

	SearcherManager searchManager;

	
	private static Set<String> deepKinds;
	static {
		init();
	}

	public static void init() {
		AtomicBoolean isInitialized= ALREADY_INITIALIZED.get();
		if (!isInitialized.get()) {
			if (indexers != null) {
				indexers.forEach((k, v) -> {
					System.out.println("init shutdown " + k.getAbsolutePath());
					v.shutdown();
				});
			}
			FETCH_WORKERS = ConfigHelper.getInt("ix.fetchWorkerCount",4);
			deepKinds = Play.application().configuration()
					.getStringList("ix.index.deepfields", new ArrayList<>())
					.stream()
					.map(s->{
						try{
							return EntityUtils.getEntityInfoFor(s).getTypeAndSubTypes();
						}catch(Exception e){
							e.printStackTrace();
							return null;
						}
					})
					.filter(Objects::nonNull)
					.flatMap(Collection::stream)
					.map(ei->ei.getName())
					.collect(Collectors.toSet());
			
			
			indexers = new ConcurrentHashMap<File, TextIndexer>();

			isInitialized.set(true);
		}
	}


	public static TextIndexer getInstance(File baseDir) throws IOException {

		return indexers.computeIfAbsent(baseDir, dir -> {
			try {
				return new TextIndexer(dir);
			} catch (IOException ex) {
				ex.printStackTrace();
				return null;
			}
		});

	}

	private TextIndexer() {
		// empty instance should only be used for
		// facet subsearching so we only need to have
		// a single thread...
		threadPool = ForkJoinPool.commonPool();
		scheduler = null;
		isShutDown = false;
		isEmptyPool = true;

	}

	public TextIndexer(File dir) throws IOException {
		this.baseDir = dir;
		threadPool = Executors.newFixedThreadPool(FETCH_WORKERS);
		scheduler = Executors.newSingleThreadScheduledExecutor();
		isShutDown = false;
		isEmptyPool = false;

		// Path dirPath = baseDir.toPath();
		if (dir.exists() && !dir.isDirectory())
			throw new IllegalArgumentException("Not a directory: " + dir);

		indexFileDir = new File(dir, "index");
		Files.createDirectories(indexFileDir.toPath());
		//
		// if (!indexFileDir.exists())
		// indexFileDir.mkdirs();
		indexDir = new NIOFSDirectory(indexFileDir, NoLockFactory.getNoLockFactory());

		facetFileDir = new File(dir, "facet");
		Files.createDirectories(facetFileDir.toPath());
		// if (!facetFileDir.exists())
		// facetFileDir.mkdirs();
		taxonDir = new NIOFSDirectory(facetFileDir, NoLockFactory.getNoLockFactory());

		indexAnalyzer = createIndexAnalyzer();
		IndexWriterConfig conf = new IndexWriterConfig(LUCENE_VERSION, indexAnalyzer);
		indexWriter = new IndexWriter(indexDir, conf);

		searchManager = new SearcherManager(indexWriter, true, null);

		// indexReader = DirectoryReader.open(indexWriter, true);
		taxonWriter = new DirectoryTaxonomyWriter(taxonDir);

		
		
		facetsConfig = loadFacetsConfig(new File(dir, FACETS_CONFIG_FILE));
		if (facetsConfig == null) {
			int size = taxonWriter.getSize();
			if (size > 0) {
				Logger.warn("There are " + size + " dimensions in " + "taxonomy but no facet\nconfiguration found; "
						+ "facet searching might not work properly!");
			}
			facetsConfig = new FacetsConfig();
			facetsConfig.setMultiValued(DIM_CLASS, true);
			facetsConfig.setRequireDimCount(DIM_CLASS, true);
		}

		suggestDir = new File(dir, "suggest");
		Files.createDirectories(suggestDir.toPath());
		// if (!suggestDir.exists())
		// suggestDir.mkdirs();

		// load saved lookups
		lookups = new ConcurrentHashMap<String, SuggestLookup>();
		
		for (File f : suggestDir.listFiles()) {
			if (f.isDirectory()) {
				try {
					lookups.put(f.getName(), new SuggestLookup(f));
				} catch (IOException ex) {
					ex.printStackTrace();
					Logger.error("Unable to load lookup from " + f, ex);
				}
			}
		}
		
		Logger.info("## " + suggestDir + ": " + lookups.size() + " lookups loaded!");

		sorters = loadSorters(new File(dir, SORTER_CONFIG_FILE));
		Logger.info("## " + sorters.size() + " sort fields defined!");

		// setFetchWorkers (FETCH_WORKERS);

		flushDaemon = new FlushDaemon();
		// run daemon every 10s
		scheduler.scheduleWithFixedDelay(flushDaemon, 10, 35, TimeUnit.SECONDS);
	}

	@FunctionalInterface
	interface SearcherFunction<R> {
		R apply(IndexSearcher indexSearcher) throws Exception;
	}

	private <R> R withSearcher(SearcherFunction<R> worker) throws Exception {
		searchManager.maybeRefresh();
		IndexSearcher searcher = searchManager.acquire();
		try {
			return worker.apply(searcher); //what happens if this starts using the
										   //searcher in another thread?
		} finally {
			searchManager.release(searcher); 
		}
	}

	static boolean DEBUG(int level) {
		Global g = Global.getInstance();
		if (g != null)
			return g.debug(level);
		return false;
	}

	@SuppressWarnings("deprecation")
	static Analyzer createIndexAnalyzer() {
		Map<String, Analyzer> fields = new HashMap<String, Analyzer>();
		fields.put(FIELD_ID, new KeywordAnalyzer());
		fields.put(FIELD_KIND, new KeywordAnalyzer());
        //dkatzel 2017-08 no stop words
        return new PerFieldAnalyzerWrapper(new StandardAnalyzer(LUCENE_VERSION, CharArraySet.EMPTY_SET), fields);
	}

	/**
	 * Create a empty RAM instance. This is useful for searching/filtering of a
	 * subset of the documents stored.
	 */
	public TextIndexer createEmptyInstance() throws IOException {
		TextIndexer indexer = new TextIndexer();
		indexer.indexDir = new RAMDirectory();
		indexer.taxonDir = new RAMDirectory();
		return config(indexer);
	}

	protected TextIndexer config(TextIndexer indexer) throws IOException {
		indexer.indexAnalyzer = createIndexAnalyzer();
		IndexWriterConfig conf = new IndexWriterConfig(LUCENE_VERSION, indexer.indexAnalyzer);
		indexer.indexWriter = new IndexWriter(indexer.indexDir, conf);

		indexer.searchManager = new SearcherManager(indexer.indexWriter, true, null);
		indexer.taxonWriter = new DirectoryTaxonomyWriter(indexer.taxonDir);
		indexer.facetsConfig = new FacetsConfig();
		
		//This should also be reset by the re-indexing trigger
		facetsConfig.getDimConfigs().forEach((dim,dconf)->{
			indexer.facetsConfig.setHierarchical(dim, dconf.hierarchical);
			indexer.facetsConfig.setMultiValued(dim, dconf.multiValued);
			indexer.facetsConfig.setRequireDimCount(dim, dconf.requireDimCount);
		});
		
		
		indexer.lookups = new ConcurrentHashMap<String, SuggestLookup>();
		indexer.sorters = new ConcurrentHashMap<String, SortField.Type>();
		indexer.sorters.putAll(sorters);
		return indexer;
	}
	
	

	public List<SuggestResult> suggest(String field, CharSequence key, int max) throws IOException {
		SuggestLookup lookup = lookups.get(field);
		if (lookup == null) {
			Logger.debug("Unknown suggest field \"" + field + "\"");
			return Collections.emptyList();
		}

		return lookup.suggest(key, max);
	}

	public Collection<String> getSuggestFields() {
		return Collections.unmodifiableCollection(lookups.keySet());
	}

	/**
	 * Returns the number of documents indexed here.
	 * 
	 * Returns -1 if there is an issue opening the index.
	 * @return
	 */
	public int size() {
		try {
			return withSearcher(s -> s.getIndexReader().numDocs());
		} catch (Exception ex) {
			Logger.trace("Can't retrieve NumDocs", ex);
		}
		return -1;
	}
	
	public static class IxQueryParser extends QueryParser {

		private static final Pattern ROOT_CONTEXT_ADDER=
				Pattern.compile("(\\b(?!" + ROOT + ")[^ :]*_[^ :]*[:])");

		//TODO setting the default operation to AND
        //will make split words on white space act as an && operation not an or
        //so phrases won't have to be quoted

		protected IxQueryParser(CharStream charStream) {
			super(charStream);
//			setDefaultOperator(QueryParser.AND_OPERATOR);
		}
		
		public IxQueryParser(String def) {
            super(def, createIndexAnalyzer());

//            setDefaultOperator(QueryParser.AND_OPERATOR);
        }
		
		public IxQueryParser(String string, Analyzer indexAnalyzer) {
			super(string, indexAnalyzer);

//            setDefaultOperator(QueryParser.AND_OPERATOR);
		}

		@Override
		public Query parse(String qtext) throws ParseException {
			if (qtext != null) {
				qtext = transformQueryForExactMatch(qtext);
			}
			// add ROOT prefix to all term queries (containing '_') where not
			// otherwise specified
			qtext = ROOT_CONTEXT_ADDER.matcher(qtext).replaceAll(ROOT + "_$1");
			
			//If there's an error parsing, it probably needs to have
			//quotes. Likely this happens from ":" chars
			try{
				return super.parse(qtext);
			}catch(Exception e){
				return super.parse("\"" + qtext  +"\"");
			}
		}
	}


	public SearchResult search(String text, int size) throws IOException {
		return search(new SearchOptions(null, size, 0, 10), text);
	}
	public SearchResult search(SearchOptions options, String text) throws IOException {
		return search(options, text, null);
	}
	public SearchResult search(SearchOptions options, String qtext, Collection<?> subset) throws IOException {
		SearchResult searchResult = new SearchResult(options, qtext);

		Supplier<Query> qs = ()->{
		    Query query=null;
    		if (qtext == null) {
    			query = new MatchAllDocsQuery();
    		} else {
    			try {
    				QueryParser parser = new IxQueryParser(FULL_TEXT_FIELD, indexAnalyzer);
                    turnOnSuffixSearchIfNeeded(qtext, parser);
    				query = parser.parse(qtext);
    			} catch (ParseException ex) {
    				ex.printStackTrace();
    				Logger.warn("Can't parse query expression: " + qtext, ex);
    				throw new IllegalStateException(ex);
    			}
    		}
    		return query;
		};
		Supplier<Filter> fs = ()->{
			Filter f = null;
			if (subset != null) {
				List<Term> terms = getTerms(subset);
				if (!terms.isEmpty()){
					f = new TermsFilter(terms);
				}
				if(options.getOrder().isEmpty() || 
				   options.getOrder().stream().collect(Collectors.joining("_")).equals("default")){
					Stream<Key> ids = subset.stream()
											.map(o->EntityWrapper.of(o).getKey());
					Comparator<Key> comp=Util.comparator(ids);
					
					searchResult.setRank(comp);
				}
			} else if (options.getKind() != null) {
				f = createKindArrayFromOptions(options);
			}
			return f;
		};
		Query q=qs.get();
		Filter f=fs.get();
		
		try{
		    search(searchResult, q, f);
		}catch(Exception e){
		    e.printStackTrace();
		    throw new IOException(e);
		}
        
		return searchResult;
	}

	private static FieldCacheTermsFilter filterForKinds(Class<?> cls){
	    EntityInfo einfo = EntityUtils.getEntityInfoFor(cls);
        return filterForKinds(einfo);
    }
	private static FieldCacheTermsFilter filterForKinds(EntityInfo<?> einfo){
	    String[] opts= einfo.getTypeAndSubTypes()
                            .stream()
                            .map(s->s.getName())
                            .collect(Collectors.toList())
                            .toArray(new String[0]);
	    return new FieldCacheTermsFilter(FIELD_KIND, opts);
	}
	
	
	
	private FieldCacheTermsFilter createKindArrayFromOptions(SearchOptions options) {
		return filterForKinds(options.getKindInfo());
	}

	public SearchResult filter(Collection<?> subset) throws Exception {
		SearchOptions options = new SearchOptions(null, subset.size(), 0, subset.size() / 2);
		return filter(options, subset);
	}

	protected List<Term> getTerms(Collection<?> subset) {
		return subset.stream()
				.map(this::getTerm)
				.filter(t -> t != null)
				.collect(Collectors.toList());
	}
	
	protected List<Term> getTermsFromKeys(Collection<Key> subset) {
        return subset.stream()
                .map(this::getTerm)
                .filter(t -> t != null)
                .collect(Collectors.toList());
    }
	
	protected TermsFilter getTermsFilter(Collection<?> subset) {
		return new TermsFilter(getTerms(subset));
	}
	
	protected TermsFilter getTermKeysFilter(Collection<Key> keyset) {
        return new TermsFilter(getTermsFromKeys(keyset));
    }


	public SearchResult filter(SearchOptions options, Collection<?> subset) throws Exception {
		return filter(options, getTermsFilter(subset));
	}

	public SearchResult range(SearchOptions options, String field, Integer min, Integer max) throws Exception {
		Query query = NumericRangeQuery.newIntRange(field, min, max, true /* minInclusive? */, true/* maxInclusive? */);
		return search(new SearchResult(options), query, null);
	}

	protected SearchResult filter(SearchOptions options, Filter filter) throws Exception {
		return search(new SearchResult(options), new MatchAllDocsQuery(), filter);
	}

	protected SearchResult search(SearchResult searchResult, Query query, Filter filter) throws Exception {
		return withSearcher(searcher -> search(searcher, searchResult, query, filter));
	}

	public Map<String,List<Filter>> createAndRemoveRangeFiltersFromOptions(SearchOptions options) {
		Map<String, List<Filter>> filters = new HashMap<String,List<Filter>>();
		options.removeAndConsumeRangeFilters((f,r)->{
			filters
			    .computeIfAbsent(f, k -> new ArrayList<Filter>())
				.add(FieldCacheRangeFilter.newLongRange(f, r[0], r[1], true, false));
		});
		return filters;
	}
	
	
		
	public Sort createSorterFromOptions(SearchOptions options) {
		Sort sorter = null;
		if (!options.getOrder().isEmpty()) {
			List<SortField> fields = new ArrayList<SortField>();
			for (String f : options.getOrder()) {
				boolean rev = false;
				if (f.charAt(0) == SORT_ASCENDING_CHAR) {
					f = f.substring(1);
				} else if (f.charAt(0) == SORT_DESCENDING_CHAR) {
					f = f.substring(1);
					rev = true;
				}
				// Find the correct sorter field. The sorter fields
				// always have the SORT_PREFIX prefix, and should also have
				// a ROOT prefix for the full path. If the root prefix is
				// not
				// present, this will add it.

				SortField.Type type = sorters.get(TextIndexer.SORT_PREFIX + f);
				if (type == null) {
					type = sorters.get(TextIndexer.SORT_PREFIX + ROOT + "_" + f);
					f = TextIndexer.SORT_PREFIX + ROOT + "_" + f;
				} else {
					f = TextIndexer.SORT_PREFIX + f;
				}
				if (type != null) {
					SortField sf = new SortField(f, type, rev);
					Logger.debug("Sort field (rev=" + rev + "): " + sf);
					fields.add(sf);
				} else {
					//System.out.println("Couldn't find sorter:" + f + " in " + sorters.keySet().toString());
					Logger.warn("Unknown sort field: \"" + f + "\"");
				}
			}

			if (!fields.isEmpty()) {
				sorter = new Sort(fields.toArray(new SortField[0]));
			}
			
			
		}
		return sorter;
	}
	
	
	
	public static void collectBasicFacets(Facets facets, SearchResult sr) throws IOException{
		Map<String,List<DrillAndPath>> providedDrills = sr.getOptions().getDrillDownsMap();
		
		List<FacetResult> facetResults = facets.getAllDims(sr.getOptions().getFdim());
		if (DEBUG(1)) {
			Logger.info("## Drilled " + (sr.getOptions().isSideway() ? "sideway" : "down") + " " + facetResults.size()+ " facets");
		}
		
		//Convert FacetResult -> Facet, and add to 
		//search result
		facetResults.stream()
			.filter(Objects::nonNull)
			.map(result -> {
				Facet fac = new Facet(result.dim, sr);
				
				// make sure the facet value is returned
				// for selected value
				
				List<DrillAndPath> dp = providedDrills.get(result.dim);
				if (dp != null) {
				    List<String> selected=dp.stream()
				                        .map(l->l.asLabel())
				                        .collect(Collectors.toList());
					fac.setSelectedLabels(selected);
				}
				
				for(LabelAndValue lv:result.labelValues){
				    fac.add(lv.label, lv.value.intValue());
				}
				
				fac.getMissingSelections().stream().forEach(l->{
				    try {
                        Number value = facets.getSpecificValue(result.dim, l);
                        if (value != null && value.intValue() >= 0) {
                            fac.add(l, value.intValue());
                        } else {
                            Logger.warn("Facet \"" + result.dim + "\" doesn't have any " + "value for label \""
                                    + l + "\"!");
                        }
                    } catch (Exception e) {
                       Logger.warn("error collecting facets", e);
                    }
				});
				
				fac.sort();
				return fac;
			})
			.forEach(f -> sr.addFacet(f));
	}
	
	
	// Abstracted interface to help with cleaning up workflow
	// Call search, then call getTopDocs and/or getFacets
	
	public static interface LuceneSearchProvider{
		public LuceneSearchProviderResult search(IndexSearcher searcher, TaxonomyReader taxon, Query q,FacetsCollector facetCollector) throws IOException;
	}
	public static interface LuceneSearchProviderResult{
		public TopDocs getTopDocs();
		public Facets getFacets();
	}
	public static class DefaultLuceneSearchProviderResult implements LuceneSearchProviderResult{
		private TopDocs hits=null;
		private Facets facets=null;
		public DefaultLuceneSearchProviderResult(TopDocs hits,Facets facets){
			this.hits=hits;
			this.facets=facets;
		}
		@Override
		public TopDocs getTopDocs() {
			return hits;
		}
		@Override
		public Facets getFacets() {
			return facets;
		}
		
	}
	
	
	public class BasicLuceneSearchProvider implements LuceneSearchProvider{
		private Sort sorter;
		private Filter filter;
		private int max;
		
		public BasicLuceneSearchProvider(Sort sorter,Filter filter, int max){
			this.sorter=sorter;
			this.filter=filter;
			this.max=max;
		}

		@Override
		public DefaultLuceneSearchProviderResult search(IndexSearcher searcher, TaxonomyReader taxon, Query query, FacetsCollector facetCollector) throws IOException {
			TopDocs hits=null;
			Facets facets=null;
			//FacetsCollector.
			//with sorter
			if (sorter != null) { 
				hits = (FacetsCollector.search(searcher, query, filter, max, sorter, facetCollector));
			//without sorter
			}else { 
				hits = (FacetsCollector.search(searcher, query, filter, max, facetCollector));
			}
			facets = new FastTaxonomyFacetCounts(taxon, facetsConfig, facetCollector);
			return new DefaultLuceneSearchProviderResult(hits,facets);
		}
		
	}
	public class DrillSidewaysLuceneSearchProvider implements LuceneSearchProvider{
		private TopDocs hits=null;
		private Facets facets=null;
		private Sort sorter;
		private Filter filter;
		private SearchOptions options;
		
		public DrillSidewaysLuceneSearchProvider(Sort sorter,Filter filter, SearchOptions options){
			this.sorter=sorter;
			this.filter=filter;
			this.options=options;	
		}

		@Override
		public LuceneSearchProviderResult search(IndexSearcher searcher, TaxonomyReader taxon, Query ddq1, FacetsCollector facetCollector) throws IOException {
			if(!(ddq1 instanceof DrillDownQuery)){
				throw new IllegalStateException("Query must be drill down query");
			}
			DrillDownQuery ddq = (DrillDownQuery)ddq1;
			DrillSideways sideway = new DrillSideways(searcher, facetsConfig, taxon);
			
			
			DrillSideways.DrillSidewaysResult swResult = sideway.search(ddq, filter, null, options.max(),
					sorter, false, false);
			
			
			
			/*
			 * TODO: is this the only way to collect the counts for
			 * range/dynamic facets?
			 * 
			 */
			if (!options.getLongRangeFacets().isEmpty()){
				FacetsCollector.search(searcher, ddq, filter, options.max(), facetCollector);
			}

			facets = swResult.facets;
			hits = swResult.hits;
			return new DefaultLuceneSearchProviderResult(hits,facets);
		}
		
	}

	// This is the most important method, everything goes here
	protected SearchResult search(IndexSearcher searcher, SearchResult searchResult, Query query, Filter filter)
			throws IOException {
		SearchOptions options = searchResult.getOptions();

		if (DEBUG(1)) {
			Logger.debug("## Query: " + query + " Filter: " + (filter != null ? filter.getClass() : "none")
					+ " Options:" + options);
		}

		long start = TimeUtil.getCurrentTimeMillis();

		final TopDocs hits;

		try (TaxonomyReader taxon = new DirectoryTaxonomyReader(taxonWriter)) {
		    hits=firstPassLuceneSearch(searcher,taxon,searchResult,filter, query);
		}
		
		if (DEBUG(1)) {
			Logger.debug(
					"## Query executes in " 
							+ String.format("%1$.3fs", (TimeUtil.getCurrentTimeMillis() - start) * 1e-3)
							+ "..." 
							+ hits.totalHits 
							+ " hit(s) found!");
		}

		try {
			LuceneSearchResultPopulator payload = new LuceneSearchResultPopulator(searchResult, hits, searcher);
			//get everything, forever
			if (options.getFetch() <= 0) {
				payload.fetch();
			} else {
				// we first block until we have enough result to show
				// should be fetch plus a little extra padding (2 here)
				// why 2?
			    // TODO: should we really block here?
				int fetch = options.getFetch() + EXTRA_PADDING;
				
				payload.fetch(fetch);
				
				if (hits.totalHits > fetch) {
					// now queue the payload so the remainder is fetched in
					// the background
					// fetchQueue.put(payload);
					threadPool.submit(() -> {
						try {
							withSearcher(s->{
									long tstart = System.currentTimeMillis();
									Logger.debug(Thread.currentThread() + ": fetching payload " + payload.hits.totalHits
											+ " for " + payload.result);
									payload.setSearcher(s);
									try{
										payload.fetch();
									}catch(InterruptedException e){
										throw new IOException(e); //just to make it throw through withSearcher
									}
									Logger.debug(Thread.currentThread() + ": ## fetched " + payload.result.size()
											+ " for result " + payload.result + " in "
											+ String.format("%1$dms", System.currentTimeMillis() - tstart));
								return payload;
							});
						} catch (Exception e) {
							e.printStackTrace();
							Logger.error("Error in processing payload", e);
						}
						
					});
				} else {
					searchResult.done();
				}
				
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			Logger.trace("Can't queue fetch results!", ex);
		}

		return searchResult;
	}

	/**
	 * Performs a basic Lucene query using the provided Filter and Query, and any other
	 * refining information from the SearchResult options. Facet results are placed in
	 * the provided SearchResult, and the TopDocs hits from the Lucene search are returned.
	 * 
	 * @param searcher
	 * @param taxon
	 * @param searchResult
	 * @param ifilter
	 * @param query
	 * @return
	 * @throws IOException
	 */
	public TopDocs firstPassLuceneSearch(IndexSearcher searcher,TaxonomyReader taxon, SearchResult searchResult, Filter ifilter, Query query) throws IOException{
		final TopDocs hits;
		SearchOptions options = searchResult.getOptions();
		FacetsCollector facetCollector = new FacetsCollector();
		LuceneSearchProvider lsp;
		
		Filter filter = ifilter;
		
		// You may wonder why some of these options parsing 
		// elements are directly accessible from SearchOptions
		// methods, while others have external parsing functions,
		// while seeming to have the same object dependencies...
		//
		// That's really just to avoid having Lucene-specific
		// code in the SearchOptions class. If it's Lucene-specific,
		// then the parser is here. If it's a more general function,
		// then it's put into SearchOptions directly.
		//
		// This may change in the future

		Sort sorter = createSorterFromOptions(options);
		
		
		List<Filter> filtersFromOptions = createAndRemoveRangeFiltersFromOptions(options)
				.values()
				.stream()
				.map(val->new ChainedFilter(val.toArray(new Filter[0]), ChainedFilter.OR))
				.collect(Collectors.toList());
		
		options.getTermFilters()
		    .stream()
			.map(k-> new TermsFilter(new Term(k.getField(), k.getTerm())))
			.forEach(f->filtersFromOptions.add(f));
		

		Query qactual = query;
			
		//Collect the range filters into one giant filter.
		//Specifically, each element of a group of filters is set
		//to be joined by an "OR", while each group is joined
		//by "AND" to the other groups
		if(!filtersFromOptions.isEmpty()){
			filtersFromOptions.add(ifilter);
			filter = new ChainedFilter(filtersFromOptions.stream()
										.collect(Collectors.toList())
										.toArray(new Filter[0]), ChainedFilter.AND);
			filtersFromOptions.remove(filtersFromOptions.size()-1);
		}
		
		//no specified facets (normal search)
		if (options.getFacets().isEmpty()) { 
			lsp = new BasicLuceneSearchProvider(sorter, filter, options.max());
		} else {
			DrillDownQuery ddq = new DrillDownQuery(facetsConfig, query);
			
			options.getDrillDownsMap()
			    .entrySet()
			    .stream()
			    .flatMap(e->e.getValue().stream())
			    .forEach((dp)->{
			        ddq.add(dp.getDrill(), dp.getPaths());
			    });
			
			qactual=ddq;

			// sideways
			if (options.isSideway()) {
				lsp = new DrillSidewaysLuceneSearchProvider(sorter, filter, options);
			
			// drilldown
			} else { 
				lsp = new BasicLuceneSearchProvider(sorter, filter, options.max());
			}
		} // facets is empty

		
		//Promote special matches
		if(searchResult.getOptions().getKindInfo() !=null){
		    //Special "promoted" match types
			Set<String> sponsoredFields = searchResult.getOptions()
			                                           .getKindInfo()
			                                           .getSponsoredFields();

			if (searchResult.getQuery() != null) {
				try {
				    for (String sp : sponsoredFields) {
						String theQuery = "\"" + toExactMatchString(
								TextIndexer.replaceSpecialCharsForExactMatch(searchResult.getQuery().trim().replace("\"", ""))).toLowerCase() + "\"";
						QueryParser parser = new IxQueryParser(sp, indexAnalyzer);

						Query tq = parser.parse(theQuery);
						LuceneSearchProviderResult lspResult = lsp.search(searcher, taxon, tq,new FacetsCollector()); //special q
						TopDocs td = lspResult.getTopDocs();
						for (int j = 0; j < td.scoreDocs.length; j++) {
							Document doc = searcher.doc(td.scoreDocs[j].doc);
							try {
								Key k = Key.of(doc);
								searchResult.addSponsoredNamedCallable(new EntityFetcher<>(k));
							} catch (Exception e) {
								e.printStackTrace();
								
								Logger.error(e.getMessage());
							}
						}

					}
				} catch (Exception ex) {
				    Logger.warn("Error performing lucene search", ex);
				}
			}
		}


		LuceneSearchProviderResult lspResult=lsp.search(searcher, taxon,qactual,facetCollector);
		hits=lspResult.getTopDocs();
		
		collectBasicFacets(lspResult.getFacets(), searchResult);
		
		collectLongRangeFacets(facetCollector, searchResult);
		
		if(USE_ANALYSIS && options.getKind()!=null){
			EntityInfo<?> entityMeta= EntityUtils.getEntityInfoFor(options.getKind());
			
			FieldNameDecorator fnd=FieldNameDecoratorFactory
										.getInstance(Play.application())
										.getSingleResourceFor(entityMeta);
			
			getQueryBreakDownFor(query).stream().forEach(oq->{
				try{
					FacetsCollector facetCollector2 = new FacetsCollector();
					Filter f=null;
					if(options.getKind()!=null){
						EntityUtils.getEntityInfoFor(options.getKind());
						List<String> analyzers = entityMeta.getTypeAndSubTypes()
									.stream()
									.map(e->e.getName())
									.map(n->ANALYZER_VAL_PREFIX + n)
									.collect(Collectors.toList());
						
						f = new FieldCacheTermsFilter(FIELD_KIND, analyzers.toArray(new String[0]));
					}
					LuceneSearchProvider lsp2 = new BasicLuceneSearchProvider(null, f, options.max());
					LuceneSearchProviderResult res=lsp2.search(searcher, taxon,oq.k(),facetCollector2);
					res.getFacets().getAllDims(options.getFdim()).forEach(fr->{
						if(fr.dim.equals(TextIndexer.ANALYZER_FIELD)){
							
						Arrays.stream(fr.labelValues).forEach(lv->{
								String newQuery = serializeAndRestrictQueryToField(oq.k(),lv.label);
								searchResult.addFieldQueryFacet(
										new FieldedQueryFacet(lv.label)
												.withExplicitCount(lv.value.intValue())
												.withExplicitQuery(newQuery)
												.withExplicitMatchType(oq.v())
												.withExplicitDisplayField(fnd.getDisplayName(lv.label))
												);
								});
						}
					});
				}catch(Exception e){
					Logger.warn("Error analyzing query:" + e.getMessage(), e);
				}
			});
		} //End of Idea
		
		return hits;
	}
	
	public IxQueryParser getQueryParser(String def){
		return new IxQueryParser(def, indexAnalyzer);
	}
	
	public IxQueryParser getQueryParser(){
		return getQueryParser(FULL_TEXT_FIELD);
	}
	
	public Query parseQuery(String q) throws Exception{
        QueryParser parser = getQueryParser();
        turnOnSuffixSearchIfNeeded(q, parser);


        Query q1=parser.parse(q);
		//return q1;
		return withSearcher(s -> q1.rewrite(s.getIndexReader()));
	}

    private void turnOnSuffixSearchIfNeeded(String q, QueryParser parser) {

        if(q ==null || q.isEmpty()){
            return;
        }
        String queryPart = q.substring(q.indexOf(':')+1);
        if(queryPart.charAt(0) == '*'){
            //suffix search
            parser.setAllowLeadingWildcard(true);
        }
    }

    /**
	 * Prepare a given query to be more specified by restricting it to the field
	 * provided, and using 
	 * @param q
	 * @param field
	 * @return
	 */
	public static String serializeAndRestrictQueryToField(Query q, String field){
		String qAsIs=q.toString();
		
		//replace all mentions of text: with the actual field name provided
		qAsIs=qAsIs.replace(FULL_TEXT_FIELD + ":", field.replace(" ", "\\ ") + ":");
		
		//START_WORD and STOP_WORD better be good regexes
		qAsIs=Util.replaceIgnoreCase(qAsIs,TextIndexer.START_WORD, TextIndexer.GIVEN_START_WORD);
		qAsIs=Util.replaceIgnoreCase(qAsIs,TextIndexer.STOP_WORD, TextIndexer.GIVEN_STOP_WORD);
		return qAsIs;
	}
	
	
	
	/**
	 * This method attempts to generate suggested variant queries (more specific)
	 * from an existing query. If this query deals with a non-generic field, or other
	 * query unlikely to be interpretable in analysis, it returns an empty list.  
	 * 
	 * <pre>
	 * If you provide -> Maybe you're actually interested in is
	 * 
	 * Single Term Query      -> [Single Term Query, Exact Full Term Query]
	 * "OR" Term Query        -> [Exact Full Term Query, "OR" Term Query, Phrase Term Query]
	 * Phrase Term Query      -> [Exact Full Term Query, Phrase Term Query]
	 * Exact Term Query       -> [Exact Full Term Query]
	 * Starts With Term Query -> [Exact Full Term Query, Starts With Term Query]
	 * Ends With Term Query   -> [Exact Full Term Query, Ends With Term Query]
	 * Starts With * Query    -> [Starts With * Query]
	 * Field-specific Query   -> []
	 * "NOT" Term Query       -> []
	 * Other                  -> []
	 * </pre>
	 * 
	 * 
	 * This method also includes a MATCH_TYPE response for each new Query,
	 * which is used in categorization of the Query types.
	 * 
	 * TODO: Align MATCH_TYPE options more with reality.
	 * 
	 * @param q
	 * @return
	 */
	public static List<Tuple<Query,MATCH_TYPE>> getQueryBreakDownFor(Query q){
		List<Tuple<Query,MATCH_TYPE>> suggestedQueries = new ArrayList<Tuple<Query,MATCH_TYPE>>();
		
		Function<Stream<Term>, PhraseQuery> exatctQueryMaker = lterms->{
			PhraseQuery exactQuery = new PhraseQuery();
			exactQuery.add(new Term(FULL_TEXT_FIELD,TextIndexer.START_WORD.trim().toLowerCase()));
			lterms.forEach(tq->{
				exactQuery.add(new Term(FULL_TEXT_FIELD,tq.text()));
			});
			exactQuery.add(new Term(FULL_TEXT_FIELD,TextIndexer.STOP_WORD.trim().toLowerCase()));
			return exactQuery;
		};
		Function<Stream<Term>, PhraseQuery> phraseQueryMaker = lterms->{
			PhraseQuery exactQuery = new PhraseQuery();
			lterms.forEach(tq->{
				exactQuery.add(new Term(FULL_TEXT_FIELD,tq.text()));
			});
			return exactQuery;
		};
		Predicate<Term> isGeneric = (t->t.field().equals(FULL_TEXT_FIELD));
		
		//First, we explicity allow TermQueries
		if(q instanceof TermQuery){
			Term tq=((TermQuery)q).getTerm();
			if(tq.field().equals(FULL_TEXT_FIELD)){
				PhraseQuery exactQuery = new PhraseQuery();
				exactQuery.add(new Term(FULL_TEXT_FIELD,TextIndexer.START_WORD.trim().toLowerCase()));
				exactQuery.add(new Term(FULL_TEXT_FIELD,tq.text()));
				exactQuery.add(new Term(FULL_TEXT_FIELD,TextIndexer.STOP_WORD.trim().toLowerCase()));
				suggestedQueries.add(new Tuple<Query,MATCH_TYPE>(exatctQueryMaker.apply(Stream.of(tq)),MATCH_TYPE.FULL));
				suggestedQueries.add(new Tuple<Query,MATCH_TYPE>(q,MATCH_TYPE.WORD));
			}
		}else if(q instanceof PhraseQuery){
			PhraseQuery pq =(PhraseQuery)q;
			Term[] terms=pq.getTerms();
			if(Arrays.stream(terms).allMatch(isGeneric)){
				boolean starts=terms[0].text().equalsIgnoreCase(TextIndexer.START_WORD.trim());
				boolean ends=terms[terms.length-1].text().equalsIgnoreCase(TextIndexer.STOP_WORD.trim());
				
				if(starts && ends){
					//was exact
					suggestedQueries.add(
							new Tuple<Query, MATCH_TYPE>(
									q,
									MATCH_TYPE.FULL)
								);
				}else if(starts){
					//System.out.println("Start Only:" + q.toString());
				}else if(ends){
					//System.out.println("Ends Only:" + q.toString());
				}else{
					suggestedQueries.add(
							new Tuple<Query, MATCH_TYPE>(
									exatctQueryMaker.apply(Arrays.stream(terms)),
									MATCH_TYPE.FULL)
								);
					
					suggestedQueries.add(
							new Tuple<Query, MATCH_TYPE>(
									q,
									MATCH_TYPE.WORD)
								);
				}
			}else{
				//Some field-specific Phrase query?
				//System.out.println("reject:" + q.toString());
			}	
		}else if(q instanceof BooleanQuery){
			BooleanQuery bq = (BooleanQuery)q;
			List<BooleanClause> bclauses = flattenToLinkedOr(bq);
			if(bclauses!=null){
				//ALL-OR Query
				List<Query> qs=bclauses.stream().map(b->b.getQuery()).collect(Collectors.toList());
				if(qs.stream().allMatch(qq->(qq instanceof TermQuery))){
					//All Terms
					List<Term> terms = qs.stream().map(qq -> ((TermQuery) qq).getTerm()).collect(Collectors.toList());
					if(terms.stream().allMatch(isGeneric)){
						suggestedQueries.add(
								new Tuple<Query, MATCH_TYPE>(
										exatctQueryMaker.apply(terms.stream()),
										MATCH_TYPE.FULL)
									);
						
						suggestedQueries.add(
								new Tuple<Query, MATCH_TYPE>(
										phraseQueryMaker.apply(terms.stream()),
										MATCH_TYPE.CONTAINS)
									);
						
						suggestedQueries.add(
								new Tuple<Query, MATCH_TYPE>(
										q,
										MATCH_TYPE.WORD)
									);
						
						
					}else{
						//More complex?
						//System.out.println("Specified term?" + q.toString());
					}
				}else{
					//More complex?
					//System.out.println("Non term?" + q.toString());
				}
			}else{
				//Boolean query is complex, with things other than "OR"
				//System.out.println("Something else QUERY" + q.toString());
			}
		}else if(q instanceof WildcardQuery){
			WildcardQuery wq = (WildcardQuery)q;
			if(isGeneric.test(wq.getTerm())){
				suggestedQueries.add(
						new Tuple<Query, MATCH_TYPE>(
								q,
								MATCH_TYPE.CONTAINS)
							);
			}else{
				//System.out.println("This is a non generic wildcard");
			}
		}else if(q instanceof PrefixQuery){
			PrefixQuery pq = (PrefixQuery)q;
			if(isGeneric.test(pq.getPrefix())){
				suggestedQueries.add(
						new Tuple<Query, MATCH_TYPE>(
								q,
								MATCH_TYPE.WORD_STARTS_WITH)
							);
			}else{
				//System.out.println("This is a non generic Prefix query");
			}
		}
		return suggestedQueries;
	}
	
	private static List<BooleanClause> flattenToLinkedOr(BooleanQuery bq){
		List<BooleanClause> bqs= new ArrayList<BooleanClause>();
		for(BooleanClause bcl: bq.getClauses()){
			if(bcl.getOccur() != Occur.SHOULD) return null;
			if(bcl.getQuery() instanceof BooleanQuery){
				List<BooleanClause> sublist = flattenToLinkedOr((BooleanQuery) bcl.getQuery());
				if(sublist==null){
					return null;
				}else{
					bqs.addAll(sublist);
				}
			}else{
				bqs.add(bcl);
			}
		}
		return bqs;
	}

	protected void collectLongRangeFacets(FacetsCollector fc, SearchResult searchResult) throws IOException {
		SearchOptions options = searchResult.getOptions();
		Map<String,List<DrillAndPath>> providedDrills = options.getDrillDownsMap();
		
		for (SearchOptions.FacetLongRange flr : options.getLongRangeFacets()) {
			if (flr.range.isEmpty())
				continue;

			Logger.debug("[Range facet: \"" + flr.field + "\"");
			
			//
			LongRange[] range = flr.range
			                .entrySet()
			                .stream().map(Tuple::of)
			                .map(me ->new LongRange(me.k(), me.v()[0], true, me.v()[1], true))
			                .toArray(i->new LongRange[i]);

			Facets facets = new LongRangeFacetCounts(flr.field, fc, range);
			FacetResult result = facets.getTopChildren(options.getFdim(), flr.field);
			Facet f = new Facet(result.dim, searchResult);
			
			if (DEBUG(1)) {
				Logger.info(" + [" + result.dim + "]");
			}
			
			
					
			
			Arrays.stream(result.labelValues)
				.forEach(lv->f.add(lv.label, lv.value.intValue()));
			
			List<DrillAndPath> dp = providedDrills.get(f.name);
			if (dp != null) {
			    List<String> selected=dp.stream()
			                        .map(l->l.asLabel())
			                        .collect(Collectors.toList());
				f.setSelectedLabels(selected);
			}
			
			searchResult.addFacet(f);
		}
	}

	protected <T> Term getTerm(T entity) {
		if (entity == null)
			return null;
		
		return EntityWrapper.of(entity)
		           .getOptionalKey()
                   .map(key->getTerm(key))
                   .orElseGet(()->{
                       Logger.warn("Entity " + entity + "[" + entity.getClass() + "] has no Id field!");
                       return null;
                   });
	}
	
	protected <T> Term getTerm(Key k){
	    Tuple<String,String> tup=k.asLuceneIdTuple();
	    return new Term(tup.k(),tup.v());
	}

	public Document getDoc(Object entity) throws Exception {
		Term term = getTerm(entity);
		if (term != null) {
			// IndexSearcher searcher = getSearcher ();
			withSearcher(searcher -> {
				TopDocs docs = searcher.search(new TermQuery(term), 1);
				if (docs.totalHits > 0) {
					return searcher.doc(docs.scoreDocs[0].doc);
				}
				return null;
			});
		}
		return null;
	}

	public JsonNode getDocJson(Object entity) throws Exception {
		Document _doc = getDoc(entity);
		if (_doc == null) {
			return null;
		}
		List<IndexableField> _fields = _doc.getFields();
		ObjectMapper mapper = new ObjectMapper();
		ArrayNode fields = mapper.createArrayNode();
		for (IndexableField f : _fields) {
			ObjectNode node = mapper.createObjectNode();
			node.put("name", f.name());
			if (null != f.numericValue()) {
				node.put("value", f.numericValue().doubleValue());
			} else {
				node.put("value", f.stringValue());
			}

			ObjectNode n = mapper.createObjectNode();
			IndexableFieldType type = f.fieldType();
			if (type.docValueType() != null)
				n.put("docValueType", type.docValueType().toString());
			n.put("indexed", type.indexed());
			n.put("indexOptions", type.indexOptions().toString());
			n.put("omitNorms", type.omitNorms());
			n.put("stored", type.stored());
			n.put("storeTermVectorOffsets", type.storeTermVectorOffsets());
			n.put("storeTermVectorPayloads", type.storeTermVectorPayloads());
			n.put("storeTermVectorPositions", type.storeTermVectorPositions());
			n.put("storeTermVectors", type.storeTermVectors());
			n.put("tokenized", type.tokenized());

			node.put("options", n);
			fields.add(node);
		}

		ObjectNode doc = mapper.createObjectNode();
		doc.put("num_fields", _fields.size());
		doc.put("fields", fields);
		return doc;
	}

	/**
	 * recursively index any object annotated with Entity
	 */
	public void add(EntityWrapper ew) throws IOException {
		if(!INDEXING_ENABLED)return;
		Objects.requireNonNull(ew);
		try{
			if(!ew.shouldIndex()){
				if (DEBUG(2)) {
					Logger.debug(">>> Not indexable " + ew.getValue());
				}
				return;
			}
			if (DEBUG(2)){
				Logger.debug(">>> Indexing " + ew.getValue() + "...");
			}
	
			CachedSupplier<Boolean> isDeep  = 
					CachedSupplier.of(()->deepKinds.contains(ew.getKind()));
			
			
			HashMap<String,List<TextField>> fullText = new HashMap<>();
            Document doc = new Document();
			
			Consumer<IndexableField> fieldCollector = f->{
					if(f instanceof TextField || f instanceof StringField){
						String text = f.stringValue();
						if (text != null) {
							if (DEBUG(2)){
								Logger.debug(".." + f.name() + ":" + text + " [" + f.getClass().getName() + "]");
							}
							TextField tf=new TextField(FULL_TEXT_FIELD, text, NO);
							//tf.set
							doc.add(tf);
							if(USE_ANALYSIS && isDeep.get() && f.name().startsWith(ROOT +"_")){
								fullText.computeIfAbsent(f.name(),k->new ArrayList<TextField>())
									.add(tf);
							}
						}
					}else if(f instanceof FacetField){
					    String key = ((FacetField)f).dim;
					    String text = ((FacetField)f).path[0];

					    if (text != null) {
					        TermVectorField tvf = new TermVectorField(TERM_VEC_PREFIX + key,text);
					        doc.add(tvf);
					    }
					}
					doc.add(f);
			};
			
			//flag the kind of document
			IndexValueMaker<Object> valueMaker= IndexValueMakerFactory
										.getInstance(Play.application())
										.getSingleResourceFor(ew.getEntityInfo());

			valueMaker.createIndexableValues(ew.getValue(), iv->{
				this.instrumentIndexableValue(fieldCollector, iv);
			});
			if(USE_ANALYSIS && isDeep.get() && ew.hasKey()){
				Key key =ew.getKey();
				if(!key.getIdString().equals("")){  //probably not needed
					StringField toAnalyze=new StringField(FIELD_KIND, ANALYZER_VAL_PREFIX + ew.getKind(),YES);
					
					Tuple<String,String> luceneKey = key.asLuceneIdTuple();
					StringField docParent=new StringField(ANALYZER_VAL_PREFIX+luceneKey.k(),luceneKey.v(),YES);
					FacetField  docParentFacet =new FacetField(ANALYZER_VAL_PREFIX+luceneKey.k(),luceneKey.v());
					//This is a test of a terrible idea, which just. might. work.
					fullText.forEach((name,group)->{
							try{
                                Document fielddoc = new Document();
								fielddoc.add(toAnalyze);
								fielddoc.add(docParent);
								fielddoc.add(docParentFacet);
								fielddoc.add(new FacetField(ANALYZER_FIELD,name));
								for(IndexableField f:group){
										fielddoc.add(f);
								}
								addDoc(fielddoc);
							}catch(Exception e){
								Logger.error("Analyzing index failed", e);
							}
						});
				}
			}
			
			fieldCollector.accept(new StringField(FIELD_KIND, ew.getKind(), YES));
			
			
			//System.out.println("Adding index for:" + ew.getKind());
			// now index
			addDoc(doc);
			
			if (DEBUG(2))
				Logger.debug("<<< " + ew.getValue());
		}catch(Exception e){
			e.printStackTrace();
			Logger.error("Error indexing record [" + ew.toString() + "] This may cause consistency problems", e);
		}finally{
			//System.out.println("||| " + ew.getKey());
		}
	}
	
	//One more thing:
	// 1. need list of fields indexed.
	// 2. that's easy! At index time, just also index each field
	// 3. in fact... it's already maybe present ...

	public void addDoc(Document doc) throws IOException {
		doc = facetsConfig.build(taxonWriter, doc);
		if (DEBUG(2))
			Logger.debug("++ adding document " + doc);
		indexWriter.addDocument(doc);
		markChange();
	}

	//TODO: Should be an interface, which can throw a DataHasChange event ... or something 
	// like that
	public void markChange(){
		lastModified.set(TimeUtil.getCurrentTimeMillis());
		IxCache.markChange();
		
	}
	
	
	
	public boolean hasBeenModifiedSince(long thistime){
		if(lastModified()>thistime)return true;
		return false;
	}
	
	public long lastModified() {
		return lastModified.get();
	}


	public void remove(EntityWrapper ew) throws Exception {
		if (ew.shouldIndex()) {
			if (ew.hasKey()) {
				remove(ew.getKey());
			} else {
				Logger.warn("Entity " + ew.getKind() + "'s Id field is null!");
			}
		}
	}
	
	public void remove(Key key) throws Exception {
				Tuple<String,String> docKey=key.asLuceneIdTuple();
				if (DEBUG(2)){
					Logger.debug("Deleting document " + docKey.k() + "=" + docKey.v() + "...");
				}
				
				BooleanQuery q = new BooleanQuery();
				q.add(new TermQuery(new Term(docKey.k(), docKey.v())), BooleanClause.Occur.MUST);
				q.add(new TermQuery(new Term(FIELD_KIND, key.getKind())), BooleanClause.Occur.MUST);
				indexWriter.deleteDocuments(q);
				
				if(USE_ANALYSIS){ //eliminate 
					BooleanQuery qa = new BooleanQuery();
					qa.add(new TermQuery(new Term(ANALYZER_VAL_PREFIX+docKey.k(), docKey.v())), BooleanClause.Occur.MUST);
					qa.add(new TermQuery(new Term(FIELD_KIND, ANALYZER_VAL_PREFIX + key.getKind())), BooleanClause.Occur.MUST);
					indexWriter.deleteDocuments(qa);
				}
				markChange();
	}
	
	public void removeAllType(EntityInfo<?> ei) throws Exception{
		TermQuery q = new TermQuery(new Term(FIELD_KIND, ei.getName()));
		indexWriter.deleteDocuments(q);
		markChange();
	}
	
	public void removeAllType(Class<?> type) throws Exception{
		removeAllType(EntityUtils.getEntityInfoFor(type));
	}

	public void remove(String text) throws Exception {
		try {
			QueryParser parser = new QueryParser(LUCENE_VERSION, FULL_TEXT_FIELD, indexAnalyzer);
			Query query = parser.parse(text);
			Logger.debug("## removing documents: " + query);
			indexWriter.deleteDocuments(query);
		} catch (ParseException ex) {
			Logger.warn("Can't parse query expression: " + text, ex);
			throw new IllegalArgumentException("Can't parse query: " + text, ex);
		}
	}
	
	
	


	
	/**
	 * Gets the Facet field for a range query. Find the interval that the
	 * given value falls within. Uses that to make a Facet.
	 * @param name
	 * @param ranges
	 * @param value
	 * @return
	 */
	static FacetField getRangeFacet(String name, long[] ranges, long value) {		
		if (ranges.length == 0)return null;
		if (value < ranges[0])return new FacetField(name, "<" + ranges[0]);
		
		int i=0;
		for (; i < ranges.length; ++i) {
			if (value < ranges[i])
				break;
		}
		if (i == ranges.length) {
			return new FacetField(name, ">" + ranges[i - 1]);
		}
		return new FacetField(name, ranges[i - 1] + ":" + ranges[i]);
	}

	static FacetField getRangeFacet(String name, double[] ranges, double value, String format) {
		if (ranges.length == 0)
			return null;

		if (value < ranges[0]) {
			return new FacetField(name, "<" + String.format(format, ranges[0]));
		}

		int i = 1;
		for (; i < ranges.length; ++i) {
			if (value < ranges[i])
				break;
		}

		if (i == ranges.length) {
			return new FacetField(name, ">" + String.format(format, ranges[i - 1]));
		}

		return new FacetField(name, String.format(format, ranges[i - 1]) + ":" + String.format(format, ranges[i]));
	}

	static void setFieldType(FieldType ftype) {
		ftype.setIndexed(true);
		ftype.setTokenized(true);
		ftype.setStoreTermVectors(true);
		ftype.setIndexOptions(IndexOptions.DOCS_AND_FREQS_AND_POSITIONS_AND_OFFSETS);
	}


	static FacetsConfig getFacetsConfig(JsonNode node) {
		if (!node.isContainerNode())
			throw new IllegalArgumentException("Not a valid json node for FacetsConfig!");

		String text = node.get("version").asText();
		Version ver = Version.parseLeniently(text);
		if (!ver.equals(LUCENE_VERSION)) {
			Logger.warn("Facets configuration version (" + ver + ") doesn't " + "match index version (" + LUCENE_VERSION
					+ ")");
		}

		FacetsConfig config = null;
		ArrayNode array = (ArrayNode) node.get("dims");
		if (array != null) {
			config = new FacetsConfig();
			for (int i = 0; i < array.size(); ++i) {
				ObjectNode n = (ObjectNode) array.get(i);
				String dim = n.get("dim").asText();
				config.setHierarchical(dim, n.get("hierarchical").asBoolean());
				config.setIndexFieldName(dim, n.get("indexFieldName").asText());
				config.setMultiValued(dim, n.get("multiValued").asBoolean());
				config.setRequireDimCount(dim, n.get("requireDimCount").asBoolean());
			}
		}

		return config;
	}

	static JsonNode setFacetsConfig(FacetsConfig config) {
		ObjectMapper mapper = new ObjectMapper();
		ObjectNode node = mapper.createObjectNode();
		node.put("created", TimeUtil.getCurrentTimeMillis());
		node.put("version", LUCENE_VERSION.toString());
		node.put("warning", "AUTOMATICALLY GENERATED FILE; DO NOT EDIT");
		Map<String, FacetsConfig.DimConfig> dims = config.getDimConfigs();
		node.put("size", dims.size());
		ArrayNode array = node.putArray("dims");
		for (Map.Entry<String, FacetsConfig.DimConfig> me : dims.entrySet()) {
			FacetsConfig.DimConfig c = me.getValue();
			ObjectNode n = mapper.createObjectNode();
			n.put("dim", me.getKey());
			n.put("hierarchical", c.hierarchical);
			n.put("indexFieldName", c.indexFieldName);
			n.put("multiValued", c.multiValued);
			n.put("requireDimCount", c.requireDimCount);
			array.add(n);
		}
		return node;
	}

	File getFacetsConfigFile() {
		return new File(baseDir, FACETS_CONFIG_FILE);
	}

	File getSorterConfigFile() {
		return new File(baseDir, SORTER_CONFIG_FILE);
	}

	static void saveFacetsConfig(File file, FacetsConfig facetsConfig) {
		JsonNode node = setFacetsConfig(facetsConfig);
		ObjectMapper mapper = new ObjectMapper();
		try (OutputStream out = new BufferedOutputStream(new FileOutputStream(file))) {

			mapper.writerWithDefaultPrettyPrinter().writeValue(out, node);

		} catch (IOException ex) {
			Logger.trace("Can't persist facets config!", ex);
			ex.printStackTrace();
		}
	}

	static FacetsConfig loadFacetsConfig(File file) {
		FacetsConfig config = null;
		if (file.exists()) {
			ObjectMapper mapper = new ObjectMapper();
			try {
				JsonNode conf = mapper.readTree(new BufferedInputStream(new FileInputStream(file)));
				config = getFacetsConfig(conf);
				Logger.info("## FacetsConfig loaded with " + config.getDimConfigs().size() + " dimensions!");
			} catch (Exception ex) {
				Logger.trace("Can't read file " + file, ex);
			}
		}
		return config;
	}


	static ConcurrentMap<String, SortField.Type> loadSorters(File file) {
		ConcurrentMap<String, SortField.Type> sorters = new ConcurrentHashMap<String, SortField.Type>();
		if (file.exists()) {
			ObjectMapper mapper = new ObjectMapper();
			try {
				JsonNode conf = mapper.readTree(new BufferedInputStream(new FileInputStream(file)));
				ArrayNode array = (ArrayNode) conf.get("sorters");
				if (array != null) {
					for (int i = 0; i < array.size(); ++i) {
						ObjectNode node = (ObjectNode) array.get(i);
						String field = node.get("field").asText();
						String type = node.get("type").asText();
						sorters.put(field, SortField.Type.valueOf(SortField.Type.class, type));
					}
				}
			} catch (Exception ex) {
				Logger.trace("Can't read file " + file, ex);
			}
		}
		return sorters;
	}

	static void saveSorters(File file, Map<String, SortField.Type> sorters) {
		ObjectMapper mapper = new ObjectMapper();

		ObjectNode conf = mapper.createObjectNode();
		conf.put("created", TimeUtil.getCurrentTimeMillis());
		ArrayNode node = mapper.createArrayNode();
		for (Map.Entry<String, SortField.Type> me : sorters.entrySet()) {
			ObjectNode obj = mapper.createObjectNode();
			obj.put("field", me.getKey());
			obj.put("type", me.getValue().toString());
			node.add(obj);
		}
		conf.put("sorters", node);

		try (OutputStream fos = new BufferedOutputStream(new FileOutputStream(file))) {

			mapper.writerWithDefaultPrettyPrinter().writeValue(fos, conf);
		} catch (Exception ex) {
			Logger.trace("Can't persist sorter config!", ex);
			ex.printStackTrace();
		}
	}

	/**
	 * Closing this indexer will shut it down. This is the same as calling
	 * {@link #shutdown()}.
	 */
	@Override
	public void close() {
		shutdown();
	}

	@Override
	public void newProcess() {

		flushDaemon.lockFlush();
        closeAndClear(lookups);
		//we have to clear our suggest fields since they are about to be completely replaced
		//lookups.clear();
		sorters.clear();
		isReindexing = true;
		flushDaemon.unLockFlush();
	}

    private <K, V extends Closeable> void closeAndClear(Map<K, V> map){
        Iterator<Map.Entry<K, V>> iter = map.entrySet().iterator();
        while(iter.hasNext()){
            closeAndIgnore(iter.next().getValue());
            iter.remove();

        }

    }
	@Override
	public void doneProcess() {
		isReindexing = false;
	}

	@Override
	public void recordProcessed(Object o) {	}

	@Override
	public void error(Throwable t) {	}

	@Override
	public void totalRecordsToProcess(int total) {	}

	@Override
	public void countSkipped(int numSkipped) {	}

	public void shutdown() {
		if (isShutDown) {
			return;
		}
		try {
			if (scheduler != null) {
				try {
					isShutDown = true;
					scheduler.shutdown();
					scheduler.awaitTermination(1, TimeUnit.MINUTES);
					flushDaemon.execute();
				} catch (Throwable e) {
					e.printStackTrace();
					throw new RuntimeException(e);
				}
			}

			saveFacetsConfig(getFacetsConfigFile(), facetsConfig);
			saveSorters(getSorterConfigFile(), sorters);

			for (SuggestLookup look : lookups.values()) {
				closeAndIgnore(look);
			}
			// clear the lookup value map
			// if we restart without clearing we might
			// think we have lookups we don't have if we delete the ginas.ix
			// area
			lookups.clear();

			closeAndIgnore(searchManager);
			closeAndIgnore(indexWriter);
			closeAndIgnore(taxonWriter);

			closeAndIgnore(indexDir);
			closeAndIgnore(taxonDir);

		} catch (Exception ex) {
			ex.printStackTrace();
			Logger.trace("Closing index", ex);
		} finally {
			if (indexers != null) {
				if (baseDir != null) {
					TextIndexer indexer = indexers.remove(baseDir);
				}
			}
			if (!isEmptyPool) {
				threadPool.shutdown();
				try {
					threadPool.awaitTermination(1, TimeUnit.MINUTES);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			isShutDown = true;
		}
	}
	
	
	public TermVectors getTermVectors (Class kind, String field) throws Exception{
	    return getTermVectors(kind,field, (Filter)null, null);
	}
	
	public TermVectors getTermVectors(Class kind, String field, List<Object> subset, Query q) throws Exception{
	    LazyList<Key, Object> llist = LazyList.of(subset, (o)->EntityWrapper.of(o).getKey());
	    
	    List<Key> klist = llist.getInternalList()
	         .stream()
	         .map(n->n.getName())
	         .collect(Collectors.toList());
	    
	    return getTermVectorsFromKeys(kind,field, klist,q);
    }
	
	public TermVectors getTermVectorsFromKeys (Class kind, String field, List<Key> subset, Query q) throws Exception{
	    return getTermVectors(kind,field, getTermKeysFilter(subset),q);
    }
	
	
	public TermVectors getTermVectors (Class kind, String field, Filter luceneFilter, Query query)
	                throws Exception {
	    
	    return withSearcher(searcher -> {
	        return TermVectorsCollector.make(kind, field, searcher, luceneFilter, query)
	               .termVectors();
        }); 
	}

	private static void closeAndIgnore(Closeable closeable) {
		if (closeable == null) {
			return;
		}
		try {
			closeable.close();
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}

	// ************************************
	// Things that modify state are below:
	
	
	//make the fields for the dynamic facets

	public void createDynamicField(Consumer<IndexableField> fieldTaker, IndexableValue iv) {
		facetsConfig.setMultiValued(iv.name(), true);
		facetsConfig.setRequireDimCount(iv.name(), true);
		fieldTaker.accept(new FacetField(iv.name(), iv.value().toString()));
		fieldTaker.accept(new TextField(iv.path(), TextIndexer.START_WORD + iv.value().toString() + TextIndexer.STOP_WORD, NO));
		if(iv.suggest()){
			addSuggestedField(iv.name(),iv.value().toString());
		}
	}

	//make the fields for the primitive fields

	public void instrumentIndexableValue(Consumer<IndexableField> fields, IndexableValue indexableValue) {

		if(indexableValue.isDirectIndexField()){
			fields.accept(indexableValue.getDirectIndexableField());
			return;
		}

		if(indexableValue.isDynamicFacet()){
			createDynamicField(fields,indexableValue);
			return;
		}

		// Used to be configurable, now just always NO
		// for all cases we use.
		org.apache.lucene.document.Field.Store store = Store.NO;
		
		String fname = indexableValue.name();
		String name = indexableValue.rawName();

		String full = indexableValue.path();
		Object value = indexableValue.value();
		boolean sorterAdded = false;
		boolean asText = true;
		if (value instanceof Long) {
			Long lval = (Long) value;
			fields.accept(new LongField(full, lval, NO));
			asText = indexableValue.facet();
			if (!asText && !name.equals(full))
				fields.accept(new LongField(name, lval, store));
			if (indexableValue.sortable()) {
				String f = SORT_PREFIX + full;
				sorters.put(f, SortField.Type.LONG);
				fields.accept(new LongField(f, lval, store));
				sorterAdded = true;
			}
			FacetField ff = getRangeFacet(fname, indexableValue.ranges(), lval);
			if (ff != null) {
				facetsConfig.setMultiValued(fname, true);
				facetsConfig.setRequireDimCount(fname, true);
				fields.accept(ff);
				asText = false;
			}
		} else if (value instanceof Integer) {
			// fields.add(new IntDocValuesField (full, (Integer)value));
			Integer ival = (Integer) value;
			fields.accept(new IntField(full, ival, NO));
			asText = indexableValue.facet();
			if (!asText && !name.equals(full))
				fields.accept(new IntField(name, ival, store));

			if (indexableValue.sortable()) {
				String f = SORT_PREFIX + full;
				sorters.put(f, SortField.Type.INT);
				fields.accept(new IntField(f, ival, store));
				sorterAdded = true;
			}

			FacetField ff = getRangeFacet(fname, indexableValue.ranges(), ival);
			if (ff != null) {
				facetsConfig.setMultiValued(fname, true);
				facetsConfig.setRequireDimCount(fname, true);
				fields.accept(ff);
				asText = false;
			}
		} else if (value instanceof Float) {
			// fields.add(new FloatDocValuesField (full, (Float)value));
			Float fval = (Float) value;
			fields.accept(new FloatField(name, fval, store));
			if (!full.equals(name))
				fields.accept(new FloatField(full, fval, NO));

			if (indexableValue.sortable()) {
				String f = SORT_PREFIX + full;
				sorters.put(f, SortField.Type.FLOAT);
				fields.accept(new FloatField(f, fval, NO));
				sorterAdded = true;
			}

			FacetField ff = getRangeFacet(fname, indexableValue.dranges(), fval, indexableValue.format());
			if (ff != null) {
				facetsConfig.setMultiValued(fname, true);
				facetsConfig.setRequireDimCount(fname, true);
				fields.accept(ff);
			}
			asText = false;
		} else if (value instanceof Double) {
			// fields.add(new DoubleDocValuesField (full, (Double)value));
			Double dval = (Double) value;
			fields.accept(new DoubleField(name, dval, store));
			if (!full.equals(name)) {
				fields.accept(new DoubleField(full, dval, NO));
			}
			if (indexableValue.sortable()) {
				String f = SORT_PREFIX + full;
				sorters.put(f, SortField.Type.DOUBLE);
				fields.accept(new DoubleField(f, dval, NO));
				sorterAdded = true;
			}

			FacetField ff = getRangeFacet(fname, indexableValue.dranges(), dval, indexableValue.format());
			if (ff != null) {
				facetsConfig.setMultiValued(fname, true);
				facetsConfig.setRequireDimCount(fname, true);
				fields.accept(ff);
			}
			asText = false;
		} else if (value instanceof java.util.Date) {
			long date = ((Date) value).getTime();
			fields.accept(new LongField(name, date, YES));
			if (!full.equals(name)) {
				fields.accept(new LongField(full, date, YES));
			}
			if (indexableValue.sortable()) {
				String f = SORT_PREFIX + full;
				sorters.put(f, SortField.Type.LONG);
				fields.accept(new LongField(f, date, NO));
				sorterAdded = true;
			}
			asText = indexableValue.facet();
			if (asText) {
				value = YEAR_DATE_FORMAT.get().format(date);
			}
		}

		if (asText) {
			String text = value.toString();
			if(text.isEmpty()){
				if(indexableValue.indexEmpty()){
					text=indexableValue.emptyString();
				}else{
					return;
				}
			}
			String dim = indexableValue.name();
			
			if("".equals(dim)){
				dim = full;
			}
			
			if (indexableValue.facet() || indexableValue.taxonomy()) {


				if (indexableValue.taxonomy()) {
                    facetsConfig.setMultiValued(dim, true);
                    facetsConfig.setRequireDimCount(dim, true);
					facetsConfig.setHierarchical(dim, true);

					fields.accept(new FacetField(dim, indexableValue.splitPath(text)));
				} else {
//                    System.out.println("full index path = " + full + "  = " + text + " fname = " + fname + " raw name = " + name);
                    if(indexableValue.useFullPath()){
                        facetsConfig.setMultiValued(full, true);
                        facetsConfig.setRequireDimCount(full, true);
                        fields.accept(new FacetField(full, text));
                    }else {
                        facetsConfig.setMultiValued(dim, true);
                        facetsConfig.setRequireDimCount(dim, true);
                        fields.accept(new FacetField(dim, text));
                    }
				}
			}

			if (indexableValue.suggest()) {
				// also index the corresponding text field with the
				// dimension name
				fields.accept(new TextField(dim, text, NO));
				addSuggestedField(dim, text);
			}

			String exactMatchStr = toExactMatchString(text);

			if (!(value instanceof Number)) {
				if (!name.equals(full)){
					// Added exact match
					fields.accept(new TextField(full,exactMatchStr, NO));
				}
			}

			// Add specific sort column only if it's not added by some other
			// mechanism
			if (indexableValue.sortable() && !sorterAdded) {
				sorters.put(SORT_PREFIX + full, SortField.Type.STRING);
				fields.accept(new StringField(SORT_PREFIX + full, text, store));
			}
			// Added exact match
			fields.accept(new TextField(name, exactMatchStr , store));
		}
	}
	

	private static String toExactMatchString(String in){
		return TextIndexer.START_WORD + replaceSpecialCharsForExactMatch(in) + TextIndexer.STOP_WORD;
	}

	private static String replaceSpecialCharsForExactMatch(String in) {

		String tmp = LEVO_PATTERN.matcher(in).replaceAll(LEVO_WORD);
		tmp = DEXTRO_PATTERN.matcher(tmp).replaceAll(DEXTRO_WORD);
		return tmp;

	}

	/*
	qtext = qtext.replace(TextIndexer.GIVEN_START_WORD, TextIndexer.START_WORD);
				qtext = qtext.replace(TextIndexer.GIVEN_STOP_WORD, TextIndexer.STOP_WORD);
	 */

	private static String transformQueryForExactMatch(String in){

		String tmp =  START_PATTERN.matcher(in).replaceAll(TextIndexer.START_WORD);
		tmp =  STOP_PATTERN.matcher(tmp).replaceAll(TextIndexer.STOP_WORD);
		tmp =  LEVO_PATTERN.matcher(tmp).replaceAll(TextIndexer.LEVO_WORD);

		tmp =  DEXTRO_PATTERN.matcher(tmp).replaceAll(TextIndexer.DEXTRO_WORD);


		return tmp;
	}

	private static final Pattern START_PATTERN = Pattern.compile(TextIndexer.GIVEN_START_WORD,Pattern.LITERAL );
	private static final Pattern STOP_PATTERN = Pattern.compile(TextIndexer.GIVEN_STOP_WORD,Pattern.LITERAL );

	private static final Pattern LEVO_PATTERN = Pattern.compile("\\(-\\)");
	private static final Pattern DEXTRO_PATTERN = Pattern.compile("\\(\\+\\)");

	private static final String LEVO_WORD = "LEVOROTATION";

	private static final String DEXTRO_WORD = "DEXTROROTATION";


	/**
	 * Add the specified field and value pair to the suggests
	 * which are used for type-ahead queries.
	 * @param name
	 * @param value
	 */
	void addSuggestedField(String name, String value) {
		name = SUGGESTION_WHITESPACE_PATTERN.matcher(name).replaceAll("_");
		try {
			SuggestLookup lookup = lookups.computeIfAbsent(name, n -> {
				try {
					return new SuggestLookup(n);
				} catch (Exception ex) {
					ex.printStackTrace();
					Logger.trace("Can't create Lookup!", ex);
					return null;
				}
			});
			if (lookup != null) {
				lookup.add(value);
			}

		} catch (Exception ex) {
			Logger.trace("Can't create Lookup!", ex);
		}
	}
}
