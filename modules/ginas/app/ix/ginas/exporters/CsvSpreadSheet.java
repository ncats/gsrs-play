package ix.ginas.exporters;

import ix.core.util.IOUtil;
import org.apache.commons.io.IOUtils;

import javax.swing.text.DateFormatter;
import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * Created by katzelda on 8/19/16.
 */
public class CsvSpreadSheet implements Spreadsheet {


    private final String delimiter;
    private final boolean quoteCells;

    private final DateFormat dateFormat;

    private CsvRow[] rows = new CsvRow[0];

    private final BufferedWriter writer;

    private volatile boolean closed=false;


    private CsvSpreadSheet(Builder builder){
        this.writer = builder.writer;
        this.delimiter = builder.delimiter;
        this.quoteCells = builder.quoteCells;

        this.dateFormat = builder.dateFormat;

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
        ensureCapacity(i);
        CsvRow r= rows[i];
        if(r ==null){
            r = new CsvRow(dateFormat);
            rows[i] =r;
        }

        return r;
    }

    @Override
    public void close() throws IOException {
        if(!closed){
            closed = true;
            try {
                for (CsvRow r : rows) {
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
            }finally{
                IOUtils.closeQuietly(writer);
            }
        }

    }

    private void ensureNotClosed(){
        if(closed){
            throw new IllegalStateException("already closed");
        }
    }
    private void ensureCapacity(int length){
        if(rows.length < length) {
            rows= Arrays.copyOf(rows, length);
        }
    }


    public static class Builder{
        private static final SimpleDateFormat DEFAULT_FORMAT = new SimpleDateFormat("yyyy-MMM-dd");

        private DateFormat dateFormat=  new SimpleDateFormat(DEFAULT_FORMAT.toPattern());
        private String delimiter = ",";
        private boolean quoteCells;

        private final BufferedWriter writer;

        public Builder(File outputFile) throws IOException{
            Objects.requireNonNull(outputFile);

            File parent = outputFile.getParentFile();

            //TODO use Paths to make dir so we get IOExceptions
            if(parent !=null){
                parent.mkdirs();
            }
            writer = new BufferedWriter(new FileWriter(outputFile));
        }

        public Builder(OutputStream out){
            writer = new BufferedWriter( new OutputStreamWriter(out));
        }

        public Builder delimiter(String delimiter){
            Objects.requireNonNull(delimiter);
            this.delimiter = delimiter;

            return this;
        }

        public Builder dateFormat(DateFormat dateFormat){
            Objects.requireNonNull(dateFormat);
            this.dateFormat = dateFormat;

            return this;
        }

        public Builder quoteCells(boolean quoteCells){
            this.quoteCells = quoteCells;

            return this;
        }

        public Spreadsheet build(){
            return new CsvSpreadSheet(this);
        }
    }

    private static final class CsvRow implements Row{
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

        private void ensureCapacity(int length){
            if(values.length < length) {
                 values= Arrays.copyOf(values, length);
            }
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
    }
}
