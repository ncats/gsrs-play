package ix.ncats.controllers.reach;

import java.util.*;

import play.*;
import play.db.ebean.*;
import play.data.*;
import play.mvc.*;

import ix.core.NamedResource;
import ix.ncats.models.Employee;
import ix.core.controllers.EntityFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

@NamedResource(name="employees", type=Employee.class)
public class EmployeeFactory extends EntityFactory {
    static final public Model.Finder<Long, Employee> finder = 
        new Model.Finder(Long.class, Employee.class);

    public interface Callback {
        public boolean ok (Employee e);
    }

    public static List<Employee> all () { return all (finder); }
    public static Employee getEmployee (Long id) {
        return getEntity (id, finder);
    }
    public static Result count () {
        return count (finder);
    }
    public static Integer getCount () {
        try {
            return getCount (finder);
        }
        catch (Exception ex) {
            Logger.trace("Can't get row count", ex);
        }
        return null;
    }

    public static Result page (int top, int skip) {
        return EmployeeFactory.page(top, skip, null, null);
    }
    public static Result page (int top, int skip, String filter) {
        return page (top, skip, filter, finder);
    }

    public static Result edits (Long id) {
        return edits (id, Employee.class);
    }

    public static Result get (Long id, String expand) {
        return get (id, expand, finder);
    }

    public static Result field (Long id, String path) {
        return field (id, path, finder);
    }

    public static Result create () {
        return create (Employee.class, finder);
    }

    public static Result delete (Long id) {
        return delete (id, finder);
    }

    public static Result update (Long id, String field) {
        return update (id, field, Employee.class, finder);
    }

    public static int createIfEmpty (String username, String password) {
        return createIfEmpty (username, password, null);
    }

    public static int createIfEmpty (String username, 
                                     String password, Callback callback) {
        Integer count = getCount ();
        if (count == null || count == 0) {
            try {            
                NIHLdapConnector ldap = 
                    new NIHLdapConnector (username, password);
                List<Employee> employees = ldap.list();
                for (Employee e : employees) {
                    if (callback == null 
                        || (callback != null && callback.ok(e))) {
                        Logger.info("lastname=\""+e.lastname
                                    +"\" forename=\""+e.forename+"\"");
                        e.save();
                    }
                }
                count = employees.size();
            }
            catch (Exception ex) {
                Logger.trace("Can't sync with LDAP", ex);
            }
        }
        return count;
    }

    public static Result list () {
        DynamicForm requestData = Form.form().bindFromRequest();
        String username = requestData.get("username");
        String password = requestData.get("password");

        try {
            NIHLdapConnector ldap = new NIHLdapConnector (username, password);
            List<Employee> employees = ldap.list();

            ObjectMapper mapper = new ObjectMapper ();
            for (Employee e : employees) {
                e.save();
                Logger.debug(mapper.writerWithDefaultPrettyPrinter()
                             .writeValueAsString(e));
            }

            return ok ("Loaded "+employees.size()+" record(s)!");
        }
        catch (Exception ex) {
            Logger.trace("Can't list LDAP", ex);
            return internalServerError (ex.getMessage());
        }
    }

    public static Result ldap () {
        return ok (ix.ncats.views.html.ldap.render("LDAP Listing"));
    }
}
