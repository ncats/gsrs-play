package ix.core.controllers;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.TreeSet;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OptimisticLockException;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.Expr;
import com.avaje.ebean.FutureRowCount;
import com.avaje.ebean.Query;
import com.avaje.ebean.Transaction;
import com.avaje.ebean.annotation.Transactional;
import com.avaje.ebean.bean.EntityBean;
import com.avaje.ebean.bean.EntityBeanIntercept;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.deser.DeserializationProblemHandler;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.node.ObjectNode;

import ix.core.DefaultValidator;
import ix.core.ValidationMessage;
import ix.core.ValidationResponse;
import ix.core.Validator;
import ix.core.adapters.EntityPersistAdapter;
import ix.core.models.BeanViews;
import ix.core.models.DataVersion;
import ix.core.models.ETag;
import ix.core.models.Edit;
import ix.core.models.ForceUpdatableModel;
import ix.core.models.Principal;
import ix.core.models.Structure;
import ix.core.plugins.TextIndexerPlugin;
import ix.core.search.TextIndexer;
import ix.ginas.models.v1.Substance;
import ix.utils.Global;
import ix.utils.Util;
import ix.utils.pojopatch.PojoDiff;
import ix.utils.pojopatch.PojoPatch;
import play.Logger;
import play.Play;
import play.db.ebean.Model;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.Results;

public class EntityFactory extends Controller {
    private static final String RESPONSE_TYPE_PARAMETER = "type";

	static final SecureRandom rand = new SecureRandom ();

    static final ExecutorService _threadPool = 
        Executors.newCachedThreadPool();

    static Model.Finder<Long, Principal> _principalFinder;

    static TextIndexer _textIndexer;
    
    static{
    	init();
    }
    
    public static void init(){
    	_textIndexer=Play.application().plugin(TextIndexerPlugin.class).getIndexer();
    	_principalFinder=new Model.Finder(Long.class, Principal.class);
    }

    public static class FetchOptions {
        public int top=10;
        public int skip=0;
        public String filter;
        public List<String> expand = new ArrayList<String>();
        public List<String> order = new ArrayList<String>();
        public List<String> select = new ArrayList<String>();

        // only in the context of a request
                        
        public FetchOptions () {
            for (Map.Entry<String, String[]> me
                     : request().queryString().entrySet()) {
                String param = me.getKey();
                if ("order".equalsIgnoreCase(param)) {
                    for (String s : me.getValue())
                        order.add(s);
                }
                else if ("expand".equalsIgnoreCase(param)) {
                    for (String s : me.getValue())
                        expand.add(s);
                }
                else if ("select".equalsIgnoreCase(param)) {
                    for (String s : me.getValue())
                        select.add(s);
                }
                else if ("top".equalsIgnoreCase(param)) {
                    String n = me.getValue()[0];
                    try {
                        top = Integer.parseInt(n);
                    }
                    catch (NumberFormatException ex) {
                        Logger.trace("Bogus top value: "+n, ex);
                    }
                }
                else if ("skip".equalsIgnoreCase(param)) {
                    String n = me.getValue()[0];
                    try {
                        skip = Integer.parseInt(n);
                    }
                    catch (NumberFormatException ex) {
                        Logger.trace("Bogus skip value:"+n, ex);
                    }
                }
                else if ("filter".equalsIgnoreCase(param)) {
                    filter = me.getValue()[0];
                }
            }
        }
        
        public FetchOptions (int top, int skip, String filter) {
                try{
                    for (Map.Entry<String, String[]> me
                             : request().queryString().entrySet()) {
                        String param = me.getKey();
                        if ("order".equalsIgnoreCase(param)) {
                            for (String s : me.getValue())
                                order.add(s);
                        }
                        else if ("expand".equalsIgnoreCase(me.getKey())) {
                            for (String s : me.getValue())
                                expand.add(s);
                        }
                    }
                }catch(Exception e){
                        
                }
            this.top = top;
            this.skip = skip;
            this.filter = filter;
        }
        // TP: 03/01/2016
        // TODO: have someone look into this
        public static String normalizeFilter(String f){
        	if(f!=null){
        		f=f.replaceAll("\\s*=\\s*null", " is null");
        		return f;
        	}
        	return f;
        }
        public Query applyToQuery(Query q){
        	for (String path : this.expand) {
                Logger.debug("  -> fetch "+path);
                q = q.fetch(path);
            }
        	if(this.filter!=null){
        		q = q.where(normalizeFilter(this.filter));
        	}

            if (!this.order.isEmpty()) {
                for (String order : this.order) {
                    Logger.debug("  -> order "+order);
                    char ch = order.charAt(0);
                    if (ch == '$') { // desc
                        q = q.order(order.substring(1)+" desc");
                    }else {
                        if (ch == '^') {
                            order = order.substring(1);
                        }
                    // default to asc
                        q = q.order(order+" asc");
                    }
                }
            }
            return q;
        }

        public String toString () {
            return "FetchOptions{top="+top+",skip="+skip
                +",expand="+expand.size()
                +",filter="+filter+",order="+order.size()+"}";
        }
    }

    protected static <K,T> List<T> all (Model.Finder<K, T> finder) {
        return finder.all();
    }


    static public class EntityMapper extends ObjectMapper {
        public static EntityMapper FULL_ENTITY_MAPPER(){
        	return new EntityMapper(BeanViews.Full.class);
        }

		public static EntityMapper COMPACT_ENTITY_MAPPER() {
			return new EntityMapper(BeanViews.Compact.class);
		}
		
        public EntityMapper (Class<?>... views) {
            configure (MapperFeature.DEFAULT_VIEW_INCLUSION, true);
            configure (SerializationFeature.WRITE_NULL_MAP_VALUES, false);
            _serializationConfig = getSerializationConfig();
            for (Class v : views) {
                _serializationConfig = _serializationConfig.withView(v);
            }
            addHandler ();
        }

        void addHandler () {
            addHandler (new DeserializationProblemHandler () {
                    public boolean handleUnknownProperty
                        (DeserializationContext ctx, JsonParser parser,
                         JsonDeserializer deser, Object bean, String property) {
                        return _handleUnknownProperty
                            (ctx, parser, deser, bean, property);
                    }
                });
        }
        
        public EntityMapper () {
            addHandler ();
        }

        public boolean _handleUnknownProperty
            (DeserializationContext ctx, JsonParser parser,
             JsonDeserializer deser, Object bean, String property) {
            try {
                Logger.warn("Unknown property \""
                            +property+"\" (token="
                            +parser.getCurrentToken()
                            +") while parsing "
                            +bean+"; skipping it..");
                parser.skipChildren();
            }
            catch (IOException ex) {
                ex.printStackTrace();
                Logger.error
                    ("Unable to handle unknown property!", ex);
                return false;
            }
            return true;
        }

        public String toJson (Object obj) {
            return toJson (obj, false);
        }
        
        public String toJson (Object obj, boolean pretty) {
            try {
                return pretty
                    ? writerWithDefaultPrettyPrinter().writeValueAsString(obj)
                    : writeValueAsString (obj);
            }
            catch (Exception ex) {
                Logger.trace("Can't write Json", ex);
            }
            return null;
        }
    }

    protected static class EditHistory implements PropertyChangeListener {
        List<Edit> edits = new ArrayList<Edit>();
        ObjectMapper mapper = getEntityMapper ();
        final public Edit edit;
        
        
        
        public EditHistory (String payload) throws Exception {
            edit = new Edit ();
            edit.path = null; 
            edit.newValue = payload;
           
        }
        

        public void add (Edit e) { edits.add(e); }
        public List<Edit> edits () { return edits; }
        public void attach (Object ebean, final Callable callback) {
            if (ebean instanceof EntityBean) {
                ((EntityBean)ebean)._ebean_intercept()
                    .addPropertyChangeListener(new PropertyChangeListener(){

						@Override
						public void propertyChange(PropertyChangeEvent evt) {
							//System.out.println(evt.getPropertyName() + " changed to " + evt.getNewValue() + " from " + evt.getOldValue());
							EditHistory.this.propertyChange(evt);
							try {
								callback.call();
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
                    	
                    	
                    });
                
            }
            else {
                throw new IllegalArgumentException
                    (ebean+" is not of an EntityBean!");
            }
        }
        public void detach (Object ebean) {
            if (ebean instanceof EntityBean) {
                ((EntityBean)ebean)._ebean_intercept()
                    .removePropertyChangeListener(this);
            }
            else {
                throw new IllegalArgumentException
                    (ebean+" is not of an EntityBean!");
            }
        }

        public void propertyChange (PropertyChangeEvent e) {
            Logger.debug("### "+e.getSource()+": propertyChange: name="
                         +e.getPropertyName()+" old="+e.getOldValue()
                         +" new="+e.getNewValue());
            try {
                Edit edit = new Edit ();
                Object id = getId (e.getSource());
                if (id != null)
                    edit.refid = id.toString();
                else
                    Logger.warn("No id set of edit for "+e.getSource());
                edit.kind = e.getSource().getClass().getName();
                edit.path = e.getPropertyName();
                edit.oldValue = mapper.writeValueAsString(e.getOldValue());
                edit.newValue = mapper.writeValueAsString(e.getNewValue());
                edits.add(edit);
                
            }
            catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    protected static <K,T> List<T> filter (FetchOptions options,
                                           Model.Finder<K, T> finder) {

        try{
                Logger.debug(request().uri()+": "+options);
        }catch(Exception e){
                Logger.debug("non-request-bound: "+options);
        }
        Query<T> query = finder.query();
        
        query=options.applyToQuery(query);
        /*
        for (String path : options.expand) {
            Logger.debug("  -> fetch "+path);
            query = query.fetch(path);
        }
        query = query.where(options.filter);

        if (!options.order.isEmpty()) {
            for (String order : options.order) {
                Logger.debug("  -> order "+order);
                char ch = order.charAt(0);
                if (ch == '$') { // desc
                    query = query.order(order.substring(1)+" desc");
                }
                else {
                    if (ch == '^') {
                        order = order.substring(1);
                    }
                // default to asc
                    query = query.order(order+" asc");
                }
            }
        }
        else {
            //query = query.orderBy("id asc");
        }
        */

        try {
            long start = System.currentTimeMillis();
            List<T> results = query
                .setFirstRow(options.skip)
                .setMaxRows(options.top)
                .findList();
            Logger.debug(" => "+results.size()+" in "+(System.currentTimeMillis()-start)+"ms");
            return results;
        }
        catch (Exception ex) {
            ex.printStackTrace();
            throw new RuntimeException ("Can't execute query "+ex.getMessage());
        }
    }

    protected static <K,T> Result page (int top, int skip, String filter,
                                        final Model.Finder<K, T> finder) {

        //if (select != null) finder.select(select);
        final FetchOptions options = new FetchOptions (top, skip, filter);
        List<T> results = filter (options, finder);
        
        
        final ETag etag = new ETag ();
        etag.top = options.top;
        etag.skip = options.skip;
        etag.query = canonicalizeQuery (request ());
        etag.count = results.size();
        etag.uri = Global.getHost()+request().uri();
        etag.path = request().path();
        // only include query parameters that fundamentally alters the
        // number of results
        etag.sha1 = Util.sha1(request(), "filter");
        etag.method = request().method();
        etag.filter = options.filter;

        if (options.filter == null)
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
                // TODO: Need to use Akka here!
                // execute in the background to determine the actual number
                // of rows that this query should return
                /*
                _threadPool.submit(new Runnable () {
                        public void run () {
                            FutureIds<T> future = 
                                finder.where(options.filter).findFutureIds();
                            try {
                                List<Object> ids = future.get();
                                etag.total = ids.size();
                                etag.save();
                                
                                for (Object id : ids) {
                                    ETagRef ref = new ETagRef (etag, (Long)id);
                                    ref.save();
                                }
                                Logger.debug(Thread.currentThread().getName()
                                             +": "+options.filter+" => "
                                             +ids.size());
                            }
                            catch (Exception ex) {
                                ex.printStackTrace();
                                Logger.trace(Thread.currentThread().getName()
                                             +": ETag "+etag.id, ex);
                            }
                        }
                    });
                */
            }
        }
        
        try{
            etag.save();
        }
        catch (Exception e) {
            Logger.error
                ("Error saving etag. This sometimes happens on empty DB");
        }

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

    
    
    static public EntityMapper getEntityMapper () {
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
        }else {
            views.add(BeanViews.Compact.class);
        }
        EntityMapper em=new EntityMapper (views.toArray(new Class[0]));
        
        
        return new EntityMapper (views.toArray(new Class[0]));
    }

    protected static <K,T> T getEntity (K id, Model.Finder<K, T> finder) {
        return finder.byId(id);
    }
    
    protected static <K,T> List<T> filter (String filter, 
                                           Model.Finder<K, T> finder) {
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

    protected static <K,T> List<T> filter (JsonNode json, int top, int skip,
                                           Model.Finder<K, T> finder) {
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
                //.orderBy("id asc")
                .setFirstRow(skip)
                .setMaxRows(top)
                .findList();
        }
        return results;
    }

    protected static <K,T> Result get (K id, Model.Finder<K, T> finder) {
        return get (id, null, finder);
    }

    protected static <K,T> Result doc (K id, Model.Finder<K, T> finder) {
        try {
            T inst = finder.byId(id);
            if (inst != null) {
                return ok (_textIndexer.getDocJson(inst));
            }
            return notFound ("Bad request: "+request().uri());
        }
        catch (Exception ex) {
            ex.printStackTrace();
            return internalServerError
                (request().uri()+": "+ex.getMessage());
        }
    }

    protected static <K,T> Result get (K id, String expand, 
                                       Model.Finder<K, T> finder) {
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

    protected static <K,T> Result count (Model.Finder<K, T> finder) {
        try {
            return ok (String.valueOf(getCount (finder)));
        }
        catch (Exception ex) {
            return internalServerError
                (request().uri()+": can't get count");
        }
    }

    protected static <K,T> Integer getCount (Model.Finder<K, T> finder) 
        throws InterruptedException, ExecutionException {
        FutureRowCount<T> count = finder.findFutureRowCount();
        return count.get();
    }
    
//    protected static Integer getCount () 
//            throws InterruptedException, ExecutionException {
//            //FutureRowCount<T> count = finder.findFutureRowCount();
//            return 0;
//    }

    protected static <K,T> Result field (K id, String field, 
                                         Model.Finder<K, T> finder) {
        try {
            Logger.debug("id: "+id+" field: "+field);
            T inst = finder.byId(id);
            //query.setId(id).findUnique();
            if (inst == null) {
                return notFound ("Bad request: "+request().uri());
            }
            return field (inst, field);
        }
        catch (Exception ex) {
            ex.printStackTrace();
            return internalServerError (ex.getMessage());
        }
    }
    
    protected static <T> Result field (Object inst, String field) {
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
        String[] paths = field.split("/");
        Pattern regex = Pattern.compile("([^\\(]+)\\((-?\\d+)\\)");
        StringBuilder uri = new StringBuilder ();

        boolean isRaw = paths[paths.length-1].charAt(0) == '$'; 
        int i = 0;
        Field f = null;
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

            Logger.debug("obj="+obj+"["+obj.getClass()
                         +"] pname="+pname+" pindex="+pindex);
            
            try {
                uri.append("/"+paths[i]);

                /**
                 * TODO: check for JsonProperty annotation!
                 */                
                f = obj.getClass().getField(pname);
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
                    // last resort.. serialize as json and iterate from there
                    ObjectMapper mapper = new ObjectMapper ();
                    JsonNode node = mapper.valueToTree(obj).get(pname);
                    if (node == null) {
                        Logger.error(uri.toString()
                                     +": No method or field matching "
                                     +"requested path");
                        return notFound ("Invalid field path: "+uri);
                    }
                    
                    while (++i < paths.length && node != null) {
                        if (pindex != null) {
                            node = node.isArray() && pindex < node.size()
                                ? node.get(pindex) : null;
                        }
                        else {
                            uri.append("/"+paths[i]);
                            pname = paths[i]; // field name
                            pindex = null; // field index if field is a list
            
                            matcher = regex.matcher(pname);
                            if (matcher.find()) {
                                pname = matcher.group(1);
                                pindex = Integer.parseInt(matcher.group(2));
                            }
                            
                            if (pname.charAt(0) == '$')
                                pname = pname.substring(1);
                            
                            node = node.get(pname);
                            if (node == null) {
                                Logger.error(uri.toString()
                                             +": No method or field matching "
                                             +"requested path");
                                return notFound ("Invalid field path: "+uri);
                            }
                        }
                    }

                    if (node == null)
                        return isRaw ? noContent () : ok ("null");
                    
                    return isRaw && !node.isContainerNode()
                        ? ok (node.asText()) : ok (node);
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

        if (obj == null) {
            return isRaw ? noContent () : ok ("null");
        }
        
        ObjectMapper mapper = getEntityMapper ();
        JsonNode node = mapper.valueToTree(obj);
        if (isRaw && !node.isContainerNode()) {
            // make sure the content type is properly set if the
            // value is a json string
            JsonDeserialize json = (JsonDeserialize)
                f.getAnnotation(JsonDeserialize.class);
            Results.Status status = ok (node.asText());
            if (json != null && json.as() == JsonNode.class) {
                status.as("application/json");
            }
            
            return status;
        }
        return ok (node);
    }
    @Transactional
    protected static <K, T extends Model> 
        Result create (Class<T> type, Model.Finder<K, T> finder) {
    	
    	return create(type,finder,null);
    }
    @Transactional
    protected static <K, T extends Model> 
        Result create (Class<T> type, Model.Finder<K, T> finder, Validator<T> validator) {
        if (!request().method().equalsIgnoreCase("POST")) {
            return badRequest ("Only POST is accepted!");
        }
        String content = request().getHeader("Content-Type");
        if (content == null || (content.indexOf("application/json") < 0
                                && content.indexOf("text/json") < 0)) {
            return badRequest ("Mime type \""+content+"\" not supported!");
        }

        try {
            EntityMapper mapper = EntityMapper.FULL_ENTITY_MAPPER();
            mapper.addHandler(new DeserializationProblemHandler () {
                    public boolean handleUnknownProperty
                        (DeserializationContext ctx, JsonParser parser,
                         JsonDeserializer deser, Object bean, String property) {
                        try {
                            Logger.warn("Unknown property \""
                                        +property+"\" (token="
                                        +parser.getCurrentToken()
                                        +") while parsing "
                                        +bean+"; skipping it..");
                            parser.skipChildren();
                        }
                        catch (IOException ex) {
                            ex.printStackTrace();
                            Logger.error
                                ("Unable to handle unknown property!", ex);
                            return false;
                        }
                        return true;
                    }
                });
            
            
            JsonNode node = request().body().asJson();
            T inst = mapper.treeToValue(node, type);
            if(validator!=null){
		            ValidationResponse vr=validator.validate(inst);
		            
	            	if(!vr.isValid()){
		            	return badRequest(validationResponse(vr));
		            }
            }
            inst.save();
            
            Status s=created (mapper.toJson(inst));
            return s;
        }
        catch (Throwable ex) {
        	Logger.error("Problem creating record", ex);
            return internalServerError (ex.getMessage());
        }
    }
    
    public static enum RESPONSE_TYPE{
    	FULL,
    	MESSAGES
    }

    protected static <K, T extends Model> Result validate(Class<T> type,
                                                          Model.Finder<K, T> finder, Validator<T> validator) {
        if (!request().method().equalsIgnoreCase("POST")) {
            return badRequest("Only POST is accepted!");
        }
        String returnType=request().getQueryString(RESPONSE_TYPE_PARAMETER);
        
        RESPONSE_TYPE rept= RESPONSE_TYPE.MESSAGES;
        
        try{
        	rept=RESPONSE_TYPE.valueOf(returnType.toUpperCase());
        }catch(Exception e){
        	
        }
        String content = request().getHeader("Content-Type");
        if (content == null
            || (content.indexOf("application/json") < 0 && content
                .indexOf("text/json") < 0)) {
            return badRequest("Mime type \"" + content + "\" not supported!");
        }

        try {
            ObjectMapper mapper = new ObjectMapper();
            
            mapper.addHandler(new DeserializationProblemHandler() {
                    public boolean handleUnknownProperty(
                                                         DeserializationContext ctx, JsonParser parser,
                                                         JsonDeserializer deser, Object bean, String property) {
                        try {
                            Logger.warn("Unknown property \"" + property
                                        + "\" (token=" + parser.getCurrentToken()
                                        + ") while parsing " + bean + "; skipping it..");
                            parser.skipChildren();
                        } catch (IOException ex) {
                            ex.printStackTrace();
                            Logger.error("Unable to handle unknown property!", ex);
                            return false;
                        }
                        return true;
                    }
                });

            
            JsonNode node = request().body().asJson();
            
            T inst = mapper.treeToValue(node, type);
            
            FetchedValue oldValueContainer=getCurrentValue(inst);
            
            
            
            ValidationResponse<T> vr=validator.validate(inst,(T)oldValueContainer.value);
            
            if(rept==RESPONSE_TYPE.FULL){
            	return ok(validationResponse(vr,true));
            }else{
            	return ok(validationResponse(vr,false));
            }
        } catch (Throwable ex) {
        	ex.printStackTrace();
            return internalServerError(ex.getMessage());
        }
    }
    protected static JsonNode validationResponse(ValidationResponse vr){
    	return validationResponse(vr,false);
    }
    protected static JsonNode validationResponse(ValidationResponse vr, boolean full){
    	EntityMapper mapper = EntityMapper.FULL_ENTITY_MAPPER();
    	if(full){
    		mapper = EntityMapper.COMPACT_ENTITY_MAPPER();
    	}
    	return mapper.valueToTree(vr);
    }

    protected static <K,T extends Model> 
        Result delete (K id, 
                       Model.Finder<K, T> finder) {
        T inst = finder.ref(id);
        if (inst != null) {
            ObjectMapper mapper = getEntityMapper ();
            JsonNode node = mapper.valueToTree(inst);
            inst.delete();
            return ok (node);
        }
        return notFound (request().uri()+" not found");
    }

    protected static Result edits (Object id, Class<?>... cls) {
    	List<Edit> edits = new ArrayList<Edit>();
    	FetchOptions fe=new FetchOptions ();
    	
    	//if(true)return EditFactory.page(fe.top, fe.skip, fe.filter);
    	//EditFactory.page(top, skip, filter)
        for (Class<?> c : cls) {
        	Query q=EditFactory.finder.where
                    (Expr.and(Expr.eq("refid", id.toString()),
                            Expr.eq("kind", c.getName())));
        	q=fe.applyToQuery(q);
            List<Edit> tmpedits = q
                .findList();
            if(tmpedits!=null){
            	edits.addAll(tmpedits);
            }
        }
        if (!edits.isEmpty()) {
            ObjectMapper mapper = getEntityMapper ();
            return ok (mapper.valueToTree(edits));
        }

        return notFound (request().uri()+": No edit history found!");
    }
    
    /**
     * Handle generic update to field, without special deserializationHandler
     * 
     * @param id
     * @param field
     * @param type
     * @param finder
     * @return
     */
    protected static <K, T extends Model> Result update 
        (K id, String field, Class<T> type, Model.Finder<K, T> finder) {
                return update (id,field,type,finder, null, null);
    }
    
    // This expects an update of the full record to be done using "/path/*"
    // or "/path/_"
    //TODO: allow high-level changes to be captured
    protected static <K, T extends Model> Result update 
        (K id, String field, Class<T> type, Model.Finder<K, T> finder,
         DeserializationProblemHandler deserializationHandler, Validator<T> validator) {

        if (!request().method().equalsIgnoreCase("PUT")) {
            return badRequest ("Only PUT is accepted!");
        }

        String content = request().getHeader("Content-Type");
        if (content == null || (content.indexOf("application/json") < 0
                                && content.indexOf("text/json") < 0)) {
            return badRequest ("Mime type \""+content+"\" not supported!");
        }

        Object tempid=id;
        Object temptype=type;

        List<Object[]> changes = new ArrayList<Object[]>();
        
        T obj = finder.ref(id);
        if (obj == null)
            return notFound ("Not a valid entity id="+id);

        
        Object[] rootChange = new Object[]{
                null, 
                (new ObjectMapper()).valueToTree(obj).toString(),
                null,
                type,
                id};
        final Map<Object,Model> oldObjects = new HashMap<Object,Model>();
        recursivelyApply(obj, "", new EntityCallable() {
                @Override
                public void call(Object m, String path) {
                    if (m instanceof Model) {
                        try {
                            
                            Method f = EntityFactory.getIdSettingMethodForBean(m);
                            Object _id = EntityFactory.getIdForBean(m);
                            oldObjects.put(_id, (Model) m);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
        Principal principal = null;
        if (request().username() != null) {
            principal = _principalFinder
                .where().eq("name", request().username())
                .findUnique();
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
            if(deserializationHandler!=null)
                mapper.addHandler(deserializationHandler);
            JsonNode value = request().body().asJson();

            String[] paths = field.split("/");
            if (paths.length == 1 
                && (paths[0].equals("_") || paths[0].equals("*"))) {
                final T inst = mapper.treeToValue(value, type);
                ValidationResponse vr=validator.validate(inst);
                if(!vr.isValid()){
	            	return badRequest(validationResponse(vr,false));
	            }
                try {
                    Method m=EntityFactory.getIdSettingMethodForBean(inst);
                    m.invoke(inst, id);
                }catch (Exception ex) {
                    ex.printStackTrace();
                    return internalServerError (ex.getMessage());
                }
                obj=inst;
                recursivelyApply(inst, "", new EntityCallable(){
                        @Override
                        public void call(Object m, String path) {
                            if(inst == m)return;
                            Model old=null;
                            if(m instanceof Model){
                                try{
                                    
                                    Method f=EntityFactory.getIdSettingMethodForBean(m);
                                    Object _id=EntityFactory.getIdForBean(m);

                                    //Get old model, do something with it?
                                    old=oldObjects.get(_id);
                                    if(_id==null){
                                        //((Model) m).save();
                                    }else{
                                        if(f!=null){
                                            f.invoke(m,_id);
                                        }
                                                                        
                                        ((Model)m).update(_id);
                                        Logger.debug("Success updating:" + path);
                                    }
                                                                
                                }catch(OptimisticLockException e){
                                                                
                                    Logger.error("Lock error:" + path + "\t" + m.getClass().getName());
                                    if(m instanceof Structure && old!=null){
                                        Logger.error("Lock change:" + ((Structure)m).lastEdited + "\t" + ((Structure)old).lastEdited);  
                                    }
                                    if(m.getClass().getName().contains("tructure"))
                                        e.printStackTrace();
                                                                
                                    //Logger.debug((new ObjectMapper()).valueToTree(m).toString());
                                }catch(Exception e){
                                    Logger.error("Error updating:" + path + "\t" + m.getClass().getName());
                                    e.printStackTrace();
                                                                
                                }
                            }
                        }});
                
            } else {
                Object inst = obj;
                StringBuilder uri = new StringBuilder ();

                Pattern regex = Pattern.compile("([^\\(]+)\\((-?\\d+)\\)");

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
                        Object valp = val;
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
                                    val = it.next();
                            }
                            else {
                                return badRequest 
                                    ("Property \""
                                     +fname+"\" is not an array or list");
                            }
                            Logger.debug(fname+"["+pindex+"] = "+val);
                        }
                        
                        {
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
                            
                            //if this is not the final piece
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
                                        Logger.debug("Saving ...." );
                                        val = getJsonValue 
                                            (valp, value, f, pindex);
                                        Logger.debug("Saved" );
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
                            if(pindex == null){
                            try {
                                Method set = inst.getClass().getMethod
                                    ("set"+bname, ftype);
                                set.invoke(inst, val);
                                changes.add(new Object[]{
                                                uri.toString(), 
                                                oldVal,
	                                                val,
	                                                temptype,
	                                                tempid});
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
                        }

                        if (val != null) {
                            ftype = val.getClass();
                            if (null != ftype.getAnnotation(Entity.class)) {
                                Object tid=EntityFactory.getIdForBean(val);
                                if(tid!=null){
                                    tempid=tid;
                                    temptype=ftype;
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
               
            }
            
            //System.out.println((new ObjectMapper()).valueToTree(obj));
            
                obj.update();
            rootChange[0]="/";
            rootChange[2]=obj;
            changes.add(rootChange);


            //eventually, figure out enumerated changes directly
            
//            
//            for (Object[] c : changes) {
//              
//                Edit e = new Edit ((Class<?>) c[3], c[4]);
//                
//                e.path = (String)c[0];
//                e.editor = principal;
//                e.oldValue = (String)c[1];
//                //Need to preserve full tree changes
//                e.newValue = (new ObjectMapper()).writeValueAsString(c[2]);
//                Logger.debug("Saving change" + c + "\t" + id);
//                Transaction tx = Ebean.beginTransaction();
//                try {
//                    e.save();
//                    tx.commit();
                        //Logger.debug("Edit "+e.id+" kind:"+e.kind+" old:"+e.oldValue+" new:"+e.newValue);
//                }
//                catch (Exception ex) {
//                      Logger.error(ex.getMessage());
//                    Logger.trace
//                        ("Can't persist Edit for "+type+":"+id, ex);
//                }
//                finally {
//                    Ebean.endTransaction();
//                }   
//            }
            return ok (mapper.valueToTree(obj));
                    }
                    catch (Exception ex) {
            ex.printStackTrace();
            Logger.error("Instance "+id, ex);
            return internalServerError (ex.getMessage());
                    }
    } // update ()
    
    public static interface EntityCallable{
        public void call(Object m, String path);
    }
    protected static void recursivelyApply(Model entity, String path,
                                           EntityCallable c){
        try{
            for(Field f: entity.getClass().getFields()){
                Class type= f.getType();
                Annotation e=type.getAnnotation(Entity.class);
                        
                if(e!=null){
                    try {
                        Object nextEntity=f.get(entity);
                                        
                        if(nextEntity instanceof Model)
                            recursivelyApply((Model) nextEntity, path + "/" + f.getName(), c);
                    } catch (IllegalArgumentException e1) {
                        e1.printStackTrace();
                    } catch (IllegalAccessException e1) {
                        e1.printStackTrace();
                    }
                } else if (Collection.class.isAssignableFrom(type)) {
                    Collection col = (Collection) f.get(entity);
                    if(col!=null){
                        int i=0;
                        for(Object nextEntity:col){
                            if(nextEntity instanceof Model)
                                recursivelyApply((Model) nextEntity, path + "/" + f.getName() + "(" + i + ")",c);
                            i++;
                        }
                    }
                }
                        
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        c.call(entity, path);
    }

    protected static Result updateEntity (Class<?> type) {

        if (!request().method().equalsIgnoreCase("PUT")) {
            return badRequest ("Only PUT is accepted!");
        }

        String content = request().getHeader("Content-Type");
        if (content == null || (content.indexOf("application/json") < 0
                                && content.indexOf("text/json") < 0)) {
            return badRequest ("Mime type \""+content+"\" not supported!");
        }
        JsonNode json = request().body().asJson();
        return updateEntity (json, type, new DefaultValidator());
    }
    protected static Result updateEntity (JsonNode json, Class<?> type) {
        return updateEntity (json, type, new DefaultValidator());
    }
    
    

    /*
     * Ok, at the most fundamental level, assuming all changes come only through this method,
     * then we just need to store the old JSON, and the new.
     * 
     */
    protected static Result updateEntity (JsonNode json, Class<?> type, Validator validator ) {
        EntityMapper mapper = EntityMapper.FULL_ENTITY_MAPPER();  
        Transaction tx = Ebean.beginTransaction();
        try {       
            Object newValue = mapper.treeToValue(json, type);

            //Fetch old value
            FetchedValue oldValueContainer=getCurrentValue(newValue);
            String oldVersion=EntityFactory.getVersionForBeanAsString(oldValueContainer.value);

            if(oldValueContainer.value==null){
            	throw new IllegalStateException("Cannot update a non-existing record");
            }
            String oldJSON=mapper.toJson(oldValueContainer.value);
            
            //validate new value
            ValidationResponse vr=validator.validate(newValue,oldValueContainer.value);
	        if(!vr.isValid()){
	            return badRequest(validationResponse(vr,false));
	        }

            EditHistory eh = new EditHistory (json.toString());
            
            //this saves and everything
            EntityPersistAdapter.storeEditForUpdate(oldValueContainer.getValueClass(), oldValueContainer.id, eh.edit);
            
            //Get the difference as a patch
            PojoPatch patch =PojoDiff.getDiff(oldValueContainer.value, newValue);
            
            //Apply the changes, grabbing every change along the way
            Stack changeStack=patch.apply(oldValueContainer.value);
        	
            
        	while(!changeStack.isEmpty()){
        		Object v=changeStack.pop();
        		if(v instanceof ForceUpdatableModel){
            		((ForceUpdatableModel)v).forceUpdate();
            	}else if(v instanceof Model){
            		((Model)v).update();
            	}
        	}
        	
        	//The old value is now the new value
        	newValue = oldValueContainer.value;
        	
        	
            String newJSON=mapper.toJson(newValue);
            
            //granular parts not working yet
            if (newValue != null) {
            	    Object id = getId (newValue);
	                eh.edit.refid = id != null ? id.toString() : null;
	                eh.edit.kind = newValue.getClass().getName();
	                eh.edit.oldValue=oldJSON;
	                eh.edit.newValue=newJSON;
	                eh.edit.version=oldVersion;
	                eh.edit.save();
	                for (Edit e : eh.edits()) {
	                    e.batch = eh.edit.id.toString();
	                    e.save();
	                }
	                Logger.debug("** New edit history "+eh.edit.id);
            }
            //Should this be here?
            //EntityPersistAdapter.popEditForUpdate(previousValContainer.getValueClass(), previousValContainer.value);
            
            tx.commit();
            return ok (mapper.valueToTree(newValue));          
        }
        catch (Exception ex) {
        	Logger.error("Error updating record", ex);
            ex.printStackTrace();
            return internalServerError (ex.getMessage());
        }
        finally {
            Ebean.endTransaction();
        }
    }

    private static List<Field> getFields (Object entity, Class... annotation) {
        List<Field> fields = new ArrayList<Field>();
        for (Field f : entity.getClass().getFields()) {
            for (Class c : annotation) {
                if (f.getAnnotation(c) != null) {
                    fields.add(f);
                    break;
                }
            }
        }
        return fields;
    }
    
    private static List<Field> getFieldsForClass (Class entity, Class... annotation) {
        List<Field> fields = new ArrayList<Field>();
        for (Field f : entity.getFields()) {
            for (Class c : annotation) {
                if (f.getAnnotation(c) != null) {
                    fields.add(f);
                    break;
                }
            }
        }
        return fields;
    }
    
    static List getAnnotatedValues (Object entity, Class... annotation)
        throws Exception {
        List values = new ArrayList ();
        for (Field f : getFields (entity, annotation)) {
            Object v = f.get(entity);
            if (v != null)
                values.add(v);
        }
        return values;
    }

    public static Object getId (Object entity) throws Exception {
        Field f = getIdField (entity);
        Object id = null;
        if (f != null) {
            id = f.get(entity);
            if (id == null) { // now try bean method
                try {
                    Method m = entity.getClass().getMethod
                        ("get"+getBeanName (f.getName()));
                    id = m.invoke(entity, new Object[0]);
                }
                catch (NoSuchMethodException ex) {
                    ex.printStackTrace();
                }
            }
        }
        return id;
    }
    public static Object getVersion (Object entity) throws Exception {
        Field f = getVersionField (entity);
        Object version = null;
        if (f != null) {
            version = f.get(entity);
            if (version == null) { // now try bean method
                try {
                    Method m = entity.getClass().getMethod
                        ("get"+getBeanName (f.getName()));
                    version = m.invoke(entity, new Object[0]);
                }
                catch (NoSuchMethodException ex) {
                    ex.printStackTrace();
                }
            }
        }
        return version;
    }

    public static Field getVersionField (Object entity) throws Exception {
        List<Field> fields = getFields (entity, DataVersion.class);
        return fields.isEmpty() ? null : fields.iterator().next();
    }
    public static Field getIdField (Object entity) throws Exception {
        List<Field> fields = getFields (entity, Id.class);
        return fields.isEmpty() ? null : fields.iterator().next();
    }
    public static Field getIdFieldForClass (Class entity) throws Exception {
        List<Field> fields = getFieldsForClass (entity, Id.class);
        return fields.isEmpty() ? null : fields.iterator().next();
    }
    
    static boolean isValid (Field f) {
        int mods = f.getModifiers();
        JsonProperty jp;
        
        return (!Modifier.isStatic(mods)
                && !Modifier.isFinal(mods)
                && !Modifier.isTransient(mods)
                //&& f.getAnnotation(JsonIgnore.class) == null
                && f.getAnnotation(DataVersion.class) == null
                && f.getAnnotation(Id.class) == null);
    }

    /**
     * set value of field using property bean instead of field-based
     * as it's not recognized by ebean
     */
    static Object setValue (Object instance, Field field, Object value)
        throws Exception {
    	//System.out.println("Setting field:" + field.getName());
        Logger.debug("** setValue: field "+field.getName()+"["
                     +instance.getClass().getName()+"] value="+value);
        
        String method = "set" + getBeanName (field.getName());
        Object old = field.get(instance);
        try {
            Method set = instance.getClass().getMethod
                (method, field.getType());
            set.invoke(instance, value);
        }
        catch (NoSuchMethodException ex) {
            Logger.warn("No method \""+method
                        +"\" found in class "+instance.getClass().getName());
            if (instance instanceof EntityBean) {
                // find the closest match?
                Method set = null;
                for (Method m : instance.getClass().getMethods()) {
                    if (m.getName().startsWith("set")) {
                        Class[] types = m.getParameterTypes();
                        if (types.length == 1
                            && types[0].isAssignableFrom(field.getType())) {
                            // let's assume this is what we're looking for
                            set = m;
                            break;
                        }
                    }
                }
                
                if (set == null) {
                    throw ex;
                }
            }
            else { // revert to field-based
                field.set(instance, value);
            }
        }
        return old;
    }

    static void debugBean (Object value) throws Exception {
        if (value == null) {
            return;
        }
        else if (!(value instanceof EntityBean)) {
            Logger.warn("Not an EntityBean: "+value
                        +"["+value.getClass().getName()+"]");
            return;
        }

        EntityBean bean = (EntityBean)value;
        EntityBeanIntercept ebi = bean._ebean_intercept();

        Class cls = value.getClass();
        int c = ebi.getPersistenceContext().size(cls);
        Logger.debug("## There are "+c+" instance of "+cls.getName()+
                     " in the Ebean persitence context!");
        Logger.debug("## intercepting="+ebi.isIntercepting()
                     +" dirty=" + ebi.isDirty()+" new="+ebi.isNew()
                     +" ref="+ebi.isReference()
                     +" context="+ebi.getPersistenceContext());
    }

    
    public static class Instrumented{
    	Object newObject;
    	boolean changed=false;
    }
    /**
     * Ok, this method seems overly complicated. What it's doing is recursively
     * going through and saving each property. That sounds fine, but it causes a 
     * lot of problems elsewhere.
     * 
     * For example, because this is a depth-first traversal, the individual elements
     * will be changed before the parent is. Elsewhere, in a postUpdate hook, each model
     * tries to save its full serialized version before and after update. So consider
     * the following:
     * 
     * OLD value:
     * {
     *  version:1,
     * 	names:[
     * 		{
     * 			"name":"oldname"
     * 		}
     *  ]
     * }
     * 
     * NEW value:
     * {
     *  version:2,
     * 	names:[
     * 		{
     * 			"name":"newname"
     * 		}
     *  ]
     * }
     * 
     * This code will save the "newname" change at '/names/0/' before saving '/'.
     * Then, when the postUpdate hook is called on the parent ('/'), it will fetch
     * the value before update and store it. In this case, that value will still 
     * point to the "newname" updated object. 
     * 
 * 
     * Another problem is that this method uses reflection, and trusts that the POJO
     * are roughly what they need to be for updating. With advanced getters/setters,
     * this may cause problems.
     * 
     * @param eh
     * @param value
     * @param json
     * @return
     * @throws Exception
     */
    protected static Instrumented instrument
    (EditHistory eh, Object value, JsonNode json, String path) throws Exception {
    	FetchedValue fv=getCurrentValue(value);
    	
    	Class cls;
    	Object xval;
    	Object id;
    	
	    cls=fv.getValueClass();
	    id=fv.id;
	    xval=fv.value;
	    final Instrumented retInst= new Instrumented();
	    
	    
	    /*
	    if(xval!=null){
		    EntityMapper om = EntityMapper.FULL_ENTITY_MAPPER();
		    JsonNode o=om.valueToTree(xval);
		    JsonNode n=om.valueToTree(fv.value);
		    JsonNode jp = JsonDiff.asJson(o,n);
			for(JsonNode jschange: jp){
		    	System.out.println("#READ CHANGE:" + jschange + " old:" + o.at(jschange.get("path").asText()));
		
		    }
	    }*/
	    //xval=fv.value;
    
    
    
    
    if (xval == null) {
    	if(!(value instanceof Model)){
    		retInst.newObject=value;
    		return retInst;
    	}
    	System.out.println("Saving new:" + path);
    	((Model)value).save();
    	
        id = getId (value);
        Logger.debug("<<< " + id);
        
        Edit e = new Edit (value.getClass(), id);
        ObjectMapper mapper = new ObjectMapper ();
        e.newValue = mapper.writeValueAsString(value);
        eh.add(e);
        retInst.newObject=value;
        
        //retInst.changed=true;
        return retInst;
    }else{
    	
    }

    
    
    eh.attach(xval, new Callable(){

		@Override
		public Object call() throws Exception {
			
			//retInst.changed=true;
			return null;
		}
    	
    });

    // now sync up val and value
    Map<Field, Object> values = new HashMap<Field, Object>();
    for (Field f : cls.getFields()) {
        JsonProperty prop = f.getAnnotation(JsonProperty.class);
        JsonNode node = null;
        if(json!=null){
        	node=json.get(prop != null ? prop.value() : f.getName());
        }

        Class type = f.getType();       
        Logger.debug("field \""+f.getName()+"\"["+type
                     +"] valid="+isValid(f)
                     +" node="+node);            
        if (!isValid (f) 
        		//|| node == null
        		)
            continue;
        
        Object v = f.get(value);
        if (v != null) {
            if (type.getAnnotation(Entity.class) != null) {
            	Instrumented sinst=instrument (eh, v, node,path+ "/" + f.getName());
                retInst.changed|=sinst.changed;
                setValue (xval, f, sinst.newObject);
            }
            else if (type.isArray()) {
                Class<?> t = type.getComponentType();
                if (t.getAnnotation(Entity.class) != null) {
                    int len = Array.getLength(v);
                    Object vv = f.get(xval);
                    if (vv != null && Array.getLength(vv) > 0) {
                        Logger.debug("$$ TODO: handle array properly!");
                    }
                    else {
                        Object vals = Array.newInstance(t, len);
                        for (int i = 0; i < len; ++i) {
                            Object xv = Array.get(v, i);
                            Instrumented sinst=instrument (eh, xv, node.get(i),path+ "/" + f.getName());
                            retInst.changed|=sinst.changed;
                            Array.set(vals, i,
                                      sinst.newObject);
                        }
                        setValue (xval, f, vals);
                    }
                }
                else {
                    // just copy over
                    setValue (xval, f, v);
                }
            }
            else if (Collection.class.isAssignableFrom(type)) {
                Collection col = (Collection)v;
                //Logger.debug("Enter Collection.."+col.size());
                if (!col.isEmpty()) {
                    Class t = getComponentType (f.getGenericType());
                    //Logger.debug("..component type "+t);
                    if (t.getAnnotation(Entity.class) != null) {
                        Collection xcol = (Collection)f.get(xval);
                        if (xcol == null) {
                            setValue (xval, f, xcol = new ArrayList ());
                        }
                        // update the list wholesale..
                        xcol.clear();
                        
                        int i = 0;
                        for (Iterator it = col.iterator();
                             it.hasNext(); ++i) {
                            Object xv = it.next();
                            Logger.debug(i+": "+xv+" <=> "+node.get(i));
                            Instrumented sinst=instrument (eh, xv, node.get(i),path+ "/" + f.getName());
                            retInst.changed|=sinst.changed;
                            xv = sinst.newObject;
                            xcol.add(xv);
                        }
                    }
                    else {
                        setValue (xval, f, v);
                    }
                }
            }
            else {
                setValue (xval, f, v);
            }
        }
    }
    
    if(false || xval instanceof ForceUpdatableModel){
    	
    	
    	boolean applied=((ForceUpdatableModel)xval).tryUpdate();
    	if(!applied && retInst.changed){
    		((ForceUpdatableModel)xval).forceUpdate();
    		applied=true;
    	}else{
    		
    	}
    	retInst.changed|=applied;
    	
    }else{
    	if(xval instanceof Model){
    		((Model)xval).update();
    	}
    }
    
    
    
    
    Logger.debug("<<< "+id);
    eh.detach(xval);
    retInst.newObject=xval;
    return retInst;
}
    public static class FetchedValue{
    	public Object value;
    	public Object id;
    	public Class cls;
    	
    	public FetchedValue(Object val, Object id, Class cls){
    		this.value=val;
    		this.id=id;
    		this.cls=cls;
    	}
    	
    	public Class getValueClass(){
    		return cls;
    	}
    	
    }
    
    private static FetchedValue getCurrentValue(Object value) throws Exception{
    	Class cls = value.getClass();
        if (cls.getAnnotation(Entity.class) == null)
            throw new IllegalArgumentException
                ("Class "+cls.getName()+" is not an entity");

        Field idf = getIdField (value);
        if (idf == null)
            throw new IllegalArgumentException
                ("Entity "+cls.getName()+" has no Id field!");
        
        Model.Finder finder = new Model.Finder
            (idf.getType(), value.getClass());

        Object xval = null;
        
        Object id = idf.get(value);
        if (id == null) {
            // if this entity has no id set, then we see if there is a
            // unique column defined.. if so, we retrieve it
            List<Field> columns = getFields (value, Column.class);
            // now check which field has unique annotation
            for (Field f : columns) {
                Column column = (Column)f.getAnnotation(Column.class);
                if (column.unique()) {
                    Logger.debug("Field \""+f.getName()
                                 +"\" contains unique value for class "
                                 +value.getClass().getName());
                    xval = finder.where()
                        .eq(column.name().equals("")
                            ? f.getName() : column.name(),
                            f.get(value))
                        .findUnique();
                    
                    if (xval != null)
                        id = getId (xval);
                    
                    break;
                }
            }
        }
        else {
            xval = finder.byId(id);
        }
        return new FetchedValue(xval,id,cls);
    }
    
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
            Logger.debug("Should be a collection dude");
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
                Logger.debug("Yeah, there's an index");
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
        ObjectMapper mapper;
        Iterator<Map.Entry<String, JsonNode>> it = value.fields();
        while (it.hasNext()) {
                mapper = new ObjectMapper ();
            Map.Entry<String, JsonNode> jf = it.next();
            String set = "set"+getBeanName (jf.getKey());
            Class<?> type = null;
            try {
                Field bf = bean.getClass().getField(jf.getKey());
                type = bf.getType();
                Method m = bean.getClass().getMethod(set, type);
                //@JsonDeserialize(using=KeywordListDeserializer.class)
                JsonDeserialize jdes=bf.getAnnotation(JsonDeserialize.class);
                if(jdes!=null){
                        JsonDeserializer jder=jdes.using().newInstance();
                        SimpleModule module =
                                          new SimpleModule("CustomDeserializer",
                                              new Version(1, 0, 0, null));
                        module.addDeserializer(type, jder);
                        mapper.registerModule(module);
                }
                m.invoke(bean, mapper.treeToValue(jf.getValue(), type));
            }
            catch (NoSuchFieldException ex) {
                Logger.debug("Ignore field '"+jf.getKey()+"'");
            }
            catch (NoSuchMethodException ex) {
            	System.out.println("No such method '"
                        +set+"("+type+")' for class "
                        +bean.getClass());
                Logger.error("No such method '"
                             +set+"("+type+")' for class "
                             +bean.getClass());
            }
            catch (Exception ex) {
                Logger.trace("Can't update bean '"+set+"'", ex);
            }
        }
    }

    public static UUID toUUID (String id) {
        if (id.length() == 32) { // without -'s
            id = id.substring(0,8)+"-"+id.substring(8,12)+"-"
                +id.substring(12,16)+"-"+id.substring(16,20)+"-"
                +id.substring(20);
        }
        return UUID.fromString(id);
    }


	public static Object getIdForBean(Object entity){
	    if (!entity.getClass().isAnnotationPresent(Entity.class)) {
	        return null;
	    }
	    try {
	        Object id = EntityFactory.getId(entity);
	        if (id != null) return id;
	    }
	    catch (Exception ex) {
	        Logger.trace("Unable to fetch ID for "+entity, ex);
	    }
	    return null;
	}

	public static Field getIdFieldForBean(Object entity){
	    if (!entity.getClass().isAnnotationPresent(Entity.class)) {
	        return null;
	    }
	    try {
	        for (Field f : entity.getClass().getFields()) {
	            if (f.getAnnotation(Id.class) != null) {
	                return f;
	            }
	        }
	    } catch (Exception ex) {
	        Logger.trace("Unable to update index for "+entity, ex);
	    }
	    return null;
	}

	public static Method getIdSettingMethodForBean(Object entity){
	    Field f=getIdFieldForBean(entity);
	    for(Method m:entity.getClass().getMethods()){
	        if(m.getName().toLowerCase().equals("set" + f.getName().toLowerCase())){
	            return m;
	        }
	    }
	    return null;
	}

	public static String getIdForBeanAsString(Object entity){
	    Object id=getIdForBean(entity);
	    if(id!=null)return id.toString();
	    return null;
	}
	
	public static String getVersionForBeanAsString(Object entity){
		try{
		    Object version=getVersion(entity);
		    if(version!=null)return version.toString();
		}catch(Exception e){
			Logger.warn(e.getMessage());
		}
	    return null;
	}

    public static abstract class EntityFilter {
        public abstract boolean accept (Object sub);
        public List filter(List results) {
			List filteredSubstances = new ArrayList<Substance>();
			for (Object sub : results) {
				if (accept(sub)) {
					filteredSubstances.add(sub);
				}
			}
			return filteredSubstances;
		}
    }
}
