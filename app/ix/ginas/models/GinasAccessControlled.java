package ix.ginas.models;

import ix.core.models.Group;

import java.util.Collection;
import java.util.Set;

/**
 * Locks down a ginas object so that only
 * users who belong to particular {@link Group}s
 * can access this object.
 */
public interface GinasAccessControlled {
	Set<Group> getAccess();
	void setAccess(Set<Group> access);
	void addRestrictGroup(Group p);
	void addRestrictGroup(String group);
	
}
