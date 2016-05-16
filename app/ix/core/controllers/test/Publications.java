package ix.core.controllers.test;

import java.io.*;
import java.util.*;

import ix.core.util.Java8Util;
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

    static int process (List<Publication> pubs, String text) throws Exception {
        int count = 0;
        for (String p : text.split("[,\\s;]+")) {
            try {
                long pmid = Long.parseLong(p);
                Publication pub =
                    PublicationFactory.registerIfAbsent(pmid);
                if (pub != null) {
                    Logger.debug("Publication "+pub.id
                                 +"/"+pmid+" retrieved!");
                    if (pubs != null)
                        pubs.add(pub);
                    ++count;
                }
                else {
                    Logger.debug
                        ("Unable to retrieve publication for "+pmid);
                }
            }
            catch (Exception ex) {
                Logger.trace("Can't process pmid "+p, ex);
            }
        }
        return count;
    }
    
    public static Result fetch () {
        DynamicForm requestData = Form.form().bindFromRequest();
        
        Http.MultipartFormData body = request().body().asMultipartFormData();
        Http.MultipartFormData.FilePart part = body.getFile("pmid-file");
        try {
            if (part != null) {
                String name = part.getFilename();
                String content = part.getContentType();
                Logger.debug("file="+name+" content="+content);
                File file = part.getFile();
                BufferedReader br = new BufferedReader (new FileReader (file));
                int total = 0;
                for (String line; (line = br.readLine()) != null; ) {
                    total += process (null, line);
                }
                br.close();
                
                return ok (total+" publications fetched from "
                           +part.getFilename());
            }
            else {
                String arg = requestData.get("pmid");
                Logger.debug("## pmid="+arg);
                ObjectMapper mapper = new ObjectMapper ();
                List<Publication> pubs = new ArrayList<Publication>();          
                process (pubs, arg);
                return Java8Util.ok (mapper.valueToTree(pubs));
            }
        }
        catch (Exception ex) {
            ex.printStackTrace();
            return internalServerError (ex.getMessage());
        }
    }
}
