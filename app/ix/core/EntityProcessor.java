package ix.core;

import play.Logger;

public interface EntityProcessor<K>{
	class FailProcessingException extends Exception{
		public FailProcessingException() {
			super();
		}

		public FailProcessingException(String message) {
			super(message);
		}

		public FailProcessingException(String message, Throwable cause) {
			super(message, cause);
		}

		public FailProcessingException(Throwable cause) {
			super(cause);
		}
	}
	
	default void prePersist(K obj) throws FailProcessingException{};
	default void postPersist(K obj) throws FailProcessingException{};
	default void preRemove(K obj) throws FailProcessingException{};
	default void postRemove(K obj) throws FailProcessingException{};
	default void preUpdate(K obj) throws FailProcessingException{};
	default void postUpdate(K obj) throws FailProcessingException{};
	default void postLoad(K obj) throws FailProcessingException{};
	
	default EntityProcessor<K> combine(EntityProcessor<K> other){
		EntityProcessor<K> thisOne=this;
		return new EntityProcessor<K>(){

			@Override
			public void prePersist(K obj) throws FailProcessingException {
				thisOne.prePersist(obj);
				other.prePersist(obj);
			}

			@Override
			public void postPersist(K obj) throws FailProcessingException {
				thisOne.postPersist(obj);
				other.postPersist(obj);
			}

			@Override
			public void preRemove(K obj) throws FailProcessingException {
				thisOne.preRemove(obj);
				other.preRemove(obj);
			}

			@Override
			public void preUpdate(K obj) throws FailProcessingException {
				thisOne.preUpdate(obj);
				other.preUpdate(obj);
			}

			@Override
			public void postRemove(K obj) throws FailProcessingException {
				try{
					thisOne.postRemove(obj);
				}catch(Exception e){
					Logger.warn(e.getMessage(),e);
				}
				try{
					other.postRemove(obj);
				}catch(Exception e){
					Logger.warn(e.getMessage(),e);
				}
			}


			@Override
			public void postUpdate(K obj) throws FailProcessingException {
				try{
					thisOne.postUpdate(obj);
				}catch(Exception e){
					Logger.warn(e.getMessage(),e);
				}
				try{
					other.postUpdate(obj);
				}catch(Exception e){
					Logger.warn(e.getMessage(),e);
				}
			}

			@Override
			public void postLoad(K obj) throws FailProcessingException {
				try{
					thisOne.postLoad(obj);
				}catch(Exception e){
					Logger.warn(e.getMessage(),e);
				}
				try{
					other.postLoad(obj);
				}catch(Exception e){
					Logger.warn(e.getMessage(),e);
				}
			}
			
		};
	}
}