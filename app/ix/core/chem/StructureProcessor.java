package ix.core.chem;

import ix.core.models.Keyword;
import ix.core.models.Structure;
import ix.core.models.Text;
import ix.core.models.Value;
import ix.core.util.StreamUtil;

import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import lychi.LyChIStandardizer;
import lychi.util.ChemUtil;
import chemaxon.formats.MolImporter;
import chemaxon.struc.MolAtom;
import chemaxon.struc.MolBond;
import chemaxon.struc.Molecule;
import chemaxon.util.MolHandler;
import gov.nih.ncgc.jchemical.Jchemical;


public class StructureProcessor {
    static final Logger logger = 
        Logger.getLogger(StructureProcessor.class.getName());

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

    public static Structure instrument (byte[] buf) {
        return instrument (buf, null);
    }

    public static Structure instrument (byte[] buf,
                                        Collection<Structure> components) {
        return instrument (buf, components, true);
    }
    
    public static Structure instrument (byte[] buf,
                                        Collection<Structure> components,
                                        boolean standardize) {
        try {
            return instrument (MolImporter.importMol(buf),
                               components, standardize);
        }
        catch (Exception ex) {
            throw new IllegalArgumentException (ex);
        }
    }
    
    public static Structure instrument (String mol) {
        return instrument (mol, null, true);
    }

    public static Structure instrument
        (String mol, Collection<Structure> components) {
        return instrument (mol, components, true);
    }
    
    public static Structure instrument
        (String mol, Collection<Structure> components, boolean standardize) {
        Structure struc = new Structure ();
        struc.digest = digest (mol);
        try {
            MolHandler mh = new MolHandler (mol);
            instrument (struc, components, mh.getMolecule(), standardize);
        }catch (Exception ex) {
            
            System.err.println("Trouble reading structure:");
            System.err.println(mol);
            System.err.println("Attempting to eliminate SGROUPS");
            String nmol = ChemCleaner.removeSGroups(mol);
            try{
                MolHandler mh = new MolHandler (nmol);
                instrument (struc, components, mh.getMolecule(), standardize);
            }catch(Exception e){
                System.err.println("Attempt failed");
                throw new IllegalArgumentException (e);    
            }
        }
        return struc;
    }

    public static Structure instrument (Molecule mol) {
        return instrument (mol, true);
    }
    
    public static Structure instrument (Molecule mol, boolean standardize) {
        return instrument (mol, null, standardize);
    }

    public static Structure instrument (Molecule mol,
                                        Collection<Structure> components) {
        return instrument (mol, components, true);
    }
    
    public static Structure instrument (Molecule mol,
                                        Collection<Structure> components,
                                        boolean standardize) {
        Structure struc = new Structure ();
        instrument (struc, components, mol, standardize);
        return struc;
    }

    static void instrument (Structure struc,
                            Collection<Structure> components,
                            Molecule mol) {
        instrument (struc, components, mol, true);
    }
    
    
    /**
     * All instrument calls lead to this one
     * @param settings
     */
    static void instrument (StructureProcessorTask settings) {
        Structure struc = settings.getStructure();
        Collection<Structure> components = settings.getComponents();
        Molecule mol = settings.getMolecule();
        boolean standardize = settings.isStandardize();
        boolean query = settings.isQuery();
        
        if (struc.digest == null) {
            struc.digest = digest (mol);
        }

        if (mol.getDim() < 2) {
            mol.clean(2, null);
        }
        
        if(query){
            struc.molfile = mol.toFormat("mol");
            struc.smiles = createQuery(struc.molfile);
        }
        
        // no explicit Hs
        mol.hydrogenize(false);
        
        // make sure molecule is kekulized consistently
        mol.aromatize();
        mol.dearomatize();

        if(!query){
            struc.molfile = mol.toFormat("mol");
        }
        
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
        if (standardize) {
            LyChIStandardizer mstd = new LyChIStandardizer ();
            mstd.removeSaltOrSolvent(false);
            
            try {
                mstd.standardize(stdmol);
                /*
                if (mstd.getFragmentCount() > 1) {
                    Molecule[] frags = stdmol.cloneMolecule().convertToFrags();
                    // break this structure into its individual components
                    Structure[] moieties = new Structure[frags.length];
                    for (int i = 0; i < frags.length; ++i) {
                        moieties[i] = new Structure ();
                        if (components != null)
                            components.add(moieties[i]);
                        // sigh.. recurse
                        instrument (moieties[i], null, frags[i], false);
                    }
                }
                */
                // use this to indicate that the structure has
                //  been standardized!
                struc.properties.add
                    (new Text (Structure.F_LyChI_SMILES,
                               ChemUtil.canonicalSMILES(stdmol)));
            }
            catch (Exception ex) {
                mol.clonecopy(stdmol);
                logger.log(Level.SEVERE, 
                           "Can't standardize structure", ex);
            }
        }

        // TP: commented out standardization, and 2 moiety limit.
        // the unfortunate side effect was to strip waters
        
        // Also, probably better to be err on the side of 
        // preserving user input
        
        // break this structure into its individual components
        Molecule[] frags = stdmol.cloneMolecule().convertToFrags();
        // used to not duplicate moieties
        Map<String, Structure> moietiesMap = new HashMap<String,Structure>();
        //System.err.println("+++++++++ "+frags.length+" components!");
        if (frags.length >= 1 && components!=null) {
            for (int i = 0; i < frags.length; ++i) {
                Structure moiety = new Structure ();
                //System.err.println("+++++++++++++ component "+i+"!");
                instrument (moiety, null, frags[i], false);
                for(Value v:moiety.properties){
                    if(v instanceof Keyword){
                        if(((Keyword)v).label.equals(Structure.H_LyChI_L4)){
                            String hash=((Keyword) v).term;
                            Structure s = moietiesMap.get(hash);
                            if(s!=null){
                                s.count++;
                            }else{
                                moietiesMap.put(hash,moiety);
                                if (components != null)
                                    components.add(moiety);
                            }
                            break;
                        }
                    }
                }
            }
        }
       
        
        //System.out.print(mol.toFormat("mol"));
        
        String[] hash = LyChIStandardizer.hashKeyArray(stdmol);
        struc.properties.add(new Keyword (Structure.H_LyChI_L1, hash[0]));
        struc.properties.add(new Keyword (Structure.H_LyChI_L2, hash[1]));
        struc.properties.add(new Keyword (Structure.H_LyChI_L3, hash[2]));
        struc.properties.add(new Keyword (Structure.H_LyChI_L4, hash[3]));

        struc.definedStereo = def;
        struc.stereoCenters = stereo;
        struc.ezCenters = ez;
        struc.charge = charge;
        //struc.formula = mol.getFormula();
        Chem.setFormula(struc);
        struc.mwt = mol.getMass();
        
        if(!query){
            struc.smiles = ChemUtil.canonicalSMILES(mol);
        }
        System.out.println("Canonical:" + struc.smiles);
        
        calcStereo (struc);
        
        
    }
  
    
    /**
     * This should return a decomposed version of a structure for G-SRS.
     * 
     * This means that a molfile should come back with moieties
     * and a structure, with statistics and predicted stereo
     * 
     * @param struc
     * @param components
     * @param mol
     * @param standardize
     */
    static void instrument (Structure struc, 
                            Collection<Structure> components,
                            Molecule mol, 
                            boolean standardize) {
        StructureProcessorTask settings = new StructureProcessorTask.Builder()
                                                            .structure(struc)
                                                            .query(false)
                                                            .mol(mol)
                                                            .components(components)
                                                            .build();
        instrument(settings);
    }

    static void calcStereo (Structure struc) {
        int total = struc.stereoCenters, defined = struc.definedStereo;
        if (total == 0) {
            struc.stereoChemistry = Structure.Stereo.ACHIRAL;
            struc.opticalActivity = Structure.Optical.NONE;
        }
        else if (total == defined) {
            struc.stereoChemistry = Structure.Stereo.ABSOLUTE;
            struc.opticalActivity = Structure.Optical.UNSPECIFIED;
        }
        else if (total == 1 && defined == 0) {
            struc.stereoChemistry = Structure.Stereo.RACEMIC;
            struc.opticalActivity = Structure.Optical.PLUS_MINUS;
        }
        else if ((total - defined) == 1) {
            struc.stereoChemistry = Structure.Stereo.EPIMERIC;
            struc.opticalActivity = Structure.Optical.UNSPECIFIED;
        }
        else if ((total - defined) > 1) {
            struc.stereoChemistry = Structure.Stereo.MIXED;
            struc.opticalActivity = Structure.Optical.UNSPECIFIED;
        }
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

    public static String createQuery (String mol) {
        try {
            MolHandler mh = new MolHandler (mol);
            return createQuery (mh.getMolecule());
        }
        catch (Exception ex) {
            throw new IllegalArgumentException ("Can't parse molecule", ex);
        }
    }

    public static String createQuery (Molecule mol) {
        Map<Integer, Integer> amap = new HashMap<Integer, Integer>();
        MolAtom[] atoms = mol.getAtomArray();
        for (int i = 0; i < atoms.length; ++i) {
            switch (atoms[i].getAtno()) {
            case MolAtom.PSEUDO:
            case MolAtom.RGROUP:
            case MolAtom.SGROUP:
            case MolAtom.ANY:
            case MolAtom.NOTLIST:
            case MolAtom.LIST:
            case 114:
                // this is what marvinjs specifies for atom *
                amap.put(i, 114 == atoms[i].getAtno()
                         ? MolAtom.ANY : atoms[i].getAtno());
                atoms[i].setAtno(6); // force this to be carbon
                break;
            default:
                // do nothing
            }
        }
        
        mol.aromatize();
        // now restore the atom labels
        for (Map.Entry<Integer, Integer> me : amap.entrySet()) {
            atoms[me.getKey()].setAtno(me.getValue());
        }
        
        return mol.toFormat("smarts");
    }
}
