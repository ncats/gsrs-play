package ix.ginas.models.v1;

import ix.core.models.Group;
import ix.ginas.models.GinasAccessReferenceControlled;
import ix.ginas.models.GinasSubstanceDefinitionAccess;
import ix.ginas.models.v1.Substance.SubstanceClass;

import com.fasterxml.jackson.annotation.JsonIgnore;
import javax.persistence.*;

import java.util.List;
import java.util.Set;

@Entity
@Inheritance
@DiscriminatorValue("DIV")
public class StructurallyDiverseSubstance extends Substance implements GinasSubstanceDefinitionAccess{
    @OneToOne(cascade=CascadeType.ALL)
    public StructurallyDiverse structurallyDiverse;

    public StructurallyDiverseSubstance () {
    	super(SubstanceClass.structurallyDiverse);
        
    }
    
    @Override
    public void delete(){
    	super.delete();
    }

    @JsonIgnore
    public GinasAccessReferenceControlled getDefinitionElement(){
        return structurallyDiverse;
    }

    @Override
	@JsonIgnore
	public List<GinasAccessReferenceControlled> getAllChildrenCapableOfHavingReferences(){
		List<GinasAccessReferenceControlled> temp = super.getAllChildrenCapableOfHavingReferences();
		if(this.structurallyDiverse!=null){
			temp.addAll(this.structurallyDiverse.getAllChildrenAndSelfCapableOfHavingReferences());
		}
		return temp;
	}
}
