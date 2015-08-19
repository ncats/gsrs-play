package ix.ginas.models.utils;

import gov.nih.ncgc.chemical.Chemical;
import gov.nih.ncgc.chemical.ChemicalReader;
import ix.core.plugins.GinasRecordProcessorPlugin.RecordExtractor;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;

public class GinasSDFExtractor extends RecordExtractor<Map>{
		private static final String LINE_SPLIT = "\n";
		private static final String DELIM = "\t";

		ChemicalExtractor chemExtract;
		
		Map<String,String> mapping = new HashMap<String,String>();
		
		public GinasSDFExtractor(InputStream is) {
			super(is);
			chemExtract=new ChemicalExtractor(is);
			
		}

		
		/*
		 * This will work like this:
		 * 	1) Extract Molecule
		 * 	2) Make decision on substance class
		 * 		For now, always chemical.
		 *  3) Extract molfile, put into
		 *  4) 
		 * 
		 */
		@Override
		public Map getNextRecord() {
			ObjectMapper objectMapper = new ObjectMapper();
			
			List<ExtractionError> errors=new ArrayList<ExtractionError>();
			Chemical c=chemExtract.getNextRecord();
			Map<String,String> keyValueMap = new HashMap<String,String>();
			
			try {
				keyValueMap.put("name", c.getName());
				keyValueMap.put("structure", c.export(Chemical.FORMAT_MOL));
				
				//structureObject.put("molfile", c.export(Chemical.FORMAT_MOL));
			} catch (Exception e) {
				//keyValueMap.put("structure", );
				errors.add(new ExtractionError(e.getMessage(),
							"structure",null
						));
			}
			for(String property:c.getPropertyList()){
				String value = c.getProperty(property);
				int rnum=0;
				for(String line:value.split(LINE_SPLIT)){
					int cnum=0;
					for(String col:line.split(DELIM)){
						String path=property + "." + rnum + "." + cnum;
						keyValueMap.put("property." + path, col);
						cnum++;
					}
					rnum++;
				}
			}
			
			//substanceObject.put("structure", structureObject);
			return keyValueMap;
		}
		
		static public class ExtractionError{
			static enum TYPE {ERROR, WARNING};
			
			TYPE errorType=TYPE.ERROR;
			String sourcePath;
			String desitnationPath;
			String errorMessage;
			
			public ExtractionError(String msg){
				this(msg,null,null);
			}
			public ExtractionError(String msg, String sourcePath,String destination){
				this.errorMessage=msg;
				this.sourcePath=sourcePath;
				this.desitnationPath=destination;
			}
			
			
		}
		

		@Override
		public void close() {
			try{
				this.chemExtract.close();
			}catch(Exception e){
				e.printStackTrace();
			}
		}

		@Override
		public RecordExtractor<Map> makeNewExtractor(InputStream is) {
			return new GinasSDFExtractor(is);
		}
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		public static class ChemicalExtractor extends RecordExtractor<Chemical>{
			ChemicalReader mi;
			public ChemicalExtractor(InputStream is) {
				super(is);
				try{
					ChemicalReader cr = ChemicalReader.DEFAULT_CHEMICAL_FACTORY().createChemicalReader(is);
				}catch(Exception e){
					e.printStackTrace();
				}
			}

			@Override
			public Chemical getNextRecord() {
				if(mi==null)return null;
				try {
					return mi.next();
				} catch (Exception e) {
					e.printStackTrace();
				}
				return null;
			}

			@Override
			public void close() {
				try{
					if(mi!=null){
						//probably should have chemicalreader close
						//System.out.println("Close");
					}

				}catch(Exception e){
					e.printStackTrace();
				}
			}

			@Override
			public RecordExtractor<Chemical> makeNewExtractor(InputStream is) {
				return new ChemicalExtractor(is);
			}
			
		}
		
	}