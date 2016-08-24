package ix.ginas.exporters;

import gov.nih.ncgc.chemical.Chemical;
import ix.core.models.Structure;
import ix.ginas.models.v1.ChemicalSubstance;
import ix.ginas.models.v1.Code;
import ix.ginas.models.v1.PolymerSubstance;
import ix.ginas.models.v1.Substance;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Substance Exporter that writes out data to a Spreadsheet.
 * Created by katzelda on 8/19/16.
 */
public class SubstanceSpreadsheetExporter implements Exporter<Substance> {

    private final Spreadsheet spreadsheet;

    private int row=1;

    private final Map<String, ColumnValueRecipe<Substance>> recipeMap;


    private SubstanceSpreadsheetExporter(Builder builder){
        this.spreadsheet = builder.spreadsheet;
        this.recipeMap = builder.columns;
        int j=0;
        Spreadsheet.Row header = spreadsheet.getRow(0);
        for(String col : recipeMap.keySet()){
            header.getCell(j++).writeString(col);
        }
    }
    @Override
    public void export(Substance s) throws IOException {
        Spreadsheet.Row header = spreadsheet.getRow( row++);

        int j=0;
        for(ColumnValueRecipe<Substance> recipe : recipeMap.values()){
            SpreadsheetCell cell = header.getCell(j++);
            recipe.writeValue(s, cell);
        }
    }

    @Override
    public void close() throws IOException {
        spreadsheet.close();
    }

    private static Map<Column, ColumnValueRecipe<Substance>> DEFAULT_RECIPE_MAP;

    static{
        DEFAULT_RECIPE_MAP = new LinkedHashMap<>();

        DEFAULT_RECIPE_MAP.put(DefaultColumns.UUID, (s, cell) -> cell.write(s.getOrGenerateUUID()));
        //TODO preferred TERM ?
        DEFAULT_RECIPE_MAP.put(DefaultColumns.NAME, (s, cell) -> cell.writeString(s.getName()));
        DEFAULT_RECIPE_MAP.put(DefaultColumns.APPROVAL_ID, (s, cell) -> cell.writeString(s.getApprovalID()));

        DEFAULT_RECIPE_MAP.put(DefaultColumns.SMILES, (s, cell) -> {
            if(s instanceof ChemicalSubstance){
                cell.writeString(((ChemicalSubstance)s).structure.smiles);
            }
        });

        DEFAULT_RECIPE_MAP.put(DefaultColumns.FORMULA, (s, cell) -> {
            if(s instanceof ChemicalSubstance){
                cell.writeString(((ChemicalSubstance)s).structure.formula);
            }else if(s instanceof PolymerSubstance){
                cell.writeString("Polymer substance not supported");
            }
        });
        DEFAULT_RECIPE_MAP.put(DefaultColumns.SUBSTANCE_TYPE, (s, cell) -> cell.writeString(s.substanceClass.name()));

        DEFAULT_RECIPE_MAP.put(DefaultColumns.STD_INCHIKEY, new  ChemicalExportRecipe(Chemical.FORMAT_STDINCHIKEY));

        DEFAULT_RECIPE_MAP.put(DefaultColumns.STD_INCHIKEY_FORMATTED, (s, cell) ->{
            if(s instanceof ChemicalSubstance){
                Structure.Stereo ster=((ChemicalSubstance)s).getStereochemistry();
                if(!ster.equals(Structure.Stereo.ABSOLUTE) && !ster.equals(Structure.Stereo.ACHIRAL)){
                    return;
                }

                try{
                    Chemical chem = s.toChemical();
                    cell.writeString( chem.export(Chemical.FORMAT_STDINCHIKEY).replace("InChIKey=",""));
                }catch(Exception e){

                }
            }
        });

        DEFAULT_RECIPE_MAP.put(DefaultColumns.STD_INCHI, new  ChemicalExportRecipe(Chemical.FORMAT_STDINCHI));


        DEFAULT_RECIPE_MAP.put(DefaultColumns.CAS, new CodeSystemRecipe("CAS"));
        DEFAULT_RECIPE_MAP.put(DefaultColumns.EINES, new CodeSystemRecipe("EC (EINECS)"));
        DEFAULT_RECIPE_MAP.put(DefaultColumns.ITIS, new CodeSystemRecipe("ITIS"));
        DEFAULT_RECIPE_MAP.put(DefaultColumns.NCBI_TAXOMONY, new CodeSystemRecipe("NCBI TAXONOMY"));
        DEFAULT_RECIPE_MAP.put(DefaultColumns.USDA_PLANTS, new CodeSystemRecipe("USDA PLANTS"));
        DEFAULT_RECIPE_MAP.put(DefaultColumns.INN, new CodeSystemRecipe("INN"));
        DEFAULT_RECIPE_MAP.put(DefaultColumns.NCI_THESAURUS, new CodeSystemRecipe("NCI_THESAURUS"));




    }

    private static class ChemicalExportRecipe implements ColumnValueRecipe<Substance>{

        private final  int chemicalFormat;

        public ChemicalExportRecipe(int chemicalFormat) {
            this.chemicalFormat = chemicalFormat;
        }

        @Override
        public void writeValue(Substance s, SpreadsheetCell cell) {
            if(s instanceof ChemicalSubstance){
                try{
                    Chemical chem = s.toChemical();
                    cell.writeString(chem.export(chemicalFormat));
                }catch(Exception e){

                }
            }
        }
    }

    private static class CodeSystemRecipe implements ColumnValueRecipe<Substance>{

        private final String codeSystemToFind;

        public CodeSystemRecipe(String codeSystemToFind) {
            this.codeSystemToFind = codeSystemToFind;
        }

        @Override
        public void writeValue(Substance s, SpreadsheetCell cell) {
            String bestCode=null;
            for(Code cd: s.codes){
                if(cd.codeSystem.equals(codeSystemToFind)){
                    if("PRIMARY".equals(cd.type)){
                        bestCode = cd.code;
                        break;
                    }
                    bestCode = cd.code;
                }
            }

            if(bestCode !=null){
                cell.writeString(bestCode);
            }
        }
    }

    /**
     * Builder class that makes a SpreadsheetExporter.  By default, the default columns are used
     * but these may be modified using the add/remove column methods.
     *
     */
    public static class Builder{
        private final Map<String, ColumnValueRecipe<Substance>> columns = new LinkedHashMap<>();
        private final Spreadsheet spreadsheet;

        /**
         * Create a new Builder that uses the given Spreadsheet to write to.
         * @param spreadSheet the {@link Spreadsheet} object that will be written to by this exporter. can not be null.
         *
         * @throws NullPointerException if spreadsheet is null.
         */
        public Builder(Spreadsheet spreadSheet){
            Objects.requireNonNull(spreadSheet);
            this.spreadsheet = spreadSheet;

            for(Map.Entry<Column, ColumnValueRecipe<Substance>> entry : DEFAULT_RECIPE_MAP.entrySet()){
                columns.put(entry.getKey().name(), entry.getValue());
            }
        }

        public Builder addColumn(Column column, ColumnValueRecipe<Substance> recipe){
            return addColumn(column.name(), recipe);
        }
        public Builder addColumn(String columnName, ColumnValueRecipe<Substance> recipe){
            Objects.requireNonNull(columnName);
            Objects.requireNonNull(recipe);
            columns.put(columnName, recipe);

            return this;
        }

        public Builder removeColumn(Column column){
            return removeColumn(column.name());
        }

        public Builder removeColumn(String columnName){
            columns.remove(columnName);
            return this;
        }

        public SubstanceSpreadsheetExporter build(){
            return new SubstanceSpreadsheetExporter(this);
        }
    }
}
