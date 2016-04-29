package ix.core.plugins;

/**
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
