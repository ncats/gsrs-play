package ix.ginas.models;

import ix.core.models.Keyword;
import ix.ginas.models.v1.Reference;

import java.util.Collection;
import java.util.Set;

public interface GinasAccessReferenceControlled extends GinasAccessControlled{
	void addReference(String refUUID);
	void addReference(Reference r);
	Set<Keyword> getReferences();
	void setReferences(Set<Keyword> references);
	
	
}