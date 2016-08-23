package ix.ginas.exporters;

import ix.ginas.models.v1.Substance;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Created by katzelda on 8/23/16.
 */
public class SubstanceCsvExporter implements Exporter<Substance> {

    private final Spreadsheet spreadSheet;

    private int currentRow=1; //first row is header

    private final Map<String, ColumnValueRecipe<Substance>> columns;

    public SubstanceCsvExporter(Spreadsheet spreadSheet,  Map<String, ColumnValueRecipe<Substance>> columns) {
        this.spreadSheet = spreadSheet;
        this.columns = columns;

        Spreadsheet.Row header = spreadSheet.getRow(0);
        int i=0;
        for(String columnName : columns.keySet()){
            header.getCell(i++).writeString(columnName);
        }
    }

    @Override
    public void export(Substance obj) throws IOException {

        Spreadsheet.Row row = spreadSheet.getRow(currentRow++);

        int i=0;
        for(Map.Entry<String, ColumnValueRecipe<Substance>> entry : columns.entrySet()){
            entry.getValue().writeValue(obj, row.getCell(i++));
        }


    }

    @Override
    public String getExtension() {
        //TODO is this right?
        return "csv.txt";
    }

    @Override
    public void close() throws IOException {
        spreadSheet.close();
    }


}
