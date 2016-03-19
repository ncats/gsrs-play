package ix.ginas.models.v1;

import javax.persistence.CascadeType;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.OneToOne;
import javax.persistence.Transient;

@SuppressWarnings("serial")
@Entity
@Inheritance
@DiscriminatorValue("PRO")
public class ProteinSubstance extends Substance {

	@OneToOne(cascade=CascadeType.ALL)
    public Protein protein;

    
    public ProteinSubstance () {
        super (SubstanceClass.protein);
    }
    @Override
    public boolean hasModifications(){
    	if(this.protein.modifications!=null){
    		if(this.protein.modifications.agentModifications.size()>0 || this.protein.modifications.physicalModifications.size()>0 || this.protein.modifications.structuralModifications.size()>0){
    			return true;
    		}
    	}
		return false;
    }
    @Override
    public int getModificationCount(){
    	int ret=0;
    	if(this.protein.modifications!=null){
    		ret+=this.protein.modifications.agentModifications.size();
    		ret+=this.protein.modifications.physicalModifications.size();
    		ret+=this.protein.modifications.structuralModifications.size();
    	}
    	return ret;
    }
    
    
    @Override
    public Modifications getModifications(){
    	return this.protein.modifications;
    }
    
    
    @Transient
    private boolean _dirtyModifications=false;
    
    
    public void setModifications(Modifications m){
    	if(this.protein==null){
    		this.protein = new Protein();
    		_dirtyModifications=true;
    	}
    	this.protein.setModifications(m);
    	this.modifications=m;
    }
    
    public void setProtein(Protein p){
    	this.protein=p;
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
    
    
}
