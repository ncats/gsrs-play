package ix.ginas.models.v1;

import java.util.List;
import java.util.Map;
import javax.persistence.*;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.annotation.JsonProperty;

import ix.core.models.Indexable;
import ix.core.models.Principal;

import ix.ginas.models.utils.JSONEntity;
import ix.ginas.models.utils.JSONConstants;
import ix.ginas.models.Ginas;


@JSONEntity(title = "Relationship", isFinal = true)
@Entity
@Table(name="ix_ginas_relationship")
public class Relationship extends Ginas {
    @JSONEntity(title="Amount")
    @OneToOne
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
    @OneToOne
    public SubstanceReference relatedSubstance;
    
    @JSONEntity(title = "Relationship Type", format = JSONConstants.CV_RELATIONSHIP_TYPE, isRequired = true)
    public String type;

    public Relationship () {}
}
