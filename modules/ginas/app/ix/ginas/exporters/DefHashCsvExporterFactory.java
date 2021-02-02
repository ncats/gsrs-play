package ix.ginas.exporters;

import com.google.common.io.FileBackedOutputStream;
import ix.core.exporters.OutputFormat;
import ix.ginas.models.v1.Substance;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collections;
import java.util.Set;
import java.util.zip.GZIPOutputStream;
import play.Logger;

/*
Created 29 January 2021 as a test
Mitch Miller
 */
public class DefHashCsvExporterFactory implements SubstanceExporterFactory {
    OutputFormat format = new OutputFormat("defhash", "Definitional hash (defhash) File");

    @Override
    public boolean supports(Parameters params) {
        boolean sup = params.getFormat().equals(format);
        Logger.trace("DefHashCsvExporterFactory.supports to return " + sup);
        return sup;
    }

    @Override
    public Set<OutputFormat> getSupportedFormats() {
        return Collections.singleton(format);
    }

    @Override
    public Exporter<Substance> createNewExporter(OutputStream out, Parameters params) throws IOException {
        Logger.trace("createNewExporter ");
        return new DefHashCsvExporter(out);
    }

}
