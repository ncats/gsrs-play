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

/**
 * Created by peryeata on 8/29/17.
 */
public class SRSLegacyDictionaryExporterFactory implements SubstanceExporterFactory {

    private static final Set<OutputFormat> formats = Collections.singleton( new OutputFormat("dic", "Legacy SRS Dictionary File"));
   
    @Override
    public boolean supports(Parameters params) {
        return "dic".equals(params.getFormat().getExtension());
    }

    @Override
    public Set<OutputFormat> getSupportedFormats() {
        return formats;
    }

    @Override
    public Exporter<Substance> createNewExporter(OutputStream out, Parameters params) throws IOException {
        return new SRSLegacyDictionaryExporter(out);
    }
}
