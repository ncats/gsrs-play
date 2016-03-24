package ix.ginas.models.v1;

import ix.ginas.models.GinasCommonSubData;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Entity
@Table(name="ix_ginas_polymer")
public class Polymer extends GinasCommonSubData {
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
