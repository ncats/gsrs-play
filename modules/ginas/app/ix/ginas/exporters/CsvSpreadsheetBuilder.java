package ix.ginas.exporters;

import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Objects;

/**
 * Created by katzelda on 8/22/16.
 */
public class CsvSpreadsheetBuilder {
    public static final String DEFAULT_FORMAT_STRING = "yyyy-MMM-dd";
    private static final SimpleDateFormat DEFAULT_FORMAT = new SimpleDateFormat(DEFAULT_FORMAT_STRING);

    private DateFormat dateFormat = new SimpleDateFormat(DEFAULT_FORMAT.toPattern());
    private String delimiter = ",";
    private boolean quoteCells;

    private final BufferedWriter writer;

    private int maxRowsInMemory =-1;

    /**
     * Create a new Builder that will build a {@link Spreadsheet} implementation
     * that will write to the given output file.
     * @param outputFile the File to write to.  If any parent directories
     *                   do not exist, they will be created.
     * @throws IOException if there is a problem creating parent directories or opening the file for writing.
     *
     * @throws NullPointerException if out is null.
     */
    public CsvSpreadsheetBuilder(File outputFile) throws IOException {
        Objects.requireNonNull(outputFile);

        File parent = outputFile.getParentFile();

        //TODO use Paths to make dir so we get IOExceptions
        if (parent != null) {
            parent.mkdirs();
        }
        writer = new BufferedWriter(new FileWriter(outputFile));
    }

    /**
     * Create a new Builder that will build a {@link Spreadsheet} implementation
     * that will write to the given outputStream.
     * @param out the OutputStream to write to (can not be null).
     *
     * @throws NullPointerException if out is null.
     */
    public CsvSpreadsheetBuilder(OutputStream out) {
        writer = new BufferedWriter(new OutputStreamWriter(out));
    }

    /**
     * Change the delimiter used to separate columns in the csv file
     * (default is comma).
     * @param delimiter the delimiter character to use.
     * @return this
     */
    public CsvSpreadsheetBuilder delimiter(char delimiter) {
        return delimiter(Character.toString(delimiter));
    }
    /**
     * Change the delimiter used to separate columns in the csv file
     * (default is comma).
     * @param delimiter the delimiter String to use.
     * @return this
     */
    public CsvSpreadsheetBuilder delimiter(String delimiter) {
        Objects.requireNonNull(delimiter);
        this.delimiter = delimiter;

        return this;
    }

    /**
     * Set the max number of rows that can be in memory at any one time.
     * If the number of rows created exceeds this number, then the old rows
     * will be written to the output and can no longer be modified.
     * This essentially makes a low memory footprint streaming writer
     * where users can go back and randomly access/edit the previous X rows.
     *
     * @param maxRowsInMemory the number of rows to keep in memory.  If set to null,
     *                        then everything will be kept in memory (the default).
     * @return this
     */
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

    /**
     * Set the {@link DateFormat} used to write dates in the csv file.
     * By default it is {@value DEFAULT_FORMAT_STRING}.
     *
     * @param dateFormat the date format to use; can not be null.
     * @return this.
     * @throws NullPointerException if dateFormat is null.
     */
    public CsvSpreadsheetBuilder dateFormat(DateFormat dateFormat) {
        Objects.requireNonNull(dateFormat);
        this.dateFormat = dateFormat;

        return this;
    }

    /**
     * Should the cells in the csv get quoted.  If not called,
     * defaults to false.
     * @param quoteCells {@code true} if should be quoted; {@code false} otherwise.
     * @return this.
     */
    public CsvSpreadsheetBuilder quoteCells(boolean quoteCells) {
        this.quoteCells = quoteCells;

        return this;
    }

    /**
     * Use the current configuration to construct a new {@link Spreadsheet}
     * instance.
     * @return a new Spreadsheet instance will never be null.
     */
    public Spreadsheet build() {

        if(maxRowsInMemory ==-1) {
            return new InMemoryCsvSpreadSheet(this);
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
