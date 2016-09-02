package ix.utils;

import java.util.HashSet;
import java.util.LinkedList;

public class LinkedReferenceSet<K> implements ExecutionStack<K>{
	LinkedList<LiteralReference<K>> list = new LinkedList<LiteralReference<K>>();
	HashSet<LiteralReference<K>> set = new HashSet<LiteralReference<K>>();
	
	public boolean contains(K k){
		return set.contains(k);
	}
	
	private K pop(){
		LiteralReference<K> lr = list.pop();
		set.remove(lr);
		return lr.o;
	}
	private void push(K k){
		if(!contains(k)){
			LiteralReference<K> lr = new LiteralReference<K>(k);
			list.add(lr);
		}
	}

	@Override
	public void pushAndPopWith(K obj, Runnable r) {
		push(obj);
		try{
			r.run();
		}finally{
			pop();	
		}
	}

	@Override
	public K getFirst() {
		return list.peek().o;
	}
}