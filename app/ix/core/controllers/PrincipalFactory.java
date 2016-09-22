package ix.core.controllers;

import ix.core.NamedResource;
import ix.core.adapters.InxightTransaction;
import ix.core.models.Principal;
import ix.core.util.EntityUtils.EntityWrapper;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.codec.digest.DigestUtils;
import play.Logger;
import play.db.ebean.Model;
import play.mvc.Result;


import com.fasterxml.jackson.databind.JsonNode;


@NamedResource(name = "principal",
        type = Principal.class,
        description = "Users, groups and organizations")
public class PrincipalFactory extends EntityFactory {
    public static Model.Finder<Long, Principal> finder;

    public static Map<String, Principal> justRegisteredCache;


    static{
        init();
    }

    public static void init(){
        finder =
                new Model.Finder(Long.class, Principal.class);

        justRegisteredCache = new ConcurrentHashMap<String, Principal>();
    }

    public static List<Principal> all() {
        return all(finder);
    }

    public static Result count() {
        return count(finder);
    }

    public static Result page(int top, int skip, String filter) {
        return page(top, skip, filter, finder);
    }

    public static List<Principal> filter(int top, int skip) {
        return filter(top, skip, null);
    }

    public static List<Principal> filter(int top, int skip, String filter) {
        return filter(new FetchOptions(top, skip, filter), finder);
    }

    public static List<Principal> filter(JsonNode json, int top, int skip) {
        return filter(json, top, skip, finder);
    }

    public static Result get(Long id, String select) {
        return get(id, select, finder);
    }

    public static Result field(Long id, String path) {
        return field(id, path, finder);
    }

    public static Result create() {
        return create(Principal.class, finder);
    }

    public static Result delete(Long id) {
        return delete(id, finder);
    }

    public static Result update(Long id, String field) {
        return update(id, field, Principal.class, finder);
    }

    public static Principal byUserName(String uname) {
        //System.out.println("########## "+ uname);
        Principal p = justRegisteredCache.get(uname.toUpperCase());
        if (p != null) return p;
        p =  finder.where().ieq("username", uname).findUnique();
        if(p!=null)justRegisteredCache.put(p.username.toUpperCase(), p);
        return p;
    }

    public static synchronized Principal registerIfAbsent(Principal org) {
        Principal results = byUserName(org.username);
        if (results == null) {
            try {
                org.save();
                return org;
            } catch (Exception ex) {
                Logger.trace("Can't register principal: " + org.username, ex);
                throw new IllegalArgumentException(ex);
            }
        }
        return results;
    }


}
