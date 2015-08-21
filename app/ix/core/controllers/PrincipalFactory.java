package ix.core.controllers;

import ix.core.NamedResource;
import ix.core.models.Principal;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import play.Logger;
import play.db.ebean.Model;
import play.mvc.Result;

import com.fasterxml.jackson.databind.JsonNode;


@NamedResource(name="principal",
               type=Principal.class,
               description="Users, groups and organizations")
public class PrincipalFactory extends EntityFactory {
    public static final Model.Finder<Long, Principal> finder = 
        new Model.Finder(Long.class, Principal.class);

    public static Map<String,Principal> justRegisteredCache = new ConcurrentHashMap<String,Principal>();
    
    public static List<Principal> all () { return all (finder); }
    public static Result count () { return count (finder); }
    public static Result page (int top, int skip, String filter) {
        return page (top, skip, filter, finder);
    }

    public static List<Principal> filter (int top, int skip) {
        return filter (top, skip, null);
    }

    public static List<Principal> filter (int top, int skip, String filter) {
        return filter (new FetchOptions (top, skip, filter), finder);
    }

    public static List<Principal> filter (JsonNode json, int top, int skip) {
        return filter (json, top, skip, finder);
    }

    public static Result get (Long id, String select) {
        return get (id, select, finder);
    }

    public static Result field (Long id, String path) {
        return field (id, path, finder);
    }

    public static Result create () {
        return create (Principal.class, finder);
    }

    public static Result delete (Long id) {
        return delete (id, finder);
    }

    public static Result update (Long id, String field) {
        return update (id, field, Principal.class, finder);
    }
    
    public static Principal byUserName (String uname) {
    	Principal p = justRegisteredCache.get(uname);
    	if(p!=null)return p;
        return finder.where().eq("username", uname).findUnique();
    }
    
    public static synchronized Principal registerIfAbsent (Principal org) {
        Principal results = byUserName(org.username);
        if (results == null) {
            try {
                org.save();
                // For some reason, there is a race condition
                // that seems to happen only with oracle,
                // where the result can be null, and there's still enough
                // time between registration and being query-able
                // The hashmap is a temporary measure to fix this.
                justRegisteredCache.put(org.username, org);
                return org;
            }
            catch (Exception ex) {
                Logger.trace("Can't register principal: "+org.username, ex);
                throw new IllegalArgumentException (ex);
            }
        }
        return results;
    }
}
