package ix.ginas.models.v1;

import javax.persistence.*;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.List;
import java.util.ArrayList;

@Entity
@Inheritance
@DiscriminatorValue("MIX")
public class MixtureSubstance extends Substance {
	@OneToOne(cascade=CascadeType.ALL)
    public Mixture mixture;
	
    public MixtureSubstance () {}
    
    
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
}
