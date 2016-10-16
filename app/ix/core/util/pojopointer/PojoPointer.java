package ix.core.util.pojopointer;

import com.fasterxml.jackson.core.JsonPointer;

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
	/**
	 * Parses the supplied JsonPointer into a PojoPointer.
	 * 
	 * @param jp
	 * @return
	 */
	public static PojoPointer fromJsonPointer(JsonPointer jp){
		final PojoPointer root=new IdentityPath();
		PojoPointer parent=root;

		for(;jp!=null;jp=jp.tail()){
			if(jp.matches()) {
				continue;
			}
			final String prop=jp.getMatchingProperty();
			final int pp=jp.getMatchingIndex();
			PojoPointer c=null;
			if(pp>=0){
				c = new ArrayPath(pp);
			}else{
				c = new ObjectPath(prop);
			}
			parent.tail(c);
			parent=c;
		}
		return root;
	}

	/**
	 * Parses the supplied string to a JsonPointer, and then
	 * into a PojoPointer.
	 * 
	 * @param jp
	 * @return
	 */
	public static PojoPointer fromJsonPointer(final String jp){
		return PojoPointer.fromJsonPointer(JsonPointer.compile(jp));
	}


	
	/**
	 * ABNF:
	 *  TBD
	 * 
	 * @param uripath
	 * @return
	 */
	public static PojoPointer fromUriPath(String uripath){
		return URIPojoPointerParser.fromURI(uripath);
	}

	
	default boolean hasTail(){
		final PojoPointer tail = tail();
		return tail!=null;
	}

	default boolean isLeafRaw(){
		PojoPointer pp=this;
		while(pp.hasTail()){
			pp=pp.tail();
		}
		return pp.isRaw();
	}

	public boolean isRaw();
	public void setRaw(boolean raw);
	public void tail(PojoPointer pp);
	public PojoPointer tail();
	public JsonPointer toJsonPointer();
	public String toURIpath();

}