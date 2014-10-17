package ix.core.models;

import play.db.ebean.Model;
import javax.persistence.*;
import java.util.List;
import java.util.ArrayList;

@Entity
@Table(name="ix_core_gene")
public class Gene extends Model {
    @Id
    public Long id;

    @Indexable(facet=true,suggest=true,name="Gene")
    public String name;
    
    @ManyToMany(cascade=CascadeType.ALL)
    @JoinTable(name="ix_core_gene_synonym")
    public List<Keyword> synonyms = new ArrayList<Keyword>();

    public Gene () {}
}
