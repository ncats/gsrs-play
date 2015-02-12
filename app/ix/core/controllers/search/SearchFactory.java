package ix.core.controllers.search;

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
import ix.core.plugins.*;
import ix.core.search.TextIndexer;
import ix.core.search.SearchOptions;
import ix.core.controllers.EntityFactory;

public class SearchFactory extends EntityFactory {
    static final Model.Finder<Long, ETag> etagDb = 
        new Model.Finder(Long.class, ETag.class);
    static TextIndexerPlugin plugin;

    static synchronized TextIndexer getIndexer () {
        if (plugin == null) {
            plugin = Play.application().plugin(TextIndexerPlugin.class);
        }
        return plugin != null ? plugin.getIndexer() : null;
    }

    public static SearchOptions parseSearchOptions
        (SearchOptions options, Map<String, String[]> queryParams) {
        if (options == null) {
            options = new SearchOptions ();
        }
        for (Map.Entry<String, String[]> me : queryParams.entrySet()) {
            if ("facet".equalsIgnoreCase(me.getKey())) {
                for (String s : me.getValue()){
                    options.facets.add(s);
                }
            }
            else if ("order".equalsIgnoreCase(me.getKey())) {
                for (String s : me.getValue())
                    options.order.add(s);
            }
            else if ("expand".equalsIgnoreCase(me.getKey())) {
                for (String s : me.getValue())
                    options.expand.add(s);
            }
            else if ("drill".equalsIgnoreCase(me.getKey())) {
                for (String s : me.getValue())
                    options.sideway = "sideway".equalsIgnoreCase(s);
            }
            else if ("kind".equalsIgnoreCase(me.getKey())) {
                if (options.kind == null) {
                    for (String kind: me.getValue()) {
                        if (kind.length() > 0) {
                            try {
                                options.kind = Class.forName(kind);
                                break; // there should only be one!
                            }
                            catch (Exception ex) {
                                Logger.error("Unable to load class: "+kind, ex);
                            }
                        }
                    }
                }
            }
            
            if (Global.DEBUG(1)) {
                StringBuilder sb = new StringBuilder ();
                for (String s : me.getValue())
                    sb.append("\n"+s);
                Logger.debug(me.getKey()+sb);
            }
        }
        return options;
    }
    
    public static TextIndexer.SearchResult
        search (Class kind, String q, int top, int skip, int fdim,
                Map<String, String[]> queryParams) throws IOException {
        SearchOptions options = new SearchOptions (kind, top, skip, fdim);

        StringBuilder filter = new StringBuilder ();
        if (queryParams != null) {
            parseSearchOptions (options, queryParams);
            for (String f : options.facets) {
                if (filter.length() > 0)
                    filter.append("&");
                filter.append("facet="+f);
            }
        }
        
        if (q == null) {
        }
        else if (q.startsWith("etag:") || q.startsWith("ETag:")) {
            String id = q.substring(5, 21);
            try {
                ETag etag = etagDb.where().eq("etag", id).findUnique();
                if (etag.query != null) {
                    if (etag.filter != null) {
                        String[] facets = etag.filter.split("&");
                        for (int i = facets.length; --i >= 0; ) {
                            if (facets[i].length() > 0) {
                                filter.insert(0, facets[i]+"&");
                                options.facets.add
                                    (0, facets[i].replaceAll("facet=", ""));
                            }
                        }
                    }
                    q = etag.query; // rewrite the query
                }
                else {
                    Logger.warn("ETag "+id+" is not a search!");
                }
            }
            catch (Exception ex) {
                Logger.trace("Can't find ETag "+id, ex);
            }
        }
        
        return getIndexer().search(options, q);
    }
        
    public static Result search (String q, int top, int skip, int fdim) {
        return search (null, q, top, skip, fdim);
    }
        
    public static Result search (Class kind, String q, 
                                 int top, int skip, int fdim) {
        if (Global.DEBUG(1)) {
            Logger.debug("SearchFactory.search: kind="
                         +(kind != null ? kind.getName():"")+" q="
                         +q+" top="+top+" skip="+skip+" fdim="+fdim);
        }

        try {
            TextIndexer.SearchResult result = search
                (kind, q, top, skip, fdim, request().queryString());
            SearchOptions options = result.getOptions();
            
            ObjectMapper mapper = getEntityMapper ();
            ArrayNode nodes = mapper.createArrayNode();
            for (Object obj : result.getMatches()) {
                if (obj != null) {
                    try {
                        ObjectNode node = (ObjectNode)mapper.valueToTree(obj);
                        if (kind == null)
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
            etag.uri = Global.getHost()+request().uri();
            etag.path = request().path();
            etag.sha1 = Util.sha1(request(), "q", "facet");
            etag.query = q;
            etag.method = request().method();
            etag.filter = options.filter;
            etag.save();

            ObjectNode obj = (ObjectNode)mapper.valueToTree(etag);
            obj.put(options.sideway ? "sideway" : "drilldown",
                    mapper.valueToTree(options.facets));
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
