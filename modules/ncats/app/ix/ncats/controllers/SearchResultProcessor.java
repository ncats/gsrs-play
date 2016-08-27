package ix.ncats.controllers;

import java.util.Enumeration;

import akka.actor.ActorRef;
import akka.actor.Props;
import ix.utils.Util;
import play.Logger;
import play.Play;
import play.libs.Akka;

/**
 * Structure searching
 */
public abstract class SearchResultProcessor<T, R> {
    protected Enumeration<T> results;
    final SearchResultContext context = new SearchResultContext ();
    boolean wait=false;
    
    public SearchResultProcessor () {
    }
    
    public void setWait(boolean wait){
    	this.wait=wait;
    }

    public void setResults (int rows, Enumeration<T> results)
        throws Exception {
        this.results = results;
        
        context.start = System.currentTimeMillis();
        if(wait){
        	
        	process();
        	context.setStatus(SearchResultContext.Status.Determined);
        	context.stop = System.currentTimeMillis();
            
        }else{
            // the idea is to generate enough results for 1 page, and 1 extra record
        	// (enough to show pagination) and return immediately. as the user pages,
            // the background job will fill in the rest of the results.
        	int count = process (rows+1);
            
            // while we continue to fetch the rest of the results in the
            // background
            ActorRef handler = Akka.system().actorOf
                (Props.create(SearchResultHandler.class));
            handler.tell(this, ActorRef.noSender());
            Logger.debug("## search results submitted: "+handler);
        }
    }
    
    public SearchResultContext getContext () { return context; }
    public boolean isDone () { return false; }

    public int process () throws Exception {
        return process (0);
    }
    
    public int process (int max) throws Exception {
        while (results.hasMoreElements()
               && !isDone () 
               && (max <= 0 || context.getCount() < max)) {
            T r = results.nextElement();
            // This will simulate a slow structure processing (e.g. slow database fetch)
            // This should be used in conjunction with another debugSpin in TextIndexer
            // to simulate both slow fetches and slow lucene processing
            //System.out.println("Processing:" + r);
            
            //Util.debugSpin(10);
            
            try {
                R obj = instrument (r);
                if (obj != null) {
                    context.add(obj);
                }
            }catch (Exception ex) {
                ex.printStackTrace();
                Logger.error("Can't process structure search result", ex);
            }
        }
        return context.getCount();
    }
    
    protected abstract R instrument (T r) throws Exception;
}