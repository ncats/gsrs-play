package ix.core.utils.executor;

/**
 * Listener for process progress.
 */
public interface ProcessListener {
    /**
     * Starting a new process
     */
    void newProcess();

    /**
     * Finished a process.
     */
    void doneProcess();

    /**
     * The following object was
     * successfully processed.
     * @param o the object that was re-indexed.
     */
    void recordProcessed(Object o);

    /**
     * An error occurred.
     * @param t the {@link Throwable} that caused the error.
     */
    void error(Throwable t);

    /**
     * The total number of records that are to be processed.
     * This method will be called before {@link #doneProcess()}
     * but is not guaranteed to be be called before
     * {@link #recordProcessed(Object)} because some implementations
     * may compute the total asynchronously.
     *
     * @param total the total number of records to be processed.
     */
    void totalRecordsToProcess(int total);

    void countSkipped(int numSkipped);
    
    
    public static ProcessListener doNothingListener(){
    	return new ProcessListener(){
			@Override
			public void newProcess() {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void doneProcess() {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void recordProcessed(Object o) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void error(Throwable t) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void totalRecordsToProcess(int total) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void countSkipped(int numSkipped) {
				// TODO Auto-generated method stub
				
			}
    		
    	};
    }
}
