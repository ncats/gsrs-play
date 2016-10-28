package ix.core.java8Util;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;

import javax.persistence.PostLoad;
import javax.persistence.PostPersist;
import javax.persistence.PostRemove;
import javax.persistence.PostUpdate;
import javax.persistence.PrePersist;
import javax.persistence.PreRemove;
import javax.persistence.PreUpdate;

import ix.core.EntityProcessor;
import ix.core.adapters.EntityPersistAdapter;
import ix.core.util.EntityUtils;
import ix.core.util.EntityUtils.EntityWrapper;
import ix.core.util.EntityUtils.Key;
import ix.core.util.EntityUtils.MethodMeta;

/**
 * This class was created so Ebean enchanced classes using Play 2.3 can use Java
 * 8 language features. We have to factor out all the java 8 new language
 * features into not only a separate class but a different package so they
 * aren't "enhanced" by ebean and it's java 7 bytecode parser. Not doing this
 * will cause silent errors and make persisting to the database not work.
 * 
 * 
 * Update: It appears that we have to do some work to determine which
 * packages are "OK", and which are not. I believe any that are explicitly
 * mentioned for Ebean to analyze (typically in the conf file) are the 
 * issues.
 *
 * Created by katzelda on 6/28/16.
 */
public class Java8ForOldEbeanHelper {


	/**
	 * Method to perform indexing needed. This method is just for
	 * {@link EntityPersistAdapter#makeIndexOnBean(Object)} delegation.
	 * 
	 * @param epa
	 * @param ew
	 * @throws java.io.IOException
	 */
	public static void makeIndexOnBean(EntityPersistAdapter epa, EntityWrapper<?> ew) throws java.io.IOException {
	
		try {
			epa.getTextIndexerPlugin().getIndexer().add(ew);
		} catch (Exception e) {
			System.err.println(e.getMessage());
		}

		if (ew.isEntity()) {
			makeStructureIndexesForBean(epa,ew);
			makeSequenceIndexesForBean(epa,ew);
		}
	}
	
	
	public static void makeSequenceIndexesForBean(EntityPersistAdapter epa, EntityWrapper<?> ew){
		Key k = ew.getKey();

		ew.streamSequenceFieldAndValues(d->true).map(p->p.v()).filter(s->s instanceof String).forEach(str->{
			try {
				epa.getSequenceIndexer().add(k.getIdString(), str.toString());
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
	}
	public static void makeStructureIndexesForBean(EntityPersistAdapter epa, EntityWrapper<?> ew){
		Key k = ew.getKey();

		ew.streamStructureFieldAndValues(d->true).map(p->p.v()).filter(s->s instanceof String).forEach(str->{
			try {
				epa.getStructureIndexer().add(k.getIdString(), str.toString());
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
	}
	
	public static void removeStructureIndexesForBean(EntityPersistAdapter epa, EntityWrapper<?> ew){
		Key key = ew.getKey();
		ew.getEntityInfo().getStructureFieldInfo().stream().findAny().ifPresent(s -> {
			tryTaskAtMost(() -> epa.getStructureIndexer().remove(null,key.getIdString()), t -> t.printStackTrace(), 2);
		});
	}
	public static void removeSequenceIndexesForBean(EntityPersistAdapter epa, EntityWrapper<?> ew){
		Key key = ew.getKey();
		ew.getEntityInfo().getSequenceFieldInfo().stream().findAny().ifPresent(s -> {
			tryTaskAtMost(() -> epa.getSequenceIndexer().remove(key.getIdString()), t -> t.printStackTrace(), 2);
			
		});
	}

	
	public static void deleteIndexOnBean(EntityPersistAdapter epa, EntityWrapper<?> beanWrapped) throws Exception {
		if (epa.getTextIndexerPlugin() != null){
			epa.getTextIndexerPlugin()
					.getIndexer()
					.remove(beanWrapped); 
		}
		
		if (beanWrapped.isEntity() && beanWrapped.hasKey()) {
			removeSequenceIndexesForBean(epa,beanWrapped);
			removeStructureIndexesForBean(epa,beanWrapped);
		}
	}
	
	public static BiFunction<Object,EntityProcessor,Callable> processorCallableFor(Class<?> annotation){
		return (b,ep)->{
			if(annotation.equals(PrePersist.class)){
				return ()->{
					ep.prePersist(b);
					return null;
				};
			}else if(annotation.equals(PostPersist.class)){
				return ()->{
					ep.postPersist(b);
					return null;
				};
			}else if(annotation.equals(PreUpdate.class)){
				return ()->{
					ep.preUpdate(b);
					return null;
				};
			}else if(annotation.equals(PostUpdate.class)){
				return ()->{
					ep.postUpdate(b);
					return null;
				};
			}else if(annotation.equals(PreRemove.class)){
				return ()->{
					ep.preRemove(b);
					return null;
				};
			}else if(annotation.equals(PostRemove.class)){
				return ()->{
					ep.postRemove(b);
					return null;
				};
			}else if(annotation.equals(PostLoad.class)){
				return ()->{
					ep.postLoad(b);
					return null;
				};
			}else{
				throw new IllegalArgumentException("Unknown option for:" + annotation.getName());
			}
		};
	}

	/**
	 * Recursively call {@link EntityPersistAdapter#reindex(Object, boolean)} 
	 * for all objects in object tree
	 * 
	 * @param epa
	 * @param bean
	 * @param deleteFirst
	 */
	public static void deepreindex(EntityPersistAdapter epa, EntityWrapper<?> bean, boolean deleteFirst) {
		bean.traverse().execute((p, child)->epa.reindex(child, deleteFirst));
	}

	private interface ThrowingRunnable {
		void run() throws Exception;
	}

	private static void tryTaskAtMost(ThrowingRunnable t, Consumer<Throwable> cons, int n) {
		n = Math.max(1, n);
		while (n-- > 0) {
			try {
				t.run();
				return;
			} catch (Exception e) {
				if (n == 0)
					cons.accept(e);
			}
		}
	}

}
