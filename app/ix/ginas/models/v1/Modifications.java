package ix.ginas.models.v1;

import java.util.UUID;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;

import javax.persistence.*;

import play.Logger;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.annotation.JsonProperty;

import ix.core.models.Indexable;
import ix.ginas.models.utils.JSONEntity;
import ix.ginas.models.GinasCommonSubData;

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
        /*
        Logger.info("agent " + this.agentModifications.size());
        Logger.info("phy " + this.physicalModifications.size());
        Logger.info("struc " + this.structuralModifications.size());
        */
        return this.agentModifications.size() + this.physicalModifications.size() + this.structuralModifications.size();
    }
}
