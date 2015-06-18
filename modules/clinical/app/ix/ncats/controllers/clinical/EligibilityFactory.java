package ix.ncats.controllers.clinical;

import java.util.*;

import play.*;
import play.db.ebean.*;
import play.data.*;
import play.mvc.*;

import ix.core.models.Keyword;
import ix.ncats.models.clinical.Eligibility;
import ix.core.controllers.EntityFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

public class EligibilityFactory extends EntityFactory {
    static final public Model.Finder<Long, Eligibility> finder = 
        new Model.Finder(Long.class, Eligibility.class);

    public static List<Eligibility> all () { return all (finder); }
    public static Eligibility getEmployee (Long id) {
        return getEntity (id, finder);
    }
    public static Result count () {
        return count (finder);
    }
    public static Result page (int top, int skip) {
        return EligibilityFactory.page(top, skip, null);
    }
    public static Result page (int top, int skip, String filter) {
        return page (top, skip, filter, finder);
    }

    public static Result edits (Long id) {
        return edits (id, Eligibility.class);
    }

    public static Result get (Long id, String expand) {
        return get (id, expand, finder);
    }

    public static Result field (Long id, String path) {
        return field (id, path, finder);
    }

    public static Result create () {
        return create (Eligibility.class, finder);
    }

    public static Result update (Long id, String field) {
        return badRequest ("Not supported operation");
    }

    public static Result delete (Long id) {
        return badRequest ("Not supported operation");
    }

    public static Result test1 () {
        Eligibility el = new Eligibility ();
        el.inclusions.add(new Keyword ("1. Patients with 1 prior resection of histologically-diagnosed de novo GBM"));
        el.inclusions.add(new Keyword ("2. Patient must have MRI evidence of disease recurrence"));
        el.inclusions.add(new Keyword ("3. Patients must have Eastern Cooperative Oncology Group (ECOG) performance status <= 2"));
        el.inclusions.add(new Keyword ("4. Patients >= 18 years of age"));

        el.exclusions.add(new Keyword ("1. Less than 18 years of age"));
        el.exclusions.add(new Keyword ("2. Diagnosis of anything other than first-recurrence GBM"));
        el.exclusions.add(new Keyword ("3. GBM tissue from first-resection not available"));
        el.save();

        Logger.debug("Eligibility "+el.id+" created!");
        ObjectMapper mapper = new ObjectMapper ();
        el = finder.byId(el.id);

        return ok (mapper.valueToTree(el));
    }
}
