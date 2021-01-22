package ix.ginas.controllers.v1;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import ix.core.controllers.EntityFactory;
import ix.core.search.SearchResult;
import ix.core.search.SearchResultContext;
import ix.core.util.Java8Util;
import ix.ginas.models.v1.Substance;
import play.mvc.Results;

import play.Logger;
import play.Play;
import play.db.ebean.Model;

import ix.ncats.controllers.App;

public class Java8FactoryHelper {
	public static play.mvc.Result substanceFactoryDetailedSearch(SearchResultContext context) throws InterruptedException, ExecutionException{
        context.setAdapter((srequest, ctx) -> {
            try {
                SearchResult sr = App.getResultFor(ctx, srequest,true);
                
                List<Substance> rlist = new ArrayList<Substance>();
                
                sr.copyTo(rlist, srequest.getOptions().getSkip(), srequest.getOptions().getTop(), true); // synchronous
                for (Substance s : rlist) {
                    s.setMatchContextFromID(ctx.getId());
                }
                return sr;
            } catch (Exception e) {
                e.printStackTrace();
                throw new IllegalStateException("Error fetching search result", e);
            }
        });

        String s = play.mvc.Controller.request().getQueryString("sync");

        if ("true".equals(s) || "".equals(s)) {
            try {
                context.getDeterminedFuture().get(1, TimeUnit.MINUTES);
                return play.mvc.Controller.redirect(context.getResultCall());
            } catch (TimeoutException e) {
                Logger.warn("Structure search timed out!", e);
            }
        }
        return Java8Util.ok(EntityFactory.getEntityMapper().valueToTree(context));
    }
}
