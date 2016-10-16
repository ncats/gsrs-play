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
		CachedSupplier.generatedVersion.incrementAndGet();
	}

	private final Supplier<T> c;
	private T cache;
	private boolean run=false;
	private long generatedWithVersion;

	public CachedSupplier(final Supplier<T> c){
		this.c=c;
	}

	/**
	 * Delegates to {@link #get()}
	 */
	@Override
	public T call() throws Exception{
		return get();
	}

	@Override
	public T get() {
		if(this.run && this.generatedWithVersion==CachedSupplier.generatedVersion.get()) {
			return this.cache;
		}
		this.generatedWithVersion=CachedSupplier.generatedVersion.get();
		this.cache=this.c.get();
		this.run=true;
		return this.cache;
	}


	public synchronized T getSync() {
		return get();
	}



	/**
	 * Flag to signal this instance to recalculate from its
	 * supplier on next call.
	 */
	public void resetCache(){
		this.run=false;
	}

	public static <T> CachedSupplier<T> of(final Supplier<T> supplier){
		return new CachedSupplier<T>(supplier);
	}

	/**
	 * Wrap the provided callable as a cached supplier
	 * @param callable
	 * @return
	 */
	public static <T> CachedSupplier<T> ofCallable(final Callable<T> callable){
		return of(()->{
			try{
				return callable.call();
			}catch(final Exception e){
				throw new IllegalStateException(e);
			}
		});
	}
}