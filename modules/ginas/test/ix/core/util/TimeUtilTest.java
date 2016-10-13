package ix.core.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Calendar;
import java.util.Date;

import org.junit.Rule;
import org.junit.Test;

import ix.AbstractGinasTest;

/**
 * Created by katzelda on 3/24/16.
 */
public class TimeUtilTest extends AbstractGinasTest{

    @Rule
    public TimeTraveller timeTraveller = new TimeTraveller();

    @Test
    public void defaultToCurrentTime(){
        long now = System.currentTimeMillis();

        long actual = TimeUtil.getCurrentTimeMillis();

        ensureSameWithinDelta(now, actual);
    }
    private static void ensureSameWithinDelta(Date now, Date actual) {
        ensureSameWithinDelta(now.getTime(), actual.getTime());
    }
    private static void ensureSameWithinDelta(long now, long actual) {
        long delta = Math.abs(actual - now);
        System.out.println("Delta:" + delta);
        //1 second resolution is good enough for
        //this test.  We just care that we are close.
        assertTrue("Expected dt to be < 1000, got " + delta , delta < 1000);
    }

    @Test
    public void defaultToCurrentDate(){
        Date now = new Date();

        Date actual = TimeUtil.getCurrentDate();

        ensureSameWithinDelta(now, actual);
    }

    @Test
    public void setTime(){
        long expected = 123456789L;
        timeTraveller.travelTo(expected);

        assertEquals(expected, TimeUtil.getCurrentTimeMillis());
        assertEquals(new Date(expected), TimeUtil.getCurrentDate());

    }

    @Test
    public void setDate(){
        long expected = 123456789L;
        timeTraveller.travelTo(new Date(expected));

        assertEquals(expected, TimeUtil.getCurrentTimeMillis());
        assertEquals(new Date(expected), TimeUtil.getCurrentDate());

    }

    @Test
    public void toDate(){
        Date actual = TimeUtil.toDate(1955, 11, 5);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(actual);

        assertEquals(1955, calendar.get(Calendar.YEAR));
        //Calendar has 0-based months
        assertEquals(10, calendar.get(Calendar.MONTH));
        assertEquals(5, calendar.get(Calendar.DAY_OF_MONTH));

        assertEquals(0, calendar.get(Calendar.HOUR_OF_DAY));
        assertEquals(0, calendar.get(Calendar.MINUTE));
        assertEquals(0, calendar.get(Calendar.SECOND));
    }

}
