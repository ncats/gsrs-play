package ix.ginas.models;

import java.util.List;
import java.util.ArrayList;
import javax.persistence.*;

import ix.core.models.Indexable;
import ix.core.models.XRef;
import ix.core.models.Structure;

@Entity
@DiscriminatorValue("CHE")
public class Chemical extends Substance {
    @OneToOne(cascade=CascadeType.ALL)
    public Structure structure;

    /*
    @ManyToMany(cascade=CascadeType.ALL)
    @JoinTable(name="ix_ginas_substance_moiety")
    public List<XRef> moieties = new ArrayList<XRef>();
    */
    
    public Chemical () {
        super (SubstanceClass.Chemical);
    }

    /*
    @Indexable(name="Moiety Count", facet=true)
    public int getMoietyCount () {
        return moieties.size();
    }
    */
}
