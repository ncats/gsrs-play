package ix.ginas.models.v1;

import java.util.List;
import java.util.ArrayList;
import javax.persistence.*;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import ix.core.models.Keyword;
import ix.ginas.models.Ginas;
import ix.ginas.models.KeywordListSerializer;
import ix.ginas.models.utils.JSONEntity;

@Entity
@Table(name="ix_ginas_strucdiv")
public class StructurallyDiverse extends Ginas {
    public String developmentalStage;
    public String fractionName;
    public String fractionMaterialType;
    public String organismFamily;
    public String organismGenus;
    public String organismSpecies;
    public String partLocation;
    @JSONEntity(title = "Parts", itemsTitle = "Part")
    @ManyToMany(cascade=CascadeType.ALL)
    @JoinTable(name="ix_ginas_strucdiv_part")
    @JsonSerialize(using = KeywordListSerializer.class)    
    public List<Keyword> part = new ArrayList<Keyword>();
    public String sourceMaterialClass;
    public String sourceMaterialState;
    public String sourceMaterialType;
    public String infraSpecificType;
    public String infraSpecificName;
    @OneToOne(cascade=CascadeType.ALL)
    public SubstanceReference hybridSpeciesPaternalOrganism;
    @OneToOne(cascade=CascadeType.ALL)
    public SubstanceReference hybridSpeciesMaternalOrganism;
    @OneToOne(cascade=CascadeType.ALL)
    public SubstanceReference parentSubstance;

    public StructurallyDiverse () {}
    
    public String getDisplayParts(){
    	String ret="";
    	if(part!=null){
    		for(Keyword k: part){
    			if(ret.length()>0){
    				ret+="; ";
    			}
    			ret+=k.getValue();
    		}
    	}
    	return ret;
    }
}
