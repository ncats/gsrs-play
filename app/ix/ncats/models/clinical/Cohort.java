package ix.ncats.models.clinical;

import play.db.ebean.Model;
import javax.persistence.*;

import ix.core.models.Indexable;

@Entity
@Table(name="ix_ncats_clinical_cohort")
public class Cohort extends Model {
    @Id
    public Long id;
    @Indexable(facet=true,suggest=true,name="Clinical Cohort")
    public String label;
    @Lob
    public String description;

    public Cohort () {}
}
