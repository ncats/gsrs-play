package ix.ginas.exporters;

import ix.core.exporters.Exporter;
import ix.core.exporters.OutputFormat;
import ix.core.exporters.SubstanceExporterFactory;
import ix.core.exporters.SubstanceExporterFactory.Parameters;
import ix.ginas.models.v1.Substance;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collections;
import java.util.Set;
import java.util.zip.GZIPOutputStream;

/**
 * Created by katzelda on 8/31/16.
 */
public class JsonExporterFactory implements SubstanceExporterFactory{

    OutputFormat format = new OutputFormat("gsrs", "Json Export (gsrs) File");

    @Override
    public boolean supports(Parameters params) {
        return params.getFormat().equals(format);
    }

    @Override
    public Set<OutputFormat> getSupportedFormats() {
        return Collections.singleton(format);
    }

    @Override
    public Exporter<Substance> createNewExporter(OutputStream out, Parameters params) throws IOException {
       // if(params.shouldCompress()) {
            return new JsonExporter(new GZIPOutputStream(out));
//        }
//        return new JsonExporter(out);
    }
}
