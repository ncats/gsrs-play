package ix.core;

public interface EntityProcessor<K>{
	
	public void prePersist(K obj);
	public void postPersist(K obj);
	public void preRemove(K obj);
	public void postRemove(K obj);
	public void preUpdate(K obj);
	public void postUpdate(K obj);
	public void postLoad(K obj);
	
	
}