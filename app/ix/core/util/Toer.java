package ix.core.util;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import ix.core.util.Conditional.InstantiatedConditional;

/**
 * This is a very simple interface that simply
 * adds a {@link Toer#_to(Function)} method to a class, 
 * which will take the source object and map it using a 
 * provided mapping function. The main purpose is simply
 * to allow for a coding style which is uses
 * functional method-chaining constructions
 * rather than an iterative approach.
 * 
 * For example, you may have a Builder pattern
 * where you only want to set a property based
 * on some external condition. A typical Builder
 * solution might look like this:
 * 
 * <pre>
 * Builder b = new MyType.Builder()
 *                       .property1("Some value")
 *                       .property2("Some other value");
 * if(criteria){
 *    b=b.someSetting();
 * }
 * 
 * MyType t = b.build();
 * </pre>
 * 
 * If the Builder here implements {@link Toer} then this
 * can be written like this:
 * <pre>
 * MyType t = new MyType.Builder()
 *                       .property1("Some value")
 *                       .property2("Some other value")
 *                       ._to(mb->(criteria)?mb.someSetting():mb)
 *                       .build();
 * </pre>
 * 
 * This allows a builder pattern not to be interrupted by a new condition
 * not directly anticipated in the builder chain.
 * 
 * @author peryeata
 *
 * @param <T>
 */
public interface Toer<T> {
    
    /**
     * Map this value to a new value using the supplied
     * {@link Function}. This is often used in Builder
     * patterns.
     * 
     * @param map
     * @return
     */
    public default <U> U _to(Function<T, U> map){
        return map.apply((T)this);
    }
    
    
    /**
     * Creates a stream of this object.
     * @return
     */
    public default Stream<T> _stream(){
        return Stream.of((T)this);
    }
    
    /**
     * Performs an operation using this object
     * @return
     */
    public default T _do(Consumer<T> c){
        T t= (T)this;
        c.accept(t);
        return t;
    }
    
    /**
     * Performs an operation using this object,
     * only if a predicate is matched
     * @return
     */
    public default T _doIf(Predicate<T> pred, Consumer<T> c){
        T t= (T)this;
        if(pred.test(t)){
            c.accept(t);
        }
        return t;
    }
    
    /**
     * Performs a conditional operation
     * on this object.
     * @return
     */
    public default InstantiatedConditional<T> _if(Predicate<T> pred){
        T t= (T)this;
        return Conditional.of(pred).with(t);
    }
    
}