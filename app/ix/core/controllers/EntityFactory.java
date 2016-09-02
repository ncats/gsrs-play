package ix.core.controllers;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.Stack;
import java.util.TreeSet;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.avaje.ebean.Expr;
import com.avaje.ebean.FutureRowCount;
import com.avaje.ebean.Query;
import com.avaje.ebean.annotation.Transactional;
import com.avaje.ebean.bean.EntityBean;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.deser.DeserializationProblemHandler;
import com.fasterxml.jackson.databind.node.ObjectNode;

import ix.core.DefaultValidator;
import ix.core.ExceptionValidationMessage;
import ix.core.IgnoredModel;
import ix.core.SingleParent;
import ix.core.ValidationResponse;
import ix.core.Validator;
import ix.core.adapters.EntityPersistAdapter;
import ix.core.adapters.InxightTransaction;
import ix.core.models.BeanViews;
import ix.core.models.ETag;
import ix.core.models.Edit;
import ix.core.models.ForceUpdatableModel;
import ix.core.models.Principal;
import ix.core.plugins.TextIndexerPlugin;
import ix.core.search.text.EntityUtils;
import ix.core.search.text.EntityUtils.EntityInfo;
import ix.core.search.text.EntityUtils.EntityWrapper;
import ix.core.search.text.TextIndexer;
import ix.core.util.Java8Util;
import ix.utils.Global;
import ix.utils.Util;
import ix.utils.pojopatch.ChangeEventListener;
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


    static Model.Finder<Long, Principal> _principalFinder;

    static TextIndexerPlugin textIndexerPlugin;
    
    static{
    	init();
    }
    
    public static void init(){
        textIndexerPlugin=Play.application().plugin(TextIndexerPlugin.class);
    	_principalFinder=new Model.Finder(Long.class, Principal.class);
    }

    static TextIndexer getTextIndexer(){
        return textIndexerPlugin.getIndexer();
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
        public static EntityMapper INTERNAL_ENTITY_MAPPER(){
        	return new EntityMapper(BeanViews.Internal.class);
        }

		public static EntityMapper COMPACT_ENTITY_MAPPER() {
			return new EntityMapper(BeanViews.Compact.class);
		}
		
        public EntityMapper (Class<?>... views) {
            configure (MapperFeature.DEFAULT_VIEW_INCLUSION, true);
            configure (SerializationFeature.WRITE_NULL_MAP_VALUES, false);
            this.setSerializationInclusion(Include.NON_NULL);
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
            	ex.printStackTrace();
                Logger.trace("Can't write Json", ex);
            }
            return null;
        }
    }

    protected static class EditHistory implements PropertyChangeListener {
        List<Edit> edits = new ArrayList<Edit>();
        ObjectMapper mapper = getEntityMapper ();
        public Edit edit;
        
        
        
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
                
                String id = new EntityWrapper(e.getSource()).getIdAsString();
                if (id != null)
                    edit.refid = id;
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
                return Java8Util.ok (getTextIndexer().getDocJson(inst));
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
                return Java8Util.ok (mapper.valueToTree(inst));
            }else{
            	System.out.println("There's no one here by that name");
            }
        }
        else {
            T inst = finder.byId(id);
            if (inst != null) {
                return Java8Util.ok (mapper.valueToTree(inst));
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
    
    
    //Prime canididate for rewrite with EntityWrapper
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
                        ? ok (node.asText()) : Java8Util.ok (node);
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
        return Java8Util.ok (node);
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
        InxightTransaction tx = InxightTransaction.beginTransaction();
        
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
            tx.commit();
            Status s=created (mapper.toJson(inst));
            return s;
        }
        catch (Throwable ex) {
        	Logger.error("Problem creating record", ex);
        	System.out.println(ex.getMessage());
        	ex.printStackTrace();
        	tx.rollback(ex);
            return internalServerError ("Problem creating record:" + ex.getMessage());
        } finally{
        	tx.end();
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
            
			ValidationResponse<T> vr = validator.validate(inst,
					(T) getCurrentValue(inst).map(o -> o.getValue()).orElse(null));

            if(rept==RESPONSE_TYPE.FULL){
            	return ok(validationResponse(vr,true));
            }else{
            	return ok(validationResponse(vr,false));
            }
        } catch (Throwable ex) {
        	ValidationResponse vr = new ValidationResponse(null);
        	vr.setInvalid();
        	vr.addValidationMessage(new ExceptionValidationMessage(ex));
        	//should this be ok? Or internalServerError?
            return ok(validationResponse(vr,false));
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
            return Java8Util.ok (node);
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
            return Java8Util.ok (mapper.valueToTree(edits));
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
    @Deprecated
    protected static <K, T extends Model> Result update 
        (K id, String field, Class<T> type, Model.Finder<K, T> finder) {
                return update (id,field,type,finder, null, null);
    }
    
    // This expects an update of the full record to be done using "/path/*"
    // or "/path/_"
    //
    //
    // TODO: allow high-level changes to be captured
    // =======================
    // Update: This is no longer being used, due to the logic
    // never being determined via ebean. Need to disable it.
    //
    @Deprecated
    protected static <K, T extends Model> Result update 
        (K id, String field, Class<T> type, Model.Finder<K, T> finder,
         DeserializationProblemHandler deserializationHandler, Validator<T> validator) {
    	return forbidden ("Update field methods not available at this time");
    } // update ()
    
    public interface EntityCallable{
        void call(Object m, String path);
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
    	InxightTransaction tx = InxightTransaction.beginTransaction();
    	//EntityWrapper oldValuaeContainer = null;
    	EntityWrapper<?> og=null;
        try {       
            Object newValue = mapper.treeToValue(json, type);

            
            //Fetch old value
            og =  getCurrentValue(newValue).orElseThrow(()->new IllegalStateException("Cannot update a non-existing record"));
            
            String oldVersion = og.getVersion().orElse(null);
            String oldJSON = og.toJson(mapper).toString();
            
            
            //validate new value
            ValidationResponse vr=validator.validate(newValue,og.getValue());
	        if(!vr.isValid()){
	            return badRequest(validationResponse(vr,false));
	        }

            EditHistory eh = new EditHistory (json.toString());
            
            //this saves and everything
            eh.edit=EntityPersistAdapter.storeEditForPossibleUpdate(og.getValue());
            
            boolean usePojoPatch=true;
            if(!og.getClazz().equals(type)){
            	usePojoPatch=false;
            }
            
            if(usePojoPatch){
	            //Get the difference as a patch
	            PojoPatch patch =PojoDiff.getDiff(og.getValue(), newValue);
	            
	            
	            final List<Object> removed = new ArrayList<Object>();
	            //Apply the changes, grabbing every change along the way
	            Stack changeStack=patch.apply(og.getValue(),new ChangeEventListener(){
					@Override
					public void handleChange(ix.utils.pojopatch.Change c) {
						//System.out.println("Change IS:" + c);
						if("remove".equals(c.op)){
							removed.add(c.oldValue);
						}
					}
	            });
	            
	            
	        	while(!changeStack.isEmpty()){
	        		Object v=changeStack.pop();
	        		if(!v.getClass().isAnnotationPresent(IgnoredModel.class)){
		        		if(v instanceof ForceUpdatableModel){
		        			//System.out.println("Force update for:" + v);
		            		((ForceUpdatableModel)v).forceUpdate();
		            	}else if(v instanceof Model){
		            		//System.out.println("Regular update for:" + v);
		            		((Model)v).update();
		            	}else{
		            		//System.out.println("Nothing to do for:" + v);
		            	}
	        		}
	        	}

	        	//explicitly delete deleted things
	        	
	        	//This should ONLY delete objects which "belong"
	        	//to something. That is, have a @SingleParent annotation
	        	//inside
	        	for(Object toDelete : removed){
	        		if(toDelete !=null){
		        		if(!toDelete.getClass().isAnnotationPresent(IgnoredModel.class) &&
		        			toDelete.getClass().isAnnotationPresent(SingleParent.class)){
		        			if(toDelete instanceof Model){
		        				System.out.println("deleting:" + ((Model)toDelete));
		        				((Model)toDelete).delete();
		        			}
		            	}
	        		}
	        	}
	        	
	        	
	        	//The old value is now the new value
	        	newValue = og.getValue();
	        	
            }else{
            	Model m=(Model)og.getValue();
            	m.delete();
            	// Now need to take care of bad update pieces:
            	//	1. Version not incremented correctly (post update hooks not called) 
            	//  2. Some metadata / audit data may be problematic

            	EntityPersistAdapter.getInstance().preUpdateBeanDirect(newValue);
            	((Model)newValue).save();
            	EntityPersistAdapter.getInstance().postUpdateBeanDirect(newValue, m);
            	
            }
        	EntityWrapper newEnt = new EntityWrapper(newValue);
            String newJSON=mapper.toJson(newValue);
            
            
            //Should this be here?
            //EntityPersistAdapter.popEditForUpdate(previousValContainer.getValueClass(), previousValContainer.value);
            
            tx.commit();
            
            //granular parts not working yet
            if (newValue != null) {
	                eh.edit.refid = newEnt.getIdAsString();
	                eh.edit.kind = newEnt.getKind();
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
            
            // This was added because there are times
            // when the parent entity isn't actually
            // updated at all, at least from the ebean perspective
            // so this forces the issue
            
            EntityPersistAdapter.getInstance().deepreindex(newValue);
            
            return Java8Util.ok (mapper.valueToTree(newValue));
        }catch (Exception ex) {
        	Logger.error("Error updating record", ex);
            System.out.println(ex.getMessage());
            ex.printStackTrace();
            
            //Ebean.rollbackTransaction();
            return internalServerError (ex.getMessage());
        }finally {
        	if(og!=null){
        		EntityInfo ei =EntityUtils.getEntityInfoFor(og.getValue());
        		EntityPersistAdapter.popEditForUpdate(ei.getFieldAndId(og.getValue()));
        	}
            tx.end();
        }
    }

    
    // This could become a nice method. It isn't now ... 
    // It's unclear if you really need to attempt to
    // grab every extending class or not.
    //
    //
    private static Optional<EntityWrapper> getCurrentValue(Object value){
    	EntityWrapper thisvalue = EntityWrapper.of(value);
    	
        if (!thisvalue.isEntity())
            throw new IllegalArgumentException("Class "+thisvalue.getKind()+" is not an entity");
        if (!thisvalue.hasIdField())
            throw new IllegalArgumentException("Class "+thisvalue.getKind()+" is not an entity");
        
        LinkedHashSet<EntityInfo> possibleClasses = new LinkedHashSet<EntityInfo>();
        possibleClasses.add(thisvalue.getEntityInfo());
        possibleClasses.addAll(thisvalue.getEntityInfo().getAllEquivalentEntityInfos());
        
        Object id = thisvalue.getId().get();
        Object xval = null;
        //This may not be necessary after all?
        for(EntityInfo ee:possibleClasses){
	        if (id != null) {
	        	xval = ee.getFinder().byId(id);
	        }else{
	            // if this entity has no id set, then we see if there is a
	            // unique column defined.. if so, we retrieve it
	        	xval=ee.getUniqueColumns().stream().map(c->{
	        		return c.getValue(value).flatMap(v->{
	        			Object dbv=ee.getFinder().where().eq(c.getColumnName(),v)
                    			.findUnique();
	        			return Optional.ofNullable(dbv);
	        		});
	        	}).filter(Optional::isPresent).findAny();
	        	if(xval!=null)break;
	        }
        }
        return Optional.ofNullable(EntityWrapper.of(xval));
    }
    
}
