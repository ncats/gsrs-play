package ix.ginas.models;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonIgnore;

import ix.core.models.Keyword;
import ix.ginas.models.v1.Reference;
import ix.ginas.models.v1.Substance;

public interface GinasAccessReferenceControlled extends GinasAccessControlled{
	void addReference(String refUUID);
	void addReference(Reference r);
	void addReference(Reference r, Substance s);
	Set<Keyword> getReferences();


	void setReferences(Set<Keyword> references);
	
	@JsonIgnore
	public List<GinasAccessReferenceControlled> getAllChildrenCapableOfHavingReferences();

	@JsonIgnore
	public default List<GinasAccessReferenceControlled> getAllChildrenAndSelfCapableOfHavingReferences(){
		List<GinasAccessReferenceControlled> temp = new ArrayList<GinasAccessReferenceControlled>();
		temp.add(this);
		temp.addAll(this.getAllChildrenCapableOfHavingReferences());
		return temp;

	}

	@JsonIgnore
	public default Set<UUID> getReferencesAsUUIDs(){
		return this.getReferences().stream()
				                   .map(new Function<Keyword,UUID>(){

										@Override
										public UUID apply(Keyword t) {
											return UUID.fromString(t.term);
										}

				                   })
				                   .collect(Collectors.toSet());
	}

}