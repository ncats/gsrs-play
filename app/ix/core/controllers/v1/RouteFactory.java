package ix.core.controllers.v1;

import static ix.core.search.ArgumentAdapter.getLastBooleanOrElse;
import static ix.core.search.ArgumentAdapter.getLastDoubleOrElse;
import static ix.core.search.ArgumentAdapter.getLastIntegerOrElse;
import static ix.core.search.ArgumentAdapter.getLastStringOrElse;

import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

import gov.nih.ncats.chemkit.api.Chemical;
import ix.core.chem.StructureProcessorTask;
import ix.core.controllers.search.SearchRequest;
import org.reflections.Reflections;

import com.avaje.ebean.Expr;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import be.objectify.deadbolt.java.actions.Dynamic;
import ix.core.Experimental;
import ix.core.NamedResource;
import ix.core.NamedResourceFilter;
import ix.core.UserFetcher;
import ix.core.controllers.EntityFactory;
import ix.core.controllers.EntityFactory.EntityMapper;
import ix.core.controllers.SequenceFactory;
import ix.core.controllers.StructureFactory;
import ix.core.controllers.search.SearchFactory;
import ix.core.models.Acl;
import ix.core.models.Namespace;
import ix.core.models.Principal;
import ix.core.models.Role;
import ix.core.models.Structure;
import ix.core.models.UserProfile;
import ix.core.plugins.IxContext;
import ix.core.plugins.TextIndexerPlugin;
import ix.core.search.SearchOptions;
import ix.core.search.text.FacetMeta;
import ix.core.search.text.TextIndexer.TermVectors;
import ix.core.util.CachedSupplier;
import ix.core.util.EntityUtils;
import ix.core.util.EntityUtils.EntityInfo;
import ix.core.util.Java8Util;
import ix.ginas.models.v1.Subunit;  //It is not ideal to have ginas model imports
//but since there is no generic sequence support
                                    //outside of ginas, this is used until something
                                    //else exists
import ix.ncats.controllers.security.IxDynamicResourceHandler;
import ix.seqaln.SequenceIndexer.CutoffType;
import ix.utils.Global;
import ix.utils.Util;
import play.Application;
import play.Logger;
import play.Play;
import play.db.ebean.Model;
import play.mvc.*;
import tripod.molvec.Molvec;

public class RouteFactory extends Controller {
    private static final int MAX_POST_PAYLOAD = 1024*1024*10;
    static public CachedSupplier<Model.Finder<Long, Namespace>> resFinder =Util.finderFor(Long.class, Namespace.class);
    static public CachedSupplier<Model.Finder<Long, Acl>> aclFinder=Util.finderFor(Long.class, Acl.class);
    static public CachedSupplier<Model.Finder<Long, Principal>> palFinder=Util.finderFor(Long.class, Principal.class);

    public static class FactoryRegistry{
        private ConcurrentHashMap<String, Class<?>> registry=new ConcurrentHashMap<String, Class<?>>();
        private Set<String> _uuid = new TreeSet<String>();
        private ConcurrentHashMap<String, InstantiatedNamedResource> resources = new ConcurrentHashMap<>();
        private ConcurrentHashMap<String, String> resourceNamesForClasses = new ConcurrentHashMap<>();



        private NamedResourceFilter resourceFilter=new NamedResourceFilter(){
            @Override
            public boolean isVisible(Class<?> nr) {
                return true;
            }
            @Override
            public boolean isAccessible(Class<?> nr) {

                return true;
            }
        };


        public FactoryRegistry(Application app) {
            String resproc = app.configuration().getString("ix.core.resourcefilter", null);

            if (resproc != null) {
                Class<?> processorCls;
                try {
                    processorCls = Class.forName(resproc);
                    setResourceFilter((NamedResourceFilter) processorCls.newInstance());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            IxContext ctx= Global.getInstance().context();

            /**
             * default/global entities factory
             */
            Reflections reflections = new Reflections("ix");
            Set<Class<?>> resources = reflections.getTypesAnnotatedWith(NamedResource.class);

            Logger.info(resources.size() + " named resources...");

            for (Class c : resources) {
                NamedResource res = (NamedResource) c.getAnnotation(NamedResource.class);
                Logger.info("+ " + c.getName() + "\n  => " + ctx.context() + ctx.api() + "/" + res.name() + "["
                        + res.type().getName() + "]");
                
                
                register(res.name(), c);
            }
        }




        public Set<InstantiatedNamedResource> getInstantiatedNamedResources(){
            return resources
                    .values()
                    .stream()
                    .filter(nr -> resourceFilter.test(registry.get(nr.getName())))
                    .collect(Collectors.toSet());
        }


        public void setResourceFilter(NamedResourceFilter resourceFilter) {
            this.resourceFilter = resourceFilter;
        }

        private Class<?> put(String context, Class<?> register){
            return registry.put(context, register);
        }

        public <T extends EntityFactory, V, I> void register(String context, Class<T> factory) {
            Class<?> old = put(context, factory);
            if (old != null) {
                Logger.warn("Context \""+context
                        +"\" now maps to "+factory.getClass());
            }

            NamedResource named  = factory.getAnnotation(NamedResource.class);
            if (named != null) {
                try {
                    Class<V> type=named.type();
                    EntityInfo<V> ei = EntityUtils.getEntityInfoFor(type);

                    if (!ei.getIDFieldInfo().isPresent()) { // possible?
                        Logger.error("Fatal error: Entity "+ei.getName()
                        +" for factory "+factory.getClass()
                        +" doesn't have any Id annotation!");
                    }else {
                        Class<I> idType = (Class<I>) ei.getIdType();
                        InstantiatedNamedResource<I,V> resource=
                                InstantiatedNamedResource.of(factory, idType, type);
                        if (UUID.class.isAssignableFrom(idType)) {
                            Logger.debug("## "+ei.getName()
                            +" is globally unique!");
                            _uuid.add(context);
                        }
                        resources.put(resource.getName(), resource);
                        resourceNamesForClasses.put(resource.getKind(), resource.getName());
                    }
                }
                catch (Exception ex) {
                    ex.printStackTrace();
                    Logger.error("Can't access named resource type", ex);
                }
            }
        }

        @SuppressWarnings("unchecked")
        public <I,V> InstantiatedNamedResource<I,V> getResource(String context) throws NoSuchElementException{
            UserProfile up=UserFetcher.getActingUserProfile(true);
            List<Role> roles = Optional.ofNullable(up)
                                    .map(u->u.getRoles())
                                    .orElse(new ArrayList<Role>());
            

            return Optional.ofNullable(resources.get(context))
                    .filter(nr -> nr.isAccessible(roles))
                    .orElseThrow(() -> {
                        return new NoSuchElementException("Unknown resource '" + context + "'");
                    });
        }

        @SuppressWarnings("unchecked")
        public <I,V> InstantiatedNamedResource<I,V> getResourceFor(Class entityType){
            EntityInfo ei = EntityUtils.getEntityInfoFor(entityType).getInherittedRootEntityInfo();
            String resourceName= resourceNamesForClasses.get(ei.getName());
            if(resourceName==null) return null; //Not found
            return resources.get(resourceName);
        }

        Set<String> getUUIDcontexts() {
            return _uuid;
        }
    }

    public static CachedSupplier<FactoryRegistry> _registry = CachedSupplier.of(()->{
        return new FactoryRegistry(Play.application());
    });



    public static <T  extends EntityFactory, V> void register 
    (String context, Class<T> factory) {
        _registry.get().register(context, factory);
    }



    public static Result listResources () {
        EntityMapper em = EntityFactory.getEntityMapper();
        Set<InstantiatedNamedResource> resources = _registry
                .get()
                .getInstantiatedNamedResources();
        return ok((JsonNode)em.valueToTree(resources));
    }

    public static Result get (String ns, String resource) {
        Namespace res = resFinder.get()
                .where(Expr.eq("name", ns))
                .findUnique();
        if (res == null) {
            return _apiBadRequest ("No such namespace: "+ns);
        }

        // now see if this request has proper permission
        if (res.isPublic()) { // ok

        }
        else {
            return _apiForbidden ("You don't have permission to access resource!");
        }

        ObjectMapper mapper = new ObjectMapper ();
        return Java8Util.ok(mapper.valueToTree(res));
    }


    public static Result count (String context) {
        try {
            return _registry.get()
                    .getResource(context)
                    .count();
        }catch (Exception ex) {
            Logger.error("["+context+"]", ex);
            return _apiInternalServerError (ex);
        }
    }

    public static Result search (String context, String q, 
            int top, int skip, int fdim) {
        try {
            return  _registry.get()
                    .getResource(context)
                    .search(q, top, skip, fdim);
        }catch (Exception ex) {
            Logger.error("["+context+"]", ex);
            return _apiInternalServerError (ex);
        }
    }
    
    
    
    /**
     * <p>
     * Accepts an HTTP POST request with a sequence search payload, storing the
     * sequence payload, and redirecting to a GET request, using the stored ID
     * (UUID in this case) to make the search capable of fitting nicely in a
     * simple GET request, forwarding to the route found at
     * {@link #sequenceSearch(String, String, String, double, int, int, int, String)}
     * . The encoding of the parameters is done using
     * {@link BodyParser.FormUrlEncoded}, to be in line with the mechanism done
     * with GET requests.
     * 
     * </p>
     * 
     * <p>
     * For example, given the curl command:
     * </p>
     * 
     * <pre>
     * <code>
     * curl -L 'http://localhost:9000/ginas/app/api/v1/substances/sequenceSearch' --data 'cutoff=0.5&type=SUB&q=GGGCYFQNCPKG' --compressed
     * </code>
     * </pre>
     * 
     * <p>
     * Will redirect to the equivalent of:
     * 
     * <pre>
     * <code>
     * curl http://localhost:9000/ginas/app/api/v1/substances/sequenceSearch?q=<SOME_UUID>&type=SUB&cutoff=0.5
     * </code>
     * </pre>
     * <p>
     * 
     * @param context
     * @return
     */
    @BodyParser.Of(value = BodyParser.FormUrlEncoded.class, maxLength = 50_000)
    public static Result sequenceSearchPost(String context) {
        if (request().body().isMaxSizeExceeded()) {
            return badRequest("Sequence is too large!");
        }

        Map<String, String[]> params = request().body().asFormUrlEncoded();
        
        String q =      getLastStringOrElse(params.get("q"), null);
        String type =   getLastStringOrElse(params.get("type"), "SUB");
        Double co =     getLastDoubleOrElse(params.get("cutoff"), 0.8);
        Integer top =   getLastIntegerOrElse(params.get("top"), 10);
        Integer skip=   getLastIntegerOrElse(params.get("skip"), 0);
        Integer fdim =  getLastIntegerOrElse(params.get("fdim"), 10);
        String field =  getLastStringOrElse(params.get("field"), "");
        String seqType = getLastStringOrElse(params.get("seqType"), "Protein");
        
        Subunit sub= SequenceFactory.getStructureFrom(q,true);

        try {
            
            Call call = ix.core.controllers
                    .v1.routes.RouteFactory
                    .sequenceSearch(context,
                                    sub.uuid.toString(),
                                    type,
                                    co,
                                    top,
                                    skip,
                                    fdim,
                                    field,
                            seqType);
            return redirect(call);
        } catch (Exception ex) {
            Logger.error("Sequence search call error", ex);
            return _apiInternalServerError(ex);
        }
    }



    @BodyParser.Of(value = BodyParser.Raw.class, maxLength = 500_000)
    public static Result ocrStructure(String contextIgnored){

//        if (request().body().isMaxSizeExceeded()) {
//            return _apiBadRequest("Structure is too large!");
//        }


        String raw =  new String(request().body().asRaw().asBytes());

        String base64Encoded = raw.substring(raw.indexOf(',')+1);
//        System.out.println("base64 =" +base64Encoded);
        byte[] data = Base64.getDecoder().decode(base64Encoded);
        String mol;
        CompletableFuture<String> chemicalCompletableFuture = Molvec.ocrAsync(data);

        try {
            mol = chemicalCompletableFuture.get(10, TimeUnit.SECONDS);

             System.out.println("parsed mol=\n" +mol);
            StructureProcessorTask.Builder taskBuilder = new StructureProcessorTask.Builder()
                    .mol(mol)
                    .query(true)
                    .standardize(false)
                    ;

            return ok(EntityMapper.FULL_ENTITY_MAPPER().toJson(taskBuilder.build().instrument()));
        }catch(TimeoutException toe){
            //timeout!!
            chemicalCompletableFuture.cancel(true);
            return Results.notFound();
        }catch(Exception e){
            return _apiInternalServerError(e);
        }




    }
    /**
     * <p>
     * Accepts an HTTP POST request with a structure search payload, storing the
     * structure payload, and redirecting to a GET request, using the stored ID
     * (UUID in this case) to make the search capable of fitting nicely in a
     * simple GET request, forwarding to the route found at
     * {@link #structureSearch(String, String, String, double, int, int, int, String)}
     * . The encoding of the parameters is done using
     * {@link BodyParser.FormUrlEncoded}, to be in line with the mechanism done
     * with GET requests.
     * 
     * </p>
     * 
     * <p>
     * For example, given the curl command:
     * </p>
     * 
     * <pre>
     * <code>
     * curl -L 'http://localhost:9000/ginas/app/api/v1/substances/structureSearch' --data 'type=Substructure&q=%0D%0A+++JSDraw210241620382D%0D%0A%0D%0A++6++6++0++0++0++0++++++++++++++0+V2000%0D%0A+++15.7560+++-6.5520++++0.0000+C+++0++0++0++0++0++0++0++0++0++0++0++0%0D%0A+++14.4050+++-5.7720++++0.0000+C+++0++0++0++0++0++0++0++0++0++0++0++0%0D%0A+++14.4050+++-4.2120++++0.0000+C+++0++0++0++0++0++0++0++0++0++0++0++0%0D%0A+++17.1070+++-5.7720++++0.0000+C+++0++0++0++0++0++0++0++0++0++0++0++0%0D%0A+++17.1070+++-4.2120++++0.0000+C+++0++0++0++0++0++0++0++0++0++0++0++0%0D%0A+++15.7560+++-3.4320++++0.0000+C+++0++0++0++0++0++0++0++0++0++0++0++0%0D%0A++1++2++2++0++0++0++0%0D%0A++2++3++1++0++0++0++0%0D%0A++1++4++1++0++0++0++0%0D%0A++4++5++2++0++0++0++0%0D%0A++5++6++1++0++0++0++0%0D%0A++6++3++2++0++0++0++0%0D%0AM++END%0D%0A' --compressed
     * </code>
     * </pre>
     * 
     * <p>
     * Will redirect to the equivalent of:
     * 
     * <pre>
     * <code>
     * curl http://localhost:9000/ginas/app/api/v1/substances/structureSearch?q=<SOME_UUID>&type=Substructure
     * </code>
     * </pre>
     * <p>
     * 
     * @param context
     * @return
     */
    @BodyParser.Of(value = BodyParser.FormUrlEncoded.class, maxLength = 50_000)
    public static Result structureSearchPost(String context) {

        if (request().body().isMaxSizeExceeded()) {
            return _apiBadRequest("Structure is too large!");
        }
        Map<String, String[]> params = request().body().asFormUrlEncoded();
        
        String q =      getLastStringOrElse(params.get("q"), null);
        String type =   getLastStringOrElse(params.get("type"), "sub");
        Double co =     getLastDoubleOrElse(params.get("cutoff"), 0.8);
        Integer top =   getLastIntegerOrElse(params.get("top"), 10);
        Integer skip=   getLastIntegerOrElse(params.get("skip"), 0);
        Integer fdim =  getLastIntegerOrElse(params.get("fdim"), 10);
        String field =  getLastStringOrElse(params.get("field"), "");
        
        boolean wait = getLastBooleanOrElse(params.get("wait"), false);

        Structure struc= StructureFactory.getStructureFrom(q,true);
        
        try {
            
            Call call = ix.core.controllers
                    .v1.routes.RouteFactory
                    .structureSearch(context,
                                    struc.id.toString(),
                                    type,
                                    co,
                                    top,
                                    skip,
                                    fdim,
                                    field);
            return redirect(call);
        } catch (Exception ex) {
            Logger.error("Structure search call error", ex);
            return _apiInternalServerError(ex);
        }
    }
    
    public static Result structureSearch(String context, 
            String q, 
            String type,
            double cutoff,
            int top, 
            int skip, 
            int fdim,
            String field) {
        try {
            Structure struc = StructureFactory.getStructureFrom(q, false);
            return _registry.get()
                    .getResource(context)
                    .structureSearch(struc.molfile, type, cutoff, top, skip, fdim, field);
        } catch (Exception ex) {
            Logger.error("[" + context + "]", ex);
            return _apiInternalServerError(ex);
        }
    }

    
    public static Result sequenceSearch(String context, 
            String q, 
            String type,
            double cutoff,
            int top, 
            int skip, 
            int fdim,
            String field, String seqType) {
        try {
            Subunit sub = SequenceFactory.getStructureFrom(q, false);
            return _registry.get()
                    .getResource(context)
                    .sequenceSearch(sub.sequence,  CutoffType.valueOfOrDefault(type), cutoff, top, skip, fdim, field, seqType);
        } catch (Exception ex) {
            Logger.error("[" + context + "]", ex);
            return _apiInternalServerError(ex);
        }
    }
    
    @Dynamic(value = IxDynamicResourceHandler.CAN_UPDATE, handler = ix.ncats.controllers.security.IxDeadboltHandler.class)
    public static Result editsFlex (String context, String someid) {
        try {
        	InstantiatedNamedResource nr = _registry.get()
                    .getResource(context);
        	Object id = nr.resolveID(someid).orElse(null);
        	
            return _registry.get()
                    .getResource(context)
                    .edits(id);
        }catch (Exception ex) {
            Logger.error("["+context+"]", ex);
            return _apiInternalServerError (ex);
        }
    }

    @Dynamic(value = IxDynamicResourceHandler.CAN_DELETE, handler = ix.ncats.controllers.security.IxDeadboltHandler.class)
    public static Result deleteFlex (String context, String someid) {
        try {
        	InstantiatedNamedResource nr = _registry.get()
                    .getResource(context);
        	Object id = nr.resolveID(someid).orElse(null);

            return _registry.get()
                    .getResource(context)
                    .delete(id);

        }catch (Exception ex) {
            Logger.trace("["+context+"]", ex);
            return _apiInternalServerError (ex);
        }
    }



    public static Result getFlex (String context, String someid, String expand) {
        try {
        	InstantiatedNamedResource nr = _registry.get()
                    .getResource(context);
        	
        	Object id = nr.resolveID(someid).orElse(null);        	
            return nr.get(id, expand);
        }catch (Exception ex) {
            Logger.error("["+context+"]", ex);
            return _apiInternalServerError (ex);
        }
    }
    
    public static Result fieldFlex (String context, String someid, String field) {
    	  try {
    		  InstantiatedNamedResource nr = _registry.get()
                      .getResource(context);
          	
          	  Object id = nr.resolveID(someid).orElse(null);        	
              return nr.field(id, field);
          }catch (Exception ex) {
              Logger.error("["+context+"]", ex);
              return _apiInternalServerError (ex);
          }
    }
    
    
    public static Result get (String context, Long id, String expand) {
        try {
            return _registry.get()
                    .getResource(context)
                    .get(id,expand);
        }catch (Exception ex) {
            Logger.error("["+context+"]", ex);
            return _apiInternalServerError (ex);
        }
    }

    public static Result doc (String context, Long id) {
        try {
            return _registry.get()
                    .getResource(context)
                    .doc(id);
        }catch (Exception ex) {
            Logger.error("["+context+"]", ex);
            return _apiInternalServerError (ex);
        }
    }

    @Dynamic(value = IxDynamicResourceHandler.CAN_SEARCH, handler = ix.ncats.controllers.security.IxDeadboltHandler.class)
    public static Result getUUID (String context, String uuid, String expand) {
        try {
            return _registry.get()
                    .getResource(context)
                    .get(EntityFactory.toUUID(uuid), expand);
        }catch (Exception ex) {
            Logger.trace("["+context+"]", ex);
            return _apiInternalServerError (ex);
        }
    }

    @Dynamic(value = IxDynamicResourceHandler.CAN_DELETE, handler = ix.ncats.controllers.security.IxDeadboltHandler.class)
    public static Result deleteUUID (String context, String uuid) {
        try {
            return _registry.get()
                    .getResource(context)
                    .delete(EntityFactory.toUUID(uuid));


        }catch (Exception ex) {
            Logger.trace("["+context+"]", ex);
            return _apiInternalServerError (ex);
        }
    }

    public static Result edits (String context, Long id) {
        try {
            return _registry.get()
                    .getResource(context)
                    .edits(id);
        }catch (Exception ex) {
            Logger.error("["+context+"]", ex);
            return _apiInternalServerError (ex);
        }
    }

    @Dynamic(value = IxDynamicResourceHandler.CAN_UPDATE, handler = ix.ncats.controllers.security.IxDeadboltHandler.class)
    public static Result editsUUID (String context, String id) {
        try {
            return _registry.get()
                    .getResource(context)
                    .edits(EntityFactory.toUUID(id));
        }catch (Exception ex) {
            Logger.error("["+context+"]", ex);
            return _apiInternalServerError (ex);
        }
    }

    @Dynamic(value = IxDynamicResourceHandler.CAN_APPROVE, handler = ix.ncats.controllers.security.IxDeadboltHandler.class)
    public static Result approveUUID (String context, String id) {
        try {
            return _registry.get()
                    .getResource(context)
                    .approve(EntityFactory.toUUID(id));
        }catch (Exception ex) {
            Logger.error("["+context+"]", ex);
            return _apiInternalServerError (ex);
        }
    }

    public static Result field (String context, Long id, String field) {
        try {
            return _registry.get()
                    .getResource(context)
                    .field(id, field);
        }catch (Exception ex) {
            Logger.error("["+context+"]", ex);
            return _apiInternalServerError (ex);
        }
    }

    public static Result fieldUUID (String context, String uuid, String field) {
        try {
            return _registry.get()
                    .getResource(context)
                    .field(EntityFactory.toUUID(uuid), field);
        }catch (Exception ex) {
            Logger.error("["+context+"]", ex);
            return _apiInternalServerError (ex);
        }
    }

    public static Result page (String context, int top,
            int skip, String filter) {
        try {
           // System.out.println("Setting topskip:" + top + ":" + skip);
            return _registry.get()
                    .getResource(context)
                    .page(top, skip, filter);
        }catch (Exception ex) {

            Logger.error("["+context+"]", ex);
            return _apiInternalServerError (ex);
        }
    }


    @Experimental
    public static Result stream(String context, String field, int top, int skip) {
        try {
            return _registry.get()
                    .getResource(context)
                    .stream(field, top, skip);
        }catch (Exception ex) {
            Logger.error("["+context+"]", ex);
            return _apiInternalServerError (ex);
        }
    }

    @Dynamic(value = IxDynamicResourceHandler.CAN_REGISTER, handler = ix.ncats.controllers.security.IxDeadboltHandler.class)
    @BodyParser.Of(value = BodyParser.Json.class, maxLength = MAX_POST_PAYLOAD)
    public static Result create (String context) {
        try {
            return _registry.get()
                    .getResource(context)
                    .create();
        }catch (Exception ex) {
            Logger.error("["+context+"]", ex);
            return _apiInternalServerError (ex);
        }
    }

    @Dynamic(value = IxDynamicResourceHandler.CAN_REGISTER, handler = ix.ncats.controllers.security.IxDeadboltHandler.class)
    @BodyParser.Of(value = BodyParser.Json.class, maxLength = MAX_POST_PAYLOAD)
    public static Result validate (String context) {
        try {
            return _registry.get()
                    .getResource(context)
                    .validate();
        }catch (Exception ex) {
            Logger.error("["+context+"]", ex);
            return _apiInternalServerError (ex);
        }
    }

    @Dynamic(value = IxDynamicResourceHandler.CAN_UPDATE, handler = ix.ncats.controllers.security.IxDeadboltHandler.class)
    @BodyParser.Of(value = BodyParser.Json.class, maxLength = MAX_POST_PAYLOAD)
    public static Result update (String context, Long id, String field) {
        try {
            return _registry.get()
                    .getResource(context)
                    .update(id, field);
        }catch (Exception ex) {
            Logger.error("["+context+"]", ex);
            return _apiInternalServerError (ex);
        }
    }

    @Dynamic(value = IxDynamicResourceHandler.CAN_UPDATE, handler = ix.ncats.controllers.security.IxDeadboltHandler.class)
    @BodyParser.Of(value = BodyParser.Json.class, maxLength = MAX_POST_PAYLOAD)
    public static Result updateEntity (String context) {
        try {
            return _registry.get()
                    .getResource(context)
                    .updateEntity();
        }catch (Exception ex) {
            Logger.error("["+context+"]", ex);
            return _apiInternalServerError (ex);
        }
    }

    
    @Dynamic(value = IxDynamicResourceHandler.CAN_UPDATE, handler = ix.ncats.controllers.security.IxDeadboltHandler.class)
    @BodyParser.Of(value = BodyParser.Json.class, maxLength = MAX_POST_PAYLOAD)
    public static Result updateUUID (String context, String id, String field) {
        try {
            return _registry.get()
                    .getResource(context)
                    .update(EntityFactory.toUUID(id), field);
        }catch (Exception ex) {
            Logger.error("["+context+"]", ex);
            return _apiInternalServerError (ex);
        }
    }
    
    @Dynamic(value = IxDynamicResourceHandler.CAN_UPDATE, handler = ix.ncats.controllers.security.IxDeadboltHandler.class)
    @BodyParser.Of(value = BodyParser.Json.class, maxLength = MAX_POST_PAYLOAD)
    public static Result patchUUID (String context, String id) {
        try {
            return _registry.get()
                    .getResource(context)
                    .patch(EntityFactory.toUUID(id));
        }catch (Exception ex) {
            Logger.error("["+context+"]", ex);
            return _apiInternalServerError (ex);
        }
    }
    
    //searchFacets(context: String, q: String ?= null, field ?= null, fdim: Int ?= 10, fskip: Int ?= 0, ffilter ?= "")
    @Dynamic(value = IxDynamicResourceHandler.CAN_SEARCH, handler = ix.ncats.controllers.security.IxDeadboltHandler.class)
    public static Result searchFacets (String context, String q, String field, int fdim, int fskip, String ffilter) {
        try {
            InstantiatedNamedResource<Object, Object> resource = _registry.get()
                    .getResource(context);
            
            Class kind = resource.getTypeKind();
            Class<SearchRequest.Builder> requestBuilderClass = resource.searchRequestBuilderClass();
            return SearchFactory.searchRESTFacets(requestBuilderClass, kind, q, field, fdim, fskip, ffilter);
            
        }catch (Exception ex) {
            Logger.error("["+context+"]", ex);
            return _apiInternalServerError (ex);
        }
    }
    
    
    @Dynamic(value = IxDynamicResourceHandler.CAN_SEARCH, handler = ix.ncats.controllers.security.IxDeadboltHandler.class)
    public static Result facets (String context, int top, int skip, String field) {
        try {
            Class kind = _registry.get()
                    .getResource(context)
                    .getTypeKind();
            EntityMapper em= EntityFactory.getEntityMapper();
            
            
            TermVectors tv=Play.application()
            .plugin(TextIndexerPlugin.class)
            .getIndexer()
            .getTermVectors(kind, field);
            
            SearchOptions so = new SearchOptions.Builder()
                    .fdim(10)
                    .fskip(0)
                    .ffilter("")
                    .withParameters(Util.reduceParams(request().queryString(), 
                                    "fdim", "fskip", "ffilter"))
                    .build();
            
            FacetMeta fm=tv.getFacet(so.getFdim(), so.getFskip(), so.getFfilter(), Global.getHost() + Controller.request().uri());       
            return Java8Util.ok(em.valueToTree(fm));
            
        }catch (Exception ex) {
            Logger.error("["+context+"]", ex);
            return _apiInternalServerError (ex);
        }
    }
    
    //facets(context: String, top: Int ?=10, skip: Int ?= 0, filter: String ?= null)


    /**
     * Special form to get entities with UUID as the ID without
     * giving a specific context. This works because UUIDs are globally
     * unique. This is the same as calling {@link #getUUID(String, String, String)}
     * for each UUID context until one returns a passing (<400) status
     * code, and then forwarding that result.
     * @param uuid
     * @param expand
     * @return
     */

    public static Result _getUUID (String uuid, String expand) {
        for (String context : _registry.get().getUUIDcontexts()) {
            try{
                Result r = getUUID (context, uuid, expand);
                if (r.toScala().header().status() < 400)
                    return r;
            }catch(Exception e){
                //Not found here
            }
            System.out.println("Not found in:" + context);
        }
        return _apiNotFound ("Unknown id "+uuid);
    }

    /**
     * Special form to get fields in entities with UUID as the ID without
     * giving a specific context. This works because UUIDs are globally
     * unique. This is the same as calling {@link #fieldUUID(String, String, String)}
     * for each UUID context until one returns a passing (<400) status
     * code, and then forwarding that result.
     * @param uuid
     * @param expand
     * @return
     */
    public static Result _fieldUUID (String uuid, String field) {
        for (String context : _registry.get().getUUIDcontexts()) {
            try{
                Result r = fieldUUID (context, uuid, field);
                if (r.toScala().header().status() < 400)
                    return r;
            }catch(Exception e){
                //Not found here
            }
        }
        return _apiNotFound ("Unknown id "+uuid);
    }

    
    
    
    
    

    public static Result checkPreFlight(String path) {
        response().setHeader("Access-Control-Allow-Origin", "*");      			  // Need to add the correct domain in here!!
        response().setHeader("Access-Control-Allow-Methods", "POST, PUT, GET, PATCH");   // Only allow POST, PUT, GET, PATCH
        response().setHeader("Access-Control-Max-Age", "300");         			  // Cache response for 5 minutes
        response().setHeader("Access-Control-Allow-Headers", "Origin, X-Requested-With, Content-Type, Accept");         // Ensure this header is also allowed!  
        return ok();
    }




    /**
     * Aka WHOAMI
     * @return
     */
    public static Result profile(){
        UserProfile p=UserFetcher.getActingUserProfile(false);
        if(p!=null){
            ObjectMapper om=new ObjectMapper();
            return Java8Util.ok(om.valueToTree(p));
        }
        return _apiNotFound("No user logged in");
    }

    public static Result profileResetKey(){
        UserProfile p=UserFetcher.getActingUserProfile(false);
        if(p!=null){
            p.regenerateKey();
            p.save();
            ObjectMapper om=new ObjectMapper();
            return Java8Util.ok(om.valueToTree(p));
        }
        return _apiNotFound("No user logged in");
    }



    private static JsonNode getError(Throwable t, int status){
        Map m=new HashMap();
        if(t instanceof InvocationTargetException){
            m.put("message", ((InvocationTargetException)t).getTargetException().getMessage());
        }else{
            m.put("message", t.getMessage());
        }
        m.put("status", status);
        ObjectMapper om = new ObjectMapper();
        t.printStackTrace();
        return om.valueToTree(m);
    }





    public static Result _apiBadRequest(Throwable t){
        return badRequest(getError(t, play.mvc.Http.Status.BAD_REQUEST));
    }
    public static Result _apiInternalServerError(Throwable t){
        return internalServerError(getError(t, play.mvc.Http.Status.INTERNAL_SERVER_ERROR));
    }
    public static Result _apiUnauthorized(Throwable t){
        return internalServerError(getError(t, play.mvc.Http.Status.UNAUTHORIZED));
    }
    public static Result _apiNotFound(Throwable t){
        return notFound(getError(new Throwable(t), play.mvc.Http.Status.NOT_FOUND));
    }

    public static Result _apiBadRequest(String t){
        return badRequest(getError(new Throwable(t), play.mvc.Http.Status.BAD_REQUEST));
    }
    public static Result _apiInternalServerError(String t){
        return internalServerError(getError(new Throwable(t), play.mvc.Http.Status.INTERNAL_SERVER_ERROR));
    }
    public static Result _apiUnauthorized(String t){
        return internalServerError(getError(new Throwable(t), play.mvc.Http.Status.UNAUTHORIZED));
    }
    public static Result _apiNotFound(String t){
        return notFound(getError(new Throwable(t), play.mvc.Http.Status.NOT_FOUND));
    }
    private static Result _apiForbidden(String t) {
        return forbidden(getError(new Throwable(t), play.mvc.Http.Status.FORBIDDEN));
    }

}
