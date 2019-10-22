package ix.core.chem;

import gov.nih.ncats.molwitch.*;
import gov.nih.ncats.molwitch.Chemical;
import ix.core.models.Keyword;
import ix.core.models.Structure;
import ix.core.models.Text;
import ix.core.models.Value;
import ix.core.util.CachedSupplier;
import ix.core.util.StreamUtil;

import java.io.IOException;
import java.security.MessageDigest;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;


public class StructureProcessor {

    private static StructureHasher hasher;

    public static StructureHasher getHasher() {
        return hasher;
    }

    public static void setHasher(StructureHasher hasher) {
        StructureProcessor.hasher = hasher;
    }

    static final Logger logger =
            Logger.getLogger(StructureProcessor.class.getName());

    private StructureProcessor () {}



//    public static boolean isQuatAmine (Atom atom) {
//
//
//
//        return atom.getAtomicNumber() == 7 && atom.getBondCount() == 4;
//    }

    /**
     * Checks if an atom is a quat-amine (4 bonds), and that the nitrogen
     * is a stereocenter. This currently works by converting that atom to an
     * uncharged carbon and testing whether that carbon would be a stereocenter.
     *
     * @param
     * @return
     */
    /*
    public static boolean isQuatAmineStereoCenter (Atom atom) {
    	if(isQuatAmine(atom)){
    		 int oamap=atom.getAtomToAtomMap().orElse(0);

    		 atom.setAtomToAtomMap(99);


    		 Molecule m = (Molecule)atom.getParent();
    		 Molecule testm = m.cloneMolecule();
    		 MolAtom[] atoms = testm.getAtomArray();
    		 MolAtom matest=null;
    		 for(int i=0;i<atoms.length;i++){
    			 if(atoms[i].getAtomMap() == 99){
    				 matest=atoms[i];
    				 matest.setAtno(6);
    				 matest.setCharge(0);
    			 }
    		 }
    		 int[] gi = new int[atoms.length];
    		 testm.getGrinv(gi);
    		 atom.setAtomMap(oamap);

    		 return isTetrahedral(matest,gi);
    	}
    	return false;
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
*/
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
            return instrument (Chemical.parseMol(buf),
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
            instrument (struc, components, Chemical.parse(mol), standardize);
        }catch (Exception ex) {
            ex.printStackTrace();
            System.err.println("Trouble reading structure:");
            System.err.println(mol);
            System.err.println("Attempting to eliminate SGROUPS");
            String nmol = ChemCleaner.removeSGroups(mol);
            try{
                instrument (struc, components, Chemical.parse(nmol), standardize);
            }catch(Exception e){
                System.err.println("Attempt failed");
                e.printStackTrace();
                throw new IllegalArgumentException (e);
            }
        }
        return struc;
    }

    public static Structure instrument (Chemical mol) {
        return instrument (mol, true);
    }

    public static Structure instrument (Chemical mol, boolean standardize) {
        return instrument (mol, null, standardize);
    }

    public static Structure instrument (Chemical mol,
                                        Collection<Structure> components) {
        return instrument (mol, components, true);
    }

    public static Structure instrument (Chemical mol,
                                        Collection<Structure> components,
                                        boolean standardize) {
        Structure struc = new Structure ();
        instrument (struc, components, mol, standardize);
        return struc;
    }

    static void instrument (Structure struc,
                            Collection<Structure> components,
                            Chemical mol) {
        instrument (struc, components, mol, true);
    }


    /**
     * All instrument calls lead to this one
     * @param settings
     */
    static void instrument (StructureProcessorTask settings) {
        Structure struc = settings.getStructure();
        Collection<Structure> components = settings.getComponents();
        Chemical mol = settings.getChemical().copy();
        boolean standardize = settings.isStandardize();
        boolean query = settings.isQuery();

        LychiStandardizer standardizer = new LychiStandardizer();

        CachedSupplier<String> molSupplier = CachedSupplier.of(new Supplier<String>() {
            public String get(){
                try {
                    return mol.toMol();
                } catch (IOException e) {
                    e.printStackTrace();
                    return null;
                }
            }
        });
        if (struc.digest == null) {
            struc.digest = digest (molSupplier.get());

        }
//katzelda this probably isn't needed anymore since now settings.getChemical should
        //compute coords if needed ??
        if (!mol.hasCoordinates()) {
            try {
                mol.generateCoordinates();
                molSupplier.resetCache();
            } catch (ChemkitException e) {
                e.printStackTrace();
            }
        }

        if(query){
            struc.molfile = molSupplier.get();
            try {
                struc.smiles = mol.toSmarts();
//                System.out.println("query smarts is " + struc.smiles);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // no explicit Hs
        //dkatzel aug 2019 - exceppt when the Hs are stereo and explicitly drawn that way...
        mol.removeNonDescriptHydrogens();

        // make sure molecule is kekulized consistently
        mol.kekulize();
        molSupplier.resetCache();
        if(!query){

            struc.molfile = molSupplier.get();
        }

        Chem.fixMetals(mol);
        molSupplier.resetCache();

        int stereo = 0, def = 0, charge = 0, ez=0;

        for(Atom a : mol.getAtoms()){
            charge += a.getCharge();
        }

        List<Stereocenter> stereocenters = new ArrayList<>(mol.getTetrahedrals());
        for(Stereocenter stereocenter : stereocenters) {
            stereo++;
            if(stereocenter.isDefined()){
                def++;
            }
        }
        /*
        Set<Atom> stereoAtomsSeen = new HashSet<>();
        for(Stereocenter stereocenter : mol.getAllStereocenters()) {
//            def++;
//            stereo++;
            stereo++;

            Chirality chirality = stereocenter.getChirality();
            if (chirality.isOdd() || chirality.isEven()) {
                def++;
            } else if (chirality.isEither()) {
                Atom center = stereocenter.getCenterAtom();
                if (center != null && (center.getAtomicNumber() == 7 || center.getAtomicNumber() == 15)) {
                    //quat amine?  not stereo
                    stereo--;
                } else if (stereocenter.getPeripheralAtoms()
                        .stream()
                        .flatMap(a -> a.getBonds().stream())
                        .filter(b -> {
                            switch (b.getStereo()) {

                                case NONE:
                                    return false;
                                default:
                                    return true;
                            }
                        })
                        .findAny().isPresent()) {
                    def++;
                }
            }
//            if(chirality.isEven() || chirality.isOdd()){
//                def++;
//            }
            stereoAtomsSeen.add(stereocenter.getCenterAtom());
        }
        */
//            if(stereocenter instanceof TetrahedralChirality){
//                Chirality chirality = stereocenter.getChirality();
//                stereocenter.getCenterAtom()
//                if(chirality.isEven() || chirality.isOdd()){
//                    ez++;
//                }
//            }
//
//            Chirality chirality = stereocenter.getChirality();
//            if(chirality.isEven() || chirality.isOdd()){
//                ez++;
//            }
//        }

//        GraphInvariant gi = mol.getGraphInvariant();


//        for(Atom a : mol.getAtoms()){
//            Chirality chirality = a.getChirality();
//            if(chirality != Chirality.Non_Chiral && stereoAtomsSeen.add(a)){
//                def++;
//                stereo++;
//            }
//        }

        for(DoubleBondStereochemistry doubleBondStereochemistry : mol.getDoubleBondStereochemistry()) {
            Bond doubleBond = doubleBondStereochemistry.getDoubleBond();
            if (!doubleBond.isInRing() && !doubleBondStereochemistry.getStereo().equals(DoubleBondStereochemistry.DoubleBondStereo.NONE)) {
                ez++;
//                stereo++;
            }
        }
//            if(!doubleBond.isInRing() || doubleBondStereochemistry.getStereo().equals(DoubleBondStereochemistry.DoubleBondStereo.NONE)) {
//                Atom a1 = doubleBond.getAtom1();
//                Atom a2 = doubleBond.getAtom2();
//
//                List<Atom> a1Neighbors = a1.getNeighbors();
//                ;
//                List<Atom> a2Neighbors = a2.getNeighbors();
//                Set<Long> neighbbors = Stream.concat(a1Neighbors.stream(), a2Neighbors.stream())
//                                            .filter(a-> a.getAtomicNumber() !=1)
//                                            .map(a-> gi.getAtomInvariantValue(a.getAtomIndexInParent()))
//                                            .collect(Collectors.toSet());
//;
//                if(neighbbors.size() ==a1Neighbors.size() + a2Neighbors.size()){
//                    switch (doubleBondStereochemistry.getStereo()) {
//                    case E_TRANS:
//                    case Z_CIS:
////                    case E_OR_Z:
//                        ez++;
//                }
//                }else{
//                    System.out.println("filtered out an EZ !!!!!");
//                }
////                switch (doubleBondStereochemistry.getStereo()) {
////                    case E_TRANS:
////                    case Z_CIS:
////                    case E_OR_Z:
////                        ez++;
////                }
//            }
//        }
//        System.out.println(struc.getId());
//        if("00b17bf6-4337-4c8d-8eed-a70bf362bc44".equals(struc.getId())){
//            System.out.println("00b17bf6-4337-4c8d-8eed-a70bf362bc44 has ez =" + ez);
//        }
        /*

        Molecule jchemMol=null;
        try {
            jchemMol = MolImporter.importMol(molSupplier.get());
        }catch(MolFormatException e){
            e.printStackTrace();
        }
        MolAtom[] atoms = jchemMol.getAtomArray();
//        int stereo = 0, def = 0, charge = 0;
//
        int[] gi = new int[atoms.length];
        jchemMol.getGrinv(gi);

        for (int i = 0; i < atoms.length; ++i) {

            int chiral = jchemMol.getChirality(i);
            MolAtom atom = jchemMol.getAtom(i);





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
                if (isQuatAmineStereoCenter (atom)
                    || (pc > 0 && atom.getBondCount() > 2 )
                    || (undef && tetra)) {
                    ++stereo;

                    if (pc > 0) {
                        ++def;
                        atom.setAtomMap(0);
                    }else{
                        atom.setAtomMap(3); // unknown
                    }
                }
            }
            charge += atoms[i].getCharge();
        }

        int ez = 0; // ez centers
        MolBond[] bonds = jchemMol.getBondArray();
        for (int i = 0; i < bonds.length; ++i) {
            MolBond bond = bonds[i];
            if (jchemMol.isRingBond(i)) {
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
        /*
                    int g1 = -1;
                    for (int j = 0; j < a1.getBondCount(); ++j) {
                        MolBond b = a1.getBond(j);
                        if (b != bond) {
                            int index = jchemMol.indexOf(b.getOtherAtom(a1));
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
                            int index = jchemMol.indexOf(b.getOtherAtom(a2));
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


        Molecule jchemMol=null;
        try {
            jchemMol = MolImporter.importMol(molSupplier.get());
        }catch(MolFormatException e){
            e.printStackTrace();
        }
        */
//        Molecule stdmol = jchemMol.cloneMolecule();
        String standardizedMol=null;
        boolean updatedMol=false;

        Chemical stdMol = mol;
        if (standardize) {

            try {

                stdMol = standardizer.standardize(mol, molSupplier, struc.properties::add);
                if (stdMol != mol) {
                    standardizedMol = stdMol.toMol();
                    updatedMol = true;

                }
            } catch (IOException e) {
                e.printStackTrace();
                logger.log(Level.SEVERE,
                        "Can't standardize structure", e);
            }
        }
        if(!updatedMol){
            standardizedMol = molSupplier.get();
        }

/*
            CachedSupplier<Integer> actualAtomLimitForStandardization = ConfigHelper.supplierOf("ix.core.structureIndex.atomLimit",ATOM_LIMIT_FOR_STANDARDIZATION);
			int maxAtoms = actualAtomLimitForStandardization.get();
            try {
                if( stdmol.getAtomCount() <=maxAtoms)
				{
                    LyChIStandardizer mstd = new LyChIStandardizer ();
                    mstd.removeSaltOrSolvent(false);
					mstd.standardize(stdmol);
                    // use this to indicate that the structure has
                    //  been standardized!
                    struc.properties.add
                            (new Text (Structure.F_LyChI_SMILES,
                                    ChemUtil.canonicalSMILES(stdmol)));
//					System.out.println(
////					String.format("Standardizing because atom count %d <= cuttoff %d",
//					stdmol.getAtomCount(), maxAtoms));
				}
//				else
//				{
//					System.out.println(String.format("Skipping standardizing because atom count %d > cuttoff %d",
//						stdmol.getAtomCount(), maxAtoms));
//				}
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



            }

            catch (Exception ex) {
                jchemMol.clonecopy(stdmol);
                logger.log(Level.SEVERE,
                           "Can't standardize structure", ex);
            }

        }
        */

        // TP: commented out standardization, and 2 moiety limit.
        // the unfortunate side effect was to strip waters

        // Also, probably better to be err on the side of
        // preserving user input

        // break this structure into its individual components
        //System.out.print(mol.toFormat("mol"));
//        String standardizedMol = stdmol.toFormat("mol");
//        List<Chemical> frags;
//        try {
//            frags = Chemical.parseMol(standardizedMol).connectedComponentsAsStream().collect(Collectors.toList());
//        }catch(IOException e){
//            throw new UncheckedIOException(e);
//        }

        //Note that this currently uses the non-standardized structure instead of the standardized one.
        //This is currently intentional, as the standardized structure does some charge balancing that might be unexpected.
        //Nevertheless, there are times when a structure should really be prestandardized, and then have moieties generated

        List<Chemical> frags = mol.connectedComponentsAsStream().collect(Collectors.toList());

        // used to not duplicate moieties
        Map<String, Structure> moietiesMap = new HashMap<>();
        //System.err.println("+++++++++ "+frags.length+" components!");
        if (frags.size() >= 1 && components!=null) {
            for (Chemical frag : frags) {
                Structure moiety = new Structure ();
                //System.err.println("+++++++++++++ component "+i+"!");

                instrument(moiety, null, fixMetals(frag), false);

                for(Value v:moiety.properties){
                    if(v instanceof Keyword){
                        if(((Keyword)v).label.equals(Structure.H_EXACT_HASH)){
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


        try{
            Chemical cc=polymerSimplify(stdMol);
            getHasher().hash(cc, cc.toMol(), new BiConsumer<String, String>() {
                @Override
                public void accept(String key, String value){
                    if(value==null || value.length() < 255) {
                        struc.properties.add(new Keyword(key, value));
                    }else{
                        System.out.println("using Text!!! for " + value.length() + "  " + value);
                        struc.properties.add(new Text(key, value));
                    }
                }
            });

        }catch(Exception e){
            e.printStackTrace();
        }



//        String[] hash = LyChIStandardizer.hashKeyArray(stdmol);
//        struc.properties.add(new Keyword (Structure.H_LyChI_L1, hash[0]));
//        struc.properties.add(new Keyword (Structure.H_LyChI_L2, hash[1]));
//        struc.properties.add(new Keyword (Structure.H_LyChI_L3, hash[2]));
//        struc.properties.add(new Keyword (Structure.H_LyChI_L4, hash[3]));

        struc.definedStereo = def;
        struc.stereoCenters = stereo;
        struc.ezCenters = ez;
        struc.charge = charge;
        //struc.formula = mol.getFormula();
        Chem.setFormula(struc);
        struc.mwt = mol.getMass();

        if(!query){
            struc.smiles = standardizer.canonicalSmiles(struc, struc.molfile);
        }

        calcStereo (struc);


    }
    public static Chemical fixMetals(Chemical chemical){
        for(int i=0; i< chemical.getAtomCount(); i++){
            Atom atom = chemical.getAtom(i);
            if(lychi.ElementData.isMetal(atom.getAtomicNumber())){
                atom.setImplicitHCount(0);
            }
        }

        return chemical;
    }

    public static Chemical polymerSimplify(Chemical chem){
        try{
            String nmol = Arrays.stream(chem.toMol().split("\n"))
                    .filter(ll->!ll.startsWith("M  S"))
                    .collect(Collectors.joining("\n"));

            Chemical cc=Chemical.parseMol(nmol);

            cc.atoms().filter(ca->ca.isQueryAtom())
                    .forEach(ca->{
                        //set all queries to helium as a hack
                        ca.setAtomicNumber(2);
                    });

            return cc;
        }catch(Exception e){
            return chem;
        }
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
                            Chemical mol,
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

//    public static String digest (Molecule mol) {
//        return digest (mol.toFormat("mol"));
//    }

//    public static String createQuery (String mol) {
//        try {
//            MolHandler mh = new MolHandler (mol);
//            return createQuery (mh.getMolecule());
//        }
//        catch (Exception ex) {
//            throw new IllegalArgumentException ("Can't parse molecule", ex);
//        }
//    }

//    public static String createQuery (Molecule mol) {
//        Map<Integer, Integer> amap = new HashMap<Integer, Integer>();
//        MolAtom[] atoms = mol.getAtomArray();
//        for (int i = 0; i < atoms.length; ++i) {
//            switch (atoms[i].getAtno()) {
//            case MolAtom.PSEUDO:
//            case MolAtom.RGROUP:
//            case MolAtom.SGROUP:
//            case MolAtom.ANY:
//            case MolAtom.NOTLIST:
//            case MolAtom.LIST:
//            case 114:
//                // this is what marvinjs specifies for atom *
//                amap.put(i, 114 == atoms[i].getAtno()
//                         ? MolAtom.ANY : atoms[i].getAtno());
//                atoms[i].setAtno(6); // force this to be carbon
//                break;
//            default:
//                // do nothing
//            }
//        }
//
//        mol.aromatize();
//        // now restore the atom labels
//        for (Map.Entry<Integer, Integer> me : amap.entrySet()) {
//            atoms[me.getKey()].setAtno(me.getValue());
//        }
//
//        return mol.toFormat("smarts");
//    }
}
