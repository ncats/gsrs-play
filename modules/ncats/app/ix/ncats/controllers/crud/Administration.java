package ix.ncats.controllers.crud;

import be.objectify.deadbolt.java.actions.Dynamic;
import com.avaje.ebean.Ebean;
import com.avaje.ebean.Transaction;
import com.fasterxml.jackson.databind.JsonNode;
import ix.core.controllers.AdminFactory;
import ix.core.controllers.PrincipalFactory;
import ix.core.models.*;
import ix.ncats.controllers.App;
import ix.ncats.controllers.routes;
import play.Logger;
import play.Play;
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

    static String appContext = Play.application().configuration().getString("application.context");

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
        String groupName = requestData.get("grpName");

        ArrayList<Role.Kind> rolesChecked = new ArrayList<Role.Kind>();
        ArrayList<Acl> aclsChecked = new ArrayList<Acl>();
        List<Group> groupsChecked = new ArrayList<Group>();


        if(groupName != null && !groupName.isEmpty()){
            Group grp = new Group(groupName);
            groupsChecked.add(grp);
        }

        for (String key : requestData.data().keySet()) {
            if (key.contains("r-")) {
                Role r = new Role(Role.Kind.valueOf(requestData.data().get(key)));
                rolesChecked.add(r.role);
            }

            if (key.contains("p-")) {
                Acl p = new Acl(Acl.Permission.valueOf(requestData.data().get(key)));
                aclsChecked.add(p);
            }

            if (key.contains("g-")) {
                String grpName = requestData.data().get(key);
                Group group = AdminFactory.groupfinder.where().eq("name", grpName).findUnique();
                if(group == null){
                    group = new Group(requestData.data().get(key));
                }
                groupsChecked.add(group);
            }
        }
        newUser.save();
        UserProfile prof = _profiles.where().eq("user.username", newUser.username).findUnique();

        if (prof == null) {
            prof = new UserProfile(newUser);
            prof.active = Boolean.parseBoolean(requestData.get("active"));
            prof.setPassword(requestData.get("password"));
            prof.setRoleKinds(rolesChecked);

            for (Acl p : aclsChecked) {
                p.principals.add(newUser);
                p.save();
            }

            for (Group g : groupsChecked) {
                if(g.id != null) {
                    g.members.add(newUser);
                    g.saveManyToManyAssociations("members");
                }else {
                    g.members.add(newUser);
                    g.save();
                }
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
        String groupName = requestData.get("grpName");

        List<Role.Kind> selectedRoles = new ArrayList<Role.Kind>();
        List<Acl> selectedPerms = new ArrayList<Acl>();
        List<Group> selectedGroups = new ArrayList<Group>();

        if(groupName != null && !groupName.isEmpty()){
            Group grp = new Group(groupName);
            selectedGroups.add(grp);
        }

        for (String key : requestData.data().keySet()) {
            if (key.contains("r-")) {
                Role r = new Role(Role.Kind.valueOf(requestData.data().get(key)));
                selectedRoles.add(r.role);
            }

            if (key.contains("p-")) {
                String permName = requestData.data().get(key);
                Acl perm = AdminFactory.aclFinder.where().eq("Permission", Acl.Permission.valueOf(permName)).findUnique();
                if(perm == null) {
                    perm = new Acl(Acl.Permission.valueOf(permName));
                }
                selectedPerms.add(perm);
            }

            if (key.contains("g-")) {
                String grpName = requestData.data().get(key);
                Group group = AdminFactory.groupfinder.where().eq("name", grpName).findUnique();
                if(group == null){
                    group = new Group(requestData.data().get(key));
                }
                selectedGroups.add(group);
            }
        }

        user.username = (!userName.isEmpty() && userName != null) ? userName : user.username;
        user.email = (!email.isEmpty() && email != null) ? email : user.email;
        user.save();
        profile.user = user;

        if(!password.isEmpty() && password != null) {
        	profile.setPassword(password);
            
        }
        profile.active = Boolean.parseBoolean(active);
        profile.setRoleKinds(selectedRoles);
        AdminFactory.updateGroups(user.id, selectedGroups);
        AdminFactory.updatePermissions(user.id, selectedPerms);
        //AdminFactory.updateRoles(user.id, selectedRoles);
        profile.save();

        flash("success", " " + userName + " has been updated");
        return index();
    }

    public static Result editPrincipal(Long id) {
        Principal user = AdminFactory.palFinder.byId(id);
        UserProfile up = _profiles.where().eq("user.username", user.username).findUnique();
        up.user = user;
        return ok(ix.ncats.views.html.admin.edituser.render(
        		id, 
        		up,
        		up.getRolesKinds(), 
        		AdminFactory.aclNamesByPrincipal(user),
                AdminFactory.groupNamesByPrincipal(user), 
                appContext
                ));
    }

    public static Result deletePrincipal(Long id) {
        return AdminFactory.setUserToInactive(id);
    }

    public static Result create() {
        Form<UserProfile> userForm = Form.form(UserProfile.class);
        return ok(ix.ncats.views.html.admin.createuser.render(userForm, appContext));
    }
}
