package ix.core.search;

import akka.actor.ActorRef;
import akka.actor.Terminated;
import akka.actor.UntypedActor;
import play.Logger;

class SearchResultHandler extends UntypedActor {
    @Override
    public void onReceive (Object obj) {
        if (obj instanceof SearchResultProcessor) {
            SearchResultProcessor processor = (SearchResultProcessor)obj;
            SearchResultContext ctx = processor.getContext();               
            try {
                ctx.setStatus(SearchResultContext.Status.Running);
                ctx.start = System.currentTimeMillis();
                
                int count = processor.process();
                if(count==0){
                	ctx.setStatus(SearchResultContext.Status.Done);
                }else{
                	ctx.setStatus(SearchResultContext.Status.Determined);
                }
                
                ctx.stop = System.currentTimeMillis();
                Logger.debug("Actor "+self()+" finished; "+count
                             +" search result(s) instrumented!");
                context().stop(self ());
            }
            catch (Exception ex) {
                ctx.setStatus(SearchResultContext.Status.Failed);
                ctx.setMessage(ex.getMessage());
                ex.printStackTrace();
                Logger.error("Unable to process search results", ex);
            }
        }
        else if (obj instanceof Terminated) {
            ActorRef actor = ((Terminated)obj).actor();
            Logger.debug("Terminating actor "+actor);
        }
        else {
            unhandled (obj);
        }
    }

    public void preStart () {
    }
    
    @Override
    public void postStop () {
        Logger.debug(getClass().getName()+" "+self ()+" stopped!");
    }
}