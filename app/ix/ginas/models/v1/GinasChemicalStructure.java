package ix.ginas.models.v1;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import ix.core.models.AccessGroupRestricted;
import ix.core.models.Author;
import ix.core.models.Group;
import ix.core.models.Indexable;
import ix.core.models.Keyword;
import ix.core.models.Structure;
import ix.ginas.models.GinasAccess;
import ix.ginas.models.GinasData;
import ix.ginas.models.GinasReference;
import ix.ginas.models.GroupListSerializer;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.OneToOne;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@Entity
@DiscriminatorValue("GSRS")
public class GinasChemicalStructure extends Structure implements GinasData {

	@JsonIgnore
	@OneToOne(cascade = CascadeType.ALL)
	GinasAccess recordAccess;

	@JsonIgnore
	@OneToOne(cascade = CascadeType.ALL)
	GinasReference recordReference;

	@JsonProperty("access")
	public void setAccess(Collection<String> access) {
		ObjectMapper om = new ObjectMapper();
		Map mm = new HashMap();
		mm.put("access", access);
		mm.put("entityType", this.getClass().getName());
		JsonNode jsn = om.valueToTree(mm);
		try {
			recordAccess = om.treeToValue(jsn, GinasAccess.class);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		return;
	}

	@JsonSerialize(using = GroupListSerializer.class)
	public Set<Group> getAccess() {
		if (recordAccess != null) {
			return recordAccess.access;
		}
		return null;
	}

	@JsonSerialize(using = GroupListSerializer.class)
	public Set<Keyword> getReferences() {
		if (recordReference != null) {
			return recordReference.references;
		}
		return null;
	}

	@JsonProperty("references")
	public void setReferences(Collection<String> references) {
		System.out.println("Attempting reference set:" + references);
		ObjectMapper om = new ObjectMapper();
		Map mm = new HashMap();
		mm.put("references", references);
		mm.put("entityType", this.getClass().getName());
		JsonNode jsn = om.valueToTree(mm);
		try {
			recordReference = om.treeToValue(jsn, GinasReference.class);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		return;
	}

	public Set<Group> getAccessGroups() {
		return getAccess();
	}

	@Override
	public void addReference(String refUUID) {
		// TODO Auto-generated method stub

	}

	@Override
	public void addReference(Reference r) {
		// TODO Auto-generated method stub

	}

	@Override
	public void addRestrictGroup(Group p) {
		// TODO Auto-generated method stub

	}

	@Override
	public void addRestrictGroup(String group) {
		// TODO Auto-generated method stub

	}
}
