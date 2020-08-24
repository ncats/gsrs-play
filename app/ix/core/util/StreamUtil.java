package ix.core.util;

import java.io.Closeable;
import java.util.Arrays;
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
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;


public class StreamUtil {

    public static <T> Stream<T> forIterator(Iterator<T> it){
        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(it, Spliterator.IMMUTABLE), false)
        		            .onClose(()->{
        		            	if(it instanceof Closeable){
        		            		Unchecked.ioException(()->{
        		            			((Closeable)it).close();
        		            		});
        		            	}
        		            });
    }

    /**
     * Creates a {@link Stream} for a given {@link Supplier},
     * similar to {@link Stream#generate(Supplier)}, except that
     * it is limited rather than infinite, and will return 
     * as soon as the provided {@link Supplier} returns an
     * empty optional.
     * @param sup
     * @return
     */
    public static <T> Stream<T> forGenerator(Supplier<Optional<T>> sup){
        Iterator<T> ir=new Iterator<T>(){
            public CachedSupplier<Optional<T>> next;
            public boolean initialized=false;

            @Override
            public synchronized boolean hasNext() {
                if(!initialized)initialize();
                return next.get().isPresent();
            }

            @Override
            public synchronized T next() {
                if(!initialized)initialize();
                Optional<T> n=next.get();
                cacheNext();
                return n.get();
            }

            public synchronized void cacheNext() {
                next.resetCache();
            }

            private void initialize(){
                next=CachedSupplier.of(sup);
                initialized=true;
            }
        };
        return forIterator(ir);
    }
    
    
    /**
     * <p>Creates a {@link Stream} for a given {@link Consumer}
     * which supplies elements by feeding them to the supplied
     * {@link Consumer}. This emulated the idea of a Generator
     * common to JavaScript and C# using the "yield" keyword.
     * Type inference on this method isn't always easy. For 
     * a more explicit typing, use {@link #forYieldingGenerator(TypeReference, Consumer)}.
     * </p>
     * 
     * <pre>
     * private static Stream<String> example(){
     *    return StreamUtil.forYieldingGenerator(c->{
	 * 		c.accept("D");
	 * 		c.accept("B");	//very similar to "yield" keyword	
	 * 		c.accept("A");      
	 *      c.accept("C");  
	 *    });
     * }
     * 
     * private static void printSortedExample(){
     * 	  
     *    //Prints "A;B;C;D" 
     * 	  System.out.println(example().sorted().collect(Collectors.joining(","));
     *     
     * }
     * 
     * <pre>
     * 
     * <p>
     * Special care must be taken if the resulting stream is going to be terminated
     * early (e.g. with {@link Stream#limit(long)}), as this will result in 
     * incomplete execution of the supplied operation. It will simply
     * keep a created {@link Thread} in a waiting state. This can cause issues
     * if there are resources which need to be closed or released in the
     * generator code.
     * </p>
     * 
     * 
     * 
	 * @param takerConsumer
	 * @return
	 */
    public static <T> Stream<T> forYieldingGenerator(Consumer<Consumer<T>> takerConsumer){
    	BlockingQueue<Optional<T>> bq = new LinkedBlockingQueue<Optional<T>>(1);
    	
    	boolean[] isDone=new boolean[]{false};
    	
    	Consumer<Optional<T>> taker = (t)->{
    		try{
    			if(isDone[0])return;
    			if(!t.isPresent())isDone[0]=true;
    			bq.put(t);
    		}catch(Exception e){
    			throw new RuntimeException(e);
    		}
    	};

    	Stream<T> stream= forGenerator(()->{
    		try {
    			return bq.take();
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
    	});
    	
    	Thread t= new Thread(()->{
    		takerConsumer.accept(t1->{
    			taker.accept(Optional.ofNullable(t1));
    		});
    		//force ending
    		taker.accept(Optional.empty());
    	});
    	
    	t.start();
    	
    	return stream;
    }
    
    
    /**
     * Does the same as for {@link #forYieldingGenerator(Consumer)}
     * except a {@link TypeReference} is used to make
     * the type used explicit.
     * 
     * <pre>
     *  //This won't have issues with type inference
     * 	StreamUtil.forYieldingGenerator(new TypeReference<String>(),(c)->{
     * 		c.accept("A");
     *      c.accept("B");
     *  })
     *  .collect(Collectors.joining(";"); 
     * </pre>
     * 
     * </pre>
     * @param tr
     * @param takerConsumer
     * @return
     */
    public static <T> Stream<T> forYieldingGenerator(TypeReference<T> tr,Consumer<Consumer<T>> takerConsumer){
    	return forYieldingGenerator(takerConsumer);
    }
    
    
    /**
     * This is simply a class used to make type references
     * more explicit in certain cases. By speficying 
     * @author tyler
     *
     * @param <T>
     */
    public static class TypeReference<T>{
    }



    /**
     * Similar to {@link #forGenerator(Supplier)}, except that
     * it returning null triggers the termination of the stream,
     * rather than an empty {@link Optional}.
     * @param sup
     * @return
     */
    public static <T> Stream<T> forNullableGenerator(Supplier<T> sup){
        return forGenerator(()->{
            return Optional.ofNullable(sup.get());
        });
    }

    public static <T> Stream<T> forIterable(Iterable<T> sup){
        return StreamSupport.stream(sup.spliterator(),false);
    }

    private static <K,T> Stream<T> forNullableGenerator(final K k, Function<K,T> sup){
        return forNullableGenerator(()->sup.apply(k));
    }

    /**
     * Returns a {@link StreamGenerator}, which assumes that the provided
     * argument will be used to extract elements for a new stream. This is
     * useful for things which are like {@link Enumeration}s or {@link Iterator}
     * s, but do not explicitly implement those interfaces, making streams
     * slightly more difficult. A typical example may be something like this:
     * 
     * <pre>
     * <code>
     *     //Using standard state-full loop
     *     Matcher match = Pattern.compile("([A-Z])([0-9])").matcher("A1B2C3D4E5");
     *     StringBuilder sb = new StringBuilder();
     *     while(match.find()){
     *          if(sb.length()>0)sb.append(";");
     *          sb.append(match.group(1) + "." + match.group(2));
     *     }
     *     String mod1=sb.toString(); 
     *       
     *     
     *     //Using stream
     *     String mod2=StreamUtil.from(Pattern.compile("([A-Z])([0-9])").matcher("A1B2C3D4E5"))
     *          .streamWhile(m->m.find())
     *          .map(mr->mr.group(1) + "." + mr.group(2))
     *          .collect(Collectors.joining(";"));
     *      
     *     System.out.println(mod1);//A.1;B.2;C.3;D.4;E.5
     *     System.out.println(mod2);//A.1;B.2;C.3;D.4;E.5
     * </code>
     * </pre>
     * 
     * @param k
     * @return
     */

    public static <K> StreamGenerator<K> from(K k){
        
        
        return new StreamGenerator<K>(k);
    }


    public static class StreamGenerator<K>{
        private K k;
        private StreamGenerator(K k){
            this.k=k;
        }
        

        /**
         * Create a stream, where the seed value given is used
         * and function is provided to extract the "next" value
         * from that seed generator. If the function returns "null"
         * the stream will be terminated.
         * @param next
         * @return
         */
        public <T> Stream<T> streamNullable(ThrowableFunction<K,T> next){
            return forNullableGenerator(k, next);
        }

        /**
         * Create a stream, where the seed value given is used
         * and function is provided to extract the "next" value
         * from that seed generator. If the function returns an
         * empty Optional, then the stream is terminated.
         * @param next
         * @return
         */
        public <T> Stream<T> streamOptional(ThrowableFunction<K,Optional<T>> next){
            return forGenerator(()->next.apply(k));
        }

        /**
         * Create a stream, where the provided seed generator
         * is simply returned as long as a predicate test passes.
         * <p>
         * Note: the returned stream has undefined behavior
         * if parallelized prior to a mapping to a non-stateful
         * representation.
         * </p>
         * @param next
         * @return
         */
        public Stream<K> streamWhile(Predicate<K> next){
            return forGenerator(()->(next.test(k))?Optional.of(k):Optional.empty())
                    .sequential();
        }
    }

    /**
     * Utility function to help concatenate streams together
     * @param s
     * @return
     */
    public static <T> StreamConcatter<T> with(Stream<T> s){
        return new StreamConcatter<T>().and(s);
    }

    /**
     * Creates an infinite stream repeating the given
     * values in order.
     * @param elements
     * @return
     */
    public static <T> Stream<T> cycle(T ... elements){
        return Stream.generate(RolloverIterator.create(elements)::next);
    }


    private static final class RolloverIterator<T> {
        private final T[] elements;

        int index=0;
        private static <T> RolloverIterator<T> create(T[] elements){
            return new RolloverIterator<>(elements);
        }
        private  RolloverIterator(T[] elements){
            this.elements = elements;
        }


        public T next() {

            return elements[(index++)%elements.length];
        }
    }
    
    public static Stream<String> lines(String text){
    	return Arrays.stream(text.split("\n"));
    }
    
    
    /**
     * Returns a supplier from the stream, which will return
     * the results of the stream, in order. Will return null
     * when the stream is exhausted.
     * @param stream
     * @return
     */
    public static <T> Supplier<T> supplierFor(Stream<T> stream){
        final Iterator<T> it = stream.iterator();
        Supplier<T> sup = ()->{
            synchronized(it){
                if(it.hasNext()){
                    return it.next();
                }
            }
            return null;
        };
        return sup;
    }


    /**
     * A simple Builder pattern for concatenating a
     * stream. This is just a convenience class, to
     * avoid having to call {@link Stream#concat(Stream, Stream)}
     * recursively on many items.
     * 
     * 
     * @author peryeata
     *
     * @param <T>
     */
    public static class StreamConcatter<T>{
        Stream<T> s= Stream.empty();
        private StreamConcatter(){}

        public StreamConcatter<T> and(Stream<T> newstream){
            s=Stream.concat(s, newstream);
            return this;
        }

        public StreamConcatter<T> and(Collection<T> newCollection){
            s=Stream.concat(s, newCollection.stream());
            return this;
        }

        public StreamConcatter<T> and(Iterable<T> newCollection){
            s=Stream.concat(s, forIterator(newCollection.iterator()));
            return this;
        }

        public StreamConcatter<T> and(T ... newThings){
            s=Stream.concat(s, Stream.of(newThings));
            return this;
        }

        public Stream<T> stream(){
            return this.s;
        }
    }


    public static <T> Stream<T> forEnumeration(Enumeration<T> enumeration){
        return forNullableGenerator(()->{
            return (enumeration.hasMoreElements())?enumeration.nextElement():null;
        });
    }
    
    


    /**
     * <p>
     * Creates a {@link Collector} which will use the provided {@link Comparator}
     * and limit integer to collect only the "max" n elements, and return them
     * as a stream.
     * </p> 
     * 
     * <p>
     * For example, the following should give the same results:
     * </p>
     * 
     * <pre>
     *  <code>
     *      //Naive mechanism
     *      List<String> naive = Stream.of("B", "A", "E", "Z", "C", "Q", "T")
     *            .sort((a,b)->a.compareTo(b))
     *            .limit(3)
     *            .collect(Collectors.toList()); //["A", "B", "C"]
     *            
     *      //Targeted mechanism
     *      List<String> targeted = Stream.of("B", "A", "E", "Z", "C", "Q", "T")
     *            .collect(maxElements(3,(a,b)->a.compareTo(b))) //returns stream
     *            .collect(Collectors.toList()); //["A", "B", "C"]
     *   </code>
     * </pre>
     * 
     * @param n Limit of records returned
     * @param comp Comparator to do sorting
     * @return
     */
    public static <T> Collector<T,?,Stream<T>> maxElements(int n, Comparator<T> comp){
        return new ReducedCollector<T>(n,comp);
    }

    public static class ReducedCollector<T> implements Collector<T,TopNReducer<T>,Stream<T>>{
        private final Comparator<T> comp;
        private final int max;
        private ReducedCollector(int max,Comparator<T> comp){

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
