package ix.ginas.utils.reindex;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * A {@link ReIndexListener} that wraps several listeners behind a single
 * interface so notifying this one listener notifies all the wrapped listeners.
 *
 * This is useful for when you want to notify several listeners at the same time
 * but the class that calls the listener only takes a single listener.
 *
 * Created by katzelda on 8/30/16.
 */
public class MultiReIndexListener implements ReIndexListener {

    private final List<ReIndexListener> listeners;
    /**
     * Create a new instance that will wrap the given listeners.
     * @param listeners the list of listeners to wrap.  Can not be null
     *                  and no elements in the list can be null either.
     *
     * @throws NullPointerException if the list or any element are null.
     */
    public MultiReIndexListener(ReIndexListener... listeners) {
        this(Arrays.asList(listeners));
    }

    /**
     * Create a new instance that will wrap the given listeners.
     * @param listeners the list of listeners to wrap.  Can not be null
     *                  and no elements in the list can be null either.
     *
     * @throws NullPointerException if the list or any element are null.
     */
    public MultiReIndexListener(List<ReIndexListener> listeners) {
        //defensive copy
        this.listeners = new ArrayList<>(listeners);
        for(ReIndexListener l : listeners){
            Objects.requireNonNull(l, "listener can not be null");
        }
    }

    @Override
    public void newReindex() {
        listeners.forEach(ReIndexListener::newReindex);
    }

    @Override
    public void doneReindex() {
        listeners.forEach(ReIndexListener::doneReindex);
    }

    @Override
    public void recordReIndexed(Object o) {
        listeners.forEach(l -> l.recordReIndexed(o));
    }

    @Override
    public void error(Throwable t) {
        listeners.forEach(l -> l.error(t));
    }

    @Override
    public void totalRecordsToIndex(int total) {
        listeners.forEach(l -> l.totalRecordsToIndex(total));
    }

    @Override
    public void countSkipped(int numSkipped) {
        listeners.forEach(l -> l.countSkipped(numSkipped));
    }
}
