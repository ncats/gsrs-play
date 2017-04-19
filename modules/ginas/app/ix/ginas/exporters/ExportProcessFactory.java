package ix.ginas.exporters;

import ix.core.models.Principal;
import ix.core.util.CachedSupplier;
import ix.core.util.ConfigHelper;
import ix.ginas.controllers.plugins.GinasSubstanceExporterFactoryPlugin;
import play.Play;

import java.io.File;
import java.io.OutputStream;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by katzelda on 4/18/17.
 */
public class ExportProcessFactory {

    private static CachedSupplier<GinasSubstanceExporterFactoryPlugin> factoryPlugin = CachedSupplier
            .of(() -> Play.application().plugin(GinasSubstanceExporterFactoryPlugin.class));

    private final ConcurrentMap<String, ExportProcess> runningProcesses = new ConcurrentHashMap<>();

    public ExportProcess getProcess(String collectionId,String extension, boolean publicOnly){
        String key = getKeyFor(collectionId, extension, publicOnly);
        ExportProcess p = runningProcesses.computeIfAbsent(key, k ->{

        });

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

    private void foo(String extension, boolean publicOnly){
        if (factoryPlugin.get() == null) {
            throw new NullPointerException("could not find a factory plugin");
        }

        SubstanceExporterFactory.Parameters params = new SubstanceParameters(
                factoryPlugin.get().getFormatFor(extension),publicOnly);

        SubstanceExporterFactory factory = factoryPlugin.get().getExporterFor(params);
        if (factory == null) {
            // TODO handle null couldn't find factory for params
            throw new IllegalArgumentException("could not find suitable factory for " + params);
        }

        return factory.createNewExporter(pos, params);
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


    private OutputStream createOutputStream(ExportMetaData metadata){

        //might be a better way to do this as a one-liner using paths
        //but I don't think Path's path can contain null
        File exportDir = new File((String) ConfigHelper.getOrDefault("export.path.root", null), metadata.principal.username);
        
        String filename = createFileNameFrom(metadata);

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
