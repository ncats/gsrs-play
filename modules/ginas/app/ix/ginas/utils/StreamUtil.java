package ix.ginas.utils;

import java.util.Iterator;
import java.util.Optional;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Supplier;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class StreamUtil {
	public static <T> Stream<T> ofIterator(Iterator<T> it){
		return StreamSupport.stream(Spliterators.spliteratorUnknownSize(it, Spliterator.IMMUTABLE), false);
	}
	
	public static <T> Stream<T> forGenerator(Supplier<Optional<T>> sup){
		Iterator<T> ir=new Iterator<T>(){
			public Optional<T> next;
			public boolean initialized=false;
			
			@Override
			public synchronized boolean hasNext() {
				if(!initialized)initialize();
				return next.isPresent();
			}

			@Override
			public synchronized T next() {
				if(!initialized)initialize();
				Optional<T> n=next;
				next=sup.get();
				return n.get();
			}
			
			private void initialize(){
				next=Optional.empty();
				next();
				initialized=true;
			}
		};
		return ofIterator(ir);
	}
}
