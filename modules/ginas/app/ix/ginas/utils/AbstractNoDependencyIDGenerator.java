package ix.ginas.utils;

/**
 * An ID Generator that can generate a new ID without relying on any
 * property of the passed in object.
 *
 * @param <Ignored> the type of object passed into the generated method which is not used to generate the ID.
 * @param <K>
 */
public abstract class AbstractNoDependencyIDGenerator<Ignored, K> implements IdGeneratorForType<Ignored, K>{
    /**
     * Generates the ID by delegating to {@link #generateID()}.
     *
     * @implNote This is the same as
     *
     * <pre>
     * {@code
     * public K generateId(Ignored ignored) {
     *     return generateID();
     * }
     * }
     * </pre>
     * @param ignored the passed in object that isn't used to generate the ID.
     * @return  the new ID; will not be null.
     */
    @Override
    public K generateId(Ignored ignored) {
        return generateID();
    }

    /**
     * Generate a new ID.
     * @return the new ID can not be null.
     */
    public abstract K generateID();
}
