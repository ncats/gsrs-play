package ix.ncats.controllers.crud;

import be.objectify.deadbolt.java.actions.Dynamic;
import ix.core.controllers.AdminFactory;
import ix.core.controllers.PrincipalFactory;
import ix.core.controllers.UserProfileFactory;
import ix.core.models.*;
import ix.core.util.StreamUtil;
import ix.ncats.controllers.App;
import ix.ncats.controllers.routes;
import ix.ncats.controllers.security.IxDynamicResourceHandler;
import play.Play;
import play.data.DynamicForm;
import play.data.Form;
import play.db.ebean.Model;
import play.mvc.Result;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

//@Dynamic(value = "viewUserList", handlerKey = "idg")
public class Administration extends App {

    public static String appContext = Play.application().configuration().getString("application.context");

    public static Result index() {
        return redirect(ix.ncats.controllers.crud.routes.Administration.listPrincipals(1, "", "", ""));
    }

    @Dynamic(value = IxDynamicResourceHandler.IS_ADMIN, handler = ix.ncats.controllers.security.IxDeadboltHandler.class)
    public static Result listPrincipals(int page, String sortBy, String order, String filter) {
        return _listPrincipals(1, 10, sortBy, order, filter);
    }

    @Dynamic(value = IxDynamicResourceHandler.IS_ADMIN, handler = ix.ncats.controllers.security.IxDeadboltHandler.class)
    public static Result _listPrincipals(int page, int rows, String sortBy, String order, String filter) {

        List<UserProfile> profiles = principalsList();
        return ok(ix.ncats.views.html.admin.users.render(profiles, sortBy, order, filter));
    }

    //TODO:
    // Look at this
    public static List<UserProfile> principalsList() {
        List<Principal> users = PrincipalFactory.all();
        List<UserProfile> profiles = StreamUtil.ofIterator(UserProfileFactory.users()).collect(Collectors.toList());
        
        for (Principal p : users) {
            UserProfile pri = p.getUserProfile();
            if (pri == null) {
                UserProfile prof = new UserProfile(p);
                prof.active = true;
                prof.save();
                profiles.add(prof);
            }
        }
        return profiles;
    }



    @Dynamic(value = IxDynamicResourceHandler.IS_ADMIN, handler = ix.ncats.controllers.security.IxDeadboltHandler.class)
    public static Result addPrincipal() {
        DynamicForm requestData = Form.form().bindFromRequest();
        if (requestData.hasErrors()) {
            return ok();//badRequest(createForm.render(userForm));
        }

        addUser(requestData);
        return index();
    }

    public static void addUser(DynamicForm requestData) {
        Principal newUser = new Principal();
        newUser.admin = Boolean.parseBoolean(requestData.get("admin"));
        newUser.email = requestData.get("email");
        newUser.username = requestData.get("username");
        String groupName = requestData.get("grpName");

        ArrayList<Role> rolesChecked = new ArrayList<Role>();
       // ArrayList<Acl> aclsChecked = new ArrayList<Acl>();
        List<Group> groupsChecked = new ArrayList<Group>();


        if(groupName != null && !groupName.isEmpty()){
            Group grp = new Group(groupName);
            groupsChecked.add(grp);
        }

        for (String key : requestData.data().keySet()) {
            if (key.contains("r-")) {
                Role r = Role.valueOf(requestData.data().get(key));
                rolesChecked.add(r);
            }

           /* if (key.contains("p-")) {
                Acl p = new Acl(Acl.Permission.valueOf(requestData.data().get(key)));
                aclsChecked.add(p);
            }*/

            if (key.contains("g-")) {
                String grpName = requestData.data().get(key);
                Group group = AdminFactory.groupfinder.get().where().eq("name", grpName).findUnique();
                if(group == null){
                    group = new Group(requestData.data().get(key));
                }
                groupsChecked.add(group);
            }
        }
        newUser.save();
        UserProfile prof = newUser.getUserProfile();

        if (prof == null) {
            prof = new UserProfile(newUser);
            prof.active = Boolean.parseBoolean(requestData.get("active"));
            prof.setPassword(requestData.get("password"));
            prof.setRoles(rolesChecked);

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
    }

    @Dynamic(value = IxDynamicResourceHandler.IS_ADMIN, handler = ix.ncats.controllers.security.IxDeadboltHandler.class)
    public static Result updatePrincipal(long id) {
        updateUser(id);
        return index();
    }

    //Not returning view - to call from other modules (ex: ginas)
    public static void updateUser(long id) {
        DynamicForm requestData = Form.form().bindFromRequest();

        Principal user = AdminFactory.palFinder.get().byId(id);
        UserProfile profile = user.getUserProfile();

        String userName = requestData.get("username");
        String password = requestData.get("password");
        String email = requestData.get("email");
        String active = requestData.get("active");
        String groupName = requestData.get("grpName");

        List<Role> selectedRoles = new ArrayList<Role>();
        List<Acl> selectedPerms = new ArrayList<Acl>();
        List<Group> selectedGroups = new ArrayList<Group>();

        if(groupName != null && !groupName.isEmpty()){
            Group grp = new Group(groupName);
            selectedGroups.add(grp);
        }

        for (String key : requestData.data().keySet()) {
            if (key.contains("r-")) {
                Role r = Role.valueOf(requestData.data().get(key));
                selectedRoles.add(r);
            }

            if (key.contains("g-")) {
                String grpName = requestData.data().get(key);
                Group group = AdminFactory.groupfinder.get().where().eq("name", grpName).findUnique();
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

        if(password != null && !password.isEmpty()) {
            profile.setPassword(password);
        }
        profile.active = Boolean.parseBoolean(active);
        profile.setRoles(selectedRoles);
        AdminFactory.updateGroups(user.id, selectedGroups);
        profile.save();

        flash("success", " " + userName + " has been updated");
    }

    @Dynamic(value = IxDynamicResourceHandler.IS_ADMIN, handler = ix.ncats.controllers.security.IxDeadboltHandler.class)
    public static Result editPrincipal(Long id) {
        UserProfile up = editUser(id);
        return ok(ix.ncats.views.html.admin.edituser.render(
        		id, 
        		up,
        		up.getRoles(),
        		AdminFactory.aclNamesByPrincipal(up.user),
                AdminFactory.groupNamesByPrincipal(up.user),
                appContext
                ));
    }

    @Dynamic(value = IxDynamicResourceHandler.IS_ADMIN, handler = ix.ncats.controllers.security.IxDeadboltHandler.class)
    public static UserProfile editUser(Long id) {
        Principal user = AdminFactory.palFinder.get().byId(id);
        UserProfile up = user.getUserProfile();
        up.user = user;
        return up;
    }

    //TODO : Fix this too
    @Dynamic(value = IxDynamicResourceHandler.IS_ADMIN, handler = ix.ncats.controllers.security.IxDeadboltHandler.class)
    public static Result deletePrincipal(Long id) {
        return AdminFactory.setUserToInactive(id);
    }


    @Dynamic(value = IxDynamicResourceHandler.IS_ADMIN, handler = ix.ncats.controllers.security.IxDeadboltHandler.class)
    public static Result create() {
        Form<UserProfile> userForm = Form.form(UserProfile.class);
        return ok(ix.ncats.views.html.admin.createuser.render(userForm, appContext));
    }
}
