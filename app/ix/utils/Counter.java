package ix.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class Counter<K> {
	Map<K,Integer> _counter = new HashMap<K,Integer>();
	
	public int add(K k){
		Integer in=get(k);
		in++;
		_counter.put(k, in);
		return in;
	}
	public int get(K k){
		Integer in=_counter.get(k);
		if(in==null){
			in=0;
		}
		return in;
	}
	public Set<K> getKeySet(){
		return _counter.keySet();
	}
}
