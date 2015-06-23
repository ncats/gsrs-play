package ix.ntd.models;

import play.db.ebean.Model;
import javax.persistence.*;
import ix.core.models.Indexable;

import java.util.Date;

@Entity
@DiscriminatorValue("TRE")
public class Treatment extends Model {
    @Indexable(facet=true)
    public String treatmentName;

    public Integer dose;
    public enum DosageUnit{
        g,
        micrograms,
        mg,
        mcg_kg,
        mg_kg_day,
        mg_kg,
        ml,
        mol,
        Other

    }
    public Integer frequency;
    public enum FrequencyUnit{
        Hourly,
        Daily,
        Weekly,
        Monthly,
        Yearly,
        Other
    }
    public Integer treatmentDuration;
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

    public Integer regimenID;

    @Lob
    public String treatmentNotes;


    public Treatment(){
    }
}