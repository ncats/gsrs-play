package ix.test.util;

import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Created by katzelda on 5/3/17.
 */
public class WaitChecker<T, R> {

    private final Supplier<T> supplier;
    private final Predicate<T> predicate;
    private final Function<T, R> transformerFunction;
    private int maxNumTries = Integer.MAX_VALUE;
    private long sleepTime = 1000;

    public WaitChecker(Supplier<T> supplier, Predicate<T> predicate, Function<T, R> transformerFunction) {
        this.supplier = supplier;
        this.predicate = predicate;
        this.transformerFunction = transformerFunction;
    }

    /**
     * Sets the maximum number of times to try.
     * If this method is not called then it will keep trying until it is successful.
     *
     * @param maxNumTries a positive number.  If {@code <1} then an IllegalArgumentException is thrown.
     * @return this
     */
    public WaitChecker setMaxNumTries(int maxNumTries) {
        if(maxNumTries < 1){
            throw new IllegalArgumentException("max num tries must be >=1");
        }
        this.maxNumTries = maxNumTries;
        return this;
    }

    /**
     * Set the amount of time to wait between tries, if not specified,
     * it is 1 SECOND.
     *
     * @param duration the duration
     * @param units the TimeUnit of the duration.
     * @return this.
     */
    public WaitChecker setAwaitTime(long duration, TimeUnit units) {
        sleepTime = units.toMillis(duration);
        return this;
    }

    public Optional<R> execute() throws InterruptedException {
        int count=0;
        T t;
        boolean passed=false;
        do{
            if(count >0){
//                System.out.println("waiting.." + count + " / " + maxNumTries);
                Thread.sleep(sleepTime);
            }

            t = supplier.get();
            count++;
            passed = predicate.test(t);
        }while( !passed && count<maxNumTries);

        if(passed){
            return Optional.ofNullable(transformerFunction.apply(t));
        }
        return Optional.empty();
    }
}
