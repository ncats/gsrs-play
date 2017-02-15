package ix.test.server;

import ix.ginas.exporters.Exporter;
import ix.ginas.exporters.JsonExporterFactory;
import ix.ginas.exporters.SubstanceExporterFactory;
import ix.ginas.models.v1.Substance;

import java.io.*;
import java.util.stream.Stream;

/**
 * Writer that wraps the {@link Exporter} that can make
 * the same kind of json files that can be loaded by the
 * bulk G-SRS loader API.
 *
 *
 * Created by katzelda on 2/14/17.
 */
public class JsonSubstanceWriter implements Closeable{

    private final Exporter<Substance> exporter;

    private static final SubstanceExporterFactory.Parameters IGNORED = new SubstanceExporterFactory.Parameters() {
        @Override
        public SubstanceExporterFactory.OutputFormat getFormat() {
            return null;
        }
    };


    public JsonSubstanceWriter(File outputFile) throws IOException{
        this(new BufferedOutputStream(new FileOutputStream(outputFile)));
    }
    public JsonSubstanceWriter (OutputStream out) throws IOException{
        JsonExporterFactory factory = new JsonExporterFactory();
        exporter = factory.createNewExporter(out,IGNORED);
    }

    public void writeAll(Stream<Substance> substances) {
        substances.forEach(s -> {
            try {
                write(s);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        });
    }
    public void write(Substance substance) throws IOException{
        exporter.export(substance);
    }

    @Override
    public void close() throws IOException {
        exporter.close();
    }
}
