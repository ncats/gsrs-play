package ix.core;

public interface EntityProcessor<K>{
	public static class FailProcessingException extends Exception{
		
	}
	
	public void prePersist(K obj) throws FailProcessingException;
	public void postPersist(K obj) throws FailProcessingException;
	public void preRemove(K obj) throws FailProcessingException;
	public void postRemove(K obj) throws FailProcessingException;
	public void preUpdate(K obj) throws FailProcessingException;
	public void postUpdate(K obj) throws FailProcessingException;
	public void postLoad(K obj) throws FailProcessingException;
	
	
}