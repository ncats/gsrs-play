package ix.ginas.exporters;

import org.apache.commons.io.IOUtils;

import java.io.*;
import java.text.DateFormat;
import java.util.Arrays;
import java.util.Date;

/**
 * Created by katzelda on 8/19/16.
 */
public class CsvSpreadSheet implements Spreadsheet {


    private final String delimiter;
    private final boolean quoteCells;

    private final DateFormat dateFormat;

    protected CsvRow[] rows;

    private final BufferedWriter writer;

    private volatile boolean closed=false;


    CsvSpreadSheet(CsvSpreadsheetBuilder builder){
        this.writer = builder.getWriter();
        this.delimiter = builder.getDelimiter();
        this.quoteCells = builder.shouldQuoteCells();

        this.dateFormat = builder.getDateFormat();

        this.rows = initializeRowsArray(builder);

    }

    protected CsvRow[] initializeRowsArray(CsvSpreadsheetBuilder builder){
        return new CsvRow[0];
    }
    @Override
    public SpreadsheetCell getCell(int i, int j) {
        return getRow(i).getCell(j);
    }

    @Override
    public Row getRow(int i) {
        ensureNotClosed();

        if(i < 0){
            throw new IndexOutOfBoundsException("can not have negative indexes: " + i);
        }
        return getRowImpl(i);
    }

    protected Row getRowImpl(int absoluteOffset) {
        ensureCapacity(absoluteOffset);
        CsvRow r= rows[absoluteOffset];
        if(r ==null){
            r = createNewRow();
            rows[absoluteOffset] =r;
        }

        return r;
    }

    protected CsvRow createNewRow(){
        return new CsvRow(dateFormat);
    }

    @Override
    public void close() throws IOException {
        if(!closed){
            closed = true;
            try {
                writeRemainingRows();
            }finally{
                writer.flush();
                IOUtils.closeQuietly(writer);
            }
        }

    }

    protected void writeRemainingRows() throws IOException {
        for (CsvRow r : rows) {
            writeRow(r);
        }
    }

    protected void writeRow(CsvRow r) throws IOException {
        if (r != null) {
            String[] rowStrings = new String[r.values.length];

            for(int j=0; j< r.values.length; j++){
                CsvCell cell = r.values[j];
                String value = cell ==null? "": cell.value;
                if(quoteCells){
                    value = "\""+value + "\"";
                }
                rowStrings[j] = value;
            }
            writer.write(String.join(delimiter, rowStrings));
        }
        // if r is null treat it as a blank
        writer.newLine();
    }

    private void ensureNotClosed(){
        if(closed){
            throw new IllegalStateException("already closed");
        }
    }
    private void ensureCapacity(int offset){
        int requiredLength = offset+1;
        if(rows.length < requiredLength) {
            rows= Arrays.copyOf(rows, requiredLength);
        }
    }


    protected static final class CsvRow implements Row{
        CsvCell[] values = new CsvCell[0];
        private final DateFormat dateFormat;

        public CsvRow(DateFormat dateFormat) {
            this.dateFormat = dateFormat;
        }

        @Override
        public SpreadsheetCell getCell(int j) {
            if(j < 0){
                throw new IndexOutOfBoundsException("can not have negative indexes: " + j);
            }
            ensureCapacity(j);

            CsvCell c = values[j];
            if(c ==null){
                c = new CsvCell(dateFormat);
                values[j] = c;

            }
            return c;
        }

        private void ensureCapacity(int offset){
            int requiredLength = offset+1;
            if(values.length < requiredLength) {
                 values= Arrays.copyOf(values, requiredLength);
            }
        }

        @Override
        public String toString() {
            return "CsvRow{" +
                    "values=" + Arrays.toString(values) +
                    ", dateFormat=" + dateFormat +
                    '}';
        }
    }

    private static final class CsvCell implements SpreadsheetCell{

        private final DateFormat dateFormat;

        private String value = "";

        public CsvCell(DateFormat dateFormat) {
            this.dateFormat = dateFormat;
        }

        @Override
        public void writeInteger(int i) {
            value = Integer.toString(i);
        }

        @Override
        public void writeDate(Date date) {
            value = dateFormat.format(date);
        }

        @Override
        public void writeString(String s) {
            value = s;
        }

        @Override
        public String toString() {
            return "CsvCell{" +
                    "dateFormat=" + dateFormat +
                    ", value='" + value + '\'' +
                    '}';
        }
    }
}
