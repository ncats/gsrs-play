package ix.ginas.controllers.v1;

import java.util.*;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import java.io.*;
import java.util.function.Function;

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
import ix.core.util.StreamUtil;

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
    
    public static Optional<Tuple<Long,Code>> getHighestValueCode(String codeSystem, String suffix, Long last){
    	try(Stream<Code> codes=StreamUtil.forIterator(
    				finder.where()
    				.and(com.avaje.ebean.Expr.like("code","%" + suffix), com.avaje.ebean.Expr.eq("codeSystem",codeSystem))
    				.findIterate())){

	    	Optional<Tuple<Long,Code>> max=codes
	    		.map(new Function<Code,Tuple<Long,Code>>(){
                    public Tuple<Long,Code> apply(Code cd){
                        return Tuple.of(new Long(Long.parseLong(cd.code.replace(suffix, ""))),cd);
                    }
                })
                .filter(new Predicate<Tuple<Long, Code>>() {
                    @Override
                    public boolean test(Tuple<Long, Code> t) {
                        return last == null || t.k() <= last;
                    }
                })
	    		.max(new Comparator<Tuple<Long,Code>>(){

                    public int compare(Tuple<Long,Code> l1, Tuple<Long,Code> l2){
                        return (int)(l1.k()-l2.k());
                    }

                });
	    	
	    	if(max.isPresent()){
	    		Logger.info("################# FOUND CODE:" + max.get().v().code);
	    	}else{
	    		Logger.info("################# NO CODE!");
	    	}
	    	return max;
    	}
    }
}
