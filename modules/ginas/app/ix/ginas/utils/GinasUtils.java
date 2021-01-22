package ix.ginas.utils;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.Transaction;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import ix.core.validator.GinasProcessingMessage;
import ix.core.UserFetcher;
import ix.core.models.ProcessingRecord;
import ix.core.models.XRef;
import ix.core.plugins.GinasRecordProcessorPlugin.PayloadExtractedRecord;
import ix.core.plugins.GinasRecordProcessorPlugin.TransformedRecord;
import ix.core.processing.RecordExtractor;
import ix.core.processing.RecordPersister;
import ix.core.processing.RecordTransformer;
import ix.core.util.CachedSupplier;
import ix.core.util.ConfigHelper;
import ix.core.util.IOUtil;
import ix.ginas.models.v1.Substance;
import ix.ginas.utils.validation.DefaultSubstanceValidator;
import play.Logger;

public class GinasUtils {
	private static CachedSupplier<Boolean> validation=
			ConfigHelper.supplierOf("ix.ginas.batch.validation", true);
	
	public static CachedSupplier<GinasProcessingStrategy> DEFAULT_BATCH_STRATEGY = CachedSupplier.of(()->{
		String defaultStrat="ACCEPT_APPLY_ALL_MARK_FAILED";
		String strat=ConfigHelper.supplierOf("ix.ginas.batch.validationStrategy", defaultStrat).get();

		try{
			return GinasProcessingStrategy.fromValue(strat);
		}catch(Exception e){
			Logger.error("Unknown strategy name, defaulting to batch strategy of \"" + defaultStrat + "\"", e);
			return GinasProcessingStrategy.fromValue(defaultStrat);
		}
	});



	private static NamedIdGenerator<Substance,String> APPROVAL_ID_GEN;

	public static NamedIdGenerator<Substance, String> getApprovalIdGenerator() {
		return APPROVAL_ID_GEN;
	}

	public static void setApprovalIdGenerator(NamedIdGenerator<Substance,String> approvalIDGenerator) {
		APPROVAL_ID_GEN = approvalIDGenerator;
	}

	public static Substance makeSubstance(InputStream bis) throws Exception {
		ObjectMapper mapper = new ObjectMapper();
		JsonNode tree = mapper.readTree(bis);
		return makeSubstance(tree);
	}

	public static Substance makeSubstance(String bis) throws Exception {
		ObjectMapper mapper = new ObjectMapper();
		JsonNode tree = mapper.readTree(bis);
		return makeSubstance(tree);
	}

	public static GinasProcessingMessage.Link createSubstanceLink(Substance s){
		GinasProcessingMessage.Link l = new GinasProcessingMessage.Link();
		l.href=ix.ginas.controllers.routes.GinasApp.substance(s.getLinkingID())+"";
		l.text="[" + s.getApprovalIDDisplay() + "]" + s.getName();

		return l;
	}

	public static Substance makeSubstance(JsonNode tree) {
		return JsonSubstanceFactory.makeSubstance(tree, null);
	}

    private static char[] UNII_ALPHABET = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();
	private static Pattern UNII_PATTERN = Pattern.compile("^[0-9A-Z]{10}$");

	private static int[] UNII_CHECKSUM = new int[91];
	static{
	    for(int i=0; i<UNII_ALPHABET.length; i++){
	        UNII_CHECKSUM[ UNII_ALPHABET[i]] = i;
        }
    }
	public static boolean isUnii(String unii){
	    String trimmed = unii.trim();
        Matcher m = UNII_PATTERN.matcher(trimmed);
        if(m.matches()){
            //check digit
            char[] chars = trimmed.toCharArray();
            int sum=0;
            for(int i=0; i<9; i++){
                sum += UNII_CHECKSUM[chars[i]];
            }
            int checkDigit = sum % UNII_ALPHABET.length;
            return UNII_ALPHABET[checkDigit] == chars[9];
        }
        return false;
	}

	
	public static boolean persistSubstances(Collection<Substance> subs){
		
		try(Transaction tx = Ebean.beginTransaction()){
			for(Substance s: subs){
				s.save();
			}
			tx.commit();
			return true;
		}catch(Exception ex){
			return false;
		}
	}
	



	

	/*********************************************
	 * Ginas bits for 
	 * 1. extracting from InputStream 
	 * 2. transforming to Substance 
	 * 3. persisting
	 * 
	 * @author peryeata
	 *
	 */
	public static class GinasSubstancePersister extends RecordPersister<Substance, Substance> {
		
		public void persist(TransformedRecord<Substance, Substance> prec) throws Exception {
			//System.out.println("Persisting:" + prec.recordToPersist.uuid + "\t" + prec.recordToPersist.getName());
			UserFetcher.setLocalThreadUser(prec.rec.job.owner);            
			try{
				boolean worked = false;
				List<String> errors = new ArrayList<String>();
				if (prec.recordToPersist != null) {
					worked = persistSubstance(prec.recordToPersist, errors);
					if (worked) {
						prec.rec.status = ProcessingRecord.Status.OK;
						prec.rec.xref = new XRef(prec.recordToPersist);
						prec.rec.xref.save();
					} else {
						prec.rec.message = errors.get(0);
						prec.rec.status = ProcessingRecord.Status.FAILED;
					}
					prec.rec.stop = System.currentTimeMillis();
				}
				prec.rec.save();
	
				
				if (!worked){
					throw new IllegalStateException(prec.rec.message);
				}else{
					Logger.debug("Saved substance " + (prec.recordToPersist != null ? prec.recordToPersist.getUuid() : null)
							+ " record " + prec.rec.id);
				}
			}catch(Throwable t){
				Logger.debug("Fail saved substance " + (prec.recordToPersist != null ? prec.recordToPersist.getUuid() : null)
						+ " record " + prec.rec.id);
				throw t;
			}finally{
				UserFetcher.setLocalThreadUser(null);
			}
		}

		private static boolean persistSubstance(Substance theRecordToPersist, List<String> errors) {
			boolean worked = false;
			Transaction tx = Ebean.beginTransaction();
			try {
				theRecordToPersist.save();
				tx.commit();
				worked = true;
			} catch (Exception ex) {
				ex.printStackTrace();
				if (errors != null)
					errors.add(ex.getMessage());
			} finally {
				tx.end();
			}
			return worked;
		}
	}
	
	
	public static class GinasSubstanceForceAuditPersister extends GinasSubstancePersister {
		
		public void persist(TransformedRecord<Substance, Substance> prec) throws Exception {
			
	        UserFetcher.disableForceAuditUpdate();
			try{
				super.persist(prec);
			}catch(Throwable t){
				throw t;
			}finally{
				UserFetcher.enableForceAuditUpdate();
			}
			
			
		}
	}

	public static class GinasSubstanceTransformer extends GinasAbstractSubstanceTransformer<JsonNode> {

		public GinasSubstanceTransformer(DefaultSubstanceValidator validator) {
			super(validator);
		}

		@Override
		public String getName(JsonNode theRecord) {
			return theRecord.get("name").asText();
		}

		@Override
		public Substance transformSubstance(JsonNode rec) throws Throwable {
			Substance sub = GinasUtils.makeSubstance(rec);

			return sub;
		}
	}
	/**
	 * This Extractor is for explicitly testing that failed validation
	 * records do fail.
	 * 
	 * @author peryeata
	 *
	 */
	public static class GinasAlwaysFailTestDumpExtractor extends GinasDumpExtractor {
		public GinasAlwaysFailTestDumpExtractor(InputStream is) {
			super(is);
		}

		@Override
		public RecordTransformer getTransformer() {
			return new RecordTransformer<JsonNode, Substance>(){
				@Override
				public Substance transform(PayloadExtractedRecord<JsonNode> pr, ProcessingRecord rec) {
					throw new IllegalStateException("Intentionally failed validation");
				}

			};
		}

	}
	
	public static class GinasDumpExtractor extends GinasJSONExtractor {
		BufferedReader buff;

		private static final Pattern TOKEN_SPLIT_PATTERN = Pattern.compile("\t");
		public GinasDumpExtractor(InputStream is) {
			
			super(is);
			try {
				buff = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
			} catch (Exception e) {
				
			}

		}

		@Override
		public JsonNode getNextRecord() throws Exception{
			if (buff == null)
				return null;
			String line=null;
			ObjectMapper mapper = new ObjectMapper();
			while(true){
			try {
				line = buff.readLine();
				if (line == null) {
					return null;
				}
				//use static pattern so we don't recompile on every split call
				//which is what String.split() does
				String[] toks = TOKEN_SPLIT_PATTERN.split(line);
				// Logger.debug("extracting:"+ toks[1]);
//				ByteArrayInputStream bis = new ByteArrayInputStream(toks[2].getBytes(StandardCharsets.UTF_8));
//
//				return mapper.readTree(bis);
					if(toks ==null || toks.length <2){
						continue;
					}

				return mapper.readTree(toks[2]);
			} catch (Exception e) {
				e.printStackTrace();
				throw e;
			}
		}
		}

		@Override
		public void close() {
			try {
				if (buff != null)
					buff.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		@Override
		public RecordExtractor<JsonNode> makeNewExtractor(InputStream is) {
			return new GinasDumpExtractor(is);
		}

	}

	public static class GinasJSONExtractor extends RecordExtractor<JsonNode> {
		
		
		public GinasJSONExtractor() {
			super(null);
		}

		public GinasJSONExtractor(InputStream is) {
			super(is);
		}

		public GinasJSONExtractor(String s) throws UnsupportedEncodingException {
			super(new ByteArrayInputStream(s.getBytes("utf8")));
		}

		@Override
		public JsonNode getNextRecord() throws Exception{
			if (is == null)
				return null;

			try {
				ObjectMapper mapper = new ObjectMapper();
				JsonNode tree = mapper.readTree(is);
				is.close();
				is = null;
				return tree;
			} catch (IOException e) {
				e.printStackTrace();
				throw e;
			}
		}

		@Override
		public void close() {
            if(is ==null){
                return;
            }
            try{
                is.close();
            }catch(IOException e){
                e.printStackTrace();
                //ignore exception
            }
            is = null;
		}

		@Override
		public RecordExtractor<JsonNode> makeNewExtractor(InputStream is) {
			return new GinasJSONExtractor(is);
		}

		@Override
		public RecordTransformer getTransformer() {
			
			if(validation.get()){
				return new GinasSubstanceTransformer(DefaultSubstanceValidator.BATCH_SUBSTANCE_VALIDATOR(DEFAULT_BATCH_STRATEGY.get()));
			}else{
				return new GinasSubstanceTransformer(DefaultSubstanceValidator.IGNORE_SUBSTANCE_VALIDATOR());
			}
			
		}

	}

	public abstract static class GinasAbstractSubstanceTransformer<K> extends RecordTransformer<K, Substance> {
		
		
		DefaultSubstanceValidator validator;
		
		public GinasAbstractSubstanceTransformer(){
			useDefaultValidator();
		}
		public GinasAbstractSubstanceTransformer(DefaultSubstanceValidator validator){
			this.setValidator(validator);
		}
		
		public void setValidator(DefaultSubstanceValidator validator){
			this.validator=validator;
		}
		
		public void useDefaultValidator(){
			setValidator(DefaultSubstanceValidator.BATCH_SUBSTANCE_VALIDATOR(DEFAULT_BATCH_STRATEGY.get()));
		}

		@Override
		public Substance transform(PayloadExtractedRecord<K> pr, ProcessingRecord rec) {

			try {
				rec.name = getName(pr.theRecord);
			} catch (Exception e) {
				rec.name = "Nameless";
			}

			// System.out.println("############## transforming:" + rec.name);
			rec.job = pr.job;
			rec.start = System.currentTimeMillis();
			Substance sub = null;
			try {
				sub = transformSubstance(pr.theRecord);
				sub.addImportReference(rec.job);
				validator.validate(sub);
				rec.status = ProcessingRecord.Status.ADAPTED;
			} catch (Throwable t) {
				rec.stop = System.currentTimeMillis();
				rec.status = ProcessingRecord.Status.FAILED;
				rec.message = t.getMessage();
				Logger.error(t.getMessage());
				t.printStackTrace();
				throw new IllegalStateException(t);
			}
			return sub;
		}

		public abstract String getName(K theRecord);

		public abstract Substance transformSubstance(K rec) throws Throwable;
	}





    /**
     * 
     *  Has a reflexive type. Looks through the substance to see if it
     *  has any relationships which points back to itself and contains
     *  the given string, returns true.
     * 
     **/
    public static boolean hasReflexiveType(Substance s, String typeContains){
    
    	boolean isReflexive = s.relationships
                    .stream()
    				.filter(r -> r.type.contains(typeContains))
                    .filter(r -> r.relatedSubstance.refuuid.equals(s.uuid.toString()))
                    .findAny()
    				.isPresent();
    				
    	return isReflexive;
    }
    
    /**
     * 
     *  Returns the ingredient type classification for
     *  the given substance.
     * 
     **/
    public static String getIngredientType(Substance s){
    	
    	if(hasReflexiveType(s,"IONIC MOIETY")){
    		return "IONIC MOIETY";
    	}
    	
    	if(hasReflexiveType(s,"MOLECULAR FRAGMENT")){
    		return "MOLECULAR FRAGMENT";
    	}
    	
    	if(hasReflexiveType(s,"UNSPECIFIED INGREDIENT")){
    		return "UNSPECIFIED INGREDIENT";
    	}
    	
    	if(hasReflexiveType(s,"SPECIFIED SUBSTANCE")){
    		return "SPECIFIED SUBSTANCE";
    	}
    	
    	return "INGREDIENT SUBSTANCE";
    }
	
}
