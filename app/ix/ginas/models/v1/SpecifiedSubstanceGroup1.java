package ix.ginas.models.v1;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Inheritance
@DiscriminatorValue("SSI")
public class SpecifiedSubstanceGroup1 extends Substance {
    @ManyToMany(cascade = CascadeType.ALL)
    @JoinTable(name = "ix_ginas_substance_comp")
    public List<SpecifiedSubstanceComponent> specifiedSubstance =
        new ArrayList<SpecifiedSubstanceComponent>();

    public SpecifiedSubstanceGroup1() {
    }
}
