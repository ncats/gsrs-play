package ix.core.search.text;

import static org.apache.lucene.document.Field.Store.NO;
import static org.apache.lucene.document.Field.Store.YES;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.LongField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.facet.FacetField;
import org.apache.lucene.index.IndexableField;
import org.reflections.Reflections;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import ix.core.models.DataVersion;
import ix.core.models.DynamicFacet;
import ix.core.models.Edit;
import ix.core.models.Indexable;
import ix.core.util.CachedSupplier;
import ix.utils.LinkedReferenceSet;
import ix.utils.Tuple;
import play.Logger;
import play.db.ebean.Model;
import play.db.ebean.Model.Finder;

public class EntityUtils {
	private final static Map<String, EntityInfo> infoCache = new ConcurrentHashMap<String, EntityInfo>();
	
	@Indexable //put default indexable things here
	static final class DefaultIndexable {
		
		
	}
	private static final Indexable defaultIndexable = (Indexable) DefaultIndexable.class.getAnnotation(Indexable.class);
	
	
	public static EntityInfo getEntityInfoFor(Class<?> cls){
		return infoCache.computeIfAbsent(cls.getName(), k-> new EntityInfo(cls));
	}
	
	public static EntityInfo getEntityInfoFor(Object entity){
		return getEntityInfoFor(entity.getClass());
	}
	
	public static EntityInfo getEntityInfoFor(String className) throws ClassNotFoundException{
		EntityInfo e1= infoCache.computeIfAbsent(className, k->{ 
			try{
				return new EntityInfo(Class.forName(k));
			}catch(Exception e){
				Logger.error("No class found with name:" + className);
			}
			return null;
		});
		if(e1 == null){
			throw new ClassNotFoundException(className);
		}
		return e1;
	}
	
	/**
	 * This is a helper class used extensively to help mitigate
	 * some of the drawbacks of using generic objects in much of
	 * the deepest areas of the code. 
	 * 
	 * Information on the methods, fiends and annotations related
	 * to this class are memoized via a static ConcurrentHashMap.
	 * 
	 * Wrapping an entity in this constructor will give access
	 * to some convenience methods that can be especially useful
	 * for finding smaller sets of known indexable values from 
	 * all fields. 
	 * 
	 * The method {{EntityWrapper{@link #analyze()} is particularly
	 * useful for building {{@link EntityAnalyzer}}s, which 
	 * can allow for quick traverse of all of the entity descendants
	 * 
	 * TODO there is some inconsistent design and type-safe
	 * issues in this current instantiation
	 * 
	 * @author peryeata
	 *
	 * @param <K>
	 */
	public static class EntityWrapper<K>{
		K _k;
		EntityInfo ei;
		
		public static EntityWrapper of(Object bean){
			if(bean instanceof EntityWrapper){
				return (EntityWrapper)bean;
			}
			return new EntityWrapper(bean);
		}
		
		public EntityWrapper(K o){
			Objects.requireNonNull(o);
			this._k=o;
			ei = getEntityInfoFor(o);
		}
		
		//Useful for doing recursive searches, etc
		public EntityAnalyzer analyze(){
			return new EntityAnalyzer()
						.using(this);
		}
		
		public String toString(){
			return this.getKind() +":" +  this.getValue().toString();
		}
		
		/**
		 * Throws exception if the id is null.
		 * @return
		 */
		public String getGlobalKey(){
			return ei.uniqueKeyWithId(this.getId().get());
		}
		public List<FieldInfo> getUniqueColumns() {
			return ei.getUniqueColumns();
		}
		public Finder getFinder() {
			return ei.getFinder();
		}
		public boolean shouldIndex() {
			return ei.shouldIndex();
		}
		public Optional<ValueMakerInfo> getIdFieldInfo() {
			return ei.getIDFieldInfo();
		}
		public Stream<Tuple<ValueMakerInfo, Object>> streamSequenceFieldAndValues(Predicate<ValueMakerInfo> p) {
			return ei.getSequenceFieldInfo().stream().filter(p)
					.map(m -> new Tuple<>(m, m.getValue(this.getValue())))
					.filter(t -> t.v().isPresent())
					.map(t -> new Tuple<>(t.k(), t.v().get()));
		}
		
		public Stream<Tuple<ValueMakerInfo, Object>> streamStructureFieldAndValues(Predicate<ValueMakerInfo> p) {
			return ei.getStructureFieldInfo().stream().filter(p)
					.map(m -> new Tuple<>(m, m.getValue(this.getValue())))
					.filter(t -> t.v().isPresent())
					.map(t -> new Tuple<>(t.k(), t.v().get()));
		}
		public List<ValueMakerInfo> getStructureFieldAndValues() {
			return ei.getStructureFieldInfo();
		}
		public List<FieldInfo> getFieldInfo() {
			return ei.getFieldInfo();
		}

		public Stream<Tuple<FieldInfo, Object>> streamFieldsAndValues(Predicate<FieldInfo> p) {
			return ei.getFieldInfo().stream().filter(p)
					.map(m -> new Tuple<>(m, m.getValue(this.getValue())))
					.filter(t -> t.v().isPresent())
					.map(t -> new Tuple<>(t.k(), t.v().get()));
		}
		public List<MethodInfo> getMethodInfo() {
			return ei.getMethodInfo();
		}
		public Stream<Tuple<MethodInfo, Object>> streamMethodsAndValues(Predicate<MethodInfo> p) {
			return ei.getMethodInfo().stream().filter(p)
							.map(m -> new Tuple<>(m, m.getValue(this.getValue())))
							.filter(t -> t.v().isPresent())
							.map(t -> new Tuple<>(t.k(), t.v().get()));
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
		
		public EntityInfo getEntityInfo(){
			return this.ei;
		}

		public Tuple<String, String> getIdAndFieldName() {
			return ei.getFieldAndId(this.getValue());
		}
		
		public boolean hasLongId(){
			if(this.ei.hasLongId()){
				return true;
			}else{
				return false;
			}
		}
		public Optional<Long> getLongId(){
			if(this.hasLongId()){
				return Optional.of((Long)this.getId().get());
			}
			return Optional.empty();
		}
		public Optional<?> getId(){
			return this.ei.getIdPossiblyFromEbeanMethod((Object)this.getValue());
		}
		public String getIdAsString(){
			return this.ei.getIdString(this.getValue()).get();
		}
		
		public String getKind() {
			return this.ei.getName();
		}
		public Optional<String> getVersion() {
			return Optional.ofNullable(ei.getVersionAsStringFor(this.getValue()));
		}
		
		public K getValue() {
			return this._k;
		}
		
		public JsonNode toJson(ObjectMapper om) {
			return om.valueToTree(this.getValue());
		}
		
		
		public Optional<Tuple<String,String>> getDynamicFacet() {
			return this.ei.getDynamicFacet(this.getValue());
		}
	}
	
	public static class EntityInfo {
		final Class<?> cls;
		final String kind;
		final DynamicFacet dyna;
		final Indexable indexable;
		List<FieldInfo> fields;
		Table table;
		List<ValueMakerInfo> seqFields = new ArrayList<ValueMakerInfo>();
		List<ValueMakerInfo> strFields = new ArrayList<ValueMakerInfo>();;
		
		List<MethodInfo> methods;
		
		List<FieldInfo> uniqueColumnFields;
		
		
		ValueMakerInfo versionField=null;
		ValueMakerInfo idField=null;
		
		ValueMakerInfo ebeanIdMethod=null;
		
		MethodInfo ebeanIdMethodSetter=null;
		
		FieldInfo dynamicLabelField=null;
		FieldInfo dynamicValueField=null;
		
		volatile boolean isEntity=false;
		volatile boolean shouldIndex=true;
		volatile boolean shouldPostUpdateHooks = true;
		volatile boolean hasUniqueColumns = false;
		volatile String ebeanIdMethodName=null;
		
		String tableName = null;
		
		Class<?> idType=null;
		
		Model.Finder finder;
		boolean isIdNumeric=false;
		
		public EntityInfo(Class<?> cls) {
			Objects.requireNonNull(cls);
			
			this.cls = cls;
			this.indexable = (Indexable) cls.getAnnotation(Indexable.class);
			this.table = (Table) cls.getAnnotation(Table.class);
			if(this.table!=null){
				tableName = table.name();
			}
			kind = cls.getName();
			// ixFields.add(new FacetField(DIM_CLASS, kind));
			dyna = (DynamicFacet) cls.getAnnotation(DynamicFacet.class);
			fields = Arrays.stream(cls.getFields()).map(f2 -> new FieldInfo(f2, dyna))
					.filter(f -> {
						if(f.isId()){
							idField=f;
							return false;
						}else if(f.isDynamicFacetLabel()){
							dynamicLabelField=f;
						}else if(f.isDynamicFacetValue()){
							dynamicValueField=f;
						}
						
						if(f.isDataVersion()){
							versionField=f;
						}
						return true;
					}).collect(Collectors.toList());
			uniqueColumnFields=fields.stream()
					.filter(f->f.isUniqueColumn())
					.collect(Collectors.toList());
			
			methods = Arrays.stream(cls.getMethods())
					.map(m2 -> new MethodInfo(m2))
					.peek(m -> {
							if(m.isDataVersion()){
								versionField=m;	
							}else if(m.isId()){
								//always choose method IDs over
								//field IDs
								idField=m;
							}
						})
					.collect(Collectors.toList());
			
			seqFields = Stream.concat(fields.stream(), methods.stream()).filter(f->f.isSequence()).collect(Collectors.toList());
			strFields = Stream.concat(fields.stream(), methods.stream()).filter(f->f.isStructure()).collect(Collectors.toList());
			
			fields.removeIf(f->!f.isTextEnabled());
			
			if (cls.isAnnotationPresent(Entity.class)) {
				isEntity=true;
				if(indexable != null && !indexable.indexed()){
					shouldIndex=false;
				}
		    }
			if(Edit.class.isAssignableFrom(cls)){
				shouldPostUpdateHooks=false;
			}
			
			
			
			if(idField!=null){
				ebeanIdMethodName = getBeanName (idField.getName());
				methods.stream()
						.filter(m->(m!=idField))
						.filter(m->m.isGetter())
						.filter(m->m.getMethodName().equalsIgnoreCase("get" + ebeanIdMethodName))
						.findAny()
						.ifPresent(m->{
							ebeanIdMethod=m;
						});
				methods.stream()
						.filter(m->(m!=idField))
						.filter(m->m.isSetter())
						.filter(m->m.getMethodName().equalsIgnoreCase("set" + ebeanIdMethodName))
						.findAny()
						.ifPresent(m->{
							ebeanIdMethodSetter=m;
						});
				
				idType = idField.getType();
				if(idField!=null){
					finder = new Model.Finder(idType, this.cls);
				}
			}
			methods.removeIf(m -> !m.isTextEnabled());
			Reflections reflections = new Reflections(TextIndexer.IX_BASE_PACKAGE);

			releventClasses=reflections.getSubTypesOf(cls).stream().collect(Collectors.toList());
			for (Class<?> c : reflections.getAllSuperTypes(cls, c->c.getPackage().getName().startsWith(TextIndexer.IX_BASE_PACKAGE))) {
				releventClasses.add(c);
			}
			if(idType!=null){
				isIdNumeric=idType.isAssignableFrom(Long.class);
			}
		}
		
		/**
		 * Keys sufficient for search / caching, etc.
		 * @param id
		 * @return
		 */
		public String uniqueKeyWithId(Object id){
			return uniqueKeyFor(this.kind,id.toString());
		}
		
		public static String uniqueKeyFor(String k, String v){
			return k + "._id:" + v;
		}
		
		
		public boolean hasLongId() {
			return this.isIdNumeric;
		}

		public Class<?> getIdType() {
			return idType;
		}

		public boolean twoStringsEqual(String s1, String s2){
			if(s1==null && s2==null)return true;
			if(s1==null || s2==null)return false;
			return s1.equals(s2);
		}
		public boolean isEquivalentInfo(EntityInfo ei){
			if(this.isParentOrChildOf(ei)){
				if(this.table != null && ei.table!=null){
					if(twoStringsEqual(ei.tableName,this.tableName)){
						return true;
					}
				}
			}
			return false;
		}
		
		private List<Class<?>> releventClasses;
		
		private CachedSupplier<Set<EntityInfo>> allEquivalentInfo = 
				CachedSupplier.of((()->releventClasses.stream()
					.map(c->EntityUtils.getEntityInfoFor(c))
					.filter(this::isEquivalentInfo)
					.collect(Collectors.toSet())));
		
		public Set<EntityInfo> getAllEquivalentEntityInfos(){
			return allEquivalentInfo.call();
		}
		
		public boolean equals(Object o){
			if(o==null)return false;
			if(!(o instanceof EntityInfo))return false;
			return ((EntityInfo)o).cls==this.cls;
		}
		
		public int hashCode(){
			return this.cls.hashCode();
		}
		
		public List<FieldInfo> getUniqueColumns(){
			return this.uniqueColumnFields;
		}
		
		public Model.Finder getFinder(){
			return this.finder;
		}
		
		public Object formatIdToNative(String id){
			if(Long.class.isAssignableFrom(this.idType)){
				return Long.parseLong(id);
			}else if(this.idType.isAssignableFrom(UUID.class)){
				return UUID.fromString(id);
			}else{
				return id;
			}
		}
		
		public void setWithEbeanIdSetter(Object entity, Object id){
			if(this.ebeanIdMethodSetter!=null){
				ebeanIdMethodSetter.set(entity, id);
			}
		}
		
		public void getAndSetEbeanId(Object entity){
			if(this.ebeanIdMethodSetter!=null){
				this.getNativeIdFor(entity).ifPresent(id->{
					setWithEbeanIdSetter(entity,id);
				});
			}
		}
		
		public boolean isParentOrChildOf(EntityInfo ei){
			return (this.getClazz().isAssignableFrom(ei.getClazz()) ||
					ei.getClazz().isAssignableFrom(this.getClazz()));
		}
		public boolean shouldIndex(){
			return shouldIndex;
		}
		
		public CachedSupplier<String> getIdString(Object e){
			return CachedSupplier.of(()->"");
//			return new CachedSupplier<String>(()->{
//				Optional<?> id=this.getIdPossiblyFromEbeanMethod(e);
//				if(id.isPresent())return id.get().toString();
//				return null;
//			});
		}
		
		public Optional<Tuple<String,String>> getDynamicFacet(Object e){
			if(this.dynamicLabelField!=null && this.dynamicValueField!=null){
				String[] fv=new String[]{null,null};
				this.dynamicLabelField.getValue(e).ifPresent(
						f->{
							fv[0]=f.toString();
						});
				this.dynamicValueField.getValue(e).ifPresent(
						f->{
							fv[1]=f.toString();
						});
				if(fv[0]!=null && fv[1] !=null){
					return Optional.of(new Tuple<String,String>(fv[0],fv[1]));
				}else{
					return Optional.empty();
				}
			}
			return Optional.empty();
		}
		
		public Optional<ValueMakerInfo> getIDFieldInfo(){
			return Optional.ofNullable(this.idField);
		}
		
		public List<ValueMakerInfo> getSequenceFieldInfo() {
			return this.seqFields;
		}
		
		public List<ValueMakerInfo> getStructureFieldInfo() {
			return this.strFields;
		}

		public List<FieldInfo> getFieldInfo() {
			return this.fields;
		}

		public List<MethodInfo> getMethodInfo() {
			return this.methods;
		}

		public String getName() {
			return this.kind;
		}
		
		public Tuple<String,String> getFieldAndId(Object e){
			return new Tuple<String,String>(
					this.getExternalIdFieldName(),
					this.getIdString(e).call()
					);
		}

		// the hidden _id field stores the field's value
		// in its native type whereas the display field id
		// is used for indexing purposes and as such is
		// represented as a string
		public String getInternalIdField(){
			return kind + "._id";
		}
		
		public String getExternalIdFieldName(){
			return kind + ".id";
		}

		public boolean isEntity() {
			return this.isEntity;
		}
		
		public boolean ignorePostUpdateHooks(){
			return shouldPostUpdateHooks;
		}

		public Class<?> getClazz() {
			return this.cls;
		}
		
		public Optional<Object> getNativeIdFor(Object e){
			if(this.idField!=null){
				return idField.getValue(e);
			}
			return Optional.empty();
		}
		
		public boolean hasVersion(){
			return (this.versionField!=null);
		}
		
		public Object getVersionFor(Object entity){
			return this.versionField.getValue(entity).orElseGet(null);
		}
		
		public String getVersionAsStringFor(Object entity){
			Object o=getVersionFor(entity);
			if(o==null)return null;
			return o.toString();
		}
		
		//It seems that, in certain cases,
		//fetching the ID or some other field directly
		//using reflection (as is done here abstractly)
		//can cause problems for ebean. It may be necessary
		//to explore the method names in rare cases
		private static String getBeanName (String field) {
	        return Character.toUpperCase(field.charAt(0))+field.substring(1);
	    }
		
		
		/**
		 * This preserves some of the weird checks being done
		 * to circumvent ebean strangeness. I have no way to evaluate
		 * when it had been used before to confirm it gives the same 
		 * answers. 
		 * @param o
		 * @return
		 */
		@Deprecated
		public Optional<Object> getIdPossiblyFromEbeanMethod(Object o){
			Optional<Object> id = this.getNativeIdFor(o);
			if(!id.isPresent()){
				if(ebeanIdMethod==null)return null;
				return ebeanIdMethod.getValue(o);
			}
			return id;
		}

		public boolean hasIdField() {
			return (this.idField!=null);
		}

		public Object findById(String id) {
			return this.getFinder().byId(this.getNativeIdFor(id));
		}


//		 @Deprecated
//		    public static Object getId (Object entity) throws Exception {
//		    	if(entity instanceof Moiety){
//		    		return ((Moiety)entity).getUUID();
//		    	}
//		        Field f = getIdField (entity);
//		        Object id = null;
//		        if (f != null) {
//		            id = f.get(entity);
//		            if (id == null) { // now try bean method
//		                try {
//		                    Method m = entity.getClass().getMethod
//		                        ("get"+getBeanName (f.getName()));
//		                    id = m.invoke(entity, new Object[0]);
//		                }
//		                catch (NoSuchMethodException ex) {
//		                    ex.printStackTrace();
//		                }
//		            }
//		        }
//		        return id;
//		    }
//		    
	}

	public static interface ValueMakerInfo {
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
		
		// this is a little weird, in that this is meant to consume
		// the very value it created
		default void forEach(Object value, BiConsumer<Integer, Object> bic) {
			// Object value=this.getValue(entity);
			// if(value==null)return;
			Stream<?> s;
			if (isArray()) {
				s = Arrays.stream((Object[]) value);
			} else if (isCollection()) {
				s = ((Collection<?>) value).stream();
			} else {
				return;
			}
			int[] idx = { 0 };
			s.forEach(o -> bic.accept(idx[0]++, o));
		}
	}

	public static class MethodInfo implements ValueMakerInfo {

		Method m;
		Indexable indexable;
		boolean textEnabled = false;
		String indexingName;
		String name;
		boolean isStructure = false;
		boolean isSequence  = false;
		Class<?> type;
		
		boolean isArray=false;
		boolean isCollection=false;
		boolean isId=false;
		
		boolean isSetter=false;
		boolean isGetter=false;
		
		Class<?> setterType;

		public MethodInfo(Method m) {
			Class<?>[] args = m.getParameterTypes();
			this.m = m;
			indexable = (Indexable) m.getAnnotation(Indexable.class);
			name = m.getName();
			indexingName=name;
			if (name.startsWith("get")) {
				indexingName = name.substring(3);
				if(args.length==0){
					isGetter=true;
				}
			}else if (name.startsWith("set")) {
				if(args.length==1){
					isSetter=true;
					setterType=args[0];
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
					
					if(indexable.structure()){
						isStructure=true;
					}
					if(indexable.sequence()){
						isSequence=true;
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

		public boolean isGetter() {
			return this.isGetter;
		}

		public String getMethodName() {
			return this.name;
		}
		
		public Class<?> getSetterParameterType(){
			return setterType;
		}
		
		public void set(Object entity, Object val){
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
			if(isId)System.out.println(this.getName() + " is an ID?");
			return isId;
		}

		//Not currently supported
		public boolean isDataVersion() {
			return false;
		}

		@Override
		public Optional<Object> getValue(Object entity){
			try{
				return Optional.ofNullable(m.invoke(entity));
			}catch(Exception e){
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

		public Indexable getIndexable() {
			return indexable;
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

		public boolean isSetter(){
			return this.isSetter;
		}

		@Override
		public boolean isNumeric() {
			return type.isAssignableFrom(Long.class);
		}
		
		
	}

	public static class FieldInfo implements ValueMakerInfo {
		Field f;
		Indexable indexable;

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
		
		Column column;
		
		public boolean isSequence(){
			return this.isSequence;
		}
		public boolean isUniqueColumn() {
			return (this.isColumn() && this.getColumn().unique());
		}
		public boolean isDataVersion() {
			return isDataVersion;
		}
		public boolean isStructure(){
			return this.isStructure;
		}
		public boolean isColumn(){
			return (this.column!=null);
		}
		
		public String getColumnName(){
			if(this.isColumn()){
				String cname=this.getColumn().name();
				if(cname != null && !cname.equals("")){
					return cname;
				}
			}
			return this.getName();
		}
		
		public Column getColumn(){
			return this.column;
		}

		public FieldInfo(Field f, DynamicFacet dyna) {
			this.f = f;
			this.indexable = f.getAnnotation(Indexable.class);
			this.column=   f.getAnnotation(Column.class);
			if (indexable == null) {
				indexable = defaultIndexable;
				explicitIndexable = false;
			}

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
			if (this.indexable.sequence()){
				this.isSequence=true;
			}
			if (this.indexable.structure()){
				this.isStructure=true;
			}
			if (f.getAnnotation(DataVersion.class)!=null){
				this.isDataVersion=true;
			}
			
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

		public Indexable getIndexable() {
			return indexable;
		}

		public Optional<Object> getValue(Object entity){
			try{
				return Optional.ofNullable(f.get(entity));
			}catch(Exception e){
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
	}

	public static class EntityAnalyzer{
		private PathStack path;
		private Consumer<IndexableField> ixFields=null;
		private Consumer<Tuple<PathStack,EntityWrapper>> listens=null;
		DynamicFieldIndexerPassiveProvider dynamicFacets=null; 
		PrimitiveFieldIndexerPassiveProvider indexPerformer;
		
		
		
		LinkedReferenceSet<Object> prevEntities;
		
		
		
		EntityWrapper estart;
		
		public EntityAnalyzer(){
			path= new PathStack();
			prevEntities = new LinkedReferenceSet<Object>();
			this.ixFields=ixFields;
		}
		
		public EntityAnalyzer using(EntityWrapper e1){
			this.estart=e1;
			return this;
		}
		
		public EntityAnalyzer acceptFieldsWith(Consumer<IndexableField> ixFields){
			this.ixFields=ixFields;
			return this;
		}
		public EntityAnalyzer produceDynamicFacetsWith(DynamicFieldIndexerPassiveProvider dpp){
			this.dynamicFacets=dpp;
			return this;
		}
		public EntityAnalyzer produceDefaultIndexingWith(PrimitiveFieldIndexerPassiveProvider indexPerformer){
			this.indexPerformer=indexPerformer;
			return this;
		}
		
		public void execute(Consumer<Tuple<PathStack,EntityWrapper>> listens){
			this.listens=listens;
			instrument(estart);
		}
		public void execute(){
			instrument(estart);
		}
		
		private void next(EntityWrapper<?> ew){
			if(this.listens!=null)listens.accept(new Tuple<PathStack,EntityWrapper>(path,ew));
			instrument(ew);
		}
		
		
		
		//not thread safe at all. Never call this directly
		private void instrument(EntityWrapper<?> ew) {
			prevEntities.pushAndPopWith(ew.getValue(),()->{
					if(ixFields!=null){
						ixFields.accept(new FacetField(TextIndexer.DIM_CLASS, ew.getKind()));
						ew.getId().ifPresent(o->{
							if (o instanceof Long) {
								ixFields.accept(new LongField(ew.ei.getInternalIdField(), (Long) o, YES));
							} else {
								ixFields.accept(new StringField(ew.ei.getInternalIdField(), o.toString(), YES));
							}
							ixFields.accept(new StringField(ew.getIdField(), o.toString(), NO));
						});
						
						//primitive fields only, they should all get indexed
						ew.streamFieldsAndValues(f->f.isPrimitive()).forEach(fi->{
							path.pushAndPopWith(fi.k().getName(),()->{
								indexPerformer.defaultIndex(ixFields, fi.k().getIndexable(), path.getFirst(), path.toPath(), fi.v(), Store.NO);
							});
						});
						
						ew.getDynamicFacet().ifPresent(fv->{
							path.pushAndPopWith(fv.k(),()->{
								dynamicFacets.produceDynamicFacets(fv.k(), fv.v(), path.toPath(), ixFields);
							});
						});
						
						
						ew.streamMethodsAndValues(m->m.isArrayOrCollection()).forEach(t -> {
							path.pushAndPopWith(t.k().getName(), () -> {
									t.k().forEach(t.v(), (i, o) -> {
										path.pushAndPopWith(i + "", () -> {
											indexPerformer.defaultIndex(ixFields, t.k().getIndexable(), path.getFirst(), path.toPath(),o, Store.NO);
										});
									});
							});
						});//each array / collection
						
						
						ew.streamMethodsAndValues(m->!m.isArrayOrCollection()).forEach(t -> {
							path.pushAndPopWith(t.k().getName(), () -> {
											indexPerformer.defaultIndex(ixFields, t.k().getIndexable(), path.getFirst(), path.toPath(), t.v(), Store.NO);
							});
						});//each non-array 
					}
					
					ew.streamFieldsAndValues(f->!f.isPrimitive())
						.forEach(fi->{
								path.pushAndPopWith(fi.k().getName(),()->{
										if (fi.k().isArrayOrCollection()) {
											// MUST be done in order
											//System.out.println(path.toPath());
											fi.k().forEach(fi.v(), (i, o) -> {
												path.pushAndPopWith(i+"", ()->{
													next(new EntityWrapper(o));
												});
											});
										}else if (fi.k().isEntityType()) {
											// the value might be an entity, but the declared
											// type is something more generic, but it's been
											// simplified
											// here.
											// composite type; recurse
											if(fi.k().getIndexable().recurse()){
												next(new EntityWrapper(fi.v()));
											}
											if (fi.k().isExplicitlyIndexable()) {
												if(ixFields!=null){
													indexPerformer.defaultIndex(ixFields, fi.k().getIndexable(), path.getFirst(), path.toPath(), fi.v(), Store.NO);
												}
											}
										}else { // treat as string
											if(ixFields!=null){
												indexPerformer.defaultIndex(ixFields, fi.k().getIndexable(), path.getFirst(), path.toPath(), fi.v(), Store.NO);
											}
										}
							}); // for each field with value
					}); // foreach non-primitive field
			});
		}
	}

	
	
	

}
