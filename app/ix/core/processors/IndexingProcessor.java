package ix.core.processors;

import ix.core.EntityProcessor;
import ix.core.adapters.EntityPersistAdapter;
import ix.core.java8Util.Java8ForOldEbeanHelper;
import ix.core.util.EntityUtils.EntityWrapper;

public class IndexingProcessor<T> implements EntityProcessor<T>{
	private static IndexingProcessor _instance;

	
	
	@Override
	public void postPersist(T bean) throws ix.core.EntityProcessor.FailProcessingException {
		try {
			Java8ForOldEbeanHelper.makeIndexOnBean(EntityPersistAdapter.getInstance(),EntityWrapper.of(bean));
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}

	@Override
	public void postRemove(T obj) throws ix.core.EntityProcessor.FailProcessingException {
		try {
			Java8ForOldEbeanHelper.deleteIndexOnBean(EntityPersistAdapter.getInstance(),EntityWrapper.of(obj));
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}

	@Override
	public void postUpdate(T obj) throws ix.core.EntityProcessor.FailProcessingException {
		postRemove(obj);
		postPersist(obj);
	}

	
	public synchronized static IndexingProcessor getInstance(){
		if(_instance==null){
			_instance = new IndexingProcessor();
		}
		return _instance;
	}
}
