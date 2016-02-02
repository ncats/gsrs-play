package ix.ginas.models.v1;

import ix.ginas.models.GinasCommonSubData;
import ix.ginas.models.utils.JSONEntity;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;

@Entity
@Table(name="ix_ginas_physicalmod")
@JSONEntity(title = "Physical Modification", isFinal = true)
public class PhysicalModification extends GinasCommonSubData {
    @JSONEntity(title = "Physical Modification Role", isRequired = true)
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
