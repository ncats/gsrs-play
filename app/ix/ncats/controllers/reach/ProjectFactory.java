package ix.ncats.controllers.reach;

import java.io.*;
import java.security.*;
import java.util.*;

import play.*;
import play.db.ebean.*;
import play.data.*;
import play.mvc.*;
import ix.core.NamedResource;
import ix.ncats.models.Project;
import ix.core.controllers.EntityFactory;
import ix.core.controllers.EntityFactory.FetchOptions;
import ix.core.models.Publication;

@NamedResource(name="projects",type=Project.class)
public class ProjectFactory extends EntityFactory {
    static final public Model.Finder<Long, Project> finder = 
        new Model.Finder(Long.class, Project.class);

    public static List<Project> all () { return all (finder); }
    public static Project getProject (Long id) {
        return getEntity (id, finder);
    }

    public static List<Project> filter (int top, int skip) {
        return filter (top, skip, null);
    }

    public static List<Project> filter (int top, int skip, String filter) {
        return filter (new FetchOptions (top, skip, filter), finder);
    }
    
    public static Result count () {
        return count (finder);
    }
    public static Result page (int top, int skip) {
        return ProjectFactory.page (top, skip, null, null);
    }
    public static Result page (int top, int skip, String filter) {
        return page (top, skip, filter, finder);
    }

    public static Result edits (Long id) {
        return edits (id, Project.class);
    }

    public static Result get (Long id, String expand) {
        return get (id, expand, finder);
    }

    public static Result field (Long id, String path) {
        return field (id, path, finder);
    }

    public static Result create () {
        return create (Project.class, finder);
    }

    public static Result delete (Long id) {
        return delete (id, finder);
    }

    public static Result update (Long id, String field) {
        return update (id, field, Project.class, finder);
    }
}
