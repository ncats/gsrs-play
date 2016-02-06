package ix.ginas.controllers.v1;

import java.util.*;
import java.io.*;

import play.libs.Json;
import play.*;
import play.db.ebean.*;
import play.data.*;
import play.mvc.*;

import com.avaje.ebean.*;

import ix.core.controllers.EntityFactory;
import ix.ginas.models.*;
import ix.ginas.models.v1.*;
import ix.core.NamedResource;

@NamedResource(name="codes",
               type=Code.class,
               description="Resource for handling of GInAS codes")
public class CodeFactory extends EntityFactory {
    static public final Model.Finder<UUID, Code> finder =
        new Model.Finder(UUID.class, Code.class);

    public static Code getCode (UUID uuid) {
        return getEntity (uuid, finder);
    }

    public static List<Code> getCodes
        (int top, int skip, String filter) {
        return filter (new FetchOptions (top, skip, filter), finder);
    }

    public static Result count () {
        return count (finder);
    }

    public static Result page (int top, int skip) {
        return page (top, skip, null);
    }

    public static Result page (int top, int skip, String filter) {
        return page (top, skip, filter, finder);
    }

    public static Result edits (UUID uuid) {
        return edits (uuid, Code.class);
    }

    public static Result get (UUID uuid, String expand) {
        return get (uuid, expand, finder);
    }

    public static Result field (UUID uuid, String path) {
        return field (uuid, path, finder);
    }

    public static Result create () {
        return create (Code.class, finder);
    }

    public static Result delete (UUID uuid) {
        return delete (uuid, finder);
    }

    public static Result update (UUID uuid, String field) {
        return update (uuid, field, Code.class, finder);
    }
    public static String getMostRecentCode(String codeSystem, String like){
    	List<Code> subs=finder.where().and(com.avaje.ebean.Expr.like("code",like), com.avaje.ebean.Expr.eq("codeSystem",codeSystem)).order().desc("code").setMaxRows(1).findList();
    	if(subs!=null && !subs.isEmpty()){
    		Logger.info("#####################FOUND CODE:" + subs.get(0).code);
    		return subs.get(0).code;
    	}
    	Logger.info("################# NO CODE!");
    	return null;
    }
}
