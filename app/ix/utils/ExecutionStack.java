package ix.utils;

public interface ExecutionStack<K> {

	void pushAndPopWith(K obj, Runnable r);

	K getFirst();

}