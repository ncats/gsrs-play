package ix.ginas.models.v1;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.*;

import com.fasterxml.jackson.annotation.JsonIgnore;

import com.fasterxml.jackson.annotation.JsonInclude;
import ix.core.models.Indexable;
import ix.ginas.models.CommonDataElementOfCollection;
import ix.ginas.models.GinasAccessReferenceControlled;
import ix.ginas.models.utils.JSONConstants;
import ix.ginas.models.utils.JSONEntity;
import ix.ginas.models.utils.RelationshipUtil;


@JSONEntity(title = "Relationship", isFinal = true)
@Entity
@Table(name="ix_ginas_relationship")
public class Relationship extends CommonDataElementOfCollection {
    
	public static final String ACTIVE_MOIETY_RELATIONSHIP_TYPE="ACTIVE MOIETY";

	@JSONEntity(title="Amount")
    @OneToOne(cascade=CascadeType.ALL)
    @JsonInclude(value= JsonInclude.Include.NON_EMPTY)
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
    public boolean isAutomaticInvertible(){
    	return RelationshipUtil.isAutomaticInvertible(this);
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

    /**
     * Test if another relationship is <i>essentially</i> equivalent to this relationship.
     *
     * Here, this means:
     *
     * 1. It has equivalent reference to a substance
     * 2. It has the same relationship type
     *
     *
     * @param other
     * @return
     */
    @JsonIgnore
    public boolean isEquivalentBaseRelationship(Relationship other){
    	if (other.type.equals(this.type) && other.relatedSubstance.isEquivalentTo(this.relatedSubstance)) {
			return true;
		}
    	return false;
    }

    public String toSimpleString(){
    	return type + ":" + relatedSubstance.getName();
    }

	@Override
	@JsonIgnore
	public List<GinasAccessReferenceControlled> getAllChildrenCapableOfHavingReferences() {
		List<GinasAccessReferenceControlled> temp = new ArrayList<GinasAccessReferenceControlled>();
		if(this.amount!=null){
			temp.addAll(this.amount.getAllChildrenAndSelfCapableOfHavingReferences());
		}
		if(this.relatedSubstance!=null){
			temp.addAll(this.relatedSubstance.getAllChildrenAndSelfCapableOfHavingReferences());
		}
		return temp;
	}
	@PostLoad
	public void fixWhitespaceIssues(){

        if (this.type != null){
            this.type = RelationshipUtil.standardizeType(this);
        }
    }
}
