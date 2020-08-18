package ix.core.controllers.v1;

import be.objectify.deadbolt.java.actions.Dynamic;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import ix.core.UserFetcher;
import ix.core.controllers.AdminFactory;
import ix.core.controllers.EntityFactory;
import ix.core.controllers.UserProfileFactory;
import ix.core.models.UserProfile;
import ix.core.util.CachedSupplier;
import ix.core.util.GinasPortalGun;
import ix.ncats.controllers.auth.Authentication;
import ix.ncats.controllers.security.IxDynamicResourceHandler;
import ix.utils.Util;
import org.apache.http.entity.ContentType;
import play.data.DynamicForm;
import play.db.ebean.Model;
import play.mvc.BodyParser;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Results;

import java.util.*;

public class UserController extends Controller {

    private static CachedSupplier<Model.Finder<Long, UserProfile>> UserProfileFinder = Util.finderFor(Long.class, UserProfile.class);

    @Dynamic(value = IxDynamicResourceHandler.IS_ADMIN, handler = ix.ncats.controllers.security.IxDeadboltHandler.class)
    public static Result getAllGroupNames(){
        List<String> names = AdminFactory.getGroupNames();
        //TODO sort?
        return Results.ok(EntityFactory.EntityMapper.FULL_ENTITY_MAPPER().toJson(names))
                        .as("application/json");
    }
    @Dynamic(value = IxDynamicResourceHandler.IS_ADMIN, handler = ix.ncats.controllers.security.IxDeadboltHandler.class)
    public static Result getUserByUsername(String username){
        UserProfile up = UserProfileFactory.getUserProfileForUsername(username);
        if(up ==null){
            return GsrsApiUtil.notFound("username not found " + username);
        }
        return Results.ok((JsonNode) new ObjectMapper().valueToTree(up));
    }

    @Dynamic(value = IxDynamicResourceHandler.IS_ADMIN, handler = ix.ncats.controllers.security.IxDeadboltHandler.class)
    public static Result getUserById(long userId){
        UserProfile up = UserProfileFactory.getUserProfileById(userId);
        if(up ==null){
            //check the unlikely event that the username is all digits?
            return getUserByUsername(Long.toString(userId));

        }
        return Results.ok((JsonNode) new ObjectMapper().valueToTree(up));
    }

    @Dynamic(value = IxDynamicResourceHandler.IS_ADMIN, handler = ix.ncats.controllers.security.IxDeadboltHandler.class)
    public static Result deactivateByUsername(String username){
        UserProfile up = UserProfileFactory.getUserProfileForUsername(username);
        if(up ==null){
            return GsrsApiUtil.notFound("username not found : " + username);
        }
        up.active = false;
        up.save();
        return Results.ok((JsonNode) new ObjectMapper().valueToTree(up));
    }
    @Dynamic(value = IxDynamicResourceHandler.IS_ADMIN, handler = ix.ncats.controllers.security.IxDeadboltHandler.class)
    public static Result deactivateById(long userId){
        UserProfile up = UserProfileFactory.getUserProfileById(userId);
        if(up ==null){
            //check the unlikely event that the username is all digits?
            return deactivateByUsername(Long.toString(userId));
        }
        up.active = false;
        up.save();
        return Results.ok((JsonNode) new ObjectMapper().valueToTree(up));
    }
    @Dynamic(value = IxDynamicResourceHandler.IS_ADMIN, handler = ix.ncats.controllers.security.IxDeadboltHandler.class)
    public static Result getUsersSummary(){
        //only need user, email created modified active/inactive
        //this selected/fetched query is to only fetch the columns we need in the admin panel api
        //the Summary nested objects are to preserve the same json structure as if we returned the real
        //objects with empty everything else we don't read.
        List<UserProfile> profiles = UserProfileFinder.get()
                .select("id,active")
                .fetch("user", "username,email,created,modified")
                .findList();
        List<ProfileSummary> summaryList = new ArrayList<>(profiles.size());
        for(UserProfile p: profiles){
            summaryList.add( ProfileSummary.from(p));
        }
        //TODO get request params to slice list
        return Results.ok((JsonNode) new ObjectMapper().valueToTree(summaryList))
                .as(ContentType.APPLICATION_JSON.toString());
    }
    private static class UserSummary{
        public String username, email;
        public Date created, modified;
        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }


        public Date getCreated() {
            return created;
        }

        public void setCreated(Date created) {
            this.created = created;
        }

        public Date getModified() {
            return modified;
        }

        public void setModified(Date modified) {
            this.modified = modified;
        }
    }
    private static class ProfileSummary {
        public UserSummary user;
        public Long id;
        public boolean active;

        public static ProfileSummary from(UserProfile p){
            ProfileSummary summary = new ProfileSummary();
            summary.id=p.id;
            summary.active = p.active;
            UserSummary user = new UserSummary();
            user.created = p.user.created;
            user.modified = p.user.modified;
            user.email = p.user.email;
            user.username = p.user.username;

            summary.setUser(user);
            return summary;
        }

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public UserSummary getUser() {
            return user;
        }

        public void setUser(UserSummary user) {
            this.user = user;
        }

        public boolean isActive() {
            return active;
        }

        public void setActive(boolean active) {
            this.active = active;
        }
    }

    @Dynamic(value = IxDynamicResourceHandler.IS_ADMIN, handler = ix.ncats.controllers.security.IxDeadboltHandler.class)
    @BodyParser.Of(BodyParser.Json.class)
    public static Result changePasswordById(Long id){
        UserProfile up = UserProfileFactory.getUserProfileById(id);
        if(up ==null){
            //check for username of all digits?
            up = UserProfileFactory.getUserProfileForUsername(Long.toString(id));
        }
        JsonNode body = request().body().asJson();
        return _changePassword(up, body, false);
    }

    @Dynamic(value = IxDynamicResourceHandler.IS_ADMIN, handler = ix.ncats.controllers.security.IxDeadboltHandler.class)
    @BodyParser.Of(BodyParser.Json.class)
    public static Result changePasswordByUsername(String username){
        UserProfile up = UserProfileFactory.getUserProfileForUsername(username);
         JsonNode body = request().body().asJson();
        return _changePassword(up, body,false);
    }
    @Dynamic(value = IxDynamicResourceHandler.IS_USER_PRESENT, handler = ix.ncats.controllers.security.IxDeadboltHandler.class)
    @BodyParser.Of(BodyParser.Json.class)
    public static Result changeMyPassword(){
        UserProfile p= UserFetcher.getActingUserProfile(false);
        if(p ==null){
            return GsrsApiUtil.forbidden("not allowed to change password");
        }
        JsonNode body = request().body().asJson();
        return _changePassword(p, body, true);
    }
    private static Result _changePassword(UserProfile up, JsonNode body, boolean checkOldPassword) {
        if(up==null){
            return GsrsApiUtil.notFound("user not found");
        }
        PasswordChangeRequest passwordChangeRequest;
        try {
            passwordChangeRequest = new ObjectMapper().treeToValue(body, PasswordChangeRequest.class);
        } catch (JsonProcessingException e) {
            return GsrsApiUtil.badRequest(e);
        }
        if(passwordChangeRequest.newPassword ==null || passwordChangeRequest.newPassword.trim().isEmpty()){
            return GsrsApiUtil.badRequest("password can not be blank or all whitespace");
        }

        if(checkOldPassword && !up.acceptPassword(passwordChangeRequest.oldPassword)){
            return GsrsApiUtil.unauthorized("incorrect password");
        }
        up.setPassword(passwordChangeRequest.newPassword);

        up.save();
        Authentication.flushSessionByUserProfile(up);
        return GsrsApiUtil.created("password changed");
    }

    public static class PasswordChangeRequest{
        public String oldPassword;
        public String newPassword;


    }
    @Dynamic(value = IxDynamicResourceHandler.IS_ADMIN, handler = ix.ncats.controllers.security.IxDeadboltHandler.class)
    @BodyParser.Of(BodyParser.Json.class)
    public static Result updateUserById(Long id){

        ObjectMapper objectMapper = new ObjectMapper();

        JsonNode body = request().body().asJson();
        NewUserRequest newUserRequest;
        try {
            newUserRequest = objectMapper.treeToValue(body, NewUserRequest.class);
        } catch (JsonProcessingException e) {
            return GsrsApiUtil.badRequest(e);
        }
        //make sure id matches
        UserProfile up = UserProfileFactory.getUserProfileById(id);
        if(up ==null){
            //check for username only digits
            up = UserProfileFactory.getUserProfileForUsername(Long.toString(id));
        }
        if(up ==null){
            return GsrsApiUtil.notFound("user not found");
        }
        if(!newUserRequest.username.equals(up.user.username)){
            return GsrsApiUtil.badRequest("username in JSON doesn't match requested user");
        }
        try {
            GinasPortalGun.updateUser(up, newUserRequest.asDynamicForm());
        }catch(Exception e){
            return GsrsApiUtil.badRequest(e);
        }
        return Results.ok((JsonNode) objectMapper.valueToTree(up))
                .as(ContentType.APPLICATION_JSON.toString());
    }
    @Dynamic(value = IxDynamicResourceHandler.IS_ADMIN, handler = ix.ncats.controllers.security.IxDeadboltHandler.class)
    @BodyParser.Of(BodyParser.Json.class)
    public static Result updateUserByUsername(String username){

        ObjectMapper objectMapper = new ObjectMapper();

        JsonNode body = request().body().asJson();
        NewUserRequest newUserRequest;
        try {
            newUserRequest = objectMapper.treeToValue(body, NewUserRequest.class);
        } catch (JsonProcessingException e) {
            return GsrsApiUtil.badRequest(e);
        }
        //make sure username matches
        if(!newUserRequest.username.trim().equals(username.trim())){
            return GsrsApiUtil.badRequest("username in JSON doesn't match requested user");
        }
        UserProfile up = UserProfileFactory.getUserProfileForUsername(username);
        if(up ==null){
            return GsrsApiUtil.notFound("user not found");
        }
        try {
            GinasPortalGun.updateUser(up, newUserRequest.asDynamicForm());
        }catch(Exception e){
            return GsrsApiUtil.badRequest(e);
        }
        return Results.ok((JsonNode) objectMapper.valueToTree(up))
                .as(ContentType.APPLICATION_JSON.toString());

    }

    @Dynamic(value = IxDynamicResourceHandler.IS_ADMIN, handler = ix.ncats.controllers.security.IxDeadboltHandler.class)
    @BodyParser.Of(BodyParser.Json.class)
    public static Result createNewUser(){

        ObjectMapper objectMapper = new ObjectMapper();

        JsonNode body = request().body().asJson();
        NewUserRequest newUserRequest;
        try {
            newUserRequest = objectMapper.treeToValue(body, NewUserRequest.class);
        } catch (JsonProcessingException e) {
            return GsrsApiUtil.badRequest(e);
        }
        try {
            GinasPortalGun.addUser(newUserRequest.asDynamicForm());
        }catch(Exception e){
            return GsrsApiUtil.badRequest(e);
        }
        UserProfile up = UserProfileFactory.getUserProfileForUsername(newUserRequest.username);
        return Results.created((JsonNode) objectMapper.valueToTree(up))
                            .as(ContentType.APPLICATION_JSON.toString());

    }

    public static class NewUserRequest{
        public String username;
        public String password;
        public String email;
        public boolean isAdmin;
        public boolean isActive;
        public Set<String> groups;
        public Set<String> roles;

        public DynamicForm asDynamicForm(){

            Map<String,String> map = new HashMap<>();

           /*
            String groupName = requestData.get("grpName");

            */

           map.put("admin", Boolean.toString(isAdmin));
           map.put("username", username);

           map.put("password", password);
           map.put("active", Boolean.toString(isActive));
           if(email !=null && !email.trim().isEmpty()){
               map.put("email", email.trim());
           }
           for(String g : groups){
               map.put("g-" + g, g);
           }
           for(String r : roles){
               map.put("r-"+r, r);
           }

            DynamicForm form = new DynamicForm();
            //the code that processes this form gets data multiple ways
            //for some reason putAll vs bind set different things so we gotta do both.
            form =form.bind(map);
            form.data().putAll(map);
           return form;

        }
        //the binding form needs getters and setters
        //doesn't work for some reason if we let ebean create it?

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public boolean isAdmin() {
            return isAdmin;
        }

        public void setAdmin(boolean admin) {
            isAdmin = admin;
        }

        public boolean isActive() {
            return isActive;
        }

        public void setActive(boolean active) {
            isActive = active;
        }

        public Set<String> getGroups() {
            return groups;
        }

        public void setGroups(Set<String> groups) {
            this.groups = groups;
        }

        public Set<String> getRoles() {
            return roles;
        }

        public void setRoles(Set<String> roles) {
            this.roles = roles;
        }
    }
}
