package ix.core.util;

import java.io.Closeable;
import java.io.IOException;
import java.util.Iterator;
import java.util.Objects;

/**
 * Helper class that can create CloseableIterator
 * objects.
 *
 * Created by katzelda on 4/21/16.
 */
public final class CloseableIterators {

    private CloseableIterators(){
        //can not instantiate
    }

    /**
     * Wrap an Iterator and make it CloseableIterator that delegates all calls to Iterator#next(), Iterator#hasNext()
     * and Iterator#remove().  The returned object will usually be a new object, but will not always
     * be depending on what type is passed in.
     * <p>
     *     Rules for how to make the returned CloseableIterator instance:
     * </p>
     * <ol>
     *     <li>If the iterator is not closeable, then make a new CloseableIterator instance with an empty close()</li>
     *     <li>If the iterator IS closeable:
     *
     *          <ol>If the iterator already implements CloseableIterator, do not make a new object and just return the passed in object</ol>
     *          <ol>Make a new CloseableIterator that will delegate to the wrapped object's close()</ol>
     *     </li>
     * </ol>
     * If the iterator to wrap
     * is already also Closeable then it's close() will be correctly delegated to.
     * @param iter the iterator to wrap; can not be null.
     * @param <T> the type returned by Iterator calls to next().
     * @param <I> generic mess that is used to make compiler happy casting if the passed in iterator already is closeable.
     * @return a new CloseableIterator object if the passed in iter is not already a CloseableIterator; or
     * iter if it is.
     *
     * @throws NullPointerException if iter is null.
     */
    @SuppressWarnings("unchecked")
    public static <T, I extends Iterator<T> & Closeable> CloseableIterator<T> wrap(Iterator<T> iter){
        Objects.requireNonNull(iter);

        if(iter instanceof Closeable){
            //already closeableIterator just return it as is.
            if(iter instanceof CloseableIterator){
                return (CloseableIterator<T>) iter;
            }
            return new CloseableWrapper<>((I)iter);
        }
        return new Wrapper<>(iter);
    }


private static final class Wrapper<T> implements CloseableIterator<T>{
    private final Iterator<T> delegate;

    public Wrapper(Iterator<T> iter){
        this.delegate = iter;
    }

    @Override
    public void close() throws IOException {
       //no-op
    }

    @Override
    public boolean hasNext() {
        return delegate.hasNext();
    }

    @Override
    public T next() {
        return delegate.next();
    }

    @Override
    public void remove() {
        delegate.remove();
    }
}


    private static final class CloseableWrapper<T, I extends Iterator<T> & Closeable> implements CloseableIterator<T>{

        private final I delegate;

        public CloseableWrapper(I iter){
            this.delegate = iter;
        }

        @Override
        public void close() throws IOException {
            delegate.close();
        }

        @Override
        public boolean hasNext() {
            return delegate.hasNext();
        }

        @Override
        public T next() {
            return delegate.next();
        }

        @Override
        public void remove() {
            delegate.remove();
        }
    }
}
