package ix.core.plugins;

/**
 * A PersistRecordWorkerFactory that can persist only a single record at the same time even if in a multi-thread
 * enviornment.  Some database implementations can't save transactions across multiple threads so
 * this implementation should be used for them.
 *
 *
 *
 * Created by katzelda on 4/28/16.
 */
public class SingleThreadedPersistRecordWorkerFactory implements GinasRecordProcessorPlugin.PersistRecordWorkerFactory
{
    @Override
    public GinasRecordProcessorPlugin.PersistRecordWorker newWorkerFor(GinasRecordProcessorPlugin.PayloadExtractedRecord prg) {
        return new SingleThreadedPersistRecordWorker(prg);
    }

    public static class SingleThreadedPersistRecordWorker extends GinasRecordProcessorPlugin.PersistRecordWorker {

        private static final Object lock = new Object();

        public SingleThreadedPersistRecordWorker(GinasRecordProcessorPlugin.PayloadExtractedRecord prg) {
            super(prg);
        }

        @Override
        protected void doPersist(GinasRecordProcessorPlugin.TransformedRecord tr) {
            synchronized (lock) {
                tr.persists();
            }
        }
    }
}
