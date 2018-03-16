package ix.ginas.exporters;

import java.io.*;
import java.util.Objects;
import java.util.concurrent.Future;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

import ix.core.controllers.EntityFactory;
import ix.core.util.CachedSupplier;
import ix.core.util.IOUtil;
import ix.core.util.TimeUtil;
import ix.core.util.Unchecked;
import ix.ginas.controllers.plugins.GinasSubstanceExporterFactoryPlugin;
import ix.ginas.models.v1.Substance;
import ix.utils.Util;
import play.Play;

/**
 * Created by katzelda on 4/18/17.
 */
public class ExportProcess {
    private static CachedSupplier<GinasSubstanceExporterFactoryPlugin> factoryPlugin = CachedSupplier
            .of(() -> Play.application().plugin(GinasSubstanceExporterFactoryPlugin.class));

    private ExportDir.ExportFile<ExportMetaData> exportFile;



    private State currentState = State.INITIALIZED;
    
    
    
    private Exporter<Substance> exporter;
    private final Supplier<Stream<Substance>> substanceSupplier;

    public ExportMetaData getMetaData() {
        try {
            return exportFile.getMetaData().get();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

    }

    public ExportProcess(ExportDir.ExportFile<ExportMetaData> exportFile, Supplier<Stream<Substance>> substanceSupplier){
        this.exportFile= Objects.requireNonNull(exportFile);

        this.substanceSupplier = Objects.requireNonNull(substanceSupplier);
    }


    public synchronized Future<?> run(Function<OutputStream, Exporter<Substance>> exporterFunction) throws IOException{

        if(currentState != State.INITIALIZED){
            return null;
        }
        if(exporterFunction ==null){
            throw new NullPointerException("exporter function can not be null");
        }
        OutputStream out=null;
        try{
            out = createOutputFileStream(); // throws IOException
            exporter = exporterFunction.apply(out);
            ExportMetaData metaData = exportFile.getMetaData().orElse(new ExportMetaData());
            currentState = State.RUNNING;
            metaData.started = TimeUtil.getCurrentTimeMillis();

            IOUtil.closeQuietly(() ->  exportFile.saveMetaData(metaData));
            //make another final reference to outputstream
            //so we can reference it in the lambda for submit
            //final OutputStream fout = out;
            
            Future<?> future=factoryPlugin.get().submit( ()->{
                try(Stream<Substance> sstream = substanceSupplier.get()){
                    System.out.println("Starting export");
                    sstream.peek(s -> {
                        Unchecked.ioException( () -> exporter.export(s));
                        metaData.addRecord();
                    })
                    .anyMatch(m->{
                     return metaData.cancelled;
                    });
                    
                    currentState = State.DONE;
                }catch(Throwable t){
                    t.printStackTrace();
                    currentState = State.ERRORED_OUT;
                    throw t;
                }finally{
                    
                    
                    try {
                    	File f=exportFile.getFile();
                    	
                        metaData.sha1=Util.sha1(f);
                        metaData.size=f.length();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    metaData.finished=TimeUtil.getCurrentTimeMillis();
                    
                    IOUtil.closeQuietly(() ->  exportFile.saveMetaData(metaData));
                    IOUtil.closeQuietly(exporter);
                    
                    
                    //IxCache.remove(metaData.getKey());
                }
            });
            
            
            return future;
        }catch(Throwable t){
            IOUtil.closeQuietly(out);
            currentState = State.ERRORED_OUT;

            throw t;
        }
    }

    private void writeMetaDataFile() throws IOException{
//        try(BufferedWriter writer = new BufferedWriter(new FileWriter(metaDataFile))){
//            EntityFactory.EntityMapper.FULL_ENTITY_MAPPER().writer().writeValue(writer, metaData);
//        }
//        exportFile.saveMetaData(metaData);
    }
    private OutputStream createOutputFileStream() throws IOException{
        return IOUtil.newBufferedOutputStream(exportFile.getFile());
    }




    public enum State{
        INITIALIZED,
        RUNNING,
        DONE,
        ERRORED_OUT;
    }
}
