package ix.ginas.models.v1;

import java.util.List;
import java.util.ArrayList;
import javax.persistence.*;

import ix.core.models.Structure;
import ix.ginas.models.GinasSubData;

@Entity
@Table(name="ix_ginas_polymer")
public class Polymer extends GinasSubData {
    @OneToOne(cascade=CascadeType.ALL)
    public PolymerClassification classification;

    @OneToOne(cascade=CascadeType.ALL)
    public GinasChemicalStructure displayStructure;
    @OneToOne(cascade=CascadeType.ALL)
    public GinasChemicalStructure idealizedStructure;

    @ManyToMany(cascade=CascadeType.ALL)
    @JoinTable(name="ix_ginas_polymer_material")
    public List<Material> monomers = new ArrayList<Material>();

    @ManyToMany(cascade=CascadeType.ALL)
    @JoinTable(name="ix_ginas_polymer_unit")
    public List<Unit> structuralUnits = new ArrayList<Unit>();

    public Polymer () {}
}
