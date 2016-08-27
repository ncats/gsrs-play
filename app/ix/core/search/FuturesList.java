package ix.core.search;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.concurrent.Callable;

/**
 * This is a List where objects are stored using {@link java.util.concurrent.Callable} to 
 * lazily fetch them. Instantiating this list and using it by itself is rather pointless,
 * as it just wraps objects with a Callable wrapper. However, using {@link #addCallable(Callable)} will allow
 * certain List / Collection operations to still be performed while defering the actual fetching of the objects.
 * 
 * This is useful when actual acquisition of objects is prohibitively expensive, or the objects
 * would be prohibitively large in memory, but a List would still be useful for existing pipelines
 * or certain simple collection operations.
 *   
 * @author tyler
 *
 * @param <K>
 */
public class FuturesList<K> implements List<K>{
	public static interface ObjectNamer{
		public String nameFor(Object k);
	}
	
	private ObjectNamer objectNamer = null;
	
	public FuturesList(ObjectNamer objectNamer){
		this.objectNamer=objectNamer;
		
		
	}
	

	public static interface NamedCallable<K> extends Callable<K>{
		default String getName(){
			return null;
		}
	}
	
	public class DefaultNamedCallable<K> implements NamedCallable<K>{
		K k;
		String name=null;
		public DefaultNamedCallable(K k){
			this.k=k;
			name = FuturesList.this.objectNamer.nameFor(k);
		}
		@Override
		public K call() throws Exception {
			return k;
		}
		@Override
		public String getName(){
			return name;
		}
	}
	
	private List<NamedCallable<K>> clist = new ArrayList<NamedCallable<K>>();
	
	
	@Override
	public int size() {
		return clist.size();
	}

	@Override
	public boolean isEmpty() {
		return clist.isEmpty();
	}

	@Override
	public boolean contains(Object o) {
		throw new UnsupportedOperationException("Can't use contains method on FutureList");
	}

	@Override
	public Iterator<K> iterator() {
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
	public boolean add(K e) {
		return clist.add(new DefaultNamedCallable<K>(e));
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
	public boolean addAll(Collection<? extends K> c) {
		boolean changed = true;
		for(K k : c){
			changed &= this.add(k);
		}
		return changed;
	}

	@Override
	public boolean addAll(int index, Collection<? extends K> c) {
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
		this.clist.clear();
	}

	@Override
	public K get(int index) {
		try {
			return clist.get(index).call();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public K set(int index, K element) {
		K old = this.get(index);
		clist.set(index, ()->element);
		return old;
	}

	@Override
	public void add(int index, K element) {
		throw new UnsupportedOperationException("add at index method not supported");
	}

	@Override
	public K remove(int index) {
		K old = this.get(index);
		clist.remove(index);
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
				throw new NoSuchElementException(cindex  + " does not exist");
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
	public ListIterator<K> listIterator() {
		return new FuturesList.LazyListIterator<K>(this,0);
		
	}

	@Override
	public ListIterator<K> listIterator(int index) {
		return new FuturesList.LazyListIterator<K>(this,index);
	}

	@Override
	public List<K> subList(int fromIndex, int toIndex) {
		throw new UnsupportedOperationException("sublist method not supported");
	}
	
	/**
	 * Adds a named callable to be used for fetching results
	 * as needed
	 * 
	 * @param c
	 * @return
	 */
	public boolean addCallable(NamedCallable<K> c){
		return this.clist.add(c);
	}
	
	public void sortByNames(Comparator<String> c) {
		
		NamedCallable<K>[] a = this.clist.toArray(new NamedCallable[0]);
        if(c==null){
        	Comparator<String> stringComparator = DefaultComparator.getInstance();
        	c=stringComparator;
        }
        final Comparator<String> rawComparator =c;
        Arrays.sort(a, new Comparator<NamedCallable<K>>(){
			@Override
			public int compare(NamedCallable<K> c1, NamedCallable< K> c2) {
				String n1= c1.getName();
				String n2= c2.getName();
				return rawComparator.compare(n1, n2);
			}
        });
        ListIterator<NamedCallable<K>> i = clist.listIterator();
        for (NamedCallable<K> e : a) {
            i.next();
            i.set(e);
        }
    }
	
	@Override
	public void sort(Comparator<? super K> c) {
		NamedCallable<K>[] a = this.clist.toArray(new NamedCallable[0]);
        if(c==null){
        	c=(Comparator<? super K>)DefaultComparator.getInstance();
        }
        final Comparator<? super K> rawComparator =c;
        Arrays.sort(a, new Comparator<Callable<K>>(){
			@Override
			public int compare(Callable<K> c1, Callable< K> c2) {
				K o1=null;
				K o2=null;
				try {
					o1 = c1.call();
				} catch (Exception e) {}
				try {
					o2 = c2.call();
				} catch (Exception e) {}
				return rawComparator.compare(o1, o2);
			}
        });
        ListIterator<NamedCallable<K>> i = clist.listIterator();
        for (NamedCallable<K> e : a) {
            i.next();
            i.set(e);
        }
    }
	private static final class DefaultComparator<E extends Comparable<E>> implements Comparator<E>
	{
	    @SuppressWarnings( "rawtypes" )
	    private static final DefaultComparator<?> INSTANCE = new DefaultComparator();

	    /**
	     * Get an instance of DefaultComparator for any type of Comparable.
	     *
	     * @param <T> the type of Comparable of interest.
	     *
	     * @return an instance of DefaultComparator for comparing instances of the requested type.
	     */
	    public static <T extends Comparable<T>> Comparator<T> getInstance()
	    {
	        @SuppressWarnings("unchecked")
	        Comparator<T> result = (Comparator<T>)INSTANCE;
	        return result;
	    }

	    private DefaultComparator()
	    {
	    }

	    @Override
	    public int compare( E o1, E o2 )
	    {
	        if( o1 == o2 )
	            return 0;
	        if( o1 == null )
	            return 1;
	        if( o2 == null )
	            return -1;
	        return o1.compareTo( o2 );
	    }
	}
	
	
	
}
