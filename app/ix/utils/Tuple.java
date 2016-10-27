package ix.utils;

import java.util.Map;
import java.util.Map.Entry;
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
	
	
	public static <K,V> Tuple<K,V> of(K k, V v){
		return new Tuple<K,V>(k,v);
		
	}
	
	public static <K,V,U> Function<Tuple<K,V>, Tuple<K,U>> vmap(Function<V,U> fun){
	    return (t)->{
	        return Tuple.of(t.k(),fun.apply(t.v()));
	    };
    }
	
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
	
	
}