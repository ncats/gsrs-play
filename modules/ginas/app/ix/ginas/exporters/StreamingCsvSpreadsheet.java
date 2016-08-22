package ix.ginas.exporters;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Arrays;

/**
 * Created by katzelda on 8/22/16.
 */
public class StreamingCsvSpreadsheet extends CsvSpreadSheet {


    private int rowsWrittenSoFar = 0;

    private int populatedOffset=-1;
    StreamingCsvSpreadsheet(CsvSpreadsheetBuilder builder) {
        super(builder);
    }

    @Override
    protected CsvSpreadSheet.CsvRow[] initializeRowsArray(CsvSpreadsheetBuilder builder) {
        int size = builder.getMaxRowsInMemory();
        return new CsvRow[size];
    }

    @Override
    protected Row getRowImpl(int absoluteOffset) {
        if(absoluteOffset < rowsWrittenSoFar ){
            return null;
        }
        int relativeOffset = absoluteOffset - rowsWrittenSoFar;
        if(relativeOffset >= rows.length){
            //write everything I guess...
            try {
                for(CsvRow r : rows) {
                    writeRow(r);
                }
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
            rowsWrittenSoFar += rows.length;
            relativeOffset -= rows.length;
            for(int i=0; i< rows.length ; i++){
                rows[i] = null;
            }
            populatedOffset=-1;
        }
        if(rows[relativeOffset] !=null){
            return rows[relativeOffset];
        }
        CsvRow newRow = createNewRow();
        rows[relativeOffset] = newRow;
        populatedOffset = Math.max(populatedOffset, relativeOffset);

        return newRow;
    }

    @Override
    protected void writeRemainingRows() throws IOException {
        for(int i=0; i<= populatedOffset; i++){
            writeRow(rows[i]);
        }
    }
}
