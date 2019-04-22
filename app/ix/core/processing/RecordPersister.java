package ix.core.processing;

import ix.core.plugins.GinasRecordProcessorPlugin.TransformedRecord;
import ix.core.util.IOUtil;

import java.lang.reflect.Constructor;

public abstract class RecordPersister<K,T>{
		public abstract void persist(TransformedRecord<K,T> prec) throws Exception;

		public static RecordPersister getInstanceOfPersister(String className) {
			try{
				Class<?> clazz = IOUtil.getGinasClassLoader().loadClass(className);
				Constructor<?> ctor = clazz.getConstructor();
				RecordPersister object = (RecordPersister) ctor.newInstance();
				return object;
			}catch(Exception e){
				return null;
			}
		}		
	}
	
	