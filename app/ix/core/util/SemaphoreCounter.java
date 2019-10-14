package ix.core.util;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by katzelda on 8/29/18.
 */
public class SemaphoreCounter<T> {

    private static Runnable NO_OP = ()->{};
    private ConcurrentHashMap<T, Counter> map = new ConcurrentHashMap<>();

    /**
     * Tests it the supplied value is currently being tracked.
     *
     * @param o
     * @return
     * Returns false if not tracked. True otherwise.
     */
    public synchronized boolean contains(T o){
    	return map.containsKey(o);
    }

    /**
     * Adds the supplied value to be tracked,
     * incrementing the count if it exists, or setting
     * it to be 1 otherwise.
     *
     *
     * @param o
     * @return
     * This returns true if this was a <strong>new</strong>
     * (untracked) addition, and false otherwise.
     */
    public synchronized boolean add(T o){
        return add(o, null, true);
    }

    /**
     * Adds the supplied value to be tracked,
     * as well as a {@link Runnable} to be executed upon
     * removal. If "onlyRunIfNew" is set to true, then it will
     * not execute if the element is already tracked.
     *
     * @param o
     * @param cleanup
     * @param onlyRunIfNew
     * @return
     * This returns true if this element is a <strong>new</strong>
     * (untracked) addition, and false otherwise.
     */
    public synchronized boolean add(T o, Runnable cleanup, boolean onlyRunIfNew){
        Runnable r = cleanup == null? NO_OP: cleanup;

        Counter counter = map.computeIfAbsent(o, i->new Counter());
        if(!onlyRunIfNew || (onlyRunIfNew && counter.getDepth() ==0)){
            counter.push(r);
        }else{
            counter.push( NO_OP);
        }
        return counter.getDepth()==1;
    }

    /**
     * <p>
     * Completely remove <i>all</i> tracked counters
     * for the supplied element, executing all supplied
     * {@link Runnable}s if necessary, in LIFO order.
     * </p>
     *
     * @param o
     * @return
     * True if there was a value found to remove, false otherwise.
     *
     */
    public synchronized boolean removeCompletely(T o){
        Counter counter = map.remove(o);
        if(counter ==null){
            return false;
        }
        while(counter.getDepth()>0){
            counter.pop().run();
        }
        return true;
    }


    /**
     * <p>Remove the top of the counter stack for a supplied
     * element, executing any supplied {@link Runnable}s
     * if necessary, in LIFO order.</p>
     *
     * @param o
     * @return
     * True if there was a value found to remove, false otherwise.
     */
    public synchronized boolean remove(T o){
        Counter counter = map.get(o);
        if(counter==null){
            return false;
        }
        try{
            Runnable r = counter.pop();
            if(r ==null){
                return false;
            }
            r.run();
        }finally{
            if(counter.getDepth() ==0){
                map.remove(o);
            }
        }
        return true;
    }

    @Override
    public String toString() {
        return "SemaphoreCounter{" +
                "map=" + map +
                '}';
    }

    private static class Counter{

        Deque<Runnable> stack = new ArrayDeque<>();
        @Override
        public String toString() {
            return "Counter{" +
                    "stack=" + stack +
                    '}';
        }


        public void push(Runnable r){
            stack.push(r);
        }
        /**
         * Removes and returns the top element of the counter stack.
         * Returns null if there is nothing to return
         * @return
         */
        public Runnable pop(){
            return stack.poll();
        }

        public int getDepth(){
            return stack.size();
        }
    }
}
