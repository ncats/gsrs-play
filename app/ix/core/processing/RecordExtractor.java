package ix.core.processing;

import ix.core.controllers.PayloadFactory;
import ix.core.models.Payload;
import ix.core.stats.Estimate;
import ix.core.util.IOUtil;

import java.io.Closeable;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.util.Iterator;

public abstract class RecordExtractor<K> implements Closeable {
		
		public InputStream is;
		public RecordExtractor(InputStream is){
			this.is=is;
		}
		/**
		 * Gets the next record. Should return null if finished, and throw an exception if
		 * there's an error
		 * 
		 * @return
		 */
		abstract public K getNextRecord() throws Exception;
		abstract public void close();
		
		public Iterator<K> getRecordIterator(){
			return new Iterator<K>(){
				private K cached=null;
				private Exception e=null;
				private boolean isCached=false;
				private boolean lastError=false;
				private boolean done=false;
				@Override
				public boolean hasNext() {
					if(done)return false;
					if(!isCached){
						cacheNext();
					}
					if(cached==null && !lastError){
						return false;
					}
					return true;
					
				}
				private void cacheNext(){
					this.e=null;
					lastError=false;
					
					try{
						cached = getNextRecord();
					}catch(Exception e){
						this.e=e;
						cached=null;
						lastError=true;
					}
					isCached=true;
				}

				@Override
				public K next() {
					if(!isCached)cacheNext();
					
					K ret=cached;
					Exception ex=this.e;
					if(ex!=null){
						throw new IllegalStateException(ex);
					}
					cacheNext();
					return ret;
					
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
			for (Object m; ;) {
				try{
					m=extract.getNextRecord();
					if(m==null){
						break;
					}
				}catch(Exception e){
					
				}
				count++;
            }
			extract.close();
			return new Estimate(count, Estimate.TYPE.EXACT);
		}
		public static RecordExtractor getInstanceOfExtractor(String className){
			try{
				Class<?> clazz = IOUtil.getGinasClassLoader().loadClass(className);
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