package ix.core.controllers;

import java.io.*;
import java.security.*;
import java.util.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import play.*;
import play.db.ebean.*;
import play.data.*;
import play.mvc.*;
import com.avaje.ebean.Query;
import com.avaje.ebean.Expr;
import com.avaje.ebean.LikeType;

import ix.core.models.Organization;
import ix.core.NamedResource;

@NamedResource(name="organizations", type=Organization.class)
public class OrganizationFactory extends EntityFactory {
    static public final Model.Finder<Long, Organization> finder = 
        new Model.Finder(Long.class, Organization.class);

    public static List<Organization> all () { return all (finder); }
    public static Organization getEntity (Long id) {
        return getEntity (id, finder);
    }
    public static Result count () { return count (finder); }
    public static Result page (int top, int skip, String filter) {
        return page (top, skip, filter, finder);
    }

    public static Result get (Long id, String select) {
        return get (id, select, finder);
    }

    public static Result field (Long id, String path) {
        return field (id, path, finder);
    }

    public static Result create () {
        return create (Organization.class, finder);
    }

    public static Result delete (Long id) {
        return delete (id, finder);
    }

    public static Result update (Long id, String field) {
        return update (id, field, Organization.class, finder);
    }

    public static Organization registerIfAbsent (Organization org) {
        List<Organization> results = finder.query()
            .where().add(Expr.exampleLike(org, true, LikeType.EQUAL_TO))
            .findList();
        if (results.isEmpty()) {
            try {
                org.save();
                return org;
            }
            catch (Exception ex) {
                Logger.trace("Can't register organization: "+org.name, ex);
                throw new IllegalArgumentException (ex);
            }
        }
        return results.iterator().next();
    }
}
