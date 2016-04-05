package ix.ginas.models.v1;

import javax.persistence.*;

import ix.core.SingleParent;
import ix.ginas.models.GinasCommonSubData;

@Entity
@Table(name="ix_ginas_material")
@SingleParent
public class Material extends GinasCommonSubData {

	@ManyToOne(cascade = CascadeType.PERSIST)
	private Polymer owner;
	
    @OneToOne(cascade=CascadeType.ALL)
    public Amount amount;
    @OneToOne(cascade=CascadeType.ALL)
    public SubstanceReference monomerSubstance;
    public String type;
    public Boolean defining;

    public Material () {}
}
