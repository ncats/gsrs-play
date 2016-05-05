package ix.ginas.models.v1;

import javax.persistence.CascadeType;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import ix.core.SingleParent;
import ix.ginas.models.GinasCommonSubData;

@Entity
@Table(name="ix_ginas_component")
@Inheritance
@DiscriminatorValue("COMP")
@SingleParent
public class Component extends GinasCommonSubData {
	
    public String type;
    
    @OneToOne(cascade=CascadeType.ALL)
    public SubstanceReference substance;

    public Component () {}
}
