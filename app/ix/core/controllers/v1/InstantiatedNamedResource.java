package ix.core.controllers.v1;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.persistence.Id;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import ix.core.Experimental;
import ix.core.NamedResource;
import ix.core.controllers.EntityFactory;
import ix.core.controllers.search.SearchFactory;
import ix.core.util.CachedSupplier;
import ix.core.util.EntityUtils;
import ix.utils.Global;
import play.mvc.Result;


@Experimental
public interface InstantiatedNamedResource<I,V> {
	public static final Operation CREATE_OPERATION = new Operation("create");
	public static final Operation UPDATE_ENTITY_OPERATION = new Operation("updateEntity");
	public static final Operation COUNT_OPERATION = new Operation("count");
	public static final Operation VALIDATE_OPERATION = new Operation("validate");
	public static final Operation STREAM_OPERATION = new Operation("stream", 
													Argument.of(null, String.class, "field"),
													Argument.of(0, int.class , "top"),
													Argument.of(0, int.class , "skip"));
	public static final Operation SEARCH_OPERATION = new Operation("search", 
													Argument.of(null, String.class, "query"),
													Argument.of(0, int.class, "top"),
													Argument.of(0, int.class, "skip"),
													Argument.of(0, int.class, "fdim"));
	public static final Operation GET_OPERATION = new Operation("get", 
													Argument.of(null, Id.class, "id"),
													Argument.of(null, String.class, "expand"));
	public static final Operation DOC_OPERATION = new Operation("doc", 
													Argument.of(null, Id.class, "id"));
	public static final Operation EDITS_OPERATION = new Operation("edits", 
													Argument.of(null, Id.class, "id"));
	public static final Operation APPROVE_OPERATION = new Operation("approve", 
													Argument.of(null, Id.class, "id"));
	public static final Operation UPDATE_OPERATION = new Operation("approve", 
													Argument.of(null, Id.class, "id"),
													Argument.of(null, String.class, "field"));
	public static final Operation FIELD_OPERATION = new Operation("field", 
													Argument.of(null, Id.class, "id"),
													Argument.of(null, String.class, "field"));
	public static final Operation PAGE_OPERATION = new Operation("page", 
													Argument.of(10, int.class, "top"), 
													Argument.of(0, int.class, "skip"),
													Argument.of(null, String.class, "filter"));
	public static final Operation STRUCTURE_SEARCH_OPERATION = new Operation("structureSearch", 
			Argument.of(null, String.class, "query"),
			Argument.of("substructure", String.class, "type"),
			Argument.of(.8, double.class, "cutoff"),
			Argument.of(0, int.class, "top"),
			Argument.of(0, int.class, "skip"),
			Argument.of(0, int.class, "fdim"),
			Argument.of("", String.class, "field"));
	
	public static final Operation[] ALL_OPERATIONS = new Operation[]{
															CREATE_OPERATION, 
															UPDATE_ENTITY_OPERATION ,
															COUNT_OPERATION ,
															VALIDATE_OPERATION ,
															STREAM_OPERATION ,								
															SEARCH_OPERATION ,								
															GET_OPERATION ,									
															DOC_OPERATION ,									
															EDITS_OPERATION,									
															APPROVE_OPERATION ,								
															UPDATE_OPERATION ,								
															FIELD_OPERATION ,									
															PAGE_OPERATION ,
															STRUCTURE_SEARCH_OPERATION
														};
	
	public String getName();
	public String getDescription();
	
	default String getKind(){
		return getTypeKind().getName();
	}
	
	@JsonIgnore
	@SuppressWarnings("unchecked")
	default Class<I> getIdType(){
		return (Class<I>) EntityUtils
				.getEntityInfoFor(getTypeKind())
				.getIdType();
	}
	
	@JsonIgnore
	public Class<V> getTypeKind();
	
	default String getHref(){
		return Global.getNamespace()+"/"+getName();
	}
	
	@JsonIgnore
	public Set<Operation> getSupportedOperations();  
	
	@JsonProperty("supportedOperations")
	default Set<String> getNamedOperations(){
		return  getSupportedOperations()
			.stream()
			.map(o->o.getOperationName())
			.collect(Collectors.toSet());
	}
	
	default Result create(){
		return operate(CREATE_OPERATION);
	}
	default Result count(){
		return operate(COUNT_OPERATION);
	}

	default Result stream(String q, int top, int skip){
		return operate(STREAM_OPERATION.values(q,top,skip));
	}
	default Result search(String q, 
            int top, int skip, int fdim){
		return operate(SEARCH_OPERATION.values(q,top,skip, fdim));
	}
	
	default Result get(I id){
		return get(id, null);
	}
	
	default Result get(I id, String expand){
		return operate(GET_OPERATION.values(id,expand));
	}
	
	default Result doc(I id){
		return operate(DOC_OPERATION.values(id));
	}
	
	default Result edits(I id){
		return operate(EDITS_OPERATION.values(id));
	}
	
	default Result field(I id, String field){
		return operate(FIELD_OPERATION.values(id, field));
	}
	
	default Result page(int top, int skip){
		return page(top, skip, null);
	}
	
	default Result page(int top, int skip, String filter){
		return operate(PAGE_OPERATION.values(top,skip,filter));
	}
	default Result structureSearch(String q, String type, double cutoff, int top, int skip, int fdim){
		return structureSearch(q,type,cutoff,top,skip,fdim, "");
	}
	default Result structureSearch(String q, String type, double cutoff, int top, int skip, int fdim, String field){
		return operate(STRUCTURE_SEARCH_OPERATION.values(q,type,cutoff,top,skip,fdim,field));
	}
	
	default Result validate(){
		return operate(VALIDATE_OPERATION);
	}
	
	default Result approve(I id){
		return update(id,null);
	}
	
	default Result update(I id, String field){
		return operate(UPDATE_OPERATION.values(id,field));
	}
	
	default Result updateEntity(){
		return operate(UPDATE_ENTITY_OPERATION);
	}
	
	
	/**
	 * Single method where the more explicit methods
	 * will delegate to if they are not implemented
	 * @param operation
	 * @param objects
	 * @return
	 */
	default Result operate(Operation op ){
		throw unsupported(op.operationName);
	}
	
	public static class Operation{
		private String operationName;
		private List<Argument> arguments = new ArrayList<>();
		
		public String getOperationName(){
			return operationName;
		}
		
		public List<Argument> getArguments(){
			return arguments;
		}
		
		public Operation(String name, Argument ... args){
			this.operationName=name;
			this.arguments = Arrays.asList(args);
		}
		
		
		public Operation values(Object ... objs){
			Operation o = clone();
			System.out.println("Setting args:" + Arrays.toString(objs));
			for(int i=0;i<objs.length;i++){
				o.arguments.get(i).setValue(objs[i]);
			}
			return o;
		}
		
		public Operation clone(){
			return of(this.operationName, arguments
					.stream()
					.map(Argument::clone)
					.toArray(i->new Argument[i]));
		}
		
		public Operation withIdClass(Class<?> id){
			Operation op = clone();
			for(Argument arg: op.arguments){
				if(arg.getType().equals(Id.class)){
					arg.cls=id;
				}
			}
			return op;
		}
		
		public static Operation of(String name, Argument ... args){
			return new Operation(name,  args);
		}
		
		@Override
		public String toString(){
			return operationName;
		}
		
		@Override
		public boolean equals(Object o){
			if(o==null)return false;
			if(o==this)return true;
			if(!(o instanceof Operation))return false;
			return ((Operation)o).operationName.equals(this.operationName);
		}
		
		@Override
		public int hashCode(){
			return this.operationName.hashCode();
		}
		
		public Class<?>[] asSigniture(){
			return this.arguments.stream()
					.map(o->o.getType())
					.toArray(i->new Class<?>[i]);
		}
		
		public Object[] asRawArguments(){
			return this.arguments.stream()
					.map(o->o.getValue())
					.toArray(i->new Object[i]);
		}
	}

	public static class Argument<T> implements Serializable{
		private T arg;
		private Class<T> cls;
		private String name;
		
		public Argument(T t, Class<T> cls, String name){
			this.arg=t;
			this.cls=cls;
			this.name=name;
		}
		
		public T getValue() {
			return arg;
		}
		
		public Argument setValue(T t){
			arg=t;
			return this;
		}
		
		public String getName() {
			return name;
		}
		
		public Class<T> getType() {
			return cls;
		}
		
		public static <T> Argument<T> of(T t, Class<T> cls, String name){
			return new Argument<T>(t,cls,name);
		}
		
		public Argument<T> clone(){
			return of(arg,cls,name);
		}
		
		
	}
	
	default UnsupportedOperationException unsupported(String operation) throws UnsupportedOperationException{
		return new UnsupportedOperationException("Resource :'" + getName()  + "' does not support the '" + operation + "' operation" );
	}
	
	public static <I,V> InstantiatedNamedResource<I,V> of(Class<? extends EntityFactory> ef, Class<I> id, Class<V> resource){
		return new StaticDelegatingNamedResource<I,V>(ef,id,resource);
	}
	


}
