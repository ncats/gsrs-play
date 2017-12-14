package ix.core.controllers;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import ix.core.controllers.EntityFactory.FetchOptions;
import ix.core.models.ETag;
import ix.core.plugins.SchedulerPlugin.ScheduledTask;
import ix.core.util.EntityUtils.EntityWrapper;

import play.mvc.Controller;

import play.Logger;

public class CoreJava8FactoryHelper {
	
	public static play.mvc.Result SchedulerFactoryGet(Long id, String select){
		        ObjectMapper mapper = SchedulerFactory.getEntityMapper ();
		        try{
		        return Optional.ofNullable(SchedulerFactory.get(id))
		                    .map(t->(ObjectNode)mapper.valueToTree(t))
		                    .map(t->SchedulerFactory.ok(t))
		                    .orElse(SchedulerFactory.notFound ("Bad request: "+SchedulerFactory.request().uri()));
		        }catch(Exception e){
		            e.printStackTrace();
		            throw e;
		        }
		        
	}
	
	
	public static play.mvc.Result SchedulerFactoryPage (int top, int skip, String filter, Function<List<ScheduledTask>,Object> map) {
	      //if (select != null) finder.select(select);
	        final FetchOptions options = new FetchOptions (top, skip, filter);
	        List<ScheduledTask> results = SchedulerFactory.splug.get()
	                            .getTasks()
	                            .stream()
	                            .sorted((t1,t2)->(int)(t1.id-t2.id))
	                            .collect(Collectors.toList());
	        
	        
	        final ETag etag = new ETag.Builder()
	                .fromRequest(SchedulerFactory.request())
	                .options(options)
	                .count(results.size())
	                .sha1OfRequest("filter")
	                .build();
	        

	        if (options.filter == null){
	            etag.total = SchedulerFactory.getCount ();
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
	        
	        ObjectMapper mapper = SchedulerFactory.getEntityMapper ();
	        ObjectNode obj = (ObjectNode)mapper.valueToTree(etag);

	        return SchedulerFactory.ok (obj);
	    }
}
