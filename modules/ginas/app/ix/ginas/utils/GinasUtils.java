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

import com.avaje.ebean.Ebean;
import com.avaje.ebean.Transaction;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import gov.nih.ncgc.chemical.Chemical;
import gov.nih.ncgc.chemical.ChemicalFactory;
import gov.nih.ncgc.jchemical.JchemicalReader;
import ix.core.GinasProcessingMessage;
import ix.core.models.ProcessingRecord;
import ix.core.models.Structure;
import ix.core.models.XRef;
import ix.core.plugins.GinasRecordProcessorPlugin.PayloadExtractedRecord;
import ix.core.plugins.GinasRecordProcessorPlugin.TransformedRecord;
import ix.core.plugins.SequenceIndexerPlugin;
import ix.core.processing.RecordExtractor;
import ix.core.processing.RecordPersister;
import ix.core.processing.RecordTransformer;
import ix.ginas.models.v1.ChemicalSubstance;
import ix.ginas.models.v1.Code;
import ix.ginas.models.v1.MixtureSubstance;
import ix.ginas.models.v1.NucleicAcidSubstance;
import ix.ginas.models.v1.Name;
import ix.ginas.models.v1.PolymerSubstance;
import ix.ginas.models.v1.ProteinSubstance;
import ix.ginas.models.v1.SpecifiedSubstanceGroup1Substance;
import ix.ginas.models.v1.StructurallyDiverseSubstance;
import ix.ginas.models.v1.Substance;
import ix.ginas.utils.validation.DefaultSubstanceValidator;
import ix.seqaln.SequenceIndexer;
import play.Logger;
import play.Play;

import java.util.regex.Pattern;

public class GinasUtils {
	public static GinasProcessingStrategy DEFAULT_BATCH_STRATEGY = GinasProcessingStrategy
			.ACCEPT_APPLY_ALL_MARK_FAILED();
	private static IDGenerator<String> APPROVAL_ID_GEN = new UNIIGenerator();
	


	public static IDGenerator<String> getAPPROVAL_ID_GEN() {
		return APPROVAL_ID_GEN;
	}

	public static void setAPPROVAL_ID_GEN(IDGenerator<String> aPPROVAL_ID_GEN) {
		APPROVAL_ID_GEN = aPPROVAL_ID_GEN;
	}

	public static ChemicalFactory DEFAULT_FACTORY = new JchemicalReader();
	public static String NULL_MOLFILE = "\n\n\n  0  0  0     0  0            999 V2000\nM  END\n\n$$$$";

	public static final SequenceIndexer _seqIndexer = Play.application().plugin(SequenceIndexerPlugin.class)
			.getIndexer();





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
		return makeSubstance(tree, null);
	}

	public static Substance makeSubstance(JsonNode tree, List<GinasProcessingMessage> messages) {

		JsonNode subclass = tree.get("substanceClass");
		ObjectMapper mapper = new ObjectMapper();

		mapper.addHandler(new GinasV1ProblemHandler(messages));
		Substance sub = null;
		if (subclass != null && !subclass.isNull()) {

			Substance.SubstanceClass type;
			try {
				type = Substance.SubstanceClass.valueOf(subclass.asText());
			} catch (Exception e) {
				throw new IllegalStateException("Unimplemented substance class:" + subclass.asText());
			}
			try {
				switch (type) {
				case chemical:
					
					ObjectNode structure = (ObjectNode)tree.at("/structure");
					fixStereoOnStructure(structure);
					for(JsonNode moiety: tree.at("/moieties")){
						fixStereoOnStructure((ObjectNode)moiety);
					}
					
					sub = mapper.treeToValue(tree, ChemicalSubstance.class);
					

					try {
						((ChemicalSubstance) sub).structure.smiles = ChemicalFactory.DEFAULT_CHEMICAL_FACTORY()
								.createChemical(((ChemicalSubstance) sub).structure.molfile, Chemical.FORMAT_MOL)
								.export(Chemical.FORMAT_SMILES);
					} catch (Exception e) {

					}

					return sub;
				case protein:
					sub = mapper.treeToValue(tree, ProteinSubstance.class);
					return sub;
				case mixture:
					sub = mapper.treeToValue(tree, MixtureSubstance.class);
					return sub;
				case nucleicAcid:
					sub = mapper.treeToValue(tree, NucleicAcidSubstance.class);
					return sub;
				case polymer:
					sub = mapper.treeToValue(tree, PolymerSubstance.class);
					return sub;
				case structurallyDiverse:
					sub = mapper.treeToValue(tree, StructurallyDiverseSubstance.class);
					return sub;
				case specifiedSubstanceG1:
					sub = mapper.treeToValue(tree, SpecifiedSubstanceGroup1Substance.class);
					return sub;
				case concept:
					sub = mapper.treeToValue(tree, Substance.class);
					return sub;
				default:
					throw new IllegalStateException(
							"JSON parse error: Unimplemented substance class:\"" + subclass.asText() + "\"");
				}
			} catch (JsonProcessingException e) {
				e.printStackTrace();
				throw new IllegalStateException("JSON parse error:" + e.getMessage());
			}
		} else {
			throw new IllegalStateException("Not a valid JSON substance! \"substanceClass\" cannot be null!");
		}
	}
	
	public static void fixStereoOnStructure(ObjectNode structure){
		JsonNode jsn=structure.at("/stereochemistry");
		try{
			Structure.Stereo str=Structure.Stereo.valueOf(jsn.asText());
		}catch(Exception e){
			//e.printStackTrace();
			//System.out.println("Unknown stereo:'" + jsn.asText() + "'");
			if(!jsn.asText().equals("")){
				//System.out.println("Is not nothin");
				String newStereo=jsn.toString();
				JsonNode oldnode=structure.get("stereocomments");
				
				if(oldnode!=null && !oldnode.isNull() && !oldnode.isMissingNode() &&
						!oldnode.toString().equals("")){
					newStereo+=";" +oldnode.toString();
				}
				structure.put("stereocomments",newStereo);
				structure.put("atropisomerism", "Yes");
				
			}
			structure.put("stereochemistry", "UNKNOWN");
		}
	}
	
	public static boolean persistSubstances(Collection<Substance> subs){
		Transaction tx = Ebean.beginTransaction();
		try{
			for(Substance s: subs){
				s.save();
			}
			tx.commit();
			return true;
		}catch(Exception ex){
			return false;
		}finally{
			tx.end();
		}
	}
	

	public static boolean persistSubstance(Substance theRecordToPersist, List<String> errors) {
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

	public static List<Substance> toPersist = new ArrayList<Substance>();
	

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
			boolean worked = false;
			List<String> errors = new ArrayList<String>();
			if (prec.theRecordToPersist != null) {
				worked = GinasUtils.persistSubstance(prec.theRecordToPersist, errors);
				if (worked) {
					prec.rec.status = ProcessingRecord.Status.OK;
					prec.rec.xref = new XRef(prec.theRecordToPersist);
					prec.rec.xref.save();
				} else {
					prec.rec.message = errors.get(0);
					prec.rec.status = ProcessingRecord.Status.FAILED;
				}
				prec.rec.stop = System.currentTimeMillis();
			}
			prec.rec.save();

			Logger.debug("Saved substance " + (prec.theRecordToPersist != null ? prec.theRecordToPersist.getUuid() : null)
					+ " record " + prec.rec.id);
			if (!worked)
				throw new IllegalStateException(prec.rec.message);
		}
		public void persistb(TransformedRecord<Substance, Substance> prec) throws Exception {
			
			if (prec.theRecordToPersist != null) {
				toPersist.add(prec.theRecordToPersist);
				if(toPersist.size()>=20){
					persistSubstances(toPersist);
					toPersist.clear();
				}
				
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

				return mapper.readTree(toks[2]);
			} catch (Exception e) {
				e.printStackTrace();
				throw e;
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
		boolean validation=Play.application().configuration().getBoolean("ix.ginas.batch.validation", true);
		
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
			
			if(validation){
				return new GinasSubstanceTransformer(DefaultSubstanceValidator.BATCH_SUBSTANCE_VALIDATOR(DEFAULT_BATCH_STRATEGY));
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
			setValidator(DefaultSubstanceValidator.BATCH_SUBSTANCE_VALIDATOR(DEFAULT_BATCH_STRATEGY));
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

	
}