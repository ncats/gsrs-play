package ix.core.util;

import java.util.function.Supplier;

/**
 * Memoized supplier. Caches the result of the supplier
 * to be used after. Useful for expensive calls.
 * 
 * @author peryeata
 * @param <K>
 */
public class CachedSupplier<K> implements Supplier<K>{
	Supplier<K> c;
	K cache;
	boolean run=false;
	
	public CachedSupplier(Supplier<K> c){
		this.c=c;
	}
	
	public K call(){
		return get();
	}
	
	@Override
	public K get() {
		if(run)return cache;
		cache=c.get();
		run=true;
		return cache;
	}
	
	public static <K> CachedSupplier<K> of(Supplier<K> c){
		return new CachedSupplier<K>(c);
	}
}