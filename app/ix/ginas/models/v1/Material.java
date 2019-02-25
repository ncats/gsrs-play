package ix.ginas.models.v1;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.*;

import com.fasterxml.jackson.annotation.JsonIgnore;

import ix.core.SingleParent;
import ix.ginas.models.GinasAccessReferenceControlled;
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

    @Override
	@JsonIgnore
	public List<GinasAccessReferenceControlled> getAllChildrenCapableOfHavingReferences() {
		List<GinasAccessReferenceControlled> temp = new ArrayList<GinasAccessReferenceControlled>();
		if(amount!=null){
			temp.addAll(amount.getAllChildrenAndSelfCapableOfHavingReferences());
		}
		if(monomerSubstance!=null){
			temp.addAll(monomerSubstance.getAllChildrenAndSelfCapableOfHavingReferences());
		}
		return temp;
	}
}
