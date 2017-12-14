package ix.core.controllers;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
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
    
    static final CachedSupplier<SchedulerPlugin> splug = CachedSupplier.of(new Supplier<SchedulerPlugin>(){

		@Override
		public SchedulerPlugin get() {
			 return Play.application().plugin(SchedulerPlugin.class);
		}
    	
    	
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
        return page(top,skip,filter, new Function<List<ScheduledTask>,Object>(){

			@Override
			public Object apply(List<ScheduledTask> t) {
				return t;
			}
        	
        });
    }
    
    public static Result page (int top, int skip, String filter, Function<List<ScheduledTask>,Object> map) {
      return CoreJava8FactoryHelper.SchedulerFactoryPage(top, skip, filter, map);
    		  
    }

    public static Result get (Long id, String select) {
       return CoreJava8FactoryHelper.SchedulerFactoryGet(id, select);
        
    }
    
    public static ScheduledTask get(long id){
        return splug.get().getTasks()
                .stream()
                .filter(new Predicate<ScheduledTask>(){

					@Override
					public boolean test(ScheduledTask t) {
						return t.id.equals(id);
					}
                	
                })
                .findAny()
                .orElse(null);
    }

    
    public static Result field(Long id, String path) {
        return field(get(id), path);
    }
    
    public static Result stream(String field, int top, int skip){
        PojoPointer pojoPoint = PojoPointer.fromURIPath(field);
        return page(top, skip, null, new Function<List<SchedulerPlugin.ScheduledTask>,Object>(){

			@Override
			public Object apply(List<ScheduledTask> t) {
				
				return EntityWrapper.of(t)
						.at(pojoPoint)
		                .get()
		                .getValue();
			}
        
        });
        
                
    }

}