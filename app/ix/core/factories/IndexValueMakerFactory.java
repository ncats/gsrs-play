package ix.core.factories;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

import ix.core.FieldNameDecorator;
import ix.core.search.text.IndexValueMaker;
import ix.core.search.text.ReflectingIndexValueMaker;
import ix.core.util.EntityUtils;
import ix.core.util.EntityUtils.EntityInfo;
import play.Application;
import play.Logger;
import play.Play;

public class IndexValueMakerFactory extends AccumlatingInternalMapEntityResourceFactory<IndexValueMaker>{
	private static IndexValueMakerFactory _instance;
	
	public IndexValueMakerFactory(Application app) {
		super(app);
		_instance=this;
	}
	public static IndexValueMakerFactory getInstance(Application app){
		if(_instance!=null){
			return _instance;
		}else{
			return new IndexValueMakerFactory(app);
		}
	}
	
	@SuppressWarnings("unchecked")
	public static <T> IndexValueMaker<T> forClass(Class<T> c){
		return getInstance(Play.application()).getSingleResourceFor(c);
	}
	
	@Override
	public void initialize(Application app) {
		app.configuration()
		.getList("ix.core.indexValueMakers", new ArrayList<Object>())
		.stream()
		.map(o->(Map)o)
		.forEach(m->{
			try{
				EntityInfo<? extends IndexValueMaker> eiIndexer=
						(EntityInfo<? extends IndexValueMaker>)
						EntityUtils.getEntityInfoFor(m.get("indexer").toString());
				register(m.get("class").toString(), (IndexValueMaker<?>)eiIndexer.getInstance(), true);
			}catch(Exception e){
				Logger.error("Unable to register IndexValueMaker:" + e.getMessage(), e);
			}
		});
		
	}
	
	
	public static void init(){
		_instance=null;
		
	}
	
	@Override 
	public IndexValueMaker getDefaultResourceFor(EntityInfo emeta){
		return new ReflectingIndexValueMaker();
	}
	
	@Override
	public IndexValueMaker accumulate(IndexValueMaker t1, IndexValueMaker t2) {
		return t1.and(t2);
	}
}
