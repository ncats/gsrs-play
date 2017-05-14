package ix.ginas.controllers;

import ix.core.models.Group;
import ix.core.models.UserProfile;
import ix.ncats.controllers.App;
import play.mvc.Http.Context;
import play.mvc.Http.Request;
import play.mvc.Result;

/**
 * This is for operations relevant to {@link GinasApp} which
 * are of abstract administrative utility, such as administrative
 * locking, delegating, etc. This class only exists to avoid
 * polluting {@link GinasApp} with general tasks. This may
 * be refactored, eventually, to be a superclass of {@link GinasApp}.
 * @author peryeata
 *
 */
public class GinasAppAdmin extends App {

    private static final String SYSADMIN_GROUP = "sysadmin";


    public static Result message(Context ctx) {
        return GinasApp.warn(503, "Server is undergoing maintenance. Please try again later.");
    }
    
    
    /**
     * Checks if this should be allowed as part of admin
     * maintenance. 
     * @param r
     * @return
     */
    public static boolean isAdminRequest(Request r, UserProfile user){
        if(user==null){
            if(r.path().endsWith("login")){
                return true;
            }
            return false;
        }
        
        if(r.path().endsWith("logout")){
            return true;
        }
        
        for(Group g: user.getGroups()){
            if(g.name.equals(SYSADMIN_GROUP)){
                return true;
            }
        }
        
        
        return false;
    }
    
}
