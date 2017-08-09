package ix.ginas.exporters;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Objects;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
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
    private final File outputFile;
    private final File metaDataFile;

    private final ExportMetaData metaData;

    private State currentState = State.INITIALIZED;
    
    
    
    private Exporter<Substance> exporter;
    private final Supplier<Stream<Substance>> substanceSupplier;

    public ExportMetaData getMetaData() {
        return metaData;
    }

    public ExportProcess(File outputFile, ExportMetaData metaData, File metaDataFile, Supplier<Stream<Substance>> substanceSupplier) {
        this.metaDataFile = metaDataFile;
        this.outputFile = outputFile;
        this.metaData = metaData;
        this.substanceSupplier = Objects.requireNonNull(substanceSupplier);
    }
    public synchronized Future<?> run(Function<OutputStream, Exporter<Substance>> exporterFunction) throws IOException{
        return run(exporterFunction, new AtomicBoolean(false));
    }
    public synchronized Future<?> run(Function<OutputStream, Exporter<Substance>> exporterFunction,
                                      AtomicBoolean cancelledFlag
                                      ) throws IOException{
        System.out.println("run state = " + currentState);
        if(currentState != State.INITIALIZED){
            return null;
        }
        if(exporterFunction ==null){
            throw new NullPointerException("exporter function can not be null");
        }
        OutputStream out=null;
        try{
            out = createOutputFileStream(); // throws IOException
            System.out.println("output stream = " + out);
            exporter = exporterFunction.apply(out);

            currentState = State.RUNNING;
            metaData.started = TimeUtil.getCurrentTimeMillis();

            IOUtil.closeQuietly(this::writeMetaDataFile);
            //make another final reference to outputstream
            //so we can reference it in the lambda for submit
            //final OutputStream fout = out;
            
            Future<?> future=factoryPlugin.get().submit( ()->{
                try(Stream<Substance> sstream = substanceSupplier.get()){
                    System.out.println("Starting export");
                   sstream
                            .peek( s -> System.out.println(this.metaData.cancelled))

                            .peek(s -> {
                                            if(!this.metaData.cancelled){
                                                Unchecked.ioException( () -> exporter.export(s));
                                                this.metaData.addRecord();
                                            }

                                        })
                            .filter(s -> this.metaData.cancelled)
                           .peek(s->System.out.println("was cancelled"))
                            .findFirst();


                    currentState = State.DONE;

                }catch(Throwable t){
                    t.printStackTrace();
                    currentState = State.ERRORED_OUT;
                    throw t;
                }finally{
                    
                    
                    try {
                    	File f=this.getOutputFile();
                    	
                        metaData.sha1=Util.sha1(f);
                        metaData.size=f.length();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    metaData.finished=TimeUtil.getCurrentTimeMillis();
                    
                    IOUtil.closeQuietly(this::writeMetaDataFile);
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
        try(BufferedWriter writer = new BufferedWriter(new FileWriter(metaDataFile))){
            EntityFactory.EntityMapper.FULL_ENTITY_MAPPER().writer().writeValue(writer, metaData);
        }
    }
    private OutputStream createOutputFileStream() throws IOException{
        return IOUtil.newBufferedOutputStream(outputFile);
    }

    public File getOutputFile() {
        return outputFile;
    }


    public enum State{
        INITIALIZED,
        RUNNING,
        DONE,
        ERRORED_OUT;
    }
}
