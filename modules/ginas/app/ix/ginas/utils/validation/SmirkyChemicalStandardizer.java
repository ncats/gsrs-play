package ix.ginas.utils.validation;


import ix.core.models.Structure;

public class SmirkyChemicalStandardizer implements ChemicalStandardizer{

    @Override
    public Structure standardize(Structure s) {
       throw new RuntimeException("Smirks-based standardizer not yet implemented");
    }

}
