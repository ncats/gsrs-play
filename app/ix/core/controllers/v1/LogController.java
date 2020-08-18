package ix.core.controllers.v1;

import be.objectify.deadbolt.java.actions.Dynamic;
import com.fasterxml.jackson.databind.JsonNode;
import ix.core.util.GinasPortalGun;
import ix.ncats.controllers.security.IxDynamicResourceHandler;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Results;

import java.io.IOException;

public class LogController extends Controller {

    @Dynamic(value = IxDynamicResourceHandler.IS_ADMIN, handler = ix.ncats.controllers.security.IxDeadboltHandler.class)
    public static Result getLogFileListAsJsonNode(){
        JsonNode json = GinasPortalGun.getLogListAsJsonNode();
        return Results.ok(json)
                        .as("application/json");
    }
    @Dynamic(value = IxDynamicResourceHandler.IS_ADMIN, handler = ix.ncats.controllers.security.IxDeadboltHandler.class)
    public static Result getAllFilesListAsJsonNode(){
        JsonNode json = GinasPortalGun.getDirListAsJsonNode();
        return Results.ok(json)
                .as("application/json");
    }
    @Dynamic(value = IxDynamicResourceHandler.IS_ADMIN, handler = ix.ncats.controllers.security.IxDeadboltHandler.class)
    public static Result downloadFile(String path){
        return GinasPortalGun.downloadFile(path);
    }

    @Dynamic(value = IxDynamicResourceHandler.IS_ADMIN, handler = ix.ncats.controllers.security.IxDeadboltHandler.class)
    public static Result downloadLogFile(String path){
        return GinasPortalGun.downloadFile("logs/"+path);
    }
}
