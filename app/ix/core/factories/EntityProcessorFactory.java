package ix.core.factories;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;

import ix.core.EntityProcessor;
import ix.core.processors.ReflectionEntityProcessor;
import ix.core.util.EntityUtils.EntityInfo;
import ix.utils.Tuple;
import play.Application;
import play.Logger;

public class EntityProcessorFactory extends AccumlatingInternalMapEntityResourceFactory<EntityProcessor>{

	private static EntityProcessorFactory _instance = null;

	/**
	 * Remove the instance returned by {@link #getInstance(Application)}
	 * this should only be used in tests.
	 */
	public static synchronized void clearInstance() {
		_instance=null;
	}

	private static class EntityProcessorConfig{
		String entityClassName;
		String processorClassName;
		Map with=null;
		
		private EntityProcessorConfig(String className, String processorName){
			this.entityClassName=className;
			this.processorClassName=processorName;
		}
		
		private EntityProcessorConfig(String className, String processorName, Map with){
			this.entityClassName=className;
			this.processorClassName=processorName;
			this.with=with;
		}
		private EntityProcessorConfig with(Map with){
			this.with=with;
			return this;
		}
		
		private EntityProcessor getEntityProcessor() throws Exception{
			Class<?> processorCls = Class.forName(processorClassName);
			if(with!=null){
				Constructor c=processorCls.getConstructor(Map.class);
				return (EntityProcessor) c.newInstance(with);
			}else{
				return (EntityProcessor) processorCls.newInstance();
			}
		}
	}
	
	public EntityProcessorFactory(Application app){
		super(app);
	}

	public synchronized  static EntityProcessorFactory getInstance(Application app){
		if(_instance!=null){
			return _instance;
		}


			if(_instance!=null){
				return _instance;
			}
			_instance = new EntityProcessorFactory(app);

			return _instance;

	}

	

	@Override
	public void initialize(Application app) {
		getStandardResourceStream(app,"ix.core.entityprocessors")
		.map(m->new EntityProcessorConfig((String)m.get("class"), (String)m.get("processor")).with((Map)m.get("with")))
		.map(epc->{
			try{
				EntityProcessor ep =epc.getEntityProcessor();
				return Tuple.of(epc.entityClassName, ep);
			}catch(Exception e){
				e.printStackTrace();
				Logger.warn("Unable to make processor:" + e.getMessage());
			}
			return null;
		})
		.filter(Objects::nonNull)
		.forEach(t->{
			try {
				register(t.k(), t.v(),true);
			} catch (Exception e) {
				Logger.warn("Unable to register processor:" + e.getMessage());
			}
		});
	}
	
	@Override 
	public EntityProcessor getDefaultResourceFor(EntityInfo<?> ei){
		return new ReflectionEntityProcessor(ei);
	}
	
	@Override
	public EntityProcessor accumulate(EntityProcessor t1, EntityProcessor t2) {
		return t1.combine(t2);
	}
}
