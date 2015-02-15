package ix.ginas.controllers;

import java.io.*;
import java.util.*;

import play.*;
import play.db.ebean.*;
import play.data.*;
import play.mvc.*;

import ix.core.models.Payload;
import ix.ginas.models.*;
import ix.core.plugins.*;
import ix.ginas.plugins.*;
import ix.core.NamedResource;
import ix.core.controllers.EntityFactory;

@NamedResource(name="structures",
               type=Structure.class,
               description="Resource for handling chemical structures")
public class StructureFactory extends EntityFactory {
    static final StructureProcessorPlugin structurePlugin =
        Play.application().plugin(StructureProcessorPlugin.class);
    static final PayloadPlugin payloadPlugin =
        Play.application().plugin(PayloadPlugin.class);
    
    public static final Model.Finder<UUID, Structure> finder = 
        new Model.Finder(UUID.class, Structure.class);
    
    public static Result count () { return count (finder); }
    public static Result page (int top, int skip, String filter) {
        return page (top, skip, filter, finder);
    }

    public static Result get (UUID id, String select) {
        return get (id, select, finder);
    }

    public static Result field (UUID id, String path) {
        return field (id, path, finder);
    }

    public static Result create () {
        return create (Structure.class, finder);
    }

    public static Result delete (UUID id) {
        return delete (id, finder);
    }

    /***********************************************************************
     ** Views
     ***********************************************************************/
    public static Result index () {
        return ok (ix.ginas.views.html.struc.render());
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
        
        return redirect (ix.ginas.controllers.routes.StructureFactory.index());
    }
}
