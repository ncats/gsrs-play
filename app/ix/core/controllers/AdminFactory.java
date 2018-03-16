package ix.core.controllers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.Transaction;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import ix.core.auth.AuthenticationCredentials;
import ix.core.auth.Authenticator;
import ix.core.factories.AuthenticatorFactory;
import ix.core.models.Acl;
import ix.core.models.Group;
import ix.core.models.Namespace;
import ix.core.models.Principal;
import ix.core.models.Role;
import ix.core.models.UserProfile;
import ix.core.util.CachedSupplier;
import ix.core.util.Java8Util;
import ix.core.util.TimeUtil;
import ix.utils.Util;
import play.Logger;
import play.Play;
import play.db.ebean.Model;
import play.mvc.BodyParser;
import play.mvc.Controller;
import play.mvc.Result;

public class AdminFactory extends Controller {
    static public CachedSupplier<Model.Finder<Long, Namespace>> resFinder = Util.finderFor(Long.class,Namespace.class);
    static public CachedSupplier<Model.Finder<Long, Acl>> aclFinder= Util.finderFor(Long.class,Acl.class);
    static public CachedSupplier<Model.Finder<Long, Principal>> palFinder= Util.finderFor(Long.class,Principal.class);
    static public CachedSupplier<Model.Finder<Long, Role>> roleFinder= Util.finderFor(Long.class,Role.class);
    static public CachedSupplier<Model.Finder<Long, UserProfile>> proFinder=Util.finderFor(Long.class,UserProfile.class);
    public static CachedSupplier<Model.Finder<Long, Group>> groupfinder=Util.finderFor(Long.class,Group.class);
    
    
    

    private static CachedSupplier<Map<String,Group>> alreadyRegistered = CachedSupplier.of(()->{
    	return new ConcurrentHashMap<String,Group>();
    });
    
    private static  CachedSupplier<Map<Long, Group>> groupCache = CachedSupplier.of(()->{
    	return new ConcurrentHashMap<Long,Group>();
    });
    
    

    @BodyParser.Of(value = BodyParser.Json.class)
    public static Result createUser() {
        if (request().body().isMaxSizeExceeded()) {
            return badRequest("Json content too large!");
        }
        
        Principal pal = null;
        try {
            JsonNode node = request().body().asJson();
            ObjectMapper mapper = new ObjectMapper();
            pal = mapper.treeToValue(node, Principal.class);
            pal.save();

            return Java8Util.ok(mapper.valueToTree(pal));
        } catch (Exception ex) {
            Logger.error("Can't create new user", ex);
            if (pal != null)
                return badRequest
                        ("Principal \"" + pal.uri + "\" is not available!");

            return internalServerError
                    ("Unable to process request due to internal server error!");
        }
    }
    
    

    public static Result getUser(Long id) {
        Principal pal = palFinder.get().byId(id);
        if (pal != null) {
            ObjectMapper mapper = new ObjectMapper();
            return Java8Util.ok(mapper.valueToTree(pal));
        }
        return notFound("Unknown principal: " + id);
    }

    public static Result deleteUser(Long id) {
        Principal pal = palFinder.get().byId(id);
        if (pal != null) {
            try {
                pal.delete();
                ObjectMapper mapper = new ObjectMapper();
                return Java8Util.ok(mapper.valueToTree(pal));
            } catch (Exception ex) {
                return badRequest(ex.getMessage());
            }
        }
        return notFound("Unknown principal: " + id);
    }



    public static Result deletePermission(Long id) {
        Acl permission = aclFinder.get().byId(id);
        if (permission != null) {
            try {
                permission.delete();
                ObjectMapper mapper = new ObjectMapper();
                return Java8Util.ok(mapper.valueToTree(permission));
            } catch (Exception ex) {
                return badRequest(ex.getMessage());
            }
        }
        return notFound("Unknown permission: " + id);
    }

    public static Result deleteUserPermission(Long permId, Long userId) {

        Principal user = palFinder.get().byId(userId);
        if (user != null) {
            try {

                Acl acl = aclFinder.get().setId(permId).fetch("principals").findUnique();
                acl.principals.remove(palFinder.get().byId(userId));
                acl.saveManyToManyAssociations("principals");
            } catch (Exception ex) {
                return badRequest(ex.getMessage());
            }
        }
        return notFound("Unknown user: " + userId);
    }

    public static Result deleteUserGroup(Long grpId, Long userId) {

        try {
            Group grp = groupfinder.get().setId(grpId).fetch("members").findUnique();
            grp.members.remove(palFinder.get().byId(userId));
            grp.saveManyToManyAssociations("members");
        }catch(Exception ex){
            return badRequest(ex.getMessage());
        }
        return ok("user: " + userId);
    }

    public static Result getNamespace(Long id) {
        Namespace res = resFinder.get().byId(id);
        if (res != null) {
            ObjectMapper mapper = new ObjectMapper();
            return Java8Util.ok(mapper.valueToTree(res));
        }
        return notFound("Unknown resource: " + id);
    }

    public static Result deleteNamespace(Long id) {
        Namespace res = resFinder.get().byId(id);
        if (res != null) {
            try {
                //res.save("archive");

                res.delete();
                ObjectMapper mapper = new ObjectMapper();
                return Java8Util.ok(mapper.valueToTree(res));
            } catch (Exception ex) {
                Logger.error("Can't delete Resource " + id, ex);
                return badRequest(ex.getMessage());
            }
        }
        return notFound("Unknown resource: " + id);
    }

    static String pkey() {
        return random(16);
    }

    static String random(int length) {
        java.security.SecureRandom rand = new java.security.SecureRandom();
        byte[] buf = new byte[length / 2];
        rand.nextBytes(buf);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < buf.length; ++i) {
            sb.append(String.format("%1$02x", buf[i] & 0xff));
        }
        return sb.toString();
    }
    
    

    public static Result createTest1() {

        Transaction tx = Ebean.beginTransaction();
        Principal user1 = new Principal("jdoe@army.mil");
        user1.username = "Joe Doe";

        Principal user2 = new Principal("jdoe@navy.mil");
        user2.username = "Jane Doe";

        Principal user3 = new Principal("jdoe@marine.mil");
        user3.username = "Jimmy Doe";

        Principal user4 = new Principal(true, "jdean@nsa.mil");
        user4.username = "Jimmy Dean";

        Principal user5 = new Principal();

        Group group1 = new Group("Illuminati");
        group1.members.add(user1);
        group1.members.add(user3);
        

        Group group2 = new Group("FreeMason");
        group2.members.add(user2);
        group2.members.add(user3);
        group2.members.add(user4);

        Group group3 = new Group("FaternalOrder");
        group3.members.add(user1);
        group3.members.add(user2);
        group3.members.add(user3);

      /*  Acl acl1 = Acl.newRead();
        acl1.principals.add(user4);
        acl1.groups.add(group1);

        Acl acl2 = Acl.newWrite();
        acl2.principals.add(user2);

        Acl acl3 = Acl.newAdmin();
        acl3.groups.add(group3);

        Acl acl4 = Acl.newReadWrite();
        acl4.principals.add(user1);
        acl4.principals.add(user3);*/

        Acl acl5 = Acl.newExecute();
        acl5.groups.add(group2);

  /*      Acl acl6 = Acl.newRead();
        acl6.principals.add(user1);
        acl6.principals.add(user2);
        acl6.principals.add(user3);

        Acl acl7 = Acl.newNone();
        acl7.principals.add(user4);

        Acl acl8 = Acl.newNone();
        acl8.principals.add(user1);
        acl8.principals.add(user5);

        Role role1 = Role.newGuest();
        role1.principal = user1;

        Role role2 = Role.newUser();
        role2.principal = user2;

        Role role3 = Role.newOwner();
        role3.principal = user3;

        Role role4 = Role.newAdmin();
        role4.principal = user4;*/

        tx.commit();

        return ok("Test1 data created!");
    }

    public static Result get(String name) {
        Namespace resource = resFinder.get()
                .where().eq("name", name)
                .findUnique();
        if (resource != null) {
            ObjectMapper mapper = new ObjectMapper();
            return Java8Util.ok(mapper.valueToTree(resource));
        }
        return badRequest("Unknown resource: " + name);
    }

    

    public static String generateSalt() {
        return Util.encrypt(TimeUtil.getCurrentDate().toString(), String.valueOf(Math.random()));
    }

    public static boolean validatePassword(UserProfile profile, String password) {
        return profile.acceptPassword(password);
    }
    

    public static List<String> aclNamesByPrincipal(Principal cred) {
        List<Acl> perms = permissionByPrincipal(cred);
        List<String> permList = new ArrayList<String>();
        for(Acl a : perms){
            permList.add(a.getValue());
        }
        return permList;
    }

    public static List<String> groupNamesByPrincipal(Principal cred) {
        List<Group> grps = groupsByPrincipal(cred);
        List<String> grpList = new ArrayList<String>();
        for(Group g : grps){
            grpList.add(g.name);
        }
        return grpList;
    }

    public static List<Group> groupsByPrincipal(Principal cred) {
        Long userId = cred.id;
        List<Group> groupByUser = AdminFactory.groupfinder.get().where().eq("members.id", userId).findList();
        return groupByUser;
    }
    
    public static Group getGroupByName(String name){
    	return AdminFactory.groupfinder.get().where().ieq("name", name).findList()
    			.stream()
    			.findFirst()
    			.orElse(null);
    }


    public static List<Acl> permissionByPrincipal(Principal cred) {
        Long userId = cred.id;
        List<Acl> perByUser = AdminFactory.aclFinder.get().where().eq("principals.id", userId).findList();
        return perByUser;
    }

    public static Result setUserToInactive(Long id) {
        Principal user = palFinder.get().byId(id);
        if (user != null) {
            UserProfile profile = user.getUserProfile();
            profile.active = false;
            profile.save();
            return ok("Inactivated user:" + id);
        }
        return notFound("Unknown user: " + id);
    }

    public static void updateGroups(Long userId, List<Group> groups){

        Principal user = palFinder.get().byId(userId);

        for(Group g : AdminFactory.groupsByPrincipal(user)){
            AdminFactory.deleteUserGroup(g.id, user.id);
        }

        for (Group g : groups) {
            if(g.id != null) {
                g.members.add(user);
                g.saveManyToManyAssociations("members");
            }else {
                g.members.add(user);
                g.save();
            }
        }
    }

    public static void updatePermissions(Long userId, List<Acl> perms){
        Principal user = palFinder.get().byId(userId);
        for(Acl p : AdminFactory.permissionByPrincipal(user)){
            AdminFactory.deleteUserPermission(p.id, user.id);
        }

        for (Acl p : perms) {
            if(p.id != null){
                p.principals.add(user);
                p.saveManyToManyAssociations("principals");
            }else {
                p.principals.add(user);
                p.save();
            }
        }
    }
    
    
    
    
    public static List<Group> allGroups() {
        return groupfinder.get().all();
    }
    
    public static Group getGroupById(Long id) {
    	Group g=groupCache.get().get(id);
    	if(g!=null)return g;
        g=groupfinder.get().byId(id);
        if(g!=null){
        	cacheGroup(g);
        }
        return g;
    }

    private static void cacheGroup(Group g){
    	alreadyRegistered.get().put(g.name, g);
    	groupCache.get().put(g.id, g);
    }

    public static synchronized Group registerGroupIfAbsent(Group org) {
    	Group grp=alreadyRegistered.get().get(org.name);
        if(grp==null){
        	grp = groupfinder.get().where().eq("name", org.name).findUnique();
        }
        if (grp == null) {
            try {
                org.save();
                cacheGroup(org);
                return org;
            } catch (Exception ex) {
                Logger.trace("Can't register Group: " + org.name, ex);
                throw new IllegalArgumentException(ex);
            }
        }
        return grp;
    }

}
