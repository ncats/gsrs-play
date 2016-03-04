package ix.ginas.models;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.persistence.CascadeType;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.persistence.OneToOne;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import ix.core.UserFetcher;
import ix.core.controllers.AdminFactory;
import ix.core.models.Group;
import ix.core.models.Indexable;
import ix.core.models.Principal;
import ix.utils.Global;
import play.Logger;
import play.db.ebean.Model;

@MappedSuperclass
public class GinasCommonData extends Model implements GinasAccessControlled{
    static public final String REFERENCE = "GInAS Reference";
    static public final String TAG = "GInAS Tag";
    
    
    @Id
    public UUID uuid;


    public Date created=null;
    
    @OneToOne(cascade=CascadeType.ALL)
    @Indexable(facet = true, name = "Created By")
    public Principal createdBy;
    
    @Indexable(facet = true, name = "Last Edited Date")
    public Date lastEdited;
    
    //TP: why is this one-to-one?
    @OneToOne(cascade=CascadeType.ALL)
    @Indexable(facet = true, name = "Last Edited By")
    public Principal lastEditedBy;
    
    @JsonDeserialize(using = PrincipalDeserializer.class)
    public Principal getCreatedBy() {
		return createdBy;
	}

    @JsonSerialize(using = PrincipalSerializer.class)
	public void setCreatedBy(Principal createdBy) {
		this.createdBy = createdBy;
	}
    
    @JsonDeserialize(using = PrincipalDeserializer.class)
    public Principal getLastEditedBy() {
		return lastEditedBy;
	}

    @JsonSerialize(using = PrincipalSerializer.class)
	public void setLastEditedBy(Principal lastEditedBy) {
		this.lastEditedBy = lastEditedBy;
	}


	//Where did this come from?
    public boolean deprecated;
    
    
    @JsonIgnore
    @OneToOne(cascade=CascadeType.ALL)
    public GinasAccessContainer recordAccess;

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public Date getLastEdited() {
        return lastEdited;
    }

    public void setLastEdited(Date lastEdited) {
        this.lastEdited = lastEdited;
    }

    public boolean isDeprecated() {
        return deprecated;
    }

    public void setDeprecated(boolean deprecated) {
        this.deprecated = deprecated;
    }

    public GinasAccessContainer getRecordAccess() {
        return recordAccess;
    }

    public void setRecordAccess(GinasAccessContainer recordAccess) {
        this.recordAccess = recordAccess;
    }

    @JsonProperty("access")
    public void setAccess(Collection<String> access){
    	ObjectMapper om = new ObjectMapper();
    	Map mm = new HashMap();
    	mm.put("access", access);
    	mm.put("entityType", this.getClass().getName());
    	JsonNode jsn=om.valueToTree(mm);
    	try {
			recordAccess= om.treeToValue(jsn, GinasAccessContainer.class);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
    	return;
    }
    
    @JsonSerialize(using = GroupListSerializer.class)
    public Set<Group> getAccess(){
    	if(recordAccess!=null){
    		return recordAccess.access;
    	}
    	return null;
    }
    

    public GinasCommonData () {
    }
    
    
    @PrePersist
    public void beforeCreate () {
        
    }
    
    /**
     * Called before saving. Updates with the current time and user
     * for bookkeeping purposes. Note that this method currently uses
     * reflection to find the current logged in user, as the method used
     * is not found in the core module.
     * 
     * This method is not ideal for 2 reasons. First, the reflection 
     * piece is ugly and hardcoded, making it difficult to maintain.
     * Second, when doing a batch load, there is no method for fetching
     * the user who submitted the record. These should be addressed in
     * the future, ideally by having a submission "context" which can be
     * referenced either here, or before reaching this point.
     * 
     * Update: The UserFetcher class now falls back to the user set
     * explicitly on the localThread. This will be set by the 
     * job code prior to getting to this point, so things should be
     * ok.
     * 
     */
    @PrePersist
    @PreUpdate
    public void modified () {
        this.lastEdited = new Date ();
        Principal p1=UserFetcher.getActingUser();
        if(p1!=null){
    		lastEditedBy=p1;
    		if(this.createdBy==null){
    			createdBy=p1;
    			created= new Date();
        	}
        }
        
    }
    
    @JsonProperty("_self")
    @Indexable(indexed=false)
    public String getself () {
        if (uuid != null) {
            try {
                String ref = Global.getRef(this);
                if (ref != null)
                    return ref+"?view=full";
            }
            catch (Exception ex) {
                ex.printStackTrace();
                Logger.error("Not a valid persistence Entity", ex);
            }
        }
        return null;
    }
    
    @JsonIgnore
    public UUID getOrGenerateUUID(){
    	if(uuid!=null)return uuid;
    	this.uuid=UUID.randomUUID();
    	return uuid;
    }
    


	public void addRestrictGroup(Group p){
		if(this.recordAccess==null){
			this.recordAccess=new GinasAccessContainer();
		}
		this.recordAccess.add(p);
	}
	public void addRestrictGroup(String group){
		addRestrictGroup(AdminFactory.registerGroupIfAbsent(new Group(group)));
	}


	@JsonIgnore
	public Set<Group> getAccessGroups() {
		return this.getAccess();
	}
	
	
	
}
