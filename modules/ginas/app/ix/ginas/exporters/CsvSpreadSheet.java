package ix.ginas.exporters;

import org.apache.commons.io.IOUtils;

import java.io.BufferedWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.util.Arrays;
import java.util.Date;

/**
 * Created by katzelda on 8/23/16.
 */
public abstract class CsvSpreadSheet implements Spreadsheet {
    protected final String delimiter;
    protected final boolean quoteCells;
    protected final DateFormat dateFormat;
    protected final BufferedWriter writer;

    private volatile boolean closed=false;

    public CsvSpreadSheet(CsvSpreadsheetBuilder builder) {

        this.dateFormat = builder.getDateFormat();
        this.writer = builder.getWriter();
        this.delimiter = builder.getDelimiter();
        this.quoteCells = builder.shouldQuoteCells();
    }

    @Override
    public Row getRow(int i) {
        ensureNotClosed();

        if(i < 0){
            throw new IndexOutOfBoundsException("can not have negative indexes: " + i);
        }
        return getRowImpl(i);
    }

    protected abstract Row getRowImpl(int absoluteOffset);

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

    protected abstract void writeRemainingRows() throws IOException;

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
