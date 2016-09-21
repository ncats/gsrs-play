package ix.ginas.controllers.v1;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import java.io.*;

import play.libs.Json;
import play.*;
import play.db.ebean.*;
import play.data.*;
import play.mvc.*;

import com.avaje.ebean.*;

import ix.ginas.models.*;
import ix.ginas.models.v1.*;
import ix.utils.Tuple;
import ix.core.NamedResource;
import ix.core.controllers.EntityFactory;

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

    public static Integer getCount () {
        try {
            return getCount (finder);
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
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
    
    public static <T> Stream<T> asStream(Iterator<T> sourceIterator) {
        return asStream(sourceIterator, false);
    }

    public static <T> Stream<T> asStream(Iterator<T> sourceIterator, boolean parallel) {
        Iterable<T> iterable = () -> sourceIterator;
        return StreamSupport.stream(iterable.spliterator(), parallel);
    }
    
    public static Optional<Tuple<Long,Code>> getHighestValueCode(String codeSystem, String suffix){
    	Stream<Code> codes=asStream(
    				finder.where()
    				.and(com.avaje.ebean.Expr.like("code","%" + suffix), com.avaje.ebean.Expr.eq("codeSystem",codeSystem))
    				.findIterate());
    	
    
    	Optional<Tuple<Long,Code>> max=codes
    		.map(cd-> 
    			new Tuple<Long,Code>(
    					new Long(Long.parseLong(cd.code.replace(suffix, "")))
    					,cd)
    			)
    		.max((l1,l2)->(int)(l1.k()-l2.k()));
    	
    	if(max.isPresent()){
    		Logger.info("################# FOUND CODE:" + max.get().v().code);
    	}else{
    		Logger.info("################# NO CODE!");
    	}
    	return max;
    }
}
