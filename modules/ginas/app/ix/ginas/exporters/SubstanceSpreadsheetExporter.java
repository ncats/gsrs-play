package ix.ginas.exporters;

import java.io.IOException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Stream;

import gov.nih.ncats.molwitch.Chemical;
import gov.nih.ncats.molwitch.inchi.Inchi;
import ix.core.models.Group;
import ix.core.models.Structure;
import ix.core.util.CachedSupplier;
import ix.core.util.ConfigHelper;
import ix.ginas.controllers.v1.SubstanceFactory;
import ix.ginas.models.v1.ChemicalSubstance;
import ix.ginas.models.v1.Code;
import ix.ginas.models.v1.NucleicAcidSubstance;
import ix.ginas.models.v1.PolymerSubstance;
import ix.ginas.models.v1.ProteinSubstance;
import ix.ginas.models.v1.StructurallyDiverseSubstance;
import ix.ginas.models.v1.Substance;
import ix.ginas.models.v1.SubstanceReference;
import ix.ginas.models.v1.Subunit;
import ix.ginas.utils.GinasUtils;
import ix.utils.Util;

/**
 * Substance Exporter that writes out data to a Spreadsheet.
 * Created by katzelda on 8/19/16.
 */
public class SubstanceSpreadsheetExporter implements Exporter<Substance> {

    private final Spreadsheet spreadsheet;

    private int row=1;

    private final List<ColumnValueRecipe<Substance>> recipeMap;


    private SubstanceSpreadsheetExporter(Builder builder){
        this.spreadsheet = builder.spreadsheet;
        this.recipeMap = builder.columns;
        int j=0;
        Spreadsheet.SpreadsheetRow header = spreadsheet.getRow(0);
        for(ColumnValueRecipe<Substance> col : recipeMap){
            j+= col.writeHeaderValues(header, j);
        }
    }
    @Override
    public void export(Substance s) throws IOException {
        Spreadsheet.SpreadsheetRow row = spreadsheet.getRow( this.row++);

        int j=0;
        for(ColumnValueRecipe<Substance> recipe : recipeMap){
            j+= recipe.writeValuesFor(row, j, s);
        }
    }

    @Override
    public void close() throws IOException {
        spreadsheet.close();
    }

    private static Map<Column, ColumnValueRecipe<Substance>> DEFAULT_RECIPE_MAP;

    static{
        DEFAULT_RECIPE_MAP = new LinkedHashMap<>();

        DEFAULT_RECIPE_MAP.put(DefaultColumns.UUID,  SingleColumnValueRecipe.create( DefaultColumns.UUID ,(s, cell) -> cell.write(s.getOrGenerateUUID())));
        //TODO preferred TERM ?
        DEFAULT_RECIPE_MAP.put(DefaultColumns.NAME, SingleColumnValueRecipe.create( DefaultColumns.NAME ,(s, cell) -> cell.writeString(s.getName())));
        DEFAULT_RECIPE_MAP.put(DefaultColumns.APPROVAL_ID, SingleColumnValueRecipe.create(DefaultColumns.APPROVAL_ID  ,(s, cell) -> cell.writeString(s.getApprovalID())));

        DEFAULT_RECIPE_MAP.put(DefaultColumns.SMILES, SingleColumnValueRecipe.create( DefaultColumns.SMILES ,(s, cell) -> {
            if(s instanceof ChemicalSubstance){
                cell.writeString(((ChemicalSubstance)s).structure.smiles);
            }
        }));

        DEFAULT_RECIPE_MAP.put(DefaultColumns.FORMULA, SingleColumnValueRecipe.create(DefaultColumns.FORMULA  ,(s, cell) -> {
            if(s instanceof ChemicalSubstance){
                cell.writeString(((ChemicalSubstance)s).structure.formula);
            }else if(s instanceof PolymerSubstance){
                cell.writeString("Polymer substance not supported");
            }
        }));
        DEFAULT_RECIPE_MAP.put(DefaultColumns.SUBSTANCE_TYPE, SingleColumnValueRecipe.create( DefaultColumns.SUBSTANCE_TYPE ,(s, cell) -> cell.writeString(s.substanceClass.name())));

        //DEFAULT_RECIPE_MAP.put(DefaultColumns.STD_INCHIKEY, new  ChemicalExportRecipe(Chemical.FORMAT_STDINCHIKEY));

        boolean includeInChiKeysAnyway = ConfigHelper.getBoolean("ix.gsrs.delimitedreports.inchikeysforambiguousstereo", false);
        play.Logger.debug("includeInChiKeysAnyway: " + includeInChiKeysAnyway);
        DEFAULT_RECIPE_MAP.put(DefaultColumns.STD_INCHIKEY_FORMATTED, SingleColumnValueRecipe.create(DefaultColumns.STD_INCHIKEY_FORMATTED  ,(s, cell) ->{
            if(s instanceof ChemicalSubstance){
                Structure.Stereo ster=((ChemicalSubstance)s).getStereochemistry();
                if(!ster.equals(Structure.Stereo.ABSOLUTE) && !ster.equals(Structure.Stereo.ACHIRAL) && !includeInChiKeysAnyway){
                    return;
                }

                try{
                    Chemical chem = s.toChemical();
                    cell.writeString(Inchi.asStdInchi(chem).getKey().replace("InChIKey=",""));
                }catch(Exception e){

                }
            }
        }));

        // DEFAULT_RECIPE_MAP.put(DefaultColumns.STD_INCHI, new  ChemicalExportRecipe(Chemical.FORMAT_STDINCHI));


        DEFAULT_RECIPE_MAP.put(DefaultColumns.CAS, new CodeSystemRecipe(DefaultColumns.CAS,"CAS"));
        DEFAULT_RECIPE_MAP.put(DefaultColumns.EC, new CodeSystemRecipe(DefaultColumns.EC,"ECHA (EC/EINECS)"));
        DEFAULT_RECIPE_MAP.put(DefaultColumns.ITIS, ParentSourceMaterialRecipeWrapper.wrap(new CodeSystemRecipe(DefaultColumns.ITIS, "ITIS")));
        DEFAULT_RECIPE_MAP.put(DefaultColumns.NCBI, ParentSourceMaterialRecipeWrapper.wrap(new CodeSystemRecipe(DefaultColumns.NCBI, "NCBI TAXONOMY")));
        DEFAULT_RECIPE_MAP.put(DefaultColumns.USDA_PLANTS, ParentSourceMaterialRecipeWrapper.wrap(new CodeSystemRecipe(DefaultColumns.USDA_PLANTS, "USDA PLANTS")));
        DEFAULT_RECIPE_MAP.put(DefaultColumns.INN, new CodeSystemRecipe(DefaultColumns.INN, "INN"));
        DEFAULT_RECIPE_MAP.put(DefaultColumns.NCI_THESAURUS, new CodeSystemRecipe(DefaultColumns.NCI_THESAURUS,"NCI_THESAURUS"));
        
        DEFAULT_RECIPE_MAP.put(DefaultColumns.RXCUI, new CodeSystemRecipe(DefaultColumns.RXCUI, "RXCUI"));
        DEFAULT_RECIPE_MAP.put(DefaultColumns.PUBCHEM, new CodeSystemRecipe(DefaultColumns.PUBCHEM, "PUBCHEM"));
        DEFAULT_RECIPE_MAP.put(DefaultColumns.MPNS, ParentSourceMaterialRecipeWrapper.wrap(new CodeSystemRecipe(DefaultColumns.MPNS, "MPNS")));
        DEFAULT_RECIPE_MAP.put(DefaultColumns.GRIN, ParentSourceMaterialRecipeWrapper.wrap(new CodeSystemRecipe(DefaultColumns.GRIN, "GRIN")));
        
        
        DEFAULT_RECIPE_MAP.put(DefaultColumns.INGREDIENT_TYPE, SingleColumnValueRecipe.create(DefaultColumns.INGREDIENT_TYPE, (s, cell) ->{
            cell.writeString(GinasUtils.getIngredientType(s));
        }));


        //Lazy place to put new default columns
        DEFAULT_RECIPE_MAP.put(DefaultColumns.PROTEIN_SEQUENCE,SingleColumnValueRecipe.create(DefaultColumns.PROTEIN_SEQUENCE  , (s, cell) ->{
            if(s instanceof ProteinSubstance){
                List<Subunit> subunits=((ProteinSubstance)s).protein.getSubunits();
                StringBuilder sb = new StringBuilder();
                for(Subunit su:subunits){
                    if(sb.length()!=0){
                        sb.append("|");
                    }
                    sb.append(su.sequence);
                }
                cell.writeString(sb.toString());
            }
        }));

        DEFAULT_RECIPE_MAP.put(DefaultColumns.NUCLEIC_ACID_SEQUENCE, SingleColumnValueRecipe.create( DefaultColumns.NUCLEIC_ACID_SEQUENCE ,(s, cell) ->{
            if(s instanceof NucleicAcidSubstance){
                List<Subunit> subunits=((NucleicAcidSubstance)s).nucleicAcid.getSubunits();

                StringBuilder sb = new StringBuilder();

                for(Subunit su:subunits){
                    if(sb.length()!=0){
                        sb.append("|");
                    }
                    sb.append(su.sequence);
                }
                cell.writeString(sb.toString());
            }
        }));
        DEFAULT_RECIPE_MAP.put(DefaultColumns.RECORD_ACCESS_GROUPS, SingleColumnValueRecipe.create( DefaultColumns.RECORD_ACCESS_GROUPS ,(s, cell) ->{
            StringBuilder sb = new StringBuilder();
            for(Group g:s.getAccess()){
                if(sb.length()!=0){
                    sb.append("|");
                }
                sb.append(g.name);
            }
            cell.writeString(sb.toString());
        }));



    }
    
    private static interface SubstanceColumnValueRecipe extends SingleColumnValueRecipe<Substance>{

    	public default SubstanceFetcherRecipeWrapper wrapped(Function<Substance,Substance> trans){
    		return new SubstanceFetcherRecipeWrapper(this){
				@Override
				public Substance getSubstance(Substance s) {
					return trans.apply(s);
				}
    		};

    	}
    }


    
    private static class ParentSourceMaterialRecipeWrapper extends SubstanceFetcherRecipeWrapper {

        public ParentSourceMaterialRecipeWrapper(ColumnValueRecipe<Substance> del) {
            super(del);
        }

        @Override
        public Substance getSubstance(Substance s) {

            if(s instanceof StructurallyDiverseSubstance){
                StructurallyDiverseSubstance sdiv = (StructurallyDiverseSubstance)s;
                SubstanceReference sr=sdiv.structurallyDiverse.parentSubstance;
                if(sr!=null){
                    Substance full = SubstanceFactory.getFullSubstance(sr);
                    if(full!=null){
                        return full;
                    }
                }
            }
            return s;
        }

        /**
         * Fetches the parent substance (if one exists) rather than the given substance
         * for use in column recipes.
         * @param col
         * @return
         */
        public static ParentSourceMaterialRecipeWrapper wrap(ColumnValueRecipe<Substance> col){
            return new ParentSourceMaterialRecipeWrapper(col);
        }

    }

    
    
    /**
     * Wraps a {@link ColumnValueRecipe} to fetch a (possibly) different object before applying 
     * the recipe.
     * 
     * @author tyler
     *
     */
    private static abstract class SubstanceFetcherRecipeWrapper implements ColumnValueRecipe<Substance>{

    	ColumnValueRecipe<Substance> _delegate;
    	
    	public SubstanceFetcherRecipeWrapper(ColumnValueRecipe<Substance>  del){
    		this._delegate=del;
    		
    	}
    	
    	public abstract Substance getSubstance(Substance s);


        @Override
        public int writeValuesFor(Spreadsheet.SpreadsheetRow row, int currentOffset, Substance obj) {
            return _delegate.writeValuesFor(row, currentOffset, getSubstance(obj));
        }

        @Override
        public int writeHeaderValues(Spreadsheet.SpreadsheetRow row, int currentOffset) {
            return _delegate.writeHeaderValues(row, currentOffset);
        }

        @Override
        public boolean containsColumnName(String name) {
            return _delegate.containsColumnName(name);
        }

        @Override
        public ColumnValueRecipe<Substance> replaceColumnName(String oldName, String newName) {
            _delegate = _delegate.replaceColumnName(oldName, newName);
            return this;
        }
    }
    
    
    
    private static class CodeSystemRecipe implements SingleColumnValueRecipe<Substance>{

        private final String columnName;

        private final String codeSystemToFind;
        private final boolean publicOnly;
        
        public CodeSystemRecipe(Enum<?> columnName, String codeSystemToFind) {
            this(columnName, codeSystemToFind, false);
        }



        public CodeSystemRecipe(Enum<?> columnName, String codeSystemToFind, boolean publicOnly) {
            this.codeSystemToFind = codeSystemToFind;
            this.publicOnly = publicOnly;
            this.columnName = columnName.name();
        }
        private CodeSystemRecipe(String columnName, String codeSystemToFind, boolean publicOnly) {
            this.codeSystemToFind = codeSystemToFind;
            this.publicOnly = publicOnly;
            this.columnName = columnName;
        }

        @Override
        public int writeHeaderValues(Spreadsheet.SpreadsheetRow row, int currentOffset) {
            row.getCell(currentOffset).writeString(columnName);
            return 1;
        }

        public CodeSystemRecipe asPublicOnly(){
            return new CodeSystemRecipe(columnName, codeSystemToFind, true);
        }

        @Override
        public boolean containsColumnName(String name) {
            return Objects.equals(name, columnName);
        }

        @Override
        public ColumnValueRecipe<Substance> replaceColumnName(String oldName, String newName) {
           if(containsColumnName(oldName)){
               return new CodeSystemRecipe(newName, codeSystemToFind, true);
           }
           return this;
        }

        @Override
        public void writeValue(Substance s, SpreadsheetCell cell) {
            
            s.codes
             .stream()
             .filter(cd->!(publicOnly && !cd.isPublic()))
             .filter(cd->codeSystemToFind.equalsIgnoreCase(cd.codeSystem))             
             .sorted(codePriority.get())
             .findFirst()
             .map(cd->{
            	 if("PRIMARY".equals(cd.type)){
            		 return cd.code;
            	 }else{
            		 return cd.code + " [" + cd.type + "]";
            	 }
             })
             .ifPresent(cdstr->{
            	 cell.writeString(cdstr);
             });            
        }
        
        static CachedSupplier<Comparator<Code>> codePriority = CachedSupplier.of(()->{
        	return Util.comparator(c->c.type, Stream.of("PRIMARY",
				    "MAJOR COMPONENT STRUCTURE/SEQUENCE",
				    "NON-SPECIFIC STEREOCHEMISTRY",
				    "NON-SPECIFIC STOICHIOMETRY",
				    "GENERIC (FAMILY)",
				    "NON-SPECIFIC SUBSTITUTION",
				    "ALTERNATIVE",
				    "NO STRUCTURE GIVEN",
				    "SUPERCEDED"));
        });
        
        
        
        
    }

    /**
     * Builder class that makes a SpreadsheetExporter.  By default, the default columns are used
     * but these may be modified using the add/remove column methods.
     *
     */
    public static class Builder{
        private final List<ColumnValueRecipe<Substance>> columns = new ArrayList<>();
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

            for(Map.Entry<Column, ColumnValueRecipe<Substance>> entry : DEFAULT_RECIPE_MAP.entrySet()){
                columns.add(entry.getValue());
            }
        }

        public Builder addColumn(Column column, ColumnValueRecipe<Substance> recipe){
            return addColumn(column.name(), recipe);
        }

        public Builder addColumn(String columnName, ColumnValueRecipe<Substance> recipe){
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
            ListIterator<ColumnValueRecipe<Substance>> iter = columns.listIterator();
            while(iter.hasNext()){

                ColumnValueRecipe<Substance> oldValue = iter.next();
                ColumnValueRecipe<Substance> newValue = oldValue.replaceColumnName(oldName, newName);
                if(oldValue != newValue){
                   iter.set(newValue);
                }
            }
            return this;
        }

        public SubstanceSpreadsheetExporter build(){

            if(publicOnly){
                ListIterator<ColumnValueRecipe<Substance>> iter = columns.listIterator();
                while(iter.hasNext()){

                    ColumnValueRecipe<Substance> value = iter.next();
                    if(value instanceof CodeSystemRecipe){
                        iter.set(((CodeSystemRecipe) value).asPublicOnly());
                    }

                }

            }

            return new SubstanceSpreadsheetExporter(this);
        }

        public Builder includePublicDataOnly(boolean publicOnly){
            this.publicOnly = publicOnly;
            return this;
        }

    }
}