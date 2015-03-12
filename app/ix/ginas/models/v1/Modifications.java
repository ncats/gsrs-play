package ix.ginas.models.v1;

import java.util.UUID;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import javax.persistence.*;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.annotation.JsonProperty;

import ix.core.models.Indexable;
import ix.ginas.models.utils.JSONEntity;
import ix.ginas.models.Ginas;

@Entity
@Table(name="ix_ginas_modifications")
@JSONEntity(name = "modifications", title = "Modifications", isFinal = true)
public class Modifications extends Ginas {
    @JSONEntity(title = "Agent Modifications")
    @ManyToMany(cascade=CascadeType.ALL)
    @JoinTable(name="ix_ginas_modifications_agent")
    public List<AgentModification> agentModifications =
        new ArrayList<AgentModification>();
    
    @JSONEntity(title = "Physical Modifications")
    @ManyToMany(cascade=CascadeType.ALL)
    @JoinTable(name="ix_ginas_modifications_physical")
    public List<PhysicalModification> physicalModifications =
        new ArrayList<PhysicalModification>();
    
    @JSONEntity(title = "Structural Modifications")
    @ManyToMany(cascade=CascadeType.ALL)
    @JoinTable(name="ix_ginas_modifications_structural")
    public List<StructuralModification> structuralModifications =
        new ArrayList<StructuralModification>();

    public Modifications () {}
}
