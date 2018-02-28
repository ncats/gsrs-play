package ix.ginas.exporters;

import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentHashMap;


public class Memoizer {

    private final ConcurrentMap cache = new ConcurrentHashMap();

    /*
    public V computeIfAbsent(A arg, Function computation) throws InterruptedException {

        while(true){

            Future f = cache.get(arg);
            if(f == null) {
                FutureTask ft = new FutureTask(() -> computation.apply(arg));
                //this is a double check just in case another thread
                // happened to put it in...
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
    */

    public void clear() {
        cache.clear();
    }
}
