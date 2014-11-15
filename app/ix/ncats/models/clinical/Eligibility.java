package ix.ncats.models.clinical;

import java.util.List;
import java.util.ArrayList;

import play.db.ebean.Model;
import javax.persistence.*;

import ix.core.models.Indexable;
import ix.core.models.Keyword;

@Entity
@Table(name="ix_ncats_clinical_eligibility")
public class Eligibility extends Model {
    static private final String JOIN = "_ix_ncats_840372f9";
    @Id
    public Long id;

    @Column(length=32)
    @Indexable(facet=true, name="Eligibility Gender")
    public String gender;

    @Indexable(facet=true, name="Eligibility Min Age")
    public String minAge;

    @Indexable(facet=true, name="Eligibility Max Age")
    public String maxAge;

    public boolean healthyVolunteers;

    @ManyToMany(cascade=CascadeType.ALL)
    @JoinTable(name=JOIN+"_1",
               joinColumns=@JoinColumn
               (name="ix_ncats_clinical_eligibility_inclusion_id",
               referencedColumnName="id")
    )
    public List<Keyword> inclusions = new ArrayList<Keyword>();

    @ManyToMany(cascade=CascadeType.ALL)
    @JoinTable(name=JOIN+"_2",
               joinColumns=@JoinColumn
               (name="ix_ncats_clinical_eligibility_exclusion_id",
               referencedColumnName="id")
    )
    public List<Keyword> exclusions = new ArrayList<Keyword>();

    public Eligibility () {}
}
