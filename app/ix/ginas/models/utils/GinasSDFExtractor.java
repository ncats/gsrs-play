package ix.ginas.models.utils;

import gov.nih.ncgc.chemical.Chemical;
import gov.nih.ncgc.chemical.ChemicalReader;
import gov.nih.ncgc.jchemical.JchemicalReader;

import ix.core.models.Payload;
import ix.core.plugins.GinasRecordProcessorPlugin.RecordExtractor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.lang.Comparable;
import java.util.Collections;

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
				//System.out.println("############# chemical is null");
			}else{
				//System.out.println("############# chemical is NOT null:" + c.getName());
			}
			Map<String,String> keyValueMap = new HashMap<String,String>();
			
			try {
				keyValueMap.put("name", c.getName());
				keyValueMap.put("molfile", c.export(Chemical.FORMAT_MOL));
				
				//structureObject.put("molfile", c.export(Chemical.FORMAT_MOL));
			} catch (Exception e) {
				e.printStackTrace();
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
						String path=property + "{" + rnum + "}{" + cnum + "}";
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
			
			
			Map<String,FieldStatistics> fstats = new TreeMap<String,FieldStatistics>();
			int count=0;

			Map m=null;
			while((count<MAX) && (m=gex.getNextRecord())!=null){
				//System.out.println("############## Got one");
				Set<String> uniqueFields = new HashSet<String>();
				for(Object k:m.keySet()){
					String tstart = (k+"").split("\\{")[0];
					//System.out.println("############## got stat:" + k);
					FieldStatistics fst= fstats.get(tstart);
					if(fst==null){
						fst=new FieldStatistics(tstart);
						fstats.put(tstart,fst);
					}
					fst.addValue(m.get(k)+"",k+"");
					if(!uniqueFields.contains(tstart)){
						uniqueFields.add(tstart);
						fst.recordReferences++;
					}
				}
				count++;
			}
			for(String k : fstats.keySet()){
				fstats.get(k).arrangeCounts();
			}
			return fstats;
		}
		
		
		
		public static class FieldStatistics{
			public static enum DATA_TYPE{
				STRING,
				NUMBER,
				ORDINAL,
				UNKNOWN
			};			
			public static int MAX_FIRST_STATS=5;
			public static int MAX_TOP_STATS=100;
			public static int MAX_ORDINAL_CARDINALITY=5;
			public String path;
			public int references;
			public int recordReferences;
			
			private DATA_TYPE dataType=DATA_TYPE.UNKNOWN;
			
			public Map<String,FieldValue> counts = new LinkedHashMap<String,FieldValue>();
			public List<String> firstValues = new ArrayList<String>();
			public LinkedHashSet<String> paths = new LinkedHashSet<String>();
			
			public FieldStatistics(String path){
				this.path=path;
			}
			
			public void addValue(String v, String path){
				references++;
				try{
					double d=Double.parseDouble(v);
					if(dataType==DATA_TYPE.UNKNOWN){
						dataType=DATA_TYPE.NUMBER;
					}
				}catch(Exception e){
					if(dataType==DATA_TYPE.NUMBER){
						dataType=DATA_TYPE.STRING;
					}
				}
				if(firstValues.size()<MAX_FIRST_STATS){
					firstValues.add(v);
				}
				FieldValue fv = counts.get(v);
				if(fv==null){
					fv= new FieldValue(v);
					counts.put(v, fv);
				}
				fv.increment();
				if(counts.size()>MAX_ORDINAL_CARDINALITY)
					arrangeCounts();
				paths.add(path);
			}
			public void arrangeCounts(){
				List<FieldValue> fvals = new ArrayList<FieldValue>(counts.values());
				Collections.sort(fvals);
				counts.clear();
				int k=0;
				for(FieldValue fv1:fvals){
					if(k<MAX_ORDINAL_CARDINALITY){
						counts.put(fv1.value,fv1);
					}else{
						break;
					}
					k++;
				}
			}

			public int getCardinality(){
				return counts.size();
			}

			public DATA_TYPE getPredictedType(){
				if(dataType == DATA_TYPE.UNKNOWN){
					if(getCardinality() < MAX_ORDINAL_CARDINALITY)
						return DATA_TYPE.ORDINAL;
					return DATA_TYPE.STRING;
				}
				return dataType;
			}
			
		}
		public static class FieldValue implements Comparable<FieldValue>{
			public String value;
			public int count=0;
			public FieldValue(String f){
				this.value=f;
			}
			public void increment(){
				this.count++;
			}
			public int compareTo(FieldValue fv){
				return this.count-fv.count;
			}
		}
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		public static class ChemicalExtractor extends RecordExtractor<Chemical>{
			ChemicalReader mi;
			public ChemicalExtractor(InputStream is) {
				super(is);
				try{
					mi = new JchemicalReader().createChemicalReader(is);
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
