package ix.ginas.models;

import java.util.Date;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Stream;

import javax.persistence.Basic;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.persistence.OneToOne;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Version;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import ix.core.UserFetcher;
import ix.core.controllers.AdminFactory;
import ix.core.controllers.EntityFactory.EntityMapper;
import ix.core.models.BaseModel;
import ix.core.models.ForceUpdatableModel;
import ix.core.models.Group;
import ix.core.models.Indexable;
import ix.core.models.Principal;
import ix.core.util.StreamUtil;
import ix.core.util.TimeUtil;
import ix.ginas.models.serialization.GroupDeserializer;
import ix.ginas.models.serialization.GroupSerializer;
import ix.ginas.models.serialization.PrincipalDeserializer;
import ix.ginas.models.serialization.PrincipalSerializer;
import ix.utils.Global;
import ix.utils.Util;

/**
 * Base class for all Ginas Model objects, contains all fields
 * common to all including UUID, and audit information.
 */
@MappedSuperclass
public class GinasCommonData extends BaseModel implements GinasAccessControlled, ForceUpdatableModel{
    static public final String REFERENCE = "GInAS Reference";
    static public final String TAG = "GInAS Tag";
	public static final String LANGUAGE = "GInAS Language";
	public static final String DOMAIN = "GInAS Domain";
	public static final String REFERENCE_TAG = "GInAS Document Tag";
	public static final String NAME_JURISDICTION = "GInAS Name Jurisdiction";
	public static final String SUB_CLASS = "GInAS Subclass";
    
	
    //used only for forcing updates
    @JsonIgnore
    private int currentVersion=0;
    

    @Version
    private Long internalVersion;
    
    @Id
    public UUID uuid;
    
    @Indexable(name = "Creation Date", sortable=true)
    public Date created=null;
    
    @OneToOne()
    @Indexable(facet = true, name = "Created By", sortable=true, recurse=false)
    public Principal createdBy;
    
    @Indexable( name = "Last Edited Date", sortable=true)
    public Date lastEdited;
    
    //TP: why is this one-to-one?
    @OneToOne()
    @Indexable(facet = true, name = "Last Edited By", sortable=true, recurse=false, useFullPath = true)
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
    
    //OLD WAY
    @JsonIgnore
    @Basic(fetch=FetchType.LAZY)
    //@Lob
//    @OneToOne(cascade=CascadeType.ALL)
    private GinasAccessContainer recordAccess;
//    
    
//    @JsonIgnore
//    @Lob
//    private String recordAccessJSON;
    
    
    

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
    	if(this.uuid==null){
    		this.uuid = uuid;	
    	}
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

    @JsonIgnore
    public GinasAccessContainer getRecordAccess() {
    	return recordAccess;
    }

    @JsonIgnore
    public void setRecordAccess(GinasAccessContainer recordAccess) {
        this.recordAccess = new GinasAccessContainer(this);
        if(recordAccess!=null){
        this.recordAccess.setAccess(recordAccess.getAccess());
        }
    }

    @JsonProperty("access")
    @JsonDeserialize(contentUsing = GroupDeserializer.class)
    public void setAccess(Set<Group> access){
    	GinasAccessContainer recordAccess=this.getRecordAccess();
    	if(recordAccess==null){
    		recordAccess=new GinasAccessContainer(this);
    	}
    	recordAccess.setAccess(access);
		setRecordAccess(recordAccess);
    }
    
    @JsonProperty("access")
    @JsonSerialize(contentUsing = GroupSerializer.class)
    public Set<Group> getAccess(){
    	GinasAccessContainer gac=getRecordAccess();
    	if(gac!=null){
    		return gac.getAccess();
    	}
    	return new LinkedHashSet<Group>();
    }

	public void addRestrictGroup(Group p){
		GinasAccessContainer gac=this.getRecordAccess();
		if(gac==null){
			gac=new GinasAccessContainer(this);
		}
		gac.add(p);
		this.setRecordAccess(gac);
	}
	
	public void addRestrictGroup(String group){
		addRestrictGroup(AdminFactory.registerGroupIfAbsent(new Group(group)));
	}


    public GinasCommonData () {
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
    
    @PreUpdate
    public void modified () {
    	updateAuditInfo(false, UserFetcher.isForceAuditUpdate());
    }
    
    @PrePersist
    public void created () {
    	updateAuditInfo(true, UserFetcher.isForceAuditUpdate());
    }
    
    
    public void updateAuditInfo(boolean creation, boolean force){

    	//The logic here is essentially:
    	//1. If lastEdited date is null, it will get the current date.
    	//2. If created date is null, it will get the current date.
    	//3. If lastEditedBy is null, it will get the current user.
    	//4. If createdBy is null, it will also get the current user.
    	//

    	Date currentDate = TimeUtil.getCurrentDate();
    	if(this.lastEditedBy == null || this.createdBy==null || force){

			Principal p1=UserFetcher.getActingUser();
	        if(p1!=null){
	        	if(lastEditedBy== null || force){
	        		this.lastEditedBy=p1;
	        		this.lastEdited = currentDate;
	        	}
	    		if(creation && (this.createdBy==null || force)){
	    			this.createdBy=p1;
	    			this.created= currentDate;
	        	}
	        }
		}
        if(this.lastEdited == null){
        	this.lastEdited=currentDate;
        }
        if(this.created == null){
        	this.created=currentDate;
        }
        if(this.uuid == null){
        	this.uuid=UUID.randomUUID();
        }
    }
    
    
    
    @JsonIgnore
    public String getDefinitionalHash(){
    	StringBuilder sb=new StringBuilder();
    	EntityMapper om = EntityMapper.FULL_ENTITY_MAPPER();
    	JsonNode jsn=om.valueToTree(this);
    	
    	Stream<String> fields=StreamUtil.forIterator(jsn.fieldNames())
    			                          .sorted();
    	
    	fields.forEach(new Consumer<String>(){
			@Override
			public void accept(String f) {
    		sb.append(f  +":" +  jsn.get(f).toString() + "\n");
    	}
    	});

    	
        return Util.sha1(sb.toString());
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
            	
                //Logger.error("Not a valid persistence Entity", ex);
            	
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

	@Override
	public void forceUpdate() {
		currentVersion++;
		super.update();
	}
	
	public boolean tryUpdate(){
		String ohash=getDefinitionalHash();
		super.update();
		String nhash=getDefinitionalHash();
		if(ohash.equals(nhash)){
			return false;
		}
		return true;
	}
	
	public boolean equals(Object o){
		if(o==null)return false;
		if(!(o instanceof GinasCommonData)){
			return false;
		}
		GinasCommonData g=(GinasCommonData)o;
		
		if(!(this.uuid+"").equals(g.uuid+"")){
			return false;
		}
		return true;
		
	}
	
	@Override
	public String fetchGlobalId(){
		if(this.uuid==null)return null;
		return this.uuid.toString();
	}
	
	
	@JsonIgnore
	/**
	 * This method returns true if the access-control
	 * group list is empty. This means that the data
	 * has not been flagged for group-level control.
	 * @return
	 */
	public boolean isPublic(){
		return this.getAccess().isEmpty();
	}
	
	
	public String toString(){
		return this.getClass().getSimpleName();
	}
	
}
