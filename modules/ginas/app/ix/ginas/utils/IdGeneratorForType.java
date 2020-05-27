package ix.ginas.utils;

/**
 * An ID Generator which may use properties of a given type
 * to compute the new ID.
 * @param <T> The object type to generate the ID from.
 * @param <K> The type of the generated ID.
 *
 * @author katzelda
 * @since 2.5.1.2
 */
public interface IdGeneratorForType<T, K> {
    /**
     * Generate a new ID which may use properties of the passed in object.
     * @param obj the object that may be used to help generate the new ID;
     *            will never be null.
     * @return a new ID will never be null.
     */
    K generateId(T obj);

    /**
     * Is the given id passes the formatting and
     * validation rules for this type of ID- This is not a existence
     * check! the passed in value may or may not exist yet.
     *
     * @param id the id to check- should not be null.
     * @return {@code true} if this passed in id is valid; {@code false} otherwise.
     */
    boolean isValidId(K id);
}
