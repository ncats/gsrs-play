package ix.core.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

import com.fasterxml.jackson.core.JsonPointer;

import ix.utils.Tuple;

/**
 * Path to a JSON serializable sub-element of an object. This is used
 * analogously to {{@link com.fasterxml.jackson.core.JsonPointer} which
 * is used to get a specific descendant JsonNode. However, unlike JsonPointer,
 * a PojoPointer may specify some additional operations, such as filters
 * for a collection, or specifying whether an object is to be returned in
 * its "raw" form, or as a JSONNode serialized form.
 * 
 * 
 * 
 * <p>
 * PojoPointer supports reading JsonPointer notation with {@link #fromJsonPointer(JsonPointer jp)}
 * </p>
 * 
 * 
 * @author peryeata
 *
 */
public interface PojoPointer{
	public static final char LAMBDA_CHAR = '!';
	public static final char RAW_CHAR = '$';
	public static final char ARRAY_CHAR = '$';
	
	public boolean isRaw();
	public void setRaw(boolean raw);
	
	default boolean isLeafRaw(){
		PojoPointer pp=this;
		while(pp.hasTail()){
			pp=pp.tail();
		}
		return pp.isRaw();
	}
	
	public PojoPointer tail();
	public void setTail(PojoPointer pp);
	
	
	public JsonPointer toJsonPointer();
	public String toURIpath();
	
	default boolean hasTail(){
		PojoPointer tail = tail();
		return tail!=null;
	}
	
	/**
	 * Parses the supplied string to a JsonPointer, and then
	 * into a PojoPointer.
	 * 
	 * @param jp
	 * @return
	 */
	public static PojoPointer fromJsonPointer(String jp){
		return fromJsonPointer(JsonPointer.compile(jp));
	}
	
	/**
	 * Parses the supplied JsonPointer into a PojoPointer.
	 * 
	 * @param jp
	 * @return
	 */
	public static PojoPointer fromJsonPointer(JsonPointer jp){
		PojoPointer root=new IdentityPath();
		PojoPointer parent=root;
		
		for(;jp!=null;jp=jp.tail()){
			if(jp.matches())continue;
			String prop=jp.getMatchingProperty();
			int pp=jp.getMatchingIndex();
			PojoPointer c=null;
			if(pp>=0){
				c = new ArrayPath(pp);
			}else{
				c = new ObjectPath(prop);
			}
			parent.setTail(c);
			parent=c;
		}
		return root;
	}
	public static Supplier<String> splitUriPaths(String uripath, final char c1){
		AtomicInteger charindex=new AtomicInteger();
		return ()->{
			int pcount=0;
			int i=charindex.get();
			if(i>=uripath.length()){
				return null;
			}
			for(int j=i;j<uripath.length();j++){
				char c=uripath.charAt(j);
    			switch(c){
	            	case '(':
	            		pcount++;
	            		break;
	            	case ')':
	            		pcount--;
	            		break;
	            	default:
	            		if(c==c1){
		            		if(pcount==0){
		            			charindex.set(j+1);
		            			return uripath.substring(i,j);
		            		}
	            		}
	            		break;
	        	}
			}
			charindex.set(uripath.length());
			return uripath.substring(i);
		};
	}
	
	public static Supplier<String> getParentheses(String parenGroup){
		AtomicInteger charindex=new AtomicInteger();
		return ()->{
			int pcount=0;
			int i=parenGroup.indexOf('(',charindex.get());
			if(i>=parenGroup.length() || i<0){
				return null;
			}
			for(int j=i;j<parenGroup.length();j++){
    			switch(parenGroup.charAt(j)){
	            	case '(':
	            		pcount++;
	            		break;
	            	case ')':
	            		pcount--;
	            		if(pcount==0){
	            			charindex.set(j+1);
	            			return parenGroup.substring(i+1,j);
	            		}
	            		break;
	        	}
			}
			charindex.set(parenGroup.length());
			return null;
		};
	}
	public static Supplier<Tuple<ELEMENT_TYPE,String>> parse(String element, int start){
		AtomicInteger charindex=new AtomicInteger(start);
		return ()->{
			ELEMENT_TYPE tt=null;
			int pcount=0;
			int i=charindex.get();
			if(i>=element.length() || i<0){
				return null;
			}
			for(int j=i;j<element.length();j++){
    			switch(element.charAt(j)){
    				case LAMBDA_CHAR:
    					System.out.println("Start lambda:" + pcount);
    					if(tt==null){
    						tt=ELEMENT_TYPE.LAMBDA;
    					}
    					if(tt==ELEMENT_TYPE.FIELD){
    						charindex.set(j);
    						return Tuple.of(tt,element.substring(1,j));
    					}
    					break;
	            	case '(':
	            		if(tt==null){
	            			tt=ELEMENT_TYPE.LOCATOR;
	            		}
	            		if(tt==ELEMENT_TYPE.FIELD){
    						charindex.set(j);
    						return Tuple.of(tt,element.substring(1,j));
    					}
	            		pcount++;
	            		break;
	            	case ')': //terminator for LAMDA and LOCATOR
	            		pcount--;
	            		if(pcount==0){
	            			charindex.set(j+1);

	            			System.out.println("End" + tt);
	    					if(tt==ELEMENT_TYPE.LOCATOR){
	    						return Tuple.of(tt,element.substring(i+1,j));
	    					}
	            			return Tuple.of(tt,element.substring(i+1,j+1));
	            		}
	            		break;
	            	default:
	            		if(tt==null && j==start){
	            			tt=ELEMENT_TYPE.FIELD;
	            		}else if(tt==null){
	            			throw new IllegalStateException("Couldn't parse");
	            		}
	        	}
			}
			System.out.println("Returning null, but was set to:" + tt + " with "+ pcount);
			System.out.println("on:" + element);
			return null;
		};
	}
	public static enum ELEMENT_TYPE{
		FIELD,
		LOCATOR,
		LAMBDA
	}
	
	public static abstract class LambdaArgumentParser implements Function<String, PojoPointer>{
		private String key;
		public LambdaArgumentParser(String key){
			Objects.requireNonNull(key);
			this.key=key;
		}
		
		@Override
		public PojoPointer apply(String t) {
			t=t.substring(key.length()+1,t.length()-1);
    		return parse(t);
		}
		 
		public abstract PojoPointer parse(String t);
		
		public String getKey(){
			return this.key;
		}
	}
	
	public static class FieldBasedLambdaArgumentParser extends LambdaArgumentParser{
		Function<PojoPointer,PojoPointer> fun;
		public FieldBasedLambdaArgumentParser(String key, Function<PojoPointer,PojoPointer> fun){
			super(key);
			Objects.requireNonNull(fun);
			this.fun=fun;
		}
		@Override
		public PojoPointer parse(String t) {
//			if(!t.startsWith("/") && t.length()>0 && t.charAt(0)!='(' && t.charAt(0)!=LAMBDA_CHAR){
//    			t="/"+t;
//    		}
			return fun.apply(fromUriPath(t));
		}
	}
	public static class LongBasedLambdaArgumentParser extends LambdaArgumentParser{
		Function<Long,PojoPointer> fun;
		public LongBasedLambdaArgumentParser(String key, Function<Long,PojoPointer> fun){
			super(key);
			Objects.requireNonNull(fun);
			this.fun=fun;
		}
		
		@Override
		public PojoPointer parse(String t) {
			Long l=Long.parseLong(t);
			return fun.apply(l);
		}
	}
	/**
	 * ABNF:
	 *  TBD
	 * 
	 * @param uripath
	 * @return
	 */
	public static PojoPointer fromUriPath(String uripath){
		
		if(!uripath.startsWith("/") && uripath.length()>0 && uripath.charAt(0)!='(' && uripath.charAt(0)!=LAMBDA_CHAR){
			uripath="/"+uripath;
		}
		PojoPointer root=new IdentityPath();
		PojoPointer parent=root;
		
		Supplier<String> paths = splitUriPaths(uripath,'/');
		
		
		String jp=paths.get();
		for(int i=0;jp!=null;i++){
			
			int p=jp.indexOf("(");
			int l=jp.indexOf(LAMBDA_CHAR);
			
			
			String field=jp;
			if(p>=0 || l>=0){
				int to = Math.min(p, l);
				if(to<0)to=Math.max(p, l);
				field=field.substring(0, to);
			}
			
            if(i==0){
            	if(p<0 && l<0){
            		jp=paths.get();
            		continue;
            	}else{
            		field=null;
            	}
            }	
            
            boolean raw=false;
            
            if(field!=null){
            	if (field.length()>0 && field.charAt(0) == RAW_CHAR){
                	raw=true;
                	field = field.substring(1);
                }
            	System.out.println("Field is:" + field);
    			PojoPointer c=new ObjectPath(field);
    			parent.setTail(c);
    			parent=c;
            }else{
            	PojoPointer.IdentityPath c = new IdentityPath();
            	parent.setTail(c);
    			parent=c;
            }
            
            Supplier<Tuple<ELEMENT_TYPE,String>> parser=parse("_" + jp,0);
            
            
            for(Tuple<ELEMENT_TYPE,String> tup=parser.get();tup!=null;tup=parser.get()){
            	switch(tup.k()){
				case FIELD:
					System.out.println("Not going to parse field:" + tup.v() + " already did!");
					break;
				case LAMBDA:
					System.out.println("It's a lambda");
					String lambdaString = tup.v();
					
					if(lambdaString.startsWith("(")){
	            		lambdaString="map"+lambdaString;
	            	}
					
					String key=parse("_" + lambdaString,0).get().v();
					System.out.println("Found key:" + key);
					
					PojoPointer pp = Registry.getPojoPointerParser(key)
										     .apply(lambdaString);
					
					parent.setTail(pp);
    				parent=pp;
    				
					break;
				case LOCATOR:
					System.out.println("It's:" + root.toURIpath());
					
					boolean isNumber = tup.v().chars().skip(1).allMatch(Character::isDigit);
	            	if(tup.v().startsWith(ARRAY_CHAR) && isNumber){
	            		PojoPointer.ArrayPath ap = new ArrayPath(Integer.parseInt(tup.v().substring(1)));
	    				parent.setTail(ap);
	    				parent=ap;
	            	}else if(tup.v().contains(":")){
	            		PojoPointer.FilterPath fp = new FilterPath(tup.v());
	    				parent.setTail(fp);
	    				parent=fp;
	            	}else{
	            		PojoPointer.IDFilterPath fp = new IDFilterPath(tup.v());
	            		
	    				parent.setTail(fp);
	    				parent=fp;
	            	}
	            	System.out.println("Now it's:" + root.toURIpath());
	            	break;
				default:
					break;
            	
            	}
            }
            
            
			if(raw){
				parent.setRaw(true);
			}
        	jp=paths.get();
		}
		return root;
	}
	
	
	public static abstract class AbstractPath implements PojoPointer{
		private boolean isRaw=false;
		private PojoPointer child=null;
		@Override
		public void setTail(PojoPointer child){
			this.child=child;
		}
		@Override
		public PojoPointer tail(){
			return this.child;
		}
		@Override
		public void setRaw(boolean r){
			this.isRaw=r;
		}
		@Override
		public boolean isRaw(){
			return this.isRaw;
		}
		
		@Override
		public JsonPointer toJsonPointer(){
			String s=thisJsonPointerString();
			if(this.child!=null){
				JsonPointer jp = this.child.toJsonPointer();
				s+=jp.toString();
			}
			return JsonPointer.compile(s);
		}
		
		@Override
		public String toURIpath(){
			String uri=thisURIPath();
			if(this.child!=null){
				uri+= this.child.toURIpath();
			}
			return uri;
		}
		
		protected abstract String thisURIPath();
		protected abstract String thisJsonPointerString();
	}
	public static final class IdentityPath extends PojoPointer.AbstractPath{
		final static JsonPointer jsonIdentity=JsonPointer.compile("");
		final static String uriIdentity="";
		public IdentityPath(){}

		@Override
		protected String thisJsonPointerString() {
			throw new UnsupportedOperationException("`thisJsonPointerString` on IdentityPath should not be called");
		}
		@Override
		protected String thisURIPath() {
			throw new UnsupportedOperationException("`thisURIPath` on IdentityPath should not be called");
		}
		
		@Override
		public JsonPointer toJsonPointer(){
			if(this.hasTail()){
				return this.tail().toJsonPointer();
			}
			return jsonIdentity;
		}
		
		@Override
		public String toURIpath(){
			if(this.hasTail()){
				return this.tail().toURIpath();
			}
			return uriIdentity;
		}
    }
	
	public static class ObjectPath extends PojoPointer.AbstractPath{
		private String f;
    	public ObjectPath(String field){
    		f=field;
    	}
		
		@Override
		protected String thisJsonPointerString() {
			return "/" + f;
		}

		@Override
		protected String thisURIPath() {
			return thisJsonPointerString();
		}
		
		public String getField(){
			return f;
		}
    }
	
    public static class ArrayPath extends PojoPointer.AbstractPath{
    	private int index=0;
    	public ArrayPath(int i){
    		this.index=i;
    	}
    	@Override
		protected String thisJsonPointerString() {
			return "/" + index;
		}
    	@Override
		protected String thisURIPath() {
			return "($" + index + ")";
		}
    	
    	public int getIndex(){
			return index;
		}
    }
    public static class IDFilterPath extends PojoPointer.LambdaPath{
    	private String id;
    	public IDFilterPath(String id){
    		Objects.requireNonNull(id);
    		this.id=id;
    	}
    	
    	public String getId(){
    		return this.id;
    	}
    	
		@Override
		protected String thisURIPath() {
			return "(" + id + ")";
		}
    }
    
    public static class FilterPath extends PojoPointer.LambdaPath{
    	private static final String VALUE_EQUALS = ":";
		//String filter;
    	private PojoPointer field;
    	private String value;
    	
    	//TODO: Should be a lucene thing?
		public FilterPath(String filter) {
			
			String[] fsplit=filter.split(VALUE_EQUALS);
			if(fsplit.length!=2){
				throw new IllegalStateException("Filters must specify one field, and one value");
			}
			setField(PojoPointer.fromUriPath(fsplit[0]));
			setValue(fsplit[1]);
		}

		@Override
		protected String thisURIPath() {
			return "(" + getField().toURIpath() + VALUE_EQUALS + getValue()  + ")";
		}
    	
		public PojoPointer getFieldPath(){
			return getField();
		}
		public String getFieldValue(){
			return getValue();
		}

		public String getValue() {
			return value;
		}

		public void setValue(String value) {
			this.value = value;
		}

		public PojoPointer getField() {
			return field;
		}

		public void setField(PojoPointer field) {
			this.field = field;
		}
    }
    
    public static class MapPath extends PojoPointer.SinglePathNamedLambdaPath{
    	public MapPath(PojoPointer field){
    		super(field);
    	}
		@Override
		public String name() {
			return "map";
		}
    }
    
    public static class FlatMapPath extends PojoPointer.SinglePathNamedLambdaPath{
    	public FlatMapPath(PojoPointer field){
    		super(field);
    	}
		@Override
		public String name() {
			return "flatmap";
		}
    }
    
    public static class CountPath extends PojoPointer.SinglePathNamedLambdaPath{
    	public CountPath(PojoPointer field){
    		super(field);
    	}
		@Override
		public String name() {
			return "count";
		}
    }
    
    public static abstract class SingleElementPath<T> extends PojoPointer.LambdaPath{
    	T t;
    	public SingleElementPath(T t){
    		this.t=t;
    	}
    	
    	public T getValue(){
    		return t;
    	}
    	
    	public void setValue(T t){
    		this.t=t;
    	}
    	
    	public abstract String name();

		@Override
		protected String thisURIPath() {
			return LAMBDA_CHAR + name() + "(" + t + ")";
		} 
    }
    
    public static class LimitPath extends PojoPointer.SingleElementPath<Long>{
    	public LimitPath(Long l){
    		super(l);
    	}
		@Override
		public String name() {
			return "limit";
		}
    }
    public static class SkipPath extends PojoPointer.SingleElementPath<Long>{
    	public SkipPath(Long l){
    		super(l);
    	}
		@Override
		public String name() {
			return "skip";
		}
    }
    
    public static class DistinctPath extends PojoPointer.SinglePathNamedLambdaPath{
    	public DistinctPath(PojoPointer field){
    		super(field);
    	}
		@Override
		public String name() {
			return "distinct";
		}
    }
    
    /**
     * {@link LambdaPath} which is used to group elements together based on
     * a matching element found at {@link #getField()}. The key to be used for
     * the mapping should be the same values found.
     * @author peryeata
     *
     */
    public static class GroupPath extends PojoPointer.SinglePathNamedLambdaPath{
    	
    	public GroupPath(PojoPointer field){
    		super(field);
    	}
    	
		@Override
		public String name() {
			return "group";
		}
    }
    public static class FieldPath extends PojoPointer.SinglePathNamedLambdaPath{
    	public FieldPath(PojoPointer field){
    		super(field);
    	}
    	
		@Override
		public String name() {
			return "$fields";
		}
    }
    public static class SortPath extends PojoPointer.SinglePathNamedLambdaPath{
    	boolean rev=false;
    	
    	public SortPath(PojoPointer field, boolean rev){
    		super(field);
    		this.rev=rev;
    	}
    	
    	public boolean isReverse(){
    		return rev;
    	}
    	
		@Override
		public String name() {
			if(!rev)
				return "sort";
			else
				return "revsort";
		}
    }
    
    
    public static abstract class SinglePathNamedLambdaPath extends PojoPointer.LambdaPath{
    	private PojoPointer field;
    	public SinglePathNamedLambdaPath(PojoPointer pp){
    		field=pp;
    	}
    	@Override
		protected String thisURIPath() {
			return LAMBDA_CHAR + name() + "(" + field.toURIpath() + ")";
		}
    	public abstract String name();
    	
    	public PojoPointer getField() {
			return this.field;
		}
    }
    
    
    public static abstract class LambdaPath extends PojoPointer.AbstractPath{
    	 
    	@Override
		protected String thisJsonPointerString() {
			throw new UnsupportedOperationException("JsonPointer unsupported for function paths");
		}
    }
    
    static class Registry{
    	static CachedSupplier<Map<String,Function<String,PojoPointer>>> subURIparsers= CachedSupplier.of(()->{
    		Map<String,Function<String,PojoPointer>> map = new HashMap<>();
    		
    		
    		
    		//Needs an argument, definitely
    		map.put("map", new FieldBasedLambdaArgumentParser("map", (p)->new MapPath(p)));
    		
    		//Can use an argument, definitely
    		map.put("sort", new FieldBasedLambdaArgumentParser("sort", (p)->new SortPath(p,false)));
    		map.put("revsort", new FieldBasedLambdaArgumentParser("revsort", (p)->new SortPath(p,true)));
    		map.put("flatmap", new FieldBasedLambdaArgumentParser("flatmap", (p)->new FlatMapPath(p)));
    		
    		
    		map.put("distinct", new FieldBasedLambdaArgumentParser("distinct", (p)->new DistinctPath(p)));
    		
    		//Probably doesn't need an argument
    		map.put("count", new FieldBasedLambdaArgumentParser("count", (p)->new CountPath(p)));
    		
    		
    		//Not for collections
    		map.put("$fields", new FieldBasedLambdaArgumentParser("$fields", (p)->new FieldPath(p)));
    		
    		
    		map.put("group", new FieldBasedLambdaArgumentParser("group", (p)->new GroupPath(p)));
    		
    		map.put("limit", new LongBasedLambdaArgumentParser("limit", (p)->new LimitPath(p)));
    		
    		map.put("skip", new LongBasedLambdaArgumentParser("skip", (p)->new SkipPath(p)));
    		
    		
    		return map;
    	});
    	
    	public static Function<String,PojoPointer> getPojoPointerParser(String key){
    		return subURIparsers.get().get(key);
    	}
		
    	
    }
    
    
}