package ix.core.util;

import java.util.concurrent.*;
import java.util.function.Function;

/**
 * Created by katzelda on 9/30/16.
 */
public class Memoizer<A, V>  {

    private final ConcurrentMap<A, Future<V>> cache = new ConcurrentHashMap<>();

    public V computeIfAbsent(A arg, Function<A, V> computation) throws InterruptedException {

        while(true){

            Future<V> f = cache.get(arg);
            if(f == null) {
                FutureTask<V> ft = new FutureTask<>(() -> computation.apply(arg));
                //this is a double check just in case another thread
                //happened to put it in...
                f = cache.putIfAbsent(arg, ft);
                if (f == null) {
                    //was put because it was absent
                    f = ft;
                    ft.run(); // run in current thread?
                }
            }
            try{
                return f.get();
            }catch(CancellationException e){
                cache.remove(arg, f);
            }catch(ExecutionException e){
                throw new IllegalStateException(e);
            }
        }
    }

    public void clear() {
        cache.clear();
    }
}
