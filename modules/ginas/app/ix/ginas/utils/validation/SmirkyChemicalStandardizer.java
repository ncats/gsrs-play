package ix.ginas.utils.validation;

import javax.transaction.NotSupportedException;

import ix.core.models.Structure;

public class SmirkyChemicalStandardizer implements ChemicalStandardizer{

    @Override
    public Structure standardize(Structure s) {
       throw new RuntimeException(new NotSupportedException("Smirks-based standardizer not yet implemented"));
    }

}
