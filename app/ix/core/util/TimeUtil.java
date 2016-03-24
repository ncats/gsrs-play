package ix.core.util;

import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Created by katzelda on 3/24/16.
 */
public final class TimeUtil {

    private TimeUtil(){
        //can not instantiate
    }

    private static AtomicReference<Long> FIXED_TIME = new AtomicReference<>();

    public static Date getCurrentDate(){
        return new Date(getCurrentTimeMillis());
    }
    public static long getCurrentTimeMillis(){
        Long setTime = FIXED_TIME.get();
        if(setTime ==null){
            return System.currentTimeMillis();
        }
        return setTime.longValue();
    }

    public static void setCurrentTime(long timeMillis){
        FIXED_TIME.set(timeMillis);
    }

    public static void setCurrentTime(Date date){
        setCurrentTime(date.getTime());
    }

    public static void useSystemTime(){
        FIXED_TIME.set(null);
    }


    /**
     * Helper method to create a new Date object with the given
     * year, month and day values at midnight.  This is actually complcated to do
     * pre-Java 8 since you have to make Calendar instances and call
     * its mutator methods.
     *
     * Note: There is no checking that the day/month combination is valid.
     *
     * @param year the year (4 digit).
     * @param month the month (starting at 1).
     * @param day the day (starting at 1).
     * @return a new Date object.
     */
    public static Date toDate(int year, int month, int day){
        Calendar c = Calendar.getInstance();
        c.clear();
        c.set(year, month - 1, day);

        return c.getTime();
    }
}
