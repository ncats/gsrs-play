package ix.ginas.models.v1;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.OneToOne;
import javax.persistence.Transient;

import com.fasterxml.jackson.annotation.JsonIgnore;

import ix.ginas.models.GinasAccessReferenceControlled;
import ix.ginas.models.GinasSubstanceDefinitionAccess;

@SuppressWarnings("serial")
@Entity
@Inheritance
@DiscriminatorValue("PRO")
public class ProteinSubstance extends Substance implements GinasSubstanceDefinitionAccess {

	@OneToOne(cascade=CascadeType.ALL)
    public Protein protein;

    
    public ProteinSubstance () {
        super (SubstanceClass.protein);
    }
    @Override
    public boolean hasModifications(){
    	if(this.modifications!=null){
    		if(this.modifications.agentModifications.size()>0 || this.modifications.physicalModifications.size()>0 || this.modifications.structuralModifications.size()>0){
    			return true;
    		}
    	}
		return false;
    }
    @Override
    public int getModificationCount(){
    	int ret=0;
    	if(this.modifications!=null){
    		ret+=this.modifications.agentModifications.size();
    		ret+=this.modifications.physicalModifications.size();
    		ret+=this.modifications.structuralModifications.size();
    	}
    	return ret;
    }
    
    
    @Override
    public Modifications getModifications(){
    	return this.modifications;
    }
    
    
    @Transient
    private boolean _dirtyModifications=false;
    
    
    
    public void setModifications(Modifications m){
    	if(this.protein==null){
    		this.protein = new Protein();
    		_dirtyModifications=true;
    	}
    	this.modifications=m;
    	this.protein.setModifications(m);
    }
    
    public void setProtein(Protein p){
    	this.protein=p;
    	this.protein.setProteinSubstance(this);
    	if(_dirtyModifications){
    		this.protein.setModifications(this.modifications);
    		_dirtyModifications=false;
    	}
    }
    
    @Override
    public void delete(){
    	super.delete();
    	for(Subunit su:this.protein.subunits){
    		su.delete();
    	}
    	//protein.delete();
    }

	@JsonIgnore
	public GinasAccessReferenceControlled getDefinitionElement(){
		return protein;
	}
    
	@Override
	@JsonIgnore
	public List<GinasAccessReferenceControlled> getAllChildrenCapableOfHavingReferences(){
		List<GinasAccessReferenceControlled> temp = super.getAllChildrenCapableOfHavingReferences();
		if(this.protein!=null){
			temp.addAll(this.protein.getAllChildrenAndSelfCapableOfHavingReferences());
		}
		return temp;
	}


}
