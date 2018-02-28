package ix.core.plugins;

import ix.core.util.TimeUtil;
import play.Application;
import play.Play;
import play.Plugin;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class TimeTravellerPlugin  extends Plugin {

    public TimeTravellerPlugin (Application app){
        String date = app.configuration().getString("ix.timeTraveller.jumpTo");
//        System.out.println("TIME TRAVELLING TO " + date);
        if(date !=null) {
            DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE;
            TimeUtil.setCurrentTime( LocalDate.parse(date, formatter));
        }
    }

}
