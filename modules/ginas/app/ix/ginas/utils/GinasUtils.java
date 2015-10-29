package ix.ginas.utils;

import gov.nih.ncgc.chemical.Chemical;
import gov.nih.ncgc.chemical.ChemicalFactory;
import gov.nih.ncgc.jchemical.JchemicalReader;
import ix.core.chem.Chem;
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
import ix.ginas.models.v1.Moiety;
import ix.ginas.models.v1.Name;
import ix.ginas.models.v1.PolymerSubstance;
import ix.ginas.models.v1.Protein;
import ix.ginas.models.v1.ProteinSubstance;
import ix.ginas.models.v1.SpecifiedSubstanceGroup1Substance;
import ix.ginas.models.v1.StructurallyDiverseSubstance;
import ix.ginas.models.v1.Substance;
import ix.ginas.models.v1.Subunit;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import ix.seqaln.SequenceIndexer;
import play.Logger;
import play.Play;
import tripod.chem.indexer.StructureIndexer;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.Transaction;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class GinasUtils {
    public static ChemicalFactory DEFAULT_FACTORY = new JchemicalReader();
    public static String NULL_MOLFILE = "\n\n\n  0  0  0     0  0            999 V2000\nM  END\n\n$$$$";
        
    public static final SequenceIndexer _seqIndexer = Play.application()
        .plugin(SequenceIndexerPlugin.class).getIndexer();

    public static Chemical substanceToChemical(Substance s, List<GinasProcessingMessage> messages){
        Chemical c;
        if(s instanceof ChemicalSubstance){
            c=structureToChemical(((ChemicalSubstance) s).structure, messages);
        }else if(s instanceof PolymerSubstance){
            messages.add(GinasProcessingMessage.WARNING_MESSAGE("Polymer substance structure is for display, and is not complete in definition"));
            c=structureToChemical(((PolymerSubstance) s).polymer.displayStructure, messages);
        }else{
            c=DEFAULT_FACTORY.createChemical(NULL_MOLFILE, Chemical.FORMAT_SDF);
            messages.add(GinasProcessingMessage.WARNING_MESSAGE("Structure is non-chemical. Structure format is largely meaningless."));
        }
        c.setProperty("APPROVAL_ID", s.approvalID);
        c.setProperty("NAME", s.getName());
        c.setName(s.getName());
        StringBuilder sb = new StringBuilder();
                
        for(Name n:s.getOfficialNames()){
            String name=n.name;
            sb.append(name + "\n");
                        
            for(String loc:n.getLocators(s)){
                sb.append(name + " [" + loc + "]\n");
            }
                        
        }
        if(sb.length()>0){
            c.setProperty("OFFICIAL_NAMES", sb.toString());
        }
        //clear builder
        sb.setLength(0);
        for(Name n:s.getNonOfficialNames()){
            String name=n.name;
            sb.append(name + "\n");
                        
            for(String loc:n.getLocators(s)){
                sb.append(name + " [" + loc + "]\n");
            }
        }
        if(sb.length()>0){
            c.setProperty("OTHER_NAMES", sb.toString());
        }
        //clear builder
        sb.setLength(0);
                
        for(Code cd:s.codes){
            String codesset = c.getProperty(cd.codeSystem);
            if(codesset == null || codesset.trim().equals("")){
                codesset="";
            }else{
                codesset = codesset + "\n";
            }
            codesset+=cd.code;
            if(!cd.type.equals("PRIMARY")){
                codesset+=" [" + cd.type + "]";
            }
            c.setProperty(cd.codeSystem, codesset);
        }
        for(GinasProcessingMessage gpm : messages){
            String codesset = c.getProperty("EXPORT-WARNINGS");
            if(codesset == null || codesset.trim().equals("")){
                codesset="";
            }else{
                codesset = codesset + "\n";
            }
            codesset+=gpm.message;
            c.setProperty("EXPORT-WARNINGS", codesset);
        }
        return c;
    }
        
    public static Chemical structureToChemical
        (Structure s,  List<GinasProcessingMessage> messages){
        Chemical c;
        String mfile =s.molfile;
        c = DEFAULT_FACTORY.createChemical(mfile, Chemical.FORMAT_SDF);
        if(s.stereoChemistry!=null)
            c.setProperty("STEREOCHEMISTRY", s.stereoChemistry.toString());
        if(s.opticalActivity!=null)
            c.setProperty("OPTICAL_ACTIVITY", s.opticalActivity.toString());
        if(s.stereoComments!=null)
            c.setProperty("STEREOCHEMISTRY_COMMENTS", s.stereoComments);
        if(s.stereoChemistry!=null){
            switch(s.stereoChemistry){
            case ABSOLUTE:
            case ACHIRAL:
                break;
            case EPIMERIC:
            case MIXED:
            case RACEMIC:                       
            case UNKNOWN:
                messages.add(GinasProcessingMessage.WARNING_MESSAGE("Structure format may not encode full stereochemical information"));
                break;
            default:
                break;          
            }
        }
        if(s.smiles!=null)
            c.setProperty("SMILES", s.smiles);
        return c;
                
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

    public static Substance makeSubstance(JsonNode tree) {
        return makeSubstance(tree, null);
    }

    public static Substance makeSubstance
        (JsonNode tree,
         List<GinasProcessingMessage> messages) {
        
        JsonNode subclass = tree.get("substanceClass");
        ObjectMapper mapper = new ObjectMapper();

        mapper.addHandler(new GinasV1ProblemHandler(messages));
        Substance sub = null;
        if (subclass != null && !subclass.isNull()) {

            Substance.SubstanceClass type;
            try {
                type = Substance.SubstanceClass.valueOf(subclass.asText());
            } catch (Exception e) {
                throw new IllegalStateException
                    ("Unimplemented substance class:" + subclass.asText());
            }
            try {
                switch (type) {
                case chemical:
                    sub = mapper.treeToValue(tree, ChemicalSubstance.class);
					try {
						((ChemicalSubstance) sub).structure.smiles = ChemicalFactory
								.DEFAULT_CHEMICAL_FACTORY()
								.createChemical(
										((ChemicalSubstance) sub).structure.molfile,
										Chemical.FORMAT_MOL)
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
                case polymer:
                    sub = mapper.treeToValue(tree, PolymerSubstance.class);
                    return sub;
                case structurallyDiverse:
                    sub = mapper.treeToValue(tree,
                                             StructurallyDiverseSubstance.class);
                    return sub;
                case specifiedSubstanceG1:
                    sub = mapper.treeToValue(tree,
                                             SpecifiedSubstanceGroup1Substance.class);
                    return sub;
                case concept:
                    sub = mapper.treeToValue(tree, Substance.class);
                    return sub;
                default:
                    throw new IllegalStateException(
                                                    "JSON parse error: Unimplemented substance class:\""
                                                    + subclass.asText() + "\"");
                }
            } catch (JsonProcessingException e) {
                throw new IllegalStateException("JSON parse error:"
                                                + e.getMessage());
            }
        } else {
            throw new IllegalStateException(
                                            "Not a valid JSON substance! \"substanceClass\" cannot be null!");
        }
    }

    public static boolean persistSubstance(Substance theRecordToPersist,
                                           StructureIndexer index,
                                           List<String> errors) {
        boolean worked = false;
        Transaction tx = Ebean.beginTransaction();
        try {
            if (theRecordToPersist instanceof ChemicalSubstance) {
                persist((ChemicalSubstance) theRecordToPersist, index);
            } else if (theRecordToPersist instanceof ProteinSubstance) {
                Protein protein =
                    ((ProteinSubstance) theRecordToPersist).protein;
                for (Subunit su : protein.subunits) {
                    if (su.sequence != null && su.sequence.length() > 0) {
                        su.save();
                        _seqIndexer.add(su.uuid.toString(), su.sequence);
                    }
                }
            }
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

    public static boolean persistSubstance(Substance theRecordToPersist,
                                           StructureIndexer index) {
        return persistSubstance(theRecordToPersist, index);
    }

    static Substance persist(ChemicalSubstance chem, StructureIndexer index)
        throws Exception {
        // now index the structure for searching
        try {
            Chem.setFormula(chem.structure);
            chem.structure.save();
            index.add(String.valueOf(chem.structure.id), chem.structure.molfile);
        } catch (IOException e) {
            e.printStackTrace();
            throw e;
        }
        for (Moiety m : chem.moieties)
            m.structure.save();
        return chem;
    }

    /*********************************************
     * Ginas bits for 1. extracting from InputStream 2. transforming to
     * Substance 3. persisting
     * 
     * @author peryeata
     *
     */
    public static class GinasSubstancePersister extends
                                                    RecordPersister<Substance, Substance> {
        @Override
        public void persist(TransformedRecord<Substance, Substance> prec)
            throws Exception {
            boolean worked = false;
            List<String> errors = new ArrayList<String>();
            if (prec.theRecordToPersist != null) {

                worked = GinasUtils.persistSubstance(prec.theRecordToPersist,
                                                     prec.indexer, errors);
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

            Logger.debug("Saved substance "
                         + (prec.theRecordToPersist != null ? prec.theRecordToPersist.uuid
                            : null) + " record " + prec.rec.id);
            if (!worked)
                throw new IllegalStateException(prec.rec.message);
        }
    }

    public static class GinasSubstanceTransformer
        extends  GinasAbstractSubstanceTransformer<JsonNode> {

        @Override
        public String getName(JsonNode theRecord) {
            return theRecord.get("name").asText();
        }

        @Override
        public Substance transformSubstance(JsonNode rec) throws Throwable {
            return GinasUtils.makeSubstance(rec);
        }
    }

    public static class GinasDumpExtractor extends RecordExtractor<JsonNode> {
        BufferedReader buff;

        public GinasDumpExtractor(InputStream is) {
            super(is);
            // Logger.debug("I'm going to make a nice reader for everyone to use!");
            try {
                buff = new BufferedReader(new InputStreamReader(is));
                // Logger.debug("####################################### Making reader ");
            } catch (Exception e) {
                // Logger.debug("Pfft ... just kidding, I hate readers anyway.");
            }

        }

        @Override
        public JsonNode getNextRecord() {
            if (buff == null)
                return null;
            try {
                String line = buff.readLine();
                if (line == null)
                    return null;
                String[] toks = line.split("\t");
                // Logger.debug("extracting:"+ toks[1]);
                ByteArrayInputStream bis = new ByteArrayInputStream
                    (toks[2].getBytes("utf8"));
                ObjectMapper mapper = new ObjectMapper();
                JsonNode tree = mapper.readTree(bis);
                return tree;
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
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

        @Override
        public RecordTransformer getTransformer() {
            return new GinasSubstanceTransformer();
        }

    }

    public static class GinasJSONExtractor extends RecordExtractor<JsonNode> {
        public GinasJSONExtractor(InputStream is) {
            super(is);
        }

        public GinasJSONExtractor(String s) throws UnsupportedEncodingException {
            super(new ByteArrayInputStream(s.getBytes("utf8")));
        }

        @Override
        public JsonNode getNextRecord() {
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
            }
            return null;
        }

        @Override
        public void close() {
        }

        @Override
        public RecordExtractor<JsonNode> makeNewExtractor(InputStream is) {
            return new GinasJSONExtractor(is);
        }

        @Override
        public RecordTransformer getTransformer() {
            return new GinasSubstanceTransformer();
        }

    }

    public abstract static class GinasAbstractSubstanceTransformer<K> extends
                                                                          RecordTransformer<K, Substance> {
        public static GinasProcessingStrategy DEFAULT_STRAT = GinasProcessingStrategy
            .ACCEPT_APPLY_ALL_WARNINGS_MARK_FAILED();

        @Override
        public Substance transform(PayloadExtractedRecord<K> pr,
                                   ProcessingRecord rec) {

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
                prepareSubstance(DEFAULT_STRAT, sub);
                rec.status = ProcessingRecord.Status.ADAPTED;
            } catch (Throwable t) {
                rec.stop = System.currentTimeMillis();
                rec.status = ProcessingRecord.Status.FAILED;
                rec.message = t.getMessage();
                Logger.error(t.getMessage());
                return null;
            }
            return sub;
        }

        public static List<GinasProcessingMessage> prepareSubstance
            (GinasProcessingStrategy prc, Substance sub) throws Exception {
            List<GinasProcessingMessage> valid =
                Validation.validateAndPrepare(sub, prc);
            prc.handleMessages(sub, valid);
            prc.addWarnings(sub, valid);
            return valid;
        }

        public static void prepareSubstance(Substance sub) throws Exception {
            prepareSubstance(DEFAULT_STRAT, sub);
        }

        public abstract String getName(K theRecord);

        public abstract Substance transformSubstance(K rec) throws Throwable;
    }
}
