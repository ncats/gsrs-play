package ix.ginas.models.v1;

import javax.persistence.*;

@Entity
@Inheritance
@DiscriminatorValue("POL")
public class PolymerSubstance extends Substance {
    @OneToOne(cascade=CascadeType.ALL)
    public Polymer polymer;

    public PolymerSubstance () {}
}
