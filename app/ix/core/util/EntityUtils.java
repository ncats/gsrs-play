package ix.core.util;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.Table;

import org.apache.lucene.document.Document;
import org.reflections.Reflections;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import ix.core.IgnoredModel;
import ix.core.controllers.EntityFactory;
import ix.core.controllers.EntityFactory.EntityMapper;
import ix.core.models.Backup;
import ix.core.models.ChangeReason;
import ix.core.models.DataValidated;
import ix.core.models.DataVersion;
import ix.core.models.DynamicFacet;
import ix.core.models.Edit;
import ix.core.models.Indexable;
import ix.core.models.Keyword;
import ix.core.search.text.IndexValueMaker;
import ix.core.search.text.PathStack;
import ix.core.search.text.TextIndexer;
import ix.ginas.models.v1.VocabularyTerm;
import ix.utils.LinkedReferenceSet;
import ix.utils.Tuple;
import play.Logger;
import play.db.ebean.Model;
import play.db.ebean.Model.Finder;


import play.Play;
/**
 * A utility class, mostly intended to do the grunt work of reflection.
 * 
 * @author peryeata
 */
public class EntityUtils {

	private static final String ID_FIELD_NATIVE_SUFFIX = "._id";
	private static final String ID_FIELD_STRING_SUFFIX = ".id";

	private final static Map<String, EntityInfo<?>> infoCache = new ConcurrentHashMap<String, EntityInfo<?>>();

	@Indexable // put default indexable things here
	static final class DefaultIndexable {

	}

	private static final Indexable defaultIndexable = (Indexable) DefaultIndexable.class.getAnnotation(Indexable.class);

	public static <T> EntityInfo<T> getEntityInfoFor(Class<T> cls) {
		return (EntityInfo<T>) infoCache.computeIfAbsent(cls.getName(), k -> new EntityInfo<>(cls));
	}

	public static <T> EntityInfo<T> getEntityInfoFor(T entity) {
		return getEntityInfoFor((Class<T>) entity.getClass());
	}

	public static EntityInfo<?> getEntityInfoFor(String className) throws ClassNotFoundException {
		EntityInfo<?> e1 = infoCache.computeIfAbsent(className, k -> {
			try {
				return EntityInfo.of(Class.forName(k));
			} catch (Exception e) {
				Logger.error("No class found with name:" + className);
			}
			return null;
		});
		if (e1 == null) {
			throw new ClassNotFoundException(className);
		}
		return e1;
	}

	/**
	 * This is a helper class used extensively to help mitigate some of the
	 * drawbacks of using generic objects in much of the deepest areas of the
	 * code.
	 * 
	 * Information on the methods, fiends and annotations related to this class
	 * are memoized via a static ConcurrentHashMap.
	 * 
	 * Wrapping an entity in this constructor will give access to some
	 * convenience methods that can be especially useful for finding smaller
	 * sets of known indexable values from all fields.
	 * 
	 * The method {{EntityWrapper{@link #traverse()} is particularly useful for
	 * building {{@link EntityTraverser}}s, which can allow for quick probing
	 * of all of the entity descendants.
	 * 
	 * TODO there is some inconsistent design and type-safe issues in this
	 * current instantiation
	 * 
	 * @author peryeata
	 *
	 * @param <K>
	 */
	public static class EntityWrapper<T> {
		private T _k;
		EntityInfo<T> ei;

		public static <T> EntityWrapper<T> of(T bean) {
			Objects.requireNonNull(bean);
			if (bean instanceof EntityWrapper) {
				return (EntityWrapper<T>) bean;
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

		public String toInternalJson() {
			return EntityMapper.INTERNAL_ENTITY_MAPPER().toJson(getValue());
		}

		public String toFullJson() {
			return EntityMapper.FULL_ENTITY_MAPPER().toJson(getValue());
		}
		
		public JsonNode toFullJsonNode() {
			return EntityMapper.FULL_ENTITY_MAPPER().valueToTree(getValue());
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

		public Finder getFinder() {
			return ei.getFinder();
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

		public Stream<Tuple<MethodOrFieldMeta, Object>> streamSequenceFieldAndValues(Predicate<MethodOrFieldMeta> p) {
			return (ei).getSequenceFieldInfo().stream().filter(p).map(m -> new Tuple<>(m, m.getValue(this.getValue())))
					.filter(t -> t.v().isPresent()).map(t -> new Tuple<>(t.k(), t.v().get()));
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
			return ei.getFieldInfo();
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
			return ei.getFieldInfo().stream()
					.filter(p)
					.map(f -> new Tuple<>(f, f.getValue(bean)))
					.filter(t -> t.v().isPresent())
					.map(t -> new Tuple<>(t.k(), t.v().get()));
		}

		public List<MethodMeta> getMethodInfo() {
			return ei.getMethodInfo();
		}

		public Stream<Tuple<MethodMeta, Object>> streamMethodsAndValues(Predicate<MethodMeta> p) {
			return ei.getMethodInfo().stream().filter(p).map(m -> new Tuple<>(m, m.getValue(this.getValue())))
					.filter(t -> t.v().isPresent()).map(t -> new Tuple<>(t.k(), t.v().get()));
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

		public boolean ignorePostUpdateHooks() {
			return ei.ignorePostUpdateHooks();
		}

		public Class<?> getClazz() {
			return ei.getClazz();
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
				return EntityFactory.getEdits(this.ei.getNativeIdFor(this._k).get(), 
					this.ei.getInherittedRootEntityInfo().getTypeAndSubTypes()
					.stream()
					.map(em->em.getClazz())
					.toArray(len->new Class<?>[len]));
			}else{
				return new ArrayList<Edit>();
			}
		}
		
		public Object getValueFromMethod(String name){
			return this.streamMethodsAndValues(m->m.getMethodName().equals(name)).findFirst().get().v();
		}
		
	}

	public static class EntityInfo<T> {
		final Class<T> cls;
		final String kind;
		final DynamicFacet dyna;
		final Indexable indexable;
		private List<FieldMeta> fields;
		Table table;
		List<MethodOrFieldMeta> seqFields = new ArrayList<MethodOrFieldMeta>();
		List<MethodOrFieldMeta> strFields = new ArrayList<MethodOrFieldMeta>();;

		List<String> sponsoredFields = new ArrayList<>();

		public List<String> getSponsoredFields() {
			return sponsoredFields;
		}

		List<MethodMeta> methods;
		List<MethodMeta> keywordFacetMethods;

		List<FieldMeta> uniqueColumnFields;

		MethodOrFieldMeta changeReasonField = null;
		MethodOrFieldMeta versionField = null;
		MethodOrFieldMeta validatedField = null;
		MethodOrFieldMeta idField = null;

		MethodOrFieldMeta ebeanIdMethod = null;

		MethodMeta ebeanIdMethodSetter = null;

		FieldMeta dynamicLabelField = null;
		FieldMeta dynamicValueField = null;

		volatile boolean isEntity = false;
		volatile boolean shouldIndex = true;
		volatile boolean shouldDoPostUpdateHooks = true;
		volatile boolean hasUniqueColumns = false;
		String ebeanIdMethodName = null;

		String tableName = null;

		Class<?> idType = null;

		Model.Finder<?, T> nativeVerySpecificfinder;

		boolean isIdNumeric = false;
		Inheritance inherits;
		boolean isIgnoredModel = false;

		boolean hasBackup = false;
		EntityInfo<?> ancestorInherit;

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

		Supplier<Set<EntityInfo<? extends T>>> forLater;
		
		
		public EntityInfo(Class<T> cls) {
			Objects.requireNonNull(cls);

			this.cls = cls;
			this.hasBackup = (cls.getAnnotation(Backup.class) != null);
			
			this.isIgnoredModel = (cls.getAnnotation(IgnoredModel.class) != null);
			this.indexable = (Indexable) cls.getAnnotation(Indexable.class);
			this.table = (Table) cls.getAnnotation(Table.class);
			this.inherits = (Inheritance) cls.getAnnotation(Inheritance.class);
			ancestorInherit = this;
			if (this.table != null) {
				tableName = table.name();
			} else if (this.inherits != null) {
				EntityInfo<?> ei = EntityUtils.getEntityInfoFor(cls.getSuperclass());
				tableName = ei.getTableName();
				table = ei.table;
				ancestorInherit = ei.ancestorInherit;
			}

			kind = cls.getName();
			// ixFields.add(new FacetField(DIM_CLASS, kind));
			dyna = (DynamicFacet) cls.getAnnotation(DynamicFacet.class);
			fields = Arrays.stream(cls.getFields()).map(f2 -> new FieldMeta(f2, dyna)).filter(f -> {
				if (f.isId()) {
					idField = f;
					return false;
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
				return true;
			}).collect(Collectors.toList());

			uniqueColumnFields = fields.stream().filter(f -> f.isUniqueColumn()).collect(Collectors.toList());

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

			if (cls.isAnnotationPresent(Entity.class)) {
				isEntity = true;
				if (indexable != null && !indexable.indexed()) {
					shouldIndex = false;
				}
			}
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
					nativeVerySpecificfinder = new Model.Finder(idType, this.cls);
				}
			}

			methods.removeIf(m -> !m.isTextEnabled());

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






			Map m = (Map)Play.application().configuration().getObject("ix.core.exactsearchfields",null);
			if(m!=null){



				m.forEach((k,v)->{
								if(k.equals(EntityInfo.this.kind)) {
									List<String> fields = (List<String>) v;
									for (String f : fields) {
										sponsoredFields.add(f);
									}
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

		public Class<?> getIdType() {
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

		public Model.Finder getFinder() {
			return this.getInherittedRootEntityInfo().getNativeSpecificFinder();
		}

		public Model.Finder getNativeSpecificFinder() {
			return this.nativeVerySpecificfinder;
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
			return (this.getClazz().isAssignableFrom(ei.getClazz()) || ei.getClazz().isAssignableFrom(this.getClazz()));
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

		public List<FieldMeta> getFieldInfo() {
			return this.fields;
		}

		public List<MethodMeta> getMethodInfo() {
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

		public boolean isEntity() {
			return this.isEntity;
		}

		public boolean ignorePostUpdateHooks() {
			return !shouldDoPostUpdateHooks;
		}

		public Class<T> getClazz() {
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
			return this.versionField.getValue(entity).orElseGet(null);
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

		public T fromJson(String oldValue) throws JsonParseException, JsonMappingException, IOException {
			return EntityMapper.FULL_ENTITY_MAPPER().readValue(oldValue, this.getClazz());
		}

		private static final <T> EntityInfo<T> of(Class<T> cls) {
			return new EntityInfo<T>(cls);
		}

		public boolean hasBackup() {
			return this.hasBackup;
		}

		public T getInstance() throws Exception{
			return (T) this.getClazz().newInstance();
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
		public Optional<Object> getValue(Object entity);
		public boolean isNumeric();
		public Class<?> getType();
		public String getName();
		default boolean isArrayOrCollection() {
			return isArray() || isCollection();
		}
		public boolean isArray();
		public boolean isCollection();
		public boolean isTextEnabled();
		public boolean isSequence();
		public boolean isStructure();

		
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

	public static class MethodMeta implements MethodOrFieldMeta {

		Method m;
		//Indexable indexable;
		InstantiatedIndexable instIndexable;
		boolean textEnabled = false;
		String indexingName;
		String name;
		boolean isStructure = false;
		boolean isSequence = false;
		Class<?> type;

		boolean isArray = false;
		boolean isCollection = false;
		boolean isId = false;

		boolean isSetter = false;
		boolean isGetter = false;
		boolean isDataValidatedFlag = false;
		boolean isChangeReason=false;
		Class<?> setterType;

		public MethodMeta(Method m) {
			Class<?>[] args = m.getParameterTypes();
			this.m = m;
			Indexable indexable = (Indexable) m.getAnnotation(Indexable.class);
			if(indexable!=null){
				instIndexable=new InstantiatedIndexable(indexable);
			}
			name = m.getName();
			indexingName = name;
			if (name.startsWith("get")) {
				indexingName = name.substring(3);
				if (args.length == 0) {
					isGetter = true;
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

	}

	public static class InstantiatedIndexable{
		Indexable index;
		Pattern p;
		public InstantiatedIndexable(Indexable index){
			this.index=index;
			p = Pattern.compile(index.pathsep());
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
		Field f;
		Indexable indexable;
		
		InstantiatedIndexable instIndexable;
		

		Class<?> type;
		String name;

		boolean textEnabled = true;
		boolean isID = false;
		boolean explicitIndexable = true;
		boolean isPrimitive;
		boolean isArray;
		boolean isCollection;
		boolean isEntityType;
		boolean isDynaLabel = false;
		boolean isDynaValue = false;
		boolean isSequence = false;
		boolean isStructure = false;
		boolean isDataVersion = false;
		boolean isDataValidatedFlag = false;
		boolean isChangeReason=false;

		Column column;
		
		List<VocabularyTerm> possibleTerms = new ArrayList<VocabularyTerm>();
		
		
		

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
			if (!indexable.indexed() || Modifier.isStatic(mods) || Modifier.isTransient(mods)) {
				textEnabled = false;
			}
			type = f.getType();
			isID = (f.getAnnotation(Id.class) != null);
			isPrimitive = type.isPrimitive();
			isArray = type.isArray();
			isCollection = Collection.class.isAssignableFrom(type);
			name = f.getName();
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
//		
//		public List<VocabularyTerm> getPossibleTerms(){
//			
//			return this.possibleTerms;
//		}
//		
//		//TODO: Implement this
//		public boolean isControlled(){
//			return false;
//		}
//		
	}

	public static class Key {
		private EntityInfo kind;
		private Object _id; 

		private Key(EntityInfo k, Object id) {
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

		public EntityInfo getEntityInfo() {
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
		private Object nativeFetch(){
			return kind.getFinder().byId(this.getIdNative());
		}
		
		
		// fetches from finder
		public Optional<EntityWrapper> fetch() {
			Object o=nativeFetch();
			if(o==null)return Optional.empty();
			return Optional.of(EntityWrapper.of(o));
		}
		

		public Tuple<String, String> asLuceneIdTuple() {
			return new Tuple<String, String>(kind.getInternalIdField(), this.getIdString());
		}

		public static Key of(EntityInfo meta, Object id) {
			return new Key(meta, id);
		}

		// For lucene document
		public static Key of(Document doc) throws Exception {
			 // TODO: This should be moved to somewhere more Abstract, probably
			String kind = doc.getField(TextIndexer.FIELD_KIND).stringValue();
			EntityInfo ei = EntityUtils.getEntityInfoFor(kind);
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



		

	}

	//Not sure how this should be paramaterized
	public static class EntityTraverser {
		private PathStack path;
		private BiConsumer<PathStack, EntityWrapper> listens = null;
		LinkedReferenceSet<Object> prevEntities; // protect against infinite
											     // recursion
		EntityWrapper estart; // seed
		
		public EntityTraverser() {
			path = new PathStack();
			prevEntities = new LinkedReferenceSet<Object>();
		}

		public EntityTraverser using(EntityWrapper e1) {
			this.estart = e1;
			return this;
		}

		public void execute(BiConsumer<PathStack, EntityWrapper> listens) {
			this.listens = listens;
			next(estart);
		}
		
		private void next(EntityWrapper<?> ew) {
			if (this.listens != null)
				listens.accept(path, ew); // actual call happens here!
			instrument(ew);
		}

		// not thread safe at all. Never call this directly
		private void instrument(EntityWrapper<?> ew) {
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
				
				
			});
		}
	}

}
