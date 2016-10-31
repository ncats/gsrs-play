package ix.core;

import java.util.function.Predicate;

public interface NamedResourceFilter extends Predicate<Class<?>>{
	public boolean isVisible(Class<?> nr);
	public boolean isAccessible(Class<?> nr);
	
	@Override
	default boolean test(Class<?> cls){
		return isVisible(cls);
	}
}