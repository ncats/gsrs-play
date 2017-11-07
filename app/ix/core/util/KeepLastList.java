package ix.core.util;

import java.util.*;

/**
 * A {@link List} that only keeps the last X elements.  When more than
 * the specified max elements are added to the list, the head of the list is removed.
 *
 * @param <E> the type of element in the list.
 *
 * @author katzelda
 */
public class KeepLastList<E> extends AbstractSequentialList<E>{

    private final LinkedList<E> list = new LinkedList<>();

    private final int maxSize;

    /**
     * Create a new KeepLastList with the given maxSize.
     * @param maxSize the maximum size the list can be, must be &ge; 1.
     *
     * @throws IllegalArgumentException if maxSize is &lt; 1.
     */
    public KeepLastList(int maxSize) {
        if(maxSize <1){
            throw new IllegalArgumentException("max size must be positive");
        }
        this.maxSize = maxSize;
    }

    @Override
    public int size() {
        return list.size();
    }

    @Override
    public boolean add(E e) {
        boolean ret = list.add(e);
        if(list.size() > maxSize){
            list.removeFirst();
        }
        return ret;
    }


    @Override
    public ListIterator<E> listIterator(int index) {
        return new ListIter(index);
    }

    private class ListIter implements ListIterator<E>{

        private ListIterator<E> iter;

        public ListIter(int index){
            this.iter = KeepLastList.this.list.listIterator(index);
        }
        @Override
        public boolean hasNext() {
            return iter.hasNext();
        }

        @Override
        public E next() {
            return iter.next();
        }

        @Override
        public boolean hasPrevious() {
            return iter.hasPrevious();
        }

        @Override
        public E previous() {
            return iter.previous();
        }

        @Override
        public int nextIndex() {
            return iter.nextIndex();
        }

        @Override
        public int previousIndex() {
            return iter.previousIndex();
        }

        @Override
        public void remove() {
            iter.remove();
        }

        @Override
        public void set(E e) {
            iter.set(e);
        }

        @Override
        public void add(E e) {
            //this is the tricky one
            // if this causes a roll over it would increment concurrent modification 2x
            //which isn't good

            iter.add(e);
            System.out.println("iter added now size is " + size()+ " maxsize is ");
            if(size() > maxSize){
                int nextIndex= nextIndex();
                System.out.println("removing first");
                //need to remove first element
                KeepLastList.this.remove(0);
                iter = list.listIterator(nextIndex-1);
            }
        }
    }
}