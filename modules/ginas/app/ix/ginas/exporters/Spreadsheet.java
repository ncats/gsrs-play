package ix.ginas.exporters;

import java.io.Closeable;

/**
 * Created by katzelda on 8/19/16.
 */
public interface Spreadsheet extends Closeable {

    SpreadsheetCell getCell(int i, int j);

    Row getRow(int i);

    String getExtension();

    interface Row{
        SpreadsheetCell getCell(int j);
    }
}
