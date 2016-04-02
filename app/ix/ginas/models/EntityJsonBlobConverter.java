package ix.ginas.models;

import java.io.IOException;

import ix.core.controllers.EntityFactory;
import ix.core.controllers.EntityFactory.EntityMapper;

public abstract class EntityJsonBlobConverter<K> extends EntityBlobConverter<K> {
	public EntityMapper em =  EntityFactory.EntityMapper.FULL_ENTITY_MAPPER();
	private Class<K> cls;
	
	public EntityJsonBlobConverter(Class<K> cls) {
		super(cls);
		this.cls=cls;
	}

	@Override
	public byte[] convertToBytes(K value) throws IOException {
		return em.writeValueAsBytes(value);
	}

	@Override
	public K convertFromBytes(byte[] bytes) throws IOException {
		if(bytes==null)return null;
		return em.readValue(bytes, cls);
	}
	
	    


    
}