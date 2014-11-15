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
            ActiveComparator,
            PlaceboComparator,
            ShamComparator,
            NoIntervention,
            Other;

        public static Type forName (String name) {
            if ("Experimental".equalsIgnoreCase(name))
                return Experimental;
            if ("Active Comparator".equalsIgnoreCase(name))
                return ActiveComparator;
            if ("Placebo Comparator".equalsIgnoreCase(name))
                return PlaceboComparator;
            if ("Sham Comparator".equalsIgnoreCase(name))
                return ShamComparator;
            if ("No intervention".equalsIgnoreCase(name))
                return NoIntervention;
            return Other;
        }
    }

    @Id
    public Long id;
    @Indexable(facet=true,suggest=true,name="Clinical Arm")
    public String label;
    @Lob
    public String description;
    public String type;

    public Arm () {}
    public Arm (String type) { this.type = type; }
}
