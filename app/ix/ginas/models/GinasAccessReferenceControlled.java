package ix.ginas.models;

import ix.core.models.Keyword;
import ix.ginas.models.v1.Reference;

import java.util.Collection;
import java.util.Set;

public interface GinasAccessReferenceControlled extends GinasAccessControlled{
	public void addReference(String refUUID);
	public void addReference(Reference r);
	public Set<Keyword> getReferences();
	public void setReferences(Collection<String> references);	
}