package ix.core.chem;

import gov.nih.ncats.molwitch.Atom;
import gov.nih.ncats.molwitch.Chemical;
import gov.nih.ncats.molwitch.inchi.Inchi;
import gov.nih.ncats.molwitch.io.ChemFormat;
import ix.core.models.Structure;
import ix.core.models.Text;
import ix.core.models.Value;
import ix.core.util.CachedSupplier;
import ix.core.util.ConfigHelper;

import java.io.IOException;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class InchiStandardizer implements StructureStandardizer{
    private static final int ATOM_LIMIT_FOR_STANDARDIZATION = 240;
    private static final CachedSupplier<Integer> ATOM_LIMIT_SUPPLIER = ConfigHelper.supplierOf("ix.core.structureIndex.atomLimit",ATOM_LIMIT_FOR_STANDARDIZATION);

    private static final ChemFormat.SmilesFormatWriterSpecification CANONICAL_SMILES_SPEC = new ChemFormat.SmilesFormatWriterSpecification()
                                                                                                    .setCanonization(ChemFormat.SmilesFormatWriterSpecification.CanonicalizationEncoding.CANONICAL);
    @Override
    public String canonicalSmiles(Structure s, String mol) {
        String smiles=null;
        for(Value v : s.properties){
            if(Structure.F_SMILES.equals(v.label)){
                smiles = (String) v.getValue();
                break;
            }
        }
        if(smiles !=null){
            return smiles;
        }
        try {
            return s.toChemical().toSmiles(CANONICAL_SMILES_SPEC);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public Chemical standardize(Chemical orig, Supplier<String> molSupplier, Consumer<Value> valueConsumer) throws IOException {
        int maxAtoms = ATOM_LIMIT_SUPPLIER.get();

        if(orig.getAtomCount() > maxAtoms || orig.hasPseudoAtoms()){
            return orig;
        }
        try {
            String inchi = orig.toInchi().getInchi();
            Chemical chem= Inchi.toChemical(inchi);
            valueConsumer.accept(
                    (new Text(Structure.F_SMILES,chem.toSmiles(CANONICAL_SMILES_SPEC)
                           )));
            return chem;
        }catch(Exception e){
            e.printStackTrace();
            return orig;
        }
    }


}
