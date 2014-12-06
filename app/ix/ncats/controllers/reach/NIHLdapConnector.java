package ix.ncats.controllers.reach;

import java.io.*;
import java.util.*;
import javax.naming.*;
import javax.naming.directory.*;
import javax.naming.ldap.*;
import java.text.*;

import ix.ncats.models.Employee;

public class NIHLdapConnector {
    /* 
     * change these values accordingly
     */
    static final public String LDAP_URL = "ldap://ldapad.nih.gov:389";
    static final String LDAP_PROTOCOL = "ssl";
    static final String LDAP_AUTHENTICATION = "DIGEST-MD5";
    static final String LDAP_BASEDN = 
        "OU=Users,OU=NCATS,OU=NIH,OU=AD,DC=nih,DC=gov";

    private SearchControls userSearchControls = 
	new SearchControls (SearchControls.SUBTREE_SCOPE, 0, 0, 
			    new String[]{"mail", 
					 "displayName", 
					 "givenName",
					 "telephoneNumber",
					 "employeeID",
					 "department"}, 
			    true, true);

    Hashtable env = new Hashtable ();
    public NIHLdapConnector (String username, String password) {
	env.put(Context.INITIAL_CONTEXT_FACTORY, 
		"com.sun.jndi.ldap.LdapCtxFactory");
	env.put(Context.PROVIDER_URL, LDAP_URL);
	//env.put(Context.SECURITY_PROTOCOL, LDAP_PROTOCOL);
	// don't need to define trustStore here... we're not validating
	//  the certificate
	//env.put( "java.naming.ldap.factory.socket",
	//	 DummySSLSocketFactory.class.getName());
	//env.put(Context.SECURITY_AUTHENTICATION, LDAP_AUTHENTICATION);
        env.put(Context.SECURITY_AUTHENTICATION, "simple");
	env.put(Context.SECURITY_PRINCIPAL, "cn="+username
                +",OU=Users,OU=NCATS,OU=NIH,OU=AD,DC=nih,DC=gov");
	env.put(Context.SECURITY_CREDENTIALS, password);
	//env.put("javax.security.sasl.qop", "auth");
	//env.put("javax.security.sasl.strength", "high");
    }

    public List<Employee> list () throws Exception {
        List<Employee> employees = new ArrayList<Employee>();
        DirContext ctx = new InitialDirContext (env);        
        NamingEnumeration<Binding> names = 
            ctx.listBindings(new LdapName (LDAP_BASEDN));
        while (names.hasMore()) {
            Binding b = names.next();
            LdapContext ldap = (LdapContext)b.getObject();
            Attributes attrs = ldap.getAttributes("");
            String id = getAttr (attrs, "employeeID");
            if (id != null) {
                Employee e = new Employee ();
                e.username = getAttr (attrs, "cn");
                e.email = getAttr (attrs, "mail");
                e.lastname = getAttr (attrs, "sn"); // last
                e.forename = getAttr (attrs, "givenName"); // first 
                e.affiliation = getAttr (attrs, "department");
                e.dn = getAttr (attrs, "distinguishedName");
                e.uid = Long.parseLong(id);
                e.phone = getAttr (attrs, "telephoneNumber");
                String suffix = getAttr (attrs, "personalTitle");
                if (suffix != null) {
                    if (suffix.startsWith("Dr")) {
                        // can't figure out whether it's MD, DDS, DSc, or PhD
                        e.suffix = "Ph.D."; 
                    }
                }
                employees.add(e);
            }
        }
        ctx.close();

        return employees;
    }

    static String getAttr (Attributes attrs, String name) 
        throws NamingException {
        Attribute a = attrs.get(name);
        return a != null ? a.get().toString() : "";
    }
}
