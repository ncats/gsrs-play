package ix.core.models;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.UUID;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinTable;
import javax.persistence.Lob;
import javax.persistence.ManyToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Column;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;

import be.objectify.deadbolt.core.models.Subject;
import ix.core.controllers.AdminFactory;
import ix.core.util.TimeUtil;
import ix.utils.Util;
import play.Logger;

@Entity
@Table(name = "ix_core_userprof")
public class UserProfile extends IxModel implements Subject {
	@Basic(fetch = FetchType.EAGER)
	@OneToOne(cascade = CascadeType.ALL)
	public Principal user;

	// is the profile currently active? authorization should take
	// this into account
	public boolean active;

	private String hashp;
	private String salt;
	public boolean systemAuth; // FDA, NIH employee

	@Lob
	@JsonIgnore
	private String rolesJSON = null; // this is a silly, but quick way to
	// serialize roles

	// private key to be used in authentication
	// This is not a public/private key,
	// just a special secret to be used via API
	@Column(name = "apikey")
	private String key;

	// Not sure if this should be shown here?
	public String getKey() {
		return key;
	}

	public void regenerateKey() {
		key = Util.generateRandomString(20);
	}

	@ManyToMany(cascade = CascadeType.ALL)
	@JoinTable(name = "ix_core_userprof_prop")
	public List<Value> properties = new ArrayList<Value>();

	public UserProfile() {
	}

	public UserProfile(Principal user) {
		this.user = user;
		regenerateKey();
	}

	public List<Role> getRoles() {
		List<Role> rolekinds = new ArrayList<Role>();
		if (this.rolesJSON != null) {
			try {
				ObjectMapper om = new ObjectMapper();
				List l = om.readValue(rolesJSON, List.class);
				for (Object o : l) {
					try {
						rolekinds.add(Role.valueOf(o.toString()));
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			} catch (Exception e) {
				Logger.warn(e.getMessage(), e);
			}
		}
		return rolekinds;
	}

	public void setRoles(List<Role> rolekinds) {
		ObjectMapper om = new ObjectMapper();
		rolesJSON = om.valueToTree(rolekinds).toString();
	}

	public void addRole(Role role) {
		List<Role> roles = getRoles();
		roles.add(role);
		setRoles(new ArrayList<Role>(new LinkedHashSet<Role>(roles)));
	}

	public boolean hasRole(Role role) {
		return this.getRoles().contains(role);
	}

	@Override
	public List<Acl> getPermissions() {
		return AdminFactory.permissionByPrincipal(user); // return permissions;
	}

	public List<Group> getGroups() {
		return AdminFactory.groupsByPrincipal(user);
	}

	@Override
	public String getIdentifier() {
		return user.username;
	}

	public String getComputedToken() {
		String date = "" + Util.getCanonicalCacheTimeStamp();
		return Util.sha1(date + this.user.username + this.key);
	}

	public Long getTokenTimeToExpireMS() {
		long date = (Util.getCanonicalCacheTimeStamp() + 1) * Util.getTimeResolutionMS();
		return (date - TimeUtil.getCurrentTimeMillis());
	}

	private String getPreviousComputedToken() {
		String date = "" + (Util.getCanonicalCacheTimeStamp() - 1);
		return Util.sha1(date + this.user.username + this.key);
	}

	public boolean acceptKey(String key) {
		if (key.equals(this.key))
			return true;
		return false;
	}

	public boolean acceptToken(String token) {
		if (this.getComputedToken().equals(token))
			return true;
		if (this.getPreviousComputedToken().equals(token))
			return true;
		return false;
	}

	public boolean acceptPassword(String password) {
		if (this.hashp == null || this.salt == null)
			return false;
		return this.hashp.equals(Util.encrypt(password, this.salt));
	}

	public void setPassword(String password) {
		if (password == null || password.length() <= 0) {
			password = UUID.randomUUID().toString();
		}
		this.salt = AdminFactory.generateSalt();
		this.hashp = Util.encrypt(password, this.salt);
	}



	public static UserProfile GUEST() {
		UserProfile up = new UserProfile(new Principal("GUEST"));
		up.addRole(Role.Query);

		return up;
	}

	public boolean isRoleQueryOnly(){

		if(this.hasRole(Role.Query) && this.getRoles().size()==1){
			return true;

		}
		return false;
	}

}
