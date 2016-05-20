package ix.ginas.utils;

public abstract class IDGenerator<K> {
	public abstract K generateID();
	public abstract boolean isValidId(K id);
}
