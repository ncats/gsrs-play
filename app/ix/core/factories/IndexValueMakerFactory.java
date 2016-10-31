package ix.core.factories;

import ix.core.search.text.IndexValueMaker;
import ix.core.search.text.ReflectingIndexValueMaker;
import ix.core.util.EntityUtils;
import ix.core.util.EntityUtils.EntityInfo;
import play.Application;
import play.Logger;

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
	
	@Override
	public void initialize(Application app) {
		getStandardResourceStream(app,"ix.core.indexValueMakers")
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
	
	
	@Override 
	public IndexValueMaker getDefaultResourceFor(EntityInfo emeta){
		return new ReflectingIndexValueMaker();
	}
	
	@Override
	public IndexValueMaker accumulate(IndexValueMaker t1, IndexValueMaker t2) {
		return t1.and(t2);
	}
}
