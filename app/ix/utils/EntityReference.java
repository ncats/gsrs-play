package ix.utils;

import ix.core.util.EntityUtils.EntityWrapper;

public class EntityReference<K>{
	private K o;
	public EntityReference(K o){
		this.o=o;
	}
	
	public K get(){
		return o;
	
	}
	
	
	public String getKey(){
		return EntityWrapper.of(o)
				            .getOptionalKey()
				            .map(k->k.toString())
				            .orElse(null);
	}
	
	
	@Override
	public int hashCode(){
		return getKey().hashCode();
	}
	@Override
	public boolean equals(Object oref){
		if(oref==null)return false;
		if(oref instanceof EntityReference){
			EntityReference<?> or=(EntityReference<?>)oref;
			
			
			return this.getKey().equals(or.getKey());
		}
		return false;
	}
	public static <K> EntityReference<K> of(K k) {
		return new EntityReference<K>(k);
	}
	
	public String toString(){
		return "Ref to:" + o.toString();
	}
}