package ix.ginas.models;

import ix.core.models.Group;

import java.util.Collection;
import java.util.Set;

public interface GinasAccessControlled {
	public Set<Group> getAccess();
	public void addRestrictGroup(Group p);
	public void addRestrictGroup(String group);
	public void setAccess(Collection<String> access);
	
}
