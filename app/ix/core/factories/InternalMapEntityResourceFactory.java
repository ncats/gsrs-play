package ix.core.factories;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

import ix.core.util.EntityUtils.EntityInfo;
import ix.utils.Util;
import play.Application;

public abstract class InternalMapEntityResourceFactory<T> implements EntityResourceFactory<T>{
	
	private final Map<String, Set<T>> internalMap;
	
	public InternalMapEntityResourceFactory(Application app){
		internalMap = new ConcurrentHashMap<>();
		initialize(app);
	}
	
	public abstract void initialize(Application app);
	
	@Override
	public Set<T> getRegisteredResourcesFor(EntityInfo<?> emeta) {
		return internalMap.computeIfAbsent(emeta.getName(), k-> getDefaultListFor(emeta));
	}

	@Override
	public <V extends T> void register(EntityInfo<?> emeta, V resource) {
		internalMap.computeIfAbsent(emeta.getName(), k->getDefaultListFor(emeta)).add(resource);
	}
	
	public Set<T> getDefaultListFor(EntityInfo<?> emeta){
		Set<T> tlist= new LinkedHashSet<>();
		T def=getDefaultResourceFor(emeta);
		if(def!=null){
			tlist.add(def);
		}
		return tlist;
	}
	
	public T getDefaultResourceFor(EntityInfo<?> emeta){
		return null;
	}
	
	
	public static Stream<Map> getStandardResourceStream(Application app, String path){
		return app.configuration()
		.getList(path,new ArrayList<Object>())
		.stream()
		.filter(Objects::nonNull)
		.filter(o->o instanceof Map)
		.map(o->(Map)o);
	}
	

}
