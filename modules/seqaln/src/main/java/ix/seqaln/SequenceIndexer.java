package ix.seqaln;

import static org.apache.lucene.document.Field.Store.NO;
import static org.apache.lucene.document.Field.Store.YES;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.TreeMap;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.KeywordAnalyzer;
import org.apache.lucene.analysis.miscellaneous.PerFieldAnalyzerWrapper;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.IntField;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.NumericRangeQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.SearcherManager;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.AlreadyClosedException;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.NIOFSDirectory;
import org.apache.lucene.store.NoLockFactory;
import org.apache.lucene.util.Version;
import org.jcvi.jillion.align.pairwise.PairwiseSequenceAlignment;
import org.jcvi.jillion.core.DirectedRange;
import org.jcvi.jillion.core.Range;
import org.jcvi.jillion.core.residue.Residue;
import org.jcvi.jillion.core.residue.ResidueSequence;
import org.jcvi.jillion.core.residue.aa.ProteinSequence;
import org.jcvi.jillion.core.residue.nt.NucleotideSequence;

import com.fasterxml.jackson.annotation.JsonIgnore;

import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import play.Logger;

public class SequenceIndexer {
    static final String CACHE_NAME = SequenceIndexer.class.getName()+".Cache";
    static final Version LUCENE_VERSION = Version.LATEST;

    static CacheManager CACHE_MANAGER;

    static int DEFAULT_KMER_SIZE = 3;



    /**
     * A slightly slimmed down form of CachedSupplier,
     * only repeated here because the seqaln module 
     * can't see the Util class.
     * 
     * @author peryeata
     *
     * @param <T> The type to be supplied
     */
    public static class CachedSup<T> implements Supplier<T>{
        private static AtomicLong generatedVersion= new AtomicLong();
        private long generatedWith=-1;
        private Supplier<T> sup;
        private boolean wasRun=false;
        private T local=null;



        public CachedSup(Supplier<T> sup){
            this.sup=sup;
        }

        public static <T> CachedSup<T> of(Supplier<T> sup) {
            return new CachedSup<T>(sup);
        }

        @Override
        public T get() {
            if(wasRun && generatedWith==generatedVersion.get()){
                return local;
            }else{
                local=sup.get();
                wasRun=true;
                generatedWith=generatedVersion.get();
                return local;
            }
        }

        public static void resetAllCaches(){
            generatedVersion.incrementAndGet();
        }

    }

    static CachedSup<Ehcache> CACHE = CachedSup.of(()->{
        CACHE_MANAGER = CacheManager.getInstance();
        return CACHE_MANAGER.addCacheIfAbsent(CACHE_NAME);
    });

    public static enum CutoffType{
        LOCAL,
        GLOBAL,
        SUB;

        public static CutoffType valueOfOrDefault(String s){
            try{
                CutoffType ct=CutoffType.valueOf(s);
                return ct;
            }catch(Exception e){
                return GLOBAL;
            }
        }
    }

    static class HSP implements Comparable<HSP> {
        public String kmer;
        public int i, j;

        HSP (String kmer, int i, int j) {
            this.kmer = kmer;
            this.i = i;
            this.j = j;
        }
        public int gap () { return Math.abs(i - j); }
        public String toString () { return kmer+"["+i+","+j+"]"; }
        public int compareTo (HSP hsp) {
            int d = hsp.kmer.length() - kmer.length();
            if (d == 0) {
                d = gap () - hsp.gap();
            }
            if (d == 0) {
                d = i - hsp.i;
            }
            if (d == 0) {
                d = j - hsp.j;
            }
            if (d == 0) {
                d = kmer.compareTo(hsp.kmer);
            }
            return d;
        }
    }

    public static class SEG implements Comparable<SEG>, Serializable {
        /**
         * 
         */
        private static final long serialVersionUID = 1L;
        private int qi, qj;
        private int ti, tj;

        public SEG (int qi, int qj, int ti, int tj) {
            this.qi = qi;
            this.qj = qj;
            this.ti = ti;
            this.tj = tj;
        }

        public boolean overlap (SEG seg) {
            return overlap (seg, 0);
        }

        public boolean overlap (SEG seg, int gap) {
            /*
             * a_i ------ a_j
             *       b_i ---------- b_j
             *
             *            a_i ------ a_j
             * b_i ---------- b_j
             */

            return (Math.abs(qi - seg.qj) <= gap
                    && Math.abs(ti - seg.tj) <= gap)
                    || (Math.abs(seg.qi - qj) <= gap
                    && Math.abs(seg.ti - tj) <= gap);
        }

        public SEG merge (SEG seg, int gap) {
            if (overlap (seg, gap)) {
                qi = Math.min(qi, seg.qi);
                qj = Math.max(qj, seg.qj);
                ti = Math.min(ti, seg.ti);
                tj = Math.max(tj, seg.tj);
                return this;
            }
            return null;
        }

        public int compareTo (SEG seg) {
            return qi - seg.qi;
        }

        public String toString () {
            return "q=["+qi+","+qj+"] t=["+ti+","+tj+"]";
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            SEG seg = (SEG) o;

            if (qi != seg.qi) return false;
            if (qj != seg.qj) return false;
            if (ti != seg.ti) return false;
            return tj == seg.tj;

        }

        @Override
        public int hashCode() {
            int result = qi;
            result = 31 * result + qj;
            result = 31 * result + ti;
            result = 31 * result + tj;
            return result;
        }
    }

    public static class Alignment implements Comparable<Alignment>, Serializable {

        /**
         * 
         */
        private static final long serialVersionUID = 1L;

        private final SEG segment; // coordinate of query
        public final String query; // segment of query
        public final String target; // segment of sequence
        public final String alignment; // full alignment string
        public final int score;
        public final double iden;
        public final double global;
        public final double sub;

        @JsonIgnore
        public SEG getSegment(){
            return segment;
        }

        BitSet bsq;
        BitSet bst;

        Alignment (SEG segment,
                String query, 
                String target,
                String alignment, 
                int score, 
                double iden, 
                double global,
                double sub,
                BitSet qsites,
                BitSet tsites
                ) {
            this.segment = segment;
            this.query = query;
            this.target = target;
            this.alignment = alignment;
            this.score = score;
            this.iden = iden;
            this.global = global;
            this.sub=sub;
            bsq=qsites;
            bst=tsites;
        }

        public int compareTo (Alignment aln) {
            int d = aln.score - score;
            if (d == 0) {
                if (iden < aln.iden) d = 1;
                else if (iden > aln.iden) d = -1;
            }
            if (d == 0)
                d = segment.qi - aln.segment.qi;

            return d;
        }

        public String toString () {
            return "[score] "+score+"\n[identity] "
                    +String.format("%1$.3f", iden)
                    +"\n[alignment]\n"+alignment;
        }

        /**
         * Returns a bitset of the indexes where a match was
         * found for the target sequence
         * @return Bitset of the matching target sites. 1-indexed.
         */
        public BitSet targetSites(){
            return bst;
        }

        /**
         * Returns a bitset of the indexes where a match was
         * found for the query sequence
         * @return Bitset of the matching query sites. 1-indexed.
         */
        public BitSet querySites(){
            return bsq;
        }
    }

    public static class Result implements Serializable{
        /**
         * 
         */
        private static final long serialVersionUID = 1L;
        public final CharSequence query;
        public final String id;
        public final CharSequence target;
        public final List<Alignment> alignments = new ArrayList<Alignment>();
        public double score;
        public CutoffType scoreType;


        Result () {
            query = null;
            id = null;
            target = null;
        }
        Result (String id, CharSequence query, CharSequence target) {
            this.id = id;
            this.query = query;
            this.target = target;
        }

        public Result setScore(double score, CutoffType cot){
        	this.score=score;
        	this.scoreType=cot;
        	return this;
        }

        @Override
        public String toString() {
            return "Result{" +
                    "query=" + query +
                    ", id='" + id + '\'' +
                    ", target=" + target +
                    ", alignments=" + alignments +
                    '}';
        }
    }

    static final Result POISON_RESULT = new Result ();

    public static class ResultEnumeration implements Enumeration<Result> {
        final BlockingQueue<Result> queue;
        Result next;

        ResultEnumeration (BlockingQueue<Result> queue) {
            this.queue = queue;
            if(queue==null){
                next=POISON_RESULT;
            }else{
                next ();
            }
        }

        void next () {
            try {
                next = queue.take();
            }
            catch (Exception ex) {
                ex.printStackTrace();
                next = POISON_RESULT; // terminate
            }
        }           

        public boolean hasMoreElements () {
            return next != POISON_RESULT;
        }

        public Result nextElement () {
            if(!hasMoreElements()){
                throw new NoSuchElementException();
            }
            Result current = next;
            next ();
            Collections.sort(current.alignments);
            return current;
        }
    }


    public static final String FIELD_KMER = "_KMER";
    public static final String FIELD_ID = "_ID";
    public static final String FIELD_SEQ = "_SEQ";
    public static final String FIELD_FP = "_FP";

    public static final String FIELD_TAGS = "_TAG";
    public static final String FIELD_SOURCE = "_SOURCE";
    public static final String FIELD_LENGTH = "_LENGTH";
    public static final String FIELD_POSITION = "_POSITION";
    public static final String FIELD_TEXT = "text";

    //public static final String FIELD_FP_1MER = "_FP1";


    private boolean useFingerprints=true;

    private File baseDir;
    private Directory indexDir;
    private Directory kmerDir;    
    private IndexWriter indexWriter;
    private IndexWriter kmerWriter;    
    private DirectoryReader _kmerReader;
    private DirectoryReader _indexReader;    
    private Analyzer indexAnalyzer;

    private ExecutorService threadPool;
    private boolean localThreadPool = false;
    private SearcherManager kmerSearchManager;
    private SearcherManager seqSearchManager;

    private AtomicLong lastModified = new AtomicLong (0);

    private int kmerSize = DEFAULT_KMER_SIZE;

    private int proteinKmer = DEFAULT_KMER_SIZE;
    private int nucleicKmer = DEFAULT_KMER_SIZE;

    public void setProteinKmer(int proteinKmer) {
        this.proteinKmer = proteinKmer;
    }

    public void setNucleicKmer(int nucleicKmer) {
        this.nucleicKmer = nucleicKmer;
    }

    public static SequenceIndexer openReadOnly (File dir) throws IOException {
        return new SequenceIndexer (dir, true);
    }

    public static SequenceIndexer open (File dir) throws IOException {
        return new SequenceIndexer (dir, false);
    }


    private SequenceIndexer (File dir, boolean readOnly) throws IOException {
        this (dir, readOnly, ForkJoinPool.commonPool());
        localThreadPool = true;
    }

    public SequenceIndexer (File dir, boolean readOnly,
            ExecutorService threadPool) throws IOException {

        if (!readOnly) {
            dir.mkdirs();
        }

        if (!dir.isDirectory())
            throw new IllegalArgumentException ("Not a directory: "+dir);

        File index = new File (dir, "index");
        if (!index.exists())
            index.mkdirs();
        File kmer = new File (dir, "kmer");
        if (!kmer.exists())
            kmer.mkdirs();

        indexAnalyzer = createIndexAnalyzer ();
        indexDir = new NIOFSDirectory(index, NoLockFactory.getNoLockFactory());
        kmerDir = new NIOFSDirectory (kmer, NoLockFactory.getNoLockFactory());
        if (!readOnly) {
            indexWriter = new IndexWriter (indexDir, new IndexWriterConfig 
                    (LUCENE_VERSION, indexAnalyzer));
            kmerWriter = new IndexWriter
                    (kmerDir, new IndexWriterConfig
                            (LUCENE_VERSION, indexAnalyzer));
            kmerSearchManager = new SearcherManager (kmerWriter, true, null);
            seqSearchManager = new SearcherManager (indexWriter, true, null);
            _kmerReader = DirectoryReader.open(kmerWriter, true);
            _indexReader = DirectoryReader.open(indexWriter, true);
        }else {
            _kmerReader = DirectoryReader.open(kmerDir);
            _indexReader = DirectoryReader.open(indexDir);
            kmerSearchManager = new SearcherManager (kmerDir, null);
            seqSearchManager = new SearcherManager (indexDir, null);
        }

        this.baseDir = dir;
        this.threadPool = threadPool;
    }

    public int getKmerSize() {
        return kmerSize;
    }

    public void setKmerSize(int kmerSize) {
        this.kmerSize = kmerSize;
    }

    @SuppressWarnings("deprecation")
    static Analyzer createIndexAnalyzer () {
        Map<String, Analyzer> fields = new HashMap<String, Analyzer>();
        fields.put(FIELD_ID, new KeywordAnalyzer ());
        fields.put(FIELD_KMER, new KeywordAnalyzer ());
        return  new PerFieldAnalyzerWrapper 
                (new StandardAnalyzer (LUCENE_VERSION), fields);
    }



    public File getBasePath () { return baseDir; }

    public long getSize() {
        if (indexWriter == null)
            return _indexReader.numDocs();
        try{
            return indexWriter.numDocs();
        } catch(AlreadyClosedException e){
            Logger.trace("Index already closed",e);
            return 0;
        }
    }

    private static void closeAndIgnore(Closeable c){
        if(c ==null){
            return;
        }
        try{
            c.close();
        }catch(IOException e){
            e.printStackTrace();
        }
    }

    public void shutdown () {
        closeAndIgnore(kmerSearchManager);
        closeAndIgnore(seqSearchManager);

        closeAndIgnore(_kmerReader);
        closeAndIgnore(_indexReader);
        closeAndIgnore(indexWriter);

        closeAndIgnore(kmerWriter);

        closeAndIgnore(kmerDir);
        closeAndIgnore(indexDir);

        if (localThreadPool) {
            threadPool.shutdownNow();
        }


    }

    public void remove(String id) throws IOException{
        Objects.requireNonNull(id);
        indexWriter.deleteDocuments(new Term (FIELD_ID, id));
        kmerWriter.deleteDocuments(new Term (FIELD_ID, id));
    }



    public static class KmerFingerprintWrapper{
    	public String id;
    	public Kmers.HoloFingerprint fp;
    	public int length;

    	//public Kmers.HoloFingerprint onemerfp;

    	public static KmerFingerprintWrapper of(String id, Kmers.HoloFingerprint fp, int length){
    		KmerFingerprintWrapper k= new KmerFingerprintWrapper();
    		k.id=id;
    		k.fp=fp;
    		k.length=length;
    		//k.onemerfp=onemers;
    		return k;
    	}

    	public Kmers.HoloFingerprint kMers(){
    		return this.fp;
    	}
    	public int hammingDistanceKmers(Kmers.HoloFingerprint q){
    		return q.hammingDistanceTo(this.fp);
    	}
//    	public Kmers.HoloFingerprint oneMers(){
//			return this.onemerfp;
//		}
//    	public int hammingDistanceOnemers(Kmers.HoloFingerprint q){
//    		return q.hammingDistanceTo(this.onemerfp);
//    	}
    }


    private static final List<String> DNA_TAGS = Arrays.asList("DNA");
    private static final List<String> RNA_TAGS = Arrays.asList("RNA");
    private static final List<String> PROTEIN_TAGS = Arrays.asList("PROTEIN");
    public void add (String id, NucleotideSequence seq) throws IOException{

        add(id, seq.toString(), seq.isDna()? DNA_TAGS: RNA_TAGS);
    }

    public void add (String id, ProteinSequence seq) throws IOException{
        add(id, seq.toString(), PROTEIN_TAGS);
    }

    public void addAminoAcidSequence(String id, String seq) throws IOException{
        add(id, seq, PROTEIN_TAGS);
    }
    public void addNucleicAcidSequence(String id, String seq) throws IOException{
        add(id, seq, RNA_TAGS);
    }
    public void add (String id, String seq) throws IOException {
        add(id, seq, Collections.emptyList());
    }
    private int getKmerSizeFor(List<String> tags){
        if(tags.containsAll(PROTEIN_TAGS)){
            return proteinKmer;
        }
        if(tags.containsAll(DNA_TAGS) || tags.containsAll(RNA_TAGS)){
            return nucleicKmer;
        }
        return DEFAULT_KMER_SIZE;
    }
    public void add (String id, String seq, List<String> tags) throws IOException {
        if (indexWriter == null)
            throw new RuntimeException ("Index is read-only!");

        try {
            Document doc = new Document ();
            int myKmerSize = getKmerSizeFor(tags);
            Kmers kmers = Kmers.create(seq, myKmerSize);
            //Kmers onemers = Kmers.create(seq, 1);

            StringField idf = new StringField (FIELD_ID, id, YES);


            doc.add(idf);
            doc.add(new IntField (FIELD_LENGTH, seq.length(), YES));
            doc.add(new StoredField (FIELD_SEQ, seq.toString())); //why toString?
            doc.add(new StringField (FIELD_FP, kmers.holoFingerPrint().encode(),YES));
           // doc.add(new StringField (FIELD_FP_1MER, onemers.holoFingerPrint().encode(),YES));



            for(String tag: tags){
                doc.add(new StringField (FIELD_TAGS,tag,NO));
            }
            indexWriter.addDocument(doc);
            // indexWriter.updateDocument(new Term (FIELD_ID, id), doc);


            for (String kmer : kmers.kmers()) {
                BitSet positions = kmers.positions(kmer);
                StringField kmerf = new StringField (FIELD_KMER, kmer, YES);
                Document doc2 = new Document ();
                doc2.add(idf);
                doc2.add(kmerf);
                for(String tag: tags){
                    doc2.add(new StringField (FIELD_TAGS,tag,NO));
                }
                for (int i = positions.nextSetBit(0);
                        i>=0; i = positions.nextSetBit(i+1)) {
                    doc2.add(new IntField (FIELD_POSITION, i, YES));

                }
                kmerWriter.addDocument(doc2);
            }
           // allKmers.add(KmerTest.of(id,kmers));
            indexWriter.commit();
            kmerWriter.commit();
        }
        finally {
            lastModified.set(System.currentTimeMillis());
        }
    }


    public long lastModified () { return lastModified.get(); }


    public ResultEnumeration search(String query, double identity, CutoffType rt, String seqType) {
        return search (query, identity, 1,rt, seqType);
    }

    public ResultEnumeration search (final String query,
            final double identity, final int gap,CutoffType rt, String seqType) {
        if (getSize()<=0 || query == null || query.length() == 0) {
            return new ResultEnumeration(null);
        }
        final BlockingQueue<Result> out = new LinkedBlockingQueue<Result>();
        threadPool.submit(()->{
            ix.core.util.StopWatch.timeElapsed(()->{
                try {
                    search (out, query, identity, gap, rt, seqType);
                }catch (Exception ex) {
                    ex.printStackTrace();
                }finally{
                    try {
                        out.put(POISON_RESULT);// finish
                    }catch (InterruptedException e) {
                        Logger.error(e.getMessage(), e);
                    } 
                }
            });
        });

        return new ResultEnumeration (out);
    }

    protected void search (BlockingQueue<Result> results,
            String query, double identity, int gap,CutoffType rt, String seqType)
                    throws Exception {

        /*
         * this can be expensive if we call search often. having a daemon
         * thread to performed this in the background on a regular interval
         * is recommended but it'll fail the test cases.
         */
        kmerSearchManager.maybeRefresh();
        IndexSearcher searcher = kmerSearchManager.acquire();
        try {
            search (searcher, results, query, identity, gap, rt, seqType);
        }finally {
            kmerSearchManager.release(searcher);
        }
        searcher = null;
    }

    public boolean getUseFingerprint(){
    	return this.useFingerprints;
    }

    public void  setUseFingerprint(boolean fpuse){
    	this.useFingerprints=fpuse;
    }

    private int getKmerSizeForType(String seqType){
        if(seqType.toLowerCase().contains("nucleicacid")){
            return nucleicKmer;
        }else if("Protein".equalsIgnoreCase(seqType)){
            return proteinKmer;
        }
        return DEFAULT_KMER_SIZE;
    }

    public static class StringAndDouble implements Comparable<StringAndDouble>{
    	public String s;
    	public double d;
    	public static StringAndDouble from(String s, double d){
    		StringAndDouble sd= new StringAndDouble();
    		sd.s=s;
    		sd.d=d;
    		return sd;
    	}

		@Override
		public int compareTo(StringAndDouble arg0) {
			int c= Double.compare(this.d, arg0.d);
			if(c==0){
				return this.s.compareTo(arg0.s);
			}
			return c;
		}

		public boolean equals(Object o){
			if(o == null)return false;
			if(!(o instanceof StringAndDouble))return false;
			StringAndDouble other = (StringAndDouble)o;
			return other.s.equals(this.s) && other.d==this.d;

		}
		public int hashCode(){
			return s.hashCode() ^ Double.hashCode(d);
		}


    }


    protected void search (IndexSearcher kmerSearcher,
            BlockingQueue<Result> results,
            String query, double identity, int gap, CutoffType rt, String seqType)
                    throws Exception {

        final int K = getKmerSizeForType(seqType);
        Kmers kmers = Kmers.create(query, K);


        Kmers.HoloFingerprint fpkmer = kmers.holoFingerPrint();

        Map<StringAndDouble, String> seqMap = new TreeMap<>();


        List<String> tags= new ArrayList<String>();
		if(seqType!=null){
			//This isn't good, but we're doing this weird right now
			//and the seqType that gets in from the application isn't always what's expected
			//TODO: change to using an enum rather than a string
			if(seqType.toLowerCase().contains("nucleicacid")){
				tags.addAll(DNA_TAGS);
				tags.addAll(RNA_TAGS);
			}else{
				tags.addAll(PROTEIN_TAGS);
			}
		}


        if(useFingerprints && (rt==CutoffType.GLOBAL || rt==CutoffType.SUB)){
        		int maxDistance = (int)Math.ceil(query.length()*(1-identity));
        		int pen = K;

        		int maxKmerDistance = maxDistance*(pen);

        		int lowerBoundLength = (int)Math.floor(query.length()*identity);
        		int upperBoundLength = (int)Math.ceil(query.length()/identity);
        		if(rt==CutoffType.SUB){
        			upperBoundLength=Integer.MAX_VALUE;
        		}



        		List<KmerFingerprintWrapper> res = getFPSequencesWithBounds(lowerBoundLength,upperBoundLength,tags);
        		for(KmerFingerprintWrapper tup: res){
        			int fudge=0;
        			int d=tup.length-query.length();
        			if(d>=0 && rt==CutoffType.SUB){
        				fudge=d;
        			}
        			int[] hammingKd=tup.fp.hammingMoreAndLessDistanceTo(fpkmer);
        			if((hammingKd[0] <= maxKmerDistance + fudge &&
        			    hammingKd[1] <= maxKmerDistance  )){
        				StringAndDouble score = StringAndDouble.from(tup.id,hammingKd[0]+hammingKd[1]);
        				seqMap.computeIfAbsent(score, k->getSeq(k.s));
        			}
        		}
        }else{

        int ndocs = Math.max(1, kmerSearcher.getIndexReader().numDocs());
        	//Use only for local alignment
        for (Map.Entry<String, BitSet> entry : kmers.positionEntrySet()) {
            if(Thread.currentThread().isInterrupted()){
                return;
            }
            String kmer = entry.getKey();
            TermQuery tq = new TermQuery (new Term (FIELD_KMER, kmer));
	            Query fullQ=tq;

            	if(!tags.isEmpty()){
            		BooleanQuery bq=new BooleanQuery();
            		bq.setMinimumNumberShouldMatch(1);
            		for(String tag: tags){
                		bq.add(new TermQuery (new Term (FIELD_TAGS, tag)),Occur.SHOULD);
                	}
            		BooleanQuery fq=new BooleanQuery();
            		fq.add(fullQ,Occur.MUST);
            		fq.add(bq,Occur.MUST);
            		fullQ=fq;
            	}

	            TopDocs docs = kmerSearcher.search(fullQ, ndocs);
		            //BitSet positions = entry.getValue();

            for (int i = 0; i < docs.totalHits; ++i) {
                Document doc = kmerSearcher.doc(docs.scoreDocs[i].doc);
                final String id = doc.get(FIELD_ID);
	                StringAndDouble sd = StringAndDouble.from(id, 0);
		                seqMap.computeIfAbsent(sd, k->getSeq(k.s));
		            }
            }
        }
//        System.out.print(seqMap.size() + "\t");
//        System.out.print(seqMap.keySet().stream().map(s->s.d+"").collect(Collectors.joining(",")) + "\t");
        //if(true)return;
        SequenceAlignmentHelper alignmentHelper = SequenceAlignmentHelper.createFor(seqType);

        ResidueSequence querySeq = alignmentHelper.toSequence(query);

        Map<String,Result> _cachedResults = new ConcurrentHashMap<String,Result>();

        seqMap.entrySet().stream()
              .parallel()
              .forEach(entry->{
            	  try{
	               	//if(true)continue;

	              	String tseq = entry.getValue();


	              	Result result=_cachedResults.computeIfAbsent(tseq, k->{
	              		Result r= new Result(entry.getKey().s,querySeq.toString(), tseq);
	              		r.setScore(-1, rt);
	              		return r;
	              	});
	              	//Result result=new Result(resultC.id,resultC.query,resultC.target);

	              	if(result.score>=0 && result.score<identity){
	              		return;
	              	}else if(result.score>=identity){
	              		Result resultCopy=new Result(entry.getKey().s,result.query,result.target);
	              		resultCopy.alignments.addAll(result.alignments);
	              		resultCopy.setScore(result.score,result.scoreType);
	              		results.put(resultCopy);
                return;
            }


	              	//Don't bother doing the alignment if it's a global cutoff and the lengths
	              	//of the strings would make it impossible for there to be overlap
	              	int tlength=tseq.length();
	              	int qlength=query.length();
	              	if(rt==CutoffType.GLOBAL){
	      	        	if(Math.min(qlength, tlength) < Math.max(qlength, tlength)*identity){
	      	        		return;
                }
            }





	                  ResidueSequence targetSeq;
	                  try {
	                      targetSeq = alignmentHelper.toSequence(entry.getValue());
	                  }catch(Exception e){
	                      //prob a bad seq
	                      e.printStackTrace();
	                      return;
	                  }
	                  PairwiseSequenceAlignment alignment = alignmentHelper.align(querySeq, targetSeq, gap, rt);

	                  //double score;
	                  if(rt == CutoffType.SUB){
	                  	DirectedRange trange=alignment.getSubjectRange();

	                  	ResidueSequence targetSeq2 = alignmentHelper.toSequence(targetSeq.toBuilder(trange.asRange()).build().toString());

	                      PairwiseSequenceAlignment alignment2 = alignmentHelper.align(querySeq, targetSeq2, gap, CutoffType.GLOBAL);
	                  	result.setScore(alignment2.getPercentIdentity(),rt);

                }else {
	                      result.setScore(alignment.getPercentIdentity(),rt);
                    }


	                  if(result.score >= identity){

	                      ResidueSequence gappedQuery = alignment.getGappedQueryAlignment();
	                      ResidueSequence gappedSubject = alignment.getGappedSubjectAlignment();
	                      BitSet qbits = new BitSet((int) querySeq.getLength());
	                      BitSet tbits = new BitSet((int) targetSeq.getLength());

	                      int qOffset = (int) alignment.getQueryRange().getBegin();
	                      int tOffset = (int) alignment.getSubjectRange().getBegin();
	                      Iterator<Residue> qIter = gappedQuery.iterator();
	                      Iterator<Residue> sIter = gappedSubject.iterator();
	                      int qGaps=0, sGaps=0;
	                      int currentOffset=0;
	                      StringBuilder topBuilder = new StringBuilder(alignment.getAlignmentLength());
	                      StringBuilder middleBuilder = new StringBuilder(alignment.getAlignmentLength());
	                      StringBuilder bottomBuilder = new StringBuilder(alignment.getAlignmentLength());

	                      int matched=0;
	                      while(qIter.hasNext()){
	                          Residue q = qIter.next();
	                          Residue s = sIter.next();
	                          if(q.isGap()){
	                              qGaps++;
	                              middleBuilder.append(' ');
	                          }else if(s.isGap()){
	                              sGaps++;
	                              middleBuilder.append(' ');
	                          }else if(q.equals(s)){
	                              qbits.set(qOffset+currentOffset - qGaps);
	                              tbits.set(tOffset+currentOffset - sGaps);
	                              middleBuilder.append('|');
	                              matched++;
	                          }else{
	                              middleBuilder.append(' ');
            }
	                          topBuilder.append(q.getCharacter());
	                          bottomBuilder.append(s.getCharacter());
	                          currentOffset++;

            }

	      //GK    1 - 456 [Query
	                      Alignment aln = new Alignment(null, query, entry.getValue(),
	                              topBuilder +"    "+ alignment.getQueryRange().asRange().getBegin(Range.CoordinateSystem.RESIDUE_BASED) + " - " +   alignment.getQueryRange().asRange().getEnd(Range.CoordinateSystem.RESIDUE_BASED) + " [Query]\n"
	                                      + middleBuilder + "\n" + bottomBuilder+ "    "+ alignment.getSubjectRange().asRange().getBegin(Range.CoordinateSystem.RESIDUE_BASED) + " - " +   alignment.getSubjectRange().asRange().getEnd(Range.CoordinateSystem.RESIDUE_BASED) + " [Target]\n"
	                              ,


	                              matched, matched/(double)query.length(),
	                              alignment.getPercentIdentity(),
	                              matched/(double)query.length(), qbits, tbits);


                result.alignments.add(aln);
	                      results.put(result);
            }

            	  }catch(Exception e){
            		  e.printStackTrace();
            	  }
                  //Sub alignment score
                  // (local alignment score, multiplied by the fraction of the
                  //  residues found in the query)
                  //
                  //	The purpose of this is to penalize the local identity score
                  //  such that a strong local alignment that doesn't have much
                  //  of the query present is not weighted as strongly

                  //  As an example where we want to have high match
                  //   Query: ABC
                  //  Target: XXXXXXXXABCXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
                  //
                  //	As an example where we want to have a low match
                  //   Query: XXXXXXXXABCXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
                  //  Target: ABC
                  //
                 // double sub=iden*score/(double)query.length();

              });

        /*
            if(maxaln!=null){
                double score =0;
                switch(rt){
                case GLOBAL:
                	int totalMatch = result.alignments.stream()
                	                 .mapToInt(t->t.bsq.cardinality())
                	                 .sum();

                    score=(totalMatch +0.0)/Math.max(seq.length(),query.length()) ;
                    break;
                case LOCAL:
                    score=maxaln.iden;
                    break;
                case SUB:
                    score=maxaln.sub;
                    break;
                default:
                    break;
                }
                result.setScore(score, rt);
                if (score >= identity) {
                    results.put(result);
                }
            }
        }
        */
    }

    /**
     * There might be additional bases that can be aligned
     * beyond the Segments that weren't included because they were at the edges
     * of the sequence and did not fit into a kmer.  Or the edges of the next kmer
     * might have a few bases match but not enough to merge.
     *
     * @param segments the old segment list
     * @param query the query sequence as a String.
     * @param target the target sequence as a String.
     *
     * @return a new List of segments which might have some old SEG
     * objects and might have some new extended SEGs.
     */
    private List<SEG> extendsSegments(List<SEG> segments, String query, String target) {
        char[] queryArray = query.toUpperCase().toCharArray();
        char[] targetArray = target.toUpperCase().toCharArray();

        List<SEG> extended = new ArrayList<>(segments.size());
        for(SEG seg : segments) {


            int qi = seg.qi-1;
            int ti = seg.ti-1;

            while(qi >=0 && ti >=0){
                if(queryArray[qi] != targetArray[ti]) {
                    break;
                }
                qi--;
                ti--;
            }
            qi++;
            ti++;
            int qj = seg.qj;
            int tj = seg.tj;
//            System.out.println("qj = " + qj + " queryLength = " + queryArray.length);
//            System.out.println("tj = " + tj + " targetLength = " + targetArray.length);
            while(qj < queryArray.length && tj < targetArray.length){
//                System.out.println("looking to extend " + qj + "  " +queryArray[qj] +" vs " + tj + " " + targetArray[tj]);
                if(queryArray[qj] != targetArray[tj]) {
                    break;
                }
                qj++;
                tj++;
            }
            //j coords is EXCLUSIVE so it's +1 past actual alignment
            qj = Math.min(qj, queryArray.length);
            tj  = Math.min(tj, targetArray.length);

            int leftExtension = seg.qi - qi;
            int rightExtension = qj - seg.qj;
            if(leftExtension >0 || rightExtension >0){
                //new Seg
                extended.add( new SEG(qi, qj, ti, tj));
            }else{
                //keep old
                extended.add(seg);
            }
        }
        return extended;

    }

    public String getSeq (final String id) {
        try {
            return getOrElse
                    (getClass().getName()+"/"+FIELD_SEQ+"/"+id,
                            ()->{
                                seqSearchManager.maybeRefresh();
                                IndexSearcher searcher = seqSearchManager.acquire();
                                try {
                                    TopDocs docs = searcher.search
                                            (new TermQuery (new Term (FIELD_ID, id)), 1);
                                    if (docs.totalHits > 0) {
                                        Document d = searcher.doc
                                                (docs.scoreDocs[0].doc);
                                        return d.get(FIELD_SEQ);
                                    }
                                }finally {
                                    seqSearchManager.release(searcher);
                                }
                                return null;
                            });
        }catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }


    public List<KmerFingerprintWrapper> getFPSequencesWithBounds(int lowerBound, int upperbound, List<String> mustHaveAtLeastOneTag) {
        try {
                                seqSearchManager.maybeRefresh();
                                IndexSearcher searcher = seqSearchManager.acquire();
                                try {
                                	NumericRangeQuery<Integer> q= NumericRangeQuery.newIntRange(FIELD_LENGTH, lowerBound, upperbound, true, true);

                                	Query fullQ=q;

                                	if(!mustHaveAtLeastOneTag.isEmpty()){
                                		BooleanQuery bq=new BooleanQuery();
                                		bq.setMinimumNumberShouldMatch(1);
                                		for(String tag: mustHaveAtLeastOneTag){
                                    		TermQuery tq = new TermQuery (new Term (FIELD_TAGS, tag));
                                    		bq.add(tq,Occur.SHOULD);
                                    	}
                                		BooleanQuery fq=new BooleanQuery();
                                		fq.add(fullQ,Occur.MUST);
                                		fq.add(bq,Occur.MUST);
                                		fullQ=fq;
                                	}

                                    TopDocs docs = searcher.search
                                            (fullQ, Integer.MAX_VALUE);
                                    //System.out.println("Hits:" + docs.totalHits);
                                    if (docs.totalHits > 0) {
                                    	return Arrays.stream(docs.scoreDocs)
                                    	      .map(d-> {
                                    	    	  try{
                                    	    	  	return searcher.doc(d.doc);
                                    	    	  }catch(Exception e){
                                    	    		  e.printStackTrace();
                                    	    		return null;
                                    	    	  }
                                    	      })
                                    	      .filter(d->d!=null)
                                    	      .map(d->{
                                    	    	  try{
                                    	    	//  System.out.println("Fetching...");
                                    	    	String fpenc=d.get(FIELD_FP);
                                    	    	//String fp1enc=d.get(FIELD_FP_1MER);
                                    	    	String id=d.get(FIELD_ID);
                                    	    	int len=Integer.parseInt(d.get(FIELD_LENGTH));
                                    	    	Kmers.HoloFingerprint fp=Kmers.HoloFingerprint.decode(fpenc);
                                    	    	//System.out.println("Fetched.");
                                    	    	return KmerFingerprintWrapper.of(id, fp,len);
                                    	    	  }catch(Exception e){
                                    	    		  e.printStackTrace();
                                    	    		  return null;
                                    	    	  }
                                    	      })
                                    	      .filter(f->f!=null)
                                    	      .collect(Collectors.toList());
                                    }
                                }finally {
                                    seqSearchManager.release(searcher);
                                }
                                return new ArrayList<KmerFingerprintWrapper>();
        }catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }



    static Alignment align (SEG seg, String query, String target) {
        return align (seg, query, target, 1, -1);
    }

    /**
     * do global alignment on subsequences that have been extracted
     * by the HSP segments
     */
    static Alignment align (SEG seg, String query, String target, 
            int match, int gap) {
        char[] q = query.substring(seg.qi, seg.qj).toUpperCase().toCharArray();
        char[] s = target.substring(seg.ti, seg.tj).toUpperCase().toCharArray();


//        System.err.println("** aligning subsequences...");
//        System.err.println(q);
//        System.err.println(s);


        int[][] M = new int[q.length+1][s.length+1];
        for (int i = 0; i <= q.length; ++i)
            M[i][0] = gap*i;
        for (int i = 0; i <= s.length; ++i)
            M[0][i] = gap*i;
        for (int i = 1; i <= q.length; ++i) {
            char a = q[i-1];
            for (int j = 1; j <= s.length; ++j) {
                char b = s[j-1];
                int mat = M[i-1][j-1] + (a == b ? match : 0);
                int del = M[i-1][j] + gap;
                int ins = M[i][j-1] + gap;
                M[i][j] = Math.max(mat, Math.max(del, ins));
            }
        }
        StringBuilder qa = new StringBuilder ();
        StringBuilder qs = new StringBuilder ();
        StringBuilder qq = new StringBuilder ();
        int i = q.length;
        int j = s.length;
        BitSet qsites = new BitSet();
        BitSet tsites = new BitSet();

        while (i > 0 && j > 0) {
            char a = q[i-1];
            char b = s[j-1];
            boolean matched =
                    Character.toUpperCase(a) == Character.toUpperCase(b);

            if (i > 0 && j > 0 && M[i][j] == M[i-1][j-1]
                    + (matched ? match : 0)) {
                qa.insert(0, a);
                qs.insert(0, b);
                qq.insert(0, matched ? '|' : ' ');
                --i;
                --j;
                qsites.set(i+seg.qi);
                tsites.set(j+seg.ti);
            }
            else if (i > 0 && M[i][j] == M[i-1][j] + gap) {
                qa.insert(0, a);
                qs.insert(0, '-');
                qq.insert(0, ' ');
                --i;
            }
            else {
                qa.insert(0, '-');
                qs.insert(0, b);
                qq.insert(0, ' ');
                --j;
            }
        }

        /*
        System.err.println("** score: "+M[q.length()][s.length()]);
        System.err.println(qa);
        System.err.println(qq);
        System.err.println(qs);
         */

        int score = M[q.length][s.length];

        //Local alignment score 
        // (fraction of aligned residues in the longest local sequence)
        double iden=(double)score/Math.max(q.length,s.length);

        //Global alignment score
        // (fraction of aligned residues in the longest global sequence)
        double glob=(double)score/Math.max(query.length(),target.length());

        //Sub alignment score
        // (local alignment score, multiplied by the fraction of the
        //  residues found in the query)
        //
        //	The purpose of this is to penalize the local identity score
        //  such that a strong local alignment that doesn't have much 
        //  of the query present is not weighted as strongly

        //  As an example where we want to have high match
        //   Query: ABC
        //  Target: XXXXXXXXABCXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
        //  
        //	As an example where we want to have a low match
        //   Query: XXXXXXXXABCXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
        //  Target: ABC
        //
        double sub=iden*score/(double)query.length();

        String rangeq=String.format("%1$5d - %2$d", seg.qi+1,seg.qj);
        String ranget=String.format("%1$5d - %2$d", seg.ti+1,seg.tj);

        int diff=ranget.length()-rangeq.length();
        String pad="         ".substring(0, Math.abs(diff)+1);
        rangeq+=(diff>0)?pad:" ";
        ranget+=(diff<0)?pad:" ";

        return new Alignment (seg, qa.toString(), qs.toString(),
                qa+rangeq +"[Query]"
                        +"\n"+qq+"\n"
                        +qs+ranget + "[Target]",
                        score, 
                        iden,
                        glob,
                        sub,
                        qsites,
                        tsites

                );
    }

    @SuppressWarnings("unchecked")
    static <T> T getOrElse (String key, Callable<T> generator)
            throws Exception {
        Object value = CACHE.get().get(key);


        if (value == null) {
            value = generator.call();
            CACHE.get().put(new Element (key, value));

        }else {
            value = ((Element)value).getObjectValue();
        }

        return (T)value;
    }

    static void dump (ResultEnumeration results) throws Exception {
        while (results.hasMoreElements()) {
            Result res = results.nextElement();
            System.err.println("+++++ "+res.query);
            System.err.println("----- "+res.target);
            System.err.println();
            for (Alignment aln : res.alignments) {
                System.err.println(aln);
                System.err.println();
            }
            System.err.println("\n");
        }
    }



    public boolean isClosed() {
        try{
            indexWriter.numDocs();
            return false;
        }catch(AlreadyClosedException e){
            System.out.println("Already closed");
        }
        return true;
    }
}
