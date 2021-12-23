package ix.ginas.exporters;

import ix.core.exporters.OutputFormat;
import ix.core.util.ConfigHelper;

import java.io.IOException;
import java.io.OutputStream;
import java.util.*;

/**
 * Created by epuzanov.
 */

public class JmespathSpreadsheetExporterFactory implements SubstanceExporterFactory{

    OutputFormat format = new OutputFormat("custom.xlsx", "Custom Report (xlsx) File");

    private final static List<Map<String, String>> columnExpressions = ConfigHelper.getOrDefault("ix.ginas.exporters.JmespathSpreadsheetExporterFactory.columns", (List<Map<String, String>>) Arrays.asList((Map<String, String>) new HashMap<String, String>(){{put("name", "UUID"); put("expression", "uuid");}}));

    @Override
    public boolean supports(Parameters params) {
        return params.getFormat().equals(format);
    }

    @Override
    public Set<OutputFormat> getSupportedFormats() {
        return Collections.singleton(format);
    }

    @Override
    public JmespathSpreadsheetExporter createNewExporter(OutputStream out, Parameters params) throws IOException {
        String ext = format.getExtension();
        Spreadsheet spreadsheet;
        if (ext.endsWith(".csv")) {
            spreadsheet = new CsvSpreadsheetBuilder(out)
                .quoteCells(true)
                .maxRowsInMemory(100)
                .build();
        } else if (ext.endsWith(".txt")) {
            spreadsheet = new CsvSpreadsheetBuilder(out)
                .delimiter('\t')
                .quoteCells(false)
                .maxRowsInMemory(100)
                .build();
        } else {
            spreadsheet = new ExcelSpreadsheet.Builder(out)
                .maxRowsInMemory(100)
                .build();
        }
        JmespathSpreadsheetExporter.Builder builder = new JmespathSpreadsheetExporter.Builder(spreadsheet);
        for (Map<String, String> columnExpression : columnExpressions) {
            String columnName = columnExpression.get("name");
            ColumnValueRecipe recipe = JmespathColumnValueRecipe.create(columnName, columnExpression.get("expression"), columnExpression.getOrDefault("delimiter", "|"));
            builder = builder.addColumn(columnName, recipe);
        }
        return builder.includePublicDataOnly(params.publicOnly()).build();
    }
}
