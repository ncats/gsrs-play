package ix.ncats.models.clinical;

import play.db.ebean.Model;
import javax.persistence.*;

import ix.core.models.Indexable;

@Entity
@Table(name="ix_ncats_clinical_arm")
public class Arm extends Model {
    public enum Type {
        Unknown,
        Experimental,
            Active_Comparator,
            Placebo_Comparator,
            Sham_Comparator,
            No_Intervention,
            Other
            }

    @Id
    public Long id;
    @Indexable(facet=true,suggest=true,name="Clinical Arm")
    public String label;
    @Lob
    public String description;
    public Type type = Type.Unknown;

    public Arm () {}
    public Arm (Type type) { this.type = type; }
}
