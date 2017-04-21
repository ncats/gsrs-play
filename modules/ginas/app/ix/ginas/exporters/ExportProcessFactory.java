package ix.ginas.exporters;

import ix.core.controllers.EntityFactory;
import ix.core.models.Principal;
import ix.core.plugins.IxCache;
import ix.core.util.CachedSupplier;
import ix.core.util.ConfigHelper;
import ix.ginas.controllers.plugins.GinasSubstanceExporterFactoryPlugin;
import ix.ginas.models.v1.Substance;
import ix.utils.CallableUtil;
import play.Play;
import play.api.mvc.Result;
import play.libs.F;

import java.io.*;
import java.time.ZoneId;
import java.util.Objects;
import java.util.Optional;
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


    //need to synchronize
    public synchronized ExportProcess getProcess(ExportMetaData metaData, Supplier<Stream<Substance>> substanceSupplier) throws Exception{
        String key = getKey(metaData);

        return IxCache.getOrElse(key, CallableUtil.TypedCallable.of(()->createExportProcessFor(metaData, substanceSupplier), ExportProcess.class));


    }

    public static String getKeyFor(String collectionId,String extension, boolean publicOnly){
       // return new StringBuilder().append(collectionId).append('/').append(extension).append('/').append(publicOnly).toString();
        StringBuilder builder = new StringBuilder(collectionId);

        if(!publicOnly){
            builder.append("_private");
        }
        builder.append('.').append(extension);

        return builder.toString();
    }

    public static Optional<ExportProcess.State> getStatusFor(String username, String collectionId, String extension, boolean publicOnly) throws IOException{
        String key = getKeyFor(collectionId, extension, publicOnly);

        System.out.println("username = " + username);
        //don't need to get check the cache just the file system

        File[] files = getFiles(getExportDirFor(username), key);
        File metaDataFile = files[1];
        if(!metaDataFile.exists()){
            return Optional.empty();
        }
        try {
            ExportMetaData metaData = EntityFactory.EntityMapper.FULL_ENTITY_MAPPER().readValue(metaDataFile, ExportMetaData.class);
            System.out.println("metaData Finished = " + metaData.finished);
            if (metaData.finished != null) {
                //guess it's done
                return Optional.of(ExportProcess.State.DONE);
            }

        }catch(Exception e){
            //something happened when trying to read
            //maybe other thread edited it while we were reading?
            //or only wrote partial file?
            e.printStackTrace();
        }
        return Optional.ofNullable(ExportProcess.State.RUNNING);
    }

    public static InputStream download(String username, String collectionId, String extension, boolean publicOnly) throws IOException{
        String key = getKeyFor(collectionId, extension, publicOnly);

        //don't need to get check the cache just the file system

        File[] files = getFiles(getExportDirFor(username), key);
        File downloadFile = files[0];
        if(downloadFile.exists()){
            return new BufferedInputStream(new FileInputStream(downloadFile));
        }
        throw new FileNotFoundException("could not find file for user "+ username + " collectionId" + collectionId + " extension " + extension + ((!publicOnly)?" with private data" : ""));
    }



    private ExportProcess createExportProcessFor(ExportMetaData metadata, Supplier<Stream<Substance>> substanceSupplier){

        //might be a better way to do this as a one-liner using paths
        //but I don't think Path's path can contain null
        String username = metadata.username;
        File[] filename = getFiles(metadata, username);

        return new ExportProcess(filename[0], metadata, filename[1], substanceSupplier);
    }

    private File[] getFiles(ExportMetaData metadata, String username) {
        File exportDir = getExportDirFor(username);

        return createFilesFrom(exportDir, metadata);
    }

    private static File getExportDirFor(String username) {
        return new File((String) ConfigHelper.getOrDefault("export.path.root", "exports"), username);
    }

    private File[] createFilesFrom(File parent, ExportMetaData metadata) {
        String key = getKey(metadata);

        return getFiles(parent, key);

    }

    private static File[] getFiles(File exportDir, String key) {
        return new File[]{
                (new File(exportDir, key)),
                (new File(exportDir, key+".metadata"))
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
