package ix.ginas.models.v1;

import javax.persistence.*;

@Entity
@Inheritance
@DiscriminatorValue("PRO")
public class ProteinSubstance extends Substance {
    @OneToOne(cascade=CascadeType.ALL)
    public Protein protein;

    public ProteinSubstance () {}
}
