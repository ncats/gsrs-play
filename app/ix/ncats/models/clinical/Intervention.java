package ix.ncats.models.clinical;

import java.util.List;
import java.util.ArrayList;

import play.db.ebean.Model;
import javax.persistence.*;

import ix.core.models.Indexable;
import ix.core.models.Keyword;

@Entity
@Table(name="ix_ncats_clinical_intervention")
public class Intervention extends Model {
    public enum Type {
        Drug,
            Device,
            Biological, // vaccine
            Procedure, // surgery
            Radiation,
            Behavioral,
            Genetic,
            Dietary,
            Other
            }

    @Id
    public Long id;

    @Indexable(suggest=true,facet=true,name="Clinical Intervention")
    public String name;
    @Lob
    public String description;

    @ManyToMany(cascade=CascadeType.ALL)
    @JoinTable(name="ix_ncats_clinical_intervention_synonym")
    public List<Keyword> synonyms = new ArrayList<Keyword>();

    @Indexable(facet=true,name="Clinical Intervention Type")
    public Type type;

    @ManyToMany(cascade=CascadeType.ALL)
    @JoinTable(name="ix_ncats_clinical_intervention_arm")
    public List<Arm> arms = new ArrayList<Arm>();

    @ManyToMany(cascade=CascadeType.ALL)
    @JoinTable(name="ix_ncats_clinical_intervention_cohort")
    public List<Cohort> cohorts = new ArrayList<Cohort>();

    public Intervention () {}
}
