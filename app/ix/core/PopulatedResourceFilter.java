package ix.core;

import java.lang.reflect.Method;

import play.Logger;

public class PopulatedResourceFilter implements NamedResourceFilter{

	@Override
	public boolean isVisible(Class<?> factory) {
		if (factory != null) {
            try {
                Method m= factory.getMethod("getCount");
                Object c=m.invoke(null);
                if(((Integer)c)>0){
                	return true;
                }
            }catch (Exception ex) {
            	Logger.trace("Unknown method \"count\" in class "+factory.getName(), ex);
            }
        }
		return false;
	}

	@Override
	public boolean isAccessible(Class<?> nr) {
		return true;
	}

}
