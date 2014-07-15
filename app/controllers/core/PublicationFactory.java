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

import models.core.Publication;

public class PublicationFactory extends EntityFactory {
    static final Model.Finder<Long, Publication> finder = 
        new Model.Finder(Long.class, Publication.class);

    public static List<Publication> all () { return all (finder); }
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
        return create (Publication.class, finder);
    }

    public static Result delete (Long id) {
        return delete (id, finder);
    }

    public static Result update (Long id, String field) {
        return update (id, field, Publication.class, finder);
    }
}
