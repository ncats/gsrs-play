package ix.ginas.exporters;

/**
 * Created by katzelda on 8/19/16.
 */
public interface ColumnValueRecipe<T> {
    int writeValuesFor(Spreadsheet.SpreadsheetRow row, int currentOffset, T obj);
    int writeHeaderValues(Spreadsheet.SpreadsheetRow row, int currentOffset);

    boolean containsColumnName(String name);

    ColumnValueRecipe<T> replaceColumnName(String oldName, String newName);
}
