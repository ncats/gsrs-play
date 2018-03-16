package ix.ginas.exporters;

import com.fasterxml.jackson.databind.ObjectMapper;
import ix.core.util.IOUtil;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.attribute.FileAttribute;
import java.util.Objects;
import java.util.Optional;

public class ExportDir<T> {

    private final File dir;

    private final File metaDir;

    private final ObjectMapper mapper = new ObjectMapper();
    private final Class<? extends T> defaultType;

    public ExportDir(File dir, Class<? extends T> type) {
        this.dir = dir;
        this.metaDir = getExportMetaDirFor(dir);
        defaultType = Objects.requireNonNull(type);
    }


    private static File getExportMetaDirFor(File parentDir){
        File metaDirectory = new File(parentDir, "meta");
        try {
            IOUtil.createDirectories(metaDirectory);
        } catch (IOException e) {
            throw new UncheckedIOException("error getting or creating export meta directory for " + parentDir.getName(), e);
        }
        return metaDirectory;
    }
    public Optional<ExportFile<T>> getFile(String filename) throws IOException{
        return getFile(filename, null);
    }
    public Optional<ExportFile<T>> getFile(String filename, Class<? extends T> clazzReader) throws IOException{

        File f = new File(dir, filename);
        if(!f.exists()){
            return Optional.empty();
        }
        File metaDataFile = getMetaDataFileFor(filename);

        return Optional.of(new ExportFile<>(f, metaDataFile, null,  mapper, clazzReader==null? defaultType : clazzReader));

    }
    public ExportFile createFile(String filename, T metaData) throws IOException{
        return createFile(filename, metaData,true);
    }
    public ExportFile createFile(String filename, T metaData, boolean overwrite) throws IOException{
        Objects.requireNonNull(filename);

        File f = new File(dir, filename);
        if(!overwrite && f.exists()){
            throw new IOException("file " + filename + " already exists");
        }
        File metaDataFile = getMetaDataFileFor(filename);
        if(metaData !=null) {

            mapper.writeValue(metaDataFile, metaData);
        }
        return new ExportFile(f, metaDataFile, metaData, mapper, defaultType);
    }



    public File getMetaDataFileFor(String filename) {
        return new File(metaDir, filename +".metadata");
    }

    public static class ExportFile<T>{

        private final File file, metaDataFile;
        private T metaData;

        private final ObjectMapper mapper;

        private final Class<? extends T> type;

        private ExportFile(File file, File metaDataFile, T metaData, ObjectMapper mapper, Class<? extends T> type) {
            this.file = Objects.requireNonNull(file);
            this.metaData = metaData;
            this.mapper = mapper;
            this.type = type;
            this.metaDataFile = metaDataFile;
            this.metaData=metaData;
        }

        public Optional<T> getMetaData() throws IOException{
            if(metaData !=null){
                return Optional.of(metaData);
            }
            if(metaDataFile ==null || !metaDataFile.exists()){
                return Optional.empty();
            }
                return Optional.of(mapper.readValue(metaDataFile, type));

        }

        public void delete(){
            //should we throw an error if delete fails? (or use new Java 7 Files.delete()
            //this is more fault tolerant
            file.delete();
            getMetaDataFileFor(file).delete();
        }
        public void saveMetaData(T metaData) throws IOException{
            File f = getMetaDataFileFor(file);
            mapper.writeValue(f, metaData);
            this.metaData = metaData;
        }

        private static File getMetaDataFileFor(File file) {
            return new File(new File(file.getParentFile(), "meta"), file.getName() +".metadata");
        }
        public InputStream getInputStreamOutputStream(){
            System.out.println("DOWNLOADING FILE" + file.getAbsolutePath());
            try {
                return new BufferedInputStream(new FileInputStream(file));
            } catch (FileNotFoundException e) {
                //shouldn't happen
                throw new UncheckedIOException(e);
            }
        }
        public OutputStream getBufferedOutputStream(){
            try {
                return new BufferedOutputStream(new FileOutputStream(file));
            } catch (FileNotFoundException e) {
                //shouldn't happen
                throw new UncheckedIOException(e);
            }
        }
        public File getFile() {
            return file;
        }

        public File getMetaDataFile() {
            return getMetaDataFileFor(file);
        }
    }
}
