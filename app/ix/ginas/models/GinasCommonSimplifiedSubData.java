package ix.ginas.models;

import java.util.Date;
import java.util.Set;
import java.util.UUID;

import javax.persistence.MappedSuperclass;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import ix.core.models.Group;
import ix.core.models.Indexable;
import ix.core.models.Keyword;
import ix.core.models.Principal;

@SuppressWarnings("serial")
@MappedSuperclass
public class GinasCommonSimplifiedSubData extends GinasCommonSubData implements GinasAccessReferenceControlled{
	 	@JsonIgnore
	    public Set<Keyword> getReferences(){
	    	return super.getReferences();
	    }
	 	
	 	@JsonIgnore
	 	public UUID getUuid() {
			return uuid;
		}

	 	@JsonIgnore
	 	@JsonDeserialize(using = PrincipalDeserializer.class)
		public Principal getCreatedBy() {
			return super.getCreatedBy();
		}

	 	@JsonIgnore
		public Date getLastEdited() {
			return lastEdited;
		}

	 	@JsonIgnore
	 	@JsonDeserialize(using = PrincipalDeserializer.class)
		public Principal getLastEditedBy() {
			return super.getLastEditedBy();
		}

	 	@JsonIgnore
		public boolean isDeprecated() {
			return deprecated;
		}

	 	@JsonIgnore
		public Date getCreated() {
			return created;
		}
	 	
	 	
	 	@JsonIgnore
	 	 @JsonSerialize(using = GroupListSerializer.class)
	     public Set<Group> getAccess(){
	     	return super.getAccess();
	     }
	 	
	 	@JsonProperty("_self")
	 	@JsonIgnore
	    @Indexable(indexed=false)
	    public String getself () {
	 		return super.getself();
	    }
	 	
	 	
	 	
	
}
