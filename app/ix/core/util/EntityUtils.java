package ix.core.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.PostLoad;
import javax.persistence.PostPersist;
import javax.persistence.PostRemove;
import javax.persistence.PostUpdate;
import javax.persistence.PrePersist;
import javax.persistence.PreRemove;
import javax.persistence.PreUpdate;
import javax.persistence.Table;

import ix.core.adapters.EntityPersistAdapter;
import ix.core.controllers.PayloadFactory;
import ix.core.models.*;
import ix.core.plugins.SequenceIndexerPlugin;
import ix.ginas.models.v1.NucleicAcidSubstance;
import ix.ginas.models.v1.Substance;
import ix.seqaln.SequenceIndexer;
import org.apache.lucene.document.Document;
import org.jcvi.jillion.fasta.*;
import org.reflections.Reflections;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.DefaultSerializerProvider;

import ix.core.FieldNameDecorator;
import ix.core.IgnoredModel;
import ix.core.History;
import ix.core.ResourceReference;
import ix.core.SingleParent;
import ix.core.controllers.EntityFactory;
import ix.core.controllers.EntityFactory.EntityMapper;
import ix.core.factories.ApiFunctionFactory;
import ix.core.factories.FieldNameDecoratorFactory;
import ix.core.factories.SpecialFieldFactory;
import ix.core.search.EntityFetcher;
import ix.core.search.EntityFetcher.CacheType;
import ix.core.search.text.IndexableValueFromIndexable;
import ix.core.search.text.PathStack;
import ix.core.search.text.TextIndexer;
import ix.core.util.pojopointer.ArrayPath;
import ix.core.util.pojopointer.CountPath;
import ix.core.util.pojopointer.DistinctPath;
import ix.core.util.pojopointer.FieldPath;
import ix.core.util.pojopointer.FilterPath;
import ix.core.util.pojopointer.FlatMapPath;
import ix.core.util.pojopointer.GroupPath;
import ix.core.util.pojopointer.IDFilterPath;
import ix.core.util.pojopointer.IdentityPath;
import ix.core.util.pojopointer.LambdaPath;
import ix.core.util.pojopointer.LimitPath;
import ix.core.util.pojopointer.MapPath;
import ix.core.util.pojopointer.ObjectPath;
import ix.core.util.pojopointer.PojoPointer;
import ix.core.util.pojopointer.SkipPath;
import ix.core.util.pojopointer.SortPath;
import ix.utils.Global;
import ix.utils.LinkedReferenceSet;
import ix.utils.Tuple;
import ix.utils.Util;
import play.Logger;
import play.Play;
import play.db.ebean.Model;
import play.db.ebean.Model.Finder;
/**
 * A utility class, mostly intended to do the grunt work of reflection.
 * 
 * @author peryeata
 */
public class EntityUtils {
	private static final String ID_FIELD_NATIVE_SUFFIX = "._id";
	private static final String ID_FIELD_STRING_SUFFIX = ".id";

	/**
	 * This is a simplified memoized map to help avoid recalculating the same
	 * expensive value several times with different threads. It will also avoid
	 * the infinite loop caused by {@link ConcurrentHashMap} when adding a key
	 * with the same reduced {@link Object#hashCode()} during a call to
	 * {@link ConcurrentHashMap#computeIfAbsent(Object, Function)}.
	 * 
	 * Specifically, {@link ConcurrentHashMap} will hang indefinitely in the
	 * following case, but will not with a {@link CachedMap#computeIfAbsent(Object, Function)}
	 * call instead:
	 * 
	 * <pre>
     * {@code 
     *  public static void main(String... args) throws Exception {
     *    ConcurrentHashMap<MyKey, String> mymap = new ConcurrentHashMap<>();
     *      mymap.computeIfAbsent(new MyKey(), k->{
     *            mymap.computeIfAbsent(new MyKey(), k2->{   //Will hang forever
     *              return "OK";
     *          });
     *          return "OK2";
     *      });
     *  }
     *  public static class MyKey {
     *      &#64;Override
     *      public int hashCode() {
     *          return 1;   ///force to always be the same
     *      }
     *      public boolean equals(Object o) {
     *          if (this == o)
     *              return true;
     *          return false;
     *      }
     *  }
	 * }
	 * 
	 * </pre>
	 * 
	 * 
	 * @author peryeata
	 *
	 * @param <K>
	 * @param <V>
	 */
	private static class CachedMap<K,V>{
		Map<K, CachedSupplier<V>> map;
		
		public CachedMap(int size){
			map= new ConcurrentHashMap<K, CachedSupplier<V>>(size);
		}
		
		public V computeIfAbsent(K k, Function<K,V> fun){
			return map.computeIfAbsent(k, k2->CachedSupplier.of(()->fun.apply(k2)))
					.getSync();
		}
		
	}
	
	private final static CachedSupplier<CachedMap<String, EntityInfo<?>>> infoCache 
					= CachedSupplier.of(()->new CachedMap<>(2048));


	@Indexable // put default indexable things here
	static final class DefaultIndexable {

	}

	private static final Indexable defaultIndexable = (Indexable) DefaultIndexable.class.getAnnotation(Indexable.class);

	@SuppressWarnings("unchecked")
	public static <T> EntityInfo<T> getEntityInfoFor(Class<T> cls) {
		return (EntityInfo<T>) infoCache
								.get()
								.computeIfAbsent(cls.getName(), k -> new EntityInfo<>(cls));
	}

	public static <T> EntityInfo<T> getEntityInfoFor(T entity) {
		return getEntityInfoFor((Class<T>) entity.getClass());
	}

	public static EntityInfo<?> getEntityInfoFor(String className) throws ClassNotFoundException {
		Class<?> cls = IOUtil.getGinasClassLoader().loadClass(className);
		return getEntityInfoFor(cls);
	}

	/**
	 * This is a helper class used extensively to help mitigate some of the
	 * drawbacks of using generic objects in much of the deepest areas of the
	 * code.
	 * 
	 * <p>
	 * Information on the methods, fiends and annotations related to this class
	 * are memoized via a static #{@link ConcurrentHashMap}.
	 * </p>
	 * 
	 * <p>
	 * Wrapping an entity in this constructor will give access to some
	 * convenience methods that can be especially useful for finding smaller
	 * sets of known indexable values from all fields.
	 * </p>
	 * 
	 * <p>
	 * The method  {@link #traverse()} is particularly useful for
	 * building {@link EntityTraverser}s, which can allow for quick probing
	 * of all of the entity descendants.
	 * </p>
	 * 
	 * <p>
	 * TODO there is some inconsistent design and type-safe issues in this
	 * current instantiation
	 * </p>
	 * @author peryeata
	 *
	 *
	 * @param <T> The type of object wrapped by the {@link EntityWrapper}
	 */
	public static class EntityWrapper<T> {
		private T _k;
		private EntityInfo<T> ei;

		public static <T> EntityWrapper<T> of(T bean) {
			Objects.requireNonNull(bean, "wrapped object is null");
			if (bean instanceof EntityWrapper) {
				return (EntityWrapper) bean;
			}
			return new EntityWrapper<T>(bean);
		}

		private EntityWrapper(T o) {
			Objects.requireNonNull(o);
			this._k = o;
			ei = getEntityInfoFor(o);
		}

		public String toCompactJson() {
			return EntityMapper.COMPACT_ENTITY_MAPPER().toJson(getValue());
		}

		/**
		 * Get the Json of this entity using only the fields that are
		 * considered when json-diffing different versions of the entity.
		 * @return
		 */
		public String toJsonDiffJson() {
			return EntityMapper.JSON_DIFF_ENTITY_MAPPER().toJson(getValue());
		}
		/**
		 * Get the Json of this entity using only the fields that are
		 * considered when json-diffing different versions of the entity.
		 * @return
		 */
		public JsonNode toJsonDiffJsonNode() {
			return EntityMapper.JSON_DIFF_ENTITY_MAPPER().valueToTree(getValue());
		}
		public String toInternalJson() {
			return EntityMapper.INTERNAL_ENTITY_MAPPER().toJson(getValue());
		}

		public String toFullJson() {
			return EntityMapper.FULL_ENTITY_MAPPER().toJson(getValue());
		}

		public JsonNode toFullJsonNode() {
			return EntityMapper.FULL_ENTITY_MAPPER().valueToTree(getValue());
		}
		
		public T getClone() throws JsonProcessingException{
			return this.ei.fromJsonNode(this.toFullJsonNode());
		}
		
		public T getWrappedClone() throws JsonProcessingException{
			return this.ei.fromJsonNode(this.toFullJsonNode());
		}

		public Key getKey() throws NoSuchElementException {
			return Key.of(this);
		}

		public Optional<Key> getOptionalKey() {

			// TODO: Try catch is not really right here, should be
			// handled slightly differently
			try {
				return Optional.of(this.getKey());
			} catch (Exception e) {
				return Optional.empty();
			}
		}

		// Useful for doing recursive searches, etc
		public EntityTraverser traverse() {
			return new EntityTraverser().using(this);
		}

		public String toString() {
			return this.getKind() + ":" + this.getValue().toString();
		}

		public List<FieldMeta> getUniqueColumns() {
			return ei.getUniqueColumns();
		}

		public Finder<?,T> getFinder() {
			return ei.getNativeSpecificFinder();
		}
		
		public Finder<?,T> getFinder(String datasource) {
			return ei.getNativeSpecificFinder(datasource);
		}

		public boolean isValidated(){
			if(!ei.hasValidationField()){
				return false;
			}
			// If the validation field is anything other than boolean,
			// this will be a problem. 
			try{
				return (boolean) ei.getValidationField().getValue(this).orElse(false);
			}catch(Exception e){
				Logger.error("DataValidated annotation set on non-boolean!");
				return false;
			}
		}

		public boolean shouldIndex() {
			return ei.shouldIndex();
		}

		public Optional<MethodOrFieldMeta> getIdFieldInfo() {
			return ei.getIDFieldInfo();
		}
		private static Pattern PAYLOAD_UUID_PATTERN = Pattern.compile("payload\\((.+?)\\)");
		public Stream<Tuple<MethodOrFieldMeta, Object>> streamSequenceFieldAndValues(Predicate<MethodOrFieldMeta> p) {
			Stream<Tuple<MethodOrFieldMeta, Object>> fieldInfoStream = ei.getSequenceFieldInfo().stream()
					.filter(p)
					.map(m -> new Tuple<>(m, m.getValue(this.getValue())))
					.filter(t -> t.v().isPresent())
					.map(t -> new Tuple<>(t.k(), t.v().get()));

			if (NucleicAcidSubstance.class.isAssignableFrom(ei.getEntityClass())) {
				Substance obj = ((Substance) this.getValue());
				SequenceIndexer sequenceIndexer = EntityPersistAdapter.getSequenceIndexer();
				obj.references.stream()
						.filter(r -> r.uploadedFile != null)

						.flatMap(r -> {
									if (r.tags.stream()
											.peek(k -> System.out.println(k))
											.filter(k -> k.term.equalsIgnoreCase("fasta"))
											.findAny()
											.isPresent()) {
										Matcher m = PAYLOAD_UUID_PATTERN.matcher(r.uploadedFile);
										if (m.find()) {
											String uuid = m.group(1);
											System.out.println("found payload " + uuid);
											Payload payload = PayloadFactory.getPayload(UUID.fromString(uuid));
											return Stream.of(payload);
										}
									}
									return Stream.empty();
								}
						).forEach(payload -> {
					File f = PayloadFactory.getFile(payload);


					try {
						FastaFileParser.create(f).parse(new FastaVisitor() {
							@Override
							public FastaRecordVisitor visitDefline(FastaVisitorCallback fastaVisitorCallback, String id, String comment) {
								//TODO process comments
								return new AbstractFastaRecordVisitor(id, comment) {
									@Override
									protected void visitRecord(String id, String comment, String seq) {

										System.out.println("adding seq:" + seq);
										try {
											sequenceIndexer.add(">"+obj.uuid +"|"+id, seq);
										} catch (IOException e) {
											e.printStackTrace();
										}


									}
								};
							}

							@Override
							public void visitEnd() {

							}

							@Override
							public void halted() {

							}
						});
					} catch (IOException e) {
						e.printStackTrace();
					}
				});

			}

				return fieldInfoStream;

		}

			private static class FastaMeta implements MethodOrFieldMeta{

				private final String sequence;

				public FastaMeta(String sequence) {
					this.sequence = sequence;
				}

				@Override
				public Optional<Object> getValue(Object entity) {
					return Optional.of(sequence);
				}

				@Override
				public boolean isGenerated() {
					return true;
				}

				@Override
				public boolean isNumeric() {
					return false;
				}

				@Override
				public Class<?> getType() {
					return String.class;
				}

				@Override
				public String getName() {
					return null;
				}

				@Override
				public boolean isJsonSerialized() {
					return false;
				}

				@Override
				public String getJsonFieldName() {
					return null;
				}

				@Override
				public boolean isArray() {
					return false;
				}

				@Override
				public boolean isCollection() {
					return false;
				}

				@Override
				public boolean isTextEnabled() {
					return false;
				}

				@Override
				public boolean isSequence() {
					return true;
				}

				@Override
				public boolean isStructure() {
					return false;
				}

				@Override
				public Class<?> deserializeAs() {
					return null;
				}

				@Override
				public <T> JsonSerializer<T> getSerializer() {
					return null;
				}

				@Override
				public int getJsonModifiers() {
					return 0;
				}
			}

		public Stream<Tuple<MethodOrFieldMeta, Object>> streamStructureFieldAndValues(Predicate<MethodOrFieldMeta> p) {
			return ei.getStructureFieldInfo().stream().filter(p)
					.map(m -> new Tuple<>(m, m.getValue(this.getValue())))
					.filter(t -> t.v().isPresent())
					.map(t -> new Tuple<>(t.k(), t.v().get()));
		}

		public List<MethodOrFieldMeta> getStructureFieldAndValues() {
			return ei.getStructureFieldInfo();
		}

		public List<FieldMeta> getFieldInfo() {
			return ei.getTextIndexableFields();
		}

		//TODO: move
		public static Predicate<FieldMeta> isCollection = (f -> f.isArrayOrCollection());

		public Stream<Tuple<FieldMeta, List<Tuple<Integer,Object>>>> streamCollectedFieldsAndValues(Predicate<FieldMeta> p) {
			return streamFieldsAndValues(isCollection.and(p))
					.map(fi->new Tuple<FieldMeta,List<Tuple<Integer,Object>>>(fi.k(), //It's so easy that a child could do it!
							fi.k().valuesList(fi.v()) // list
							));
		}

		public Stream<Tuple<FieldMeta, Object>> streamFieldsAndValues(Predicate<FieldMeta> p) {
			Object bean = this.getValue();
			return ei.getTextIndexableFields().stream()
					.filter(p)
					.map(f -> new Tuple<>(f, f.getValue(bean)))
					.filter(t -> t.v().isPresent())
					.map(t -> new Tuple<>(t.k(), t.v().get()));
		}

		public List<MethodMeta> getMethodInfo() {
			return ei.getTextIndexableMethods();
		}

		public Stream<Tuple<MethodMeta, Object>> streamMethodsAndValues(Predicate<MethodMeta> p) {
			return ei.getTextIndexableMethods().stream()
					.filter(p)
					.map(m -> new Tuple<>(m, m.getValue(this.getValue())))
					.filter(t -> t.v().isPresent())
					.map(t -> Tuple.of(t.k(), t.v().get()));
		}
		
		public Stream<Tuple<MethodOrFieldMeta, Object>> streamJsonMethodsOrFieldsAndValues(Predicate<MethodOrFieldMeta> p) {
			return Stream
					.concat(ei.getTextIndexableMethods().stream(),ei.getTextIndexableMethods().stream())
					.filter(p)
					.map(m -> new Tuple<>(m, m.getValue(this.getValue())))
					.filter(t -> t.v().isPresent())
					.map(t -> Tuple.of(t.k(), t.v().get()));
		}

		public String get_IdField() {
			return ei.getInternalIdField();
		}

		public String getIdField() {
			return ei.getExternalIdFieldName();
		}

		public boolean isEntity() {
			return ei.isEntity();
		}
		
		public boolean isArrayOrCollection() {
			return Collection.class.isAssignableFrom(ei.getEntityClass()) || ei.getEntityClass().isArray();
		}
		
		/**
		 * Returns an Optional Tuple for the value at the field.
		 * The reason for the strange nested structure is to allow to interrogate
		 * why a value wasn't found if one isn't found. It may be that the field itself
		 * is not found, or that the value at that field is not retrievable. 
		 * 
		 * This method will return a non-empty optional if the field is accessible, and the
		 * internal Optional will be non-empty if the value there is real.
		 * @param field
		 * @return
		 */
		public Optional<Tuple<MethodOrFieldMeta,Optional<Object>>> getValueAndFieldAt(String field){
			if(Map.class.isAssignableFrom(ei.getEntityClass())){
				VirtualMapMethodMeta vmmm=new VirtualMapMethodMeta(field);
				return Optional.of(Tuple.of(vmmm, vmmm.getValue(this.getValue())));
			}
			return ei.getJsonGetterFor(field).map(f->Tuple.of(f,f.getValue(this.getValue())));
		}
		
		public Stream<MethodOrFieldMeta> fields(){
			if(Map.class.isAssignableFrom(ei.getEntityClass())){
				return ((Map<String,Object>)this.getValue())
						   .keySet()
				           .stream()
				           .map(k->new VirtualMapMethodMeta(k.toString()));
			}
			return ei.streamJsonGetters();
		}
		
		public Stream<Tuple<MethodOrFieldMeta,Optional<Object>>> fieldsAndValues(){
			System.out.println("Getting values for:"  + getKind());
			return fields().map(m->Tuple.of(m, m.getValue(this.getValue())));
		}
		
		
		
		public Object getValueOrNullAt(String field){
			return getValueAndFieldAt(field)
					.map(t->t.v())
					.orElse(Optional.empty())
					.orElse(null);
		}

		public boolean ignorePostUpdateHooks() {
			return ei.ignorePostUpdateHooks();
		}

		public Class<T> getEntityClass() {
			return ei.getEntityClass();
		}

		public boolean hasVersion() {
			return ei.hasVersion();
		}

		public boolean hasIdField() {
			return ei.hasIdField();
		}

		public EntityInfo<T> getEntityInfo() {
			return this.ei;
		}

		public Optional<?> getId() {
			return this.ei.getIdPossiblyFromEbeanMethod((Object) this.getValue());
		}

		public String getKind() {
			return this.ei.getName();
		}

		public Optional<String> getVersion() {
			return Optional.ofNullable(ei.getVersionAsStringFor(this.getValue()));
		}

		public T getValue() {
			return this._k;
		}

		public JsonNode toJson(ObjectMapper om) {
			return om.valueToTree(this.getValue());
		}

		public Optional<Tuple<String, String>> getDynamicFacet() {
			return this.ei.getDynamicFacet(this.getValue());
		}

		// Convenience Method
		public boolean hasKey() {
			return this.getOptionalKey().isPresent();
		}

		public boolean isIgnoredModel() {
			return this.ei.isIgnoredModel();
		}
		
		public boolean storeHistory() {
			return this.ei.storeHistory();
		}

		public String getInternalIdField() {
			return this.ei.getInternalIdField();
		}

		public Stream<Keyword> streamMethodKeywordFacets() {
			return this.ei.getKeywordFacetMethods()
					.stream()
					.map(m->m.getValue(this._k))
					.filter(Optional::isPresent)
					.map(o->(Keyword)o.get());
		}

		public Optional<String> getChangeReason() {
			return ei.getChangeReasonFor(_k);
		}

		public List<Edit> getEdits(){
			Optional<Object> opId= this.ei.getNativeIdFor(this._k);
			if(opId.isPresent()){
				return EntityFactory.getEdits(opId.get(), 
						this.getEntityInfo().getInherittedRootEntityInfo().getTypeAndSubTypes()
						.stream()
						.map(em->em.getEntityClass())
						.toArray(len->new Class<?>[len]));
			}else{
				return new ArrayList<Edit>();
			}
		}

		public Object getValueFromMethod(String name){
			return this.streamMethodsAndValues(m->m.getMethodName().equals(name)).findFirst().get().v();
		}

		public boolean isExplicitDeletable() {
			return this.ei.isExplicitDeletable();
		}
		/**
		 * This method just delegates to {@link Model#save()} for the
		 * wrapped entity, only also passing in the datasource
		 * that was specified via the config file.
		 */
		public void save(){
			if(this.ei.datasource!=null) {
				((Model) this.getValue()).save(ei.datasource);
			}else{
				((Model) this.getValue()).save();
			}
		}

		public void delete() {
			if(this.ei.datasource!=null) {
				((Model) this.getValue()).delete(ei.datasource);
			}else{
			((Model)this.getValue()).delete();
		}
		}

		public void update() {
			if(_k instanceof ForceUpdatableModel){ //TODO: Move to EntityInfo
        		((ForceUpdatableModel)_k).forceUpdate();
        	}else if(_k instanceof Model){
        	    if(this.ei.datasource!=null){
        	        ((Model)_k).update(this.ei.datasource);
        	    }else{
        		((Model)_k).update();
        	}
		}
		}

		public Optional<Object> getArrayElementAt(int index) {
			if(!this.isArrayOrCollection()){
				return Optional.empty();
			}
			if(this.getValue() instanceof Collection){
				Collection coll = (Collection)this.getValue();
				if(coll instanceof List){
					List l = (List)coll;
					try{
						return Optional.of(l.get(index));
					}catch(Exception e){
						return Optional.empty();
					}
				}else{
					return coll.stream().skip(index).limit(1).findFirst();
				}
			}else{
				try{
					return Optional.of(((Object[])this.getValue())[index]);
				}catch(Exception e){
					return Optional.empty();
				}
			}
		}
		
		public Stream<Object> streamArrayElements() {
			if(!this.isArrayOrCollection()){
				return Stream.empty();
			}
			if(this.getValue() instanceof Collection){
				return ((Collection<Object>)this.getValue()).stream();
			}else{
				return (Arrays.stream((Object[])this.getValue()));
			}
		}
		
		public Stream<T> streamArrayElements(Class<T> type) {
            if(!this.isArrayOrCollection()){
                return Stream.empty();
            }
            if(this.getValue() instanceof Collection){
                return ((Collection<T>)this.getValue()).stream();
            }else{
                return (Arrays.stream((T[])this.getValue()));
            }
        }
		
		/**
		 * Same as {@link #streamArrayElements()} only the elements
		 * are contained in {@link EntityWrapper} as a stream
		 * @return
		 */
		public Stream<EntityWrapper<?>> streamWrappedArrayElements() {
			return streamArrayElements().map(EntityWrapper::of);
		}
		
		
		
		
		private static 
		CachedSupplier<Map<String,BiFunction<PojoPointer, EntityWrapper<?>, Optional<EntityWrapper<?>>>>>
			finders = CachedSupplier.of(()->{
				Map<String, BiFunction<PojoPointer, EntityWrapper<?>, Optional<EntityWrapper<?>>>>
					registry= new HashMap<>();
				
				registry.put(IdentityPath.class.getName(), (cpath,current)->{
					return Optional.of(current);
				});
				
				//ObjectPath locator
				registry.put(ObjectPath.class.getName(), (cpath,current)->{
					ObjectPath op = (ObjectPath)cpath;
	        		String fieldname=op.getField();
	        		Optional<Tuple<MethodOrFieldMeta,Optional<Object>>> value 
	        				=current.getValueAndFieldAt(fieldname);
	        		if(!value.isPresent() || !value.get().v().isPresent()){
	        			return Optional.empty();
	        		}
	        		Object rv=value.get().v().get();
	        		return Optional.of(EntityWrapper.of(rv));
				});
				
				//ArrayPath locator
				registry.put(ArrayPath.class.getName(), (cpath,current)->{
					ArrayPath ap = (ArrayPath)cpath;
					
					Optional<Object> value =current.getArrayElementAt(ap.getIndex());
	        		if(!value.isPresent()){
	        			return Optional.empty();
	        		}
	        		return Optional.of(EntityWrapper.of(value.get()));
				});
				
				//IDPath locator
				registry.put(IDFilterPath.class.getName(), (cpath,current)->{
					IDFilterPath idp = (IDFilterPath)cpath;
					
					Optional<EntityWrapper<?>> atId=current
							.streamWrappedArrayElements()
							.filter(e->idp.getId().equals(e.getKey().getIdString()))
							.findFirst();
					return atId;
				});
				
				//FilterPath locator
				registry.put(FilterPath.class.getName(), (cpath,current)->{
	        		FilterPath fp = (FilterPath)cpath;
	        		List<Object> list=current.streamWrappedArrayElementsAt(fp.getField())
	        		  .filter(t->t.v().isPresent())
	        		  .filter(t->fp.getValue().equals(t.v().get().getValue()+""))
	        		  .map(t->t.k())
	        		  .map(EntityWrapper::getValue)
	        		  .collect(Collectors.toList());
	        		return Optional.of(EntityWrapper.of(list));
				});
				
				//MapPath locator
				registry.put(MapPath.class.getName(), (cpath,current)->{
					MapPath mp =(MapPath)cpath;
					
					List<Object> list=current
	        					.streamWrappedArrayElementsAt(mp.getField())
	        					.map(t->t.v().orElse(null))
	        					.filter(Objects::nonNull) 		//TODO keep nulls?
	        					.map(EntityWrapper::getValue)
	        					.collect(Collectors.toList());
	        		return Optional.of(EntityWrapper.of(list));
				});
				
				registry.put(FlatMapPath.class.getName(), (cpath,current)->{
					FlatMapPath mp =(FlatMapPath)cpath;
					List<Object> list =current
							.streamWrappedArrayElementsAt(mp.getField())
        					.map(t->t.v().orElse(null))
        					.filter(Objects::nonNull)
        					.filter(ew->ew.isArrayOrCollection())
        					.flatMap(ew->ew.streamArrayElements())
        					.collect(Collectors.toList());
					return Optional.of(EntityWrapper.of(list));
				});
				
				
				registry.put(CountPath.class.getName(), (cpath,current)->{//Should be 0 arg?
					CountPath mp =(CountPath)cpath;						  //Probably.
	        		Long value =current.streamArrayElements()             //It's now doing a map(f->f.path()).filter().count()
	        					.map(EntityWrapper::of)
	        					.map(e->e.at(mp.getField()).orElse(null))
	        					.filter(Objects::nonNull)
	        					.count();
	        		return Optional.of(EntityWrapper.of(value));
				});
				
				//Should be 0 arg?
				//Yes, I think so
				//This is now doing a map(f->f.path()).filter().distinct()
				registry.put(DistinctPath.class.getName(), (cpath,current)->{
					DistinctPath mp =(DistinctPath)cpath;		
	        		Object value =current.streamArrayElements() 
	        					.map(EntityWrapper::of)
	        					.map(e->e.at(mp.getField()).orElse(null))
	        					.filter(Objects::nonNull)
	        					.map(EntityWrapper::getValue)
	        					.distinct()
	        					.collect(Collectors.toList());
	        		return Optional.of(EntityWrapper.of(value));
				});
				
				
				
				registry.put(FieldPath.class.getName(), (cpath,current)->{
					FieldPath fp =(FieldPath)cpath;		
	        		List<Tuple<String,Object>> value=current
	        					.fieldsAndValues()
	        					.map(t->Tuple.of(t.k().getJsonFieldName(), t.v().orElse(null)))
	        					.collect(Collectors.toList());
	        		return Optional.of(EntityWrapper.of(value));
				});
				
				registry.put(SortPath.class.getName(), (cpath,current)->{
					SortPath sp =(SortPath)cpath;
	        		List<Object> value=current.streamArrayElements() 
        					.map(EntityWrapper::of)
        					.map(Util.toIndexedTuple())
        					.map(e->Tuple.of(e,e.v().at(sp.getField())))
        					.sorted((t1,t2)->{
        						int r=0;
        						if(t1.v().isPresent() && t2.v().isPresent()){
        							EntityWrapper ew1=t1.v().get();
        							EntityWrapper ew2=t2.v().get();
        							r= ew1.compareIfPossible(ew2);
        						}else if(t1.v().isPresent()){
        							r= 1;
        						}else{
        							r= -1;
        						}
        						if(r==0){
        							r=Integer.compare(t1.k().k(),t2.k().k());
        						}
        						if(sp.isReverse())return -r;
        						return r;
        					})
        					.map(t->t.k().v().getValue())
        					.collect(Collectors.toList());
	        		return Optional.of(EntityWrapper.of(value));
				});
				
				registry.put(LimitPath.class.getName(), (cpath,current)->{
					LimitPath sp =(LimitPath)cpath;
	        		List<Object> value=current.streamArrayElements() 
        					.limit(sp.getValue())
        					.collect(Collectors.toList());
	        		return Optional.of(EntityWrapper.of(value));
				});
				
				registry.put(SkipPath.class.getName(), (cpath,current)->{
					SkipPath sp =(SkipPath)cpath;
	        		List<Object> value=current.streamArrayElements() 
        					.skip(sp.getValue())
        					.collect(Collectors.toList());
	        		return Optional.of(EntityWrapper.of(value));
				});
				
				registry.put(GroupPath.class.getName(), (cpath,current)->{
					GroupPath mp =(GroupPath)cpath;
	        		Map<String,List<Object>> value= new LinkedHashMap<>();
	        		current.streamArrayElements() 
	        				.map(EntityWrapper::of)
        					.collect(Collectors.groupingBy(
		        							t->t.at(mp.getField())
		        							    .orElse(EntityWrapper.of("<NONE>"))
		        							    .getValue()
		        							    .toString()))
        					.forEach((k,v)->{
			        			List<Object> olist=v.stream()
			        								.map(EntityWrapper::getValue)
			        								.collect(Collectors.toList());
			        			value.put(k, olist);
			        		});
	        		return Optional.of(EntityWrapper.of(value));
				});
				
				try{
				ApiFunctionFactory
					.getInstance(Play.application())
					.getRegisteredFunctions()
					.stream().forEach(rf->{
						registry.put(rf.getFunctionClass().getName(), (cpath,current)->{
							BiFunction<LambdaPath, Object, Optional<Object>> function =
									rf.getOperation();
							return function
										.apply((LambdaPath) cpath, (Object)current.getValue())
										.map(EntityWrapper::of);
						});
					});
				}catch(Exception e){
				    Logger.error(e.getMessage(),e);
				}
				return registry;
			});
		
		
		@SuppressWarnings("unchecked")
		private Stream<Tuple<EntityWrapper<?>,Optional<EntityWrapper<?>>>> streamWrappedArrayElementsAt(PojoPointer cpath){
			return this.streamWrappedArrayElements()
					   .map(e->Tuple.of(e, e.at(cpath)));
		}
		
		/**
		 * Uses a {@link PojoPointer} to retrieve specific elements within
		 * the value wrapped by this {@link EntityWrapper}. This can be used
		 * much like {@link JsonNode#at(com.fasterxml.jackson.core.JsonPointer)}
		 * which retrieves a JsonNode representing the element at that location.
		 * 
		 * <b>Experimental</b>
		 * @param cpath The PojoPointer representing the location of the element(s) to be retrieved
		 * @return
		 */
		public Optional<EntityWrapper<?>> at(PojoPointer cpath){

	        EntityWrapper<Object> current=(EntityWrapper<Object>) this;
	        
	        do {
	        	BiFunction<PojoPointer, EntityWrapper<?>, Optional<EntityWrapper<?>>> finder=
	        	finders.get().get(cpath.getClass().getName());
	        	if(finder!=null){
	        	    Optional<EntityWrapper<?>> found=finder.apply(cpath, current);
	        		if(!found.isPresent())return found;
	        		current=(EntityWrapper<Object>) found.get();
	        	}else{
	        		throw new IllegalArgumentException("Unknown PojoPointer type:" + cpath.getClass());
	        	}
	        	if(!cpath.hasTail())break;
	        	
	        	//next
	        	cpath=cpath.tail();
	        } while(true);
	        
	        return Optional.of(current);
	    }

		
		private int compareIfPossible(EntityWrapper<T> ew2) {
			Objects.requireNonNull(ew2);
			if(this.getValue() instanceof Comparable && ew2.getValue() instanceof Comparable){
				return ((Comparable<T>)this.getValue()).compareTo(ew2.getValue());
			}
			return 0;
		}

		
		/**
		 * Get the "raw" value of this object for the REST API. Typically this 
		 * is just the value itself, not serialized as a JsonNode, but it may
		 * mean something special in certain cases.
		 * @return
		 */
		public Object getRawValue() {
			if(this.getValue() instanceof ResourceReference){
				return ((ResourceReference) this.getValue()).rawJson();
			}
			return this.getValue();
		}

        public String getDataSource() {
            return this.ei.datasource;
        }

		
	}

	public static class EntityInfo<T> {
	    
	    private String datasource=null;
	    
		private final Class<T> cls;
		private final String kind;
		private final DynamicFacet dyna;
		private final Indexable indexable;
		private List<FieldMeta> fields;
		private Table table;
		private List<MethodOrFieldMeta> seqFields = new ArrayList<MethodOrFieldMeta>();
		private List<MethodOrFieldMeta> strFields = new ArrayList<MethodOrFieldMeta>();;

		private List<MethodMeta> methods;

		private List<MethodMeta> textMethods;
		
		private List<MethodOrFieldMeta> jsonFields;

		private List<MethodMeta> keywordFacetMethods;

		private List<FieldMeta> uniqueColumnFields;

		private MethodOrFieldMeta changeReasonField = null;
		private MethodOrFieldMeta versionField = null;
		private MethodOrFieldMeta validatedField = null;
		private MethodOrFieldMeta idField = null;

		private MethodOrFieldMeta ebeanIdMethod = null;

		private MethodMeta ebeanIdMethodSetter = null;

		private FieldMeta dynamicLabelField = null;
		private FieldMeta dynamicValueField = null;

		private volatile boolean isEntity = false;
		private volatile boolean shouldIndex = true;
		private volatile boolean shouldDoPostUpdateHooks = true;
		private volatile boolean hasUniqueColumns = false;
		private String ebeanIdMethodName = null;

		private String tableName = null;

		private Class<?> idType = null;

		private CachedSupplier<Model.Finder<?, T>> nativeVerySpecificFinder;

		private boolean isIdNumeric = false;
		private Inheritance inherits;
		private boolean isIgnoredModel = false;

		private boolean hasBackup = false;
		private boolean storeHistory = true;
		private EntityInfo<?> ancestorInherit;

		private boolean isExplicitDeletable=false;

		private Supplier<Set<EntityInfo<? extends T>>> forLater;
		
		
		Map<String,MethodOrFieldMeta> jsonGetters;

		//Some simple factory helper methods
		public Set<String> getSponsoredFields() {
			return SpecialFieldFactory
					.getInstance(Play.application())
					.getRegisteredResourcesFor(this)
					;
		}

		public boolean storeHistory() {
			return storeHistory;
		}

		public FieldNameDecorator getFieldNameDecorator() {
			return FieldNameDecoratorFactory
					.getInstance(Play.application())
					.getSingleResourceFor(this);
		}


		public boolean isExplicitDeletable(){
			return isExplicitDeletable;
		}


		public static boolean isPlainOldEntityField(FieldMeta f) {
			return (!f.isPrimitive() && !f.isArrayOrCollection() && f.isEntityType() && f.getIndexable().recurse());
		}

		public Optional<String> getChangeReasonFor(T value) {
			if(this.changeReasonField==null)return Optional.empty();
			return this.changeReasonField
					.getValue(value)
					.map(k->k.toString());
		}

		public List<MethodMeta> getKeywordFacetMethods() {
			return this.keywordFacetMethods;
		}


		public EntityInfo(Class<T> cls) {

			Objects.requireNonNull(cls);

			this.cls = cls;

			this.hasBackup = (cls.getAnnotation(Backup.class) != null);

			this.isIgnoredModel = (cls.getAnnotation(IgnoredModel.class) != null);
			this.indexable = (Indexable) cls.getAnnotation(Indexable.class);
			this.table = (Table) cls.getAnnotation(Table.class);
			this.inherits = (Inheritance) cls.getAnnotation(Inheritance.class);
			ancestorInherit = this;
			if (cls.isAnnotationPresent(Entity.class)) {
                isEntity = true;
                if (indexable != null && !indexable.indexed()) {
                    shouldIndex = false;
                }
            }
			if (this.table != null) {
				tableName = table.name();
			} else if (this.inherits != null || isEntity) {
				EntityInfo<?> ei = EntityUtils.getEntityInfoFor(cls.getSuperclass());
				tableName = ei.getTableName();
				table = ei.table;
				if(tableName!=null){
				    ancestorInherit = ei.ancestorInherit;
				}
			}
			History history = (History) cls.getAnnotation(History.class);
			
			if(history!=null){
				this.storeHistory = history.store();
			}
			kind = cls.getName();

			// ixFields.add(new FacetField(DIM_CLASS, kind));
			dyna = (DynamicFacet) cls.getAnnotation(DynamicFacet.class);
			fields = Arrays.stream(cls.getFields())
					.map(f2 -> new FieldMeta(f2, dyna))
					.peek(f -> {
						if (f.isId()) {
							idField = f;
						} else if (f.isDynamicFacetLabel()) {
							dynamicLabelField = f;
						} else if (f.isDynamicFacetValue()) {
							dynamicValueField = f;
						}
						if (f.isDataVersion()) {
							versionField = f;
						}
						if (f.isDataValidationFlag()){
							validatedField = f;
						}
						if (f.isChangeReason()){
							this.changeReasonField=f;
						}
					})
					.collect(Collectors.toList());

			
			

			methods = Arrays.stream(cls.getMethods()).map(m2 -> new MethodMeta(m2)).peek(m -> {
				if (m.isDataVersion()) {
					versionField = m;
				} else if (m.isId()) {
					// always choose method IDs over
					// field IDs
					idField = m;
				}
				if (m.isDataValidationFlag()){
					validatedField = m;
				}
				if (m.isChangeReason()){
					this.changeReasonField=m;
				}
			}).collect(Collectors.toList());
			
			jsonGetters =Stream.concat(methods.stream(),  fields.stream())
			      .filter(m->m.isJsonSerialized())
			      .collect(Collectors.toMap(m->m.getJsonFieldName(), m->m, (a,b)->{
			    	  if(a.getJsonModifiers()>b.getJsonModifiers())return a;
			    	  return b;
			      }));
			
			
			uniqueColumnFields = fields.stream()
					.filter(f -> f.isUniqueColumn())
					.collect(Collectors.toList());
			
			fields.removeIf(f->f.isId());
			

			this.keywordFacetMethods = methods.stream()
					.filter(m->m.isGetter())
					.filter(m->m.getType().isAssignableFrom(Keyword.class))
					.filter(m->m.getIndexable()!=null)
					.collect(Collectors.toList());
			
			

			seqFields = Stream.concat(fields.stream(), methods.stream())
					.filter(f -> f.isSequence())
					.collect(Collectors.toList());
			strFields = Stream.concat(fields.stream(), methods.stream())
					.filter(f -> f.isStructure())
					.collect(Collectors.toList());


			fields.removeIf(f -> !f.isTextEnabled());

			
			if (Edit.class.isAssignableFrom(cls)) {
				shouldDoPostUpdateHooks = false;
			}

			if (idField != null) {
				ebeanIdMethodName = getBeanName(idField.getName());
				methods.stream().filter(m -> (m != idField)).filter(m -> m.isGetter())
				.filter(m -> m.getMethodName().equalsIgnoreCase("get" + ebeanIdMethodName)).findAny()
				.ifPresent(m -> {
					ebeanIdMethod = m;
				});
				methods.stream().filter(m -> (m != idField)).filter(m -> m.isSetter())
				.filter(m -> m.getMethodName().equalsIgnoreCase("set" + ebeanIdMethodName)).findAny()
				.ifPresent(m -> {
					ebeanIdMethodSetter = m;
				});

				idType = idField.getType();

				if (idField != null) {
					nativeVerySpecificFinder = CachedSupplier.of(()->{
					    if(this.datasource==null){
					        return new Model.Finder(idType, this.cls);  
					    }else{
					        return new Model.Finder(this.datasource,idType, this.cls);
					    }
					});
				}
			}

			textMethods=methods.stream().filter(m -> m.isTextEnabled()).collect(Collectors.toList());

			//needs deferred
			forLater = ()->{
				Set<EntityInfo<? extends T>> releventClasses= new HashSet<EntityInfo<? extends T>>();
				Reflections reflections = new Reflections(TextIndexer.IX_BASE_PACKAGE);
				releventClasses = reflections.getSubTypesOf((Class<T>) cls).stream()
						.map(c -> EntityUtils.getEntityInfoFor(c)).collect(Collectors.toSet());
				releventClasses.add(this);
				return releventClasses;
			};

			forLater = CachedSupplier.of(forLater); //make it Memoized
			//(vaaawwwy memoized)

			if (idType != null) {
				isIdNumeric = idType.isAssignableFrom(Long.class);
			}
			
			isExplicitDeletable=(!cls.isAnnotationPresent(IgnoredModel.class) &&
					 cls.isAnnotationPresent(SingleParent.class));
			isExplicitDeletable &= Model.class.isAssignableFrom(cls);
			
			
			if(this.isEntity()){
    			Map m = new HashMap();
    			
    			Map mm=ConfigHelper.getOrDefault("ebean", m);
    			
    			mm.forEach((k,v)->{
    			   List<String> classes= new ArrayList<>();
    			   if(v instanceof String){
    			       classes= Arrays.stream(((String) v).split(","))
    			             .map(s->s.trim())
    			             .collect(Collectors.toList());
    			   }else if(v instanceof List){
    			       classes= ((List<Object>)v).stream()
    			             .map(o->o.toString().trim())
    			             .collect(Collectors.toList());
    			       
    			                       
    			   }
    			    
    			   Optional<String> p = classes.stream()
    			           .map(s->s.replace(".*", ""))
    			           .filter(s->{
    			               return this.getName().startsWith(s);
    			          }).findFirst();
    			   
    			   if(p.isPresent()){
    			       if(!k.toString().equals("default")){
    			           this.datasource=k.toString();
    			       }
    			      // System.out.println(this.getName() + ":" + p.get() + "->" + k);
    			   }else{
    			       
    			   }
    			   
    			});
			}
		}



		public EntityInfo<?> getInherittedRootEntityInfo() {
			return ancestorInherit;
		}

		public String getTableName() {
			return this.tableName;
		}

		public boolean isIgnoredModel() {
			return this.isIgnoredModel;
		}

		public boolean hasLongId() {
			return this.isIdNumeric;
		}
		public boolean hasValidationField(){
			return this.validatedField!=null;
		}
		public MethodOrFieldMeta getValidationField(){
			return this.validatedField;
		}

		public Class getIdType() {
			return idType;
		}

		public Set<EntityInfo<? extends T>> getTypeAndSubTypes() {
			return forLater.get();
		}

		public boolean equals(Object o) {
			if (o == null)
				return false;
			if (!(o instanceof EntityInfo))
				return false;
			return ((EntityInfo) o).cls == this.cls;
		}

		public int hashCode() {
			return this.cls.hashCode();
		}

		public List<FieldMeta> getUniqueColumns() {
			return this.uniqueColumnFields;
		}

		public Model.Finder<Object,?> getFinder() {
			return (Finder<Object, ?>) this.getInherittedRootEntityInfo().getNativeSpecificFinder();
		}
		
		
		public Model.Finder<Object,?> getFinder(String datasource) {
			return (Finder<Object, ?>) this.getInherittedRootEntityInfo().getNativeSpecificFinder(datasource);
		}

		public Model.Finder<Object,T> getNativeSpecificFinder() {
			if(this.nativeVerySpecificFinder==null)return null;
			return (Finder<Object, T>) this.nativeVerySpecificFinder.get();
		}
		
		
		private Map<String, Model.Finder<Object,T>> findermap = new ConcurrentHashMap<>();
		
		public Model.Finder<Object,T> getNativeSpecificFinder(String datasource) {
			if(this.nativeVerySpecificFinder==null)return null;
			return findermap.computeIfAbsent(datasource,(k)->new Model.Finder(k, idType, this.cls));
		}

		public Object formatIdToNative(String id) {
			if (Long.class.isAssignableFrom(this.idType)) {
				return Long.parseLong(id);
			} else if (this.idType.isAssignableFrom(UUID.class)) {
				return UUID.fromString(id);
			} else {
				return id;
			}
		}

		public void setWithEbeanIdSetter(Object entity, Object id) {
			if (this.ebeanIdMethodSetter != null) {
				ebeanIdMethodSetter.set(entity, id);
			}
		}

		public void getAndSetEbeanId(Object entity) {
			if (this.ebeanIdMethodSetter != null) {
				this.getNativeIdFor(entity).ifPresent(id -> {
					setWithEbeanIdSetter(entity, id);
				});
			}
		}

		public boolean isParentOrChildOf(EntityInfo<?> ei) {
			return (this.getEntityClass().isAssignableFrom(ei.getEntityClass()) || ei.getEntityClass().isAssignableFrom(this.getEntityClass()));
		}

		public boolean shouldIndex() {
			return shouldIndex;
		}

		// This may be unnecessary now. ID fetching isn't slow
		public CachedSupplier<String> getIdString(Object e) {
			return new CachedSupplier<String>(() -> {
				Optional<?> id = this.getIdPossiblyFromEbeanMethod(e);
				if (id.isPresent())
					return id.get().toString();
				return null;
			});
		}

		public Optional<Tuple<String, String>> getDynamicFacet(Object e) {
			if (this.dynamicLabelField != null && this.dynamicValueField != null) {
				String[] fv = new String[] { null, null };
				this.dynamicLabelField.getValue(e).ifPresent(f -> {
					fv[0] = f.toString();
				});
				this.dynamicValueField.getValue(e).ifPresent(f -> {
					fv[1] = f.toString();
				});
				if (fv[0] != null && fv[1] != null) {
					return Optional.of(new Tuple<String, String>(fv[0], fv[1]));
				} else {
					return Optional.empty();
				}
			}
			return Optional.empty();
		}

		public Optional<MethodOrFieldMeta> getIDFieldInfo() {
			return Optional.ofNullable(this.idField);
		}

		public List<MethodOrFieldMeta> getSequenceFieldInfo() {
			return this.seqFields;
		}

		public List<MethodOrFieldMeta> getStructureFieldInfo() {
			return this.strFields;
		}

		public List<FieldMeta> getTextIndexableFields() {
			return this.fields;
		}

		public List<MethodMeta> getTextIndexableMethods() {
			return this.textMethods;
		}
		
		public List<MethodOrFieldMeta> getJsonSerializableMethodOrFields() {
			return this.jsonFields;
		}
		
		public Optional<MethodOrFieldMeta> getJsonGetterFor(String jsonField){
			Objects.requireNonNull(jsonField);
			MethodOrFieldMeta mofm = jsonGetters.get(jsonField);
			return Optional.ofNullable(mofm);
		}
		
		public Stream<MethodOrFieldMeta> streamJsonGetters(){
			return jsonGetters.values().stream();
		}

		public List<MethodMeta> getMethods() {
			return this.methods;
		}

		public String getName() {
			return this.kind;
		}

		// the hidden _id field stores the field's value
		// in its native type whereas the display field id
		// is used for indexing purposes and as such is
		// represented as a string
		public String getInternalIdField() {
			return kind + ID_FIELD_NATIVE_SUFFIX;
		}

		public String getExternalIdFieldName() {
			return kind + ID_FIELD_STRING_SUFFIX;
		}
		
		
		/**
         * Returns a {@link Tuple} of the kind field and the
         * kind of this object, useful for filtering
         * by this kind in Lucene.
         * @return
         */
		public Tuple<String,String>  getLuceneKindTuple(){
		    return Tuple.of(TextIndexer.FIELD_KIND,this.getName());
		}

		public boolean isEntity() {
			return this.isEntity;
		}

		public boolean ignorePostUpdateHooks() {
			return !shouldDoPostUpdateHooks;
		}

		
		/**
		 * Returns the raw {@link Class} that this {@link EntityInfo} is
		 * wrapping around.
		 * @return
		 */
		public Class<T> getEntityClass() {
			return this.cls;
		}

		public Optional<Object> getNativeIdFor(Object e) {
			if (this.idField != null) {
				return idField.getValue(e);
			}
			return Optional.empty();
		}

		public boolean hasVersion() {
			return (this.versionField != null);
		}

		public Object getVersionFor(Object entity) {
		    if(!this.hasVersion())return null;
		    if(this.versionField==null)return null;
		    return this.versionField.getValue(entity).orElse(null);
		}
		
		public Optional<MethodOrFieldMeta> getVersionField() {
            if(this.hasVersion())return Optional.of(versionField);
            return Optional.empty();
        }
		

		public String getVersionAsStringFor(Object entity) {
			Object o = getVersionFor(entity);
			if (o == null)
				return null;
			return o.toString();
		}

		// It seems that, in certain cases,
		// fetching the ID or some other field directly
		// using reflection (as is done here abstractly)
		// can cause problems for ebean. It may be necessary
		// to explore the method names in rare cases
		private static String getBeanName(String field) {
			return Character.toUpperCase(field.charAt(0)) + field.substring(1);
		}
		
		
		

		/**
		 * This preserves some of the weird checks being done to circumvent
		 * ebean strangeness. I have no way to evaluate when it had been used
		 * before to confirm it gives the same answers.
		 * 
		 * @param o
		 * @return
		 */
		@Deprecated
		public Optional<Object> getIdPossiblyFromEbeanMethod(Object o) {
			Optional<Object> id = this.getNativeIdFor(o);
			if (!id.isPresent()) {
				if (ebeanIdMethod == null)
					return Optional.empty();
				return ebeanIdMethod.getValue(o);
			}
			return id;
		}

		public boolean hasIdField() {
			return (this.idField != null);
		}

		public T findById(String id) {
			//Object nativeId=formatIdToNative(id);
			return (T) this.getFinder().byId(id);
		}
		
		public T findById(String id, String datasource) {
			return (T) this.getFinder(datasource).byId(id);
		}

		public T fromJson(String oldValue) throws JsonParseException, JsonMappingException, IOException {
			return EntityMapper.FULL_ENTITY_MAPPER().readValue(oldValue, this.getEntityClass());
		}

		public T fromJsonNode(JsonNode value) throws JsonProcessingException {
			return EntityMapper.FULL_ENTITY_MAPPER().treeToValue(value, this.getEntityClass());
		}

		private static final <T> EntityInfo<T> of(Class<T> cls) {
			return new EntityInfo<T>(cls);
		}

		public boolean hasBackup() {
			return this.hasBackup;
		}

		public T getInstance() throws Exception{
			return (T) this.getEntityClass().newInstance();
		}

        public String getDatasource() {
           if(this.datasource==null)return "default";
           return datasource;
        }





		// HERE BE DRAGONS!!!!
		// This was one of the (many) ID-generating methods before "The Great Refactoring".
		// I am still unsure whether the explicit call to a Moiety is at all necessary ...
		// 
		// I suspect it was an attempt at making something work, back when we were throwing
		// the kitchen sink at it. I think it's safe with it out, but I'm keeping this here
		// for now, as a warning / reminder to future developers on the dangers of 
		// customizing an especially generic framework.
		//
		// @Deprecated
		// public static Object getId (Object entity) throws Exception {
		// if(entity instanceof Moiety){
		// return ((Moiety)entity).getUUID();
		// }
		// Field f = getIdField (entity);
		// Object id = null;
		// if (f != null) {
		// id = f.get(entity);
		// if (id == null) { // now try bean method
		// try {
		// Method m = entity.getClass().getMethod
		// ("get"+getBeanName (f.getName()));
		// id = m.invoke(entity, new Object[0]);
		// }
		// catch (NoSuchMethodException ex) {
		// ex.printStackTrace();
		// }
		// }
		// }
		// return id;
		// }
		//
	}

	public static interface MethodOrFieldMeta{
		
		public static int JSON_MODIFIER_METHOD = 2;
		public static int JSON_MODIFIER_EXPLICIT = 1;
		

		public Optional<Object> getValue(Object entity);
		
		default BiFunction<ObjectMapper,Object,JsonNode> getValueSerializer(){
			return (om, o)->{
				if(this.getSerializer()!=null){
					try{
						JsonFactory jsf=om.getFactory();
						ByteArrayOutputStream baos = new ByteArrayOutputStream();
						JsonGenerator jg=jsf.createGenerator(baos);
						SerializerProvider sp=new DefaultSerializerProvider.Impl();
						this.getSerializer().serialize(o, jg, sp);
						jg.close();
						return om.readTree(baos.toByteArray());
					}catch(Exception e){
						Logger.warn("Cannot serialize using custom, using default", e);
					}
				}
				return om.valueToTree(o);
			};
		}
		
		public boolean isGenerated();
		public boolean isNumeric();
		public Class<?> getType();
		public String getName();
		public boolean isJsonSerialized();
		public String getJsonFieldName();
		default boolean isArrayOrCollection() {
			return isArray() || isCollection();
		}
		public boolean isArray();
		public boolean isCollection();
		public boolean isTextEnabled();
		public boolean isSequence();
		public boolean isStructure();
		
		public Class<?> deserializeAs();
		public <T> JsonSerializer<T> getSerializer();
		
		
		public int getJsonModifiers();


		// Below are convenience functions that aren't so 
		// convenient.

		// this is a little weird, in that this is meant to consume
		// the very value the ValueMaker already created
		default void forEach(Object value, BiConsumer<Integer, Object> bic) {
			valuesStream(value).forEach(t->bic.accept(t.k(), t.v()));
		}
		default List<Tuple<Integer,Object>> valuesList(Object value) {
			return valuesStream(value).collect(Collectors.toList());
		}
		default Stream<Tuple<Integer,Object>> valuesStream(Object value) {
			Stream<?> s;
			if (isArray()) {
				s = Arrays.stream((Object[]) value);
			} else if (isCollection()) {
				s = ((Collection<?>) value).stream();
			} else {
				throw new IllegalArgumentException("Value must be an array or collection to stream");
			}
			int[] idx = { 0 };
			return s.map(o -> new Tuple<Integer,Object>(idx[0]++, o));
		}
	}
	
	public static class VirtualMapMethodMeta implements MethodOrFieldMeta{

		private String field;
		
		public VirtualMapMethodMeta(String field){
			this.field=field;
		}
		
		public Map asMap(Object o ){
			return (Map)o;
		}
		
		@Override
		public Optional<Object> getValue(Object entity) {
			Object val=asMap(entity).get(field);
			return Optional.ofNullable(val);
		}

		@Override
		public boolean isNumeric() {
			return false;
		}

		@Override
		public Class<?> getType() {
			return Object.class;
		}

		@Override
		public String getName() {
			return field;
		}

		@Override
		public boolean isJsonSerialized() {
			return true;
		}

		@Override
		public String getJsonFieldName() {
			return field;
		}

		@Override
		public boolean isArray() {
			return false;
		}

		@Override
		public boolean isCollection() {
			return false;
		}

		@Override
		public boolean isTextEnabled() {
			return false;
		}

		@Override
		public boolean isSequence() {
			return false;
		}

		@Override
		public boolean isStructure() {
			return false;
		}

		@Override
		public Class<?> deserializeAs() {
			return null;
		}

		@Override
		public <T> JsonSerializer<T> getSerializer() {
			return null;
		}

		@Override
		public int getJsonModifiers() {
			return 0;
		}

		@Override
		public boolean isGenerated() {
			return false;
		}
	}

	public static class MethodMeta implements MethodOrFieldMeta {

		static final MethodHandles.Lookup LOOKUP = MethodHandles.lookup();

		private Method m;
		//Indexable indexable;
		private InstantiatedIndexable instIndexable;
		private boolean textEnabled = false;
		private String indexingName;
		private String name;
		private boolean isStructure = false;
		private boolean isSequence = false;
		private Class<?> type;

		private boolean isArray = false;
		private boolean isCollection = false;
		private boolean isId = false;

		private boolean isSetter = false;
		private boolean isGetter = false;
		private boolean isDataValidatedFlag = false;
		private boolean isChangeReason=false;
		private boolean isJsonIgnore=false;
		private boolean isGeneratedByPlay=false;
		
		private JsonProperty jsonProperty=null;
		private JsonSerialize jsonSerialize=null; 
		private JsonSerializer<?> serializer=null;
		
		private String serializedName = null;
		private String correspondingFieldName=null;
		private Class<?> setterType;

		private Set<Class<?>> hookTypes= new HashSet<Class<?>>();

		private void addHookIf(Class annot){
			if (m.getAnnotation(annot) != null) {
				hookTypes.add(annot);
			}
		}

		private static class MethodHandleHook implements Hook {

			private final String name;
			private final MethodHandle methodHandle;

			public MethodHandleHook(String name, MethodHandle methodHandle) {
				this.name = name;
				this.methodHandle = methodHandle;
			}

			@Override
			public void invoke(Object o) throws Exception {
				try {
					methodHandle.invoke(o);
				} catch (Throwable t) {
					throw new Exception(t.getMessage(), t);
				}
			}

			@Override
			public String getName() {
				return name;
			}
		}

		private CachedSupplier<Hook> hook=CachedSupplier.of(()->createMethodHook());


		public boolean hasHooks(){
			return !hookTypes.isEmpty();
		}
		public Hook getMethodHook(){
			return hook.get();
		}

		private Hook createMethodHook(){
			try {
				return new MethodHandleHook(m.getName(), LOOKUP.unreflect(m));
			} catch (IllegalAccessException e) {
				return null;
			}
		}

		public Set<Class<?>> getHookTypes(){
			return hookTypes;
		}

		public MethodMeta(Method m) {
			Class<?>[] args = m.getParameterTypes();
			this.m = m;
			Indexable indexable = (Indexable) m.getAnnotation(Indexable.class);
			if(indexable!=null){
				instIndexable=new InstantiatedIndexable(indexable);
			}
			name = m.getName();
			indexingName = name;
			//name length check is to skip over the methods that are just called get()
			if (name.startsWith("get") && name.length()>3) {

				indexingName = Character.toLowerCase(name.charAt(3)) + name.substring(4);
				if (args.length == 0) {
					isGetter = true;
					serializedName=indexingName;
					correspondingFieldName=indexingName;

				}
			} else if (name.startsWith("set")) {
				if (args.length == 1) {
					isSetter = true;
					setterType = args[0];
				}
			}
			if (m.getAnnotation(DataValidated.class) != null) {
				this.isDataValidatedFlag = true;
			}
			if (m.getAnnotation(ChangeReason.class) != null) {
				this.isChangeReason = true;
			}
			addHookIf(PrePersist.class);
			addHookIf(PostPersist.class);
			addHookIf(PreUpdate.class);
			addHookIf(PostUpdate.class);
			addHookIf(PreRemove.class);
			addHookIf(PostRemove.class);
			addHookIf(PostLoad.class);
			
			jsonSerialize=m.getAnnotation(JsonSerialize.class);
			if(jsonSerialize!=null){
				if(jsonSerialize.using()!=com.fasterxml.jackson.databind.JsonSerializer.None.class){
					try {
						this.serializer=jsonSerialize.using().newInstance();
					} catch (Exception e) {
						Logger.error(e.getMessage(),e);
					}
				}
			}
			
			if (indexable != null) {
				// we only index no arguments methods
				if (args.length == 0) {
					if (indexable.indexed()) {
						textEnabled = true;
						if (!indexable.name().equals("")) {
							indexingName = indexable.name();
						}
					}

					if (indexable.structure()) {
						isStructure = true;
					}
					if (indexable.sequence()) {
						isSequence = true;
					}
				}
			}
			
			if(m.getName().equals("getClass") 
					|| m.isSynthetic()
					|| m.isBridge() 
					){
				isJsonIgnore=true;
			}
			
			if(m.getAnnotation(play.core.enhancers.PropertiesEnhancer.GeneratedAccessor.class)!=null){
				isGeneratedByPlay=true;
				FieldMeta fm;
				//Get the corresponding field
				try {
					Field f=m.getDeclaringClass().getDeclaredField(correspondingFieldName);
					fm = new FieldMeta(f,null);
					
					isJsonIgnore=!fm.isJsonSerialized();
				} catch (Exception e) {}
			}
			
			if(Modifier.isPrivate(m.getModifiers())){
				isJsonIgnore=true;
			}
			
			if(m.getAnnotation(JsonIgnore.class)!=null){
				isJsonIgnore=true;
			}
			jsonProperty= m.getAnnotation(JsonProperty.class);
			if(jsonProperty!=null){
				if(jsonProperty.value().length()>0){
					serializedName=jsonProperty.value();
				}
			}

			type = m.getReturnType();
			if (Collection.class.isAssignableFrom(type)) {
				isCollection = true;
			} else if (type.isArray()) {
				isArray = true;
			}
			isId = (m.getAnnotation(Id.class) != null);
		}

		public boolean isChangeReason() {
			return isChangeReason;
		}

		public boolean isGetter() {
			return this.isGetter;
		}

		public String getMethodName() {
			return this.name;
		}

		public Class<?> getSetterParameterType() {
			return setterType;
		}
		public boolean isDataValidationFlag() {
			return isDataValidatedFlag;
		}

		/**
		 * This method is called "set", but it really 
		 * just involves the underlying method, passing
		 * the value in as the parameter.
		 * 
		 * Most exceptions will be swallowed here,
		 * and it's not really used right now,
		 * after some deeper refactoring.
		 * 
		 * @param entity
		 * @param val
		 */
		@Deprecated
		public void set(Object entity, Object val) {
			try {
				m.invoke(entity, val);
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		public boolean isId() {
			return isId;
		}

		// Not currently supported
		// Deprecated is a misnomer here, of course
		@Deprecated
		public boolean isDataVersion() {
			return false;
		}



		@Override
		public Optional<Object> getValue(Object entity) {
			try {
				return Optional.ofNullable(m.invoke(entity));
			} catch (Exception e) {
				return Optional.empty();
			}
		}

		@Override
		public boolean isArrayOrCollection() {
			return this.isArray || this.isCollection;
		}

		@Override
		public boolean isArray() {
			return isArray;
		}

		@Override
		public boolean isCollection() {
			return isCollection;
		}

		@Override
		public boolean isTextEnabled() {
			return textEnabled;
		}

		public String getName() {
			return this.indexingName;
		}

		public InstantiatedIndexable getIndexable() {
			return instIndexable;
		}

		@Override
		public boolean isSequence() {
			return this.isSequence;
		}

		@Override
		public boolean isStructure() {
			return this.isStructure;
		}

		@Override
		public Class<?> getType() {
			return type;
		}

		public boolean isSetter() {
			return this.isSetter;
		}

		@Override
		public boolean isNumeric() {
			return type.isAssignableFrom(Long.class);
		}
		@Override
		public boolean isJsonSerialized() {
			if(isJsonIgnore)return false;
			return (isGetter || (jsonProperty!=null && !isSetter));
		}
		@Override
		public String getJsonFieldName() {
			return serializedName;
		}
		@Override
		public int getJsonModifiers() {
			int ret = MethodOrFieldMeta.JSON_MODIFIER_METHOD;
			if(this.jsonProperty!=null)ret|=MethodOrFieldMeta.JSON_MODIFIER_EXPLICIT;
			return ret;
		}

		@Override
		public Class<?> deserializeAs() {
			JsonDeserialize json = (JsonDeserialize) this.m.getAnnotation(JsonDeserialize.class);
			if (json != null) {
				return json.as();
			}else{
				if(jsonSerialize!=null){
					//j.
				}
				return JsonNode.class;
			}
		}
		@Override
		public JsonSerializer<?> getSerializer() {
			
			return this.serializer;
		}
		
		@Override
		public boolean isGenerated() {
			return this.isGeneratedByPlay;
		}

	}

	public static class InstantiatedIndexable{
		Indexable index;
		Pattern p;
		public InstantiatedIndexable(Indexable index){
			this.index=index;
			p = Pattern.compile(index.pathsep());
		}
		public boolean useFullPath() {
			return index.useFullPath();
		}
		public boolean indexed() {
			return index.indexed();
		}
		public boolean sortable() {
			return index.sortable();
		}
		public boolean taxonomy() {
			return index.taxonomy();
		}
		public boolean facet() {
			return index.facet();
		}
		public boolean suggest() {
			return index.suggest();
		}
		public boolean sequence() {
			return index.sequence();
		}
		public boolean structure() {
			return index.structure();
		}
		public boolean fullText() {
			return index.fullText();
		}
		public String pathsep() {
			return index.pathsep();
		}
		public String name() {
			return index.name();
		}
		public long[] ranges() {
			return index.ranges();
		}
		public double[] dranges() {
			return index.dranges();
		}
		public String format() {
			return index.format();
		}
		public boolean recurse() {
			return index.recurse();
		}
		public boolean indexEmpty() {
			return index.indexEmpty();
		}
		public boolean equals(Object obj) {
			return index.equals(obj);
		}
		public String emptyString() {
			return index.emptyString();
		}
		public int hashCode() {
			return index.hashCode();
		}
		public String toString() {
			return index.toString();
		}
		public Class<? extends Annotation> annotationType() {
			return index.annotationType();
		}

		public Pattern getPathSepPattern(){
			return this.p;
		}

		public String[] splitPath(String path){
			return this.p.split(path);
		}
	}

	public static class FieldMeta implements MethodOrFieldMeta {
		private Field f;
		private Indexable indexable;

		private InstantiatedIndexable instIndexable;


		private Class<?> type;
		private String name;

		private boolean textEnabled = true;
		private boolean isID = false;
		private boolean explicitIndexable = true;
		private boolean isPrimitive;
		private boolean isArray;
		private boolean isCollection;
		private boolean isEntityType;
		private boolean isDynaLabel = false;
		private boolean isDynaValue = false;
		private boolean isSequence = false;
		private boolean isStructure = false;
		private boolean isDataVersion = false;
		private boolean isDataValidatedFlag = false;
		private boolean isChangeReason=false;

		private boolean isJsonSerailzed=true;

		private JsonSerialize jsonSerialize=null; 
		private JsonSerializer<?> serializer=null;
		
		private String serializedName;

		JsonProperty jsonProperty=null;
		
		private Column column;

		public boolean isSequence() {
			return this.isSequence;
		}

		public boolean isDataValidationFlag() {
			return isDataValidatedFlag;
		}

		public boolean isUniqueColumn() {
			return (this.isColumn() && this.getColumn().unique());
		}

		public boolean isDataVersion() {
			return isDataVersion;
		}

		public boolean isStructure() {
			return this.isStructure;
		}

		public boolean isColumn() {
			return (this.column != null);
		}

		public String getColumnName() {
			if (this.isColumn()) {
				String cname = this.getColumn().name();
				if (cname != null && !cname.equals("")) {
					return cname;
				}
			}
			return this.getName();
		}

		public Column getColumn() {
			return this.column;
		}

		public FieldMeta(Field f, DynamicFacet dyna) {
			this.f = f;
			this.indexable = f.getAnnotation(Indexable.class);
			this.column = f.getAnnotation(Column.class);
			if (indexable == null) {
				indexable = defaultIndexable;
				explicitIndexable = false;
			}
			instIndexable=new InstantiatedIndexable(indexable);

			int mods = f.getModifiers();
			if (Modifier.isStatic(mods) || Modifier.isPrivate(mods) || f.isSynthetic()) {
				this.isJsonSerailzed=false;
			}
			if (!indexable.indexed() || Modifier.isStatic(mods) || Modifier.isTransient(mods)) {
				textEnabled = false;
			}
			type = f.getType();
			isID = (f.getAnnotation(Id.class) != null);
			isPrimitive = type.isPrimitive();
			isArray = type.isArray();
			isCollection = Collection.class.isAssignableFrom(type);
			name = f.getName();
			serializedName=name;
			if (dyna != null && name.equals(dyna.label())) {
				isDynaLabel = true;
			}
			if (dyna != null && name.equals(dyna.value())) {
				isDynaValue = true;
			}
			if (type.isAnnotationPresent(Entity.class)) {
				this.isEntityType = true;
			}
			if (this.indexable.sequence()) {
				this.isSequence = true;
			}
			if (this.indexable.structure()) {
				this.isStructure = true;
			}
			if (f.getAnnotation(DataVersion.class) != null) {
				this.isDataVersion = true;
			}
			if (f.getAnnotation(DataValidated.class) != null) {
				this.isDataValidatedFlag = true;
			}
			if (f.getAnnotation(ChangeReason.class) != null) {
				this.isChangeReason = true;
			}
			if (f.getAnnotation(JsonIgnore.class) != null) {
				this.isJsonSerailzed = false;
			}
			jsonProperty= f.getAnnotation(JsonProperty.class);
			if(jsonProperty!=null){
				if(jsonProperty.value().length()>0){
					serializedName=jsonProperty.value();
				}
			}
			jsonSerialize=f.getAnnotation(JsonSerialize.class);
			if(jsonSerialize!=null){
				if(jsonSerialize.using()!=com.fasterxml.jackson.databind.JsonSerializer.None.class){
					try {
						this.serializer=jsonSerialize.using().newInstance();
					} catch (Exception e) {
						Logger.error(e.getMessage(),e);
					}
				}
			}
		}

		public boolean isChangeReason(){
			return this.isChangeReason;
		}

		public boolean isExplicitlyIndexable() {
			return explicitIndexable;
		}

		public boolean isId() {
			return isID;
		}

		public boolean isPrimitive() {
			return isPrimitive;
		}



		public InstantiatedIndexable getIndexable() {
			return instIndexable;
		}

		public Optional<Object> getValue(Object entity) {
			try {
				return Optional.ofNullable(f.get(entity));
			} catch (Exception e) {
				return Optional.empty();
			}
		}

		public boolean isTextEnabled() {
			return this.textEnabled;
		}

		public String getName() {
			return name;
		}

		public Class<?> getType() {
			return type;
		}

		public boolean isEntityType() {
			return isEntityType;
		}

		public boolean isArrayOrCollection() {
			return this.isArray || this.isCollection;
		}

		public boolean isDynamicFacetLabel() {
			return isDynaLabel;
		}

		public boolean isDynamicFacetValue() {
			return isDynaValue;
		}

		@Override
		public boolean isArray() {
			return this.isArray;
		}

		@Override
		public boolean isCollection() {
			return this.isCollection;
		}

		@Override
		public boolean isNumeric() {
			return Number.class.isAssignableFrom(this.type);
		}
		
		@Override
		public boolean isJsonSerialized() {
			return this.isJsonSerailzed;
		}

		@Override
		public String getJsonFieldName() {
			return serializedName;
		}
		
		
		@Override
		public int getJsonModifiers() {
			int ret = 0;
			if(this.jsonProperty!=null)ret|=MethodOrFieldMeta.JSON_MODIFIER_EXPLICIT;
			return ret;
		}

		@Override
		public Class<?> deserializeAs() {
			JsonDeserialize json = (JsonDeserialize) this.f.getAnnotation(JsonDeserialize.class);
			if (json != null) {
				return json.as();
			}else{
				return JsonNode.class;
			}
		}

		@Override
		public JsonSerializer<?> getSerializer() {
			return this.serializer;
		}

		@Override
		public boolean isGenerated() {
			return false;
		}
		public boolean isDate() {
            return Date.class.isAssignableFrom(this.type);
        }
	}

	public static class Key {
		private EntityInfo<?> kind;
		private Object _id; 

		private Key(EntityInfo<?> k, Object id) {
			this.kind = k;
			this._id = id;
		}

		public String getKind() {
			return this.kind.getName();
		}

		public Object getIdNative() {
			return this._id;
		}

		public String getIdString() {
			return this._id.toString();
		}

		public EntityInfo<?> getEntityInfo() {
			return kind;
		}

		@Override
		public String toString() {
			return kind.getName() + ID_FIELD_NATIVE_SUFFIX + ":" + getIdString();
		}

		/**
		 * Returns null if not present
		 * @return
		 */
		@SuppressWarnings("unchecked")
		private Object nativeFetch(){
			return kind.getFinder().byId(this.getIdNative());
		}
		
		/**
		 * Returns null if not present
		 * @return
		 */
		@SuppressWarnings("unchecked")
		private Object nativeFetch(String datasource){
			return kind.getFinder(datasource).byId(this.getIdNative());
		}


		// fetches from finder
		public Optional<EntityWrapper<?>> fetch() {
			if(ds!=null)return fetch(ds);
			Object o=nativeFetch();
			if(o==null)return Optional.empty();
			return Optional.of(EntityWrapper.of(o));
		}

		// fetches from finder
		public Optional<EntityWrapper<?>> fetch(String datasource) {
			Object o=nativeFetch(datasource);
			if(o==null)return Optional.empty();
			return Optional.of(EntityWrapper.of(o));
		}


		/**
		 * Returns a {@link Tuple} of the ID field and ID 
		 * as a string, that would be used to retrieve this
		 * entity from the lucene store.
		 * @return
		 */
		public Tuple<String, String> asLuceneIdTuple() {
			return new Tuple<String, String>(kind.getExternalIdFieldName(), this.getIdString());
		}

		public static Key of(EntityInfo<?> meta, Object id) {
			return new Key(meta, id);
		}

		// For lucene document
		public static Key of(Document doc) throws Exception {
			// TODO: This should be moved to somewhere more Abstract, probably
			String kind = doc.getField(TextIndexer.FIELD_KIND).stringValue();
			EntityInfo<?> ei = EntityUtils.getEntityInfoFor(kind);
			if(ei.hasIdField()){
				if (ei.hasLongId()) {
					Long id = doc.getField(ei.getInternalIdField()).numericValue().longValue();
					return new Key(ei, id);
				} else {
					String id = doc.getField(ei.getInternalIdField()).stringValue();
					return new Key(ei, id);
				}
			}else{
				throw new NoSuchElementException("Entity:" + kind + " has no ID field");
			}
		}

		// For EntityWrapper (weird place for this, I know)
		public static Key of(EntityWrapper ew) throws NoSuchElementException {
			Objects.requireNonNull(ew);
			return new Key(ew.getEntityInfo(), ew.getId().get());
		}

		@Override
		public int hashCode() {
			return this.toString().hashCode(); // Probably something that can be
			// better
		}

		@Override
		public boolean equals(Object k) {
			if (k == null || !(k instanceof Key)) {
				return false;
			} else {
				return this.toString().equals(k.toString()); // Probably
				// something
				// better that
				// could be done
			}
		}
		
		
		public String asResourcePath(){
		    return Global.getRef(kind.getEntityClass(), this._id);
		}

        public static <T> Key of(Class<T> class1, Object id) {
            EntityInfo<T> emeta=EntityUtils.getEntityInfoFor(class1);
            return of(emeta,id);
        }

        
        public EntityFetcher getFetcher() throws Exception{
            return EntityFetcher.of(this);
        }
        
        public EntityFetcher getFetcher(CacheType ct) throws Exception{
            return EntityFetcher.of(this, ct);
        }
        
        public EntityFetcher getFetcher(String datasource) throws Exception{
        	Key dupe=Key.of(this.getEntityInfo(), this.getIdNative());
        	dupe.setDefaultDS(datasource);
        	
            return EntityFetcher.of(dupe);
        }
        
        public EntityFetcher getFetcher(String datasource,CacheType ct) throws Exception{
        	Key dupe=Key.of(this.getEntityInfo(), this.getIdNative());
        	dupe.setDefaultDS(datasource);
            return EntityFetcher.of(dupe, ct);
        }
        
        
        private String ds = null;
        private void setDefaultDS(String datasource){
        	this.ds=datasource;
        }
	}

	//Not sure how this should be parameterized
	public static class EntityTraverser {
		private PathStack path;
		private BiConsumer<PathStack, EntityWrapper> listens = null;
		LinkedReferenceSet<Object> prevEntities; // protect against infinite
		// recursion
		EntityWrapper estart; // seed
		private static final Integer DEFAULT_MAX_DEPTH = 10; // should be good enough for anybody

		public EntityTraverser() {
			path = new PathStack();
			path.setMaxDepth(DEFAULT_MAX_DEPTH);
			prevEntities = new LinkedReferenceSet<Object>();
		}

		public EntityTraverser using(EntityWrapper e1) {
			this.estart = e1;
			return this;
		}

		public EntityTraverser maxDepth(Integer maxDepth){
			path.setMaxDepth(maxDepth);
			return this;
		}

		public void execute(BiConsumer<PathStack, EntityWrapper> listens) {
			this.listens = listens;
			next(estart);
		}

		private void next(EntityWrapper<?> ew) {
			instrument(ew);
		}

		// not thread safe at all. Never call this directly
		private void instrument(EntityWrapper<?> ew) {
			if(!prevEntities.contains(ew.getValue())){
				if (this.listens != null){
					listens.accept(path, ew); // listener caller
				}
				//May 18, 2018, Clinical Trial Testing purpose
				/*System.out.println(prevEntities.asStream()
						.map(o->EntityWrapper.of(o))
						.map(ew1 -> ew1.getOptionalKey())
						.map(k -> k.toString())
						.collect(Collectors.joining("->")));
                  */

				prevEntities.pushAndPopWith(ew.getValue(), () -> {
					//ALL collections and arrays are recursed
					//it doesn't matter if they are entities or not
					ew.streamFieldsAndValues(f -> f.isArrayOrCollection()).forEach(fi -> {
						path.pushAndPopWith(fi.k().getName(), () -> {
							fi.k().forEach(fi.v(), (i, o) -> {
								path.pushAndPopWith(String.valueOf(i), () -> {
									next(EntityWrapper.of(o));
								});
							});
						});
					});

					//only Entities are recursed for non-arrays
					ew.streamFieldsAndValues(EntityInfo::isPlainOldEntityField).forEach(fi -> {
						path.pushAndPopWith(fi.k().getName(), () -> {
							next(EntityWrapper.of(fi.v()));
						});
					});

					ew.streamMethodsAndValues(m -> m.isArrayOrCollection()).forEach(t -> {
						path.pushAndPopWith(t.k().getName(), () -> {
							t.k().forEach(t.v(), (i, o) -> {
								path.pushAndPopWith(String.valueOf(i), () -> {
									next(EntityWrapper.of(o));
								});
							});
						});
					});// each array / collection

					ew.streamMethodsAndValues(m -> !m.isArrayOrCollection()).forEach(t -> {
						path.pushAndPopWith(t.k().getName(), () -> {
							next(EntityWrapper.of(t.v()));
						});
					});// each non-array

				});
			}
		}
	}
	public static interface Hook{
		public void invoke(Object o) throws Exception;
		default String getName(){
			return "Unnamed hook";
		}
	}

}
