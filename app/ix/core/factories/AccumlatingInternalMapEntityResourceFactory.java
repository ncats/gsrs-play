package ix.core.factories;

import ix.core.util.EntityUtils.EntityInfo;
import play.Application;

public abstract class AccumlatingInternalMapEntityResourceFactory<T> extends InternalMapEntityResourceFactory<T> implements SingleEntityResourceFactory<T>{
	public AccumlatingInternalMapEntityResourceFactory(Application app) {
		super(app);
	}

	//Doesn't cache
	@Override
	public T getSingleResourceFor(EntityInfo<?> ei) {
		return this.getRegisteredResourcesFor(ei)
			.stream()
			.reduce(this::accumulate)
			.get();
	}
	
	public abstract T accumulate(T t1, T t2);

	

}
