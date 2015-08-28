package ix.ginas.models.v1;

import ix.core.models.BeanViews;
import ix.core.models.Indexable;
import ix.core.models.Structure;
import ix.ginas.models.utils.JSONEntity;
import ix.utils.Global;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToOne;
import javax.persistence.Transient;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.node.ObjectNode;

@SuppressWarnings("serial")
@JSONEntity(name = "chemicalSubstance", title = "Chemical Substance")
@Entity
@Inheritance
@DiscriminatorValue("CHE")
public class ChemicalSubstance extends Substance {
    @JSONEntity(isRequired = true)
    @OneToOne(cascade=CascadeType.ALL)
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

    @Indexable(name="SubstanceStereoChemistry", facet=true)
    public Structure.Stereo getStereoChemistry () {
        return structure != null ? structure.stereoChemistry : null;
    }
    
    @Transient 
    private int[] atomMaps=null;
    
    @JsonIgnore
    public int[] getAtomMaps(){
    	if(atomMaps==null)return new int[0];
    	return atomMaps;
    }
    
	@JsonIgnore
	public String getAtomMapsString() {
		return Arrays.toString(getAtomMaps()).replace("[", "").replace("]", "")
				.replace(" ", "");
	}
    
    @JsonIgnore
    public void setAtomMaps(int[] am){
    	atomMaps=am;
    }
}
