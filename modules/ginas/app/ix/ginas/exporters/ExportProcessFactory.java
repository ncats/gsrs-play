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
        return createExportProcessFor(metaData, substanceSupplier);
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
        return getFiles(parent, metadata.getFilename());
    }

    private static File[] getFiles(File exportDir, String fname) {
        
        File metaDirectory = getExportMetaDirFor(exportDir);
        
        return new File[]{
                (new File(exportDir, fname)),
                (new File(metaDirectory, fname + ".metadata"))
        };
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
                     .sorted((m1,m2)->{
                    	 //Newest first
                    	return (int) (m2.started-m1.started);
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
    
    
    public static Optional<ExportMetaData> getMetaForLatestKey(String username,String emetaKey) {
    	return getExplicitExportMetaData(username).stream()
    							.filter(em->em.getKey().equals(emetaKey))
    							.findFirst();
    }
    
    public static void remove(ExportMetaData meta){
        inProgress.remove(meta.id);
        File[] files = getFiles(getExportDirFor(meta.username), meta.getFilename());
        files[0].delete();
        files[1].delete();
        
    }
    
    
    
}
