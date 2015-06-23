package ix.ginas.models.v1;

import java.util.List;
import java.util.ArrayList;

import javax.persistence.*;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.annotation.JsonProperty;

import ix.core.models.Indexable;
import ix.ginas.models.utils.JSONEntity;
import ix.ginas.models.Ginas;

@Entity
@Table(name="ix_ginas_structural_modification")
@JSONEntity(title = "Structural Modification", isFinal = true)
public class StructuralModification extends Ginas {
    @JSONEntity(title = "Modification Type", isRequired = true)
    @Column(nullable=false)
    public String structuralModificationType;
    
    @JSONEntity(title = "Modification Location Type")
    public String locationType;
    
    @JSONEntity(title = "Residue Modified")
    public String residueModified;
    
    @JSONEntity(title = "Modified Sites", format = "table", itemsTitle = "Site")
    @ManyToMany(cascade=CascadeType.ALL)
    @JoinTable(name="ix_ginas_structural_modification_1")
    public List<Site> sites = new ArrayList<Site>();
    
    @JSONEntity(title = "Extent", values = "JSONConstants.ENUM_EXTENT", isRequired = true)
    public String extent;

    @OneToOne(cascade=CascadeType.ALL)
    public Amount extentAmount;
    @OneToOne(cascade=CascadeType.ALL)
    public SubstanceReference molecularFragment;
    
    @JSONEntity(title = "Modification Group")
	String modificationGroup = "1";
    public StructuralModification () {}
}
