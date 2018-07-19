package ix.ginas.utils.validation.validators;

import ix.core.validator.GinasProcessingMessage;
import ix.core.validator.ValidatorCallback;
import ix.ginas.models.v1.NucleicAcidSubstance;
import ix.ginas.models.v1.Substance;
import ix.ginas.utils.NucleicAcidUtils;

/**
 * Created by katzelda on 5/14/18.
 */
public class NucleicAcidValidator extends AbstractValidatorPlugin<Substance> {
    @Override
    public void validate(Substance s, Substance objold, ValidatorCallback callback) {
        NucleicAcidSubstance cs = (NucleicAcidSubstance)s;
        
        if (cs.nucleicAcid == null) {
            callback.addMessage(GinasProcessingMessage
                    .ERROR_MESSAGE("Nucleic Acid substance must have a nucleicAcid element"));
        } else {
            if (cs.nucleicAcid.getSubunits() == null
                    || cs.nucleicAcid.getSubunits().isEmpty()) {
                callback.addMessage(GinasProcessingMessage
                        .ERROR_MESSAGE("Nucleic Acid substance must have at least 1 subunit"));
            }
            if (cs.nucleicAcid.getSugars() == null
                    || cs.nucleicAcid.getSugars().isEmpty()) {
                callback.addMessage(GinasProcessingMessage
                        .ERROR_MESSAGE("Nucleic Acid substance must have at least 1 specified sugar"));
            }
            if (cs.nucleicAcid.getLinkages() == null
                    || cs.nucleicAcid.getLinkages().isEmpty()) {
                callback.addMessage(GinasProcessingMessage
                        .ERROR_MESSAGE("Nucleic Acid substance must have at least 1 specified linkage"));
            }


            int unspSugars = NucleicAcidUtils
                    .getNumberOfUnspecifiedSugarSites(cs);
            if (unspSugars != 0) {
                callback.addMessage(GinasProcessingMessage
                        .ERROR_MESSAGE("Nucleic Acid substance must have every base specify a sugar fragment. Missing "
                                + unspSugars + " sites."));
            }


            int unspLinkages = NucleicAcidUtils
                    .getNumberOfUnspecifiedLinkageSites(cs);
            if (unspLinkages != 0) {
                callback.addMessage(GinasProcessingMessage
                        .ERROR_MESSAGE("Nucleic Acid substance must have every linkage specify a linkage fragment. Missing "
                                + unspLinkages + " sites."));
            }

        }
        
    }
}
