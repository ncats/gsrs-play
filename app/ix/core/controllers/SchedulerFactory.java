package ix.core.controllers;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import ix.core.NamedResource;
import ix.core.models.ETag;
import ix.core.plugins.SchedulerPlugin;
import ix.core.plugins.SchedulerPlugin.ScheduledTask;
import ix.core.util.CachedSupplier;
import ix.core.util.EntityUtils.EntityWrapper;
import ix.core.util.pojopointer.PojoPointer;
import play.Logger;
import play.Play;
import play.mvc.Result;

@NamedResource(name="scheduledjobs",
               type=ScheduledTask.class,
               description="Resource for scheduled tasks",
               adminOnly=true,
               allowSearch=false)
public class SchedulerFactory extends EntityFactory {

    static final Logger.ALogger ScheduleLogger = Logger.of("scheduledjobs");
    
    static final CachedSupplier<SchedulerPlugin> splug = CachedSupplier.of(()->{
        return Play.application().plugin(SchedulerPlugin.class);
    });
    

    public static Result count () { return ok (getCount()+""); }
    
    public static Integer getCount () {
        try {
            return splug.get().getTasks().size();
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }
    public static Result page (int top, int skip, String filter) {
        return page(top,skip,filter, (l)->l);
    }
    
    public static Result page (int top, int skip, String filter, Function<List<ScheduledTask>,Object> map) {
      //if (select != null) finder.select(select);
        final FetchOptions options = new FetchOptions (top, skip, filter);
        List<ScheduledTask> results = splug.get()
                            .getTasks()
                            .stream()
                            .sorted((t1,t2)->(int)(t1.id-t2.id))
                            .collect(Collectors.toList());
        
        
        final ETag etag = new ETag.Builder()
                .fromRequest(request())
                .options(options)
                .count(results.size())
                .sha1OfRequest("filter")
                .build();
        

        if (options.filter == null){
            etag.total = getCount ();
        }else if(etag.count<etag.top){ //if count returned is less than top,
                                       //it's done
            etag.total = etag.skip + etag.count;
        }else{
            EntityWrapper.of(etag)
            .getFinder()
            .where()
            .eq("sha1", etag.sha1)
            .orderBy("modified desc")
            .setMaxRows(1)
            .findList()
            .stream().findFirst()
            .ifPresent(e->{
                Logger.debug(">> cached "+etag.sha1+" from ETag "+e.etag);
                etag.total = e.total;
            });
            
            
        }
        
        try{
            etag.save();
        }catch (Exception e) {
            Logger.error
                ("Error saving etag. This sometimes happens on empty DB");
        }

        etag.setContent(map.apply(results));
        
        ObjectMapper mapper = getEntityMapper ();
        ObjectNode obj = (ObjectNode)mapper.valueToTree(etag);

        return ok (obj);
    }

    public static Result get (Long id, String select) {
        ObjectMapper mapper = getEntityMapper ();
        try{
        return Optional.ofNullable(get(id))
                    .map(t->(ObjectNode)mapper.valueToTree(t))
                    .map(t->ok(t))
                    .orElse(notFound ("Bad request: "+request().uri()));
        }catch(Exception e){
            e.printStackTrace();
            throw e;
        }
        
    }
    
    public static ScheduledTask get(long id){
        return splug.get().getTasks()
                .stream()
                .filter(t->t.id.equals(id))
                .findAny()
                .orElse(null);
    }

    
    public static Result field(Long id, String path) {
        return field(get(id), path);
    }
    
    public static Result stream(String field, int top, int skip){
        PojoPointer pojoPoint = PojoPointer.fromURIPath(field);
        return page(top, skip, null, (l)->EntityWrapper.of(l)
                .at(pojoPoint)
                .get()
                .getValue());
    }

}