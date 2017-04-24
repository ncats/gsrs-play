package ix.ginas.exporters;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import ix.core.controllers.EntityFactory;
import ix.core.controllers.EntityFactory.EntityMapper;
import ix.core.plugins.IxCache;
import ix.core.util.CachedSupplier;
import ix.core.util.ConfigHelper;
import ix.ginas.controllers.plugins.GinasSubstanceExporterFactoryPlugin;
import ix.ginas.models.v1.Substance;
import ix.utils.CallableUtil;
import play.Play;

/**
 * Created by katzelda on 4/18/17.
 */
public class ExportProcessFactory {

    private static CachedSupplier<GinasSubstanceExporterFactoryPlugin> factoryPlugin = CachedSupplier
            .of(() -> Play.application().plugin(GinasSubstanceExporterFactoryPlugin.class));
    
    private static ConcurrentHashMap<String,ExportMetaData> inProgress = new ConcurrentHashMap<>();
    
    


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
    
    public static InputStream download(String username, String fname) throws FileNotFoundException{
        File[] files = getFiles(getExportDirFor(username), fname);
        File downloadFile = files[0];
        if(downloadFile.exists()){
            return new BufferedInputStream(new FileInputStream(downloadFile));
        }
        throw new FileNotFoundException("could not find file for user "+ username + ":" +  fname);
    }



    private ExportProcess createExportProcessFor(ExportMetaData metadata, Supplier<Stream<Substance>> substanceSupplier){

        //might be a better way to do this as a one-liner using paths
        //but I don't think Path's path can contain null
        String username = metadata.username;
        File[] filename = getFiles(metadata, username);
        
        
        if(metadata.getFilename()==null){
            metadata.setFilename(filename[0].getName());
        }
        
        inProgress.put(metadata.id, metadata);
        
        return new ExportProcess(filename[0], metadata, filename[1], substanceSupplier);
    }

    private File[] getFiles(ExportMetaData metadata, String username) {
        File exportDir = getExportDirFor(username);

        return createFilesFrom(exportDir, metadata);
    }

    private static File getExportDirFor(String username) {
        return new File((String) ConfigHelper.getOrDefault("export.path.root", "exports"), username);
    }
    
    private static File getExportMetaDirFor(File parentDir) {
        File metaDirectory = new File(parentDir, "meta");
        metaDirectory.mkdirs();
        return metaDirectory;
    }

    private File[] createFilesFrom(File parent, ExportMetaData metadata) {
        String key = getKey(metadata);

        return getFiles(parent, key);

    }

    private static File[] getFiles(File exportDir, String key) {
        
        File metaDirectory = getExportMetaDirFor(exportDir);
        
        return new File[]{
                (new File(exportDir, key)),
                (new File(metaDirectory, key+".metadata"))
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
    
    //TODO: probably change the way this is stored to make it a little easier
    //the use of metadata files, right now, is probably overkill
    //If we must have 1 metafile per, I'd like to have a separate folder
    //perhaps?
    public static List<ExportMetaData> getExplicitExportMetaData(String username){
        EntityMapper em = EntityFactory.EntityMapper.FULL_ENTITY_MAPPER();
        File metaDirectory = getExportMetaDirFor(getExportDirFor(username));
        return Arrays.stream(metaDirectory.listFiles())
                     .filter(f->f.getName().endsWith(".metadata"))
                     .map(f->{
                            try {
                                return em.readValue(f, ExportMetaData.class);
                            } catch (Exception e) {
                                return null;
                            }
                      })
                     .filter(Objects::nonNull)
                     .map(m->{
                         ExportMetaData em2=inProgress.get(m.id);
                         if(em2==null){
                             return m;
                         }else{
                             return em2;
                         }
                     })
                     .collect(Collectors.toList());
    }

    
    
    public static Optional<ExportMetaData> getStatusFor(String username, String downloadID) {
        ExportMetaData emeta=inProgress.computeIfAbsent(downloadID, (k)->{
            return getExplicitExportMetaData(username)
                        .stream()
                        .filter(em->em.id.equals(downloadID))
                        .findFirst()
                        .orElse(null);
        });
        return Optional.ofNullable(emeta);
    }
    
}
