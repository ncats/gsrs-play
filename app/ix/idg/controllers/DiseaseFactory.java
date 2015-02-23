package ix.idg.controllers;

import ix.core.NamedResource;
import ix.idg.models.Disease;
import ix.core.controllers.EntityFactory;

import play.db.ebean.Model;
import play.mvc.Result;

import java.util.List;

@NamedResource(name="diseases",type=Disease.class)
public class DiseaseFactory extends EntityFactory {
    static final public Model.Finder<Long, Disease> finder = 
        new Model.Finder(Long.class, Disease.class);

    public static Disease getDisease (Long id) {
        return getEntity (id, finder);
    }

    public static List<Disease> getDiseases(int top, int skip, String filter) {
        return filter(new FetchOptions(top, skip, filter), finder);
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

    public static Disease registerIfAbsent (String name) {
        List<Disease> diseases = finder.where().eq("name", name).findList();
        if (diseases.isEmpty()) {
            Disease dis = new Disease ();
            dis.name = name;
            dis.save();
            return dis;
        }
        return diseases.iterator().next();
    }
}
