import java.io.*;
import java.util.*;

import play.GlobalSettings;
import play.Application;
import play.Logger;

import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.SerialMergeScheduler;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.NIOFSDirectory;
import org.apache.lucene.store.NoLockFactory;
import org.apache.lucene.util.Version;

import org.apache.lucene.analysis.core.KeywordAnalyzer;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.miscellaneous.PerFieldAnalyzerWrapper;

public class Global extends GlobalSettings {

    static Global instance;
    public static Global getInstance () {
        return instance;
    }

    private File home = new File (".");
    private Directory idxDir;
    private IndexWriter idxWriter;
    private Analyzer idxAnalyzer;

    protected void init (Application app) throws Exception {
        String h = app.configuration().getString("granite.home");
        if (h != null) {
            home = new File (h);
            if (!home.exists())
                home.mkdirs();
        }

        if (!home.exists())
            throw new IllegalArgumentException
                ("granite.home \""+h+"\" is not accessible!");

        Logger.info("## home: \""+home.getCanonicalPath()+"\"");
        idxDir = new NIOFSDirectory (home, NoLockFactory.getNoLockFactory());
        idxAnalyzer = createIndexAnalyzer ();
        IndexWriterConfig conf = new IndexWriterConfig 
            (Version.LUCENE_4_9, idxAnalyzer);
	idxWriter = new IndexWriter (idxDir, conf);
    }

    Analyzer createIndexAnalyzer () {
        Map<String, Analyzer> fields = new HashMap<String, Analyzer>();
        fields.put("id", new KeywordAnalyzer ());
	return 	new PerFieldAnalyzerWrapper 
            (new StandardAnalyzer (Version.LUCENE_40), fields);
    }


    @Override
    public void onStart (Application app) {
        if (instance == null) {
            try {
                init (app);
            }
            catch (Exception ex) {
                ex.printStackTrace();
            }

            Logger.info("Global instance "+this);
            instance = this;
        }
        Logger.info("## starting app: secret=\""
                    +app.configuration().getString("application.secret")+"\"");
    }

    @Override
    public void onStop (Application app) {
        try {
            if (idxWriter != null)
                idxWriter.close();
        }
        catch (IOException ex) {
            ex.printStackTrace();
        }
        Logger.info("## stopping");
    }
}
