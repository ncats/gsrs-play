package ix.ginas.models.v1;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Pattern;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Lob;
import javax.persistence.OneToOne;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnore;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import ix.core.models.Indexable;
import ix.core.models.Keyword;
import ix.core.util.EntityUtils;
import ix.ginas.models.CommonDataElementOfCollection;
import ix.ginas.models.utils.JSONConstants;
import ix.ginas.models.utils.JSONEntity;
import ix.ginas.models.utils.RelationshipUtil;
import org.json4s.JsonUtil;


@JSONEntity(title = "Relationship", isFinal = true)
@Entity
@Table(name="ix_ginas_relationship")
public class Relationship extends CommonDataElementOfCollection {
    
	public static final String ACTIVE_MOIETY_RELATIONSHIP_TYPE="ACTIVE MOIETY";

	@JSONEntity(title="Amount")
    @OneToOne(cascade=CascadeType.ALL)
    public Amount amount;
    
    @JSONEntity(title = "Comments")
    @Lob
    @Basic(fetch=FetchType.EAGER)
    public String comments;
    
    
    
    @JSONEntity(title = "Interaction Type", format = JSONConstants.CV_INTERACTION_TYPE)
    @Indexable(name="Interaction Type", facet = true)
    public String interactionType;

    @JSONEntity(title = "Qualification", format = JSONConstants.CV_QUALIFICATION)
    public String qualification;
    
    @JSONEntity(title = "Related Substance", isRequired = true)
    @OneToOne(cascade=CascadeType.ALL)
    public SubstanceReference relatedSubstance;
    
    @JSONEntity(title = "Mediator Substance")
    @OneToOne(cascade=CascadeType.ALL)
    public SubstanceReference mediatorSubstance;
    
    
    public String originatorUuid=null;
    
    
    @Indexable(facet=true,name="Relationships")
    @JSONEntity(title = "Relationship Type", format = JSONConstants.CV_RELATIONSHIP_TYPE, isRequired = true)
    public String type;

    public Relationship () {}
    
    @JsonIgnore
    public String getDisplayType(){
       return RelationshipUtil.getDisplayType(this);
    }
    
    
    @PrePersist
    @PreUpdate
    public void fixNewLine(){
		//System.out.println("Persisting relationship");

		if (comments != null) {
			comments = comments.replaceAll("[\\\\][\\\\]*n", "\n");
		}
		if(originatorUuid==null){
			originatorUuid=this.getOrGenerateUUID().toString();
		}
		//System.out.println("Persisted");
    }
   
    
    /**
     * Returns true if this relationship should be inverted. That is, it is invertible,
     * and it is not the product of an earlier inversion.
     * @return
     */
    @JsonIgnore
    public boolean shouldBeInverted(){
    	return this.isGenerator() && this.isAutomaticInvertable();
    }
    
    /**
     * Returns true if this relationship is made directly, and not from another relationship
     * @return
     */
    @JsonIgnore
    public boolean isGenerator(){
    	if(this.originatorUuid==null||this.originatorUuid.equals(this.uuid.toString())){
    		return true;
    	}
    	return false;
    }
    
    /**
     * Returns true if this relationship is invertible
     * @return
     */
    @JsonIgnore
    public boolean isAutomaticInvertable(){
    	return RelationshipUtil.isAutomaticInvertable(this);
    }
    
    public boolean hasComments(){
    	return ((this.comments!=null) && !this.comments.isEmpty());
    }
    
    public String getComments(){
    	return comments;
    }
    
    public void setComments(String comments){
    	this.comments=comments;
    }
    
    public Relationship fetchInverseRelationship(){
        return RelationshipUtil.createInverseRelationshipFor(this);
    }
    
    //This flag is used to explicitly allow deleting of this relationship
    @JsonIgnore
    private transient boolean okToRemoveFlag=false;
    
    @JsonIgnore
    public void setOkToRemove(){
    	okToRemoveFlag=true;
    }
    
    @JsonIgnore
    public boolean isOkToRemove(){
    	return okToRemoveFlag;
    }

    @Override
    public String toString() {
        return "Relationship{" +
                "uuid =" + getOrGenerateUUID() +
                ", amount=" + amount +
                ", comments='" + comments + '\'' +
                ", interactionType='" + interactionType + '\'' +
                ", qualification='" + qualification + '\'' +
                ", relatedSubstance=" + relatedSubstance +
                ", mediatorSubstance=" + mediatorSubstance +
                ", originatorUuid='" + originatorUuid + '\'' +
                ", type='" + type + '\'' +
                ", okToRemoveFlag=" + okToRemoveFlag +
                '}';
    }
}
