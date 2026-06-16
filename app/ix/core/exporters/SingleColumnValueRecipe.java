package ix.core.exporters;

import java.util.function.BiFunction;

/**
 * Created by katzelda on 8/19/16.
 */
public interface SingleColumnValueRecipe<T> extends ColumnValueRecipe<T> {

    void writeValue(T object , SpreadsheetCell cell);



    @Override
    default int writeValuesFor(Spreadsheet.SpreadsheetRow row, int currentOffset, T obj){
        try{
            writeValue(obj, row.getCell(currentOffset));
        }catch(Exception e){
            e.printStackTrace();
        }
        return 1;
    }

    interface WriterFunction<T>{
        void writeValue(T object , SpreadsheetCell cell);

    }
    static <T>  ColumnValueRecipe<T> create(Enum<?> enumValue, WriterFunction<T> function){
        return create(enumValue.name(), function);
    }
    static <T>  ColumnValueRecipe<T> create(String columnName, WriterFunction<T> function){
        return new BasicColumnValueRecipe<T>(columnName, function);
    }

}
