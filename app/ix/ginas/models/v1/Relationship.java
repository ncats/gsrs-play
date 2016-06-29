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
public class Relationship extends CommonDataElementOfCollection {
    
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
    
    
    public String originatorUuid=null;
    
    
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
     * Returns true if this relationship is invertable
     * @return
     */
    @JsonIgnore
    public boolean isAutomaticInvertable(){
        //Explicitly ignore alternative relationships
        if(this.type.equals(Substance.ALTERNATE_SUBSTANCE_REL) || this.type.equals(Substance.PRIMARY_SUBSTANCE_REL)){
            return false;
        }
    	if(this.fetchOwner().getOrGenerateUUID().toString().equals(this.relatedSubstance.refuuid)){
    		return false;
    	}

    	String[] types=this.type.split("->");
    	if(types.length>=2)return true;
    	return false;
    }
    
    public Relationship fetchInverseRelationship(){
    	if(!isAutomaticInvertable()){
    		throw new IllegalStateException("Relationship :" + this.type + " is not invertable");
    	}
    	Relationship r=new Relationship();
    	String[] types=this.type.split("->");
    	r.type=types[1] + "->" + types[0];
    	r.setAccess(this.getAccess());
    	return r;
    }
    
    //This flag is used to explicitly allow deleting of this relationship
    private transient boolean okToRemoveFlag=false;
    
    public void setOkToRemove(){
    	okToRemoveFlag=true;
    }
    
    public boolean isOkToRemove(){
    	return okToRemoveFlag;
    }
    
}
