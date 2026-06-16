package ix.ginas.exporters;

import ix.core.exporters.OutputFormat;
import ix.ginas.models.v1.Substance;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collections;
import java.util.Set;
import java.util.zip.GZIPOutputStream;

/**
 * Created by epuzanov on 8/30/21.
 */
public class JsonPortableExporterFactory implements SubstanceExporterFactory{

    OutputFormat format = new OutputFormat("gsrsp", "Json Portable Export (gsrsp) File");

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
            return new JsonPortableExporter(new GZIPOutputStream(out));
//        }
//        return new JsonExporter(out);
    }
}
