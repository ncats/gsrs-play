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
@Table(name="ix_ginas_physicalmod")
@JSONEntity(title = "Physical Modification", isFinal = true)
public class PhysicalModification extends Ginas {
    @JSONEntity(title = "SecurityRole of Modification", isRequired = true)
    @Column(nullable=false)
    public String physicalModificationRole;
    
    @JSONEntity(title = "Physical Parameters", isRequired = true, minItems = 1)
    @ManyToMany(cascade=CascadeType.ALL)
    @JoinTable(name="ix_ginas_physical_modparam")
    public List<PhysicalParameter> parameters =
        new ArrayList<PhysicalParameter>();
    @JSONEntity(title = "Modification Group")
        String modificationGroup = "1";
    public PhysicalModification () {}
}
