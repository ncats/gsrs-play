package ix.core.controllers.test;

import java.io.*;
import java.util.*;

import play.*;
import play.db.ebean.*;
import play.data.*;
import play.mvc.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import ix.core.models.Payload;
import ix.core.plugins.*;

public class Structures extends Controller {
    static final StructureProcessorPlugin structurePlugin =
        Play.application().plugin(StructureProcessorPlugin.class);
    static final PayloadPlugin payloadPlugin =
        Play.application().plugin(PayloadPlugin.class);
    
    public static Result index () {
        return ok (ix.core.views.html.test.struc.render());
    }
    
    public static Result load () {
        String mesg = "processing "+new java.util.Random().nextInt(100);
        Logger.debug("submitting "+mesg);
        
        DynamicForm requestData = Form.form().bindFromRequest();
        String max = requestData.get("max-strucs");
        try {
            Payload payload = payloadPlugin.parseMultiPart
                ("load-file", request ());
            structurePlugin.submit(payload);
        }
        catch (IOException ex) {
            return internalServerError ("Request is not multi-part encoded!");
        }
        
        return redirect (routes.Structures.index());
    }
}
