package ix.core.factories;

import ix.core.util.EntityUtils;
import ix.core.util.EntityUtils.EntityInfo;

public interface SingleEntityResourceFactory<T>{
	public T getSingleResourceFor(EntityInfo<?> ei);
	default T getSingleResourceFor(Class<?> cls){
		return getSingleResourceFor(EntityUtils.getEntityInfoFor(cls));
	}
}