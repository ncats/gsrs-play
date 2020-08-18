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
import ix.core.chem.StructureProcessor;
import ix.core.models.DefinitionalElement;
import ix.core.validator.GinasProcessingMessage;
import ix.core.models.BeanViews;
import ix.core.models.Indexable;
import ix.core.models.Structure;
import ix.core.validator.ValidationMessage;
import ix.core.validator.Validator;
import ix.core.validator.ValidatorCallback;
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


    @Indexable(name="moieties")
    @JsonIgnore
    public List<GinasChemicalStructure> getMoietiesForIndexing(){
    	List<GinasChemicalStructure> mlist = new ArrayList<>();
    	for(Moiety m: this.moieties){
    		mlist.add(m.structure);
    	}
    	return mlist;
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

        addStructureDefinitialElements(structure, consumer);
    }

    private void addStructureDefinitialElements(Structure structure, Consumer<DefinitionalElement> consumer) {
        play.Logger.debug("starting addStructureDefinitialElements (ChemicalSubstance)");
        consumer.accept(DefinitionalElement.of("structure.properties.hash1",
                structure.getStereoInsensitiveHash(), 1));
        play.Logger.debug("structure.getStereoInsensitiveHash(): "  + structure.getStereoInsensitiveHash());
        consumer.accept(DefinitionalElement.of("structure.properties.hash2",
                structure.getExactHash(), 2));
        play.Logger.debug("structure.getExactHash(): " + structure.getExactHash());

        if(structure.stereoChemistry!=null){
            consumer.accept(DefinitionalElement.of("structure.properties.stereoChemistry",
                    structure.stereoChemistry.toString(), 2));
            play.Logger.debug("structure.stereoChemistry : " + structure.stereoChemistry.toString());
        }
        if(structure.opticalActivity!=null){
            consumer.accept(DefinitionalElement.of("structure.properties.opticalActivity",
                    structure.opticalActivity.toString(), 2));
            play.Logger.debug("structure.opticalActivity.toString(): " + structure.opticalActivity.toString());
        }
        if( moieties != null) {
            for(Moiety m: moieties){
                String mh=m.structure.getStereoInsensitiveHash();
                play.Logger.debug("processing moiety with hash " + mh);
                consumer.accept(DefinitionalElement.of("moiety.hash1", m.structure.getStereoInsensitiveHash(),1));
                consumer.accept(DefinitionalElement.of("moiety.hash2", m.structure.getExactHash(),2));
                play.Logger.debug("m.structure.getExactHash(): " + m.structure.getExactHash());
                consumer.accept(DefinitionalElement.of("moiety[" + mh + "].stereoChemistry",
                        m.structure.stereoChemistry.toString(), 2));
                play.Logger.debug("m.structure.stereoChemistry.toString(): " + m.structure.stereoChemistry.toString());
                consumer.accept(DefinitionalElement.of("moiety[" + mh + "].opticalActivity",
                        m.structure.opticalActivity.toString(), 2));
                play.Logger.debug("m.structure.opticalActivity.toString(): " + m.structure.opticalActivity.toString());
                consumer.accept(DefinitionalElement.of("moiety[" + mh + "].countAmount",
                        m.getCountAmount().toString(), 2));
                play.Logger.debug("m.getCountAmount().toString(): " + m.getCountAmount().toString());
            }
        }
    }


}
