package ix.ginas.utils;

import java.io.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

//import chemaxon.formats.*;
//import chemaxon.struc.*;
//import chemaxon.util.MolHandler;
//
//import org.codehaus.jackson.map.ObjectMapper;
//import org.codehaus.jackson.node.ArrayNode;
//import org.codehaus.jackson.node.ObjectNode;
//import org.codehaus.jackson.JsonNode;
//
//import gov.nih.ncgc.util.GrayCode;
//import static ginas.registry.ChemUtils.*;

//Uncomment and actually use when ready
public class EnantiomerService {
//    static final Logger logger = 
//	Logger.getLogger(EnantiomerService.class.getName());
//
//    public interface Callback {
//        void generated (Molecule mol, Map instance);
//    }
//
//    protected Molecule mol;
//    protected Stereo stereo;
//    protected Optical optical;
//
//    // stereo annotation
//    protected Map<Integer, Integer> stereocenters = 
//        new TreeMap<Integer, Integer>();
//    protected int stereodefined;
//    
//    public EnantiomerService () {
//    }
//
//    public EnantiomerService (ObjectNode json) {
//        setJSON (json);
//    }
//
//    public void setMol (Molecule mol) { this.mol = mol; }
//    public Molecule getMol () { return mol; }
//    public void setStereo (Stereo stereo) { this.stereo = stereo; }
//    public Stereo getStereo () { return stereo; }
//    public Optical getOptical () { return optical; }
//    public void setOptical (Optical optical) { this.optical = optical; }
//
//    /* assume json contains at the minimum the following fields:
//     {
//        molfile: "C1=CC=CC=C1C(C)CCC",
//        stereochemistry: "RACEMIC",
//        opticalActivity:"(+/-)"
//     }
//    */
//    public void setJSON (ObjectNode json) {
//        JsonNode node = json.get("molfile");
//        if (node == null) {
//            throw new IllegalArgumentException
//                ("No field \"molfile\" defined!");
//        }
//        try {
//            MolHandler mh = new MolHandler (node.asText());
//            this.mol = mh.getMolecule();
//            instrumentation ();
//        }
//        catch (Exception ex) {
//            throw new IllegalArgumentException
//                ("Bogus molfile: "+node.asText());
//        }
//
//        node = json.get("stereochemistry");
//        if (node == null) {
//            throw new IllegalArgumentException
//                ("No field \"stereochemistry\" defined!");
//        }
//        try {
//            this.stereo = Stereo.valueOf(node.asText());
//        }
//        catch (Exception ex) {
//            throw new IllegalArgumentException 
//                ("Bogus stereochemistry specified: "+node.asText());
//        }
//
//        node = json.get("opticalActivity");
//        if (node == null) {
//            this.optical = Optical.UNSPECIFIED;
//        }
//        else {                
//            String activity = node.asText();
//            try {
//                int plus = 0, minus = 0;
//                if (activity.indexOf('+') >= 0) ++plus;
//                if (activity.indexOf('-') >= 0) ++minus;
//
//                if (plus > 0 || minus > 0) {
//                    if (plus > 0 && minus > 0)
//                        optical = Optical.PLUS_MINUS;
//                    else if (plus > 0)
//                        optical = Optical.PLUS;
//                    else // if (minus > 0)
//                        optical = Optical.MINUS;
//                }
//                else {
//                    this.optical = Optical.valueOf(activity);
//                }
//            }
//            catch (Exception ex) {
//                throw new IllegalArgumentException 
//                    ("Bogus optical activity specified: "+activity);
//            }
//        }
//    }
//
//    void instrumentation () {
//        if (mol.getDim() < 2)
//            mol.clean(2, null);
//        
//        stereocenters.clear();
//        stereodefined = 0;
//        for (int i = 0; i < mol.getAtomCount(); ++i) {
//            int chiral = mol.getChirality(i);
//            if (chiral == MolAtom.CHIRALITY_R) {
//                stereocenters.put(i, chiral);
//                ++stereodefined;
//            }
//            else if (chiral == MolAtom.CHIRALITY_S) {
//                stereocenters.put(i, chiral);
//                ++stereodefined;
//            }
//            else if (chiral != 0) {
//                stereocenters.put(i, chiral); // either R/S
//            }
//        }
//    }
//
//    public void generate (final Callback callback) {
//        if (stereocenters.isEmpty()) {
//            logger.info("## No stereocenters; nothing to generate...");
//            return;
//        }
//
//        logger.info("## generating enantiomers stereochemistry="+stereo
//                    +" optical activity="+optical
//                    +" stereocenters "+stereodefined+"/"
//                    +stereocenters.size()+" "+stereocenters);
//
//        switch (stereo) {
//        case RACEMIC:
//            if (optical == Optical.PLUS_MINUS) {
//                if (stereodefined == 0 && stereocenters.size() == 1) {
//                    // generate one version each
//                    Integer chiral = stereocenters.keySet().iterator().next();
//                    Molecule clone = mol.cloneMolecule();
//                    clone.setChirality(chiral, MolAtom.CHIRALITY_R);
//                    Map props = new HashMap ();
//                    ChemUtils.instrument(clone, props);
//                    callback.generated(clone, props);
//
//                    clone = mol.cloneMolecule();
//                    clone.setChirality(chiral, MolAtom.CHIRALITY_S);
//                    props = new HashMap ();
//                    ChemUtils.instrument(clone, props);
//                    callback.generated(clone, props);
//                }
//                else if (stereodefined > 0 
//                         && stereodefined == stereocenters.size()) {
//                    Map props = new HashMap ();
//                    ChemUtils.instrument(mol, props);
//                    // generate a version as-is
//                    callback.generated(mol, props); 
//
//                    // now the flipped version
//                    Molecule clone = mol.cloneMolecule();
//                    for (Map.Entry<Integer, Integer> me : 
//                             stereocenters.entrySet()) {
//                        Integer atom = me.getKey();
//                        Integer chiral = me.getValue();
//                        if (chiral == MolAtom.CHIRALITY_R) {
//                            clone.setChirality(atom, MolAtom.CHIRALITY_S);
//                        }
//                        else { // chiral == MolAtom.CHIRALITY_S 
//                            clone.setChirality(atom, MolAtom.CHIRALITY_R);
//                        }
//                    }
//                    ChemUtils.instrument(clone, props);
//                    // generate a version as-is
//                    callback.generated(clone, props);
//                }
//            }
//            else { // invalid... do nothing
//                throw new IllegalStateException
//                    ("RACEMIC stereochemistry with optical activity "+optical
//                     +" not supported!");
//            }
//            break;
//
//        case EPIMERIC:
//            if (stereocenters.size() > 1 
//                && stereodefined+1 == stereocenters.size()) {
//                Integer atom = null;
//                for (Map.Entry<Integer, Integer> me 
//                         : stereocenters.entrySet()) {
//                    if (me.getValue() != MolAtom.CHIRALITY_R
//                        && me.getValue() != MolAtom.CHIRALITY_S) {
//                        atom = me.getKey();
//                    }
//                }
//
//                if (atom == null) {
//                    throw new IllegalStateException
//                        ("Something's rotten in the state of Maryland!");
//                }
//                else if (optical == Optical.NONE) {
//                    // do nothing
//                    return;
//                }
//
//                Molecule clone;
//                Map props;
//
//                clone = mol.cloneMolecule();
//                clone.setChirality(atom, MolAtom.CHIRALITY_R);
//                props = new HashMap ();
//                ChemUtils.instrument(clone, props);
//                callback.generated(clone, props);
//                
//                clone = mol.cloneMolecule();
//                clone.setChirality(atom, MolAtom.CHIRALITY_S);
//                props = new HashMap ();
//                ChemUtils.instrument(clone, props);
//                callback.generated(clone, props);
//
//                // multiple stereocenters but only one undefined
//                if (Optical.PLUS_MINUS == optical) {
//                    // generate four instances xxxxxR, xxxxxS, yyyyyR, yyyyyS
//                    // where y = ~x
//
//                    // now flip
//                    Molecule flip = ChemUtils.flip(mol);
//                    clone = flip.cloneMolecule();
//                    clone.setChirality(atom, MolAtom.CHIRALITY_R);
//                    props = new HashMap ();
//                    ChemUtils.instrument(clone, props);
//                    callback.generated(clone, props);
//
//                    clone = flip.cloneMolecule();
//                    clone.setChirality(atom, MolAtom.CHIRALITY_S);
//                    props = new HashMap ();
//                    ChemUtils.instrument(clone, props);
//                    callback.generated(clone, props);
//                }
//            }
//            else {
//                // invalid... do nothing
//                throw new IllegalStateException
//                    ("EPIMERIC stereochemistry with more than 1 "
//                     +"stereocenters undefined not supported!");
//            }
//            break;
//
//        case MIXED: {
//            if (optical == Optical.NONE) {
//                return;
//            }
//
//            final List<Integer> undefs = new ArrayList<Integer>();
//            for (Map.Entry<Integer, Integer> me : stereocenters.entrySet()) {
//                if (MolAtom.CHIRALITY_R != me.getValue() 
//                    && MolAtom.CHIRALITY_S != me.getValue()) {
//                    undefs.add(me.getKey());
//                }
//            }
//            
//            GrayCode gc = GrayCode.createBinaryGrayCode(undefs.size());
//            gc.addObserver(new Observer () {
//                    public void update (Observable o, Object arg) {
//                        int[] c = (int[])arg;
//
//                        Molecule clone = mol.cloneMolecule();
//                        for (int i = 0; i < c.length; ++i) {
//                            int chirality = c[i] == 0 ? MolAtom.CHIRALITY_R
//                                : MolAtom.CHIRALITY_S;
//                            Integer atom = undefs.get(i);
//                            clone.setChirality(atom, chirality);
//                        }
//                        Map props = new HashMap ();
//                        ChemUtils.instrument(clone, props);
//                        callback.generated(clone, props);
//                    }
//                });
//            gc.generate();
//
//            if (optical == Optical.PLUS_MINUS && stereodefined > 0) {
//                // flip 
//                final Molecule flip = ChemUtils.flip(mol);
//                gc = GrayCode.createBinaryGrayCode(undefs.size());
//                gc.addObserver(new Observer () {
//                        public void update (Observable o, Object arg) {
//                            int[] c = (int[])arg;
//                            
//                            Molecule clone = flip.cloneMolecule();
//                            for (int i = 0; i < c.length; ++i) {
//                                int chirality = c[i] == 0 ? MolAtom.CHIRALITY_R
//                                    : MolAtom.CHIRALITY_S;
//                                Integer atom = undefs.get(i);
//                                clone.setChirality(atom, chirality);
//                            }
//                            Map props = new HashMap ();
//                            ChemUtils.instrument(clone, props);
//                            callback.generated(clone, props);
//                        }
//                    });
//                gc.generate();
//            }
//        } break;
//
//            /*
//        case ABSOLUTE:
//        case ACHIRAL:
//        case UNKNOWN:
//            */
//        default:
//            { Map props = new HashMap ();
//                ChemUtils.instrument(mol, props);
//                callback.generated(mol, props);
//            }
//        }
//    }
}