package ix.core.chem;

import java.util.*;
import java.net.URL;
import java.net.URI;
import java.net.URLDecoder;
import java.net.URLEncoder;

import java.util.logging.Level;
import java.util.logging.Logger;
import java.security.MessageDigest;

import chemaxon.struc.MolAtom;
import chemaxon.struc.MolBond;
import chemaxon.struc.Molecule;
import chemaxon.util.MolHandler;

import lychi.LyChIStandardizer;
import lychi.util.ChemUtil;

import ix.core.models.Keyword;
import ix.core.models.Text;
import ix.core.models.Value;
import ix.core.models.XRef;
import ix.core.models.BeanViews;
import ix.core.models.Structure;


public class StructureProcessor {
    static final Logger logger = 
        Logger.getLogger(StructureProcessor.class.getName());

    public static class PathFingerprint {
        // fingerprint parameters
        int size; // in bits
        int bits; // number of bits to turn on for each path
        int depth; // recursion depth
        int[] hash;

        PathFingerprint (int size, int bits, int depth) {
            this.size = size;
            this.bits = bits;
            this.depth = depth;
            hash = new int[(size+31)/32];
        }

        public PathFingerprint generate (Molecule mol) {
            MolHandler mh = new MolHandler (mol.cloneMolecule());
            mh.aromatize();
            int[] fp = mh.generateFingerprintInInts(hash.length, bits, depth);
            for (int i = 0; i < fp.length; ++i)
                hash[i] = fp[i];
            fp = null;
            return this;
        }
        
        public int getSize () { return size; }
        public int getBits () { return bits; }
        public int getDepth () { return depth; }
        public int[] getHash () { return hash; }
    }


    private StructureProcessor () {}

    public static Molecule flip (Molecule mol) {
        Molecule flip = mol.cloneMolecule();
        for (int i = 0; i < mol.getAtomCount(); ++i) {
            int chirality = mol.getChirality(i);
            if (MolAtom.CHIRALITY_R == chirality)
                flip.setChirality(i, MolAtom.CHIRALITY_S);
            else if (MolAtom.CHIRALITY_S == chirality)
                flip.setChirality(i, MolAtom.CHIRALITY_R);
        }
        return flip;
    }

    public static boolean isQuatAmine (MolAtom atom) {
        return atom.getAtno() == 7 && atom.getBondCount() == 4;
    }

    public static boolean isTetrahedral (MolAtom atom, int[] gi) {
        Molecule m = (Molecule)atom.getParent();
        // check for symmetry 
        Set<Integer> g = new HashSet<Integer>();
        for (int i = 0; i < atom.getBondCount(); ++i) {
            MolBond b = atom.getBond(i);
            if (b.getType() != 1)
                return false;

            MolAtom xa = b.getOtherAtom(atom);
            // any symmetry is false
            if (!g.add(gi[m.indexOf(xa)]))
                return false;
        }

        return g.size() > 2 
            && atom.getAtno() != 7 
            && atom.getAtno() != 15;
    }

    public static Structure instrument (String mol) {
        Structure struc = new Structure ();
        struc.digest = digest (mol);
        try {
            MolHandler mh = new MolHandler (mol);
            instrument (struc, mh.getMolecule());
        }
        catch (Exception ex) {
            throw new IllegalArgumentException (ex);
        }
        return struc;
    }
    
    public static Structure instrument (Molecule mol) {
        Structure struc = new Structure ();
        struc.digest = digest (mol);
        instrument (struc, mol);
        return struc;
    }
    
    static void instrument (Structure struc, Molecule mol) {
        if (mol.getDim() < 2) {
            mol.clean(2, null);
        }
        
        // no explicit Hs
        mol.hydrogenize(false);
        // make sure molecule is kekulized consistently
        mol.aromatize();
        mol.dearomatize();

        struc.molfile = mol.toFormat("mol");
        MolAtom[] atoms = mol.getAtomArray();
        int stereo = 0, def = 0, charge = 0;
        
        int[] gi = new int[atoms.length];
        mol.getGrinv(gi);
        
        for (int i = 0; i < atoms.length; ++i) {
            int chiral = mol.getChirality(i);
            MolAtom atom = mol.getAtom(i);
            
            if (chiral == MolAtom.CHIRALITY_R
                || chiral == MolAtom.CHIRALITY_S) {
                ++def;
                ++stereo;
                atom.setAtomMap(chiral == MolAtom.CHIRALITY_R ? 1 : 2);
            }
            else {
                boolean undef = chiral != 0;
                
                int pc = 0;
                // now check to see if for bond parity.. if any is 
                // defined then we consider this stereocenter as
                // defined!
                for (int k = 0; k < atom.getBondCount(); ++k) {
                    MolBond bond = atom.getBond(k);
                    int parity = bond.getFlags() & MolBond.STEREO1_MASK;
                    if ((parity == MolBond.UP || parity == MolBond.DOWN)
                        && bond.getAtom1() == atom)
                        ++pc;
                }
                
                boolean tetra = isTetrahedral (atom, gi);
                if (isQuatAmine (atom) 
                    || (pc > 0 && atom.getBondCount() > 2 )
                    || (undef && tetra)) {
                    ++stereo;
                    
                    if (pc > 0) {
                        ++def;
                        atom.setAtomMap(0);
                    }
                    else
                        atom.setAtomMap(3); // unknown
                }
            }
            charge += atoms[i].getCharge();
        }
        
        int ez = 0; // ez centers
        MolBond[] bonds = mol.getBondArray();
        for (int i = 0; i < bonds.length; ++i) {
            MolBond bond = bonds[i];
            if (mol.isRingBond(i)) {
            }
            else if (bond.getType() == 2) {
                MolAtom a1 = bond.getAtom1();
                MolAtom a2 = bond.getAtom2();
                if (a1.getBondCount() == 1 || a2.getBondCount() == 1) {
                    // nothing to do
                }
                else {
                    /*
                     *  \      /
                     *   \    /
                     *    ----
                     * a1 ---- a2
                     *   /         \
                     *  /          \
                     *
                     */
                    int g1 = -1;
                    for (int j = 0; j < a1.getBondCount(); ++j) {
                        MolBond b = a1.getBond(j);
                        if (b != bond) {
                            int index = mol.indexOf(b.getOtherAtom(a1));
                            if (gi[index] == g1) {
                                g1 = -1;
                                break;
                            }
                            g1 = gi[index];
                        }
                    }
                    
                    int g2 = -1;
                    for (int j = 0; j < a2.getBondCount(); ++j) {
                        MolBond b = a2.getBond(j);
                        if (b != bond) {
                            int index = mol.indexOf(b.getOtherAtom(a2));
                            if (gi[index] == g2) {
                                g2 = -1;
                                break;
                            }
                            g2 = gi[index];
                        }
                    }
                    
                    if (g1 >= 0 && g2 >= 0)
                        ++ez;
                }
            }
        }
            
        Molecule stdmol = mol.cloneMolecule();
        LyChIStandardizer mstd = new LyChIStandardizer ();
        mstd.removeSaltOrSolvent(false);
        try {
            mstd.standardize(stdmol);
        }
        catch (Exception ex) {
            logger.log(Level.SEVERE, 
                       "Can't standardize structure", ex);
        }
        
        //System.out.print(mol.toFormat("mol"));
        
        String[] hash = LyChIStandardizer.hashKeyArray(stdmol);
        struc.properties.add(new Keyword (Structure.LyChI_HASH_L1, hash[0]));
        struc.properties.add(new Keyword (Structure.LyChI_HASH_L2, hash[1]));
        struc.properties.add(new Keyword (Structure.LyChI_HASH_L3, hash[2]));
        struc.properties.add(new Keyword (Structure.LyChI_HASH_L4, hash[3]));
        struc.properties.add(new Text (Structure.LyChI_SMILES,
                                       ChemUtil.canonicalSMILES(stdmol)));
        struc.properties.add
            (new Text (Structure.LyChI_MOLFILE, stdmol.toFormat("mol")));
        struc.definedStereo = def;
        struc.stereoCenters = stereo;
        struc.ezCenters = ez;
        struc.charge = charge;
        struc.formula = mol.getFormula();
        struc.mwt = mol.getMass();
        struc.smiles = ChemUtil.canonicalSMILES(mol);
        
        PathFingerprint pf = new PathFingerprint (512, 2, 6);
        pf.generate(mol);
    }

    public static String toHex (byte[] binary) {
        StringBuilder sb = new StringBuilder ();
        for (int i = 0; i < binary.length; ++i) {
            sb.append(String.format("%1$02x", binary[i] & 0xff));
        }
        return sb.toString();
    }

    public static String digest (String s) {
        if (s != null) {
            try {
                MessageDigest md = MessageDigest.getInstance("SHA1");
                byte[] d = md.digest(s.getBytes("utf-8"));
                String digest = toHex (d);
                return digest;
            }
            catch (Exception ex) {
                logger.log(Level.SEVERE, 
                           "Something's rotten in the state of MD", ex);
            }
        }
        return "deadbeef";
    }
    
    public static String digest (Molecule mol) {
        return digest (mol.toFormat("mol"));
    }
}
