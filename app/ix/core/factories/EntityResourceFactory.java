package ix.core.factories;

import java.util.List;
import java.util.Set;

import ix.core.util.EntityUtils;
import ix.core.util.EntityUtils.EntityInfo;
import ix.core.util.EntityUtils.EntityWrapper;


public interface EntityResourceFactory<T> {
	
	Set<T> getRegisteredResourcesFor(EntityInfo<?> emeta);
	
	default Set<T> getRegisteredResourcesFor(Class<?> cls){
		return getRegisteredResourcesFor(EntityUtils.getEntityInfoFor(cls));
	}
	
	default Set<T> getRegisteredResourcesFor(String clsName) throws ClassNotFoundException{
		return getRegisteredResourcesFor(EntityUtils.getEntityInfoFor(clsName));
	}
	
	default Set<T> getRegisteredResourcesFor(EntityWrapper<?> ew) throws ClassNotFoundException{
		return getRegisteredResourcesFor(ew.getEntityInfo());
	}
	
	public <V extends T> void register(EntityInfo<?> emeta, V resource);
	
	default <V extends T> void register(EntityInfo<?> emeta, V resource, boolean descendents){
		if(descendents){
			emeta.getTypeAndSubTypes().stream().forEach(ei->{
				register(ei,resource);
			});
		}else{
			register(emeta,resource);
		}
	}
	
	default <V extends T>  void register(Class<?> cls, V resource, boolean descendents){
		register(EntityUtils.getEntityInfoFor(cls), resource, descendents);
	}
	
	default <V extends T>  void register(String clsName, V resource, boolean descendents) throws ClassNotFoundException{
		register(EntityUtils.getEntityInfoFor(clsName), resource, descendents);
	}
	
	
	/**
	 * Check if any of the registered resources for this class are an
	 * instance of the given class. Returns true if there are any matches.
	 * @param emeta The wrapped Entity class
	 * @param instClass The class for the resource
	 * @return
	 */
	default <V extends T> boolean isRegisteredFor(EntityInfo<?> emeta, Class<V> instClass){
		return this.getRegisteredResourcesFor(emeta)
							.stream()
							.anyMatch(v->instClass.isInstance(v));
	}
	
	
}
