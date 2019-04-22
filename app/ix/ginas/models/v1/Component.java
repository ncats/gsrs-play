package ix.ginas.models.v1;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonIgnore;

import ix.core.SingleParent;
import ix.ginas.models.GinasAccessReferenceControlled;
import ix.ginas.models.GinasCommonSubData;

@Entity
@Table(name="ix_ginas_component")
@Inheritance
@DiscriminatorValue("COMP")
@SingleParent
public class Component extends GinasCommonSubData {
	
	//MAY_BE_PRESENT_ONE_OF
	// or
	//MUST_BE_PRESENT
	// or
	//MAY_BE_PRESENT_ANY_OF
	
	//@NotNull
    public String type;
    
    @OneToOne(cascade=CascadeType.ALL)
    public SubstanceReference substance;

    public Component () {}

	@Override
	@JsonIgnore
	public List<GinasAccessReferenceControlled> getAllChildrenCapableOfHavingReferences() {
		List<GinasAccessReferenceControlled> temp = new ArrayList<GinasAccessReferenceControlled>();
		if(substance!=null){
			temp.addAll(substance.getAllChildrenAndSelfCapableOfHavingReferences());
		}
		return temp;
	}
}
