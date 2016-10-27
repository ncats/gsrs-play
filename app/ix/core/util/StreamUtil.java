package ix.core.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Optional;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;


public class StreamUtil {
	public static <T> Stream<T> forIterator(Iterator<T> it){
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
		return forIterator(ir);
	}
	
	
	public static <T> Stream<T> forNullableGenerator(Supplier<T> sup){
		return forGenerator(()->{
			return Optional.ofNullable(sup.get());
		});
	}
	
	public static <T> Stream<T> forIterable(Iterable<T> sup){
        return forIterator(sup.iterator());
    }
	
	private static <K,T> Stream<T> forNullableGenerator(final K k, Function<K,T> sup){
	    return forNullableGenerator(()->sup.apply(k));
    }
	
	public static <K> StreamGenerator<K> from(K k){
	    return new StreamGenerator<K>(k);
	}
	
	
	public static class StreamGenerator<K>{
	    K k;
	    private StreamGenerator(K k){
	        this.k=k;
	    }
	    
	    public <T> Stream<T> streamNullable(ThrowableFunction<K,T> next){
	        return forNullableGenerator(k, next);
	    }
	}
	
	public static <T> StreamConcatter<T> with(Stream<T> s){
	    return new StreamConcatter<T>().and(s);
	}
	
	
	public static class StreamConcatter<T>{
	    Stream<T> s= Stream.empty();
	    private StreamConcatter(){}
	    
	    public StreamConcatter<T> and(Stream<T> newstream){
	        s=Stream.concat(s, newstream);
	        return this;
	    }
	    public Stream<T> stream(){
	        return this.s;
	        
	    }
	    
	}
	
	public static interface ThrowableFunction<T,V> extends Function<T,V>{

        @Override
	    default V apply(T t){
            try {
                return applyThrowable(t);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
	    
	    public V applyThrowable(T t) throws Exception;
	}
	
	public static <T> Stream<T> forEnumeration(Enumeration<T> enumeration){
		return forNullableGenerator(()->{
			return (enumeration.hasMoreElements())?enumeration.nextElement():null;
		});
	}
	
	

    public static <T> Collector<T,?,Stream<T>> maxElements(int n, Comparator<T> comp){
        return new ReducedCollector<T>(n,comp);
    }
    
    public static class ReducedCollector<T> implements Collector<T,TopNReducer<T>,Stream<T>>{
        private final Comparator<T> comp;
        private final int max;
        public ReducedCollector(int max,Comparator<T> comp){
            this.comp=comp;
            this.max=max;
        }
        
        @Override
        public Supplier<TopNReducer<T>> supplier() {
            return (()->new TopNReducer<T>(max,comp));
        }

        @Override
        public BiConsumer<TopNReducer<T>, T> accumulator() {
            
            return (red,t)->red.add(t);
        }

        @Override
        public BinaryOperator<TopNReducer<T>> combiner() {
            return (a,b)->{
                b.get().forEach(t->{
                    a.add(t);
                });
                return a;
            };
        }

        @Override
        public Function<TopNReducer<T>, Stream<T>> finisher() {
            return (red)->red.get();
        }

        @Override
        public Set<java.util.stream.Collector.Characteristics> characteristics() {
            return new HashSet<>();
        }

        
    }
    
    private static class TopNReducer<T> {
        private final PriorityQueue<T> pq;
        private final Comparator<T> comp;
        private final int cap;
        private final int effcap;
        private int _buff=0;
        
        
        public TopNReducer(int n, Comparator<T> comp) {
            this.comp=comp;
            pq = new PriorityQueue<T>(n, (a, b) -> {
                return -comp.compare(a, b);
            });
            cap = n;
            effcap = cap * 1;
        }
        
        public void add(T t) {
            pq.add(t);
            _buff++;
            if (_buff > effcap) {
                int r= _buff-cap;
                for(int i=0;i<r;i++){
                    pq.remove();
                }
                _buff=cap;
            }
        }

        public Stream<T> get() {
            return pq.stream()
                    .sorted(comp)
                    .limit(cap);
        }

    }
}
