package ix.ginas.exporters;

import java.io.Closeable;

/**
 * interface that represents a Spreadsheet.
 *
 * Created by katzelda on 8/19/16.
 */
public interface Spreadsheet extends Closeable {
    /**
     * Get the ith row in this Spreadsheet.
     * @param i the row (0-based offset).
     * @return a SpreadsheetRow.  If this row does not yet exist, it will be created.
     *          WARNING: THIS METHOD MAY RETURN NULL in some implementations that can't
     *          keep the entire spreadsheet in memory.
     */
    SpreadsheetRow getRow(int i);

    /**
     * interface that represents a SpreadsheetRow in a Spreadsheet.
     */
    interface SpreadsheetRow {
        /**
         * Get the jth cell in this row.
         * @param j the cell to get (0-based offset).
         * @return the {@link SpreadsheetCell}; will never be null, if the cell did not exist yet, then
         *          it will be created and the new object is returned.
         */
        SpreadsheetCell getCell(int j);
    }
}
