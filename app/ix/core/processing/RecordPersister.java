package ix.core.processing;

import ix.core.plugins.GinasRecordProcessorPlugin.TransformedRecord;

import java.lang.reflect.Constructor;

public abstract class RecordPersister<K,T>{
		public abstract void persist(TransformedRecord<K,T> prec) throws Exception;

		public static RecordPersister getInstanceOfPersister(String className) {
			try{
				Class<?> clazz = Class.forName(className);
				Constructor<?> ctor = clazz.getConstructor();
				RecordPersister object = (RecordPersister) ctor.newInstance();
				return object;
			}catch(Exception e){
				return null;
			}
		}		
	}
	
	