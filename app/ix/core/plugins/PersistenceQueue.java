package ix.core.plugins;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;

import play.Logger;
import play.Plugin;
import play.Application;
import play.db.ebean.Model;
import com.avaje.ebean.Ebean;
import com.avaje.ebean.Transaction;

public class PersistenceQueue extends Plugin implements Runnable {
    public interface PersistenceContext {
        enum Priority {
            LOW,
            MEDIUM,
            HIGH,
            URGENT
        }
        void persists () throws Exception;
        Priority priority ();
    }

    public static abstract class AbstractPersistenceContext
        implements PersistenceContext {
        public abstract void persists () throws Exception;
        public Priority priority () { return Priority.MEDIUM; }
    }

    public static class DefaultPersistenceContext
        implements PersistenceContext {
        Model model;
        public DefaultPersistenceContext (Model model) {
            this.model = model;
        }
        public void persists () throws Exception {
            model.save();
        }
        public Priority priority () { return Priority.MEDIUM; }
    }

    static class ContextWrapper implements PersistenceContext,
                                           Comparable<ContextWrapper> {
        PersistenceContext context;
        long whence = System.currentTimeMillis();
        
        ContextWrapper () {}
        ContextWrapper (PersistenceContext context) {
            this.context = context;
        }
        public void persists () throws Exception {
            if (context != null)
                context.persists();
        }
        public Priority priority () {
            return context != null ? context.priority() : Priority.LOW;
        }
        
        public int compareTo (ContextWrapper ctx) {
            int d = 0;
            if (context != null || ctx.context != null)
                d = ctx.context.priority().ordinal()
                    - context.priority().ordinal();
            if (d == 0) {
                if (whence < ctx.whence) d = -1;
                if (whence > ctx.whence) d = 1;
            }
            return d;
        }
    }

    private final Application app;
    private final ContextWrapper POISON = new ContextWrapper ();
    private final BlockingQueue<ContextWrapper> queue;
    private ExecutorService threadPool;

    public PersistenceQueue (Application app) {
        this.app = app;
        this.queue = new PriorityBlockingQueue<ContextWrapper>();
    }

    @Override
    public void onStart () {
        threadPool = Executors.newSingleThreadExecutor();
        threadPool.submit(this);
    }

    public void run () {
        String thread = Thread.currentThread().getName();
        Logger.debug("## "+getClass().getName()+" starts thread "+thread);
        long count = 0;
        try {
            for (ContextWrapper ctx; (ctx = queue.take()) != POISON;) {
                try {
                    ctx.persists();
                    ++count;
                }
                catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
        catch (InterruptedException ex) {
            Logger.debug("## "+getClass().getName()+": thread "+thread
                         +" interrupted!", ex);
        }
        Logger.debug("## "+getClass().getName()+": thread "+thread
                     +" processed "+count+" requests!");
    }

    @Override
    public void onStop () {
        if (threadPool != null) {
            try {
                queue.put(POISON);
                threadPool.shutdown();
            }
            catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        Logger.info("Plugin "+getClass().getName()+" stopped!");
    }

    public void submit (PersistenceContext context) {
        try {
            queue.put(new ContextWrapper (context));
        }
        catch (Exception ex) {
            ex.printStackTrace();
            throw new RuntimeException ("Unable submit request", ex);
        }
    }

    public void submit (Model model) {
        submit (new DefaultPersistenceContext (model));
    }
}
