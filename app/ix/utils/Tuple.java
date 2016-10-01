package ix.utils;

import java.util.function.BiFunction;

public class Tuple<K,V>{
	K k;
	V v;
	public Tuple(K k,V v){
		this.k=k;
		this.v=v;
	}
	public K k(){
		return k;
	}
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