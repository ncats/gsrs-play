package ix.ginas.models;

import java.util.List;
import java.util.ArrayList;
import javax.persistence.*;

import ix.core.models.Indexable;

@Entity
@DiscriminatorValue("PRO")
public class Protein extends Substance {
    @Column(length=32)
    @Indexable(name="Protein Type", facet=true)
    public String proteinType;
    
    @Column(length=128)
    @Indexable(name="Sequence Origin", facet=true)
    public String sequenceOrigin;
    
    @Column(length=32)
    @Indexable(name="Sequence Type", facet=true)
    public String sequenceType;

    @ManyToMany(cascade=CascadeType.ALL)
    @JoinTable(name="ix_ginas_protein_subunit")
    public List<Subunit> subunits = new ArrayList<Subunit>();
    
    public Protein () {
	super (SubstanceClass.Protein);
    }
}
