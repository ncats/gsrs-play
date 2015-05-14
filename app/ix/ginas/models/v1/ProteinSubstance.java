package ix.ginas.models.v1;

import javax.persistence.*;

@Entity
@Inheritance
@DiscriminatorValue("PRO")
public class ProteinSubstance extends Substance {
    @OneToOne
    public Protein protein;

    public ProteinSubstance () {}
}
