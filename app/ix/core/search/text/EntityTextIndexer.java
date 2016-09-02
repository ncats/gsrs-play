package ix.core.search.text;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.persistence.Entity;
import javax.persistence.Id;

import ix.core.models.DynamicFacet;
import ix.core.models.Indexable;
import ix.core.util.CachedSupplier;
import ix.utils.EntityUtils;
import ix.utils.Tuple;

public class EntityTextIndexer {
	
	@Indexable //put default indexable things here
	static final class DefaultIndexable {}
	
	static final Indexable defaultIndexable = (Indexable) DefaultIndexable.class.getAnnotation(Indexable.class);

	public final static Map<String, EntityInfo> infoCache = new ConcurrentHashMap<String, EntityInfo>();
	
	
	public static EntityInfo getEntityInfoFor(Class<?> cls){
		return infoCache.computeIfAbsent(cls.getName(), k-> new EntityInfo(cls));
	}
	
	public static EntityInfo getEntityInfoFor(Object entity){
		return getEntityInfoFor(entity.getClass());
	}
	
	public static class EntityInfo {
		Class<?> cls;
		String kind;
		DynamicFacet dyna;
		List<FieldInfo> fields;
		Indexable indexable;
		
		List<ValueMakerInfo> seqFields = new ArrayList<ValueMakerInfo>();
		List<ValueMakerInfo> strFields = new ArrayList<ValueMakerInfo>();;
		
		List<MethodInfo> methods;
		
		
		FieldInfo idField=null;
		
		FieldInfo dynamicLabelField=null;
		FieldInfo dynamicValueField=null;
		
		boolean isEntity=false;
		boolean shouldIndex=true;
		
		public EntityInfo(Class cls) {
			this.cls = cls;
			this.indexable = (Indexable) cls.getAnnotation(Indexable.class);
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
						return true;
					}).collect(Collectors.toList());
			
			methods = Arrays.stream(cls.getMethods()).map(m2 -> new MethodInfo(m2)).collect(Collectors.toList());
			
			seqFields = Stream.concat(fields.stream(), methods.stream()).filter(f->f.isSequence()).collect(Collectors.toList());
			strFields = Stream.concat(fields.stream(), methods.stream()).filter(f->f.isStructure()).collect(Collectors.toList());
			
			fields.removeIf(f->!f.isTextEnabled());
			methods.removeIf(m -> !m.isTextEnabled());
			if (cls.isAnnotationPresent(Entity.class)) {
				isEntity=true;
				if(indexable != null && !indexable.indexed()){
					shouldIndex=false;
				}
		    }
			
		}
		
		public boolean shouldIndex(){
			return shouldIndex;
		}
		
		public CachedSupplier<String> getIdString(Object e){
			return new CachedSupplier<String>(()->EntityUtils.getIdForBeanAsString(e));
		}
		
		public Optional<String[]> getDynamicFacet(Object e){
			if(this.dynamicLabelField!=null && this.dynamicValueField!=null){
				//System.out.println("There is a dynamic facet");
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
					return Optional.of(fv);
				}else{
					return Optional.empty();
				}
			}
			return Optional.empty();
		}
		
		public CachedSupplier<String> getGloballyUniqueIdString(Object e){
			return new CachedSupplier<String>(()->this.kind +":" + EntityUtils.getIdForBeanAsString(e));
		}
		
		public Optional<FieldInfo> getIDFieldInfo(){
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
			return new Tuple<String,String>(getExternalIdFieldName(),this.getIdString(e).call());
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

	}

	public static interface ValueMakerInfo {
		public Optional<?> getValue(Object entity);

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
		String name;
		boolean isStructure = false;
		boolean isSequence  = false;
		Class<?> type;
		boolean isArray;
		boolean isCollection;

		public MethodInfo(Method m) {
			this.m = m;
			indexable = (Indexable) m.getAnnotation(Indexable.class);
			name = m.getName();
			if (name.startsWith("get")) {
				name = name.substring(3);
			}

			if (indexable != null) {
				// we only index no arguments methods
				Class<?>[] args = m.getParameterTypes();
				if (args.length == 0) {
					if (indexable.indexed()) {
						textEnabled = true;
						if (!indexable.name().equals("")) {
							name = indexable.name();
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
			return this.name;
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
		
		public boolean isSequence(){
			return this.isSequence;
		}
		public boolean isStructure(){
			return this.isStructure;
		}

		public FieldInfo(Field f, DynamicFacet dyna) {
			this.f = f;
			this.indexable = (Indexable) f.getAnnotation(Indexable.class);
			
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
	}

	

}
