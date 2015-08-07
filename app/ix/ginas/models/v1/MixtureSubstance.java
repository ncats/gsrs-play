package ix.ginas.models.v1;

import javax.persistence.*;

import java.util.List;
import java.util.ArrayList;

@Entity
@Inheritance
@DiscriminatorValue("MIX")
public class MixtureSubstance extends Substance {
	@OneToOne(cascade=CascadeType.ALL)
    public Mixture mixture;
	
    public MixtureSubstance () {}
}
