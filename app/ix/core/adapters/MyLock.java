package ix.core.adapters;

import ix.core.models.Edit;
import ix.core.util.EntityUtils;
import play.Logger;

import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by katzelda on 4/14/17.
 */
class MyLock {
    private Counter count = new Counter();
    private ReentrantLock lock = new ReentrantLock();

    private final Map<EntityUtils.Key, MyLock> lockMap;

    private Edit edit = null;

    private boolean preUpdateWasCalled = false;
    private boolean postUpdateWasCalled = false;

    private Runnable onPostUpdate = new Runnable() {

        @Override
        public void run() {

        }

    };


    private final EntityUtils.Key thekey;

    public MyLock(EntityUtils.Key thekey, Map<EntityUtils.Key, MyLock> lockMap) {
        this.thekey = thekey;
        this.lockMap = lockMap;
    }

    public boolean hasEdit() {
        return this.edit != null;
    }

    public boolean isLocked() {
        return this.lock.isLocked();
    }

    public boolean tryLock() {
        return this.lock.tryLock();
    }

    public MyLock addEdit(Edit e) {
        this.edit = e;
        return this;
    }


    public void acquire() {
        synchronized (count) {
            count.increment();
        }
        while (true) {
            try {
                if (lock.tryLock(1, TimeUnit.MINUTES)) {
                    break;
                } else {
                    Logger.warn("still waiting for lock with key " + thekey);
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        //reset
        preUpdateWasCalled = false;
        postUpdateWasCalled = false;
        this.edit = null;
    }


    public MyLock addOnPostUpdate(Runnable r) {
        Runnable rold = this.onPostUpdate;
        this.onPostUpdate = new Runnable() {

            @Override
            public void run() {
                rold.run();
                r.run();
            }

        };
        return this;
    }

    public void release() {
        synchronized (count) {
            count.decrementAndGet();
        }
        lock.unlock();
        synchronized (count) {
            int value = count.intValue();
            if (value == 0) {
                //no more blocking records?
                //remove ourselves from the map to free memory
                lockMap.remove(thekey);
            }
        }
    }

    public void markPreUpdateCalled() {
        preUpdateWasCalled = true;
    }

    public void markPostUpdateCalled() {
        if (postUpdateWasCalled == false && this.onPostUpdate != null) {
            onPostUpdate.run();
        }
        postUpdateWasCalled = true;

    }

    public boolean hasPreUpdateBeenCalled() {
        return preUpdateWasCalled;
    }

    public boolean hasPostUpdateBeenCalled() {
        return postUpdateWasCalled;
    }


    private static class Counter{
        private int count=0;

        public void increment(){
            count++;
        }

        public int decrementAndGet(){
            return --count;
        }

        public int intValue(){
            return count;
        }
    }
}
