package ix.ginas.models.v1;

import javax.persistence.*;

import ix.ginas.models.Ginas;

@Entity
@Table(name="ix_ginas_material")
public class Material extends Ginas {
    @OneToOne(cascade=CascadeType.ALL)
    public Amount amount;
    @OneToOne(cascade=CascadeType.ALL)
    public SubstanceReference monomerSubstance;
    public String type;
    public Boolean defining;

    public Material () {}
}
