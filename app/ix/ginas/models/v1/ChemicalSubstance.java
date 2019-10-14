package ix.ginas.models.v1;

import java.util.*;
import java.util.function.Consumer;

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
import com.fasterxml.jackson.databind.node.ObjectNode;

import gov.nih.ncats.molwitch.Chemical;
import ix.core.models.DefinitionalElement;
import ix.core.validator.GinasProcessingMessage;
import ix.core.models.BeanViews;
import ix.core.models.Indexable;
import ix.core.models.Structure;
import ix.ginas.modelBuilders.ChemicalSubstanceBuilder;
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

    public static ChemicalSubstanceBuilder chemicalBuilder(){
        return new ChemicalSubstanceBuilder();
    }

    public ChemicalSubstanceBuilder toChemicalBuilder() {
        return super.toBuilder().asChemical();
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
    @Indexable(name = "Molecular Weight", dranges = { 0, 200, 400, 600, 800, 1000 }, format = "%1$.0f", facet=true)
    public double getMolecularWeight(){
    	return structure.mwt;
    }


    @JsonIgnore
    public GinasAccessReferenceControlled getDefinitionElement(){
        return structure;
    }

	@Override
	@JsonIgnore
	public List<GinasAccessReferenceControlled> getAllChildrenCapableOfHavingReferences(){
		List<GinasAccessReferenceControlled> temp = super.getAllChildrenCapableOfHavingReferences();
		if(this.structure!=null){
			temp.addAll(this.structure.getAllChildrenAndSelfCapableOfHavingReferences());
		}
		if(this.moieties!=null){
			for(Moiety m: this.moieties){
				temp.addAll(m.getAllChildrenAndSelfCapableOfHavingReferences());
			}
		}
		return temp;
	}

    @Override
    protected void additionalDefinitionalElements(Consumer<DefinitionalElement> consumer) {
        /*
        Key->Value
structure.properties.lychi4->"<EXAMPLE_LYCHI>"
structure.properties.stereoChemistry->"RACEMIC"
structure.properties.opticalActivity->"(+/-)"

For each Moiety:
structure.moieties[<lychi4>].lychi4->"<EXAMPLE_LYCHI>"
structure.moieties[<lychi4>].stereoChemistry->"RACEMIC"
structure.moieties[<lychi4>].opticalActivity->"(+/-)"
structure.moieties[<lychi4>].countAmount->"4 to 5 per mol"


         */

        addStructureDefinitionalElementsFor(structure, "structure.properties",consumer, Collections.newSetFromMap(new IdentityHashMap<>()));
    }

    private void addStructureDefinitionalElementsFor(Structure structure, String prefix, Consumer<DefinitionalElement> consumer, Set<Structure> visited){
        if(structure != null){
            String l4 = structure.getExactHash();
            visited.add(structure);
            if(l4 !=null){
                consumer.accept(DefinitionalElement.of(prefix+ ".lychi4", l4));
            }
            if(structure.stereoChemistry !=null){
                consumer.accept(DefinitionalElement.of(prefix+ ".stereoChemistry", structure.stereoChemistry.toString()));
            }
            //opticalActivity
            if(structure.opticalActivity !=null){
                consumer.accept(DefinitionalElement.of(prefix+ ".opticalActivity", structure.opticalActivity.toValue()));
            }
            List<Moiety> sorted = new ArrayList<>(moieties);
            Collections.sort(sorted);

            for(Moiety moiety : sorted){
                Structure moietyStructure = moiety.structure;
                if(moietyStructure == null || visited.contains(moietyStructure)){
                    continue;
                }
                String lychi = moietyStructure.getExactHash();
                consumer.accept(DefinitionalElement.of("structure.moieties."+lychi + ".countAmount" , moiety.getCountAmount().toString()));

                addStructureDefinitionalElementsFor(moietyStructure, "structure.moieties."+lychi, consumer, visited);
            }
        }
    }
}
