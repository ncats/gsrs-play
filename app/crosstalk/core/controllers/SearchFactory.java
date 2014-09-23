package crosstalk.core.controllers;

import java.io.*;
import java.security.*;
import java.util.*;
import play.*;
import play.db.ebean.*;
import play.data.*;
import play.mvc.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import com.avaje.ebean.*;
import com.avaje.ebean.event.BeanPersistListener;

import crosstalk.core.models.ETag;
import crosstalk.core.models.ETagRef;
import crosstalk.core.models.Edit;
import crosstalk.core.models.Principal;

import crosstalk.utils.Global;

public class SearchFactory extends Controller {
    public static Result search (String q, int top, int skip, String expand) {
        Global g = Global.getInstance();
        try {
            List results = g.getTextIndexer().search(q, top, skip);

            ObjectMapper mapper = new ObjectMapper ();
            ArrayNode nodes = mapper.createArrayNode();
            for (Object obj : results) {
                if (obj != null) {
                    try {
                        ObjectNode node = (ObjectNode)mapper.valueToTree(obj);
                        node.put("_kind", obj.getClass().getName());
                        nodes.add(node);
                    }
                    catch (Exception ex) {
                        Logger.trace("Unable to serialize object to Json", ex);
                    }
                }
            }

            /*
             * TODO: setup etag right here!
             */

            return ok (nodes);
        }
        catch (IOException ex) {
            return badRequest (ex.getMessage());
        }
    }
}
