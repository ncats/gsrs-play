package ix.core.util;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.stream.Stream;

import ix.utils.ExecutionStack;

public class UniqueStack<K> implements ExecutionStack<K>{
	private LinkedList<K> list = new LinkedList<K>();
	private HashSet<K> set = new HashSet<K>();
	
	public boolean contains(K k){
		return set.contains(k);
	}
	
	public K pop(){
		K lr = list.pop();
		set.remove(lr);
		return lr;
	}
	
	public void push(K k){
		if(!this.contains(k)){
		set.add(k);
		list.add(k);
		}
	}

	@Override
	public void pushAndPopWith(K obj, Runnable r) {
		if(!this.contains(obj)){
			push(obj);
			try{
				r.run();
			}finally{
				pop();	
			}
		}
	}

	@Override
	public K getFirst() {
		return list.peek();
	}
	
	public Stream<K> asStream(){
		return list.stream();
	}
}