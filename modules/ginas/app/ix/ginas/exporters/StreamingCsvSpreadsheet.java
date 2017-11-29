package ix.ginas.exporters;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.LinkedList;

/**
 * Created by katzelda on 8/22/16.
 */
public class StreamingCsvSpreadsheet extends CsvSpreadSheet {


    private int rowsWrittenSoFar = 0;

    private int populatedOffset=-1;
    private CsvSpreadSheet.CsvRow[] rows;

    StreamingCsvSpreadsheet(CsvSpreadsheetBuilder builder) {
        super(builder);
        this.rows = new CsvRow[builder.getMaxRowsInMemory()];
    }



    @Override
    protected SpreadsheetRow getRowImpl(int absoluteOffset) {
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
                relativeOffset-= rows.length;

                while (relativeOffset > 0) {
                    relativeOffset--;
                    writeRow(null);
                }
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }

            for(int i=0; i< rows.length ; i++){
                rows[i] = null;
            }

            rowsWrittenSoFar = absoluteOffset;
            relativeOffset =0;
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
