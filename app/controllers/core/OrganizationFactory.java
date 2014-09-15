package controllers.core;

import java.io.*;
import java.security.*;
import java.util.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import play.*;
import play.db.ebean.*;
import play.data.*;
import play.mvc.*;

import models.core.Organization;
import controllers.core.EntityFactory;

public class OrganizationFactory extends EntityFactory {
    static public final Model.Finder<Long, Organization> finder = 
        new Model.Finder(Long.class, Organization.class);

    public static List<Organization> all () { return all (finder); }
    public static Organization getEntity (Long id) {
        return getEntity (id, finder);
    }
    public static Result count () { return count (finder); }
    public static Result page (int top, int skip, 
                               String select, String filter) {
        return page (top, skip, select, filter, finder);
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
}
