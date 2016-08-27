package ix.ginas.exporters;

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

    private static final OutputFormat CSV = new SpreadsheetFormat("csv", "CSV (csv) File"){

        @Override
        Spreadsheet createSpeadsheet(OutputStream out) {
            return  new CsvSpreadsheetBuilder(out)
                    .quoteCells(true)
                    .maxRowsInMemory(100)
                    .build();
        }
    };

    private static final OutputFormat TSV = new SpreadsheetFormat("txt", "TSV (tab) File"){
        Spreadsheet createSpeadsheet(OutputStream out) {
            return  new CsvSpreadsheetBuilder(out)
                    .delimiter('\t')
                    .quoteCells(false)
                    .maxRowsInMemory(100)
                    .build();
        }
    };

    private static final OutputFormat XLSX = new SpreadsheetFormat("xlsx", "Excel (xlsx) File"){
        Spreadsheet createSpeadsheet(OutputStream out) {

                return new ExcelSpreadsheet.Builder(out)
                        .maxRowsInMemory(100)
                        .build();

        }
    };


    private static final Set<OutputFormat> FORMATS;

    static{
        Set<OutputFormat> set = new LinkedHashSet<>();
        set.add(TSV);
        set.add(CSV);
        set.add(XLSX);

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
        //no-op

    }

    private static abstract class SpreadsheetFormat extends OutputFormat{

        public SpreadsheetFormat(String extension, String displayname) {
            super(extension, displayname);
        }

        abstract Spreadsheet createSpeadsheet(OutputStream out);
    }
}
