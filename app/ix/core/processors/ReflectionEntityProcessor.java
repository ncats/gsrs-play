package ix.core.processors;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.PostLoad;
import javax.persistence.PostPersist;
import javax.persistence.PostRemove;
import javax.persistence.PostUpdate;
import javax.persistence.PrePersist;
import javax.persistence.PreRemove;
import javax.persistence.PreUpdate;

import ix.core.EntityProcessor;
import ix.core.util.EntityUtils.EntityInfo;
import ix.core.util.EntityUtils.EntityWrapper;
import ix.core.util.EntityUtils.Hook;
import ix.core.util.EntityUtils.MethodMeta;
import play.Logger;

public class ReflectionEntityProcessor<T> implements EntityProcessor<T> {
	private Map<Class<?>,List<Hook>> hookMap = new HashMap<>();
	

    private List<Hook> getForAnnotation(Class<?> annot) {
    	return hookMap.get(annot);
    }
    
	
    @Override
	public void prePersist(T bean) throws FailProcessingException {
    	preProcess(bean, getForAnnotation(PrePersist.class));
	}
    @Override
	public void preRemove(T bean) throws FailProcessingException {
		preProcess(bean, getForAnnotation(PreRemove.class));
	}
    @Override
	public void preUpdate(T bean) throws FailProcessingException {
		preProcess(bean, getForAnnotation(PreUpdate.class));
	}

    /**
     * Execute the supplied hooks in order, throwing an IllegalStateException
     * if one fails.
     * @param bean
     * @param methods
     */
	private void preProcess(T bean, List<Hook> methods){
		EntityWrapper<T> ew = EntityWrapper.of(bean);
		if (methods != null) {
            for (Hook m : methods) {
                try {
                    m.invoke(bean);
                }catch (Exception ex) {
                	ex.printStackTrace();
                    Logger.trace("Can't invoke method "+m.getName()+"["+ew.getKind()+"]", ex);
                    throw new IllegalStateException(ex);
                }
            }
        }
	}
	
	 /**
     * Execute the supplied hooks in order, but not throwing an exception
     * if one fails.
     * @param bean
     * @param methods
     */
	private void postProcess(T bean, List<Hook> methods){
		EntityWrapper<T> ew = EntityWrapper.of(bean);
		if (methods != null) {
            for (Hook m : methods) {
                try {
                    m.invoke(bean);
                }catch (Exception ex) {
                	ex.printStackTrace();
                    Logger.trace("Can't invoke method "+m.getName()+"["+ew.getKind()+"]", ex);
                }
            }
        }
	}
	
	@Override
	public void postPersist(T obj) throws FailProcessingException {
		postProcess(obj,getForAnnotation(PostPersist.class));
	}

	@Override
	public void postRemove(T obj) throws FailProcessingException {
		postProcess(obj,getForAnnotation(PostRemove.class));
	}

	@Override
	public void postUpdate(T obj) throws FailProcessingException {
		postProcess(obj,getForAnnotation(PostUpdate.class));
	}

	@Override
	public void postLoad(T obj) throws FailProcessingException {
		postProcess(obj,getForAnnotation(PostLoad.class));
	}

    
    
	public ReflectionEntityProcessor(EntityInfo<T> emeta) {
		if (emeta.isEntity()) {
			emeta.getMethods()
					.stream()
					.filter(MethodMeta::hasHooks)
					.forEach(mm->{
						mm.getHookTypes().stream().forEach(meth->{
							hookMap.computeIfAbsent(meth, k->new ArrayList<Hook>())
									  .add(mm.getMethodHook());
						});
					});
		} else {
			throw new IllegalArgumentException(
					emeta.getName() + " cannot use an EntityProcessor: It is not an Entity!");
		}
	}

}
