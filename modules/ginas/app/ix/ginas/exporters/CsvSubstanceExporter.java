package ix.ginas.exporters;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

import ix.core.models.Structure.Stereo;
import ix.ginas.models.v1.ChemicalSubstance;
import ix.ginas.models.v1.Code;
import ix.ginas.models.v1.PolymerSubstance;
import ix.ginas.models.v1.Substance;
import ix.core.GinasProcessingMessage;
import ix.ginas.utils.GinasUtils;

import gov.nih.ncgc.chemical.Chemical;

public class CsvSubstanceExporter implements Exporter<Substance> {
    public static abstract class Columns{
    	static Columns UUID = new Columns("UUID"){
            @Override
            String getContent(Substance s) {
                return s.getOrGenerateUUID().toString();
            }
        };
        
        static Columns NAME = new Columns("PREFERRED TERM"){
            @Override
            String getContent(Substance s) {
                return s.getName();
            }
        };
        static Columns APPROVAL_ID = new Columns("APPROVAL_ID"){
            @Override
            String getContent(Substance s) {
                return s.getApprovalID();
            }
        };
        static Columns SMILES = new Columns("SMILES"){
            @Override
            String getContent(Substance s) {
            	if(s instanceof ChemicalSubstance){
            		return ((ChemicalSubstance)s).structure.smiles;
            	}
                return null;
            }
        };
        static Columns FORMULA = new Columns("FORMULA"){
            @Override
            String getContent(Substance s) {
            	if(s instanceof ChemicalSubstance){
            		return ((ChemicalSubstance)s).structure.formula;
            	}else if(s instanceof PolymerSubstance){
            		return "Polymer substance not supported";
            	}
                return null;
            }
        };
        static Columns SUBSTANCE_TYPE = new Columns("SUBSTANCE_TYPE"){
            @Override
            String getContent(Substance s) {
            	return s.substanceClass.name();
            }
        };
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
        static Columns CAS = getCodeSystemColumn("CAS");
        static Columns EINECS = getCodeSystemColumn("EC (EINECS)");
        static Columns ITIS = getCodeSystemColumn("ITIS");
        static Columns NCBI = getCodeSystemColumn("NCBI TAXONOMY");
        static Columns PLANTS = getCodeSystemColumn("USDA PLANTS");
        static Columns INN_ID = getCodeSystemColumn("INN");
        static Columns NCI_THESAURUS = getCodeSystemColumn("NCI_THESAURUS");
        
        
        
        
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
        
        public static Columns[] values(){
        	
        	
        	
        	return new Columns[]{
        			UUID,
        			APPROVAL_ID,
        			NAME,
        			CAS,
        			FORMULA,
        			INCHIKEY_RESTRICTED,
        			EINECS,
        			NCI_THESAURUS,
        			ITIS,
        			NCBI,
        			PLANTS,
        			SMILES,
        			INN_ID,
        			SUBSTANCE_TYPE
        	};
        }
        
        String title="NO TITLE";
        
        
        public Columns(String title){
        	this.title=title;
        }
    	
        abstract String getContent(Substance s);
        public String getTitle(){
        	return title;
        }
        
    }

    private final List<Columns> columns;

    private final BufferedWriter out;

    private final String separator;
    
    private String extension="csv";

    public CsvSubstanceExporter(OutputStream out, String separator, List<Columns> columns) {
        this.columns = columns;
        this.separator  = separator;
        if(this.separator.equals("\t")){
        	extension="txt";
        }else if (this.separator.equals(",")){
        	extension="csv";
        }
        this.out = new BufferedWriter(new OutputStreamWriter(out));
        try{
        	writeTitles();
        }catch(Exception e){
        	e.printStackTrace();
        }
    }
    
    public void writeTitles() throws IOException{
    	List<String> columnContent = new ArrayList<>(columns.size());
    	for(Columns col : columns){
            String content = col.getTitle();
            if(content==null){
                columnContent.add("");
            }else{
                columnContent.add(content);
            }
        }

        out.write(String.join(separator, columnContent));
        out.newLine();
    }

    @Override
    public void export(Substance s) throws IOException {

        List<String> columnContent = new ArrayList<>(columns.size());
        for(Columns col : columns){
            String content = col.getContent(s);
            if(content==null){
                columnContent.add("");
            }else{
                columnContent.add(content);
            }
        }

        out.write(String.join(separator, columnContent));
        out.newLine();
    }

    @Override
    public void close() throws IOException {
        out.close();
    }
    
    public void setExtension(String ext){
    	this.extension=ext;
    }
    @Override
	public String getExtension() {
		return extension;
	}
}