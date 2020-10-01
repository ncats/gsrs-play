package ix.core.chem;

import gov.nih.ncats.molwitch.*;
import gov.nih.ncats.molwitch.Chemical;
import ix.core.models.Keyword;
import ix.core.models.Structure;
import ix.core.models.Text;
import ix.core.models.Value;
import ix.core.util.CachedSupplier;


import java.io.IOException;
import java.security.MessageDigest;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;


public class StructureProcessor {

    private static StructureHasher hasher;

    private static  StructureStandardizer standardizer;

    public static StructureStandardizer getStandardizer() {
        return standardizer;
    }

    public static void setStandardizer(StructureStandardizer standardizer) {
        StructureProcessor.standardizer = standardizer;
    }

    public static StructureHasher getHasher() {
        return hasher;
    }

    public static void setHasher(StructureHasher hasher) {
        StructureProcessor.hasher = hasher;
    }


    private StructureProcessor () {}


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
            return instrument (Chemical.parse(new String(buf)),
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
            String nmol = ChemCleaner.removeSGroupsAndLegacyAtomLists(mol);
            try{
                instrument (struc, components, Chemical.parse(nmol), standardize);
            }catch(Exception e){
                System.err.println("Attempt failed");
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
            	try{
            		struc.smiles = mol.toSmiles();
            	}catch(Exception e2){
            		struc.smiles = mol.toSmarts();
            	}
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // no explicit Hs
        //dkatzel aug 2019 - exceppt when the Hs are stereo and explicitly drawn that way...
        
        mol.removeNonDescriptHydrogens();

        // make sure molecule is kekulized consistently
        try{
        	mol.kekulize();
        }catch(Exception e){
        	//it's okay to fail to kekulize in rare cases, particularly
        	//in some delocalized structures.
        }
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


        for(DoubleBondStereochemistry doubleBondStereochemistry : mol.getDoubleBondStereochemistry()) {
            Bond doubleBond = doubleBondStereochemistry.getDoubleBond();
           
            // TODO: this is actually a mistake, as it's fine to have some EZ bonds in rings
            // > 7 atoms. This needs to be more specific, asking based on ring size.
            if (!doubleBondStereochemistry.getStereo().equals(DoubleBondStereochemistry.DoubleBondStereo.NONE)) {
            	 boolean isRing=false;
                 try{
                 	isRing=doubleBond.isInRing();
                 }catch(Exception e){
                 	e.printStackTrace();
                 }
                 if(!isRing){
                	 ez++;
                 }
            }
        }

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
                play.Logger.error("Can't standardize structure", e);
            }
        }
        if(!updatedMol){
            standardizedMol = molSupplier.get();
        }



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

                instrument(moiety, null, Chem.fixMetals(frag), false);

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
                        play.Logger.debug("using Text!!! for " + value.length() + "  " + value);
                        struc.properties.add(new Text(key, value));
                    }
                }
            });

        }catch(Exception e){
            e.printStackTrace();
        }


        struc.definedStereo = def;
        struc.stereoCenters = stereo;
        struc.ezCenters = ez;
        struc.charge = charge;
        //struc.formula = mol.getFormula();



//        if(!mol.hasQueryAtoms() && !mol.hasPseudoAtoms()) {
        Chem.setFormula(struc);
        struc.setMwt(mol.getMass());
//        }
        if(!query){
            struc.smiles = standardizer.canonicalSmiles(struc, struc.molfile);
        }

        calcStereo (struc);


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
						//play.Logger.debug("in calcStereo prevening Optical.NONE");
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
                play.Logger.error("Something's rotten in the state of MD", ex);
            }
        }
        return "deadbeef";
    }

}
