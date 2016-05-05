package ix.ncats.controllers;

import java.io.*;
import java.util.*;
import javax.naming.*;
import javax.naming.directory.*;
import javax.naming.ldap.*;
import java.text.*;
import ix.core.models.Principal;
import ix.ncats.controllers.auth.Authenticator;
import ix.ncats.models.Employee;

public class NIHLdapConnector implements Authenticator{
    /* 
     * change these values accordingly
     */
    static final public String LDAP_URL = "ldap://ldapad.nih.gov:389";
    static final String LDAP_PROTOCOL = "ssl";
    static final String LDAP_AUTHENTICATION = "DIGEST-MD5";
    static final String LDAP_BASEDN = 
        "OU=Users,OU=NCATS,OU=NIH,OU=AD,DC=nih,DC=gov";
    static final String LDAP_BASEDN2 = 
        "OU=Users,OU=NCATS OD Transition,OU=NCATS,OU=NIH,OU=AD,DC=nih,DC=gov";

    private SearchControls userSearchControls = 
        new SearchControls (SearchControls.SUBTREE_SCOPE, 0, 0, 
                            new String[]{"mail", 
                                         "displayName", 
                                         "givenName",
                                         "telephoneNumber",
                                         "employeeID",
                                         "department"}, 
                            true, true);

    public static boolean authenticate (String username, String password) {
        NIHLdapConnector ldap = new NIHLdapConnector (username, password);
        return ldap.authenticate();
    }
    
    
    
    public static Employee getEmployee (String username, String password) {
        NIHLdapConnector ldap = new NIHLdapConnector (username, password);
        return ldap.getEmployee();
    }
    
    private Hashtable env = new Hashtable ();
    private Employee empl;
    
    public NIHLdapConnector (String username, String password) {
        env.put(Context.INITIAL_CONTEXT_FACTORY, 
                "com.sun.jndi.ldap.LdapCtxFactory");
        env.put(Context.PROVIDER_URL, LDAP_URL);
        //env.put(Context.SECURITY_PROTOCOL, LDAP_PROTOCOL);
        // don't need to define trustStore here... we're not validating
        //  the certificate
        //env.put( "java.naming.ldap.factory.socket",
        //       DummySSLSocketFactory.class.getName());
        //env.put(Context.SECURITY_AUTHENTICATION, LDAP_AUTHENTICATION);
        env.put(Context.SECURITY_AUTHENTICATION, "simple");
        env.put(Context.SECURITY_PRINCIPAL, "cn="+username+","+LDAP_BASEDN);
        env.put(Context.SECURITY_CREDENTIALS, password);
        //env.put("javax.security.sasl.qop", "auth");
        //env.put("javax.security.sasl.strength", "high");
    }

    public Employee getEmployee () {
        if (empl == null) {
            DirContext ctx = null;
            try {
                ctx = new InitialDirContext (env);
                LdapContext ldap = (LdapContext)ctx.lookup
                    (new LdapName ((String)env.get(Context.SECURITY_PRINCIPAL)));
                empl = createEmployee (ldap);
            }
            catch (Exception ex) {
                play.Logger.warn("Can't authenticate principal \""
                                 +env.get(Context.SECURITY_PRINCIPAL)+"\"", ex);
            }
            finally {
                if (ctx != null) {
                    try {
                        ctx.close();
                    }
                    catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }
        }
        return empl;
    }
    
    public boolean authenticate () {
        return getEmployee () != null;
    }

    void list (DirContext ctx, String basedn, List<Employee> employees) throws Exception {
        NamingEnumeration<Binding> names = ctx.listBindings(new LdapName (basedn));
        while (names.hasMore()) {
            Binding b = names.next();
            LdapContext ldap = (LdapContext)b.getObject();
            Employee e = createEmployee (ldap);
            if (e != null) {
                employees.add(e);
            }
        }
    }
    
    public List<Employee> list () throws Exception {
        List<Employee> employees = new ArrayList<Employee>();
        DirContext ctx = new InitialDirContext (env);
        list (ctx, LDAP_BASEDN, employees);
        list (ctx, LDAP_BASEDN2, employees);
        ctx.close();
        return employees;
    }

    static void instrument (Employee e, Attributes attrs)
        throws NamingException {
        e.username = getAttr (attrs, "cn");
        e.email = getAttr (attrs, "mail");
        e.lastname = getAttr (attrs, "sn"); // last
        e.forename = getAttr (attrs, "givenName"); // first 
        e.affiliation = getAttr (attrs, "department");
        e.dn = getAttr (attrs, "distinguishedName");
        e.phone = getAttr (attrs, "telephoneNumber");
        String suffix = getAttr (attrs, "personalTitle");
        if (suffix != null) {
            if (suffix.startsWith("Dr")) {
                // can't figure out whether it's MD, DDS, DSc, or PhD
                e.suffix = "Ph.D.";
                if ("austin".equalsIgnoreCase(e.lastname)
                    && "christopher".equalsIgnoreCase(e.forename)) {
                    e.suffix = "M.D.";
                }
            }
        }
    }
    
    protected static Employee createEmployee (LdapContext ldap)
        throws NamingException {
        Employee e = null;
        
        Attributes attrs = ldap.getAttributes("");
        String id = getAttr (attrs, "employeeID");
        if (id != null) {
            e = new Employee ();
            e.provider = LDAP_URL;
            e.uid = Long.parseLong(id);     
            instrument (e, attrs);
        }
        else {
            play.Logger.warn(getAttr (attrs, "sn")+", "
                             +getAttr (attrs, "givenName")
                             +" doesn't have employeeID");
        }
        return e;
    }

    static String getAttr (Attributes attrs, String name) 
        throws NamingException {
        Attribute a = attrs.get(name);
        return a != null ? a.get().toString() : "";
    }

    @Override
    public Principal getUser(String username, String password){
    	return getEmployee(username,password);
    }
    
    public NIHLdapConnector(){
    	
    }

}
