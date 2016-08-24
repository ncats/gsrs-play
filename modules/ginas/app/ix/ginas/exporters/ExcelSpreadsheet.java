package ix.ginas.exporters;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;
import java.util.Objects;

/**
 * Created by katzelda on 8/24/16.
 */
public class ExcelSpreadsheet implements Spreadsheet {

    private final Workbook workbook;

    private final Sheet sheet;


    private final OutputStream out;

    private ExcelSpreadsheet(Workbook workbook, OutputStream out) {
        this.workbook = workbook;
        this.out = out;
        //just first sheet
        sheet = workbook.createSheet();
    }

    @Override
    public SpreadsheetCell getCell(int i, int j) {
        return null;
    }

    @Override
    public Row getRow(int i) {
        org.apache.poi.ss.usermodel.Row r =sheet.getRow(i);
        if(r ==null){
            r = sheet.createRow(i);
        }
        return new RowWrapper(r);
    }

    @Override
    public String getExtension() {
        return "xlsx";
    }

    @Override
    public void close() throws IOException {
        try{
            workbook.write(out);
        }finally{
            out.close();
        }

    }

    private static class RowWrapper implements Row{
        private final org.apache.poi.ss.usermodel.Row row;

        public RowWrapper(org.apache.poi.ss.usermodel.Row r) {
            this.row = r;
        }


        @Override
        public SpreadsheetCell getCell(int j) {
            Cell cell = row.getCell(j);
            if(cell ==null){
                cell = row.createCell(j);
            }
            return new CellWrapper(cell);
        }
    }

    private static class CellWrapper implements SpreadsheetCell{
        private final Cell cell;
        public CellWrapper(Cell cell) {
            this.cell =cell;
        }

        @Override
        public void writeInteger(int i) {
            cell.setCellValue(i);
        }

        @Override
        public void writeDate(Date date) {
            cell.setCellValue(date);
        }

        @Override
        public void writeString(String s) {
            cell.setCellValue(s);
        }
    }


    public static class Builder{

        private OutputStream out;

        private int maxInMemory =-1;



        public Builder(File outputFile) throws IOException{
            File parentDir = outputFile.getParentFile();
            if(parentDir !=null){
                parentDir.mkdirs();
            }
            out = new FileOutputStream(outputFile);
        }

        public Builder(OutputStream out) throws IOException{
            Objects.requireNonNull(out);

            this.out = out;
        }


        public Builder maxRowsInMemory(Integer maxRowsInMemory){
            if(maxRowsInMemory ==null){
                maxInMemory = -1;
            }else{
                maxInMemory = maxRowsInMemory.intValue();
            }
            return this;
        }

        public Spreadsheet build(){

            Workbook workbook ;
            if(maxInMemory ==-1){
                workbook = new XSSFWorkbook();
            }else{
                workbook = new SXSSFWorkbook(maxInMemory);
            }
            return new ExcelSpreadsheet(workbook, out);
        }
    }
}
