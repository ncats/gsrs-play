package ix.ginas.models.v1;

import gov.nih.ncgc.chemical.Chemical;
import ix.core.GinasProcessingMessage;
import ix.core.models.Group;
import ix.ginas.models.GinasAccessReferenceControlled;
import ix.ginas.models.GinasSubstanceDefinitionAccess;
import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;
import java.util.List;
import java.util.Set;

@Entity
@Inheritance
@DiscriminatorValue("POL")
public class PolymerSubstance extends Substance implements GinasSubstanceDefinitionAccess {
    @OneToOne(cascade=CascadeType.ALL)
    public Polymer polymer;

    public PolymerSubstance () {}

    @Override
    protected Chemical getChemicalImpl(List<GinasProcessingMessage> messages) {
        messages.add(GinasProcessingMessage
                .WARNING_MESSAGE("Polymer substance structure is for display, and is not complete in definition"));

        return polymer.displayStructure.toChemical(messages);
    }

    @JsonIgnore
    public GinasAccessReferenceControlled getDefinitionElement(){
        return polymer;
    }
}
