package ix.idg.controllers;

import java.util.*;

import play.*;
import play.db.ebean.*;
import play.data.*;
import play.mvc.*;
import com.avaje.ebean.Expr;

import ix.core.NamedResource;
import ix.idg.models.Disease;
import ix.core.controllers.EntityFactory;

@NamedResource(name="diseases",type=Disease.class)
public class DiseaseFactory extends EntityFactory {
    static final public Model.Finder<Long, Disease> finder = 
        new Model.Finder(Long.class, Disease.class);

    public static Disease getDisease (Long id) {
        return getEntity (id, finder);
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

    public static Result edits (Long id) {
        return edits (id, Disease.class);
    }

    public static Result get (Long id, String expand) {
        return get (id, expand, finder);
    }

    public static Result field (Long id, String path) {
        return field (id, path, finder);
    }

    public static Result create () {
        return create (Disease.class, finder);
    }

    public static Result delete (Long id) {
        return delete (id, finder);
    }

    public static Result update (Long id, String field) {
        return update (id, field, Disease.class, finder);
    }

    public static Disease fetchIfAbsent (Disease.Source source, String id) {
	List<Disease> diseases =
	    finder.where(Expr.and(Expr.eq("synonyms.label", source.toString()),
				  Expr.eq("synonyms.term", id)))
	    .findList();
	if (diseases.isEmpty()) {
	    
	}
	
	return null;
    }
}
