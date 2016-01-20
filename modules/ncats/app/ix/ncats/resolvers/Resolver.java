package ix.ncats.resolvers;

public interface Resolver<T> {
    /**
     * Return the type that this resolver resolves to
     */
    public Class<T> getType ();
    
    /**
     * Return the name of this resolver
     */
    public String getName ();
    
    /**
     * Given a name/token, resolve 
     */
    public T resolve (String name);
}
