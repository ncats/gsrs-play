package ix.ginas.utils.validation.validators;

import ix.core.validator.GinasProcessingMessage;
import ix.core.models.Payload;
import ix.core.plugins.PayloadPlugin;
import ix.core.util.CachedSupplier;
import ix.core.validator.ValidatorCallback;
import ix.ginas.models.v1.*;
import ix.ginas.utils.GinasUtils;
import ix.ginas.utils.ProteinUtils;
import play.Play;
import play.mvc.Call;

import java.util.*;

/**
 * Created by katzelda on 5/14/18.
 */
public class ProteinValidator extends AbstractValidatorPlugin<Substance> {
    private CachedSupplier<PayloadPlugin> _payload = CachedSupplier.of(()-> Play.application().plugin(PayloadPlugin.class));

    @Override
    public void validate(Substance objnew, Substance objold, ValidatorCallback callback) {

        ProteinSubstance cs = (ProteinSubstance) objnew;
        
        List<GinasProcessingMessage> gpm = new ArrayList<GinasProcessingMessage>();
        if (cs.protein == null) {
            callback.addMessage(GinasProcessingMessage
                    .ERROR_MESSAGE("Protein substance must have a protein element"));
        } else {
            if(cs.protein.subunits.isEmpty() ){
                if(Substance.SubstanceDefinitionLevel.INCOMPLETE.equals(cs.definitionLevel)){
                    callback.addMessage(GinasProcessingMessage.WARNING_MESSAGE("Having no subunits is allowed but discouraged for incomplete protein records."));
                }else{
                    callback.addMessage(GinasProcessingMessage.ERROR_MESSAGE("Complete protein substance must have at least one Subunit element. Please add a subunit, or mark as incomplete."));
                }
            }
            for (int i = 0; i < cs.protein.subunits.size(); i++) {
                Subunit su = cs.protein.subunits.get(i);
                if (su.subunitIndex == null) {
                    GinasProcessingMessage mes = GinasProcessingMessage
                            .WARNING_MESSAGE(
                                    "Protein subunit (at "
                                            + (i + 1)
                                            + " position) has no subunit index, defaulting to:"
                                            + (i + 1)).appliableChange(true);
                    Integer newValue = i +1;
                    callback.addMessage(mes, () -> su.subunitIndex = newValue);

                    }
                }
            }

            for (DisulfideLink l : cs.protein.getDisulfideLinks()) {

                List<Site> sites = l.getSites();
                if (sites.size() != 2) {
                    GinasProcessingMessage mes = GinasProcessingMessage
                            .ERROR_MESSAGE("Disulfide Link \""
                                    + sites.toString() + "\" has "
                                    + sites.size() + " sites, should have 2");
                    callback.addMessage(mes);
                } else {
                    for (Site s : sites) {
                        String res = cs.protein.getResidueAt(s);
                        if (res == null) {
                            GinasProcessingMessage mes = GinasProcessingMessage
                                    .ERROR_MESSAGE("Site \"" + s.toString()
                                            + "\" does not exist");
                            callback.addMessage(mes);
                        } else {
                            if (!res.equalsIgnoreCase("C")) {
                                GinasProcessingMessage mes = GinasProcessingMessage
                                        .ERROR_MESSAGE("Site \""
                                                + s.toString()
                                                + "\" in disulfide link is not a Cysteine, found: \""
                                                + res + "\"");
                                callback.addMessage(mes);
                            }
                        }
                    }
                }

            }

            Set<String> unknownRes = new HashSet<String>();
            double tot = ProteinUtils.generateProteinWeight(cs, unknownRes);
            if (unknownRes.size() > 0) {
                GinasProcessingMessage mes = GinasProcessingMessage
                        .WARNING_MESSAGE("Protein has unknown amino acid residues: "
                                + unknownRes.toString());
                callback.addMessage(mes);
            }

            List<Property> molprops = ProteinUtils.getMolWeightProperties(cs);
            if (molprops.size() <= 0) {

                GinasProcessingMessage mes = GinasProcessingMessage
                        .WARNING_MESSAGE(
                                "Protein has no molecular weight, defaulting to calculated value of: "
                                        + String.format("%.2f", tot)).appliableChange(true);
                callback.addMessage(mes, ()-> {

                    cs.properties.add(ProteinUtils.makeMolWeightProperty(tot));
                    if (!unknownRes.isEmpty()) {
                        GinasProcessingMessage mes2 = GinasProcessingMessage
                                .WARNING_MESSAGE("Calculated protein weight questionable, due to unknown amino acid residues: "
                                        + unknownRes.toString());
                        callback.addMessage(mes2);
                    }
                });

            } else {
                for(Property p :molprops){
                    if (p.getValue() != null) {
                        Double avg=p.getValue().average;
                        if(avg==null)continue;
                        double delta = tot - avg;
                        double pdiff = delta / (avg);

                        int len = 0;
                        for (Subunit su : cs.protein.subunits) {
                            len += su.sequence.length();
                        }
                        double avgoff = delta / len;
                        // System.out.println("Diff:" + pdiff + "\t" + avgoff);
                        if (Math.abs(pdiff) > .05) {
                            callback.addMessage(GinasProcessingMessage
                                    .WARNING_MESSAGE(
                                            "Calculated weight ["
                                                    + String.format("%.2f", tot)
                                                    + "] is greater than 5% off of given weight ["
                                                    + String.format("%.2f", p.getValue().average) + "]"));
                            //katzelda May 2018 - turn off appliable change since there isn't anything to change it to.
//                                    .appliableChange(true));
                        }
                        //if it gets this far, break out of the properties
                        break;
                    }
                }
            }
            // System.out.println("calc:" + tot);

        boolean sequenceHasChanged = sequenceHasChanged(cs, objold);
//		System.out.println("SEQUENCE HAS CHANGED ?? " + cs.approvalID + "  " + old.approvalID + " ? " + sequenceHasChanged);

        if(sequenceHasChanged) {
            validateSequenceDuplicates(cs, callback);
        }
    }

    private static boolean sequenceHasChanged(ProteinSubstance cs, Substance o) {
        if(o ==null || !(o instanceof ProteinSubstance)){
            return true;
        }
        ProteinSubstance old = (ProteinSubstance) o;
        Protein newProtein = cs.protein;
        Protein oldProtein = old.protein;

        if(oldProtein ==null){
//			System.out.println("old protein is null");
            return newProtein !=null;
        }
        List<Subunit> newSubs = newProtein.getSubunits();
        List<Subunit> oldSubs = oldProtein.getSubunits();
        if(newSubs.size() != oldSubs.size()){
//			System.out.println("subunit size differs " + newSubs.size() + " " + oldSubs.size());
//			System.out.println(newSubs);
//			System.out.println(oldSubs);
            return true;
        }
        int size = newSubs.size();
        for(int i=0; i< size; i++){
            Subunit newSub = newSubs.get(i);
            Subunit oldSub = oldSubs.get(i);

            //handles null
            if(!Objects.equals(newSub.sequence, oldSub.sequence)){
                return true;
            }
        }
        return false;
    }

    private void validateSequenceDuplicates(
            ProteinSubstance proteinsubstance, ValidatorCallback callback) {

        try {
            for (Subunit su : proteinsubstance.protein.subunits) {
                Payload payload = _payload.get().createPayload("Sequence Search",
                        "text/plain", su.sequence);
                List<Substance> sr = ix.ginas.controllers.v1.SubstanceFactory
                        .getNearCollsionProteinSubstancesToSubunit(10, 0, su);
                if (sr != null && !sr.isEmpty()) {
                    int dupes = 0;
                    GinasProcessingMessage mes = null;
                    for (Substance s : sr) {
                        if (proteinsubstance.getUuid() == null
                                || !s.getUuid()
                                .toString()
                                .equals(proteinsubstance.getUuid()
                                        .toString())) {

                            if (dupes <= 0) {
                                mes = GinasProcessingMessage
                                        .WARNING_MESSAGE("There is 1 substance with a similar sequence to subunit ["
                                                + su.subunitIndex + "]:");
                                GinasProcessingMessage.Link l = new GinasProcessingMessage.Link();
                                Call call = ix.ginas.controllers.routes.GinasApp
                                        .substances(payload.id.toString(), 16,1);
                                l.href = call.url() + "&type=sequence";
                                l.text = "Perform similarity search on subunit ["
                                        + su.subunitIndex + "]";

                                mes.addLink(l);
                            }
                            dupes++;
                            mes.addLink(GinasUtils.createSubstanceLink(s));
                        }
                    }
                    if(dupes > 0) {
                        if(dupes > 1){
                            mes.message = "There are "
                                    + dupes
                                    + " substances with a similar sequence to subunit ["
                                    + su.subunitIndex + "]:";
                        }
                        callback.addMessage(mes);
                    }
                }
            }
        } catch (Exception e) {
            callback.addMessage(GinasProcessingMessage
                    .ERROR_MESSAGE("Error performing seqeunce search on protein:"
                            + e.getMessage()));
        }
    }

}
