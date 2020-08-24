package ix.ginas.exporters;

import ix.core.exporters.OutputFormat;
import ix.ginas.models.v1.Substance;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collections;
import java.util.Set;

/**
 * Created by katzelda on 8/23/16.
 */
public class SPLValidatorXMLExporterFactory implements SubstanceExporterFactory {

    private static final Set<OutputFormat> formats = Collections.singleton( new OutputFormat("spl.xml", "SPL term validation (xml) File"));
   
    @Override
    public boolean supports(Parameters params) {
        return "spl.xml".equals(params.getFormat().getExtension());
    }

    @Override
    public Set<OutputFormat> getSupportedFormats() {
        return formats;
    }

    @Override
    public Exporter<Substance> createNewExporter(OutputStream out, Parameters params) throws IOException {
        return new SPLValidatorXMLExporter(out);
    }


}
