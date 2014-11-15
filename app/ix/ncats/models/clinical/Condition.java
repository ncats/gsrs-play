package ix.ncats.models.clinical;

import java.util.List;
import java.util.ArrayList;

import play.db.ebean.Model;
import javax.persistence.*;

import ix.core.models.Indexable;
import ix.core.models.Keyword;

@Entity
@Table(name="ix_ncats_clinical_condition")
public class Condition extends Model {
    static private final String JOIN = "_ix_ncats_cca46885";

    @Id
    public Long id;

    @Indexable(suggest=true,facet=true,name="Clinical Condition")
    @Column(unique=true,length=1024)
    public String name;

    @Indexable(facet=true, name="Clinical Rare Disease")
    public boolean isRareDisease;

    /**
     * Note that here because there exist multiple joins with Keyword, we
     * need to explicitly define the join columns so as to distinguish 
     * between different properties. 
     */

    @ManyToMany(cascade=CascadeType.ALL)
    @JoinTable(name=JOIN+"_1",
               joinColumns=@JoinColumn
               (name="ix_ncats_clinical_condition_synonym_id",
               referencedColumnName="id")
    )
    public List<Keyword> synonyms = new ArrayList<Keyword>();

    @ManyToMany(cascade=CascadeType.ALL)
    @JoinTable(name=JOIN+"_2",
               joinColumns=@JoinColumn
               (name="ix_ncats_clinical_condition_keyword_id",
               referencedColumnName="id")
    )
    public List<Keyword> keywords = new ArrayList<Keyword>();

    @ManyToMany(cascade=CascadeType.ALL)
    @JoinTable(name=JOIN+"_3",
               joinColumns=@JoinColumn
               (name="ix_ncats_clinical_condition_wikipedia_id",
               referencedColumnName="id")
    )
    public List<Keyword> wikipedia = new ArrayList<Keyword>();

    public Condition () {}
    public Condition (String name) { this.name = name; }
}
