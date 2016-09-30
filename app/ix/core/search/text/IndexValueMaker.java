package ix.core.search.text;

import java.util.function.Consumer;

/**
 * An IndexValueMaker is intended to be used to produce indexable fields, 
 * facets, suggest fields, etc from a given entity.
 * 
 * Specifically, the interface provides a mechanism to start from an Object
 * t, and deliver IndexableValues via a supplied consumer. 
 * 
 * 
 * @author peryeata
 *
 * @param <T>
 */
public interface IndexValueMaker<T> {
	/**
	 * Creates IndexableValues out of the given Entity T, and returns them
	 * to an awaiting Consumer. The IndexableValues may then be used to
	 * populate a full text index, type-ahead suggest, sorting function 
	 * and/or facets.
	 * 
	 * @param t
	 * @param consumer
	 */
	public void createIndexableValues(T t, Consumer<IndexableValue> consumer);
	
	
	/**
	 * Combine 2 IndexValueMakers together, so that
	 * each is called sequentially.
	 * @param other
	 * @return
	 */
	default IndexValueMaker<T> and(IndexValueMaker<T> other){
		return (t,c)->{
			this.createIndexableValues(t, c);
			other.createIndexableValues(t, c); 
		};
	}
}
