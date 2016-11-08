package ix.core.controllers;

import static ix.core.search.ArgumentAdapter.doNothing;
import static ix.core.search.ArgumentAdapter.ofInteger;
import static ix.core.search.ArgumentAdapter.ofList;
import static ix.core.search.ArgumentAdapter.ofSingleString;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Stack;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.avaje.ebean.Expr;
import com.avaje.ebean.Expression;
import com.avaje.ebean.FutureRowCount;
import com.avaje.ebean.Query;
import com.avaje.ebean.annotation.Transactional;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.deser.DeserializationProblemHandler;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.fge.jsonpatch.JsonPatch;

import ix.core.CacheStrategy;
import ix.core.DefaultValidator;
import ix.core.ExceptionValidationMessage;
import ix.core.ValidationResponse;
import ix.core.Validator;
import ix.core.adapters.EntityPersistAdapter;
import ix.core.adapters.InxightTransaction;
import ix.core.controllers.v1.RouteFactory;
import ix.core.models.BeanViews;
import ix.core.models.ETag;
import ix.core.models.Edit;
import ix.core.plugins.IxCache;
import ix.core.plugins.TextIndexerPlugin;
import ix.core.search.ArgumentAdapter;
import ix.core.search.text.TextIndexer;
import ix.core.util.CachedSupplier;
import ix.core.util.EntityUtils.EntityWrapper;
import ix.core.util.EntityUtils.Key;
import ix.core.util.Java8Util;
import ix.core.util.pojopointer.PojoPointer;
import ix.utils.Util;
import ix.utils.pojopatch.PojoDiff;
import ix.utils.pojopatch.PojoPatch;
import play.Logger;
import play.Play;
import play.db.ebean.Model;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;

public class EntityFactory extends Controller {
    private static final String RESPONSE_TYPE_PARAMETER = "type";

    static CachedSupplier<TextIndexerPlugin> textIndexerPlugin = CachedSupplier.of(()->{
    	return Play.application().plugin(TextIndexerPlugin.class);
    });
    
    static TextIndexer getTextIndexer(){
        return textIndexerPlugin.get().getIndexer();
    }

    
    
    public static class FetchOptions implements RequestOptions{
        public int top=10;
        public int skip=0;
        public String filter;
        public List<String> expand = new ArrayList<String>();
        public List<String> order = new ArrayList<String>();
        public List<String> select = new ArrayList<String>();
        
        private CachedSupplier<Map<String, ArgumentAdapter>> argumentAdapters = CachedSupplier.of(()->{
         	return Stream.of(
    		     	ofList("order", a->order=a, ()->order),
    		     	ofList("expand", a->expand=a, ()->expand),
    		     	ofList("select", a->select=a, ()->select),
    		     	ofInteger("top", a->top=a, ()->top),
    		     	ofInteger("skip", a->skip=a, ()->skip),
    		     	ofSingleString("filter", a->filter=a, ()->filter)
         	)
         	.collect(Collectors.toMap(a->a.name(), a->a));
         });

        
        // only in the context of a request
        public FetchOptions () {
        	Map<String,String[]> vals= new HashMap<>();
        	
        	//TODO: Use explicit check, rather than exception handling
        	try{ 
        		vals=request().queryString();
        	}catch(Exception e){
        		//e.printStackTrace();
        	}
        	vals.forEach((param,value)->{
        		argumentAdapters.get()
        			.getOrDefault(param,doNothing())
        			.accept(value);
        	});
        }
        
        public FetchOptions (int top, int skip, String filter) {
        	this();
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
        public <T> Query<T> applyToQuery(Query<T> q){
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

		@Override
		public int getTop() {
			return top;
		}

		@Override
		public int getSkip() {
			return skip;
		}
		@Override
		public String getFilter() {
			return filter;
		}
    }

    protected static <K,T> List<T> all (Model.Finder<K, T> finder) {
        return finder.all();
    }


    static public class EntityMapper extends ObjectMapper {
        /**
		 * Default value
		 */
		private static final long serialVersionUID = 1L;

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
            for (Class<?> v : views) {
                _serializationConfig = _serializationConfig.withView(v);
            }
            addHandler ();
        }

        void addHandler () {
            addHandler (new DeserializationProblemHandler () {
                    public boolean handleUnknownProperty
                        (DeserializationContext ctx, 
                         JsonParser parser,
                         JsonDeserializer deser, 
                         Object bean, 
                         String property) {
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
    	return page(top,skip,filter, finder, l->l);
    }
    
    

    protected static <K,T> Result page (int top, int skip, String filter,
                                        final Model.Finder<K, T> finder, 
                                        Function<List<T>, Object> streamop) {

        //if (select != null) finder.select(select);
        final FetchOptions options = new FetchOptions (top, skip, filter);
        List<T> results = filter (options, finder);
        
        Object returnObj = streamop.apply(results);
        
        final ETag etag = new ETag.Builder()
        		.fromRequest(request())
				.options(options)
				.count(results.size())
				.sha1OfRequest("filter")
				.build();
        

        if (options.filter == null){
            etag.total = finder.findRowCount();
        }else if(etag.count<etag.top){ //if count returned is less than top,
        								   //it's done
        	etag.total = etag.skip + etag.count;
        }else{
        	EntityWrapper.of(etag)
			.getFinder()
			.where()
			.eq("sha1", etag.sha1)
	        .orderBy("modified desc")
	        .setMaxRows(1)
	        .findList()
	        .stream().findFirst()
	        .ifPresent(e->{
	        	Logger.debug(">> cached "+etag.sha1+" from ETag "+e.etag);
	        	etag.total = e.total;
	        });
        	
        	
        }
        
        try{
            etag.save();
        }catch (Exception e) {
            Logger.error
                ("Error saving etag. This sometimes happens on empty DB");
        }

        etag.setContent(returnObj);
        
        ObjectMapper mapper = getEntityMapper ();
        ObjectNode obj = (ObjectNode)mapper.valueToTree(etag);

        return ok (obj);
    }

    /**
	 * @deprecated Use {@link Util#canonicalizeQuery(Http.Request)} instead
	 */
	static String canonicalizeQuery (Http.Request req) {
		return Util.canonicalizeQuery(req);
	}

    
    
    static public EntityMapper getEntityMapper () {
        List<Class<?>> views = new ArrayList<Class<?>>();

        Map<String, String[]> params = request().queryString();
        String[] args = params.get("view");
        if (args != null) {
        	
            Class<?>[] classes = BeanViews.class.getClasses();
            for (String a : args) {
                int matches = 0;
                for (Class<?> c : classes) {
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
        }else {
            results = finder.where()
                .allEq(cons)
                //.orderBy("id asc")
                .setFirstRow(skip)
                .setMaxRows(top)
                .findList();
        }
        return results;
    }

    protected static <K,T> Result stream (String field, int top, int skip , Model.Finder<K, T> finder) {
    	PojoPointer pojoPoint = PojoPointer.fromURIPath(field);
        return page (top,skip,null, finder, l->{
        	return EntityWrapper.of(l)
        						.at(pojoPoint)
        						.get()
        						.getValue();
        });
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

    protected static <K,T> Result field (K id, String field, 
                                         Model.Finder<K, T> finder) {
            Logger.debug("id: "+id+" field: "+field);
            T inst = finder.byId(id);
            //query.setId(id).findUnique();
            if (inst == null) {
                throw new IllegalArgumentException("Bad request: "+request().uri());
            }
            return field (inst, field);
    }
    
    public static class ErrorResponse{
    	public int status;
    	public String message;
    	public ErrorResponse(int status, String message){
    		this.status=status;
    		this.message=message;
    	}
    }
    
    
    //Prime canididate for rewrite with EntityWrapper
    protected static Result field (Object inst, String field) {
    	//return fieldOld(inst,field);
    	try{
	    	Object o = atFieldSerialized(inst,PojoPointer.fromURIPath(field));
	    	if(o instanceof JsonNode){
	        	return ok((JsonNode)o);
	        }else{
	        	return ok(o.toString());
	        }
    	}catch(Exception e){
    		e.printStackTrace();
    		throw e;
    	}
    }
    
    
    protected static Object atFieldSerialized (Object inst, PojoPointer cpath) {
    	EntityWrapper ew=EntityWrapper.of(inst).at(cpath).get();
    	if(cpath.isLeafRaw()){
    		return ew.getRawValue();
    	}else{
    		return ew.toFullJsonNode();
    	}
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
                        }catch (IOException ex) {
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
		            ValidationResponse<T> vr=validator.validate(inst);
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
        	System.err.println(ex.getMessage());
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

    protected static <T extends Model> Result validate(JsonNode node, Class<T> type, Validator<T> validator,RESPONSE_TYPE rept) {
        
        try {

            ObjectMapper mapper = new ObjectMapper();
            //Why don't we use this in the other place?
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

            T inst = mapper.treeToValue(node, type);


            //gets the value (if exists)
            Optional<EntityWrapper<?>> oldValue = getCurrentValue(inst);

            ValidationResponse<T> vr;

            //If it's present
            if(oldValue.isPresent()){
                //Validate using it and the new value
                vr= validator.validate(inst,(T)oldValue.map(o -> o.getValue()).get());  
            }else{
                //Validate using only new value
                vr= validator.validate(inst,null);
            } 

            if(rept==RESPONSE_TYPE.FULL){
                return ok(prepareValidationResponse(vr,true));
            }else{
                return ok(prepareValidationResponse(vr,false));
            }
        } catch (Throwable ex) {
            ex.printStackTrace();
            ValidationResponse vr = new ValidationResponse(null);
            vr.setInvalid();
            vr.addValidationMessage(new ExceptionValidationMessage(ex));
            //should this be ok? Or internalServerError?
            return ok(prepareValidationResponse(vr,false));
        }
    }
    
    protected static <K, T extends Model> Result validate(Class<T> type,
                                                          Model.Finder<K, T> finder, Validator<T> validator) {
        if (!request().method().equalsIgnoreCase("POST")) {
            return badRequest("Only POST is accepted!");
        }
        
        String content = request().getHeader("Content-Type");
        if (content == null
            || (content.indexOf("application/json") < 0 && content
                .indexOf("text/json") < 0)) {
            return badRequest("Mime type \"" + content + "\" not supported!");
        }
        JsonNode node = request().body().asJson();
        
        return validate(node,type,validator,RESPONSE_TYPE.MESSAGES);
        
    }
    
    public static UUID toUUID (String id) {
        if (id.length() == 32) { // without -'s
            id = id.substring(0,8)+"-"+id.substring(8,12)+"-"
                +id.substring(12,16)+"-"+id.substring(16,20)+"-"
                +id.substring(20);
        }
        return UUID.fromString(id);
    }
    
    protected static <T> JsonNode validationResponse(ValidationResponse<T> vr){
    	return prepareValidationResponse(vr,false);
    }
    protected static <T> JsonNode prepareValidationResponse(ValidationResponse<T> vr, boolean full){
    	EntityMapper mapper = EntityMapper.COMPACT_ENTITY_MAPPER();
    	if(full){
    		mapper = EntityMapper.FULL_ENTITY_MAPPER();
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


    //And all expressions together
	//TODO: There has got to be a cleaner way
	/**
	 * @deprecated Use {@link Util#andAll(Expression...)} instead
	 */
	public static Expression andAll(Expression... e) {
		return Util.andAll(e);
	}

    //Or all expressions together
	//TODO: There has got to be a cleaner way
	/**
	 * @deprecated Use {@link Util#orAll(Expression...)} instead
	 */
	public static Expression orAll(Expression... e) {
		return Util.orAll(e);
	}
    
	protected static Result edits(Object id, Class<?>... cls) {
		List<Edit> edits = getEdits(id,cls);
		
		if (!edits.isEmpty()) {
			ObjectMapper mapper = getEntityMapper();
			return Java8Util.ok(mapper.valueToTree(edits));
		}

		return notFound(request().uri() + ": No edit history found!");
	}
	
	public static List<Edit> getEdits(Object id, Class<?>... cls) {
		List<Edit> edits = new ArrayList<Edit>();
		FetchOptions fe = new FetchOptions();
		
		Expression[] kindExpressions = Arrays.stream(cls)
				.map(c -> Expr.eq("kind", c.getName()))
				.toArray(i->new Expression[i]);

		Query<Edit> q = EditFactory.finder
				.where(Util.andAll(
						Expr.eq("refid", id.toString()),
						Util.orAll(kindExpressions)
						));
		
		fe.applyToQuery(q);
		
		
		List<Edit> tmpedits = q.findList();
		if (tmpedits != null) {
			edits.addAll(tmpedits);
		}
		return edits;
	}
	
	
    
    /**
     * Handle generic update to field, without special deserializationHandler
     * 
     * @param id
     * @param field
     * @param type
     * @param finder
     * @return
     * @throws Exception 
     */
    @Deprecated
    protected static <K, T extends Model> Result update 
        (K id, String field, Class<T> type, Model.Finder<K, T> finder) {
        try{
            return update (id,field,type,finder, null, null);
        }catch(Exception e){
            return RouteFactory._apiBadRequest(e);
        }
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
    protected static <K, T extends Model, V extends T> Result update 
        (K id, String field, Class<V> type, Model.Finder<K, T> finder,
         DeserializationProblemHandler deserializationHandler, Validator<T> validator) throws Exception {
    	
        PojoPointer pp = PojoPointer.fromURIPath(field);
        JsonNode json = request().body().asJson();
        Key k = Key.of(type, id);
        
        return updateField(k,pp,json,validator);
    }
    
    
    
    
    
    /**
     * Update to a field (PUT) is a special form of a PATCH, which is
     * just a REPLACE operation at a specified PATH.
     * @param k
     * @param path
     * @param replace
     * @param validator
     * @return
     * @throws Exception
     */
    @Deprecated
    protected static <T extends Model,V> Result updateField 
        (Key k, PojoPointer path, JsonNode replace, Validator<T> validator) throws Exception {
    	
        PatchChanges changes = new PatchChanges()
                                                  .replace(path, replace);
        
        return patch(k,changes,validator);
    }
    
   // public static Map<String,StagedChange> stagedChanges= new ConcurrentHashMap<>();
    
    @CacheStrategy(evictable=false)
    public static class StagedChange{
        private static final long serialVersionUID = 1L;
        
        private String key;
        private String version;
        private PatchChanges changes;
        
        public StagedChange(String version, PatchChanges changes){
            this.changes=changes;
            this.version=version;
            this.key = UUID.randomUUID()
                           .toString();
        }
        
        
    }
    
    public static class UpdateResponse<T>{
        public static enum COMMIT_TYPE{
            SUBMITTED,
            STAGED,
            REJECTED
        }

        private COMMIT_TYPE commitType;
        private ValidationResponse<T> validationResponse;
        private String stagedKey;
        private Key key;
        
        public COMMIT_TYPE getCommitType() {
            return commitType;
        }
        public ValidationResponse<T> getValidationResponse() {
            return validationResponse;
        }
        
        public String getStagedKey() {
            return stagedKey;
        }
        private boolean isStaged(){
            return (commitType == COMMIT_TYPE.STAGED);
        }
        
        public String getCommitURL() {
            if(isStaged()){
                return key.asResourcePath() + "?key=" + stagedKey;
            }else{
                return null;
            }
        }
        
        public String getCommitMethod() {
            if(isStaged()){
                return "PATCH";
            }else{
                return null;
            }
        }
        public UpdateResponse(COMMIT_TYPE type, ValidationResponse<T> resp, String stagedKey, Key key){
            this.commitType=type;
            this.validationResponse=resp;
            this.stagedKey=stagedKey;
            this.key=key;
        }
    }
    
    protected static <T extends Model> Result patch(Key k, Validator<T> validator) throws Exception {
        if (!request().method().equalsIgnoreCase("PATCH")) {
            return badRequest ("Only PATCH is accepted!");
        }
        String key = request().getQueryString("key");
        if(key!=null){
            StagedChange staged= (StagedChange) IxCache.get(key);
            if(staged!=null){
                return patch(k,staged.changes, validator, staged.version, true, false);
            }else{
                throw new IllegalStateException("Key\""+key+"\" not found as a staged commit!");
            }
        }else{
            String content = request().getHeader("Content-Type");
            if (content == null || (content.indexOf("application/json") < 0
                                    && content.indexOf("text/json") < 0)) {
                throw new IllegalStateException("Mime type \""+content+"\" not supported!");
            }
            JsonNode json = request().body().asJson();
            ObjectMapper om = new ObjectMapper();
            PatchChanges changes=om.treeToValue(json, PatchChanges.class);
            return patch(k, changes, validator);
        }
    }
    
    protected static <T extends Model,V> Result patch
    (Key k, PatchChanges changes, Validator<T> validator, String version, boolean force, boolean validate) throws Exception {
        
        EntityWrapper<T> oldWrapped    = (EntityWrapper<T>)k.fetch().get();
        EntityWrapper<T> clonedWrapped =  EntityWrapper.of(oldWrapped.getClone());
        
        
        clonedWrapped.getEntityInfo()
                     .getVersionField()
                     .get();
        
        JsonPatch patch = changes.asJsonPatch(clonedWrapped);
        
        JsonNode clonedJson =clonedWrapped.toFullJsonNode();
        clonedWrapped.getEntityInfo()
            .getVersionField()
            .ifPresent(f->{
                if(!validate){
                    ((ObjectNode)clonedJson).put(f.getJsonFieldName(),version);
                }
            });
        
        
        JsonNode changedJson = patch.apply(clonedJson);
        if(!validate){
            StagedChange stagedChange = new StagedChange(version, changes);
            
            //Forces all problems to be errors by default
            ValidationResponse<T> resp = updateEntityValidated(changedJson,clonedWrapped.getEntityClass(),(t1,t2)->{
                ValidationResponse<T> vr=validator.validate(t1, t2);
                if(!force && vr.hasProblem()){
                    vr.setInvalid();
                }
                return vr;
            });
            
            UpdateResponse<T> upResp;
            
            if(resp.isValid()){
                upResp = new UpdateResponse<T>(UpdateResponse.COMMIT_TYPE.SUBMITTED, resp, null,k);
            }else{
                if(resp.hasError()){
                    upResp = new UpdateResponse<T>(UpdateResponse.COMMIT_TYPE.REJECTED, resp, null,k);
                }else{
                    IxCache.set(stagedChange.key, stagedChange);
                    upResp = new UpdateResponse<T>(UpdateResponse.COMMIT_TYPE.STAGED, resp, stagedChange.key,k);
                }
            }
            EntityMapper em = getEntityMapper();
            
            return Java8Util.ok(em.valueToTree(upResp));
        }else{
            return validate(changedJson,clonedWrapped.getEntityClass(),validator,RESPONSE_TYPE.MESSAGES);
        }
    }
    
    protected static <T extends Model,V> Result patch
        (Key k, PatchChanges changes, Validator<T> validator) throws Exception {
        
        String version = request().getQueryString("version");
        boolean validate = Optional
                                .ofNullable(request().getQueryString("validate"))
                                .isPresent();
        
        
        return patch(k, changes, validator, version, false, validate);
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
    
    
    protected static <T> Result updateEntity (JsonNode json, Class<T> type) {
        return updateEntity (json, type, new DefaultValidator<T>());
    }
    
    
    //Typically mutates, but doesn't sometimes 
    //This is not ideal. 
    //Also, the generic "T" here really doesn't do anything
    //as the contract has no enforcement.
    //
    public static <T> EntityWrapper<T> calculateAndApplyDiff(EntityWrapper<? extends T> oWrap, EntityWrapper<? extends T> nWrap) throws Exception{
    	 boolean usePojoPatch=false;
         if(oWrap.getEntityClass().equals(nWrap.getEntityClass())){ //only use POJO patch if the entities are the same type
         	usePojoPatch=true;
         }
         
         if(usePojoPatch){
         	doPojoPatch(oWrap,nWrap); //saves too!
         	return (EntityWrapper<T>)oWrap; //Mutated
         }else{
         	
         	Model oldValue=(Model)oWrap.getValue();
         	oldValue.delete();
         	
         	// Now need to take care of bad update pieces:
         	//	1. Version not incremented correctly (post update hooks not called) 
         	//  2. Some metadata / audit data may be problematic
         	//  3. The update hooks are called explicitly now
         	//     ... and that's a weird thing to do, because the persist hooks
         	//     will get called too. Does someone really expect things to
         	//     get called twice?
         	
         	Model newValue = (Model)nWrap.getValue();
         	EntityPersistAdapter.getInstance().preUpdateBeanDirect(newValue);
         	newValue.save(); 
         	EntityPersistAdapter.getInstance().postUpdateBeanDirect(newValue, oldValue);
         	
         	//This doesn't morph the object, so we're in trouble
         	return (EntityWrapper<T>)nWrap; //Delete & Create
         }
    }
    
    public static void doPojoPatch(EntityWrapper oldValue, EntityWrapper newValue) throws Exception{
    	
    	Object rawOld = oldValue.getValue();
    	Object rawNew = newValue.getValue();
    	
    	//Get the difference as a patch
        PojoPatch patch =PojoDiff.getDiff(rawOld, rawNew);
        
        
        final List<Object> removed = new ArrayList<Object>();
        
        //Apply the changes, grabbing every change along the way
        Stack changeStack=patch.apply(rawOld,c->{
				if("remove".equals(c.op)){
					removed.add(c.oldValue);
				}
        });
        if(changeStack.isEmpty()){
        	throw new IllegalStateException("No change detected");
        };
        
    	while(!changeStack.isEmpty()){
    		Object v=changeStack.pop();
    		EntityWrapper ewchanged=EntityWrapper.of(v);
    		if(!ewchanged.isIgnoredModel()){
    			ewchanged.update();
    		}
    	}

    	//explicitly delete deleted things
    	//This should ONLY delete objects which "belong"
    	//to something. That is, have a @SingleParent annotation
    	//inside
    	
    	removed.stream()
    		   .filter(Objects::nonNull)
    		   .map(o->EntityWrapper.of(o))
    		   .filter(ew->ew.isExplicitDeletable())
    		   .forEach(ew->{
    			   Logger.warn("deleting:" + ((Model)ew.getValue()));
    			   ew.delete();
    		   });
    }
    
    
    /**
     * Performs the actual update of a record, returning a {@link ValidationResponse}
     * which explains what went wrong (if something went wrong), and contains the 
     * new updated form of the object committed, or the object for which the commit
     * was attempted but failed. If the returned object has 
     * {@link ValidationResponse#isValid()}, then it was committed. 
     * @param json
     * @param type
     * @param validator
     * @return
     * @throws Exception
     */
    protected static <T> ValidationResponse<T> updateEntityValidated(JsonNode json, Class<? extends T> type, Validator<T> validator ) throws Exception{
        EntityMapper mapper = EntityMapper.FULL_ENTITY_MAPPER();  

        //Get NEW object from JSON
        Object newValue = mapper.treeToValue(json, type);

        //Fetch old value
        EntityWrapper<?> eg =  getCurrentValue(newValue)
                .orElseThrow(()->new IllegalStateException("Cannot update a non-existing record"));

        //validation response holder
        //TODO: This is a side-effect, which is not desired
        //in general.
        final List<ValidationResponse<T>> vrlist = new ArrayList<>();


        //Do we still need this???
        //EditHistory eh = new EditHistory (json.toString());

        EntityWrapper<T> savedVersion = EntityPersistAdapter.performChange(eg.getKey(),ov->{
            EntityWrapper<T> og= EntityWrapper.of((T)ov);

            ValidationResponse<T> vrr=validator.validate((T)newValue,(T)og.getValue());
            vrlist.add(vrr);
            if(!vrr.isValid()){
                return Optional.empty();
            }
            
            EntityWrapper entityThatWasSaved=null;

            InxightTransaction tx = InxightTransaction.beginTransaction();
            try{
                entityThatWasSaved=calculateAndApplyDiff(og,EntityWrapper.of(newValue)); //saving happens here
                tx.commit();

                // This was added because there are times
                // when the parent entity isn't actually
                // updated at all, at least from the ebean perspective
                // so this forces the reindexing, at least

                EntityPersistAdapter.getInstance().deepreindex(entityThatWasSaved);
            }catch(Exception e){
                Logger.error("Error updating entity", e);
            }finally{
                tx.end();
            }
            
            return Optional.ofNullable(entityThatWasSaved);
        });


        if(vrlist.isEmpty()){
            throw new IllegalStateException("Validation Response could not be generated");
        }
        
        ValidationResponse<T> vrresp = vrlist.get(0);
        if(vrresp.isValid() && savedVersion!=null){
            vrresp.setNewObject(savedVersion.getValue());
        }
        if(vrresp.getNewObect()==null){
            vrresp.setInvalid();
        }
        return vrresp;
    }

    
    /*
     * Ok, at the most fundamental level, assuming all changes come only through this method,
     * then we just need to store the old JSON, and the new.
     * 
     */
    protected static <T> Result updateEntity (JsonNode json, Class<? extends T> type, Validator<T> validator ) {
        EntityMapper mapper = EntityMapper.FULL_ENTITY_MAPPER();
        try {       
            ValidationResponse<T> response = updateEntityValidated(json,type,validator);
            if(!response.isValid()){
                //TODO: Should this be OK ... probably not
                return ok(prepareValidationResponse(response,false));
            }
            return Java8Util.ok (mapper.valueToTree(response.getNewObect()));
        }catch (Exception ex) {
        	Logger.error("Error updating record", ex);
            ex.printStackTrace();
            return RouteFactory._apiBadRequest("Error updating record");
        }
    }
    
    

    
    
    
    // This could become a nice method.
    //TODO: move to EntityWrapper
    private static Optional<EntityWrapper<?>> getCurrentValue(Object value){
    	if(EntityWrapper.of(value).hasKey()){
    		return EntityWrapper.of(value).getKey().fetch();
    	}else{
    		return Optional.empty();
    	}
    }
    
}
