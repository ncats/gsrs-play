package ix.core;

import java.util.concurrent.atomic.AtomicBoolean;

import ix.core.controllers.PrincipalFactory;
import ix.core.controllers.UserProfileFactory;
import ix.core.models.Principal;
import ix.core.models.UserProfile;
import ix.ncats.controllers.auth.Authentication;

public class UserFetcher {

	public static AtomicBoolean DEFAULT_FORCE = new AtomicBoolean(true);

	private static final String DEFAULT_USERNAME = "GUEST";

	private static ThreadLocal<Principal> localUser = new ThreadLocal<Principal>();

	private static ThreadLocal<Boolean> forceAuditUpdate = new ThreadLocal<Boolean>() {
		@Override
		protected Boolean initialValue() {
			return DEFAULT_FORCE.get();
		}
	};

	public static String getDefaultUsername() {
		return DEFAULT_USERNAME;
	}

	public static Principal getActingUser(boolean allowGuest) {
		try {
			Principal p = Authentication.getUser();
			if (p != null){
				return p;
			}
		} catch (Exception e) {
			
		}
		Principal p = localUser.get();
		if (p != null){
			return p;
		}
		if (allowGuest){
			return PrincipalFactory.registerIfAbsent(new Principal(DEFAULT_USERNAME, null));
		}
		return null;
	}

	/**
	 * Get the acting user, allowing a default guest account. This is
	 * equivalent to :
	 * <pre>
	 * <code>
	 * getActingUser(true);
	 * </code>
	 * </pre>
	 * @return A Principal
	 */
	public static Principal getActingUser() {
		return getActingUser(true);
	}

	
	public static void setLocalThreadUser(Principal p) {
		localUser.set(p);
	}

	public static UserProfile getActingUserProfile(boolean allowGuest) {
		Principal p = getActingUser(allowGuest);
		if (p != null) {
			UserProfile up = p.getUserProfile();
			if (up != null) {
				return up;
			} else {
				
			}

		}
		return null;
	}

	public static void enableForceAuditUpdate() {
		forceAuditUpdate.set(true);
	}

	public static void disableForceAuditUpdate() {
		forceAuditUpdate.set(false);
	}

	public static boolean isForceAuditUpdate() {
		return forceAuditUpdate.get();
	}

	public static void globalEnableForceAuditUpdate() {
		DEFAULT_FORCE.set(true);
	}

	public static void globalDisableForceAuditUpdate() {
		DEFAULT_FORCE.set(false);
	}

}
