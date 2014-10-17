package ix.ncats.controllers.clinical;

import java.util.*;

import play.*;
import play.db.ebean.*;
import play.data.*;
import play.mvc.*;

import ix.core.models.Keyword;
import ix.ncats.models.clinical.*;
import ix.core.controllers.EntityFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

public class ClinicalTrialFactory extends EntityFactory {
    static final public Model.Finder<Long, ClinicalTrial> finder = 
        new Model.Finder(Long.class, ClinicalTrial.class);

    public static List<ClinicalTrial> all () { return all (finder); }
    public static ClinicalTrial getEmployee (Long id) {
        return getEntity (id, finder);
    }
    public static Result count () {
        return count (finder);
    }
    public static Result page (int top, int skip) {
        return ClinicalTrialFactory.page(top, skip, null, null);
    }
    public static Result page (int top, int skip, 
                               String expand, String filter) {
        return page (top, skip, expand, filter, finder);
    }

    public static Result edits (Long id) {
        return edits (id, ClinicalTrial.class);
    }

    public static Result get (Long id, String expand) {
        return get (id, expand, finder);
    }

    public static Result field (Long id, String path) {
        return field (id, path, finder);
    }

    public static Result create () {
        return create (ClinicalTrial.class, finder);
    }

    public static Result update (Long id, String field) {
        return badRequest ("Not supported operation");
    }

    public static Result delete (Long id) {
        return badRequest ("Not supported operation");
    }

    /**
     * For a given NCT id if it doesn't exist, then fetch it directly
     * from clinicaltrials.gov and save it locally.
     */
    public static Result createIfAbsent (String nctId) {
        return ok ("...");
    }
}
