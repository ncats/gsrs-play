package ix.ginas.exporters;

import java.util.Objects;

class BasicColumnValueRecipe<T> implements SingleColumnValueRecipe<T> {
    private final String columnName;
    private final WriterFunction<T> function;

    @Override
    public void writeValue(T object, SpreadsheetCell cell) {
        function.writeValue(object, cell);
    }

    public BasicColumnValueRecipe(String columnName, WriterFunction<T> function) {
        Objects.requireNonNull(columnName);
        Objects.requireNonNull(function);
        this.columnName = columnName;
        this.function = function;
    }

    public int writeValuesFor(Spreadsheet.SpreadsheetRow row, int currentOffset, T obj) {
        function.writeValue(obj, row.getCell(currentOffset));
        return 1;
    }

    @Override
    public int writeHeaderValues(Spreadsheet.SpreadsheetRow row, int currentOffset) {
        row.getCell(currentOffset).writeString(columnName);
        return 1;
    }

    @Override
    public boolean containsColumnName(String name) {
        return Objects.equals(columnName, name);
    }

    @Override
    public BasicColumnValueRecipe<T> replaceColumnName(String oldName, String newName) {
        Objects.requireNonNull(oldName);
        Objects.requireNonNull(newName);

        if(containsColumnName(oldName)){
            return new BasicColumnValueRecipe<>(newName, function);
        }
        return this;
    }


}
