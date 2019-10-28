package ix.core.search;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.concurrent.Callable;
import play.Logger;

/**
 * This is a List where objects are stored using {@link ix.core.search.NamedCallable} to 
 * lazily fetch them. Instantiating this list and using it by itself is rather pointless,
 * as it just wraps objects with a Callable wrapper. However, using {@link #addCallable(Callable)} will allow
 * certain List / Collection operations to still be performed while deferring the actual fetching of the objects.
 * 
 * This is useful when actual acquisition of objects is prohibitively expensive, or the objects
 * would be prohibitively large in memory, but a List would still be useful for existing pipelines
 * or certain simple collection operations.
 *   
 * @author tyler
 *
 * @param <N> The type of the name to be used
 * @param <T> The type of the object returned (the things "stored" in the list)
 */
public class LazyList<N,T> implements List<T>{
	private ObjectNamer<T,N> objectNamer;
	
	private List<NamedCallable<N,T>> _internalList = new ArrayList<NamedCallable<N,T>>();
	
	public static interface ObjectNamer<T,N>{
		public N nameFor(T t);
	}

	public static interface NamedCallable<KK,VV> extends Callable<VV>{
		default KK getName(){
			return null;
		}
	}
	
	public LazyList(ObjectNamer<T,N> objectNamer){
		this.objectNamer=objectNamer;
	}
	
	public class DefaultNamedCallable implements NamedCallable<N,T>{
		T v;
		
		N name=null;
		
		public DefaultNamedCallable(T v){
			this.v=v;
			name = LazyList.this.objectNamer.nameFor(v);
		}
		@Override
		public T call() throws Exception {
			return v;
		}
		
		@Override
		public N getName(){
			return name;
		}
	}
	 
	
	public List<NamedCallable<N,T>> getInternalList(){
	    return this._internalList;
	}
	
	
	
	@Override
	public int size() {
		return _internalList.size();
	}

	@Override
	public boolean isEmpty() {
		return _internalList.isEmpty();
	}

	@Override
	public boolean contains(Object o) {
		throw new UnsupportedOperationException("Can't use contains method on FutureList");
	}

	@Override
	public Iterator<T> iterator() {
		return listIterator();
	}

	@Override
	public Object[] toArray() {
		Object[] array = new Object[this.size()];
		int i=0;
		for(Object o:this){
			array[i++]=o;
		}
		return array;
		//throw new UnsupportedOperationException("toArray method not encoraged");
	}

	@Override
	public <T> T[] toArray(T[] a) {
		throw new UnsupportedOperationException("toArray method not encoraged");
	}

	@Override
	public boolean add(T e) {
	    synchronized(_internalList){
	        return _internalList.add(new DefaultNamedCallable(e));
	    }
	}

	@Override
	public boolean remove(Object o) {
		throw new UnsupportedOperationException("remove method not encoraged");
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		throw new UnsupportedOperationException("contains method not supported");
	}

	@Override
	public boolean addAll(Collection<? extends T> c) {
		boolean changed = true;
		for(T k : c){
			changed &= this.add(k);
		}
		return changed;
	}

	@Override
	public boolean addAll(int index, Collection<? extends T> c) {
		throw new UnsupportedOperationException("add all index method not supported");
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		throw new UnsupportedOperationException("remove all index method not supported");
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		throw new UnsupportedOperationException("retain all index method not supported");
	}

	@Override
	public void clear() {
		this._internalList.clear();
	}

	@Override
	public T get(int index) {
		try {
			NamedCallable<N,T> nc=_internalList.get(index);
			try {
				return nc.call();
			} catch (Exception e) {
				System.err.println("Named callable with name:" + nc.getName().toString() + " could not be fetched");
				e.printStackTrace();
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println("Couldn't fetch Named Callable from list");
		}
		return null;
	}

	@Override
	public T set(int index, T element) {
		T old = this.get(index);
		_internalList.set(index, ()->element);
		return old;
	}

	@Override
	public void add(int index, T element) {
		throw new UnsupportedOperationException("add at index method not supported");
	}

	@Override
	public T remove(int index) {
		T old = this.get(index);
		_internalList.remove(index);
		return old;
	}

	@Override
	public int indexOf(Object o) {
		throw new UnsupportedOperationException("index of method not supported");
	}

	@Override
	public int lastIndexOf(Object o) {
		throw new UnsupportedOperationException("last index of method not supported");
	}

	private static class LazyListIterator<K> implements ListIterator<K>{
		List<K> ml;
		int cindex=0;
		int lastgot=-1;
		public LazyListIterator(List<K> inner, int cursor){
			cindex=cursor;
			ml = inner;
		}
		
		@Override
		public boolean hasNext() {
			return (cindex<ml.size());
		}

		@Override
		public K next() {
			K next=current();
			if(next==null){
				throw new NoSuchElementException("Entry number:" + cindex  + " could not be found in list of size " + ml.size() + ", the index may not be consistent with the database");
			}
			cindex++;
			return next;
		}
		
		public K current(){
			K cur=null;
			if(cindex<ml.size() && cindex>=0){
				cur=ml.get(cindex);
				lastgot=cindex;
			}
			return cur;
		}

		@Override
		public boolean hasPrevious() {
			return cindex>0;
		}

		@Override
		public K previous() {
			cindex--;
			if(cindex<0)cindex=0;
			return current();
		}

		@Override
		public int nextIndex() {
			return Math.min(cindex, ml.size());
		}

		@Override
		public int previousIndex() {
			return cindex-1;
		}

		@Override
		public void remove() {
			ml.remove(cindex);
		}

		@Override
		public void set(K e) {
			ml.set(lastgot, e);
		}

		@Override
		public void add(K e) {
			ml.add(e);
		}
	}
	
	@Override
	public ListIterator<T> listIterator() {
		return new LazyList.LazyListIterator<T>(this,0);
		
	}

	@Override
	public ListIterator<T> listIterator(int index) {
		return new LazyList.LazyListIterator<T>(this,index);
	}

	@Override
	public List<T> subList(int fromIndex, int toIndex) {
		throw new UnsupportedOperationException("sublist method not supported");
	}
	
	/**
	 * Adds a named callable to be used for fetching results
	 * as needed
	 * 
	 * @param c
	 * @return
	 */
	public boolean addCallable(NamedCallable<N,T> c){
	    synchronized(_internalList){
	        return this._internalList.add(c);
	    }
	}
	
	
	
	public void sortByNames(Comparator<N> c) {
		
		NamedCallable<N,T>[] a = this._internalList.toArray(new NamedCallable[0]);
        if(c==null){
        	Comparator<String> stringComparator = DefaultComparator.getInstance();
        	c = (n1,n2)->{
        	    return stringComparator.compare(n1.toString(), n2.toString());
        	};
        }
        final Comparator<N> rawComparator =c;
        Arrays.sort(a, (c1, c2) -> {
				N n1= c1.getName();
				N n2= c2.getName();
				return rawComparator.compare(n1, n2);
			});
        
        synchronized(_internalList){
            ListIterator<NamedCallable<N,T>> i = _internalList.listIterator();
            
            for (NamedCallable<N,T> e : a) {
                i.next();
                i.set(e);
            }
        }
    }
	
	@Override
	public void sort(Comparator<? super T> c) {
		NamedCallable<N,T>[] a = this._internalList.toArray(new NamedCallable[0]);
        if(c==null){
        	c=(Comparator<? super T>)DefaultComparator.getInstance();
        }
        final Comparator<? super T> rawComparator =c;
        Arrays.sort(a, (c1,c2) -> {
				T o1=null;
				T o2=null;
				try {
					o1 = c1.call();
				} catch (Exception e) {}
				try {
					o2 = c2.call();
				} catch (Exception e) {}
				return rawComparator.compare(o1, o2);
			});
        ListIterator<NamedCallable<N,T>> i = _internalList.listIterator();
        for (NamedCallable<N,T> e : a) {
            i.next();
            i.set(e);
        }
    }
	
	//used to get the default Comparators
	private static final class DefaultComparator<E extends Comparable<E>> implements Comparator<E>{
	    @SuppressWarnings( "rawtypes" )
	    private static final DefaultComparator<?> INSTANCE = new DefaultComparator();

	    /**
	     * Get an instance of DefaultComparator for any type of Comparable.
	     *
	     * @param <T> the type of Comparable of interest.
	     *
	     * @return an instance of DefaultComparator for comparing instances of the requested type.
	     */
	    public static <T extends Comparable<T>> Comparator<T> getInstance(){
	        @SuppressWarnings("unchecked")
	        Comparator<T> result = (Comparator<T>)INSTANCE;
	        return result;
	    }

	    private DefaultComparator(){}

	    @Override
	    public int compare( E o1, E o2 ){
	        if( o1 == o2 )
	            return 0;
	        if( o1 == null )
	            return 1;
	        if( o2 == null )
	            return -1;
	        return o1.compareTo( o2 );
	    }
	}
	
	
	
	/**
	 * Create a {@link LazyList} form of the provided collection.
	 * 
	 * This is not encouraged.
	 * 
	 * @param collection
	 * @return
	 */
	public static <T,N> LazyList<N,T> of(Collection<T> collection, ObjectNamer<T,N> on){
		if(collection instanceof LazyList){
			return (LazyList<N,T>)collection;
		}else{
			LazyList<N,T> ll = new LazyList<N,T>(on);
			ll.addAll(collection);
			return ll;
		}
	}
	
	public void addAll(LazyList<N,T> lazylist){
		this._internalList.addAll(lazylist._internalList);
	}
	
	
}
