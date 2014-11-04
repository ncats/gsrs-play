package ix.core.controllers;

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

import ix.core.models.ETag;
import ix.core.models.ETagRef;
import ix.core.models.Edit;
import ix.core.models.Principal;

import ix.utils.Global;
import ix.utils.Util;
import ix.core.search.TextIndexer;

public class SearchFactory extends EntityFactory {
    static TextIndexer getIndexer () {
        return Global.getInstance().getTextIndexer();
    }

    public static Result search (String q, int top, int skip, int fdim) {
        try {
            Map<String, String[]> query = request().queryString();

            List<String> drilldown = new ArrayList<String>();
            for (Map.Entry<String, String[]> me : query.entrySet()) {
                if ("facet".equalsIgnoreCase(me.getKey())) {
                    for (String s : me.getValue())
                        drilldown.add(s);
                }

                if (Global.DEBUG(1)) {
                    StringBuilder sb = new StringBuilder ();
                    for (String s : me.getValue())
                        sb.append("\n"+s);
                    Logger.debug(me.getKey()+sb);
                }
            }

            TextIndexer.SearchResult result = 
                getIndexer().search(q, top, skip, fdim, drilldown);

            ObjectMapper mapper = getEntityMapper ();
            ArrayNode nodes = mapper.createArrayNode();
            for (Object obj : result.getMatches()) {
                if (obj != null) {
                    try {
                        ObjectNode node = (ObjectNode)mapper.valueToTree(obj);
                        node.put("kind", obj.getClass().getName());
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
            ETag etag = new ETag ();
            etag.top = top;
            etag.skip = skip;
            etag.count = nodes.size();
            etag.total = result.count();
            etag.uri = request().uri();
            etag.path = request().path();
            etag.sha1 = Util.sha1Request(request(), "q", "facet");
            etag.query = q;
            etag.method = request().method();
            etag.save();

            ObjectNode obj = (ObjectNode)mapper.valueToTree(etag);
            obj.put("drilldown", mapper.valueToTree(result.getDrilldown()));
            obj.put("facets", mapper.valueToTree(result.getFacets()));
            obj.put("content", nodes);

            return ok (obj);
        }
        catch (IOException ex) {
            return badRequest (ex.getMessage());
        }
    }

    public static Result suggest (String q, int max) {
        return suggestField (null, q, max);
    }

    public static Result suggestField (String field, String q, int max) {
        try {
            ObjectMapper mapper = new ObjectMapper ();
            if (field != null) {
                List<TextIndexer.SuggestResult> results = 
                    getIndexer().suggest(field, q, max);
                return ok (mapper.valueToTree(results));
            }

            ObjectNode node = mapper.createObjectNode();
            for (String f : getIndexer().getSuggestFields()) {
                List<TextIndexer.SuggestResult> results = 
                    getIndexer().suggest(f, q, max);
                if (!results.isEmpty())
                    node.put(f, mapper.valueToTree(results));
            }

            return ok (node);
        }
        catch (Exception ex) {
            return internalServerError (ex.getMessage());
        }
    }

    public static Result suggestFields () {
        ObjectMapper mapper = new ObjectMapper ();
        return ok (mapper.valueToTree(getIndexer().getSuggestFields()));
    }
}
