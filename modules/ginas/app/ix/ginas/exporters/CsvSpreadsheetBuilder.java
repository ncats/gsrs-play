package ix.ginas.exporters;

import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Objects;

/**
 * Created by katzelda on 8/22/16.
 */
public class CsvSpreadsheetBuilder {
    private static final SimpleDateFormat DEFAULT_FORMAT = new SimpleDateFormat("yyyy-MMM-dd");

    private DateFormat dateFormat = new SimpleDateFormat(DEFAULT_FORMAT.toPattern());
    private String delimiter = ",";
    private boolean quoteCells;

    private final BufferedWriter writer;

    private int maxRowsInMemory =-1;

    public CsvSpreadsheetBuilder(File outputFile) throws IOException {
        Objects.requireNonNull(outputFile);

        File parent = outputFile.getParentFile();

        //TODO use Paths to make dir so we get IOExceptions
        if (parent != null) {
            parent.mkdirs();
        }
        writer = new BufferedWriter(new FileWriter(outputFile));
    }

    public CsvSpreadsheetBuilder(OutputStream out) {
        writer = new BufferedWriter(new OutputStreamWriter(out));
    }

    public CsvSpreadsheetBuilder delimiter(char delimiter) {
        return delimiter(Character.toString(delimiter));
    }
    public CsvSpreadsheetBuilder delimiter(String delimiter) {
        Objects.requireNonNull(delimiter);
        this.delimiter = delimiter;

        return this;
    }

    public CsvSpreadsheetBuilder maxRowsInMemory(Integer maxRowsInMemory){
        if(maxRowsInMemory ==null){
            this.maxRowsInMemory = -1;
            return this;
        }
        if(maxRowsInMemory.intValue() < 1){
            throw new IllegalArgumentException("max rows in Memory must be > 1");
        }
        this.maxRowsInMemory = maxRowsInMemory.intValue();
        return this;
    }

    public CsvSpreadsheetBuilder dateFormat(DateFormat dateFormat) {
        Objects.requireNonNull(dateFormat);
        this.dateFormat = dateFormat;

        return this;
    }

    public CsvSpreadsheetBuilder quoteCells(boolean quoteCells) {
        this.quoteCells = quoteCells;

        return this;
    }

    public Spreadsheet build() {

        if(maxRowsInMemory ==-1) {
            return new CsvSpreadSheet(this);
        }
        return new StreamingCsvSpreadsheet(this);
    }


    DateFormat getDateFormat() {
        return dateFormat;
    }

    String getDelimiter() {
        return delimiter;
    }

    boolean shouldQuoteCells() {
        return quoteCells;
    }

    BufferedWriter getWriter() {
        return writer;
    }

    int getMaxRowsInMemory() {
        return maxRowsInMemory;
    }
}
