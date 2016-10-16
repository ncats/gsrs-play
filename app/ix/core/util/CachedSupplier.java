package ix.core.util;

import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;

/**
 * Memoized supplier. Caches the result of the supplier
 * to be used after. Useful for expensive calls.
 * 
 * @author peryeata
 * @param <T>
 */
public class CachedSupplier<T> implements Supplier<T>, Callable<T>{
	private static AtomicLong generatedVersion= new AtomicLong();
	
	/**
	 * Flag to signal all {{@link ix.core.util.CachedSupplier} instances
	 * to regenerate from their suppliers on the next call. 
	 */
	public static void resetCaches(){
		generatedVersion.incrementAndGet();
	}
	
	private Supplier<T> c;
	private T cache;
	private boolean run=false;
	private long generatedWithVersion; 
	
	public CachedSupplier(Supplier<T> c){
		this.c=c;
	}
	
	/**
	 * Delegates to {@link #get()}
	 */
	public T call(){
		return get();
	}
	
	@Override
	public synchronized T get() {
		if(run && generatedWithVersion==generatedVersion.get())return cache;
		generatedWithVersion=generatedVersion.get();
		cache=c.get();
		run=true;
		return cache;
	}
	
	/**
	 * Flag to signal this instance to recalculate from its
	 * supplier on next call.
	 */
	public void resetCache(){
		run=false;
	}
	
	public static <T> CachedSupplier<T> of(Supplier<T> supplier){
		return new CachedSupplier<T>(supplier);
	}
	public static <T> CachedSupplier<T> ofCallable(Callable<T> callable){
		return of(()->{
			try{
				return callable.call();
			}catch(Exception e){
				throw new IllegalStateException("Error calling callable in cached supplier");
			}
		});
	}
}