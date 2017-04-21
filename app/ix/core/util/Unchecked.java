package ix.core.util;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.concurrent.Callable;

/**
 * Created by katzelda on 4/20/17.
 */
public class Unchecked {
    public interface ThrowingRunnable<E extends Exception>{
        void run() throws E;
    }

    public static void ioException(ThrowingRunnable<? super IOException> runnable){
        try{
            runnable.run();
        }catch(IOException e){
            throw new UncheckedIOException(e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static <V> V uncheck(Callable<V> callable){
        try{
            return callable.call();
        }catch(IOException e){
            throw new UncheckedIOException(e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
