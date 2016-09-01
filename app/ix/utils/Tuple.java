package ix.utils;

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
	
}