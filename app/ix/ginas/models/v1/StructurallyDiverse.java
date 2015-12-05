package ix.ginas.models.v1;

import java.util.List;
import java.util.ArrayList;

import javax.persistence.*;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import ix.core.models.Indexable;
import ix.core.models.Keyword;
import ix.ginas.models.GinasSubData;
import ix.ginas.models.KeywordListSerializer;
import ix.ginas.models.v1.PartListDeserializer;
import ix.ginas.models.utils.JSONEntity;

@Entity
@Table(name="ix_ginas_strucdiv")
public class StructurallyDiverse extends GinasSubData {

    @Indexable(name="Material Class", facet=true)
    public String sourceMaterialClass;
    @Indexable(name="Material Type", facet=true)
    public String sourceMaterialType;
    
    public String sourceMaterialState;
    
    @Indexable(name="Family", facet=true)
    public String organismFamily;
    
    @Indexable(name="Genus", facet=true)
    public String organismGenus;
    
    @Indexable(name="Species", facet=true)
    public String organismSpecies;
    
    @Indexable(name="Author", facet=true)
    public String organismAuthor;
    
    public String partLocation;
    
    
    @JSONEntity(title = "Parts", itemsTitle = "Part")
    @ManyToMany(cascade=CascadeType.ALL)
    @JoinTable(name="ix_ginas_strucdiv_part")
    @JsonSerialize(using = KeywordListSerializer.class)
    @JsonDeserialize(using = PartListDeserializer.class)    
    public List<Keyword> part = new ArrayList<Keyword>();

    public String infraSpecificType;
    public String infraSpecificName;
    public String developmentalStage;
    public String fractionName;
    public String fractionMaterialType;

    @OneToOne(cascade=CascadeType.ALL)
    @JoinColumn(name="paternal_uuid")
    public SubstanceReference hybridSpeciesPaternalOrganism;
    
    @OneToOne(cascade=CascadeType.ALL)
    @JoinColumn(name="maternal_uuid")
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
