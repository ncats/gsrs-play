package ix.core.adapters;

import ix.core.models.Edit;
import ix.core.util.EntityUtils;
import play.Logger;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by katzelda on 4/18/17.
 */
class EditLock {
    private EntityPersistAdapter.Counter count = new EntityPersistAdapter.Counter();
    private ReentrantLock lock = new ReentrantLock();


    private InxightTransaction transaction = null;
    private Edit edit = null;

    private boolean preUpdateWasCalled = false;
    private boolean postUpdateWasCalled = false;

    private Runnable onPostUpdate = new Runnable() {
        @Override
        public void run() {
        }
    };


    private final EntityUtils.Key thekey;

    public EditLock(EntityUtils.Key thekey) {
        this.thekey = thekey;
    }


    public boolean isLocked() {
        return this.lock.isLocked();
    }

    public boolean tryLock() {
        return this.lock.tryLock();
    }

    public boolean hasEdit() {
        return this.edit != null;
    }

    public EditLock addEdit(Edit e) {
        if (hasEdit()) {
            System.out.println("Existing edit will be overwritten");
        }
        this.edit = e;
        return this;
    }

    public InxightTransaction getTransaction() {
        return this.transaction;
    }

    public EditLock setTransaction(InxightTransaction it) {
        this.transaction = it;
        return this;
    }


    public void acquire() {
        synchronized (count) {
            count.increment();
        }
        while (true) {

            if (lock.isHeldByCurrentThread()) {
                System.out.println("Yes, we got this twice.");
            }
            try {
                if (lock.tryLock(1, TimeUnit.SECONDS)) {
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


    public EditLock addOnPostUpdate(Runnable r) {
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
}
