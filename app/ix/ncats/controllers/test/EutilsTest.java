package ix.ncats.controllers.test;

import java.io.*;
import java.util.*;

import play.*;
import play.db.ebean.*;
import play.data.*;
import play.mvc.*;

import ix.utils.Global;
import ix.utils.Eutils;

import ix.core.models.Publication;
import ix.core.controllers.PublicationFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;

public class EutilsTest extends Controller {
    public static Result index () {
        return ok (ix.ncats.views.html.eutils.render());
    }

    public static Result fetch () {
        DynamicForm requestData = Form.form().bindFromRequest();
        String arg = requestData.get("pmid");
        Logger.debug("## pmid="+arg);

        List<Publication> pubs = new ArrayList<Publication>();
        for (String p : arg.split("[,\\s;]+")) {
            try {
                long pmid = Long.parseLong(p);
                Publication pub = PublicationFactory.byPMID(pmid);
                if (pub == null) {
                    Logger.debug("fetching "+pmid);
                    pub = Eutils.fetchPublication(pmid);
                    if (pub != null) {
                        pubs.add(pub);
                        pub.save();
                        Logger.debug("Publication "+pub.id+"saved!");
                    }
                    else {
                        Logger.debug("Unable to retrieve publication for "+pmid);
                    }
                }
                else {
                    Logger.debug("Already have "+pmid);
                    pubs.add(pub);
                }
            }
            catch (NumberFormatException ex) {
            }
        }
        Logger.debug(pubs.size()+" publication(s) fetched!");

        ObjectMapper mapper = new ObjectMapper ();
        return ok (mapper.valueToTree(pubs));
    }
}
