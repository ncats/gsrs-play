package ix.ncats.controllers.marvin;

import java.util.concurrent.Callable;
import play.Logger;
import play.mvc.BodyParser;
import play.mvc.Result;
import play.mvc.Controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import chemaxon.util.MolHandler;

import ix.core.chem.StructureProcessor;
import ix.utils.Util;
import ix.ncats.controllers.App;

public class Marvin extends App {
    public static Result sketcher () {
        response().setHeader("X-Frame-Options", "SAMEORIGIN");
        return ok (ix.ncats.views.html.marvin.render());
    }

    @BodyParser.Of(value = BodyParser.Text.class, maxLength = 1024 * 10)
    public static Result smiles () {
        String data = request().body().asText();
        Logger.info(data);
        try {
            //String q = URLEncoder.encode(mol.toFormat("smarts"), "utf8");
            return ok (StructureProcessor.createQuery(data));
        }
        catch (Exception ex) {
            ex.printStackTrace();
            Logger.debug("** Unable to convert structure\n"+data);
            return badRequest (data);
        }
    }

    @BodyParser.Of(value = BodyParser.Json.class, maxLength = 1024*10)
    public static Result molconvert () {
        JsonNode json = request().body().asJson();        
        try {
            final String format = json.get("parameters").asText();
            final String mol = json.get("structure").asText();

            String sha1 = Util.sha1(mol);
            Logger.debug("MOLCONVERT: format="+format+" mol="
                         +mol+" sha1="+sha1);
            
            response().setContentType("application/json");
            return getOrElse (0l, sha1, new Callable<Result>() {
                    public Result call () {
                        try {
                            MolHandler mh = new MolHandler (mol);
                            if (mh.getMolecule().getDim() < 2) {
                                mh.getMolecule().clean(2, null);
                            }
                            String out = mh.getMolecule().toFormat(format);
                            //Logger.debug("MOLCONVERT: output="+out);
                            ObjectMapper mapper = new ObjectMapper ();
                            ObjectNode node = mapper.createObjectNode();
                            node.put("structure", out);
                            node.put("format", format);
                            node.put("contentUrl", "");
                           
                            return ok (node);
                        }
                        catch (Exception ex) {
                            return badRequest ("Invalid molecule: "+mol);
                        }
                    }
                });
        }
        catch (Exception ex) {
            Logger.error("Can't parse request", ex);
            ex.printStackTrace();
            
            return internalServerError ("Unable to convert input molecule");
        }
    }
}
