package ix.ginas.exporters;

import ix.core.exporters.OutputFormat;

import java.io.OutputStream;
import java.util.Objects;
import java.util.function.Function;

public abstract class SpreadsheetFormat extends OutputFormat {

    public SpreadsheetFormat(String extension, String displayname) {
        super(extension, displayname);
    }

    abstract Spreadsheet createSpeadsheet(OutputStream out);

    public SpreadsheetFormat withInfo(Function<StringBuilder, String> extension, Function<StringBuilder, String> displayName){
        Objects.requireNonNull(extension);
        Objects.requireNonNull(displayName);

        return newSubclass(this, extension.apply(new StringBuilder(this.getExtension())), displayName.apply(new StringBuilder(this.getDisplayName())));
    }

    private  SpreadsheetFormat newSubclass(SpreadsheetFormat parentClass, String ext, String display){
        return new SpreadsheetFormat(ext, display) {
            @Override
            Spreadsheet createSpeadsheet(OutputStream out) {
                return parentClass.createSpeadsheet(out);
            }
        };
    }

    public static final SpreadsheetFormat CSV = new SpreadsheetFormat("csv", "CSV (csv) File"){

        @Override
        Spreadsheet createSpeadsheet(OutputStream out) {
            return  new CsvSpreadsheetBuilder(out)
                    .quoteCells(true)
                    .maxRowsInMemory(100)
                    .build();
        }


    };

    public static final SpreadsheetFormat TSV = new SpreadsheetFormat("txt", "TSV (tab) File"){
        Spreadsheet createSpeadsheet(OutputStream out) {
            return  new CsvSpreadsheetBuilder(out)
                    .delimiter('\t')
                    .quoteCells(false)
                    .maxRowsInMemory(100)
                    .build();
        }
    };

    public static final SpreadsheetFormat XLSX = new SpreadsheetFormat("xlsx", "Excel (xlsx) File"){
        Spreadsheet createSpeadsheet(OutputStream out) {

            return new ExcelSpreadsheet.Builder(out)
                    .maxRowsInMemory(100)
                    .build();

        }
    };
}
