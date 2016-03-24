package ix.ntd.models;

import ix.core.util.TimeUtil;
import ix.ntd.models.Disease;
import play.db.ebean.Model;
import javax.persistence.*;
import ix.core.models.Indexable;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@DiscriminatorValue("PAT")
public class Patient extends Model {
    @Id
    public Long id;

    public final Date created = TimeUtil.getCurrentDate();
    public Date lastModified;

    public enum Age {
        Infant(">1 year"),
        Child("1 to 17 years"),
        Adult("18 to 64"),
        Older_Adult("65 or older");

        private String displayName;

        Age(String displayName) {
            this.displayName = displayName;
        }

        public String displayName() {
            return displayName;
        }

        }
    public enum Gender {
        Female,
        Male,
        Other
    }

    public enum Condition {
        HIV_Positive("HIV+"),
        Pregnant("Pregnant"),
        Diabetic("Diabetic"),
        Transplant("Transplant"),
        Immunosuppressed("Immunosuppressed"),
        Cancer("Cancer");

        private String displayName;

        Condition(String displayName) {
            this.displayName = displayName;
        }

        public String displayName() {
            return displayName;
        }
    }

    public enum Country{
Other
    }

    @Indexable(facet=true)
    public Age age;
    @Indexable(facet=true)
    public Gender gender;
    @Indexable(facet=true)
    public Condition condition;
    @Indexable(facet=true)
    public Country country;

    public boolean surgery;
    public boolean previousFailure;


    @Lob
    public String patientNotes;

    public Patient(){
    }
}
