package ix.ginas.models.v1;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnore;

import ix.core.SingleParent;
import ix.ginas.models.GinasAccessReferenceControlled;
import ix.ginas.models.GinasCommonSubData;
import ix.ginas.models.utils.JSONEntity;

@JSONEntity(title = "Agent Modification", isFinal = true)
@Entity
@Table(name="ix_ginas_agentmod")
@SingleParent
public class AgentModification extends GinasCommonSubData {
	@ManyToOne(cascade = CascadeType.PERSIST)
	private Modifications owner;
	
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

    @Override
    @JsonIgnore
	public List<GinasAccessReferenceControlled> getAllChildrenCapableOfHavingReferences() {
		List<GinasAccessReferenceControlled> temp = new ArrayList<GinasAccessReferenceControlled>();
		if(amount!=null){
			temp.addAll(amount.getAllChildrenAndSelfCapableOfHavingReferences());
		}
		if(agentSubstance!=null){
			temp.addAll(agentSubstance.getAllChildrenAndSelfCapableOfHavingReferences());
		}
		return temp;
	}
}
