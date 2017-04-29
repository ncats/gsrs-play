package ix.core.plugins;
import java.io.Serializable;
import java.text.ParseException;
import java.time.DayOfWeek;
import java.time.Month;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.quartz.CronExpression;

public class CronExpressionBuilder implements Serializable {

    private static final long serialVersionUID = -1676663054009319677L;
    
    private String seconds="0";
    private String minutes="0";
    private String hours="0";
    private String dayOfMonth="*"; //every day of month by default
    private String month="*";      //every month by default
    private String dayOfWeek="?";  //
    
    private String year = "";
    
    public CronExpressionBuilder(){
        
    }
    
    public static CronExpressionBuilder from(String cron){
        String[] toks=cron.split("[ ]");
        CronExpressionBuilder cb=new CronExpressionBuilder();
        cb.seconds = toks[0];
        cb.minutes = toks[1];
        cb.hours = toks[2];
        cb.dayOfMonth = toks[3];
        cb.month = toks[4];
        cb.dayOfWeek = toks[5];
        if(toks.length>6){
            cb.year=toks[6];
        }
        return cb;
    }
    
    public CronExpressionBuilder everySecond(){
        seconds = "*";
        return this;
    }
    
    public CronExpressionBuilder everyMinute(){
        minutes = "*";
        return this;
    }
    public CronExpressionBuilder everyHour(){
        hours = "*";
        return this;
    }
    public CronExpressionBuilder everyDay(){
        dayOfMonth = "*";
        return this;
    }
    
    public CronExpressionBuilder everyMonth(){
        month = "*";
        return this;
    }
    
    public CronExpressionBuilder onlyOnDaysOfWeek(DayOfWeek ... days){
        String idays=Arrays.stream(days)
       
              .map(d->d.getValue()+"")
              .collect(Collectors.joining(","));
        dayOfWeek = idays;
        return this;
    }
    
    public CronExpressionBuilder onlyOnDayOfTheMonth(int ... dayOfTheMonth){
        String idays=Arrays.stream(dayOfTheMonth)
              .mapToObj(d-> d+"")
              .collect(Collectors.joining(","));
        dayOfMonth = idays;
        return this;
    }
    
    
    
    public CronExpressionBuilder atHourAndMinute(int hour, int minute){
        this.minutes = minute + "";
        this.hours = hour + "";
        return this;
    }
    
    public CronExpressionBuilder atMinute(int minute){
        this.minutes = minute + "";
        return this;
    }
    
    public CronExpressionBuilder atHour(int hour){
        this.hours = hour + "";
        return this;
    }
    
    public CronExpressionBuilder onlyInMonths(Month  ...  m){
        String imonths=Arrays.stream(m)
                
                .map(d->d.getValue()+"")
                .collect(Collectors.joining(","));
          month = imonths;
          return this;
    }
    
    private CronExpressionBuilder onDayOfWeek(String dow){
        this.dayOfWeek=dow;
        return this;
    }
    
    private CronExpressionBuilder onDayOfMonth(String dom){
        this.dayOfMonth=dom;
        return this;
    }
    
    public OnExpression on(){
        return new OnExpression(this);
    }
    

    public static class OnExpression{
        CronExpressionBuilder cb;
        int n;
        private OnExpression(CronExpressionBuilder cb){
            this.cb=cb;
        }
        
        public PartialOnExpression nth(int n){
            return new PartialOnExpression(cb, "#" + n);
        }
        public PartialOnExpression nthToLast(int n){
            return new PartialOnExpression(cb, "L-" + n);
        }
        public PartialOnExpression last(){
            return new PartialOnExpression(cb, "L");
        }
        public PartialOnExpression secondToLast(){
            return new PartialOnExpression(cb, "L-2");
        }
        public PartialOnExpression thirdToLast(){
            return new PartialOnExpression(cb, "L-3");
        }
        
        public PartialOnExpression first(){
            return nth(1);
        }
        public PartialOnExpression second(){
            return nth(2);
        }
        public PartialOnExpression third(){
            return nth(3);
        }
        
        public PartialOnExpression forth(){
            return nth(4);
        }
        
    }
    public static class PartialOnExpression{
        CronExpressionBuilder cb;
        String n;
        private PartialOnExpression(CronExpressionBuilder cb, String nth){
            this.cb=cb;
            this.n=nth;
        }
        
        public CronExpressionBuilder weekdayOfEveryMonth(){
            if(n.startsWith("#")){
                return cb.onDayOfMonth(n.substring(1) + "W");
            }else{
                return cb.onDayOfMonth(n + "W");
            }
            
        }
        
        public CronExpressionBuilder fridayOfEveryMonth(){
            return cb.onDayOfWeek("6" + n);
        }
        public CronExpressionBuilder saturdayOfEveryMonth(){
            return cb.onDayOfWeek("7" + n);
        }
        public CronExpressionBuilder sundayOfEveryMonth(){
            return cb.onDayOfWeek("1" + n);
        }
        public CronExpressionBuilder mondayOfEveryMonth(){
            return cb.onDayOfWeek("2" + n);
        }
        public CronExpressionBuilder tuesdayOfEveryMonth(){
            return cb.onDayOfWeek("3" + n);
        }
        public CronExpressionBuilder wednesdayOfEveryMonth(){
            return cb.onDayOfWeek("4" + n);
        }
        public CronExpressionBuilder thursdayOfEveryMonth(){
            return cb.onDayOfWeek("5" + n);
        }
        
        public CronExpressionBuilder dayOfTheMonth(){
            if(n.startsWith("#")){
                return cb.onDayOfMonth(n.substring(1));
            }else{
                return cb.onDayOfMonth(n);
            }
        }
        
    }
    
    public CronExpressionBuilder every(int n, TimeUnit tu) {
        switch (tu) {
        case DAYS:
            this.dayOfMonth = "0/" + n;
            break;
        case HOURS:
            this.hours = "0/" + n;
            break;
        case MINUTES:
            this.minutes = "0/" + n;
            break;
        case SECONDS:
            this.seconds = "0/" + n;
            break;
        default:
            break;
        }
        return this;
    }
    
    public EveryExpression every(int n) {
        return new EveryExpression(this,n);
    }
    
    public static class EveryExpression{
        CronExpressionBuilder cb;
        int n;
        private EveryExpression(CronExpressionBuilder cb, int n){
            this.cb=cb;
            this.n=n;
        }
        public CronExpressionBuilder days(){
            return cb.every(n, TimeUnit.DAYS);
        }
        public CronExpressionBuilder hours(){
            return cb.every(n, TimeUnit.HOURS);
        }
        public CronExpressionBuilder minutes(){
            return cb.every(n, TimeUnit.MINUTES);
        }
        public CronExpressionBuilder seconds(){
            return cb.every(n, TimeUnit.SECONDS);
        }
        
    }
    
    public String build(){
        return  (seconds + " "
               +minutes + " "
               +hours + " "
               +dayOfMonth + " "
               +month + " "
               +dayOfWeek + " "
               +year).trim();
    }
    
    public CronExpression buildExpression() throws ParseException{
        return new CronExpression(build());
    }
    
    
    
    
}