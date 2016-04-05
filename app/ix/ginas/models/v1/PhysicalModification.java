package ix.ginas.models.v1;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import ix.core.SingleParent;
import ix.ginas.models.GinasCommonSubData;
import ix.ginas.models.utils.JSONEntity;

@Entity
@Table(name="ix_ginas_physicalmod")
@JSONEntity(title = "Physical Modification", isFinal = true)
@SingleParent
public class PhysicalModification extends GinasCommonSubData {
	@ManyToOne(cascade = CascadeType.PERSIST)
	private Modifications owner;
    @JSONEntity(title = "Physical Modification Role", isRequired = true)
    public String physicalModificationRole;
    
    @JSONEntity(title = "Physical Parameters", isRequired = true, minItems = 1)
    @OneToMany(mappedBy = "owner", cascade = CascadeType.ALL)
    public List<PhysicalParameter> parameters =
        new ArrayList<PhysicalParameter>();
    
    @JSONEntity(title = "Modification Group")
    String modificationGroup = "1";
    public PhysicalModification () {}
}
