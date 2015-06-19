package ix.ginas.models.v1;

import ix.ginas.models.Ginas;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Entity
@Table(name="ix_ginas_specifiedsubstancecomponent")
public class SpecifiedSubstanceComponent extends Component {
    public String role;
    public Amount amount;

    public SpecifiedSubstanceComponent() {}
}
