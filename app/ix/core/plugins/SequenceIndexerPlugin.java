package ix.core.plugins;

import java.io.File;
import java.io.IOException;

import ix.seqaln.SequenceIndexer;
import play.Application;
import play.Logger;
import play.Play;
import play.Plugin;

public class SequenceIndexerPlugin extends Plugin {
    private final Application app;
    private IxContext ctx;
    private SequenceIndexer indexer;
    private boolean closed=false;

    public SequenceIndexerPlugin (Application app) {
        this.app = app;
    }

    @Override
    public void onStart () {
        ctx = app.plugin(IxContext.class);
        if (ctx == null)
            throw new IllegalStateException
                ("IxContext plugin is not loaded!");
        
        try {
            File sequence = ctx.sequence();
            indexer = SequenceIndexer.open(sequence);

            int kmersize = app.configuration().getInt("ix.kmer.default", 3);
            int nuc = app.configuration().getInt("ix.kmer.nuc", 3);
            int prot = app.configuration().getInt("ix.kmer.protein", 3);
            indexer.setKmerSize(kmersize);
            indexer.setNucleicKmer(nuc);
            indexer.setProteinKmer(prot);

            Logger.info("Plugin "+getClass().getName()+" started!");        
        }
        catch (IOException ex) {
        	System.out.println("Error loading sequence indexer");
        	ex.printStackTrace();
            throw new RuntimeException
                ("Can't initialize sequence indexer", ex);
        }
    }

    @Override
    public void onStop () {
        if (indexer != null){
        	try{
        		
        		indexer.shutdown();
        	}catch(Exception e){
        		System.out.println("Failed to shutdown sequence indexer");
        		e.printStackTrace();
        	}
        }
        Logger.info("Plugin "+getClass().getName()+" stopped!");
        closed=true;
    }

    public boolean enabled () { return !closed; }
    public SequenceIndexer getIndexer () { return indexer; }


    public void add(String id, String sequence) throws IOException{
        indexer.add(id, sequence);
    }
}
