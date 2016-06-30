package ix.core.util;

import java.util.concurrent.Callable;

/**
 * Created by katzelda on 5/12/16.
 */
public final class StopWatch {

    private StopWatch(){
        //can not instantiate
    }

    public static long timeElapsed(Runnable r){
        long start = System.currentTimeMillis();
        r.run();
        long end = System.currentTimeMillis();
        return end-start;

    }

    public static long timeElapsed(Callable<Void> c) throws Exception{
        long start = System.currentTimeMillis();
        c.call();
        long end = System.currentTimeMillis();
        return end-start;
    }
}
