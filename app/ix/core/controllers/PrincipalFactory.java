package ix.core.controllers;

import ix.core.NamedResource;
import ix.core.adapters.InxightTransaction;
import ix.core.models.Principal;
import ix.core.util.CachedSupplier;
import ix.core.util.EntityUtils.EntityWrapper;
import ix.utils.Util;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.codec.digest.DigestUtils;
import play.Logger;
import play.db.ebean.Model;
import play.mvc.Result;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.Transaction;
import com.fasterxml.jackson.databind.JsonNode;


@NamedResource(name = "principal",
        type = Principal.class,
        description = "Users, groups and organizations")
public class PrincipalFactory extends EntityFactory {
	
	
    public static CachedSupplier<Model.Finder<Long, Principal>> finder = 
    		Util.finderFor(Long.class, Principal.class);

    public static CachedSupplier<Map<String, Principal>> justRegisteredCache = CachedSupplier.of(()->{
    	return new ConcurrentHashMap<String, Principal>();
    });


    public static List<Principal> all() {
        return all(finder.get());
    }

    public static Result count() {
        return count(finder.get());
    }

    public static Result page(int top, int skip, String filter) {
        return page(top, skip, filter, finder.get());
    }

    public static List<Principal> filter(int top, int skip) {
        return filter(top, skip, null);
    }

    public static List<Principal> filter(int top, int skip, String filter) {
        return filter(new FetchOptions(top, skip, filter), finder.get());
    }

    public static List<Principal> filter(JsonNode json, int top, int skip) {
        return filter(json, top, skip, finder.get());
    }

    public static Result get(Long id, String select) {
        return get(id, select, finder.get());
    }

    public static Result field(Long id, String path) {
        return field(id, path, finder.get());
    }

    public static Result create() {
        return create(Principal.class, finder.get());
    }

    public static Result delete(Long id) {
        return delete(id, finder.get());
    }

    public static Result update(Long id, String field) {
        return update(id, field, Principal.class, finder.get());
    }

    public static Principal byUserName(String uname) {
        //System.out.println("########## "+ uname);
        Principal p = justRegisteredCache.get().get(uname.toUpperCase());
        if (p != null) return p;
        p =  finder.get().where().ieq("username", uname).findUnique();
        if(p!=null){
        	//if not in an active commit, cache
	        if(Ebean.currentTransaction()==null){ 
	        	justRegisteredCache.get().put(p.username.toUpperCase(), p);
	        }
        }
        return p;
    }

    public static synchronized Principal registerIfAbsent(Principal org) {
        Principal results = byUserName(org.username);
        if (results == null) {
            try {
            	Transaction t=Ebean.currentTransaction();
                org.save();
                if(t!=null){
	                InxightTransaction it=InxightTransaction.getTransaction(t);
	                it.addPostCommitRun(()->
	                	justRegisteredCache.get().put(org.username.toUpperCase(), org)
	                	);
                }else{
	                justRegisteredCache.get().put(org.username.toUpperCase(), org);	
                }
                return org;
            } catch (Exception ex) {
                Logger.trace("Can't register principal: " + org.username, ex);
                throw new IllegalArgumentException(ex);
            }
        }
        return results;
    }


}
