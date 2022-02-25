package ix.core.plugins;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import gov.nih.ncats.molwitch.Atom;
import gov.nih.ncats.molwitch.Bond;
import gov.nih.ncats.molwitch.Chemical;
import gov.nih.ncats.structureIndexer.StructureIndexer;
import gov.nih.ncats.structureIndexer.StructureIndexer.*;
import org.apache.lucene.search.Filter;
import org.apache.lucene.search.Query;

import play.Application;
import play.Logger;
import play.Plugin;


public class StructureIndexerPlugin extends Plugin {
    private final Application app;
    private IxContext ctx;
    private StandardizedStructureIndexer indexer;
    private boolean closed=false;

    public StructureIndexerPlugin (Application app) {
        this.app = app;
    }

    @Override
    public void onStart () {
        ctx = app.plugin(IxContext.class);
        if (ctx == null)
            throw new IllegalStateException
                ("IxContext plugin is not loaded!");
        
        try {
            indexer = new StandardizedStructureIndexer(StructureIndexer.open(ctx.structure()));
            closed=false;
            Logger.info("Plugin "+getClass().getName()+" started!");
        }
        catch (IOException ex) {
            throw new RuntimeException
                ("Can't initialize structure indexer", ex);
        }
    }

    @Override
    public void onStop () {
    	
        if (indexer != null){
        	try{
        		indexer.shutdown();
        	}catch(Throwable e){
        		System.out.println("########################");
        		System.out.println("########################");
        		System.out.println("PROBLEM SHUTTING DOWN STRUCTURE INDEXER");
        		e.printStackTrace();
        	}
        }
        Logger.info("Plugin "+getClass().getName()+" stopped!");
        closed=true;
    }

    public boolean enabled () { return !closed; }
    public StandardizedStructureIndexer getIndexer () { return indexer; }
    
    
    
    
    
    /**
     * Used as a delegate to fix certain structure searches. This class just delegates, to a real {@link StructureIndexer}
     * but whenever it would pass it a molecule, it makes sure to standardize it first.
     * 
     * @author tyler
     *
     */
    public static class StandardizedStructureIndexer{

		StructureIndexer delegate;
		
		
		/**
		 * This method just finds neighboring atoms which are both charged in opposite
		 * ways. When that happens, it will change the bond between them to be a double
		 * bond, and will set the atoms to be 0 charge. This is for sulfoxides and 
		 * nitro groups.
		 * 
		 * @param m
		 * @return
		 */
		public static Chemical standardizeCharges(Chemical m){
			List<Atom> malist= m.atoms().filter(ma-> ma.getCharge() !=0)
			                            .collect(Collectors.toList());
			for(Atom c: malist){
				if(c.getCharge()==0)continue;
				
				for(Bond b : c.getBonds()){
					if(b.getBondType() == Bond.BondType.SINGLE){
						Atom otherAtom = b.getOtherAtom(c);
						if(malist.contains(otherAtom) && otherAtom.getCharge()==-c.getCharge()){
							b.setBondType(Bond.BondType.DOUBLE);
							otherAtom.setCharge(0);
							c.setCharge(0);
						}
					}
				}

			}
			return m;
		}
		
		
		
		public static Chemical getStandardized(Chemical m){

			
			m.aromatize();

			
			return standardizeCharges(m);
			
		}
		
		public static Chemical getMolecule(String str){
			try {
				return Chemical.parse(str);
			} catch (IOException e) {
				e.printStackTrace();
				System.out.println("CHEMICAL PARSE ERROR!!! for: "+ str);
				return null;
			}
		}
		
		
		
		public void add(String id, Chemical struc) throws IOException {
			delegate.add(id, getStandardized(struc));
		}

		public void add(String source, String id, Chemical struc) throws IOException {
			delegate.add(source, id, getStandardized(struc));
		}

		public void add(String arg0, String arg1, String arg2) throws IOException {
			this.add(arg0, arg1, getMolecule(arg2));
		}

		public void add(String id, String struc) throws IOException {
			this.add(id, getMolecule(struc));
		}

		public boolean equals(Object obj) {
			return delegate.equals(obj);
		}

		public File getBasePath() {
			return delegate.getBasePath();
		}

		public Codebook[] getCodebooks() {
			return delegate.getCodebooks();
		}

		public String[] getFields() {
			return delegate.getFields();
		}

		public Map<String, Integer> getSources() throws IOException {
			return delegate.getSources();
		}

		public int hashCode() {
			return delegate.hashCode();
		}

		public int[] histogram(String field, double[] range) throws IOException {
			return delegate.histogram(field, range);
		}

		public int[] histogram(String field, int[] range) throws IOException {
			return delegate.histogram(field, range);
		}

		public int[] histogram(String field, long[] range) throws IOException {
			return delegate.histogram(field, range);
		}

		public long lastModified() {
			return delegate.lastModified();
		}

		public void remove(String source, String id) throws IOException {
			delegate.remove(source, id);
		}

		public void remove(String source) throws IOException {
			delegate.remove(source);
		}

		public ResultEnumeration search(Filter... filters) throws Exception {
			return delegate.search(filters);
		}

		public ResultEnumeration search(Query query, int max, Filter... filters) throws Exception {
			return delegate.search(query, max, filters);
		}

		public ResultEnumeration search(Query query) throws Exception {
			return delegate.search(query);
		}

		public ResultEnumeration search(String query, int max, Filter... filters) throws Exception {
			return delegate.search(query, max, filters);
		}

		public ResultEnumeration search(String query, int max, int nthreads) throws Exception {
			return delegate.search(query, max, nthreads);
		}

		public ResultEnumeration search(String query, int max) throws Exception {
			return delegate.search(query, max);
		}


		public ResultEnumeration search(String query) throws Exception {
			return delegate.search(query);
		}

		public void shutdown() {
			delegate.shutdown();
		}

		public ResultEnumeration similarity(Chemical query, double threshold, Filter... filters) throws Exception {
			return delegate.similarity(getStandardized(query), threshold, filters);
		}

		public ResultEnumeration similarity(Chemical query, double threshold, int max, int nthreads, Filter... filters)
				throws Exception {
			return delegate.similarity(getStandardized(query), threshold, max, nthreads, filters);
		}

		public ResultEnumeration similarity(Chemical query, double threshold, int max, int nthreads) throws Exception {
			return delegate.similarity(getStandardized(query), threshold, max, nthreads);
		}

		public ResultEnumeration similarity(String query, double threshold, Filter... filters) throws Exception {
			return this.similarity(getMolecule(query), threshold, filters);
		}

		public ResultEnumeration similarity(String query, double threshold, int max) throws Exception {
			return this.similarity(getMolecule(query), threshold, max, 1);
		}

		public ResultEnumeration similarity(String query, double threshold) throws Exception {
			return this.similarity(getMolecule(query), threshold);
		}

		public int size() {
			return delegate.size();
		}

		public void stats(PrintStream ps) throws IOException {
			delegate.stats(ps);
		}

		public ResultEnumeration substructure(Chemical query, Filter... filters) throws Exception {
			return delegate.substructure(getStandardized(query), filters);
		}

		public ResultEnumeration substructure(Chemical query, int max, int nthreads, Filter... filters)
				throws Exception {
			return delegate.substructure(getStandardized(query), max, nthreads, filters);
		}

		public ResultEnumeration substructure(Chemical query, int max, int nthreads) throws Exception {
			return delegate.substructure(getStandardized(query), max, nthreads);
		}

		public ResultEnumeration substructure(Chemical query) throws Exception {
			return delegate.substructure(getStandardized(query));
		}

		public ResultEnumeration substructure(String query, Filter... filters) throws Exception {
			return this.substructure(getMolecule(query), filters);
		}

		public ResultEnumeration substructure(String query, int max, int nthreads, Filter... filters) throws Exception {
			return this.substructure(getMolecule(query), max, nthreads, filters);
		}

		/**
		 * Max not supported at this time
		 * @param query
		 * @param max
		 * @return
		 * @throws Exception
		 */
		public ResultEnumeration substructure(String query, int max) throws Exception {
			return this.substructure(getMolecule(query));
		}

		public String toString() {
			return delegate.toString();
		}

    	
    	
    	public StandardizedStructureIndexer(StructureIndexer d) throws IOException{
    		this.delegate=d;
    		
    	}
    	
    	public StructureIndexer getDelegate(){

    		return this.delegate;
    	}
		
		
    	
    }

}
