package ix.utils;

import java.util.Optional;
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
	
	@Override
	public Optional<K> getOptionalFirst() {
		Optional<LiteralReference<K>> ret =internalStack.getOptionalFirst();
		if(ret.isPresent()){
			return Optional.of(ret.get().get());
		}
		return Optional.empty();
	}

	@Override
	public void setMaxDepth(Integer maxDepth) {
		internalStack.setMaxDepth(maxDepth);
	}

	public Stream<K> asStream(){
		return internalStack.asStream().map(l->l.get());
	}
	
	
}