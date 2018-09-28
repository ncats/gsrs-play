package ix.core.controllers.v1;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.persistence.Id;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import ix.core.Experimental;
import ix.core.controllers.EntityFactory;
import ix.core.controllers.search.SearchRequest;
import ix.core.models.Role;
import ix.core.util.EntityUtils;
import ix.seqaln.SequenceIndexer.CutoffType;
import ix.utils.Global;
import play.mvc.Result;


@Experimental
public interface InstantiatedNamedResource<I,V> {
    
    public static enum Operations{
        CREATE_OPERATION(new Operation("create")),
        VALIDATE_OPERATION(new Operation("validate")),
        //TODO: implement
        RESOLVE_OPERATION(new Operation("resolve", 
                Argument.of(null, String.class, "id"))),
        UPDATE_ENTITY_OPERATION(new Operation("updateEntity")),
        PATCH_OPERATION(new Operation("patch",
                Argument.of(null, Id.class, "id"))),
        COUNT_OPERATION(new Operation("count")),
        STREAM_OPERATION(new Operation("stream", 
                Argument.of(null, String.class, "field"),
                Argument.of(0, int.class , "top"),
                Argument.of(0, int.class , "skip"))),
        SEARCH_OPERATION(new Operation("search", 
                Argument.of(null, String.class, "query"),
                Argument.of(0, int.class, "top"),
                Argument.of(0, int.class, "skip"),
                Argument.of(0, int.class, "fdim"))),
        GET_OPERATION(new Operation("get", 
                Argument.of(null, Id.class, "id"),
                Argument.of(null, String.class, "expand"))),
        DOC_OPERATION(new Operation("doc", 
                Argument.of(null, Id.class, "id"))),
        EDITS_OPERATION(new Operation("edits", 
                Argument.of(null, Id.class, "id"))),
        APPROVE_OPERATION(new Operation("approve", 
                Argument.of(null, Id.class, "id"))),
        UPDATE_OPERATION(new Operation("update", 
                Argument.of(null, Id.class, "id"),
                Argument.of(null, String.class, "field")
                
                )),
        FIELD_OPERATION(new Operation("field", 
                Argument.of(null, Id.class, "id"),
                Argument.of(null, String.class, "field"))),
        PAGE_OPERATION(new Operation("page", 
                Argument.of(10, int.class, "top"), 
                Argument.of(0, int.class, "skip"),
                Argument.of(null, String.class, "filter"))),
        STRUCTURE_SEARCH_OPERATION(new Operation("structureSearch", 
                Argument.of(null, String.class, "query"),
                Argument.of("substructure", String.class, "type"),
                Argument.of(.8, double.class, "cutoff"),
                Argument.of(0, int.class, "top"),
                Argument.of(0, int.class, "skip"),
                Argument.of(0, int.class, "fdim"),
                Argument.of("", String.class, "field"))),
        SEQUENCE_SEARCH_OPERATION(new Operation("sequenceSearch", 
                Argument.of(null, String.class, "query"),
                Argument.of(CutoffType.SUB, CutoffType.class, "cutofftype"),
                Argument.of(.8, double.class, "cutoff"),
                Argument.of(0, int.class, "top"),
                Argument.of(0, int.class, "skip"),
                Argument.of(0, int.class, "fdim"),
                Argument.of("", String.class, "field"),
                Argument.of("", String.class, "seqType")));
        
        private final Operation op;
        public Operation op(){
            return this.op;
        }
        private Operations(Operation op){
            this.op=op;
            
        }
        
        public Operation with(Object ...objs){
            return this.op.values(objs);
        }
    }
    
    
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
		return operate(Operations.CREATE_OPERATION.with());
	}
	default Result count(){
		return operate(Operations.COUNT_OPERATION.with());
	}

	default Result stream(String field, int top, int skip){
		return operate(Operations.STREAM_OPERATION.with(field,top,skip));
	}
	
	default Class<SearchRequest.Builder> searchRequestBuilderClass(){
		return SearchRequest.Builder.class;
	}

	default Result search(String q,
            int top, int skip, int fdim){
		return operate(Operations.SEARCH_OPERATION.with(q,top,skip, fdim));
	}
	
	default Result get(I id){
		return get(id, null);
	}
	
	default Result get(I id, String expand){
		return operate(Operations.GET_OPERATION.with(id,expand));
	}
	
	default Result doc(I id){
		return operate(Operations.DOC_OPERATION.with(id));
	}
	
	default Result edits(I id){
		return operate(Operations.EDITS_OPERATION.with(id));
	}
	
	default Result field(I id, String field){
		return operate(Operations.FIELD_OPERATION.with(id, field));
	}
	
	default Result page(int top, int skip){
		return page(top, skip, null);
	}
	
	default Result page(int top, int skip, String filter){
		return operate(Operations.PAGE_OPERATION.with(top,skip,filter));
	}
	default Result structureSearch(String q, String type, double cutoff, int top, int skip, int fdim){
		return structureSearch(q,type,cutoff,top,skip,fdim, "");
	}
	default Result structureSearch(String q, String type, double cutoff, int top, int skip, int fdim, String field){
		return operate(Operations.STRUCTURE_SEARCH_OPERATION.with(q,type,cutoff,top,skip,fdim,field));
	}
	
	default Result sequenceSearch(String q, CutoffType type, double cutoff, int top, int skip, int fdim, String field, String seqType){
        return operate(Operations.SEQUENCE_SEARCH_OPERATION.with(q,type,cutoff,top,skip,fdim,field, seqType));
    }
	
	default Result validate(){
		return operate(Operations.VALIDATE_OPERATION.with());
	}
	
	
	default Result approve(I id){
		return operate(Operations.APPROVE_OPERATION.with(id));
	}
	
	default Result update(I id, String field){
	    System.out.println("Calling update method");
		return operate(Operations.UPDATE_OPERATION.with(id,field));
	}
	

    default Result patch(I id){
        return operate(Operations.PATCH_OPERATION.with(id));
    }
	
	default Result updateEntity(){
		return operate(Operations.UPDATE_ENTITY_OPERATION.with());
	}
	
		
	default Optional<I> resolveID(String synonym){
		if(Long.class.equals(this.getIdType())){
			return (Optional<I>) Optional.of(Long.parseLong(synonym));
		}else if(UUID.class.equals(this.getIdType())){
			return (Optional<I>) Optional.of(UUID.fromString(synonym));
		}
		return Optional.empty();
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
	
	/**
	 * Should this resource be accessible to users with the 
	 * given roles? Default always returns true.
	 * 
	 * @param roles
	 * @return
	 */
	default boolean isAccessible(List<Role> roles){
	    return true;
	}


}
