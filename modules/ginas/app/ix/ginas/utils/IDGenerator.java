package ix.ginas.utils;

public interface IDGenerator<K> {
	K generateID();
	boolean isValidId(K id);
}
