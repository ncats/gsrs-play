package ix.ntd.models;

import play.db.ebean.Model;
import javax.persistence.*;
import ix.core.models.Indexable;

import java.util.Date;

@Entity
@DiscriminatorValue("TRE")
public class Treatment extends Model {
    @Indexable(facet=true)

    public enum DosageUnit{
        g("g"),
        micrograms("micrograms"),
        mg("mg"),
        mcg_kg("mcg/kg"),
        mg_kg_day("mg/kg/day"),
        mg_kg("mg/kg"),
        ml("ml"),
        mol("mol");

        private String displayName;

        DosageUnit(String displayName) {
            this.displayName = displayName;
        }

        public String displayName() {
            return displayName;
        }

    }

    public enum FrequencyUnit{
        Hourly,
        Daily,
        Weekly,
        Monthly,
        Yearly,
        Other
    }

    public enum DurationUnit{
        Hours,
        Days,
        Weeks,
        Months,
        Years,
        Other
    }

    public enum Route{
        Oral,
        Tablet,
        Inhalation,
        Injection,
        IV,
        Topical,
        Other
    }

    public String treatmentName;
    public Integer dose;
    public Integer treatmentDuration;
    public Integer frequency;
    public Integer regimenID;

    public DosageUnit dosageUnit;
    public FrequencyUnit frequencyUnit;
    public DurationUnit durationUnit;
    public Route route;
    @Lob
    public String treatmentNotes;


    public Treatment(){
    }
}