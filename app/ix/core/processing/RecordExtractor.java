package ix.core.processing;

import ix.core.controllers.PayloadFactory;
import ix.core.models.Payload;
import ix.core.stats.Estimate;

import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.util.Iterator;

public abstract class RecordExtractor<K>{
		public InputStream is;
		public RecordExtractor(InputStream is){
			this.is=is;
		}
		
		abstract public K getNextRecord();
		abstract public void close(); 
		
		public Iterator<K> getRecordIterator(){
			return new Iterator<K>(){
				private K cached;
				private boolean done=false;
				@Override
				public boolean hasNext() {
					if(done)return false;
					if(cached!=null)
						return true;
					cached = getNextRecord();
					return (cached!=null);
				}

				@Override
				public K next() {
					if(cached!=null){
						K ret=cached;
						cached=null;
						return ret;
					}
					return getNextRecord();
				}

				@Override
				public void remove() {}
				
			};
		}		
		
		public abstract RecordExtractor<K> makeNewExtractor(InputStream is);
		
		public RecordExtractor<K> makeNewExtractor(Payload p){
			InputStream pis=PayloadFactory.getStream(p);
			return makeNewExtractor(pis);
		}
		
		/**
		 * Count records extracted from payload.
		 * 
		 * By default, this is implemented naively by iteration.
		 * 
		 * Should be overridden to take advantage of better assumptions.
		 * @param p
		 * @return
		 */
		public Estimate estimateRecordCount(Payload p){
			long count=0;
			RecordExtractor extract = makeNewExtractor(p);
			for (Object m; (m = extract.getNextRecord()) != null;count++) {}
			extract.close();
			return new Estimate(count, Estimate.TYPE.EXACT);
		}
		public static RecordExtractor getInstanceOfExtractor(String className){
			try{
				Class<?> clazz = Class.forName(className);
				Constructor<?> ctor = clazz.getConstructor(InputStream.class);
				RecordExtractor object = (RecordExtractor) ctor.newInstance(new Object[] { null });
				return object;
			}catch(Exception e){
				return null;
			}
		}
		public abstract RecordTransformer getTransformer();
		
		public RecordTransformer getTransformer(Payload p){
			return getTransformer();
		}
		
	}