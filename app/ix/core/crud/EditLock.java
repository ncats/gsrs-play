package ix.core.crud;

import ix.core.adapters.InxightTransaction;
import ix.core.models.Edit;
import ix.core.util.EntityUtils;
import play.Logger;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by katzelda on 4/18/17.
 */
public class EditLock {


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
    private Counter count = new Counter();
    private ReentrantLock lock = new ReentrantLock();


    private final  Map<EntityUtils.Key, EditLock> lockMap;

    private InxightTransaction transaction = null;
    private Edit edit = null;

    private boolean preUpdateWasCalled = false;
    private boolean postUpdateWasCalled = false;

    private Runnable onPostUpdate = () -> {}; //no-op


    private final EntityUtils.Key thekey;

    public EditLock(EntityUtils.Key thekey, Map<EntityUtils.Key, EditLock> lockMap) {
        this.thekey = thekey;
        this.lockMap = lockMap;
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

    public Optional<Edit> getEdit() {
        return Optional.of(edit);
    }

    public EditLock addEdit(Edit e) {
        if (hasEdit()) {
            Logger.warn("Existing edit will be overwritten");
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
        if(count.intValue()>1){
        	Logger.warn(this.thekey + ": has more than 1 lock active:" + count.intValue());
        }

        //reset
        preUpdateWasCalled = false;
        postUpdateWasCalled = false;
        this.edit = null;

    }


    public EditLock addOnPostUpdate(Runnable r) {
        Runnable rold = this.onPostUpdate;
        this.onPostUpdate = ()->{
        	rold.run();
        	r.run();
        };
        return this;
    }

    public void release() {
        synchronized (count) {
            count.decrementAndGet();
        }
        try{
        	lock.unlock();
        }catch(Exception e){
        	e.printStackTrace();
        	throw e;
        }
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


	public int getCount() {
		return count.intValue();
	}
}
