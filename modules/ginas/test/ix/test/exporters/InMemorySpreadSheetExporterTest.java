package ix.test.exporters;

import ix.ginas.exporters.CsvSpreadSheet;
import ix.ginas.exporters.CsvSpreadsheetBuilder;
import ix.ginas.exporters.Spreadsheet;
import org.apache.commons.io.IOUtils;
import org.h2.tools.Csv;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import static org.junit.Assert.*;
/**
 * Created by katzelda on 8/22/16.
 */
public class InMemorySpreadSheetExporterTest {
    @Rule
    public TemporaryFolder tmpDir = new TemporaryFolder();


    private static final String NEW_LINE = System.lineSeparator();

    private File outputFile;
    @Before
    public void createOutputFile() throws IOException{
        outputFile = tmpDir.newFile();
    }

    @Test
    public void blankFile() throws IOException{


        Spreadsheet sheet = createSpreadsheet(new CsvSpreadsheetBuilder(outputFile));

        sheet.close();

        String actual = getOutputAsString();

        assertEquals("", actual);

    }

    protected String getOutputAsString() throws IOException {
        return IOUtils.toString(new FileReader(outputFile));
    }

    @Test
    public void singleLine() throws IOException{

        Spreadsheet sheet = createSpreadsheet(new CsvSpreadsheetBuilder(outputFile));


        Spreadsheet.Row row = sheet.getRow(0);

        row.getCell(0).writeString("foo");
        row.getCell(1).writeString("bar");

        sheet.close();

        String actual = getOutputAsString();

        assertEquals("foo,bar"+NEW_LINE, actual);
    }

    @Test
    public void overwriteCell() throws IOException{

        Spreadsheet sheet = createSpreadsheet(new CsvSpreadsheetBuilder(outputFile));


        Spreadsheet.Row row = sheet.getRow(0);

        row.getCell(0).writeString("foo");
        row.getCell(1).writeString("bar");

        row.getCell(0).writeString("xxx");

        sheet.close();

        String actual = getOutputAsString();

        assertEquals("xxx,bar"+NEW_LINE, actual);
    }

    @Test
    public void skipRowCellShouldBeBlank() throws IOException{

        Spreadsheet sheet = createSpreadsheet(new CsvSpreadsheetBuilder(outputFile));


        Spreadsheet.Row row = sheet.getRow(0);

        row.getCell(0).writeString("foo");
        row.getCell(2).writeString("bar");

        sheet.close();

        String actual = getOutputAsString();

        assertEquals("foo,,bar"+NEW_LINE, actual);
    }

    @Test
    public void changeDelimiter() throws IOException{

        Spreadsheet sheet = new CsvSpreadsheetBuilder(outputFile)
                .delimiter('\t')
                .build();


        Spreadsheet.Row row = sheet.getRow(0);

        row.getCell(0).writeString("foo");
        row.getCell(1).writeString("bar");

        sheet.close();

        String actual = getOutputAsString();

        assertEquals("foo\tbar"+NEW_LINE, actual);
    }

    @Test
    public void quoted() throws IOException{

        Spreadsheet sheet = createSpreadsheet(new CsvSpreadsheetBuilder(outputFile)
                                            .quoteCells(true)
                                            );


        Spreadsheet.Row row = sheet.getRow(0);

        row.getCell(0).writeString("foo");
        row.getCell(1).writeString("bar");

        sheet.close();

        String actual = getOutputAsString();

        assertEquals("\"foo\",\"bar\""+NEW_LINE, actual);
    }

    @Test
    public void multiLine() throws IOException{

        Spreadsheet sheet = createSpreadsheet(new CsvSpreadsheetBuilder(outputFile));


        Spreadsheet.Row row = sheet.getRow(0);

        row.getCell(0).writeString("foo");
        row.getCell(1).writeString("bar");

        Spreadsheet.Row row2 = sheet.getRow(1);

        row2.getCell(0).writeString("doe");
        row2.getCell(1).writeString("ray");
        row2.getCell(2).writeString("me");

        sheet.close();

        String actual = getOutputAsString();

        assertEquals("foo,bar"+NEW_LINE+"doe,ray,me"+NEW_LINE, actual);
    }

    @Test
    public void goBackReEditRow() throws IOException{

        Spreadsheet sheet = createSpreadsheet(new CsvSpreadsheetBuilder(outputFile));


        Spreadsheet.Row row = sheet.getRow(0);

        row.getCell(0).writeString("foo");
        row.getCell(1).writeString("bar");

        Spreadsheet.Row row2 = sheet.getRow(1);

        row2.getCell(0).writeString("doe");
        row2.getCell(1).writeString("ray");
        row2.getCell(2).writeString("me");

        Spreadsheet.Row row1Again = sheet.getRow(0);
        row1Again.getCell(1).writeString("XXX");


        sheet.close();

        String actual = getOutputAsString();

        assertEquals("foo,XXX"+NEW_LINE+"doe,ray,me"+NEW_LINE, actual);
    }

    @Test
    public void skipLineShouldBeBlank() throws IOException{

        Spreadsheet sheet = createSpreadsheet(new CsvSpreadsheetBuilder(outputFile));


        Spreadsheet.Row row = sheet.getRow(0);

        row.getCell(0).writeString("foo");
        row.getCell(1).writeString("bar");

        Spreadsheet.Row row2 = sheet.getRow(2);

        row2.getCell(0).writeString("doe");
        row2.getCell(1).writeString("ray");
        row2.getCell(2).writeString("me");

        sheet.close();

        String actual = getOutputAsString();

        assertEquals("foo,bar"+NEW_LINE+NEW_LINE+"doe,ray,me"+NEW_LINE, actual);
    }


    @Test
    public void writeLargeFile() throws IOException {

        Spreadsheet sheet = createSpreadsheet(new CsvSpreadsheetBuilder(outputFile));

        StringBuilder expected = new StringBuilder(5000);
        for(int i=0; i< 1000; i++){
            Spreadsheet.Row row = sheet.getRow(i);

            row.getCell(0).writeString("foo"+i);
            row.getCell(1).writeString("bar"+i);

            expected.append("foo"+i+",bar"+i+NEW_LINE);
        }

        sheet.close();

        String actual = getOutputAsString();
        assertEquals(expected.toString(), actual);
    }

    protected Spreadsheet createSpreadsheet(CsvSpreadsheetBuilder builder){
        return builder.build();
    }
}
