package ix.ginas.controllers;

import java.io.*;
import java.util.*;

import play.*;
import play.db.ebean.*;
import play.data.*;
import play.mvc.*;

import ix.ginas.models.*;
import ix.ginas.plugins.*;

public class StructureFactory extends Controller {
    static final StructureProcessorPlugin processor =
        Play.application().plugin(StructureProcessorPlugin.class);
    
    public static Result index () {
        return ok (ix.ginas.views.html.struc.render());
    }
    
    public static Result load () {
        String mesg = "processing "+new java.util.Random().nextInt(100);
        Logger.debug("submitting "+mesg);
        
        processor.process(mesg);
        return redirect (ix.ginas.controllers.routes.StructureFactory.index());
    }
}
