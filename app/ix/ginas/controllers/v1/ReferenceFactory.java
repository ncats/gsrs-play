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
import ix.core.models.*;

public class ReferenceFactory extends EntityFactory {
    static public final Model.Finder<UUID, Reference> finder =
        new Model.Finder(UUID.class, Reference.class);
    
    public static Result page (int top, int skip, String filter) {
        return page (top, skip, filter, finder);
    }
    
    public static Result get (String id, String expand) {
        return get (UUID.fromString(id), expand, finder);
    }
}
