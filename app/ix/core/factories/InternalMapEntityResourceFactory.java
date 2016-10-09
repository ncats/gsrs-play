package ix.core.factories;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import ix.core.util.EntityUtils.EntityInfo;
import play.Application;

public abstract class InternalMapEntityResourceFactory<T> implements EntityResourceFactory<T>{
	
	Map<String, List<T>> internalMap = new ConcurrentHashMap<>();
	
	public InternalMapEntityResourceFactory(Application app){
		initialize(app);
	}
	
	public abstract void initialize(Application app);
	
	@Override
	public List<T> getRegisteredResourcesFor(EntityInfo<?> emeta) {
		return internalMap.computeIfAbsent(emeta.getName(), k-> getDefaultListFor(emeta));
	}

	@Override
	public <V extends T> void register(EntityInfo<?> emeta, V resource) {
		internalMap.computeIfAbsent(emeta.getName(), k->getDefaultListFor(emeta))
					.add(resource);
	}
	
	public List<T> getDefaultListFor(EntityInfo<?> emeta){
		ArrayList<T> tlist= new ArrayList<T>();
		T def=getDefaultResourceFor(emeta);
		if(def!=null){
			tlist.add(def);
		}
		return tlist;
	}
	
	public T getDefaultResourceFor(EntityInfo<?> emeta){
		return null;
	}
	
	

}
