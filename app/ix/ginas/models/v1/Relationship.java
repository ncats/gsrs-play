package ix.ginas.models.v1;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import javax.persistence.*;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import ix.core.models.Indexable;
import ix.core.models.Principal;
import ix.core.models.Keyword;

import ix.ginas.models.utils.JSONEntity;
import ix.ginas.models.utils.JSONConstants;
import ix.ginas.models.*;


@JSONEntity(title = "Relationship", isFinal = true)
@Entity
@Table(name="ix_ginas_relationship")
public class Relationship extends Ginas {
    @ManyToMany(cascade=CascadeType.ALL)
    @JoinTable(name="ix_ginas_relationship_ref")
    @JsonSerialize(using=KeywordListSerializer.class)    
    public List<Keyword> references = new ArrayList<Keyword>();
    
    @JSONEntity(title="Amount")
    @OneToOne(cascade=CascadeType.ALL)
    public Amount amount;
    
    @JSONEntity(title = "Comments")
    @Lob
    @Basic(fetch=FetchType.EAGER)
    public String comments;
    
    @JSONEntity(title = "Interaction Type", format = JSONConstants.CV_INTERACTION_TYPE)
    public String interactionType;

    @JSONEntity(title = "Qualification", format = JSONConstants.CV_QUALIFICATION)
    public String qualification;
    
    @JSONEntity(title = "Related Substance", isRequired = true)
    @OneToOne(cascade=CascadeType.ALL)
    public SubstanceReference relatedSubstance;
    
    @JSONEntity(title = "Mediator Substance")
    @OneToOne(cascade=CascadeType.ALL)
    public SubstanceReference mediatorSubstance;
    
    
    @Indexable(facet=true,name="Relationships")
    @JSONEntity(title = "Relationship Type", format = JSONConstants.CV_RELATIONSHIP_TYPE, isRequired = true)
    public String type;

    public Relationship () {}
    
    @JsonIgnore
    public String getDisplayType(){
        if(type.contains("->")){
                return type.split("->")[0] + " (" +type.split("->")[1] +")";
        }
        return type;
    }
}
