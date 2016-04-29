package ix.core.plugins;

/**
 * A PersistRecordWorkerFactory that can persist many records at the same time from different threads
 * concurrently.
 *
 *
 *
 * Created by katzelda on 4/28/16.
 */
public class MultiThreadedPersistRecordWorkerFactory implements GinasRecordProcessorPlugin.PersistRecordWorkerFactory {
    @Override
    public GinasRecordProcessorPlugin.PersistRecordWorker newWorkerFor(GinasRecordProcessorPlugin.PayloadExtractedRecord prg) {
        return new MultiThreadedPersistRecordWorker(prg);
    }


    private static class MultiThreadedPersistRecordWorker extends GinasRecordProcessorPlugin.PersistRecordWorker {

        public MultiThreadedPersistRecordWorker(GinasRecordProcessorPlugin.PayloadExtractedRecord prg) {
            super(prg);
        }

        @Override
        protected void doPersist(GinasRecordProcessorPlugin.TransformedRecord tr) {
            tr.persists();
        }
    }
}
