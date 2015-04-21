package ix.ginas.models.v1;

import java.util.List;
import java.util.ArrayList;

import javax.persistence.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import ix.core.models.Keyword;
import ix.core.models.Publication;
import ix.core.models.Structure;
import ix.core.models.Indexable;
import ix.core.models.BeanViews;
import ix.core.models.Value;
import ix.core.models.XRef;
import ix.ginas.models.utils.JSONEntity;
import ix.utils.Global;

@JSONEntity(name = "chemicalSubstance", title = "Chemical Substance")
@Entity
@Inheritance
@DiscriminatorValue("CHE")
public class ChemicalSubstance extends Substance {
    @JSONEntity(isRequired = true)
    @OneToOne
    @Column(nullable=false)
    @JsonSerialize(using=StructureSerializer.class)
    public Structure structure;
    
    @JSONEntity(title = "Chemical Moieties", isRequired = true, minItems = 1)
    @ManyToMany(cascade=CascadeType.ALL)
    @JoinTable(name="ix_ginas_chemical_moiety")
    @JsonView(BeanViews.Full.class)
    public List<Moiety> moieties = new ArrayList<Moiety>();

    public ChemicalSubstance () {
        super (SubstanceClass.chemical);
    }
    
    @JsonView(BeanViews.Compact.class)
    @JsonProperty("_moieties")
    public JsonNode getJsonMoieties () {
        JsonNode node = null;
        if (!moieties.isEmpty()) {
            try {
                ObjectNode n = mapper.createObjectNode();
                n.put("count", moieties.size());
                n.put("href", Global.getRef(getClass (), uuid)
                      +"/moieties");
                node = n;
            }
            catch (Exception ex) {
                // this means that the class doesn't have the NamedResource
                // annotation, so we can't resolve the context
                node = mapper.valueToTree(moieties);
            }
        }
        return node;
    }

}
