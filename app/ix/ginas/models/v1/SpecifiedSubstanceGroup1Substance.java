package ix.ginas.models.v1;

import javax.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Inheritance
@DiscriminatorValue("SSI")
public class SpecifiedSubstanceGroup1Substance extends Substance {
	@OneToOne(cascade=CascadeType.ALL)
    public SpecifiedSubstanceGroup1 specifiedSubstance;

    public SpecifiedSubstanceGroup1Substance() {
    }
}
