package ix.qhts.controllers;

import java.util.*;

import play.*;
import play.db.ebean.*;
import play.data.*;
import play.mvc.*;
import com.avaje.ebean.Query;
import com.avaje.ebean.Expr;

import ix.qhts.models.*;
import ix.core.controllers.EntityFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

public class QhtsFactory extends Controller {
    public static Result test () {
        Model.Finder<Long, Curve> curveFinder =
            new Model.Finder(Long.class, Curve.class);

        double[] conc = new double[10];
        double[] resp = new double[conc.length];
        
        Random rand = new Random ();
        conc[0] = Double.MAX_VALUE;
        resp[0] = Double.MIN_VALUE;
        conc[1] = 0.;
        resp[1] = 1.0;
        for (int i = 2; i < conc.length; ++i) {
            conc[i] = rand.nextDouble();
            resp[i] = rand.nextDouble();
        }

        Curve crc = null;
        EntityFactory.EntityMapper mapper = new EntityFactory.EntityMapper();
        try {
            crc = new Curve ();
            crc.setData(conc, resp);
            crc.save();
            
            Logger.debug("Curve "+crc.id+" saved!");
            Logger.debug(mapper.toJson(crc, true));
            // now read them back in
            crc = curveFinder.byId(crc.id);
        }
        catch (Exception ex) {
            return internalServerError (ex.getMessage());
        }


        return ok (mapper.valueToTree(crc));
    }
}
