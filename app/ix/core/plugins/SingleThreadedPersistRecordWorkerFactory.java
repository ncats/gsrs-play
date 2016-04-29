package ix.core.plugins;

/**
 * A PersistRecordWorkerFactory that can persist only a single record at the same time even if in a multi-thread
 * enviornment.  Some database implementations can't save transactions across multiple threads so
 * this implementation should be used for them.
 *<p>
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
