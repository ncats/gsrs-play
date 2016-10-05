package ix.utils;

import java.util.stream.Stream;

import ix.core.util.UniqueStack;

public class LinkedReferenceSet<K> implements ExecutionStack<K>{
	UniqueStack<LiteralReference<K>> internalStack = new UniqueStack<LiteralReference<K>>();
	
	
	public boolean contains(K k){
		return internalStack.contains(LiteralReference.of(k));
	}
	
	
	@Override
	public void pushAndPopWith(K obj, Runnable r) {
		internalStack.pushAndPopWith(LiteralReference.of(obj), r);
	}

	@Override
	public K getFirst() {
		return internalStack.getFirst().get();
	}
	
	public Stream<K> asStream(){
		return internalStack.asStream().map(l->l.get());
	}
	
	
}