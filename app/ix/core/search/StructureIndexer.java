package ix.core.search;

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

import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.SerialMergeScheduler;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
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

import org.apache.lucene.queryparser.flexible.standard.StandardQueryParser;
import org.apache.lucene.queryparser.flexible.core.QueryNodeException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;

import org.apache.lucene.facet.*;
import org.apache.lucene.facet.range.*;
import org.apache.lucene.facet.taxonomy.*;
import org.apache.lucene.facet.taxonomy.directory.*;
import org.apache.lucene.facet.sortedset.*;

import org.apache.lucene.search.suggest.DocumentDictionary;
import org.apache.lucene.search.suggest.Lookup;
import org.apache.lucene.search.suggest.analyzing.AnalyzingSuggester;
import org.apache.lucene.search.suggest.analyzing.AnalyzingInfixSuggester;
import org.apache.lucene.search.suggest.DocumentDictionary;
import org.apache.lucene.search.suggest.Lookup;
import org.apache.lucene.search.suggest.analyzing.AnalyzingSuggester;
import org.apache.lucene.search.suggest.analyzing.AnalyzingInfixSuggester;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.ObjectWriter;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.actor.UntypedActorFactory;
import akka.actor.PoisonPill;
import akka.actor.Props;
import akka.actor.Inbox;
import akka.actor.Terminated;
import akka.routing.Broadcast;
import akka.routing.RouterConfig;
import akka.routing.FromConfig;
import akka.routing.RoundRobinRouter;
import akka.routing.SmallestMailboxRouter;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import scala.concurrent.duration.Duration;
import scala.concurrent.duration.FiniteDuration;
import scala.collection.immutable.Iterable;
import scala.collection.JavaConverters;

import play.Logger;

import chemaxon.formats.MolImporter;
import chemaxon.struc.Molecule;

public class StructureIndexer {
    static final Version LUCENE_VERSION = Version.LATEST;
    static final String CONFIG_FILE = "config.json";

    static class FingerprintConfig {
        public int size;
        public int bits;
        public int depth;

        FingerprintConfig (int size, int bits, int depth) {
            this.size = size;
            this.bits = bits;
            this.depth = depth;
        }
    }

    static public class Codebook {
        int[] dict;
        int[][] eqv;
        Random rand = new Random ();
        
        public Codebook (int size, int fpsize) {
            BitSet used = new BitSet ();
            int[] d = new int[size];
            for (int i = 0; i < d.length; ++i) {
                int b = rand.nextInt(fpsize);
                if (!used.get(b)) {
                    used.set(b);
                    d[i] = b;
                }
            }
            
            setDictionary (d);
        }
        
        public Codebook (int[] dict) {
            setDictionary (dict);
        }

        public int[] getDictionary () { return dict; }
        public void setDictionary (int[] dict) {
            if (dict == null || dict.length == 0)
                throw new IllegalArgumentException ("Invalid dictionary!");
            
            BitSet[] bsets = new BitSet[1<<dict.length];
            for (int i = 0; i < bsets.length; ++i)
                bsets[i] = new BitSet ();

            for (int i = 1; i < bsets.length; ++i) {
                for (int j = 1; j <  bsets.length; ++j) 
                    if ((i & j) == j) 
                        bsets[j].set(i);
            }
            
            eqv = new int[bsets.length][];
            for (int i = 0; i < eqv.length; ++i) {
                BitSet bs = bsets[i];
                eqv[i] = new int[bs.cardinality()];
                for (int k = 0, j = bs.nextSetBit(0);
                     j>=0; j = bs.nextSetBit(j+1))
                    eqv[i][k++] = j;
            }
            
            this.dict = dict;
        }

        public int[] apply (int[] fp) {
            int code = encode (fp);
            return code == 0 ? null : eqv[code];
        }

        /**
         * an arbitrary encoding.. 
         */
        public int encode (int[] fp) { // fingerprint bits stored as ints
            int code = 0;
            for (int i = 0; i < dict.length; ++i) {
                if (get (fp, dict[i]))
                    code |= 1<<i;
            }
            return code & 0xff;
        }

        public int encode (BitSet fp) {
            int code = 0;
            for (int i = 0; i < dict.length; ++i) {
                if (fp.get(dict[i]))
                    code |= i << i;
            }
            return code & 0xff;
        }

        public String toString () {
            StringBuilder sb = new StringBuilder ("{");
            for (int i = 0; i < dict.length; ++i) {
                if (i > 0) sb.append(",");
                sb.append(dict[i]);
            }
            sb.append("}");
            return sb.toString();
        }
    }

    static boolean get (int[] fp, int bit) {
        return (fp[bit/32] & ((1 << (31-(bit % 32))))) != 0;
    }
    

    private File baseDir;
    private Directory indexDir;
    private IndexWriter indexWriter;
    private Analyzer indexAnalyzer;
    private FacetsConfig facetsConfig;
    private Codebook[] codebooks;

    static ConcurrentMap<File, StructureIndexer> indexers = 
        new ConcurrentHashMap<File, StructureIndexer>();

    public static StructureIndexer getInstance (File baseDir)
        throws IOException {
        if (indexers.containsKey(baseDir)) 
            return indexers.get(baseDir);

        try {
            StructureIndexer indexer = new StructureIndexer (baseDir);
            StructureIndexer old = indexers.putIfAbsent(baseDir, indexer);
            return old == null ? indexer : old;
        }
        catch (IOException ex) {
            ex.printStackTrace();
            return indexers.get(baseDir);
        }
    }

    protected StructureIndexer (File dir) throws IOException {
        if (!dir.isDirectory())
            throw new IllegalArgumentException ("Not a directory: "+dir);

        File index = new File (dir, "index");
        if (!index.exists())
            index.mkdirs();
        indexDir = new NIOFSDirectory 
            (index, NoLockFactory.getNoLockFactory());

        File config = new File (dir, CONFIG_FILE);
        if (config.exists()) {
        }
        else {
            Logger.info("No configuration file "+config+" found; using "
                        +"defaults from application.conf!");
        }

        codebooks = new Codebook[256];
        for (int i = 0; i < codebooks.length; ++i)
            codebooks[i] = new Codebook (8, 512);        
    }

    public void shutdown () {
        try {
            if (indexWriter != null)
                indexWriter.close();
            indexDir.close();
        }
        catch (IOException ex) {
            ex.printStackTrace();
        }
        indexers.remove(baseDir);
    }

    public void add (Object id, String struc) throws IOException {
        
    }

    public void add (Object id, Molecule struc) throws IOException {
    }
}
