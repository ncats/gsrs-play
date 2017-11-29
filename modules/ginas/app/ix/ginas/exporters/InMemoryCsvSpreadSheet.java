package ix.ginas.exporters;

import java.io.IOException;
import java.util.Arrays;

/**
 * Created by katzelda on 8/19/16.
 */
public class InMemoryCsvSpreadSheet extends CsvSpreadSheet {

    protected CsvRow[] rows = new CsvRow[0];
    InMemoryCsvSpreadSheet(CsvSpreadsheetBuilder builder){
        super(builder);
    }

    protected SpreadsheetRow getRowImpl(int absoluteOffset) {
        ensureCapacity(absoluteOffset);
        CsvRow r= rows[absoluteOffset];
        if(r ==null){
            r = createNewRow();
            rows[absoluteOffset] =r;
        }

        return r;
    }

    private void ensureCapacity(int offset){
        int requiredLength = offset+1;
        if(rows.length < requiredLength) {
            rows= Arrays.copyOf(rows, requiredLength);
        }
    }

    protected void writeRemainingRows() throws IOException {
        for (CsvRow r : rows) {
            writeRow(r);
        }
    }
}
