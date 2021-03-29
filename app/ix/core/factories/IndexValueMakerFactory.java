package ix.core.factories;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import ix.core.search.text.IndexValueMaker;
import ix.core.search.text.ReflectingIndexValueMaker;
import ix.core.util.EntityUtils;
import ix.core.util.EntityUtils.EntityInfo;
import ix.core.util.IOUtil;
import play.Application;
import play.Logger;

import java.util.Collections;
import java.util.Map;

public class IndexValueMakerFactory extends AccumlatingInternalMapEntityResourceFactory<IndexValueMaker>{

	public static class IndexValueMakerConfig{
		public Class indexer;
		@JsonProperty("class")
		public Class entityType;

		/**
		 * Additional parameters to initialize in your instance returned
		 * these will be used by Jackson to call the corresponding setters
		 * using java beans method naming conventions.
		 */
		private Map<String, Object> parameters;

		public Map<String, Object> getParameters() {
			return parameters;
		}

		public void setParameters(Map<String, Object> parameters) {
			this.parameters = parameters;
		}
	}
	private static ThreadLocal<ObjectMapper> mapper = new ThreadLocal<ObjectMapper>(){
		@Override
		protected ObjectMapper initialValue() {
			ObjectMapper mapper= new ObjectMapper();
			TypeFactory tf = TypeFactory.defaultInstance()
					.withClassLoader(IOUtil.getGinasClassLoader());
			mapper.setTypeFactory(tf);

			return mapper;

		}
	};
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
				ObjectMapper mapperInstnce = mapper.get();
				IndexValueMakerConfig conf = mapperInstnce.convertValue(m, IndexValueMakerConfig.class);
				IndexValueMaker maker;
				if(conf.parameters ==null){
					maker = (IndexValueMaker) mapperInstnce.convertValue(Collections.emptyMap(), conf.indexer);
				}else{
					maker = (IndexValueMaker) mapperInstnce.convertValue(conf.parameters, conf.indexer);
				}
				register(conf.entityType, maker, true);
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
