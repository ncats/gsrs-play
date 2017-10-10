package ix.ginas.models.v1;

import ix.ginas.models.GinasAccessReferenceControlled;
import ix.ginas.models.GinasSubstanceDefinitionAccess;
import ix.ginas.models.v1.Substance.SubstanceClass;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Inheritance
@DiscriminatorValue("SSI")
public class SpecifiedSubstanceGroup1Substance extends Substance implements GinasSubstanceDefinitionAccess{
	@OneToOne(cascade=CascadeType.ALL)
    public SpecifiedSubstanceGroup1 specifiedSubstance;

    public SpecifiedSubstanceGroup1Substance() {
    	 super (SubstanceClass.specifiedSubstanceG1);
    }

    @JsonIgnore
    public GinasAccessReferenceControlled getDefinitionElement(){
        return specifiedSubstance;
    }
}
