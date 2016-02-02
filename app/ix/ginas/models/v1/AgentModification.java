package ix.ginas.models.v1;

import ix.ginas.models.GinasCommonSubData;
import ix.ginas.models.utils.JSONEntity;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@JSONEntity(title = "Agent Modification", isFinal = true)
@Entity
@Table(name="ix_ginas_agentmod")
public class AgentModification extends GinasCommonSubData {
    @JSONEntity(title = "Process")
    public String agentModificationProcess;
    
    @JSONEntity(title = "Role")
    public String agentModificationRole;
    
    @JSONEntity(title = "Type", isRequired = true)
    public String agentModificationType;
    
    @JSONEntity(title = "Agent Material", isRequired = true)
    @OneToOne(cascade=CascadeType.ALL)
    public SubstanceReference agentSubstance;
    
    @OneToOne(cascade=CascadeType.ALL)
    public Amount amount;
    @JSONEntity(title = "Modification Group")
    public String modificationGroup = "1";
    public AgentModification () {}
}
