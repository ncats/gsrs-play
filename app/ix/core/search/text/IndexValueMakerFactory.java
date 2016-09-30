package ix.core.search.text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import ix.core.util.EntityUtils;
import ix.core.util.EntityUtils.EntityInfo;
import play.Play;

public class IndexValueMakerFactory {
	public static boolean initialized=false;
	
	static Map<String,IndexValueMaker> registry = new HashMap<>();
	
	public static <T> IndexValueMaker<T> forClass(Class<T> c){
		if(!initialized)initialize();
		return registry.computeIfAbsent(c.getName(), k->{
			return new ReflectingIndexValueMaker<T>();
		});
	}
	
	
	public static void initialize(){
		registry.clear();
		initialized=true;
		Play.application()
		.configuration()
		.getList("ix.core.indexValueMakers", new ArrayList<Object>())
		.stream()
		.map(o->(Map)o)
		.forEach(m->{
			try{
				EntityInfo<?> eiClass=EntityUtils.getEntityInfoFor(m.get("class").toString());
				EntityInfo<? extends IndexValueMaker> eiIndexer=(EntityInfo<? extends IndexValueMaker>)EntityUtils.getEntityInfoFor(m.get("indexer").toString());
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
			System.out.println("Registering for:" + ei.getName() +" :" + ivm.getClass());
			IndexValueMaker<T> ivnew=registry.computeIfAbsent(ei.getName(),k-> new ReflectingIndexValueMaker<T>()).and(ivm);
			registry.put(ei.getName(),ivnew);
		});
	}
}
