package ix.core.chem;

import java.util.function.BiConsumer;

import chemaxon.formats.MolImporter;
import chemaxon.struc.Molecule;
import gov.nih.ncats.molwitch.Chemical;
import ix.core.models.Structure;
import lychi.LyChIStandardizer;

public class LychiStructureHasher implements StructureHasher{

    //TODO: This is a little weird. Ideally, the hash generator would do any standardization _and_ also generate a hash.
    // but this one actually requires that standardization already happen. This sort of defeats the purpose of abstracting the
    // hash function. We should have this do any required standardization if needed first.
    @Override
    public void hash(Chemical chem, String mol, BiConsumer<String, String> keyValueConsumer) {


        String[] hash;
        Molecule stdmol;
        try {
            stdmol = MolImporter.importMol(mol);
            hash = LyChIStandardizer.hashKeyArray(stdmol);
        } catch (Exception e) {
            throw new IllegalArgumentException("could not generate lychi for", e);
        }

        keyValueConsumer.accept(Structure.H_LyChI_L1, hash[0]);
        keyValueConsumer.accept (Structure.H_LyChI_L2, hash[1]);
        keyValueConsumer.accept (Structure.H_LyChI_L3, hash[2]);
        keyValueConsumer.accept (Structure.H_LyChI_L4, hash[3]);

        keyValueConsumer.accept (Structure.H_EXACT_HASH, hash[3]);
        keyValueConsumer.accept (Structure.H_STEREO_INSENSITIVE_HASH, hash[2]);

        //lychi smiles is written by the standardizer
//        keyValueConsumer.accept(Structure.F_LyChI_SMILES, ChemUtil.canonicalSMILES(stdmol));
    }
}
