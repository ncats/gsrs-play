package ix.core.util;

import java.util.concurrent.Callable;
import java.util.function.Supplier;

/**
 * Memoized supplier. Caches the result of the supplier
 * to be used after. Useful for expensive calls.
 * 
 * @author peryeata
 * @param <T>
 */
public class CachedSupplier<T> implements Supplier<T>, Callable<T>{
	Supplier<T> c;
	T cache;
	boolean run=false;
	
	public CachedSupplier(Supplier<T> c){
		this.c=c;
	}
	
	public T call(){
		return get();
	}
	
	@Override
	public synchronized T get() {
		if(run)return cache;
		cache=c.get();
		run=true;
		return cache;
	}
	
	public static <K> CachedSupplier<K> of(Supplier<K> supplier){
		return new CachedSupplier<K>(supplier);
	}
}