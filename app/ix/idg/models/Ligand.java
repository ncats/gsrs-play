package ix.idg.models;

import java.util.List;
import java.util.ArrayList;
import javax.persistence.*;

import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import ix.core.models.BeanViews;
import ix.core.models.Indexable;
import ix.core.models.Keyword;
import ix.ginas.models.Structure;
import ix.utils.Global;

@Entity
@DiscriminatorValue("LIG")
public class Ligand extends EntityModel {
    @JsonView(BeanViews.Full.class)
    @ManyToMany(cascade=CascadeType.ALL)
    @JoinTable(name="ix_idg_ligand")
    public List<Structure> structures = new ArrayList<Structure>();
	
    public Ligand () {}

    @JsonView(BeanViews.Compact.class)
    @JsonProperty("_structures")
    public JsonNode getJsonStructures () {
	ObjectNode node = null;
	if (!structures.isEmpty()) {
	    node = mapper.createObjectNode();
	    node.put("count", structures.size());
	    node.put("href", Global.getRef(getClass (), id)+"/structures");
	}
	return node;
    }
}
