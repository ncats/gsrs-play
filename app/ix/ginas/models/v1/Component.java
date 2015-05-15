package ix.ginas.models.v1;

import javax.persistence.*;

import ix.core.models.Indexable;
import ix.ginas.models.Ginas;

@Entity
@Table(name="ix_ginas_component")
public class Component extends Ginas {
    public String type;
    
    @OneToOne(cascade=CascadeType.ALL)
    public SubstanceReference substance;

    public Component () {}
}
