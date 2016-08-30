package ix.core.util;

import java.util.concurrent.Callable;

/**
 * Deferred and cached calculation of a value. This is useful for when
 * there is an expensive calculation which may or may not need to be 
 * calculated during some execution flow, but the flow of the processing
 * would suffer from determining when it is needed or not.
 * 
 * @author peryeata
 *
 * @param <K>
 */
public class CachedCallable<K> implements Callable<K>{
	Callable<K> c;
	K cache;
	public CachedCallable(Callable<K> c){
		this.c=c;
	}
	public K call(){
		if(cache!=null)return cache;
		try{
			return (cache=c.call());
		}catch(Exception e){
			return null;
		}
	}
	
	public static <K> CachedCallable<K> of(Callable<K> c){
		return new CachedCallable<K>(c);
	}
}