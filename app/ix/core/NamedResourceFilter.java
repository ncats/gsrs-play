package ix.core;

public interface NamedResourceFilter{
	public boolean isVisible(Class<?> nr);
	public boolean isAccessible(Class<?> nr);
}