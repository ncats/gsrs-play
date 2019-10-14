package ix.core.utils.executor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * A {@link ProcessListener} that wraps several listeners behind a single
 * interface so notifying this one listener notifies all the wrapped listeners.
 *
 * This is useful for when you want to notify several listeners at the same time
 * but the class that calls the listener only takes a single listener.
 *
 * Created by katzelda on 8/30/16.
 */
public class MultiProcessListener implements ProcessListener {

    private final List<ProcessListener> listeners;
    /**
     * Create a new instance that will wrap the given listeners.
     * @param listeners the list of listeners to wrap.  Can not be null
     *                  and no elements in the list can be null either.
     *
     * @throws NullPointerException if the list or any element are null.
     */
    public MultiProcessListener(ProcessListener... listeners) {
        this(Arrays.asList(listeners));
    }

    /**
     * Create a new instance that will wrap the given listeners.
     * @param listeners the list of listeners to wrap.  Can not be null
     *                  and no elements in the list can be null either.
     *
     * @throws NullPointerException if the list or any element are null.
     */
    public MultiProcessListener(List<ProcessListener> listeners) {
        //defensive copy
        this.listeners = new ArrayList<>(listeners);
        for(ProcessListener l : listeners){
            Objects.requireNonNull(l, "listener can not be null");
        }
    }

    @Override
    public void newProcess() {
        listeners.forEach(ProcessListener::newProcess);
    }

    @Override
    public void doneProcess() {
        listeners.forEach(ProcessListener::doneProcess);
    }

    @Override
    public void recordProcessed(Object o) {
        listeners.forEach(l -> l.recordProcessed(o));
    }

    @Override
    public void error(Throwable t) {
        listeners.forEach(l -> l.error(t));
    }

    @Override
    public void totalRecordsToProcess(int total) {
        listeners.forEach(l -> l.totalRecordsToProcess(total));
    }

    @Override
    public void countSkipped(int numSkipped) {
        listeners.forEach(l -> l.countSkipped(numSkipped));
    }

    @Override
    public void preRecordProcess(Object o) {
        listeners.forEach(l->l.preRecordProcess(o));
    }
}
