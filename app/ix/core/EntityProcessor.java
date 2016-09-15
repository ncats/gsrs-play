package ix.core;

public interface EntityProcessor<K>{
	public static class FailProcessingException extends Exception{
		
	}
	
	default void prePersist(K obj) throws FailProcessingException{};
	default void postPersist(K obj) throws FailProcessingException{};
	default void preRemove(K obj) throws FailProcessingException{};
	default void postRemove(K obj) throws FailProcessingException{};
	default void preUpdate(K obj) throws FailProcessingException{};
	default void postUpdate(K obj) throws FailProcessingException{};
	default void postLoad(K obj) throws FailProcessingException{};
	
	
}