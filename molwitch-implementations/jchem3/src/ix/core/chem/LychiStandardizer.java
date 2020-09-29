package ix.core.chem;

import chemaxon.formats.MolFormatException;
import chemaxon.formats.MolImporter;
import chemaxon.struc.MolAtom;
import chemaxon.struc.Molecule;
import chemaxon.util.MolHandler;
import gov.nih.ncats.molwitch.Chemical;
import ix.core.models.Structure;
import ix.core.models.Text;
import ix.core.models.Value;
import ix.core.util.CachedSupplier;
import ix.core.util.ConfigHelper;
import lychi.LyChIStandardizer;
import lychi.util.ChemUtil;

import java.io.IOException;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Created by katzelda on 7/28/19.
 */
public class LychiStandardizer implements StructureStandardizer{
    private static final int ATOM_LIMIT_FOR_STANDARDIZATION = 240;

    @Override
    public Chemical standardize(Chemical orig, Supplier<String> molSupplier, Consumer<Value> valueConsumer) throws IOException{
        CachedSupplier<Integer> actualAtomLimitForStandardization = ConfigHelper.supplierOf("ix.core.structureIndex.atomLimit",ATOM_LIMIT_FOR_STANDARDIZATION);
        int maxAtoms = actualAtomLimitForStandardization.get();

        if(orig.getAtomCount() > maxAtoms){
            return orig;
        }


        LyChIStandardizer mstd = new LyChIStandardizer ();
        mstd.removeSaltOrSolvent(false);

        Molecule stdmol;

        //aug 2019 for some reason importing to mol and back gives better results than clone ?
        if(orig.getImpl().getWrappedObject() instanceof MolHandler){
            //we're using jchem probably faster to clone than write out to mol and reparse.
            stdmol = ((MolHandler) orig.getImpl().getWrappedObject()).getMolecule().cloneMolecule();
        }else {
            stdmol = MolImporter.importMol(molSupplier.get());
        }

        //Smiles should be generated from non-standardized structure
        String smi=  ChemUtil.canonicalSMILES(stdmol);
        valueConsumer.accept(
                (new Text(Structure.F_LyChI_SMILES,
                        smi)));

        try {
            mstd.standardize(handleQueryAtoms(stdmol));
        } catch (Exception e) {
            throw new IOException("error lychi standardizing mol for " + orig.getName(), e);
        }

        return Chemical.parseMol(stdmol.toFormat("mol"));
    }

    @Override
    public String canonicalSmiles(Structure s, String mol) {
        String smiles=null;
        for(Value v : s.properties){
            if(Structure.F_LyChI_SMILES.equals(v.label)){
                smiles = (String) v.getValue();
                break;
            }
        }
        if(smiles !=null){
            return smiles;
        }
        try {
            return ChemUtil.canonicalSMILES(handleQueryAtoms(MolImporter.importMol(mol)));
        } catch (MolFormatException e) {
            e.printStackTrace();
            return null;
        }
    }

    private Molecule handleQueryAtoms(Molecule m){
        //this is a copy so we can modify it
        for (MolAtom a : m.getAtomArray()) {
            switch (a.getAtno()) {
                case MolAtom.PSEUDO:
                case MolAtom.RGROUP:
                case MolAtom.SGROUP:
                case MolAtom.ANY:
                case MolAtom.NOTLIST:
                case MolAtom.LIST:
                case 114:
                    // this is what marvinjs specifies for atom *
                    a.setAtno(2); // force this to be helium
                    break;
                default:
                    // do nothing
            }
        }

        return m;
    }
}
