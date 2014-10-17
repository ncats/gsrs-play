package ix.ncats.models.clinical;

import play.db.ebean.Model;
import javax.persistence.*;

import ix.core.models.Indexable;

@Entity
@Table(name="ix_ncats_clinical_outcome")
public class Outcome extends Model {
    public enum Type {
        Unknown,
        Primary,
            Secondary,
            Other
            }

    @Id
    public Long id;
    public Type type = Type.Unknown;
    public String measure;
    public String timeframe;
    @Lob
    public String description;
    public boolean safetyIssue;

    public Outcome () {}
    public Outcome (Type type) {
        this.type = type;
    }
}

