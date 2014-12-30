package ix.ginas.chem;

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

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

public class CoreChem {
    static final Logger logger = 
	Logger.getLogger(CoreChem.class.getName());

    static final String CACHE_NAME = CoreChem.class.getName();

    protected final static Cache cache;
    static {
        System.setProperty("net.sf.ehcache.skipUpdateCheck", "true");
        int max = Integer.getInteger("max-cache-size", 50000);

        Cache _cache = CacheManager.getInstance().getCache(CACHE_NAME);
        if (_cache == null) {
            _cache = new Cache (CACHE_NAME, max, 
                                false, false, 30*60*60, 30*60*60);
            CacheManager.getInstance().addCache(_cache);
        }
        else {
            _cache.removeAll();
        }
        cache = _cache;
    }

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

    // stereochemistry
    public enum Stereo {
        ABSOLUTE,
	ACHIRAL,
	RACEMIC,
	MIXED,
	EPIMERIC,
	UNKNOWN
	;
    }

    // optical activity
    public enum Optical {
        PLUS, // (+)
	MINUS, // (-)
	PLUS_MINUS, // (+/-)
	UNSPECIFIED,
	UNKNOWN
	;
    }

    private CoreChem () {}

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

    public static String instrument (String mol, Map props) {
        try {
            MolHandler mh = new MolHandler (mol);
            return instrument (mh.getMolecule(), props);
        }
        catch (Exception ex) {
            throw new IllegalArgumentException ("Bogus molecule "+mol);
        }
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

    public static String instrument (Molecule mol, Map props) {
        if (mol.getDim() < 2) {
            mol.clean(2, null);
        }

        // no explicit Hs
        mol.hydrogenize(false);
        // make sure molecule is kekulized consistently
        mol.aromatize();
        mol.dearomatize();

        String digest = digest (mol);
        if (props != null) {
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
                         *   /    \
                         *  /      \
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

            String hash = LyChIStandardizer.hashKeyArray(stdmol)[3];
            props.put("hash", hash);
            props.put("count", 1);
            props.put("definedStereo", def);
            props.put("stereoCenters", stereo);
            props.put("ezCenters", ez);
            props.put("charge", charge);
            props.put("formula", mol.getFormula());
            props.put("mwt", mol.getMass());
            props.put("molfile", mol.toFormat("mol"));
            props.put("imgurl", "/"+digest+"/image");
            props.put("smiles", ChemUtil.canonicalSMILES(mol));
            props.put("stdsmiles", ChemUtil.canonicalSMILES(stdmol));

            PathFingerprint pf = new PathFingerprint (512, 2, 6);
            pf.generate(mol);
            props.put("pathFingerprint", pf);
        }
        cache.put(new Element (digest, mol));

        return digest;
    }

    public static String toHex (byte[] binary) {
        StringBuilder sb = new StringBuilder ();
        for (int i = 0; i < binary.length; ++i) {
            sb.append(String.format("%02x", binary[i] & 0xff));
        }
        return sb.toString();
    }

    public static String digest (Molecule mol) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA1");
            byte[] d = md.digest(mol.toFormat("mol").getBytes("utf-8"));
            String digest = toHex (d);
            cache.put(new Element (digest, mol));
            return digest;
        }
        catch (Exception ex) {
            logger.log(Level.SEVERE, 
                       "Something's rotten in the state of MD", ex);
        }
        return "deadbeef";
    }

    public static Molecule get (String key) {
        Element elm = cache.get(key);
        return elm != null ? (Molecule)elm.getObjectValue() : null;
    }

    public static List<Map> components (Molecule mol) throws Exception {
        String molfile = mol.toFormat("mol"); // preserve the original layout

        LyChIStandardizer mstd = new LyChIStandardizer ();
        mstd.removeSaltOrSolvent(false);
        mstd.standardize(mol);
        
        Map<String, List<Map>> merged = new HashMap<String, List<Map>>();
        Map mmap = component (mol, null);
        mmap.put("molfile", molfile); // override molfile with original

        String formula = mol.getFormula();
        List<Map> entries = new ArrayList<Map>();
        entries.add(mmap);

        if (mstd.getFragmentCount() > 1) {
            Molecule[] frags = mol.convertToFrags();
            for (Molecule f : frags) {
                Map map = component (f, null);
                String hash = (String)map.get("hash");
                List<Map> list = merged.get(hash);
                if (list == null) {
                    merged.put(hash, list = new ArrayList<Map>());
                }
                list.add(map);
                if (formula == null) {
                    formula = f.getFormula();
                }
                else {
                    formula += "."+f.getFormula();
                }
            }
        }
        else {
            entries.add(mmap);
        }

        mmap.put("formula", formula);
        for (List<Map> list : merged.values()) {
            Map map = list.iterator().next();
            map.put("count", list.size());
            entries.add(map);
        }

        return entries;
    }

    static public Map component (Molecule f, Map map) {
        if (map == null) {
            map = new HashMap ();
        }

        instrument (f, map);
        return map;
    }
}
