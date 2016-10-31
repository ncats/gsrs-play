package ix.ginas.utils.reindex;

/**
 * Listener for reindexing progress.
 */
public interface ReIndexListener {
    /**
     * Starting a new re-indexing process.
     */
    void newReindex();

    /**
     * Finished a re-indexing process.
     */
    void doneReindex();

    /**
     * The following object was
     * successfully re-indexed.
     * @param o the object that was re-indexed.
     */
    void recordReIndexed(Object o);

    /**
     * An error occurred.
     * @param t the {@link Throwable} that caused the error.
     */
    void error(Throwable t);

    /**
     * The total number of records that are to be indexed.
     * This method will be called before {@link #doneReindex()}
     * but is not guaranteed to be be called before
     * {@link #recordReIndexed(Object)} because some implementations
     * may compute the total asynchronously.
     *
     * @param total the total number of records to be indexed.
     */
    void totalRecordsToIndex(int total);

    void countSkipped(int numSkipped);
}
