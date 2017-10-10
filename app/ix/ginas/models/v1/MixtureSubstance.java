package ix.ginas.models.v1;

import javax.persistence.*;

import com.fasterxml.jackson.annotation.JsonIgnore;
import ix.core.models.Group;
import ix.ginas.models.GinasAccessReferenceControlled;
import ix.ginas.models.GinasSubstanceDefinitionAccess;

import java.util.List;
import java.util.ArrayList;
import java.util.Set;

@Entity
@Inheritance
@DiscriminatorValue("MIX")
public class MixtureSubstance extends Substance implements GinasSubstanceDefinitionAccess {
	@OneToOne(cascade=CascadeType.ALL)
    public Mixture mixture;
	
    public MixtureSubstance () {
    	super(SubstanceClass.mixture);
    }
    
    
    @JsonIgnore
	public List<SubstanceReference> getDependsOnSubstanceReferences(){
    	
    	List<SubstanceReference> sref = new ArrayList<SubstanceReference>();
    	sref.addAll(super.getDependsOnSubstanceReferences());
    	for(Component c:mixture.getMixture()){
			sref.add(c.substance);
		}
    	
		return sref;
	}
    @Override
    public void delete(){
    	super.delete();
    	for(Component c:mixture.components){
    		c.delete();
    	}
    	
    }

	@JsonIgnore
	public GinasAccessReferenceControlled getDefinitionElement(){
		return mixture;
	}
	
}
