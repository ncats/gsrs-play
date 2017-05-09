package ix.utils;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Tuple<K,V>{
	private K k;
	private V v;
	public Tuple(K k,V v){
		this.k=k;
		this.v=v;
	}
	
	@JsonProperty("key")
	public K k(){
		return k;
	}
	
	@JsonProperty("value")
	public V v(){
		return v;
	}
	
	
	public void consume(ThrowableBiConsumer<K,V> c){
	    c.accept(k, v);
	}
	
	public <T> T map(BiFunction<K,V,T> c){
        return c.apply(k, v);
    }
	
	
	public static <K,V> Tuple<K,V> of(K k, V v){
		return new Tuple<K,V>(k,v);
		
	}
	
	/**
	 * Maps the "value" (2nd element) of a Tuple to a new value,
	 * while keeping the "key" (1st element) in tact. 
	 * @param fun
	 * @return
	 */
	public static <K,V,U> Function<Tuple<K,V>, Tuple<K,U>> vmap(Function<V,U> fun){
	    return (t)->{
	        return Tuple.of(t.k(),fun.apply(t.v()));
	    };
    }
	
	
	/**
     * Maps the "key" (1st element) of a Tuple to a new value,
     * while keeping the "value" (2nd element) in tact. 
     * @param fun
     * @return
     */
	public static <K,V,L> Function<Tuple<K,V>, Tuple<L,V>> kmap(Function<K,L> fun){
        return (t)->{
            return Tuple.of(fun.apply(t.k()),t.v());
        };
    }
	
	
	
	/**
	 * Used to make a conversion from an {@link Entry} to
	 * a tuple.
	 * 
	 * @param ent
	 * @return
	 */
	public static <K,V> Tuple<K,V> of(Entry<K,V> ent){
		return Tuple.of(ent.getKey(),ent.getValue());
		
	}
	
	public static<K,V> BiFunction<K,V,Tuple<K,V>> map(){
		return (k,v)->{
			return new Tuple<K,V>(k,v);
		};
	}
	
	/**
	 * Collector to go from a stream of tuples to a map
	 * of the internal keys and values
	 * @return
	 */
	public static <T,U> Collector<Tuple<T,U>,?,Map<T,U>> toMap(){
    	return Collectors.toMap(e->e.k(), e->e.v());
    }
	
	
	@Override
	public int hashCode(){
	    return this.k.hashCode() ^ (this.v.hashCode() ^ 0xDEADBEEF);
	}
	
	@Override
	public boolean equals(Object o){
	    if(o == this)return true;
	    if(!(o instanceof Tuple)){
	       return false; 
	    }

	    Tuple<K,V> tup2 = (Tuple<K,V>)o;
	    if(!(tup2.k.equals(this.k))){
	        return false;
	    }
	    if(!(tup2.v.equals(this.v))){
            return false;
        }
	    return true;
	    
	}
	
	public static <K,V> Set<Tuple<K,V>> toTupleSet(Map<K,V> map){
	    return map.entrySet().stream().map(Tuple::of).collect(Collectors.toSet());
	}
	
	@Override
	public String toString(){
	    return "<" + this.k.toString() + "," + this.v.toString() + ">";
	}
	
	public static interface ThrowableBiConsumer<K,V> extends BiConsumer<K,V>{
	    public void throwing(K k, V v) throws Exception;
	    
	    public default void accept(K k, V v){
            try {
                this.throwing(k, v);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
	}
	
}