package ix.core.controllers.v1;

import be.objectify.deadbolt.java.actions.Dynamic;
import ix.core.util.GinasPortalGun;
import ix.ncats.controllers.security.IxDynamicResourceHandler;
import play.mvc.Controller;
import play.mvc.Result;

public class LoadController extends Controller {

    @Dynamic(value = IxDynamicResourceHandler.IS_ADMIN, handler = ix.ncats.controllers.security.IxDeadboltHandler.class)
    public static Result monitor(String id){
        return GinasPortalGun.monitorProcessApi(id);
    }

    @Dynamic(value = IxDynamicResourceHandler.IS_ADMIN, handler = ix.ncats.controllers.security.IxDeadboltHandler.class)
    public static Result loadJsonViaAPI(){
        return GinasPortalGun.loadJsonViaAPI();
    }
}
