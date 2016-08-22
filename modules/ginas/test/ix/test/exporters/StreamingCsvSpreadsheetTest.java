package ix.test.exporters;

import ix.ginas.exporters.CsvSpreadsheetBuilder;
import ix.ginas.exporters.Spreadsheet;

/**
 * Created by katzelda on 8/22/16.
 */
public class StreamingCsvSpreadsheetTest extends InMemorySpreadSheetExporterTest {

    @Override
    protected Spreadsheet createSpreadsheet(CsvSpreadsheetBuilder builder) {
        builder.maxRowsInMemory(10);
        return builder.build();
    }
}
