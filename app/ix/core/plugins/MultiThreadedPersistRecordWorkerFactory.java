package ix.core.plugins;

/**
 * A PersistRecordWorkerFactory that can persist many records at the same time from different threads
 * concurrently.
 * <p>
 * This class had to be made in its own file instead of
 * being a static inner class in GinasRecordProcessPlugin because it has to be instantiated
 * by reflection and however Play framework enhances classes messes up inner classes package names
 * enough that Class.forName() can't find them.
 *</p>
 * <p>
 * To select this class as the factory implementation, set
 * {@code ix.ginas.PersistRecordWorkerFactoryImpl} value to this
 * fully qualified class name in the conf file.
 *</p>
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
