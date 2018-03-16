package ix.ginas.models.v1;

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

import ix.core.models.Indexable;
import ix.ginas.models.CommonDataElementOfCollection;
import ix.ginas.models.utils.JSONConstants;
import ix.ginas.models.utils.JSONEntity;


@JSONEntity(title = "Relationship", isFinal = true)
@Entity
@Table(name="ix_ginas_relationship")
public class Relationship extends CommonDataElementOfCollection {
    
	public static final String ACTIVE_MOIETY_RELATIONSHIP_TYPE="ACTIVE MOIETY";
    private static final String RELATIONSHIP_INV_CONST = "->";
    private static final Pattern RELATIONSHIP_SPLIT_REGEX = Pattern.compile(RELATIONSHIP_INV_CONST);

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
        if(type.contains(RELATIONSHIP_INV_CONST)){
                return type.split(RELATIONSHIP_INV_CONST)[0] + " (" +type.split(RELATIONSHIP_INV_CONST)[1] +")";
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
    	if(type==null)return false;
        //Explicitly ignore alternative relationships
        if(this.type.equals(Substance.ALTERNATE_SUBSTANCE_REL) || this.type.equals(Substance.PRIMARY_SUBSTANCE_REL)){
            return false;
        }
    	if(this.fetchOwner().getOrGenerateUUID().toString().equals(this.relatedSubstance.refuuid)){
    		return false;
    	}

    	String[] types=RELATIONSHIP_SPLIT_REGEX.split(this.type);
    	
    	
    	if(types.length>=2)return true;
    	return false;
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
    	if(!isAutomaticInvertable()){
    		throw new IllegalStateException("Relationship :" + this.type + " is not invertable");
    	}
    	Relationship r=new Relationship();
    	String[] types=RELATIONSHIP_SPLIT_REGEX.split(this.type);
    	r.type=types[1] + RELATIONSHIP_INV_CONST + types[0];
    	r.setAccess(this.getAccess());
    	return r;
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
    
}
