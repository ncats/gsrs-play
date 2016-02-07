package ix.core.controllers;

import java.io.*;
import java.util.*;

import play.*;
import play.db.ebean.*;
import play.data.*;
import play.mvc.*;

import ix.core.NamedResource;
import ix.core.models.Value;
import ix.core.models.VInt;
import ix.core.models.VStr;
import ix.core.models.VNum;
import ix.core.models.VRange;
import ix.core.models.VIntArray;


@NamedResource(name="values",
               type=Value.class,
               description="Resource for handling Value's")
public class ValueFactory extends EntityFactory {
    public static final Model.Finder<Long, Value> finder = 
        new Model.Finder(Long.class, Value.class);

    public static List<Value> all () { return all (finder); }
    public static Result count () { return count (finder); }
    
    public static Integer getCount () {
        try {
            return getCount (finder);
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }
    
    public static Result page (int top, int skip, String filter) {
        return page (top, skip, filter, finder);
    }

    public static Result get (Long id, String select) {
        return get (id, select, finder);
    }

    public static Result field (Long id, String path) {
        return field (id, path, finder);
    }

    public static Result edits (Long id) {
        return edits (id, Value.class, VInt.class, 
                      VStr.class, VNum.class, VRange.class, VIntArray.class);
    }

    public static Result delete (Long id) {
        return delete (id, finder);
    }

    public static Result update (Long id, String field) {
        return update (id, field, Value.class, finder);
    }
}
