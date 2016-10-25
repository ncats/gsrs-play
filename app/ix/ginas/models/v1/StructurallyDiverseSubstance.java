package ix.ginas.models.v1;

import ix.core.models.Group;
import ix.ginas.models.GinasAccessReferenceControlled;
import ix.ginas.models.GinasSubstanceDefinitionAccess;

import javax.persistence.*;
import java.util.Set;

@Entity
@Inheritance
@DiscriminatorValue("DIV")
public class StructurallyDiverseSubstance extends Substance implements GinasSubstanceDefinitionAccess{
    @OneToOne(cascade=CascadeType.ALL)
    public StructurallyDiverse structurallyDiverse;

    public StructurallyDiverseSubstance () {}
    
    @Override
    public void delete(){
    	super.delete();
    }

    //@JsonIgnore
    public GinasAccessReferenceControlled getDefinitionElement(){
        return structurallyDiverse;
    }
}
