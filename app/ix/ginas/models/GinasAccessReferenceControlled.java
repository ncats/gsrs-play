package ix.ginas.models;

import ix.core.models.Keyword;
import ix.ginas.models.v1.Reference;
import ix.ginas.models.v1.Substance;

import java.util.Collection;
import java.util.Set;

public interface GinasAccessReferenceControlled extends GinasAccessControlled{
	void addReference(String refUUID);
	void addReference(Reference r);
	void addReference(Reference r, Substance s);
	Set<Keyword> getReferences();
	void setReferences(Set<Keyword> references);
	
	
}