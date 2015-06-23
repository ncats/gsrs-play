package ix.ntd.models;

import ix.ntd.models.Disease;
import play.db.ebean.Model;
import javax.persistence.*;
import ix.core.models.Indexable;

import java.util.Date;

@Entity
@DiscriminatorValue("PAT")
public class Patient extends Model {
    @Id
    public Long id;

    public final Date created = new Date ();
    public Date lastModified;


    @Indexable(facet=true)
    public enum Age {
        Infant,
        Child,
        Adult,
        Older_Adult
    }

    @Indexable(facet=true)
    public enum Gender {
        Male,
        Female,
        Other
    }

    @Indexable(facet=true)
    public enum Country{

    }

    @Indexable(facet=true)
    public enum Condition {
        HIV_Positive,
        Pregnant,
        Diabetic,
        Transplant,
        Immunosuppressed,
        Cancer,
        Other
    }


    public boolean surgery;
    public boolean previousFailure;

    @Indexable(facet=true)
    public Disease disease;

    public Reference reference;

    public Outcome outcome;

    public Patient(){
    }
}
