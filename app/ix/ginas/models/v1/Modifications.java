package ix.ginas.models.v1;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnore;

import ix.ginas.models.GinasAccessReferenceControlled;
import ix.ginas.models.GinasCommonSubData;
import ix.ginas.models.utils.JSONEntity;

@Entity
@Table(name="ix_ginas_modifications")
@JSONEntity(name = "modifications", title = "Modifications", isFinal = true)
public class Modifications extends GinasCommonSubData {
    @JSONEntity(title = "Agent Modifications")
    @OneToMany(mappedBy = "owner", cascade = CascadeType.ALL)
    //@JoinTable(name="ix_ginas_mod_agent")
    public List<AgentModification> agentModifications =
        new ArrayList<AgentModification>();
    
    @JSONEntity(title = "Physical Modifications")
    @OneToMany(mappedBy = "owner", cascade = CascadeType.ALL)
    //@JoinTable(name="ix_ginas_mod_physical")
    public List<PhysicalModification> physicalModifications =
        new ArrayList<PhysicalModification>();
    
    @JSONEntity(title = "Structural Modifications")
    @OneToMany(mappedBy = "owner", cascade = CascadeType.ALL)
    //@JoinTable(name="ix_ginas_mod_structural")
    public List<StructuralModification> structuralModifications =
        new ArrayList<StructuralModification>();

    public Modifications () {}
    
    @JsonIgnore
    public int getLength(){
        return this.agentModifications.size() + this.physicalModifications.size() + this.structuralModifications.size();
    }
    
    @JsonIgnore
    public List<GinasCommonSubData> allModifications(){
    	List<GinasCommonSubData> mods = new ArrayList<GinasCommonSubData>();
    	mods.addAll(this.agentModifications);
    	mods.addAll(this.physicalModifications);
    	mods.addAll(this.structuralModifications);
    	return mods;
    }

    @Override
   	@JsonIgnore
   	public List<GinasAccessReferenceControlled> getAllChildrenCapableOfHavingReferences() {
   		List<GinasAccessReferenceControlled> temp = new ArrayList<GinasAccessReferenceControlled>();
   		for(GinasCommonSubData mod:allModifications()){
   			temp.addAll(mod.getAllChildrenAndSelfCapableOfHavingReferences());
   		}
   		return temp;
   	}
}
