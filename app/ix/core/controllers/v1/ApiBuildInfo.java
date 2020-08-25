package ix.core.controllers.v1;

import com.fasterxml.jackson.databind.JsonNode;
import ix.BuildInfo;
import ix.core.controllers.EntityFactory;
import play.mvc.Controller;
import play.mvc.Result;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.Date;

public class ApiBuildInfo extends Controller {


    public static Result info(){
        return ok(EntityFactory.EntityMapper.FULL_ENTITY_MAPPER().toJson(new InfoResult(BuildInfo.VERSION,
                BuildInfo.COMMIT,
                LocalDate.parse(BuildInfo.DATE, DateTimeFormatter.BASIC_ISO_DATE),
                BuildInfo.TIME
                )));
    }

    public static class InfoResult{
        private final String version;
        private final String commit;
        private final String buildDate;
        public final String buildTime;

        public String getVersion() {
            return version;
        }

        public String getCommit() {
            return commit;
        }

        public String getBuildDate() {
            return buildDate;
        }

        public String getBuildTime(){
            return buildTime;
        }
        public InfoResult(String version, String commit, LocalDate buildDate, String buildTime) {
            this.version = version;
            this.commit = commit;
            this.buildDate = DateTimeFormatter.ISO_DATE.format(buildDate);
            this.buildTime = buildTime;
        }
    }
}
