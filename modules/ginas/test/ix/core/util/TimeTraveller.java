package ix.core.util;

import org.junit.rules.ExternalResource;

import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * A JUnit {@link org.junit.Rule} that
 * can be used to change what
 * the date and time the Application
 * thinks it is.  Calling the
 * methods on this class will invoke
 * the corresponding
 * {@link TimeUtil} methods.  The
 * time is restored back to use system time
 * after each test.
 *
 * Example usage:
 *
 * <pre>
 *     @Rule
 *     TimeTraveller timeTraveller = new TimeTraveller();
 *
 *     ...
 *
 *     @Test
 *     public void myTest(){
 *
 *         timeTraveller.travelTo( 123456789L );
 *
 *         ...
 *     }
 *
 *
 * </pre>
 *
 *
 * Created by katzelda on 3/24/16.
 */
public final class TimeTraveller extends ExternalResource{


    private Long initialTime;

    /**
     * Create a new instance that defaults
     * to use System time until one of the travelTo methods
     * is called.
     */
    public TimeTraveller(){
        initialTime = null;
    }

    /**
     * Creates a new instance that sets the
     * time to the given epoch time in milliseconds
     * before each test.
     *
     * @param timeMillis the epoch time to set; can be negativate
     *                   if the time is before epoch start time (1970).
     *
     * @throws IllegalArgumentException if timeMillis is negative.
     */
    public TimeTraveller(long timeMillis){

        this.initialTime = timeMillis;
    }
    /**
     * Creates a new instance that sets the
     * time to the given Date
     * before each test.
     *
     * @param date the epoch date to set to; can not be null
     *
     * @throws NullPointerException if date is null.
     */
    public TimeTraveller(Date date){
        this(date.getTime());
    }

    @Override
    protected void before() throws Throwable {
       if(initialTime !=null){
           travelTo(initialTime);
       }
    }

    public void travelTo(long timeMillis){
        travelTo(timeMillis,TimeUnit.MILLISECONDS);
    }
    public void travelTo(long time, TimeUnit tu){
        TimeUtil.setCurrentTime(time,tu);
    }

    public void travelTo(Date date){
        TimeUtil.setCurrentTime(date);
    }

    /**
     * Jump to the current System time and
     * start using the normal system time until
     * another call to travelTo() or jump() is called.
     *
     * This is the same as TimeUtil.useSystemTime();
     */
    public void returnToSystemTime(){
        TimeUtil.useSystemTime();
    }

    /**
     * Jump in time forward or backward from the
     * @param amount
     * @param units
     */
    public void jump(long amount, TimeUnit units){
        long delta = units.toMillis(amount);
        long newTime = TimeUtil.getCurrentTimeMillis() + delta;

        travelTo(newTime);
    }

    @Override
    protected void after() {
        TimeUtil.useSystemTime();
    }
}
