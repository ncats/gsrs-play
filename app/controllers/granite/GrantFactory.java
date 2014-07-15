package controllers.granite;

import java.io.*;
import java.security.*;
import java.util.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import play.*;
import play.db.ebean.*;
import play.data.*;
import play.mvc.*;

import models.granite.Grant;
import controllers.core.EntityFactory;

public class GrantFactory extends EntityFactory {
    static final Model.Finder<Long, Grant> finder = 
        new Model.Finder(Long.class, Grant.class);

    public static List<Grant> all () { return all (finder); }
    public static Result count () {
        return count (finder);
    }
    public static Result page (int top, int skip) {
        return page (top, skip, null, null);
    }
    public static Result page (int top, int skip, 
                               String select, String filter) {
        return page (top, skip, select, filter, finder);
    }

    public static Result edits (Long id) {
        return edits (id, Grant.class);
    }

    public static Result get (Long id, String select) {
        return get (id, select, finder);
    }

    public static Result field (Long id, String path) {
        return field (id, path, finder);
    }

    public static Result create () {
        return create (Grant.class, finder);
    }

    public static Result delete (Long id) {
        return delete (id, finder);
    }

    public static Result update (Long id, String field) {
        return update (id, field, Grant.class, finder);
    }
}
