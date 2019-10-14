package ix.core.util;

import java.util.function.Consumer;
import java.util.function.Supplier;


public class ListeningCachedSupplier<T> extends CachedSupplier<T> {

    Consumer<T> lis;

    public ListeningCachedSupplier(Supplier<T> c,Consumer<T> whenFirstCalled) {
        super(c);
        this.lis=whenFirstCalled;
    }

    @Override
    public T get() {
        if(hasRun()) {
            return super.get();
        }else{
            synchronized(this){
                if(hasRun()){
                    return super.get();
                }
                T tt=directCall();
                setCacheDirect(tt);
                lis.accept(tt);
                return super.get();
            }
        }
    }
    public static <T> ListeningCachedSupplier<T> of(Supplier<T> supplier,Consumer<T> whenFirstCalled){
        return new ListeningCachedSupplier<T>(supplier,whenFirstCalled);
    }
}
