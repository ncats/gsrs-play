package ix.ginas.exporters;

import gov.nih.ncgc.chemical.Chemical;
import ix.core.models.Structure;
import ix.ginas.models.v1.ChemicalSubstance;
import ix.ginas.models.v1.Code;
import ix.ginas.models.v1.PolymerSubstance;
import ix.ginas.models.v1.Substance;

import java.io.IOException;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by katzelda on 8/19/16.
 */
public abstract class SpreadsheetExporter implements Exporter<Substance> {

    private final Spreadsheet spreadsheet;

    private int row=1;

    private final Map<Column, ColumnValueRecipe<Substance>> recipeMap;


    public SpreadsheetExporter(Spreadsheet spreadsheet, Map<Column, ColumnValueRecipe<Substance>> recipeMap){
        this.spreadsheet = spreadsheet;
        this.recipeMap = recipeMap;
        int j=0;
        Spreadsheet.Row header = spreadsheet.getRow(0);
        for(Column col : recipeMap.keySet()){
            header.getCell(j++).writeString(col.name());
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

        /*



        static Columns INCHIKEY = getChemicalExport("STD_INCHIKEY", Chemical.FORMAT_STDINCHIKEY);
        static Columns INCHIKEY_RESTRICTED = new Columns("STD_INCHIKEY_FORMATTED"){
            @Override
            String getContent(Substance s) {
            	if(s instanceof ChemicalSubstance){
            		Stereo ster=((ChemicalSubstance)s).getStereochemistry();
            		if(!ster.equals(Stereo.ABSOLUTE) && !ster.equals(Stereo.ACHIRAL)){
            			return null;
            		}
            	}

            	String ret=INCHIKEY.getContent(s);
            	if(ret!=null && ret.length()>0){
            		ret=ret.replace("InChIKey=","");
            	}
            	return ret;
            }
        };
        static Columns INCHI = getChemicalExport("STD_INCHI", Chemical.FORMAT_STDINCHI);

        static Columns EINECS = getCodeSystemColumn("EC (EINECS)");
        static Columns ITIS = getCodeSystemColumn("ITIS");
        static Columns NCBI = getCodeSystemColumn("NCBI TAXONOMY");
        static Columns PLANTS = getCodeSystemColumn("USDA PLANTS");
        static Columns INN_ID = getCodeSystemColumn("INN");
        static Columns NCI_THESAURUS = getCodeSystemColumn("NCI_THESAURUS");
         */


        /*
         public static Columns getCodeSystemColumn(final String codeSystem){
        	return new Columns(codeSystem){
                @Override
                String getContent(Substance s) {
                	String bestCode="";
                	for(Code cd: s.codes){
                		if(cd.codeSystem.equals(codeSystem)){
                			if("PRIMARY".equals(cd.type)){
                				bestCode = cd.code;
                				break;
                			}
                			bestCode = cd.code;
                		}
                	}
                	return bestCode;
                }
            };
        }
         */


    }

    /*
     public static Columns getChemicalExport(String format, int chemicalFormat){
        	return new Columns(format){
                @Override
                String getContent(Substance s) {
                	if(s instanceof ChemicalSubstance){
                		try{
	                		Chemical chem = s.toChemical();
	                		String ikey=chem.export(chemicalFormat);
	                		return ikey;
                		}catch(Exception e){

                		}
                	}
                    return null;
                }
            };
        }
     */

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
}
