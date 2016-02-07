package ix.ginas.utils;

import ix.core.NamedResource;
import ix.core.NamedResourceFilter;

public class GinasResourceFilter implements NamedResourceFilter{

	@Override
	public boolean isVisible(Class<?> factory) {
		NamedResource nr  = (NamedResource)factory.getAnnotation(NamedResource.class);
		if(nr.name().equalsIgnoreCase("substances")){
			return true;
		}else{
			return false;
		}
	}

	@Override
	public boolean isAccessible(Class<?> factory) {
		return true;
	}

}
