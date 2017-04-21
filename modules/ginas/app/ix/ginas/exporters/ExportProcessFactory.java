package ix.ginas.exporters;

import ix.core.models.Principal;
import ix.core.plugins.IxCache;
import ix.core.util.CachedSupplier;
import ix.core.util.ConfigHelper;
import ix.ginas.controllers.plugins.GinasSubstanceExporterFactoryPlugin;
import ix.ginas.models.v1.Substance;
import ix.utils.CallableUtil;
import play.Play;

import java.io.File;
import java.io.OutputStream;
import java.time.ZoneId;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * Created by katzelda on 4/18/17.
 */
public class ExportProcessFactory {

    private static CachedSupplier<GinasSubstanceExporterFactoryPlugin> factoryPlugin = CachedSupplier
            .of(() -> Play.application().plugin(GinasSubstanceExporterFactoryPlugin.class));

    private final ConcurrentMap<String, ExportProcess> runningProcesses = new ConcurrentHashMap<>();

    //need to synchronize
    public synchronized ExportProcess getProcess(ExportMetaData metaData, Supplier<Stream<Substance>> substanceSupplier) throws Exception{
        String key = getKey(metaData);

        return IxCache.getOrElse(key, CallableUtil.TypedCallable.of(()->createExportProcessFor(metaData, substanceSupplier), ExportProcess.class));


    }

    private String getKeyFor(String collectionId,String extension, boolean publicOnly){
       // return new StringBuilder().append(collectionId).append('/').append(extension).append('/').append(publicOnly).toString();
        StringBuilder builder = new StringBuilder(collectionId);

        if(!publicOnly){
            builder.append("_private");
        }
        builder.append('.').append(extension);

        return builder.toString();
    }



    private static class SubstanceParameters implements SubstanceExporterFactory.Parameters {
        private final SubstanceExporterFactory.OutputFormat format;

        private final boolean publicOnly;
        SubstanceParameters(SubstanceExporterFactory.OutputFormat format, boolean publicOnly) {
            Objects.requireNonNull(format);
            this.format = format;
            this.publicOnly = publicOnly;
        }

        @Override
        public SubstanceExporterFactory.OutputFormat getFormat() {
            return format;
        }

        @Override
        public boolean publicOnly() {
            return publicOnly;
        }
    }


    private ExportProcess createExportProcessFor(ExportMetaData metadata, Supplier<Stream<Substance>> substanceSupplier){

        //might be a better way to do this as a one-liner using paths
        //but I don't think Path's path can contain null
        File exportDir = new File((String) ConfigHelper.getOrDefault("export.path.root", "exports"), metadata.principal.username);
        
        File[] filename = createFilesFrom(exportDir, metadata);

        return new ExportProcess(filename[0], metadata, filename[1], substanceSupplier);
    }

    private File[] createFilesFrom(File parent, ExportMetaData metadata) {
        String key = getKey(metadata);

        return new File[]{
                (new File(parent, key)),
                (new File(parent, key+".metadata"))
        };

    }

    private String getKey(ExportMetaData metadata) {
//        StringBuilder builder = new StringBuilder(metadata.collectionId);
//
//        if(!metadata.publicOnly){
//            builder.append("_private");
//        }
//        builder.append('.').append(metadata.extension);
//
//        return builder.toString();
        return getKeyFor(metadata.collectionId, metadata.extension, metadata.publicOnly);
    }
}
