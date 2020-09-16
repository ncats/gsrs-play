package ix.ginas.initializers;

import gov.nih.ncats.molwitch.Chemical;
import gov.nih.ncats.molwitch.ChemicalBuilder;
import gov.nih.ncats.molwitch.io.ChemicalReader;
import gov.nih.ncats.molwitch.io.ChemicalReaderFactory;
import gov.nih.ncats.molwitch.io.StandardChemFormats;
import ix.core.initializers.Initializer;
import ix.core.java8Util.Java8ForOldEbeanHelper;
import ix.ginas.controllers.v1.ControlledVocabularyFactory;
import play.Application;

import java.io.ByteArrayInputStream;
import java.io.IOException;

/**
 * This Initializer is just a way to force Chemkit to be loaded
 * by the Classloader early.  Otherwise sometimes when doing a ginas/run
 * a delayed call to Chemkit might get loaded by a plugin classloader
 * and will cause problems when other classes can't see it.
 *
 * Created by katzelda on 3/20/19.
 */
public class ChemkitInitializer implements Initializer{

    @Override
    public void onStart(Application app) {
        try {
            Java8ForOldEbeanHelper.makeChemkitCall();
        }catch(Exception e){
            throw new IllegalStateException("chemkit could not be found", e);
        }

        String sdf =
                "1\n" +
                        "  -OEChem-01191613192D\n" +
                        "\n" +
                        " 31 30  0     1  0  0  0  0  0999 V2000\n" +
                        "    2.8660    0.7500    0.0000 O   0  0  0  0  0  0  0  0  0  0  0  0\n" +
                        "    2.8660   -2.2500    0.0000 O   0  5  0  0  0  0  0  0  0  0  0  0\n" +
                        "    2.0000   -0.7500    0.0000 O   0  0  0  0  0  0  0  0  0  0  0  0\n" +
                        "    3.7320    2.2500    0.0000 O   0  0  0  0  0  0  0  0  0  0  0  0\n" +
                        "    5.4641    0.2500    0.0000 N   0  3  0  0  0  0  0  0  0  0  0  0\n" +
                        "    4.5981    0.7500    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n" +
                        "    3.7320    0.2500    0.0000 C   0  0  3  0  0  0  0  0  0  0  0  0\n" +
                        "    6.3301   -0.2500    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n" +
                        "    5.9641    1.1160    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n" +
                        "    4.9641   -0.6160    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n" +
                        "    3.7320   -0.7500    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n" +
                        "    2.8660   -1.2500    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n" +
                        "    2.8660    1.7500    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n" +
                        "    2.0000    2.2500    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n" +
                        "    4.9966    1.2250    0.0000 H   0  0  0  0  0  0  0  0  0  0  0  0\n" +
                        "    4.1996    1.2250    0.0000 H   0  0  0  0  0  0  0  0  0  0  0  0\n" +
                        "    3.7320    0.8700    0.0000 H   0  0  0  0  0  0  0  0  0  0  0  0\n" +
                        "    6.0201   -0.7869    0.0000 H   0  0  0  0  0  0  0  0  0  0  0  0\n" +
                        "    6.8671   -0.5600    0.0000 H   0  0  0  0  0  0  0  0  0  0  0  0\n" +
                        "    6.6401    0.2869    0.0000 H   0  0  0  0  0  0  0  0  0  0  0  0\n" +
                        "    6.5010    0.8060    0.0000 H   0  0  0  0  0  0  0  0  0  0  0  0\n" +
                        "    6.2741    1.6530    0.0000 H   0  0  0  0  0  0  0  0  0  0  0  0\n" +
                        "    5.4272    1.4260    0.0000 H   0  0  0  0  0  0  0  0  0  0  0  0\n" +
                        "    4.4272   -0.3060    0.0000 H   0  0  0  0  0  0  0  0  0  0  0  0\n" +
                        "    4.6541   -1.1530    0.0000 H   0  0  0  0  0  0  0  0  0  0  0  0\n" +
                        "    5.5010   -0.9260    0.0000 H   0  0  0  0  0  0  0  0  0  0  0  0\n" +
                        "    3.9441   -1.3326    0.0000 H   0  0  0  0  0  0  0  0  0  0  0  0\n" +
                        "    4.3426   -0.6423    0.0000 H   0  0  0  0  0  0  0  0  0  0  0  0\n" +
                        "    2.3100    2.7869    0.0000 H   0  0  0  0  0  0  0  0  0  0  0  0\n" +
                        "    1.4631    2.5600    0.0000 H   0  0  0  0  0  0  0  0  0  0  0  0\n" +
                        "    1.6900    1.7131    0.0000 H   0  0  0  0  0  0  0  0  0  0  0  0\n" +
                        "  1  7  1  0  0  0  0\n" +
                        "  1 13  1  0  0  0  0\n" +
                        "  2 12  1  0  0  0  0\n" +
                        "  3 12  2  0  0  0  0\n" +
                        "  4 13  2  0  0  0  0\n" +
                        "  5  6  1  0  0  0  0\n" +
                        "  5  8  1  0  0  0  0\n" +
                        "  5  9  1  0  0  0  0\n" +
                        "  5 10  1  0  0  0  0\n" +
                        "  6  7  1  0  0  0  0\n" +
                        "  6 15  1  0  0  0  0\n" +
                        "  6 16  1  0  0  0  0\n" +
                        "  7 11  1  0  0  0  0\n" +
                        "  7 17  1  0  0  0  0\n" +
                        "  8 18  1  0  0  0  0\n" +
                        "  8 19  1  0  0  0  0\n" +
                        "  8 20  1  0  0  0  0\n" +
                        "  9 21  1  0  0  0  0\n" +
                        "  9 22  1  0  0  0  0\n" +
                        "  9 23  1  0  0  0  0\n" +
                        " 10 24  1  0  0  0  0\n" +
                        " 10 25  1  0  0  0  0\n" +
                        " 10 26  1  0  0  0  0\n" +
                        " 11 12  1  0  0  0  0\n" +
                        " 11 27  1  0  0  0  0\n" +
                        " 11 28  1  0  0  0  0\n" +
                        " 13 14  1  0  0  0  0\n" +
                        " 14 29  1  0  0  0  0\n" +
                        " 14 30  1  0  0  0  0\n" +
                        " 14 31  1  0  0  0  0\n" +
                        "M  CHG  2   2  -1   5   1\n" +
                        "M  END\n" +
                        "> <PUBCHEM_COMPOUND_CID>\n" +
                        "1\n" +
                        "\n" +
                        "> <PUBCHEM_COMPOUND_CANONICALIZED>\n" +
                        "1\n" +
                        "\n" +
                        "> <PUBCHEM_CACTVS_COMPLEXITY>\n" +
                        "214\n" +
                        "\n" +
                        "> <PUBCHEM_CACTVS_HBOND_ACCEPTOR>\n" +
                        "4\n" +
                        "\n" +
                        "> <PUBCHEM_CACTVS_HBOND_DONOR>\n" +
                        "0\n" +
                        "\n" +
                        "> <PUBCHEM_CACTVS_ROTATABLE_BOND>\n" +
                        "5\n" +
                        "\n" +
                        "> <PUBCHEM_CACTVS_SUBSKEYS>\n" +
                        "AAADceByOAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAHgAAAAAACBThgAYCCAMABAAIAACQ\n" +
                        "CAAAAAAAAAAAAAEIAAACABQAgAAHAAAFIAAQAAAkAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA==\n" +
                        "\n" +
                        "> <PUBCHEM_IUPAC_OPENEYE_NAME>\n" +
                        "3-acetoxy-4-(trimethylammonio)butanoate\n" +
                        "\n" +
                        "> <PUBCHEM_IUPAC_CAS_NAME>\n" +
                        "3-acetyloxy-4-(trimethylammonio)butanoate\n" +
                        "\n" +
                        "> <PUBCHEM_IUPAC_NAME>\n" +
                        "3-acetyloxy-4-(trimethylazaniumyl)butanoate\n" +
                        "\n" +
                        "> <PUBCHEM_IUPAC_SYSTEMATIC_NAME>\n" +
                        "3-acetyloxy-4-(trimethylazaniumyl)butanoate\n" +
                        "\n" +
                        "> <PUBCHEM_IUPAC_TRADITIONAL_NAME>\n" +
                        "3-acetoxy-4-(trimethylammonio)butyrate\n" +
                        "\n" +
                        "> <PUBCHEM_IUPAC_INCHI>\n" +
                        "InChI=1S/C9H17NO4/c1-7(11)14-8(5-9(12)13)6-10(2,3)4/h8H,5-6H2,1-4H3\n" +
                        "\n" +
                        "> <PUBCHEM_IUPAC_INCHIKEY>\n" +
                        "RDHQFKQIGNGIED-UHFFFAOYSA-N\n" +
                        "\n" +
                        "> <PUBCHEM_XLOGP3_AA>\n" +
                        "0.4\n" +
                        "\n" +
                        "> <PUBCHEM_EXACT_MASS>\n" +
                        "203.115758\n" +
                        "\n" +
                        "> <PUBCHEM_MOLECULAR_FORMULA>\n" +
                        "C9H17NO4\n" +
                        "\n" +
                        "> <PUBCHEM_MOLECULAR_WEIGHT>\n" +
                        "203.23558\n" +
                        "\n" +
                        "> <PUBCHEM_OPENEYE_CAN_SMILES>\n" +
                        "CC(=O)OC(CC(=O)[O-])C[N+](C)(C)C\n" +
                        "\n" +
                        "> <PUBCHEM_OPENEYE_ISO_SMILES>\n" +
                        "CC(=O)OC(CC(=O)[O-])C[N+](C)(C)C\n" +
                        "\n" +
                        "> <PUBCHEM_CACTVS_TPSA>\n" +
                        "66.4\n" +
                        "\n" +
                        "> <PUBCHEM_MONOISOTOPIC_WEIGHT>\n" +
                        "203.115758\n" +
                        "\n" +
                        "> <PUBCHEM_TOTAL_CHARGE>\n" +
                        "0\n" +
                        "\n" +
                        "> <PUBCHEM_HEAVY_ATOM_COUNT>\n" +
                        "14\n" +
                        "\n" +
                        "> <PUBCHEM_ATOM_DEF_STEREO_COUNT>\n" +
                        "0\n" +
                        "\n" +
                        "> <PUBCHEM_ATOM_UDEF_STEREO_COUNT>\n" +
                        "1\n" +
                        "\n" +
                        "> <PUBCHEM_BOND_DEF_STEREO_COUNT>\n" +
                        "0\n" +
                        "\n" +
                        "> <PUBCHEM_BOND_UDEF_STEREO_COUNT>\n" +
                        "0\n" +
                        "\n" +
                        "> <PUBCHEM_ISOTOPIC_ATOM_COUNT>\n" +
                        "0\n" +
                        "\n" +
                        "> <PUBCHEM_COMPONENT_COUNT>\n" +
                        "1\n" +
                        "\n" +
                        "> <PUBCHEM_CACTVS_TAUTO_COUNT>\n" +
                        "1\n" +
                        "\n" +
                        "> <PUBCHEM_COORDINATE_TYPE>\n" +
                        "1\n" +
                        "5\n" +
                        "255\n" +
                        "\n" +
                        "> <PUBCHEM_BONDANNOTATIONS>\n" +
                        "7  6  3\n" +
                        "\n" +
                        "$$$$";

        try(ChemicalReader reader = ChemicalReaderFactory.newReader(StandardChemFormats.SDF, new ByteArrayInputStream(sdf.getBytes()))){
            Chemical chemical = reader.read();
//            System.out.println("# atoms from sdf " + chemical.getAtomCount());
            String test=chemical.toMol();
            if(test.length()>99999){
            	System.out.println("This should never happen");
            }
        }catch(IOException e){
            e.printStackTrace();
        }
    }



}
