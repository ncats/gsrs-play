package ix.core.controllers;

import java.util.List;

import play.*;
import play.db.ebean.*;
import play.data.*;
import play.mvc.*;

import ix.core.NamedResource;
import ix.core.models.Timeline;

@NamedResource(name="timeline",
               type=Timeline.class,
               description="Resource describing event timeline")
public class TimelineFactory extends EntityFactory {
    public static final Model.Finder<Long, Timeline> finder = 
        new Model.Finder(Long.class, Timeline.class);

    public static Result count () { return count (finder); }
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
        return edits (id, Timeline.class);
    }

    public static Result create () {
        return create (Timeline.class, finder);
    }

    public static Result delete (Long id) {
        return delete (id, finder);
    }

    public static Result update (Long id, String field) {
        return update (id, field, Timeline.class, finder);
    }
}
