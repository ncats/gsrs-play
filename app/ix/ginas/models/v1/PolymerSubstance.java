package ix.ginas.models.v1;

import gov.nih.ncgc.chemical.Chemical;
import ix.core.GinasProcessingMessage;

import javax.persistence.*;
import java.util.List;

@Entity
@Inheritance
@DiscriminatorValue("POL")
public class PolymerSubstance extends Substance {
    @OneToOne(cascade=CascadeType.ALL)
    public Polymer polymer;

    public PolymerSubstance () {}

    @Override
    protected Chemical getChemicalImpl(List<GinasProcessingMessage> messages) {
        messages.add(GinasProcessingMessage
                .WARNING_MESSAGE("Polymer substance structure is for display, and is not complete in definition"));

        return polymer.displayStructure.toChemical(messages);
    }
}
