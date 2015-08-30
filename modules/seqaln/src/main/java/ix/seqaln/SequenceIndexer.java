package ix.seqaln;

import java.io.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.LongField;
import org.apache.lucene.document.IntField;
import org.apache.lucene.document.FloatField;
import org.apache.lucene.document.DoubleField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.document.IntDocValuesField;
import org.apache.lucene.document.NumericDocValuesField;
import org.apache.lucene.document.FloatDocValuesField;
import org.apache.lucene.document.DoubleDocValuesField;
import static org.apache.lucene.document.Field.Store.*;

import org.apache.lucene.index.AtomicReaderContext;
import org.apache.lucene.index.IndexReaderContext;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.SerialMergeScheduler;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.index.FieldInfo.IndexOptions;

import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.NIOFSDirectory;
import org.apache.lucene.store.NoLockFactory;
import org.apache.lucene.util.Version;
import org.apache.lucene.util.BytesRef;

import org.apache.lucene.analysis.core.KeywordAnalyzer;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.miscellaneous.PerFieldAnalyzerWrapper;

import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.DisjunctionMaxQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.RegexpQuery;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.Filter;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.FieldCacheTermsFilter;
import org.apache.lucene.search.NumericRangeQuery;
import org.apache.lucene.search.FilteredQuery;

import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import net.sf.ehcache.config.CacheConfiguration;
import net.sf.ehcache.Statistics;

public class SequenceIndexer {
    static final String CACHE_NAME =
        SequenceIndexer.class.getName()+".Cache";
    static final Version LUCENE_VERSION = Version.LATEST;

    static final CacheManager CACHE_MANAGER = CacheManager.getInstance();
    static final Ehcache CACHE = CACHE_MANAGER.addCacheIfAbsent(CACHE_NAME);

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
            int d = gap () - hsp.gap();
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

    public static class Alignment implements Comparable<Alignment> {
        public final int qi, qj; // coordinate of query
        public final int si, sj; // coordinate of database sequence
        public final String qseg; // segment of query
        public final String sseg; // segment of sequence
        public final String alignment; // full alignment string
        public final int score;
        public final double iden;

        Alignment (int qi, int qj, int si, int sj,
                   String qseg, String sseg,
                   String alignment, int score) {
            this.qi = qi;
            this.qj = qj;
            this.si = si;
            this.sj = sj;
            this.qseg = qseg;
            this.sseg = sseg;
            this.alignment = alignment;
            this.score = score;
            iden = (double)score/Math.max(qj-qi, sj-si);
        }
        
        public int compareTo (Alignment aln) {
            int d = aln.score - score;
            if (d == 0) {
                if (iden < aln.iden) d = 1;
                else if (iden > aln.iden) d = -1;
            }
            if (d == 0)
                d = qi - aln.qi;
            
            return d;
        }

        public String toString () {
            return "[score] "+score+"\n[identity] "
                +String.format("%1$.3f", iden)
                +"\n[alignment]\n"+alignment;
        }
    }
    
    public static class Result {
        public final CharSequence query;
        public final String id;
        public final CharSequence refseq;
        public final List<Alignment> alignments = new ArrayList<Alignment>();

        Result () {
            query = null;
            id = null;
            refseq = null;
        }
        Result (String id, CharSequence query, CharSequence refseq) {
            this.id = id;
            this.query = query;
            this.refseq = refseq;
        }
    }

    static final Result POISON_RESULT = new Result ();

    public static class ResultEnumeration implements Enumeration<Result> {
        final BlockingQueue<Result> queue;
        Result next;
        
        ResultEnumeration (BlockingQueue<Result> queue) {
            this.queue = queue;
            next ();
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
            Result current = next;
            next ();
            return current;
        }
    }
    
    public static final String FIELD_KMER = "_KMER";
    public static final String FIELD_ID = "_ID";
    public static final String FIELD_SEQ = "_SEQ";
    public static final String FIELD_SOURCE = "_SOURCE";
    public static final String FIELD_LENGTH = "_LENGTH";
    public static final String FIELD_POSITION = "_POSITION";
    public static final String FIELD_TEXT = "text";

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

    private AtomicLong lastModified = new AtomicLong (0);
    
    public static SequenceIndexer openReadOnly (File dir) throws IOException {
        return new SequenceIndexer (dir);
    }
    
    public static SequenceIndexer open (File dir) throws IOException {
        return new SequenceIndexer (dir, false);
    }
    
    public SequenceIndexer (File dir) throws IOException {
        this (dir, true);
    }
    
    public SequenceIndexer (File dir, boolean readOnly) throws IOException {
        this (dir, readOnly, Executors.newCachedThreadPool());
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
            _kmerReader = DirectoryReader.open(kmerWriter, true);
            _indexReader = DirectoryReader.open(indexWriter, true);
        }
        else {
            _kmerReader = DirectoryReader.open(kmerDir);
            _indexReader = DirectoryReader.open(indexDir);
        }
        
        this.baseDir = dir;
        this.threadPool = threadPool;   
    }

    static Analyzer createIndexAnalyzer () {
        Map<String, Analyzer> fields = new HashMap<String, Analyzer>();
        fields.put(FIELD_ID, new KeywordAnalyzer ());
        fields.put(FIELD_KMER, new KeywordAnalyzer ());
        return  new PerFieldAnalyzerWrapper 
            (new StandardAnalyzer (LUCENE_VERSION), fields);
    }

    protected synchronized DirectoryReader getKmerReader () throws IOException {
        DirectoryReader reader = DirectoryReader.openIfChanged(_kmerReader);
        if (reader != null) {
            _kmerReader.close();
            _kmerReader = reader;
        }
        return _kmerReader;
    }

    protected synchronized DirectoryReader getIndexReader ()
        throws IOException {
        DirectoryReader reader = DirectoryReader.openIfChanged(_indexReader);
        if (reader != null) {
            _indexReader.close();
            _indexReader = reader;
        }
        return _indexReader;
    }

    protected IndexSearcher getIndexSearcher () throws IOException {
        return new IndexSearcher (getIndexReader ());
    }
    
    protected IndexSearcher getKmerSearcher () throws IOException {
        return new IndexSearcher (getKmerReader ());
    }

    public File getBasePath () { return baseDir; }
    
    public void shutdown () {
        try {
            if (_kmerReader != null)
                _kmerReader.close();
            if (_indexReader != null)
                _indexReader.close();
            if (indexWriter != null)
                indexWriter.close();
            if (kmerWriter != null)
                kmerWriter.close();
            kmerDir.close();
            indexDir.close();

            if (localThreadPool)
                threadPool.shutdown();
        }
        catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public void add (String id, CharSequence seq)
        throws IOException {
        if (indexWriter == null)
            throw new RuntimeException ("Index is read-only!");

        try {
            //System.err.println(id+": indexing "+seq+"...");
            Document doc = new Document ();
            StringField idf = new StringField (FIELD_ID, id, YES);
            doc.add(idf);
            doc.add(new IntField (FIELD_LENGTH, seq.length(), NO));
            doc.add(new StringField (FIELD_SEQ, seq.toString(), YES));
            indexWriter.addDocument(doc);
            
            Kmers kmers = Kmers.create(seq);
            for (String kmer : kmers.kmers()) {
                BitSet positions = kmers.positions(kmer);
                StringField kmerf = new StringField (FIELD_KMER, kmer, YES);
                doc = new Document ();
                doc.add(idf);
                doc.add(kmerf);
                //System.err.println(kmer+": "+positions);
                for (int i = positions.nextSetBit(0);
                     i>=0; i = positions.nextSetBit(i+1)) {
                    doc.add(new IntField (FIELD_POSITION, i, YES));
                }
                kmerWriter.addDocument(doc);
            }
        }
        finally {
            lastModified.set(System.currentTimeMillis());
        }
    }

    public long lastModified () { return lastModified.get(); }
    
    public ResultEnumeration search (CharSequence query) {
        return search (query, 0.4);
    }

    public ResultEnumeration search (CharSequence query, double identity) {
        return search (query, identity, 3);
    }
    
    public ResultEnumeration search (final CharSequence query,
                                     final double identity, final int gap) {
        final BlockingQueue<Result> out = new LinkedBlockingQueue<Result>();
        threadPool.submit(new Runnable () {
                public void run () {
                    try {
                        search (out, query, identity, gap);
                        out.put(POISON_RESULT); // finish
                    }
                    catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            });
        
        return new ResultEnumeration (out);
    }

    protected void search (BlockingQueue<Result> results,
                           CharSequence query, double identity, int gap)
        throws Exception {

        final IndexSearcher searcher = getKmerSearcher ();
        Kmers kmers = Kmers.create(query);
        final int K = kmers.getK();
        
        int ndocs = searcher.getIndexReader().numDocs();
        final Map<String, List<HSP>> hsp = new TreeMap<String, List<HSP>>();
        for (String kmer : kmers.kmers()) {
            TermQuery tq = new TermQuery (new Term (FIELD_KMER, kmer));
            TopDocs docs = searcher.search(tq, ndocs);
            BitSet positions = kmers.positions(kmer);
            for (int i = 0; i < docs.totalHits; ++i) {
                Document doc = searcher.doc(docs.scoreDocs[i].doc);
                final String id = doc.get(FIELD_ID);
                
                List<HSP> hits = hsp.get(id);
                if (hits == null) {
                    hits = new ArrayList<HSP>();
                    hsp.put(id, hits);
                }
                
                IndexableField[] pos = doc.getFields(FIELD_POSITION);
                for (int j = 0; j < pos.length; ++j) {
                    int k = pos[j].numericValue().intValue();
                    for (int l = positions.nextSetBit(0);
                         l >= 0; l = positions.nextSetBit(l+1)) {
                        hits.add(new HSP (kmer, l, k));
                    }
                }
            }
        }

        // process in the background and return immediately
        String qs = query.toString();
        for (Map.Entry<String, List<HSP>> me : hsp.entrySet()) {
            String seq = getSeq (me.getKey());
            Result result = new Result (me.getKey(), qs, seq);
            /*
              System.err.println(" Query: "+query);
              System.err.println("Target: "+seq);
            */
            Collections.sort(me.getValue());
            
            HSP bgn = null, end = null;
            int score = 0;
            for (HSP h : me.getValue()) {
                //System.err.println("  "+h);
                if (end == null) {
                    bgn = h;
                }
                else {
                    //System.err.println("    ^"+end);
                    if (h.i < end.i || h.j < end.j
                        || ((h.i - (end.i+K)) > gap
                            && (h.j - (end.j+K)) > gap)
                        || h.gap() - end.gap() > gap
                        ) {
                        //System.err.println(" ** start: "+bgn+" end: "+end);
                        // now do global alignment of the subsequence
                        Alignment aln = align
                            (qs, bgn.i, end.i+K, seq, bgn.j, end.j+K);
                        if (aln.score > score)
                            score = aln.score;
                        result.alignments.add(aln);
                        bgn = h;
                    }
                }
                end = h;
            }
            //System.err.println(" ** start: "+bgn+" end: "+end);
            Alignment aln = align (qs, bgn.i, end.i+K, seq, bgn.j, end.j+K);
            result.alignments.add(aln);
            if (aln.score > score)
                score = aln.score;

            double sim = (double)score/Math.min(seq.length(), query.length());
            if (sim >= identity) {
                Collections.sort(result.alignments);
                results.put(result);

                System.err.println("+++++ "+result.query);
                System.err.println("----- "+result.refseq);
                System.err.println();
                for (Alignment a : result.alignments) 
                    System.err.println(a);
                System.err.println();
            }
            else {
                //System.err.println(me.getKey()+": "+sim);
            }
        }
    }

    protected String getSeq (final String id) {
        try {
            final IndexSearcher indexer = getIndexSearcher ();
            return getOrElse
                (getClass().getName()+"/"+FIELD_SEQ+"/"
                 +id, new Callable<String> () {
                         public String call () throws Exception {
                             //System.err.println("Cache missed: "+id);
                             TopDocs docs = indexer.search
                                 (new TermQuery (new Term (FIELD_ID, id)), 1);
                             if (docs.totalHits > 0) {
                                 Document d = indexer.doc
                                     (docs.scoreDocs[0].doc);
                                 return d.get(FIELD_SEQ);
                             }
                             return null;
                         }
                     });
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    static Alignment align (String q, int i, int j,
                            String s, int k, int l) {
        return align (q, i, j, s, k, l, 1, -1);
    }

    /**
     * do global alignment on subsequences that have been extracted
     * by the HSP segments
     */
    static Alignment align (String query, int qi, int qj,
                            String refseq, int si, int sj,
                            int match, int gap) {
        String q = query.substring(qi, qj);
        String s = refseq.substring(si, sj);

        /*
        System.err.println("** aligning subsequences...");
        System.err.println(q);
        System.err.println(s);
        */
        
        int[][] M = new int[q.length()+1][s.length()+1];
        for (int i = 0; i <= q.length(); ++i)
            M[i][0] = gap*i;
        for (int i = 0; i <= s.length(); ++i)
            M[0][i] = gap*i;
        for (int i = 1; i <= q.length(); ++i) {
            char a = Character.toUpperCase(q.charAt(i-1));
            for (int j = 1; j <= s.length(); ++j) {
                char b = Character.toUpperCase(s.charAt(j-1));
                int mat = M[i-1][j-1] + (a == b ? match : 0);
                int del = M[i-1][j] + gap;
                int ins = M[i][j-1] + gap;
                M[i][j] = Math.max(mat, Math.max(del, ins));
            }
        }
        StringBuilder qa = new StringBuilder ();
        StringBuilder qs = new StringBuilder ();
        StringBuilder qq = new StringBuilder ();
        int i = q.length();
        int j = s.length();
        while (i > 0 && j > 0) {
            char a = q.charAt(i-1);
            char b = s.charAt(j-1);
            boolean matched =
                Character.toUpperCase(a) == Character.toUpperCase(b);
            
            if (i > 0 && j > 0 && M[i][j] == M[i-1][j-1]
                + (matched ? match : 0)) {
                qa.insert(0, a);
                qs.insert(0, b);
                qq.insert(0, matched ? '|' : ' ');
                --i;
                --j;
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
        
        return new Alignment (qi, qj, si, sj, qa.toString(), qs.toString(),
                              qa+String.format("%1$4d - %2$d", qi,qj)+"\n"
                              +qq+"\n"
                              +qs+String.format("%1$4d - %2$d", si,sj),
                              M[q.length()][s.length()]);
    }

    static <T> T getOrElse (String key, Callable<T> generator)
        throws Exception {
        Object value = CACHE.get(key);
        if (value == null) {
            value = generator.call();
            CACHE.put(new Element (key, value));
        }
        else {
            value = ((Element)value).getObjectValue();
        }
        
        return (T)value;
    }

    static void dump (ResultEnumeration results) throws Exception {
        while (results.hasMoreElements()) {
            Result res = results.nextElement();
            System.err.println("+++++ "+res.query);
            System.err.println("----- "+res.refseq);
            System.err.println();
            for (Alignment aln : res.alignments) {
                System.err.println(aln);
                System.err.println();
            }
            System.err.println("\n");
        }
    }
    
    public static void main (String[] argv) throws Exception {
        SequenceIndexer seqidx = SequenceIndexer.open(new File ("seqidx"));
        try {
            seqidx.add("1", "abcdefghijklmnabcdef");
            seqidx.add("2", "bcefgjklabc");
            seqidx.add("3", "asdflkjdflmn");
            seqidx.add("4", "TESTOSTERONE DECANOATE");
            seqidx.add("5",
"MHTGGETSACKPSSVRLAPSFSFHAAGLQMAGQMPHSHQYSDRRQPNISDQQVSALSYSD"+
"QIQQPLTNQVMPDIVMLQRRMPQTFRDPATAPLRKLSVDLIKTYKHINEVYYAKKKRRHQ"+
"QGQGDDSSHKKERKVYNDGYDDDNYDYIVKNGEKWMDRYEIDSLIGKGSFGQVVKAYDRV"+
"EQEWVAIKIIKNKKAFLNQAQIEVRLLELMNKHDTEMKYYIVHLKRHFMFRNHLCLVFEM"+
"LSYNLYDLLRNTNFRGVSLNLTRKFAQQMCTALLFLATPELSIIHCDLKPENILLCNPKR"+
"SAIKIVDFGSSCQLGQRIYQYIQSRFYRSPEVLLGMPYDLAIDMWSLGCILVEMHTGEPL"+
"FSGANEVDQMNKIVEVLGIPPAHILDQAPKARKFFEKLPDGTWNLKKTKDGKREYKPPGT"+
"RKLHNILGVETGGPGGRRAGESGHTVADYLKFKDLILRMLDYDPKTRIQPYYALQHSFFK"+
"KTADEGTNTSNSVSTSPAMEQSQSSGTTSSTSSSSGGSSGTSNSGRARSDPTHQHRHSGG"+
"HFTAAVQAMDCETHSPQVRQQFPAPLGWSGTEAPTQVTVETHPVQETTFHVAPQQNALHH"+
"HHGNSSHHHHHHHHHHHHHGQQALGNRTRPRVYNSPTNSSSTQDSMEVGHSHHSMTSLSS"+
"STTSSSTSSSSTGNQGNQAYQNRPVAANTLDFGQNGAMDVNLTVYSNPRQETGIAGHPTY"+
"QFSANTGPAHYMTEGHLTMRQGADREESPMTGVCVQQSPVASS");
            
            ResultEnumeration results = seqidx.search("abcdelghilmn");
            dump (results);
            
            results = seqidx.search("testosterone undecanoate");
            dump (results);
            
            results = seqidx.search(
"MRHSKRTHCPDWDSRESWGHESYRGSHKRKRRSHSSTQENRHCKPHHQFKESDCHYLEAR"+
"SLNERDYRDRRYVDEYRNDYCEGYVPRHYHRDIESGYRIHCSKSSVRSRRSSPKRKRNRH"+
"CSSHQSRSKSHRRKRSRSIEDDEEGHLICQSGDVLRARYEIVDTLGEGAFGKVVECIDHG"+
"MDGMHVAVKIVKNVGRYREAARSEIQVLEHLNSTDPNSVFRCVQMLEWFDHHGHVCIVFE"+
"LLGLSTYDFIKENSFLPFQIDHIRQMAYQICQSINFLHHNKLTHTDLKPENILFVKSDYV"+
"VKYNSKMKRDERTLKNTDIKVVDFGSATYDDEHHSTLVSTRHYRAPEVILALGWSQPCDV"+
"WSIGCILIEYYLGFTVFQTHDSKEHLAMMERILGPIPQHMIQKTRKRKYFHHNQLDWDEH"+
"SSAGRYVRRRCKPLKEFMLCHDEEHEKLFDLVRRMLEYDPTQRITLDEALQHPFFDLLKK"+
"K");
            dump (results);
        }
        finally {
            seqidx.shutdown();
            CACHE_MANAGER.shutdown();
        }
    }
}
