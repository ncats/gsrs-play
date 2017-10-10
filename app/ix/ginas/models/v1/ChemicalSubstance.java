package ix.ginas.models.v1;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Transient;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.node.ObjectNode;

import gov.nih.ncgc.chemical.Chemical;
import ix.core.GinasProcessingMessage;
import ix.core.models.BeanViews;
import ix.core.models.Group;
import ix.core.models.Indexable;
import ix.core.models.Structure;
import ix.ginas.models.GinasAccessReferenceControlled;
import ix.ginas.models.GinasSubstanceDefinitionAccess;
import ix.ginas.models.utils.JSONEntity;
import ix.utils.Global;

@SuppressWarnings("serial")
@JSONEntity(name = "chemicalSubstance", title = "Chemical Substance")
@Entity
@Inheritance
@DiscriminatorValue("CHE")
public class ChemicalSubstance extends Substance implements GinasSubstanceDefinitionAccess {
	
    @JSONEntity(isRequired = true)
    @OneToOne(cascade=CascadeType.ALL)
    @Column(nullable=false)
    //@JsonSerialize(using=StructureSerializer.class)
    public GinasChemicalStructure structure;
    
    @JSONEntity(title = "Chemical Moieties", isRequired = true, minItems = 1)
    @OneToMany(mappedBy = "owner", cascade=CascadeType.ALL)
    @JsonView(BeanViews.Full.class)
    public List<Moiety> moieties = new ArrayList<Moiety>();



    @Indexable(name="SubstanceStereochemistry", facet=true)
    @JsonIgnore
    public Structure.Stereo getStereochemistry () {
        return structure != null ? structure.stereoChemistry : null;
    }
    
    @Transient 
    private int[] atomMaps=null;


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
                n.put("href", Global.getRef(getClass (), getUuid())
                        +"/moieties");
                node = n;
            }catch (Exception ex) {
                // this means that the class doesn't have the NamedResource
                // annotation, so we can't resolve the context
                node = mapper.valueToTree(moieties);
            }
        }
        return node;
    }
    @JsonIgnore
    @Transient 
    public int[] getAtomMaps(){
        if(atomMaps==null)return new int[0];
        return atomMaps;
    }
    
    @JsonIgnore
    @Transient 
    public String getAtomMapsString() {
        return Arrays.toString(getAtomMaps()).replace("[", "").replace("]", "")
            .replace(" ", "");
    }
    
    @JsonIgnore
    @Transient 
    public void setAtomMaps(int[] am){
        atomMaps=am;
    }
    
    @Override
    public void delete(){
    	//this.structure.delete();
    	//for(Moiety m: this.moieties){
    	//	m.delete();
    	//}
    	super.delete();
    	
    }

    @Override
    protected Chemical getChemicalImpl(List<GinasProcessingMessage> messages) {
        return structure.toChemical(messages);
    }
    
    @JsonIgnore
    @Indexable(indexed=false, structure=true)
	public String getStructureMolfile(){
		return structure.molfile;
	}

    @JsonIgnore
    public GinasAccessReferenceControlled getDefinitionElement(){
        return structure;
    }
}
