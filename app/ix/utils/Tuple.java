package ix.utils;

import java.util.function.BiFunction;

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
	public static<K,V> BiFunction<K,V,Tuple<K,V>> map(){
		return (k,v)->{
			return new Tuple<K,V>(k,v);
		};
	}
	
	
	
}