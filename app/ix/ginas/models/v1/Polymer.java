package ix.ginas.models.v1;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import ix.ginas.models.GinasCommonSubData;

@Entity
@Table(name="ix_ginas_polymer")
public class Polymer extends GinasCommonSubData {
    @OneToOne(cascade=CascadeType.ALL)
    public PolymerClassification classification;

    @OneToOne(cascade=CascadeType.ALL)
    public GinasChemicalStructure displayStructure;
    
    @OneToOne(cascade=CascadeType.ALL)
    public GinasChemicalStructure idealizedStructure;

    @OneToMany(mappedBy = "owner", cascade = CascadeType.ALL)
    public List<Material> monomers = new ArrayList<Material>();

    @OneToMany(mappedBy = "owner", cascade = CascadeType.ALL)
    public List<Unit> structuralUnits = new ArrayList<Unit>();

    public Polymer () {}
    
    
}
