package ix.ncats.controllers.crud;

import be.objectify.deadbolt.java.actions.Dynamic;
import com.avaje.ebean.Ebean;
import com.avaje.ebean.Transaction;
import com.fasterxml.jackson.databind.JsonNode;
import ix.core.controllers.AdminFactory;
import ix.core.controllers.PrincipalFactory;
import ix.core.models.Acl;
import ix.core.models.Principal;
import ix.core.models.Role;
import ix.core.models.UserProfile;
import ix.ncats.controllers.App;
import ix.ncats.controllers.routes;
import play.Logger;
import play.data.DynamicForm;
import play.data.Form;
import play.db.ebean.Model;
import play.mvc.Result;
import play.mvc.Controller;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

//@Dynamic(value = "viewUserList", handlerKey = "idg")
public class Administration extends App {

    static Model.Finder<Long, UserProfile> _profiles =
            new Model.Finder<Long, UserProfile>(Long.class, UserProfile.class);

    public static Result index() {
        return redirect(ix.ncats.controllers.crud.routes.Administration.listPrincipals(1, "", "", ""));
    }

    public static Result listPrincipals(int page, String sortBy, String order, String filter) {
        return _listPrincipals(1, 10, sortBy, order, filter);
    }

    public static Result _listPrincipals(int page, int rows, String sortBy, String order, String filter) {
        List<Principal> users = PrincipalFactory.all();
        List<UserProfile> profiles = _profiles.all();
        for (Principal p : users) {
            UserProfile pri = _profiles.where().eq("user.username", p.username).findUnique();
            if (pri == null) {
                UserProfile prof = new UserProfile(p);
                prof.active = true;
                prof.save();
                profiles.add(prof);
            }
        }
        return ok(ix.ncats.views.html.admin.users.render(profiles, sortBy, order, filter));
    }



    //@Dynamic(value = "adminUser", handlerKey = "idg")
    public static Result addPrincipal() {
        DynamicForm requestData = Form.form().bindFromRequest();
        if (requestData.hasErrors()) {
            return ok();//badRequest(createForm.render(userForm));
        }

        Principal newUser = new Principal();
        newUser.admin = Boolean.parseBoolean(requestData.get("admin"));
        newUser.email = requestData.get("email");
        newUser.username = requestData.get("username");

        ArrayList<Role> rolesChecked = new ArrayList<Role>();
        ArrayList<Acl> aclsChecked = new ArrayList<Acl>();

        for (String key : requestData.data().keySet()) {
            if (key.contains("r-")) {
                Role r = new Role(Role.Kind.valueOf(requestData.data().get(key)));
                rolesChecked.add(r);
            }

            if (key.contains("p-")) {
                Acl p = new Acl(Acl.Permission.valueOf(requestData.data().get(key)));
                aclsChecked.add(p);
            }
        }
        newUser.save();
        UserProfile prof = _profiles.where().eq("user.username", newUser.username).findUnique();

        if (prof == null) {
            prof = new UserProfile(newUser);
            prof.active = Boolean.parseBoolean(requestData.get("active"));
            prof.salt = AdminFactory.generateSalt();
            prof.hashp = AdminFactory.encrypt(requestData.get("password"), prof.salt);
            for (Role r : rolesChecked) {
                r.principal = newUser;
                r.save();
            }

            for (Acl p : aclsChecked) {
                p.principals.add(newUser);
                AdminFactory.registerAclIfAbsent(p);
                p.save();
            }
            prof.save();
        }
        flash("success", " " + requestData.get("username") + " has been created");
        return index();
    }

    //@Dynamic(value = "adminUser", handlerKey = "idg")
    public static Result updatePrincipal(long id) {
        DynamicForm requestData = Form.form().bindFromRequest();

        Principal user = AdminFactory.palFinder.byId(id);
        UserProfile profile = _profiles.where().eq("user.username", user.username).findUnique();

        String userName = requestData.get("username");
        String password = requestData.get("password");
        String email = requestData.get("email");
        String active = requestData.get("active");


        List<Role> selectedRoles = new ArrayList<Role>();
        List<Acl> selectedPerms = new ArrayList<Acl>();

        for(Role r : AdminFactory.rolesByPrincipal(user)){
            AdminFactory.deleteRole(r.id);
        }

        for(Acl p : AdminFactory.permissionByPrincipal(user)){
            AdminFactory.deleteUserPermission(p.id, user.id);
        }

        for (String key : requestData.data().keySet()) {
            if (key.contains("r-")) {
                Role r = new Role(Role.Kind.valueOf(requestData.data().get(key)));
                selectedRoles.add(r);
            }

            if (key.contains("p-")) {
                Acl p = new Acl(Acl.Permission.valueOf(requestData.data().get(key)));
                selectedPerms.add(p);
            }
        }

        user.username = (!userName.isEmpty() && userName != null) ? userName : user.username;
        user.email = (!email.isEmpty() && email != null) ? email : user.email;
        user.save();
        profile.user = user;

        if(!password.isEmpty() && password != null) {
            profile.hashp = AdminFactory.encrypt(password, profile.salt);
        }
        profile.active = Boolean.parseBoolean(active);
        for (Role r : selectedRoles) {
           r.principal = user;
            r.save();
        }

        for (Acl p : selectedPerms) {
          p.principals.add(user);
            //AdminFactory.registerAclIfAbsent(p);
            p.save();
        }
        profile.save();

        flash("success", " " + userName + " has been updated");
        return index();
    }

    public static Result editPrincipal(Long id) {
        Principal user = AdminFactory.palFinder.byId(id);
        UserProfile up = _profiles.where().eq("user.username", user.username).findUnique();
        up.user = user;
        return ok(ix.ncats.views.html.admin.edituser.render(id, up, AdminFactory.roleNamesByPrincipal(user), AdminFactory.aclNamesByPrincipal(user)));
    }

    //  @Dynamic(value = "adminUser", handlerKey = "idg")
    public static Result deletePrincipal(Long id) {
        return AdminFactory.setToInactive(id);
    }

    public static Result create() {
        Form<UserProfile> userForm = Form.form(UserProfile.class);
        return ok(ix.ncats.views.html.admin.createuser.render(userForm));
    }
}
