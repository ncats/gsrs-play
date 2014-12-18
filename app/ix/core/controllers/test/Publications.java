package ix.core.controllers.test;

import java.io.*;
import java.util.*;

import play.*;
import play.db.ebean.*;
import play.data.*;
import play.mvc.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;

import ix.utils.Eutils;
import ix.core.models.*;
import ix.core.controllers.PublicationFactory;

public class Publications extends Controller {
    public static Result relatedByPMID (Long pmid) {
	return PublicationFactory.relatedByPMID(pmid);
    }

    public static Result index () {
        return ok (ix.core.views.html.test.publications.render());
    }

    public static Result fetch () {
        DynamicForm requestData = Form.form().bindFromRequest();
        String arg = requestData.get("pmid");
        Logger.debug("## pmid="+arg);
        ObjectMapper mapper = new ObjectMapper ();

        List<Publication> pubs = new ArrayList<Publication>();
        for (String p : arg.split("[,\\s;]+")) {
            try {
                long pmid = Long.parseLong(p);
                Publication pub = PublicationFactory.byPMID(pmid);
                if (pub == null) {
                    Logger.debug("fetching "+pmid);
                    pub = Eutils.fetchPublication(pmid);
                    if (pub != null) {
                        Logger.debug(mapper.writeValueAsString(pub));
                        pubs.add(pub);
                        pub.save();
                        Logger.debug("Publication "+pub.id+"saved!");
                    }
                    else {
                        Logger.debug
			    ("Unable to retrieve publication for "+pmid);
                    }
                }
                else {
                    Logger.debug("Already have "+pmid);
                    pubs.add(pub);
                }
            }
            catch (Exception ex) {
                Logger.trace("Can't process pmid "+p, ex);
            }
        }
        Logger.debug(pubs.size()+" publication(s) fetched!");

        return ok (mapper.valueToTree(pubs));
    }
}
