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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.avaje.ebean.*;

import models.granite.Investigator;
import controllers.core.EntityFactory;

public class InvestigatorFactory extends EntityFactory {
    private static Model.Finder<Long, Investigator> finder = 
        new Model.Finder (Long.class, Investigator.class);

    public static List<Investigator> all () { return all (finder); }
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
        return create (Investigator.class, finder);
    }

    public static Result delete (Long id) {
        return delete (id, finder);
    }

    public static Result update (Long id, String field) {
        return update (id, field, Investigator.class, finder);
    }
}
