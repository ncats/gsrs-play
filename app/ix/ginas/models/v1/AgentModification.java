package ix.ginas.models.v1;

import javax.persistence.*;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.annotation.JsonProperty;

import ix.core.models.Indexable;
import ix.ginas.models.utils.JSONEntity;
import ix.ginas.models.Ginas;

@JSONEntity(title = "Agent Modification", isFinal = true)
@Entity
@Table(name="ix_ginas_agentmod")
public class AgentModification extends Ginas {
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
        String modificationGroup = "1";
    public AgentModification () {}
}
