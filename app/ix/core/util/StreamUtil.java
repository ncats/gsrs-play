package ix.core.util;

import java.util.Enumeration;
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
				cacheNext();
				return n.get();
			}
			
			public synchronized void cacheNext() {
				next=sup.get();
			}
			
			private void initialize(){
				cacheNext();
				initialized=true;
			}
		};
		return ofIterator(ir);
	}
	
	
	public static <T> Stream<T> forNullableGenerator(Supplier<T> sup){
		return forGenerator(()->{
			return Optional.ofNullable(sup.get());
		});
	}
	
	public static <T> Stream<T> forEnumeration(Enumeration<T> enumeration){
		return forNullableGenerator(()->{
			return (enumeration.hasMoreElements())?enumeration.nextElement():null;
		});
	}
}
