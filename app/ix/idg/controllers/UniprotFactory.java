package ix.idg.controllers;

import java.io.*;
import java.util.*;

import play.*;
import play.db.ebean.*;
import play.data.*;
import play.mvc.*;
import com.avaje.ebean.Query;
import com.avaje.ebean.Expr;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import ix.core.controllers.EntityFactory;
import ix.core.models.Predicate;
import ix.core.models.XRef;
import ix.idg.models.Target;


public class UniprotFactory extends Controller {
    static final Model.Finder<Long, Target> targetDb = 
        new Model.Finder(Long.class, Target.class);
    
    public static Result index () {
        return ok (ix.idg.views.html.uniprot.render());
    }
    
    public static Result fetch () {
        DynamicForm requestData = Form.form().bindFromRequest();
        String arg = requestData.get("accession");
        Logger.debug("## accession="+arg);
        ObjectMapper mapper = EntityFactory.getEntityMapper();
        UniprotRegistry uni = new UniprotRegistry ();

        List<Target> targets = new ArrayList<Target>();
        for (String acc : arg.split("[,\\s;]+")) {
            try {
                List<Target> list = targetDb
                    .where().eq("synonyms.term", acc).findList();
                if (list.isEmpty()) {
                    try {
                        uni.register(acc);
                        Target target = uni.getTarget();
                        targets.add(target);
                    }
                    catch (Throwable t) {
                        Logger.trace("Can't parse "+acc, t);
                    }
                }                   
                else {
                    Logger.debug("Already have "+acc);
                    targets.addAll(list);
                }
            }
            catch (Exception ex) {
                Logger.trace("Can't process accession "+acc, ex);
            }
        }
        Logger.debug(targets.size()+" targets(s) fetched!");

        return ok (mapper.valueToTree(targets));
    }
}
