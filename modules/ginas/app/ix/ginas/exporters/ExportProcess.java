package ix.ginas.exporters;

import com.fasterxml.jackson.databind.ObjectWriter;
import ix.core.controllers.EntityFactory;
import ix.core.util.*;
import ix.ginas.controllers.plugins.GinasSubstanceExporterFactoryPlugin;
import ix.ginas.models.v1.Substance;
import play.Play;

import java.io.*;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * Created by katzelda on 4/18/17.
 */
public class ExportProcess {
    private static CachedSupplier<GinasSubstanceExporterFactoryPlugin> factoryPlugin = CachedSupplier
            .of(() -> Play.application().plugin(GinasSubstanceExporterFactoryPlugin.class));
    private final File outputFile, metaDataFile;

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

    public synchronized ExportProcess run(Function<OutputStream, Exporter<Substance>> exporterFunction) throws IOException{
        System.out.println("run state = " + currentState);
        if(currentState != State.INITIALIZED){
            return this;
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
//            final OutputStream fout = out;
            factoryPlugin.get().submit( ()->{
                try{
                    substanceSupplier.get().forEach(s -> Unchecked.ioException( () -> exporter.export(s)));
                    currentState = State.DONE;
                }catch(Throwable t){
//                    IOUtil.closeQuietly(fout);
                    currentState = State.ERRORED_OUT;

                    throw t;
                 }finally{
                    IOUtil.closeQuietly(this::writeMetaDataFile);
                    IOUtil.closeQuietly(exporter);
                }
            });
            return this;
        }catch(Throwable t){
            IOUtil.closeQuietly(out);
            currentState = State.ERRORED_OUT;

            throw t;
        }
    }

    private void writeMetaDataFile() throws IOException{
        try(BufferedWriter writer = new BufferedWriter(new FileWriter(metaDataFile))){

            metaData.finished = TimeUtil.getCurrentTimeMillis();
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
