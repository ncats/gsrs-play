package ix.ginas.exporters;

import java.io.IOException;
import java.util.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.node.ArrayNode;

import ix.core.controllers.EntityFactory;
import ix.ginas.models.v1.Substance;


/**
 * Substance Exporter that writes out data to a Spreadsheet.
 * Created by epuzanov
 */
public class JmespathSpreadsheetExporter implements Exporter<Substance> {

    private static boolean publicOnly = false;
    private final Spreadsheet spreadsheet;
    private int row=1;
    private final List<ColumnValueRecipe<JsonNode>> recipeMap;
    private final ObjectWriter writer = EntityFactory.EntityMapper.FULL_ENTITY_MAPPER().writer();

    private JmespathSpreadsheetExporter(Builder builder){
        this.spreadsheet = builder.spreadsheet;
        this.recipeMap = builder.columns;
        int j=0;
        Spreadsheet.SpreadsheetRow header = spreadsheet.getRow(0);
        for(ColumnValueRecipe<JsonNode> col : recipeMap){
            j+= col.writeHeaderValues(header, j);
        }
    }

    @Override
    public void export(Substance s) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        try {
            JsonNode tree = mapper.readTree(writer.writeValueAsString(s));
            updateReferences(tree);
            Spreadsheet.SpreadsheetRow row = spreadsheet.getRow(this.row++);
            int j=0;
            for(ColumnValueRecipe<JsonNode> recipe : recipeMap){
                j+= recipe.writeValuesFor(row, j, tree);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void close() throws IOException {
        spreadsheet.close();
    }

    public void includePublicDataOnly(boolean publicOnly){
        this.publicOnly = publicOnly;
    }

    private void updateReferences(JsonNode tree) {
        ArrayNode references = (ArrayNode)tree.at("/references");
        Map<String, Integer> refMap = new HashMap<String, Integer>();
        for (int i = 0; i < references.size(); i++) {
            refMap.put(references.get(i).get("uuid").textValue(), i);
        }
        for (JsonNode refsNode: tree.findValues("references")) {
            if (refsNode.isArray()) {
                ArrayNode refs = (ArrayNode) refsNode;
                for (int i = 0; i < refs.size(); i++) {
                    JsonNode ref = refs.get(i);
                    if (ref.isTextual()) {
                        refs.set(i, references.get(refMap.get(ref.asText())));
                    }
                }
            }
        }
    }


    /**
     * Builder class that makes a SpreadsheetExporter.
     *
     */
    public static class Builder{
        private final List<ColumnValueRecipe<JsonNode>> columns = new ArrayList<>();
        private final Spreadsheet spreadsheet;

        private boolean publicOnly = false;

        /**
         * Create a new Builder that uses the given Spreadsheet to write to.
         * @param spreadSheet the {@link Spreadsheet} object that will be written to by this exporter. can not be null.
         *
         * @throws NullPointerException if spreadsheet is null.
         */
        public Builder(Spreadsheet spreadSheet){
            Objects.requireNonNull(spreadSheet);
            this.spreadsheet = spreadSheet;
        }

        public Builder addColumn(Column column, ColumnValueRecipe<JsonNode> recipe){
            return addColumn(column.name(), recipe);
        }

        public Builder addColumn(String columnName, ColumnValueRecipe<JsonNode> recipe){
            Objects.requireNonNull(columnName);
            Objects.requireNonNull(recipe);
            columns.add(recipe);
            return this;
        }


        public Builder renameColumn(Column oldColumn, String newName){
            return renameColumn(oldColumn.name(), newName);
        }

        public Builder renameColumn(String oldName, String newName){
            //use iterator to preserve order
            ListIterator<ColumnValueRecipe<JsonNode>> iter = columns.listIterator();
            while(iter.hasNext()){
                ColumnValueRecipe<JsonNode> oldValue = iter.next();
                ColumnValueRecipe<JsonNode> newValue = oldValue.replaceColumnName(oldName, newName);
                if(oldValue != newValue){
                   iter.set(newValue);
                }
            }
            return this;
        }

        public JmespathSpreadsheetExporter build(){
            return new JmespathSpreadsheetExporter(this);
        }

        public Builder includePublicDataOnly(boolean publicOnly){
            this.publicOnly = publicOnly;
            return this;
        }
    }
}