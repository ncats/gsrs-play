package ix.ginas.utils.validation.validators;

import gov.nih.ncgc.chemical.Chemical;
import gov.nih.ncgc.chemical.ChemicalFactory;
import ix.core.validator.ExceptionValidationMessage;
import ix.core.validator.GinasProcessingMessage;
import ix.core.chem.StructureProcessor;
import ix.core.models.Structure;
import ix.ginas.models.v1.ChemicalSubstance;
import ix.ginas.models.v1.GinasChemicalStructure;
import ix.ginas.models.v1.Moiety;
import ix.ginas.models.v1.Substance;
import ix.ginas.utils.ChemUtils;
import ix.ginas.utils.GinasUtils;
import ix.ginas.utils.validation.PeptideInterpreter;
import ix.ginas.utils.validation.ValidationUtils;
import ix.core.validator.ValidatorCallback;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by katzelda on 5/14/18.
 */
public class ChemicalValidator extends AbstractValidatorPlugin<Substance> {
    @Override
    public void validate(Substance s, Substance objold, ValidatorCallback callback) {

        ChemicalSubstance cs = (ChemicalSubstance)s;

        if (cs.structure == null) {
            callback.addMessage(GinasProcessingMessage.ERROR_MESSAGE("Chemical substance must have a chemical structure"));
            return;
        }
        String payload = cs.structure.molfile;
        if (payload != null) {




        try {
            ix.ginas.utils.validation.PeptideInterpreter.Protein p = PeptideInterpreter
                    .getAminoAcidSequence(cs.structure.molfile);
            if (p != null && !p.getSubunits().isEmpty()
                    && p.getSubunits().get(0).getSequence().length() > 2) {
                GinasProcessingMessage mes = GinasProcessingMessage
                        .WARNING_MESSAGE("Substance may be represented as protein as well. Sequence:["
                                + p.toString() + "]");
                callback.addMessage(mes);
            }
        } catch (Exception e) {

        }


            List<Moiety> moietiesForSub = new ArrayList<Moiety>();


            List<Structure> moieties = new ArrayList<Structure>();
            Structure struc = StructureProcessor.instrument(payload, moieties, true); // don't
            // standardize

            if(!payload.contains("M  END")){
                //not a mol convert it
                //struc is already standardized
                callback.addMessage(GinasProcessingMessage.WARNING_MESSAGE(
                        "structure should always be specified as mol file converting to format to mol automatically").appliableChange(true),
                        () -> {
                    try {
                        cs.structure = cs.structure.copy();
                        cs.structure.molfile = struc.molfile;
                    }catch(Exception e){
                        e.printStackTrace();
                        callback.addMessage(new ExceptionValidationMessage(e));
                    }

                } );

            }
            for (Structure m : moieties) {
                Moiety m2 = new Moiety();
                m2.structure = new GinasChemicalStructure(m);
                m2.setCount(m.count);
                moietiesForSub.add(m2);
            }

            if (cs.moieties == null
                    || cs.moieties.size() != moietiesForSub.size()) {

                GinasProcessingMessage mes = GinasProcessingMessage
                        .WARNING_MESSAGE("Incorrect number of moieties")
                        .appliableChange(true);
                callback.addMessage(mes, ()-> cs.moieties = moietiesForSub);


            } else {
                for (Moiety m : cs.moieties) {
                    Structure struc2 = StructureProcessor.instrument(
                            m.structure.molfile, null, true); // don't
                    // standardize

                    validateChemicalStructure(m.structure, struc2, callback);
                }
            }
            validateChemicalStructure(cs.structure, struc, callback);

            ChemUtils.fixChiralFlag(cs.structure, callback);
//            ChemUtils.checkChargeBalance(cs.structure, gpm);
            if (cs.structure.charge != 0) {
                GinasProcessingMessage mes = GinasProcessingMessage
                        .WARNING_MESSAGE("Structure is not charged balanced, net charge of: " + cs.structure.charge);
                callback.addMessage(mes);
            }

            ValidationUtils.validateReference(s,cs.structure, callback, ValidationUtils.ReferenceAction.FAIL);

            validateStructureDuplicates(cs, callback);
        } else {
            callback.addMessage(GinasProcessingMessage
                    .ERROR_MESSAGE("Chemical substance must have a valid chemical structure"));

        }

    }

    private static void validateStructureDuplicates(
            ChemicalSubstance cs, ValidatorCallback callback) {
        List<GinasProcessingMessage> gpm = new ArrayList<GinasProcessingMessage>();

        try {

            List<Substance> sr = ix.ginas.controllers.v1.SubstanceFactory
                    .getCollsionChemicalSubstances(100, 0, cs);

            if (sr != null && !sr.isEmpty()) {
                int dupes = 0;
                GinasProcessingMessage mes = null;
                for (Substance s : sr) {

                    if (cs.getUuid() == null
                            || !s.getUuid().toString()
                            .equals(cs.getUuid().toString())) {
                        if (dupes <= 0)
                            mes = GinasProcessingMessage.WARNING_MESSAGE("Structure has 1 possible duplicate:");
                        dupes++;
                        mes.addLink(GinasUtils.createSubstanceLink(s));
                    }
                }
                if (dupes > 0) {
                    if (dupes > 1)
                        mes.message = "Structure has " + dupes
                                + " possible duplicates:";
                    callback.addMessage(mes);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private static void validateChemicalStructure(
            GinasChemicalStructure oldstr, Structure newstr,
            ValidatorCallback callback) {
        List<GinasProcessingMessage> gpm = new ArrayList<GinasProcessingMessage>();

        String oldhash = null;
        String newhash = null;
        oldhash = oldstr.getLychiv4Hash();
        newhash = newstr.getLychiv4Hash();
        // Should always use the calculated pieces
        // TODO: Come back to this and allow for SOME things to be overloaded
        if (true || !newhash.equals(oldhash)) {
            GinasProcessingMessage mes = GinasProcessingMessage.INFO_MESSAGE(
                    "Given structure hash disagrees with computed")
                    .appliableChange(true);
            callback.addMessage(mes, ()->{
                    Structure struc2 = new GinasChemicalStructure(newstr);
                    oldstr.properties = struc2.properties;
                    oldstr.charge = struc2.charge;
                    oldstr.formula = struc2.formula;
                    oldstr.mwt = struc2.mwt;
                    oldstr.smiles = struc2.smiles;
                    oldstr.ezCenters = struc2.ezCenters;
                    oldstr.definedStereo = struc2.definedStereo;
                    oldstr.stereoCenters = struc2.stereoCenters;
                    oldstr.digest = struc2.digest;

            });
        }
        if (oldstr.digest == null) {
            oldstr.digest = newstr.digest;
        }
        if (oldstr.smiles == null) {
            oldstr.smiles = newstr.smiles;
        }
        if (oldstr.ezCenters == null) {
            oldstr.ezCenters = newstr.ezCenters;
        }
        if (oldstr.definedStereo == null) {
            oldstr.definedStereo = newstr.definedStereo;
        }
        if (oldstr.stereoCenters == null) {
            oldstr.stereoCenters = newstr.stereoCenters;
        }
        if (oldstr.mwt == null) {
            oldstr.mwt = newstr.mwt;
        }
        if (oldstr.formula == null) {
            oldstr.formula = newstr.formula;
        }
        if (oldstr.charge == null) {
            oldstr.charge = newstr.charge;
        }

        ChemUtils.checkValance(newstr, callback);

    }
}
