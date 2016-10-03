package ix.core.search.text;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import ix.core.util.EntityUtils;
import ix.core.util.EntityUtils.EntityInfo;
import play.Logger;
import play.Play;

public class IndexValueMakerFactory {
	public static boolean initialized=false;
	
	static ConcurrentHashMap<String,IndexValueMaker> registry = new ConcurrentHashMap<>();
	static Map<String,List<IndexValueMaker>> componentRegistry = new ConcurrentHashMap<>();
	
	@SuppressWarnings("unchecked")
	public static <T> IndexValueMaker<T> forClass(Class<T> c){
		if(!initialized)init();
		return registry.computeIfAbsent(c.getName(), k->{
			IndexValueMaker<T> ivdef= new ReflectingIndexValueMaker<T>();
			Optional<IndexValueMaker> oivm=componentRegistry.getOrDefault(k, new ArrayList<IndexValueMaker>())
							 .stream()
							 .reduce((iv1,iv2)->iv1.and(iv2));
			if(oivm.isPresent()){
				return ivdef.and(oivm.get());
			}else{
				return ivdef;
			}
		});
	}
	
	public static boolean isRegisteredFor(Class<?> cls, Class<? extends IndexValueMaker> clsIvm){
		return componentRegistry
			.get(cls.getName())
			.stream()
			.anyMatch(iv->iv.getClass().equals(clsIvm));
	}
	
	public static List<IndexValueMaker> getIndexValueMakersForClass(Class<?> cls){
		return componentRegistry
				.getOrDefault(cls.getName(), new ArrayList<IndexValueMaker>());
		
	}
	
	
	@SuppressWarnings("unchecked")
	public static void init(){
		registry.clear();
		componentRegistry.clear();
		initialized=true;
		Play.application()
		.configuration()
		.getList("ix.core.indexValueMakers", new ArrayList<Object>())
		.stream()
		.map(o->(Map)o)
		.forEach(m->{
			try{
				EntityInfo<?> eiClass=EntityUtils.getEntityInfoFor(m.get("class").toString());
				EntityInfo<? extends IndexValueMaker> eiIndexer=
						(EntityInfo<? extends IndexValueMaker>)
						EntityUtils.getEntityInfoFor(m.get("indexer").toString());
				registerIndexer(eiClass.getClazz(),eiIndexer.getInstance());
			}catch(Exception e){
				e.printStackTrace();
			}
		});
	}
	
	public static <T> void registerIndexer(Class<T> cls, IndexValueMaker<T> ivm){
		EntityInfo<T> eiClass=EntityUtils.getEntityInfoFor(cls);
		eiClass.getTypeAndSubTypes()
			.stream()
			.forEach(ei->{
				Logger.info("Registering for:" + ei.getName() +" :" + ivm.getClass());
				componentRegistry
					.computeIfAbsent(ei.getName(),k-> new ArrayList<IndexValueMaker>())
					.add(ivm);
			});
	}
}
