package ix.ginas.models.v1;

import javax.persistence.*;
import java.util.List;
import java.util.ArrayList;

@Entity
@Inheritance
@DiscriminatorValue("MIX")
public class MixtureSubstance extends Substance {
    @ManyToMany(cascade=CascadeType.ALL)
    @JoinTable(name="ix_ginas_substance_component")
    public List<Component> mixture = new ArrayList<Component>();

    public MixtureSubstance () {}
}
