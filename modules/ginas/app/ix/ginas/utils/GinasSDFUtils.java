package ix.ginas.utils;


import gov.nih.ncats.molwitch.Chemical;
import gov.nih.ncats.molwitch.io.ChemicalReader;
import gov.nih.ncats.molwitch.io.ChemicalReaderFactory;
import ix.core.models.Keyword;
import ix.core.models.Payload;
import ix.core.models.Structure;
import ix.core.processing.RecordExtractor;
import ix.core.processing.RecordTransformer;
import ix.core.util.IOUtil;
import ix.core.util.TimeUtil;
import ix.ginas.models.v1.ChemicalSubstance;
import ix.ginas.models.v1.Code;
import ix.ginas.models.v1.GinasChemicalStructure;
import ix.ginas.models.v1.Name;
import ix.ginas.models.v1.Reference;
import ix.ginas.models.v1.Substance;
import ix.ginas.utils.GinasUtils.GinasAbstractSubstanceTransformer;
import ix.ginas.utils.validation.DefaultSubstanceValidator;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

import com.fasterxml.jackson.databind.ObjectMapper;

public class GinasSDFUtils {
	private static final String FIELD_NAME = "#name";
	private static final String FIELD_INDEX = "#index";
	private static final String FIELD_MOLFILE = "#molfile";
	
	
	//Used temporarily
	public static Map<String,List<PATH_MAPPER>> mappers = new ConcurrentHashMap<String,List<PATH_MAPPER>>();
	
	
	public static class GinasSDFExtractor extends RecordExtractor<Map>{
		private static final String LINE_SPLIT = "\n";
		private static final String DELIM = "\t";

		private static final Pattern LINE_SPLIT_PATTERN = Pattern.compile(LINE_SPLIT);
		private static final Pattern DELIM_SPLIT_PATTERN = Pattern.compile(DELIM);


		ChemicalExtractor chemExtract;
		public int recordNumber=0;
		
		
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
			recordNumber++;
			ObjectMapper objectMapper = new ObjectMapper();
			//System.out.println("########## extracting");
			List<ExtractionError> errors=new ArrayList<ExtractionError>();
			Chemical c=chemExtract.getNextRecord();
			if(c==null)return null;
			
			Map<String,String> keyValueMap = new HashMap<String,String>();
			
			keyValueMap.put(FIELD_INDEX, recordNumber+"");
			try {
				keyValueMap.put(FIELD_NAME, c.getName());
				keyValueMap.put(FIELD_MOLFILE, c.toMol());
				//structureObject.put("molfile", c.export(Chemical.FORMAT_MOL));
			} catch (Exception e) {
				e.printStackTrace();
				//keyValueMap.put("structure", );
				errors.add(new ExtractionError(e.getMessage(),
						FIELD_MOLFILE,null
						));
			}

			for(Map.Entry<String, String> entry : c.getProperties().entrySet()){

				int rnum=0;
				for(String line : LINE_SPLIT_PATTERN.split(entry.getValue())){
					int cnum=0;
					for(String col : DELIM_SPLIT_PATTERN.split(line)){
						String path=entry.getKey() + "{" + rnum + "}{" + cnum + "}";
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
			 enum TYPE {ERROR, WARNING};
			
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
				UNKNOWN,
				STRUCTURE
			};			
			public static int MAX_FIRST_STATS=5;
			public static int MAX_TOP_STATS=100;
			public static int MAX_ORDINAL_CARDINALITY=10;
			public static int MAX_REWORK_COUNTS=20;
			public String path;
			public int references;
			public int recordReferences;
			
			private DATA_TYPE dataType=DATA_TYPE.UNKNOWN;
			
			public Map<String,FieldValue> counts = new LinkedHashMap<String,FieldValue>();
			public List<String> firstValues = new ArrayList<String>();
			public LinkedHashSet<String> paths = new LinkedHashSet<String>();
			
			public FieldStatistics(String path){
				this.path=path;
				
				if(path.equals(FIELD_MOLFILE)){
					dataType=DATA_TYPE.STRUCTURE;
				}
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
				if(counts.size()>MAX_REWORK_COUNTS)
					arrangeCounts();
				paths.add(path);
			}
			public void arrangeCounts(){
				List<FieldValue> fvals = new ArrayList<FieldValue>(counts.values());
				Collections.sort(fvals);
				counts.clear();
				int k=0;
				for(FieldValue fv1:fvals){
					if(k<MAX_REWORK_COUNTS){
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
			public String getCardinalityString(){
				if(getCardinality() < MAX_ORDINAL_CARDINALITY)
					return getCardinality() + "";
				return ">" + getCardinality();
			}

			public DATA_TYPE getPredictedType(){
				if(dataType == DATA_TYPE.UNKNOWN){
					if(getCardinality() < MAX_REWORK_COUNTS)
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
				return -(this.count-fv.count);
			}
		}
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		public static class ChemicalExtractor extends RecordExtractor<Chemical>{
			ChemicalReader mi;
			public ChemicalExtractor(InputStream is) {
				super(is);
				try{
					mi = ChemicalReaderFactory.newReader(is);
				}catch(Exception e){
					e.printStackTrace();
				}
			}

			@Override
			public Chemical getNextRecord() {
				if(mi==null || !mi.canRead()){
					return null;
				}

				try {
					return mi.read();
				} catch (Exception e) {
					e.printStackTrace();
				}
				return null;
			}

			@Override
			public void close() {
				IOUtil.closeQuietly(mi);
			}

			@Override
			public RecordExtractor<Chemical> makeNewExtractor(InputStream is) {
				return new ChemicalExtractor(is);
			}

			@Override
			public RecordTransformer getTransformer() {
				return null;
			}
			
		}


		@Override
		public RecordTransformer getTransformer() {
			return new GinasFlatMapTransformer();
		}

		@Override
		public RecordTransformer getTransformer(Payload p) {
			GinasFlatMapTransformer gfmt= new GinasFlatMapTransformer(mappers.get(p.id.toString()));
			gfmt.useDefaultValidator();
			return gfmt;
		}	
	}
	
	
	public static class GinasFlatMapTransformer extends GinasAbstractSubstanceTransformer<Map<String,String>>{
		List<PATH_MAPPER> fieldMaps = new ArrayList<PATH_MAPPER>();
		
		public GinasFlatMapTransformer(){
			super();
			fieldMaps.add(new PATH_MAPPER(FIELD_NAME,false,PATH_MAPPER.ADD_METHODS.ADD_NAME));
			fieldMaps.add(new PATH_MAPPER(FIELD_MOLFILE,false,PATH_MAPPER.ADD_METHODS.SET_STRUCTURE));
			fieldMaps.add(new PATH_MAPPER(".*",false,PATH_MAPPER.ADD_METHODS.NOTE_PROPERTY,true));
		}
		public GinasFlatMapTransformer(List<PATH_MAPPER> fieldMaps){
			super();
			if(fieldMaps!=null){
				this.fieldMaps=fieldMaps;
			}
		}
		
		@Override
		public String getName(Map<String, String> theRecord) {
			return theRecord.get(FIELD_NAME);
		}

		@Override
		public Substance transformSubstance(Map<String, String> rec)
				throws Throwable {
			return convertToStructure(rec,fieldMaps);
		}
		
		
	}
	public static class PATH_MAPPER {
		public static enum ADD_METHODS {
			ADD_NAME, ADD_CODE, ADD_PREFERRED_NAME, ADD_SYSTEMATIC_NAME, SET_STRUCTURE, SIMPLE_NOTE, NOTE_PROPERTY, DONT_IMPORT, NULL_TYPE
		};
		
		public String path;
		public boolean isregex=false;
		public boolean startswith=true;
		public boolean allowMultiple = false;
		public String splitBy = null;
		public ADD_METHODS method;
		public String other;

		
		
		public PATH_MAPPER(String pth, boolean allow, ADD_METHODS am, boolean regex) {
			this.path = pth;
			this.allowMultiple = allow;
			this.method = am;
			this.isregex=regex;
		}
		public PATH_MAPPER(String pth, boolean allow, ADD_METHODS am) {
			this(pth,allow,am,false);
		}
		public PATH_MAPPER() {
			
		}
		
		
		public boolean matches(String path){
			if(isregex){
				return path.matches(this.path);
			}
			if(startswith){
				return path.startsWith(this.path);
			}
			return this.path.equals(path);
		}
		public boolean apply(Substance csub, String sourceValue, String sourcePath){
			String[] values = new String[]{sourceValue};
			
			if(splitBy!=null){
				values=sourceValue.split(splitBy);
			}
			
			for (String value : values) {
				switch (method) {
				case ADD_CODE:
					PATH_MAPPER.ADD_CODE(csub, value, other, sourcePath);
					break;
				case ADD_NAME:
					PATH_MAPPER.ADD_NAME(csub, value, sourcePath);
					break;
				case ADD_PREFERRED_NAME:
					PATH_MAPPER.ADD_NAME_PREFERRED(csub, value, sourcePath);
					break;
				case NOTE_PROPERTY:
					PATH_MAPPER.ADD_PROPERTY_NOTE(csub, value, sourcePath);
					break;
				case SET_STRUCTURE:
					PATH_MAPPER.SET_STRUCTURE((ChemicalSubstance)csub, value, sourcePath);
					break;
				case SIMPLE_NOTE:
					PATH_MAPPER.ADD_SIMPLE_NOTE(csub, value, sourcePath);
					break;
				case ADD_SYSTEMATIC_NAME:
					PATH_MAPPER.ADD_SYSTEMATIC_NAME(csub, value, sourcePath);
					break;
				case DONT_IMPORT:
				default:
					break;
				}
			}
			return true;
		}

		@Override
		public int hashCode() {
			return path.hashCode();
		}

		

		public static void ADD_NAME(Substance sub, String name, String path) {
			Name n = new Name();
			n.name = name;
			sub.names.add(n);
			n.addReference(makePathReference(sub, path));
		}

		public static void ADD_SYSTEMATIC_NAME(Substance sub, String name,
				String path) {
			Name n = new Name();
			n.name = name;
			n.type = "sys";
			sub.names.add(n);
			n.addReference(makePathReference(sub, path));
		}

		public static void ADD_NAME_PREFERRED(Substance sub, String name,
				String path) {
			Name n = new Name();
			n.name = name;
			n.preferred = true;
			sub.names.add(n);
			n.addReference(makePathReference(sub, path));
		}

		public static void ADD_CODE(Substance sub, String code,
				String code_system, String path) {
			Code n = new Code();
			n.code = code;
			n.codeSystem = code_system;
			sub.codes.add(n);
			n.addReference(makePathReference(sub, path));
		}

		public static void ADD_PROPERTY_NOTE(Substance sub, String note,
				String path) {
			sub.addPropertyNote(note, path);
		}

		public static void ADD_SIMPLE_NOTE(Substance sub, String note,
				String path) {
			sub.addNote(note).addReference(makePathReference(sub, path));
		}
		
		
		public static void SET_STRUCTURE(ChemicalSubstance csub,
				String molfile, String path) {
			if (csub.structure == null)
				csub.structure = new GinasChemicalStructure();
			csub.structure.molfile = molfile;

		}

		public static Reference makePathReference(Substance s, String path) {
			Reference r = new Reference();
			r.citation = path;
			r.docType = "SDF_PROPERTY";
			r.documentDate= TimeUtil.getCurrentDate();
			s.references.add(r);
			return r;
		}

	}
	
	public static Substance convertToStructure(Map<String,String> keyValueMap,List<PATH_MAPPER> fieldMaps){
		
		
		Set<String> assigned = new HashSet<String>();
		
		
		ChemicalSubstance csub = new ChemicalSubstance(); 
		csub.structure= new GinasChemicalStructure();
		
		for(PATH_MAPPER mapper:fieldMaps){			
			for(String k:keyValueMap.keySet()){
				if(assigned.contains(k) && !mapper.allowMultiple)
					continue;
				if (mapper.matches(k)) {
					mapper.apply(csub, keyValueMap.get(k), k);
					assigned.add(k);
				}
			}
		}
		
		return csub;			
	}
	
	public static void setPathMappers(String payloadUUID, List<PATH_MAPPER> fieldMaps){
		fieldMaps.add(new PATH_MAPPER(FIELD_MOLFILE,false,PATH_MAPPER.ADD_METHODS.SET_STRUCTURE));
		mappers.put(payloadUUID, fieldMaps);
	}
	public static String PATH_TYPES(){
		ObjectMapper om = new ObjectMapper();
		return om.valueToTree(PATH_MAPPER.ADD_METHODS.values()).toString();
	}
}
