package ix.ginas.models.v1;

import javax.persistence.*;

import ix.core.models.Indexable;
import ix.ginas.models.GinasSubData;

@Entity
@Table(name="ix_ginas_component")
@Inheritance
@DiscriminatorValue("COMP")
public class Component extends GinasSubData {
    public String type;
    
    @OneToOne(cascade=CascadeType.ALL)
    public SubstanceReference substance;

    public Component () {}
}
