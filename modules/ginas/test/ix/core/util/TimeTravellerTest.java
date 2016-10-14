package ix.core.util;

import org.junit.After;
import org.junit.Test;

import java.time.LocalDate;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

/**
 * Created by katzelda on 10/13/16.
 */
public class TimeTravellerTest {

    LocalDate nov_5_1955 = LocalDate.of(1955, 11, 5);

    @After
    public void resetTheClock(){
        TimeUtil.useSystemTime();
    }
    @Test
    public void beforeNullConstructorUsesSystemTime() throws Throwable{
        long systemTime = System.currentTimeMillis();
        TimeTraveller tt = new TimeTraveller();
        tt.before();

        long beforeTime = TimeUtil.getCurrentTimeMillis();
        long delta = beforeTime - systemTime;
        assertTrue(delta < 1000);
    }

    @Test
    public void beforeConstructorUsesSpecificDate() throws Throwable{
        TimeTraveller tt = new TimeTraveller(nov_5_1955);
        tt.before();

        assertEquals("before", nov_5_1955, TimeUtil.getCurrentLocalDate());

        tt.jumpAhead(1, TimeUnit.DAYS);

        tt.after();

        tt.before();

        assertEquals("after", nov_5_1955, TimeUtil.getCurrentLocalDate());
    }

    @Test
    public void jump(){
        TimeTraveller tt = new TimeTraveller();
        LocalDate whereWeWere = LocalDate.now();
        tt.jumpAhead(1, TimeUnit.DAYS);

        LocalDate whereWeAre = TimeUtil.getCurrentLocalDate();
        assertEquals(whereWeAre, whereWeWere.plusDays(1));
    }

    @Test
    public void jumpBack(){
        TimeTraveller tt = new TimeTraveller();
        LocalDate whereWeWere = LocalDate.now();
        tt.jumpBack(1, TimeUnit.DAYS);

        LocalDate whereWeAre = TimeUtil.getCurrentLocalDate();
        assertEquals(whereWeAre, whereWeWere.minusDays(1));
    }

    @Test
    public void travelTo(){
        TimeTraveller tt = new TimeTraveller();

        tt.travelTo(nov_5_1955);

        assertEquals(nov_5_1955, TimeUtil.getCurrentLocalDate());
    }
}
