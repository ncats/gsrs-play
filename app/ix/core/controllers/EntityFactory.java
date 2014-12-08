package ix.core.controllers;

import java.io.*;
import java.security.*;
import java.util.*;
import java.util.regex.*;
import java.util.concurrent.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Array;
import java.lang.reflect.Type;
import java.lang.reflect.ParameterizedType;
import javax.persistence.Entity;

import play.libs.Json;
import play.*;
import play.db.ebean.*;
import play.data.*;
import play.mvc.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.annotation.JsonProperty;

import com.avaje.ebean.*;
import com.avaje.ebean.event.BeanPersistListener;

import ix.core.models.ETag;
import ix.core.models.ETagRef;
import ix.core.models.Edit;
import ix.core.models.Principal;
import ix.core.models.BeanViews;
import ix.core.models.Curation;
import ix.utils.Util;

public class EntityFactory extends Controller {
    static final SecureRandom rand = new SecureRandom ();

    static final ExecutorService threadPool = 
        Executors.newCachedThreadPool();

    static final Model.Finder<Long, Edit> editFinder = 
        new Model.Finder(Long.class, Edit.class);

    static final Model.Finder<Long, Principal> principalFinder = 
        new Model.Finder(Long.class, Principal.class);


    protected static <T> List<T> all (Model.Finder<Long, T> finder) {
        return finder.all();
    }


    static class EntityMapper extends ObjectMapper {
        public EntityMapper (Class<?>... views) {
            configure (MapperFeature.DEFAULT_VIEW_INCLUSION, true);
            _serializationConfig = getSerializationConfig();
            for (Class v : views) {
                _serializationConfig = _serializationConfig.withView(v);
            }
        }
    }

    protected static <T> List<T> filter (int top, int skip, 
                                         String expand, String filter, 
                                         Model.Finder<Long, T> finder) {

        Logger.debug(request().uri()+": top="+top+" skip="+skip
                     +" expand="+expand+" filter="+filter);
        Query<T> query = finder.query();
        if (expand != null) {
            StringBuilder path = new StringBuilder ();
            for (String p : expand.split("\\.")) {
                if (path.length() > 0)
                    path.append('.');
                path.append(p);
                Logger.debug("  -> fetch "+path);
                query = query.fetch(path.toString());
            }
        }

        List<T> results = query
            .where(filter)
            .orderBy("id asc")
            .setFirstRow(skip)
            .setMaxRows(top)
            .findList();

        return results;
    }

    protected static <T> Result page (int top, int skip, 
                                      final String expand,
                                      final String filter,
                                      final Model.Finder<Long, T> finder) {

        //if (select != null) finder.select(select);
        List<T> results = filter (top, skip, expand, filter, finder);

        final ETag etag = new ETag ();
        etag.top = top;
        etag.skip = skip;
        etag.query = canonicalizeQuery (request ());
        etag.count = results.size();
        etag.uri = request().uri();
        etag.path = request().path();
        // only include query parameters that fundamentally alters the
        // number of results
        etag.sha1 = Util.sha1Request(request(), "filter");
        etag.method = request().method();
        etag.filter = filter;

        if (filter == null)
            etag.total = finder.findRowCount();
        else {
            Model.Finder<Long, ETag> eFinder = 
                new Model.Finder(Long.class, ETag.class);
            List<ETag> etags = eFinder
                .where().eq("sha1", etag.sha1)
                .orderBy("modified desc").setMaxRows(1).findList();

            if (!etags.isEmpty()) {
                ETag e = etags.iterator().next();
                Logger.debug(">> cached "+etag.sha1+" from ETag "+e.etag);
                etag.total = e.total;
            }
            else {
                // execute in the background to determine the actual number
                // of rows that this query should return
                threadPool.submit(new Runnable () {
                        public void run () {
                            FutureIds<T> future = 
                                finder.where(filter).findFutureIds();
                            try {
                                List<Object> ids = future.get();
                                etag.total = ids.size();
                                etag.save();
                                
                                for (Object id : ids) {
                                    ETagRef ref = new ETagRef (etag, (Long)id);
                                    ref.save();
                                }
                                Logger.debug(Thread.currentThread().getName()
                                             +": "+filter+" => "+ids.size());
                            }
                            catch (Exception ex) {
                                Logger.trace(Thread.currentThread().getName()
                                             +": ETag "+etag.id, ex);
                            }
                        }
                    });
            }
        }
        etag.save();

        ObjectMapper mapper = getEntityMapper ();
        ObjectNode obj = (ObjectNode)mapper.valueToTree(etag);
        obj.put("content", mapper.valueToTree(results));

        return ok (obj);
    }

    static String canonicalizeQuery (Http.Request req) {
        Map<String, String[]> queries = req.queryString();
        Set<String> keys = new TreeSet<String>(queries.keySet());
        StringBuilder q = new StringBuilder ();
        for (String key : keys) {
            if (q.length() > 0)
                q.append('&');

            String[] values = queries.get(key);
            Arrays.sort(values);
            if (values != null && values.length > 0) {
                q.append(key+"="+values[0]);
            }
        }
        return q.toString();
    }

    static EntityMapper getEntityMapper () {
        List<Class> views = new ArrayList<Class>();

        Map<String, String[]> params = request().queryString();
        String[] args = params.get("view");
        if (args != null) {
            Class[] classes = BeanViews.class.getClasses();
            for (String a : args) {
                int matches = 0;
                for (Class c : classes) {
                    if (a.equalsIgnoreCase(c.getSimpleName())) {
                        views.add(c);
                        ++matches;
                    }
                }

                if (matches == 0)
                    Logger.warn("Unsupported view: "+a);
            }
        }
        else {
            views.add(BeanViews.Compact.class);
        }

        return new EntityMapper (views.toArray(new Class[0]));
    }

    protected static <T> T getEntity (Long id, Model.Finder<Long, T> finder) {
        return finder.byId(id);
    }
    
    protected static <T> List<T> filter (String filter, 
                                         Model.Finder<Long, T> finder) {
        return finder.where(filter).findList();
    }

    /**
     * filter by example
     */
    protected static <T> List<T> filter (T instance, 
                                         Model.Finder<Long, T> finder) {
        if (instance == null)
            throw new IllegalArgumentException ("Instance is null");

        Map<String, Object> cons = new HashMap<String, Object>();
        for (Field f : instance.getClass().getFields()) {
            try {
                Object value = f.get(instance);
                Class type = f.getType();
                if (value != null && type.isPrimitive()) {
                    cons.put(f.getName(), value);
                }
            }
            catch (Exception ex) {
                Logger.trace("Unable to retrieve field "+f.getName(), ex);
            }
        }

        List results = new ArrayList ();
        if (cons.isEmpty()) {
            Logger.warn("Can't filter by example because "+instance.getClass()
                        +" doesn't contain any primitive field!");
        }
        else {
            results = finder.where().allEq(cons).findList();
        }
        return results;
    }

    protected static <T> List<T> filter (JsonNode json, int top, int skip,
                                         Model.Finder<Long, T> finder) {
        if (json == null)
            throw new IllegalArgumentException ("Json is null");

        Map<String, Object> cons = new HashMap<String, Object>();
        for (Iterator<String> it = json.fieldNames(); it.hasNext(); ) {
            String field = it.next();
            JsonNode n = json.get(field);
            if (n != null && n.isValueNode() && !n.isNull()) {
                if (n.isNumber()) cons.put(field, n.numberValue());
                else if (n.isTextual()) cons.put(field, n.textValue());
            }
        }

        List results = new ArrayList ();
        if (cons.isEmpty()) {
            Logger.warn("Can't filter by example because JSON"
                        +" doesn't contain any primitive field!");
        }
        else {
            results = finder.where()
                .allEq(cons)
                .orderBy("id asc")
                .setFirstRow(skip)
                .setMaxRows(top)
                .findList();
        }
        return results;
    }

    protected static <T> Result rss (int top, int skip, final String filter,
                                     final Model.Finder<Long, T> finder) {
        response().setContentType("application/rss+xml");
        List<T> results = filter (top, skip, null, filter, finder);
        return ok("FIX ME!");
    }

    protected static <T> Result get (Long id, Model.Finder<Long, T> finder) {
        return get (id, null, finder);
    }

    protected static <T> Result get (Long id, String expand, 
                                     Model.Finder<Long, T> finder) {
        ObjectMapper mapper = getEntityMapper ();
        if (expand != null && !"".equals(expand)) {
            Query<T> query = finder.query();
            Logger.debug(request().uri()+": expand="+expand);

            StringBuilder path = new StringBuilder ();
            for (String p : expand.split("\\.")) {
                if (path.length() > 0)
                    path.append('.');
                path.append(p);
                Logger.debug("  -> fetch "+path);
                query = query.fetch(path.toString());
            }

            T inst = query.setId(id).findUnique();
            if (inst != null) {
                return ok (mapper.valueToTree(inst));
            }
        }
        else {
            T inst = finder.byId(id);
            if (inst != null) {
                return ok (mapper.valueToTree(inst));
            }
        }
        return notFound ("Bad request: "+request().uri());
    }

    protected static <T> Result count (Model.Finder<Long, T> finder) {
        try {
            return ok (String.valueOf(getCount (finder)));
        }
        catch (Exception ex) {
            return internalServerError
                (request().uri()+": can't get count");
        }
    }

    protected static <T> Integer getCount (Model.Finder<Long, T> finder) 
        throws InterruptedException, ExecutionException {
        FutureRowCount<T> count = finder.findFutureRowCount();
        return count.get();
    }

    protected static <T> Result field (Long id, String field, 
                                       Model.Finder<Long, T> finder) {
        /*
        Query<T> query = finder.query();
        int depth = 0;
        if (field != null) {
            StringBuilder path = new StringBuilder ();
            for (String p : field.split("[\\(0-9\\)\\/]+")) {
                if (path.length() > 0) path.append('.');
                path.append(p);
                ++depth;
            }
            if (depth > 1) {
                Logger.debug
                    (request().uri()+": field="+field+" => path="+path);
                query = query.fetch(path.toString());
            }
        }
        */

        T inst = finder.byId(id);
            //query.setId(id).findUnique();
        if (inst == null) {
            return notFound ("Bad request: "+request().uri());
        }

        String[] paths = field.split("/");
        Pattern regex = Pattern.compile("([^\\(]+)\\((-?\\d+)\\)");
        StringBuilder uri = new StringBuilder ();
        
        int i = 0;
        Object obj = inst;
        for (; i < paths.length && obj != null; ++i) {
            String pname = paths[i]; // field name
            Integer pindex = null; // field index if field is a list
            
            Matcher matcher = regex.matcher(pname);
            if (matcher.find()) {
                pname = matcher.group(1);
                pindex = Integer.parseInt(matcher.group(2));
            }

            if (pname.charAt(0) == '$')
                pname = pname.substring(1);

            Logger.debug("pname="+pname+" pindex="+pindex);
            
            try {
                uri.append("/"+paths[i]);

                /**
                 * TODO: check for JsonProperty annotation!
                 */                
                Field f = obj.getClass().getField(pname);
                String fname = f.getName();
                Class<?> ftype = f.getType();
                
                Object val = f.get(obj);
                if (val != null && pindex != null) {
                    if (ftype.isArray()) {
                        if (pindex >= Array.getLength(val)) {
                            return badRequest
                                (uri+": array index out of bound "
                                 +pindex);
                        }
                        val = Array.get(val, pindex);
                    }
                    else if (Collection.class.isAssignableFrom(ftype)) {
                        if (pindex >= ((Collection)val).size() || pindex < 0)
                            return badRequest
                                (uri+": list index out bound "+pindex);
                        val = ((Collection)val).toArray()[pindex];
                    }
                }
                obj = val;
            }
            catch (NoSuchFieldException ex) {
                // now try method..
                String method = "get"+pname;
                /*
                Logger.debug("Can't lookup field \""+pname
                             +"\"; trying method \""+method+"\"...");
                */
                Object old = obj;
                /**
                 * TODO: check for JsonProperty annotation!
                 */
                for (Method m : obj.getClass().getMethods()) {
                    if (m.getName().equalsIgnoreCase(method)) {
                        try {
                            Object val = m.invoke(obj);
                            if (val != null && pindex != null) {
                                Class<?> ftype = val.getClass();
                                if (ftype.isArray()) {
                                    if (pindex >= Array.getLength(val)) {
                                        return badRequest
                                            (uri+": array index out of bound "
                                             +pindex);
                                    }
                                    val = Array.get(val, pindex);
                                }
                                else if (Collection.class
                                         .isAssignableFrom(ftype)) {
                                    if (pindex >= ((Collection)val).size()
                                        || pindex < 0)
                                        return badRequest
                                            (uri+": list index out bound "
                                             +pindex);
                                    Iterator it = ((Collection)val).iterator();
                                    for (int k = 0; it.hasNext() 
                                             && k < pindex; ++k)
                                        ;
                                    val = it.next();
                                }
                            }
                            obj = val;
                            break; // don't 
                        }
                        catch (Exception e) {
                        }
                    }
                }

                if (old == obj) {
                    Logger.error
                        (uri.toString()
                         +": No method or field matching requested path");
                    return notFound ("Invalid field path: "+uri);
                }
            }
            catch (Exception ex) {
                Logger.error(uri.toString(), ex);
                return notFound ("Invalid field path: "+uri);
            }
        }
        
        if (i < paths.length) {
            return badRequest ("Path "+uri+" is null");
        }

        boolean isRaw = paths[paths.length-1].charAt(0) == '$';
        if (obj == null) {
            return isRaw ? noContent () : ok ("null");
        }
        
        ObjectMapper mapper = getEntityMapper ();
        JsonNode node = mapper.valueToTree(obj);
        return isRaw && !node.isContainerNode() 
            ? ok (node.asText()) : ok (node);
    }

    protected static <T extends Model> 
                                Result create (Class<T> type, 
                                               Model.Finder<Long, T> finder) {
        if (!request().method().equalsIgnoreCase("POST")) {
            return badRequest ("Only POST is accepted!");
        }

        String content = request().getHeader("Content-Type");
        if (content == null || (content.indexOf("application/json") < 0
                                && content.indexOf("text/json") < 0)) {
            return badRequest ("Mime type \""+content+"\" not supported!");
        }

        try {
            ObjectMapper mapper = new ObjectMapper ();
            JsonNode node = request().body().asJson();

            T inst = mapper.treeToValue(node, type);
            inst.save();

            return created (mapper.valueToTree(inst));
        }
        catch (Exception ex) {
            return internalServerError (ex.getMessage());
        }
    }

    protected static <T extends Model> 
                                Result delete (Long id, 
                                               Model.Finder<Long, T> finder) {
        T inst = finder.ref(id);
        if (inst != null) {
            ObjectMapper mapper = getEntityMapper ();
            JsonNode node = mapper.valueToTree(inst);
            inst.delete();
            return ok (node);
        }
        return notFound (request().uri()+" not found");
    }

    protected static Result edits (Long id, Class<?>... cls) {
        for (Class<?> c : cls) {
            List<Edit> edits = editFinder.where
                (Expr.and(Expr.eq("refid", id),
                          Expr.eq("kind", c.getName())))
                .orderBy("modified desc").findList();
            if (!edits.isEmpty()) {
                ObjectMapper mapper = getEntityMapper ();
                return ok (mapper.valueToTree(edits));
            }
        }

        return notFound (request().uri()+": No edit history found!");
    }

    protected static <T extends Model> Result update 
        (Long id, String field, Class<T> type, Model.Finder<Long, T> finder) {

        if (!request().method().equalsIgnoreCase("PUT")) {
            return badRequest ("Only PUT is accepted!");
        }

        String content = request().getHeader("Content-Type");
        if (content == null || (content.indexOf("application/json") < 0
                                && content.indexOf("text/json") < 0)) {
            return badRequest ("Mime type \""+content+"\" not supported!");
        }

        T obj = finder.ref(id);
        if (obj == null)
            return notFound ("Not a valid entity id="+id);

        Curation curation = null;
        if (request().username() != null) {
            Principal principal = principalFinder
                .where().eq("name", request().username())
                .findUnique();
            curation = new Curation (principal);
            // create new user if doesn't exist
            /*
            if (principal == null) {
                principal = new Principal (request().username());
                principal.save();
            }
            */
        }

        /**
         * TODO: have a mechanism whereby the principal is verified
         * to her permission to update this field
         */

        Logger.debug("Updating "+obj.getClass()+": id="+id+" field="+field);
        try {
            ObjectMapper mapper = getEntityMapper ();
            JsonNode value = request().body().asJson();

            String[] paths = field.split("/");
            if (paths.length == 1 
                && (paths[0].equals("_") || paths[0].equals("*"))) {
                T inst = mapper.treeToValue(value, type);
                try {
                    Method m = inst.getClass().getMethod("setId");
                    m.invoke(obj, id);
                    inst.update();
                }
                catch (NoSuchMethodException ex) {
                    return internalServerError (ex.getMessage());
                }

                return created (mapper.valueToTree(inst));
            }
            else {
                Object inst = obj;
                StringBuilder uri = new StringBuilder ();

                Pattern regex = Pattern.compile("([^\\(]+)\\((-?\\d+)\\)");

                List<Object[]> changes = new ArrayList<Object[]>();
                for (int i = 0; i < paths.length; ++i) {
                    Logger.debug("paths["+i+"/"+paths.length+"]:"+paths[i]);

                    String pname = paths[i]; // field name
                    Integer pindex = null; // field index if field is a list

                    Matcher matcher = regex.matcher(pname);
                    if (matcher.find()) {
                        pname = matcher.group(1);
                        pindex = Integer.parseInt(matcher.group(2));
                    }
                    Logger.debug("pname="+pname+" pindex="+pindex);
                    
                    try {
                        uri.append("/"+paths[i]);

                        Field f = inst.getClass().getField(pname);
                        String fname = f.getName();
                        String bname = getBeanName (fname);
                        Class<?> ftype = f.getType();

                        Object val = f.get(inst);
                        if (pindex != null) {
                            if (val == null) {
                                return internalServerError 
                                    ("Property \""+fname+"\" is null!");
                            }

                            if (ftype.isArray()) {
                                if (pindex >= Array.getLength(val) 
                                    || pindex < 0) {
                                    return badRequest
                                        (uri+": array index out of bound "
                                         +pindex);
                                }

                                val = Array.get(val, pindex);
                            }
                            else if (Collection
                                     .class.isAssignableFrom(ftype)) {
                                if (pindex >= ((Collection)val).size()
                                    || pindex < 0)
                                    return badRequest
                                        (uri+": list index out bound "
                                         +pindex);
                                Iterator it = ((Collection)val).iterator();
                                for (int k = 0; it.hasNext() 
                                         && k < pindex; ++k)
                                    ;
                                val = it.next();
                            }
                            else {
                                return badRequest 
                                    ("Property \""
                                     +fname+"\" is not an array or list");
                            }
                            Logger.debug(fname+"["+pindex+"] = "+val);
                        }
                        else {
                            /*
                              try {
                              Method m = inst.getClass()
                              .getMethod("get"+bname);
                              val = m.invoke(inst);
                              }
                              catch (NoSuchMethodException ex) {
                              Logger.warn("Can't find bean getter for "
                              +"field \""+fname+"\" in class "
                              +inst.getClass());
                              val = f.get(inst);
                              }
                            */
                            String oldVal = mapper.writeValueAsString(val);
                            
                            if (i+1 < paths.length) {
                                if (val == null) {
                                    // create new instance
                                    Logger.debug
                                        (uri+": new instance "+f.getType());
                                    val = f.getType().newInstance();
                                }
                            }
                            else {
                                // check to see if it references an existing 
                                // entity
                                if (value != null && !value.isNull()) {
                                    try {
                                        val = getJsonValue 
                                            (val, value, f, pindex);
                                    }
                                    catch (Exception ex) {
                                        Logger.trace
                                            ("Can't retrieve value", ex);
                                        
                                        return internalServerError
                                            (ex.getMessage());
                                    }
                                }
                                else {
                                    val = null;
                                }
                                
                                Logger.debug("Updating "+f.getDeclaringClass()
                                             +"."+f.getName()+" = new:"
                                             + (val != null ? 
                                                (val.getClass()+" "+val)
                                                : "null")
                                             +" old:"+oldVal);
                            }
                            
                            /*
                             * We can't use f.set(inst, val) here since it
                             * doesn't generate proper notifications to ebean
                             * for update
                             */
                            try {
                                Method set = inst.getClass().getMethod
                                    ("set"+bname, ftype);
                                set.invoke(inst, val);
                                changes.add(new Object[]{
                                                uri.toString(), 
                                                oldVal,
                                                val});
                            }
                            catch (Exception ex) {
                                Logger.error
                                    ("Can't find bean setter for field \""
                                     +fname+"\" in class "
                                     +inst.getClass(),
                                     ex);
                                
                                return internalServerError
                                    ("Unable to map path "+uri+"!");
                            }
                        }

                        if (val != null) {
                            ftype = val.getClass();
                            if (null != ftype.getAnnotation(Entity.class)) {
                                try {
                                    f = ftype.getField("id");
                                    /**
                                     * Record edit history on the lowest 
                                     * Entity class
                                     */
                                    type = (Class<T>)ftype;
                                    id = (Long)f.get(val);
                                }
                                catch (NoSuchFieldException ex) {
                                    Logger.warn
                                        (ftype.getName()
                                         +": Entity doesn't have field id");
                                }
                            }
                        }
                        inst = val;
                    }
                    catch (NoSuchFieldException ex) {
                        Logger.error(uri.toString(), ex);
                        return notFound ("Invalid field path: "+uri);
                    }
                }
                obj.update();

                for (Object[] c : changes) {
                    Edit e = new Edit (type, id);
                    e.path = (String)c[0];
                    if (curation != null)
                        e.curations.add(curation);
                    e.oldValue = (String)c[1];
                    e.newValue = mapper.writeValueAsString(c[2]);
                    e.save();
                    //Logger.debug("Edit "+e.id+" kind:"+e.kind+" old:"+e.oldValue+" new:"+e.newValue);
                }

                return ok (mapper.valueToTree(obj));
            }
        }
        catch (Exception ex) {
            ex.printStackTrace();
            Logger.error("Instance "+id, ex);
            return internalServerError (ex.getMessage());
        }
    } // update ()

    protected static Object getJsonValue 
        (Object val, JsonNode value, Field field, Integer index) 
        throws Exception {

        JsonNode node = value.get("id");
        Class<?> ftype = field.getType();

        Logger.debug("node: "+node+" "+ftype.getName()+"; val="+val);
        ObjectMapper mapper = getEntityMapper ();
        if (node != null && !node.isNull()) {
            long id = node.asLong();

            Model.Finder finder = new Model.Finder(Long.class, ftype);
            val = finder.byId(id);
        }
        else if (value.isArray()) {
            // if index is null, we replace the list
            // if index >= 0, then we append as the index position
            if (ftype.isArray()) {
                Class<?> c = ftype.getComponentType();
                if (val == null || index == null) {
                    val = Array.newInstance(c, value.size());
                    for (int k = 0; k < value.size(); ++k) {
                        Array.set(val, k, mapper.treeToValue(value.get(k), c));
                    }
                }
                else {
                    int len = Math.max(0, index);
                    if (len > Array.getLength(val)) {
                        throw new IllegalArgumentException
                            ("Invalid array index: "+len);
                    }
                    Object newval = Array.newInstance(c, value.size()+len);
                    int k = 0;
                    for (; k < len; ++k)
                        Array.set(newval, k, Array.get(val, k));
                    for (int i = 0; i < value.size(); ++i, ++k)
                        Array.set(newval, k, 
                                  mapper.treeToValue(value.get(i), c));
                    val = newval;
                }
                Logger.debug("Converted Json to Array (size="
                             +Array.getLength(val)+") of "+c);
            }
            else if (Collection.class.isAssignableFrom(ftype)) {
                if (val == null || index == null) {
                    val = new ArrayList ();
                    index = -1;
                }
                else if (index > ((Collection)val).size())
                    throw new IllegalArgumentException
                        ("Invalid list index: "+index);

                Collection vals = (Collection)val;
                Class cls = getComponentType (field.getGenericType());

                /* for now we only handle append or replace... no
                 * insert in a specific position 
                 */
                if (index < 0) {
                    for (Object obj : vals) {
                        if (obj instanceof Model) {
                            ((Model)obj).delete();
                        }
                    }
                }
                
                for (int k = 0; k < value.size(); ++k) {
                    vals.add(mapper.treeToValue(value.get(k), cls));
                }

                /*
                else {
                    ArrayList copy = new ArrayList ();
                    Iterator it = vals.iterator();
                    for (int k = 0; k < index && it.hasNext(); ++k)
                        copy.add(it.next());

                    for (int i = 0; i < value.size(); ++i)
                        copy.add(mapper.treeToValue(value.get(i), cls));

                    while (it.hasNext())
                        copy.add(it.next());

                    vals.addAll(copy);
                }
                */
                Logger.debug("Converted Json to List (size="
                             +((Collection)val).size()+") of "+cls);
            }
            else {
                Logger.error("Json is of type array "
                             +"but "+field.getDeclaringClass()+"."
                             +field.getName()+" is of type "+ftype);
            }
        }
        else if (ftype.isArray()) {
            Class<?> c = ftype.getComponentType();

            if (index == null 
                || index < 0 
                || index >= Array.getLength(val)) {
                if (val == null) {
                    val = Array.newInstance(c, 1);
                    index = 0;
                }
                else {
                    index = Array.getLength(val);
                    Object newval = Array.newInstance(c, index+1);
                    // copy array
                    for (int i = 0; i < index; ++i)
                        Array.set(newval, i, Array.get(val, i));
                    val = newval;
                }
            }
            else {
                int len = Array.getLength(val);
                Object newval = Array.newInstance(c, len);
                // copy array
                for (int i = 0; i < len; ++i)
                    Array.set(newval, i, Array.get(val, i));
                val = newval;
            }
            Array.set(val, index, mapper.treeToValue(value, c));
        }
        else if (Collection.class.isAssignableFrom(ftype)) {
            Class<?> c = getComponentType (field.getGenericType());
            if (index == null || index < 0 
                || index >= ((Collection)val).size()) {
                if (val == null) {
                    val = new ArrayList ();
                }
                else {
                    ArrayList newval = new ArrayList ();
                    newval.addAll((Collection)val);
                    val = newval;
                }
                ((Collection)val).add(mapper.treeToValue(value, c));
            }
            else {
                ArrayList newval = new ArrayList ();
                newval.addAll((Collection)val);
                Object el = newval.get(index);
                // now update this object
                if (el != null) {
                    updateBean (el, value);
                    Logger.debug("Updating "+el+" with "+value);
                }
                val = newval;
            }
        }
        else {
            val = mapper.treeToValue(value, ftype);
        }

        return val;
    } // getJsonValue ()

    static Class<?> getComponentType (Type type) {
        Class cls;
        if (type instanceof ParameterizedType) {
            Type[] types = ((ParameterizedType)type)
                .getActualTypeArguments();
            
            if (types.length != 1) {
                throw new IllegalStateException
                    ("Nested generic types not supported!");
            }
            cls = (Class)types[0];
        }
        else {
            cls = (Class)type;
        }
        return cls;
    }

    static String getBeanName (String field) {
        return Character.toUpperCase(field.charAt(0))+field.substring(1);
    }

    static void updateBean (Object bean, JsonNode value) {
        ObjectMapper mapper = new ObjectMapper ();
        Iterator<Map.Entry<String, JsonNode>> it = value.fields();
        while (it.hasNext()) {
            Map.Entry<String, JsonNode> jf = it.next();
            String set = "set"+getBeanName (jf.getKey());
            Class<?> type = null;
            try {
                Field bf = bean.getClass().getField(jf.getKey());
                type = bf.getType();
                Method m = bean.getClass().getMethod(set, type);
                m.invoke(bean, mapper.treeToValue(jf.getValue(), type));
            }
            catch (NoSuchFieldException ex) {
                Logger.debug("Ignore field '"+jf.getKey()+"'");
            }
            catch (NoSuchMethodException ex) {
                Logger.error("No such method '"
                             +set+"("+type+")' for class "
                             +bean.getClass());
            }
            catch (Exception ex) {
                Logger.trace("Can't update bean '"+set+"'", ex);
            }
        }
    }
}
