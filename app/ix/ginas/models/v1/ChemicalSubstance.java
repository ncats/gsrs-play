package ix.ginas.models.v1;

import ix.core.chem.StructureProcessor;
import ix.core.controllers.search.SearchFactory;
import ix.core.models.BeanViews;
import ix.core.models.Indexable;
import ix.core.models.Keyword;
import ix.core.models.Structure;
import ix.core.models.Value;
import ix.core.search.TextIndexer.SearchResult;
import ix.ginas.models.Ginas;
import ix.ginas.models.utils.GinasProcessingMessage;
import ix.ginas.models.utils.GinasProcessingStrategy;
import ix.ginas.models.utils.JSONEntity;
import ix.utils.Global;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToOne;

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
    
    public List<GinasProcessingMessage> prepare(GinasProcessingStrategy strat){
    	List<GinasProcessingMessage> gpm=super.prepare(strat);
    	
    	String payload = this.structure.molfile;
        if (payload != null) {
        	List<Moiety> moietiesForSub = new ArrayList<Moiety>();
            List<Structure> moieties = new ArrayList<Structure>();
            Structure struc = StructureProcessor.instrument
                (payload, moieties);
            this.structure=struc;
            //struc.count
            for(Structure m: moieties){
            	Moiety m2= new Moiety();
            	m2.structure=m;
            	m2.count=m.count;
            	moietiesForSub.add(m2);
            }
            
            if(this.moieties.size()<moietiesForSub.size()){
            	GinasProcessingMessage mes=GinasProcessingMessage.WARNING_MESSAGE("Incorrect number of moeities").appliableChange(true);
            	gpm.add(mes);
            	strat.processMessage(mes);
            	switch(mes.actionType){
				case APPLY_CHANGE:
					this.moieties=moietiesForSub;
					mes.appliedChange=true;
					break;
				case FAIL:
					break;
				case DO_NOTHING:
				case IGNORE:
				default:
					break;
            	}            	
            }
            if(!struc.digest.equals(this.structure.digest)){
            	GinasProcessingMessage mes=GinasProcessingMessage.WARNING_MESSAGE("Given structure digest disagrees with computed").appliableChange(true);
            	gpm.add(mes);
            	strat.processMessage(mes);
            	switch(mes.actionType){
				case APPLY_CHANGE:
					this.structure=struc;
					mes.appliedChange=true;
					break;
				case FAIL:
					break;
				case DO_NOTHING:
				case IGNORE:
				default:
					break;
            	}
            }
            String hash=null;
            for (Value val : struc.properties) {
                if (Structure.H_LyChI_L4.equals(val.label)) {
                	hash=val.getValue()+"";
                }
            }
            
            try {
				SearchResult sr = SearchFactory.search(Substance.class,"hash=" + hash, 1, 0, 0, null);
				if(sr.count()>0){
					GinasProcessingMessage mes=GinasProcessingMessage.WARNING_MESSAGE("Structure has " + sr.count() +" possible duplicate(s)").appliableChange(true);
	            	gpm.add(mes);
	            	strat.processMessage(mes);
	            	switch(mes.actionType){
					case APPLY_CHANGE:
						this.status="FAILED";
						this.addPropertyNote(mes.message, "FAIL_REASON");
						this.addRestrictGroup("admin");
						break;
					case DO_NOTHING:
						break;
					case FAIL:
						break;
					case IGNORE:
						break;
					default:
						break;
					
	            	}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
            
        }
        return gpm;
    }
    
}
