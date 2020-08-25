package ix.ginas.exporters;

import ix.core.exporters.OutputFormat;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * {@link SubstanceExporterFactory} that supports writing spreadsheet data
 * in Excel, tab and comma separated formats.
 *
 * Created by katzelda on 8/23/16.
 */
public class DefaultSubstanceSpreadsheetExporterFactory implements SubstanceExporterFactory {




    private static final Set<OutputFormat> FORMATS;

    static{
        Set<OutputFormat> set = new LinkedHashSet<>();
        set.add(SpreadsheetFormat.TSV);
        set.add(SpreadsheetFormat.CSV);
        set.add(SpreadsheetFormat.XLSX);

        FORMATS = Collections.unmodifiableSet(set);
    }

    @Override
    public Set<OutputFormat> getSupportedFormats() {
        return FORMATS;
    }

    @Override
    public boolean supports(Parameters params) {
        return params.getFormat() instanceof SpreadsheetFormat;
    }

    @Override
    public SubstanceSpreadsheetExporter createNewExporter(OutputStream out, Parameters params) throws IOException {

        SpreadsheetFormat format = (SpreadsheetFormat)params.getFormat();
        Spreadsheet spreadsheet = format.createSpeadsheet(out);

        SubstanceSpreadsheetExporter.Builder builder = new SubstanceSpreadsheetExporter.Builder(spreadsheet);

        configure(builder, params);

        return builder.build();
    }

    protected void configure(SubstanceSpreadsheetExporter.Builder builder, Parameters params){
        builder.includePublicDataOnly(params.publicOnly());
    }

}