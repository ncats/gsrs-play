package ix.ginas.utils;

public interface NamedIdGenerator<T, K> extends IdGeneratorForType<T,K> {
    /**
     * Gets the name of this ID that is generated.
     * @return
     */
    String getName();
}
