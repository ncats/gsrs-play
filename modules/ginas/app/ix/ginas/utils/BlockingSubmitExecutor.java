package ix.ginas.utils;

import java.util.concurrent.*;

/**
 * A {@link ThreadPoolExecutor} that will actually
 * block inside calls to submit() until the inner blocking queue
 * has room.  This should never use its RejectionPolicy.
 *
 * Created by katzelda on 3/25/16.
 */
public class BlockingSubmitExecutor extends ThreadPoolExecutor{

    /**
     * Create a new instance
     * @param corePoolSize the minimum number of threads always in the thread pool.
     * @param maximumPoolSize the maximum number of threads that can be in the threadpool.
     * @param keepAliveTime the amount of time an idle thread should be kept alive in the threadpool
     *                      when the pool size is above core.
     * @param unit the time unit of keep alive time.
     * @param blockingQueueCapacity the capacity of the blocking queue for jobs to be run.  Once the number of waiting
     *                              jobs exceeds this capacity, the submit() calls will block until one of the threads
     *                              completes its currently running task.
     */
    public BlockingSubmitExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, int blockingQueueCapacity) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, new BlockingOfferQueue<Runnable>(blockingQueueCapacity));
    }


    private static final class BlockingOfferQueue<E> extends LinkedBlockingDeque<E> {

        public BlockingOfferQueue(int capacity) {
            super(capacity);
        }

        @Override
        public boolean offer(E e) {
            //Executors never call blocking put()
            //but instead calls offer() which doesn't block
            //but immediately returns true/false
            //if the runnable can be put without blocking.
            //If the queue would have blocked, then
            //the Exectuor calls the RejectionPolicy
            //which will either throw Exception and abort
            //or make the runnable run on THIS thread
            //which will make things run much slower
            //because we can't submit any new threads until
            //this (possibly long running) job completes.

            //Therefore, we will change poll
            //to turn it into a put()!
            //which will force the executor to block

            try {
                put(e);
                return true;
            } catch (InterruptedException e1) {
                //we got interrupted
                //propagate up
                Thread.currentThread().interrupt();
            }
            //I don't think this will ever get called unless
            //maybe if we are interrupting?
            //eitherway makes compiler happy...
            return false;
        }
    }
}
