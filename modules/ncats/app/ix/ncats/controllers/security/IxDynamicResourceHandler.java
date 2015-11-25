package ix.ncats.controllers.security;

import be.objectify.deadbolt.core.DeadboltAnalyzer;
import be.objectify.deadbolt.core.models.Permission;
import be.objectify.deadbolt.core.models.Subject;
import be.objectify.deadbolt.java.AbstractDynamicResourceHandler;
import be.objectify.deadbolt.java.DeadboltHandler;
import be.objectify.deadbolt.java.DynamicResourceHandler;
import play.Logger;
import play.mvc.Http;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


public class IxDynamicResourceHandler implements DynamicResourceHandler {
    private static final Map<String, DynamicResourceHandler> HANDLERS = new HashMap<String, DynamicResourceHandler>();

    static {
        HANDLERS.put("isAdmin",
                new AbstractDynamicResourceHandler() {
                    public boolean isAllowed(final String name,
                                             final String meta,
                                             final DeadboltHandler deadboltHandler,
                                             final Http.Context context) {
                        Subject subject = deadboltHandler.getSubject(context);
                        boolean allowed;

                        DeadboltAnalyzer analyzer = new DeadboltAnalyzer();

                        if (analyzer.hasRole(subject, "Admin")) {
                            allowed = true;
                        } else {
                            // a call to view profile is probably a get request, so
                            // the query string is used to provide info
                            Map<String, String[]> queryStrings = context.request().queryString();
                            String[] requestedNames = queryStrings.get("userName");
                            allowed = requestedNames != null
                                    && requestedNames.length == 1
                                    && requestedNames[0].equals(subject.getIdentifier());
                        }

                        return allowed;
                    }
                });
    }

    //this will be invoked for Dynamic
    public boolean isAllowed(String name,
                             String meta,
                             DeadboltHandler deadboltHandler,
                             Http.Context context) {
        DynamicResourceHandler handler = HANDLERS.get(name);
        boolean result = false;
        if (handler == null) {
            Logger.error("No handler available for " + name);
        } else {
            result = handler.isAllowed(name,
                    meta,
                    deadboltHandler,
                    context);
        }
        return result;
    }

    //this will be invoked for custom Pattern checking
    public boolean checkPermission(final String permissionValue,
                                   final DeadboltHandler deadboltHandler,
                                   final Http.Context ctx) {
        Subject subject = deadboltHandler.getSubject(ctx);
        boolean permissionOk = false;

        if (subject != null) {
            List<? extends Permission> permissions = subject.getPermissions();
            for (Iterator<? extends Permission> iterator = permissions.iterator(); !permissionOk && iterator.hasNext(); ) {
                Permission permission = iterator.next();
                permissionOk = permission.getValue().contains(permissionValue);
            }
        }

        return permissionOk;
    }
}
