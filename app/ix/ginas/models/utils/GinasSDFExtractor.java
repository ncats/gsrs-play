package ix.ginas.models.utils;

import gov.nih.ncgc.chemical.Chemical;
import gov.nih.ncgc.chemical.ChemicalReader;
import ix.core.models.Payload;
import ix.core.plugins.GinasRecordProcessorPlugin.RecordExtractor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import akka.event.slf4j.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;

public class GinasSDFExtractor extends RecordExtractor<Map>{
		private static final String LINE_SPLIT = "\n";
		private static final String DELIM = "\t";

		ChemicalExtractor chemExtract;
		
		Map<String,String> mapping = new HashMap<String,String>();
		
		public GinasSDFExtractor(InputStream is) {
			super(is);
			if(is!=null)
				chemExtract=new ChemicalExtractor(is);
			
		}
		
		public void qq(InputStream is) throws IOException{
			System.out.println("########## SDF");
			BufferedReader buff = new BufferedReader(new InputStreamReader(is));
			String line;
			int c=0;
			
			while((line=buff.readLine())!=null){
				System.out.println(line);
				c++;
				if(c>10)break;
			}
		}
		public void qq() throws IOException{
			qq(this.is);
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
			if(c==null){
				System.out.println("############# chemical is null");
			}else{
				System.out.println("############# chemical is NOT null:" + c.getName());
			}
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
		
		
		public static Map<String,FieldStatistics> getFieldStatistics(Payload pl, int MAX){
			GinasSDFExtractor gex=
					(GinasSDFExtractor) new GinasSDFExtractor(null).makeNewExtractor(pl);
			Map m=null;
			System.out.println("########################## Got here!?");
			try {
				gex.qq();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			Map<String,FieldStatistics> fstats = new HashMap<String,FieldStatistics>();
			int count=0;
			
			while((count<MAX) && (m=gex.getNextRecord())!=null){
				System.out.println("############## Got one");
				for(Object k:m.keySet()){
					FieldStatistics fst= fstats.get(k);
					if(fst!=null){
						fst=new FieldStatistics(k+"");
					}
					fst.addValue(m.get(k)+"");
				}
				count++;
			}
			return fstats;
		}
		
		
		
		public static class FieldStatistics{
			public static int MAX_FIRST_STATS=10;
			public static int MAX_TOP_STATS=100;
			public String path;
			public int references;
			
			public Map<String,FieldValue> counts = new HashMap<String,FieldValue>();
			public List<String> firstValues = new ArrayList<String>();
			
			public FieldStatistics(String path){
				this.path=path;
			}
			
			public void addValue(String v){
				if(firstValues.size()<MAX_FIRST_STATS){
					firstValues.add(v);
				}
				FieldValue fv = counts.get(v);
				if(fv==null){
					fv= new FieldValue(v);
				}
				fv.increment();
			}
			
		}
		public static class FieldValue{
			String value;
			int count=0;
			public FieldValue(String f){
				this.value=f;
			}
			public void increment(){
				this.count++;
			}
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