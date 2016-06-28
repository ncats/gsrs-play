package ix.core.chem;

import java.io.*;
import java.util.*;

import chemaxon.formats.*;
import chemaxon.struc.*;
import chemaxon.util.MolHandler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import static ix.core.models.Structure.*;
import ix.core.models.Structure;
import play.Logger;

public class EnantiomerGenerator {

    public interface Callback {
        void generated (Structure structure);
    }

    protected Molecule mol;
    protected Stereo stereo;
    protected Optical optical;

    // stereo annotation
    protected Map<Integer, Integer> stereocenters = 
        new TreeMap<Integer, Integer>();
    protected int stereodefined;
    
    public EnantiomerGenerator () {
    }

    public EnantiomerGenerator (ObjectNode json) {
        setJSON (json);
    }

    public EnantiomerGenerator (Structure struc) {
        if (struc.molfile == null && struc.smiles == null)
            throw new IllegalArgumentException
                ("Structure contains neither molfile nor smiles!");
        try {
            MolHandler mh = new MolHandler ();
            if (struc.molfile != null) {
                mh.setMolecule(struc.molfile);
                mol = mh.getMolecule();
            }
            else {
                mh.setMolecule(struc.smiles);
                mol = mh.getMolecule();
            }
            stereo = struc.stereoChemistry;
            optical = struc.opticalActivity;
        }
        catch (Exception ex) {
            throw new IllegalArgumentException
                ("Not a valid molecule: "+ex.getMessage());
        }
    }

    public void setMol (Molecule mol) { this.mol = mol; }
    public Molecule getMol () { return mol; }
    public void setStereo (Stereo stereo) {
    	this.stereo = stereo; 
    	
    }
    public Stereo getStereo () { return stereo; }
    public Optical getOptical () { return optical; }
    public void setOptical (Optical optical) { this.optical = optical; }

    /* assume json contains at the minimum the following fields:
     {
        molfile: "C1=CC=CC=C1C(C)CCC",
        stereochemistry: "RACEMIC",
        opticalActivity:"(+/-)"
     }
    */
    public void setJSON (ObjectNode json) {
        JsonNode node = json.get("molfile");
        if (node == null) {
            throw new IllegalArgumentException
                ("No field \"molfile\" defined!");
        }
        try {
            MolHandler mh = new MolHandler (node.asText());
            this.mol = mh.getMolecule();
            instrumentation ();
        }
        catch (Exception ex) {
            throw new IllegalArgumentException
                ("Bogus molfile: "+node.asText());
        }

        node = json.get("stereoChemistry");
        if (node == null) {
            throw new IllegalArgumentException
                ("No field \"stereochemistry\" defined!");
        }
        try {
            this.stereo = Stereo.valueOf(node.asText());
        }
        catch (Exception ex) {
            throw new IllegalArgumentException 
                ("Bogus stereochemistry specified: "+node.asText());
        }

        node = json.get("opticalActivity");
        if (node == null) {
            this.optical = Optical.UNSPECIFIED;
        }
        else {                
            String activity = node.asText();
            try {
                int plus = 0, minus = 0;
                if (activity.indexOf('+') >= 0) ++plus;
                if (activity.indexOf('-') >= 0) ++minus;

                if (plus > 0 || minus > 0) {
                    if (plus > 0 && minus > 0)
                        optical = Optical.PLUS_MINUS;
                    else if (plus > 0)
                        optical = Optical.PLUS;
                    else // if (minus > 0)
                        optical = Optical.MINUS;
                }
                else {
                    this.optical = Optical.valueOf(activity);
                }
            }
            catch (Exception ex) {
                throw new IllegalArgumentException 
                    ("Bogus optical activity specified: "+activity);
            }
        }
    }

    void instrumentation () {
        if (mol.getDim() < 2)
            mol.clean(2, null);
        
        stereocenters.clear();
        stereodefined = 0;
        for (int i = 0; i < mol.getAtomCount(); ++i) {
            int chiral = mol.getChirality(i);
            if (chiral == MolAtom.CHIRALITY_R) {
                stereocenters.put(i, chiral);
                ++stereodefined;
            }
            else if (chiral == MolAtom.CHIRALITY_S) {
                stereocenters.put(i, chiral);
                ++stereodefined;
            }
            else if (chiral != 0) {
                stereocenters.put(i, chiral); // either R/S
            }
        }
    }

    public void generate (final Callback callback) {
        if (stereocenters.isEmpty()) {
            Logger.debug("## No stereocenters; nothing to generate...");
            return;
        }

        Logger.debug("## generating enantiomers stereochemistry="+stereo
                     +" optical activity="+optical
                     +" stereocenters "+stereodefined+"/"
                     +stereocenters.size()+" "+stereocenters);

        //switch (stereo.toString()) {
        if(stereo.equals(Stereo.RACEMIC)){
            if (optical == Optical.PLUS_MINUS) {
                if (stereodefined == 0 && stereocenters.size() == 1) {
                    // generate one version each
                    Integer chiral = stereocenters.keySet().iterator().next();
                    Molecule clone = mol.cloneMolecule();
                    clone.setChirality(chiral, MolAtom.CHIRALITY_R);
                    callback.generated(StructureProcessor.instrument(clone));

                    clone = mol.cloneMolecule();
                    clone.setChirality(chiral, MolAtom.CHIRALITY_S);
                    callback.generated(StructureProcessor.instrument(clone));
                }
                else if (stereodefined > 0 
                         && stereodefined == stereocenters.size()) {
                    // generate a version as-is
                    callback.generated(StructureProcessor.instrument(mol)); 

                    // now the flipped version
                    Molecule clone = mol.cloneMolecule();
                    for (Map.Entry<Integer, Integer> me : 
                             stereocenters.entrySet()) {
                        Integer atom = me.getKey();
                        Integer chiral = me.getValue();
                        if (chiral == MolAtom.CHIRALITY_R) {
                            clone.setChirality(atom, MolAtom.CHIRALITY_S);
                        }
                        else { // chiral == MolAtom.CHIRALITY_S 
                            clone.setChirality(atom, MolAtom.CHIRALITY_R);
                        }
                    }
                    callback.generated(StructureProcessor.instrument(clone));
                }
            }
            else { // invalid... do nothing
                throw new IllegalStateException
                    ("RACEMIC stereochemistry with optical activity "+optical
                     +" not supported!");
            }
        }else if(stereo.equals(Stereo.EPIMERIC)){
        	if (stereocenters.size() > 1 
                && stereodefined+1 == stereocenters.size()) {
                Integer atom = null;
                for (Map.Entry<Integer, Integer> me 
                         : stereocenters.entrySet()) {
                    if (me.getValue() != MolAtom.CHIRALITY_R
                        && me.getValue() != MolAtom.CHIRALITY_S) {
                        atom = me.getKey();
                    }
                }

                if (atom == null) {
                    throw new IllegalStateException
                        ("Something's rotten in the state of Maryland!");
                }
                else if (optical == Optical.NONE) {
                    // do nothing
                    return;
                }

                Molecule clone;
                Map props;

                clone = mol.cloneMolecule();
                clone.setChirality(atom, MolAtom.CHIRALITY_R);
                callback.generated(StructureProcessor.instrument(clone));
                
                clone = mol.cloneMolecule();
                clone.setChirality(atom, MolAtom.CHIRALITY_S);
                callback.generated(StructureProcessor.instrument(clone));

                // multiple stereocenters but only one undefined
                if (Optical.PLUS_MINUS == optical) {
                    // generate four instances xxxxxR, xxxxxS, yyyyyR, yyyyyS
                    // where y = ~x

                    // now flip
                    Molecule flip = flip (mol);
                    clone = flip.cloneMolecule();
                    clone.setChirality(atom, MolAtom.CHIRALITY_R);
                    callback.generated(StructureProcessor.instrument(clone));

                    clone = flip.cloneMolecule();
                    clone.setChirality(atom, MolAtom.CHIRALITY_S);
                    callback.generated(StructureProcessor.instrument(clone));
                }
            }
            else {
                // invalid... do nothing
                throw new IllegalStateException
                    ("EPIMERIC stereochemistry with more than 1 "
                     +"stereocenters undefined not supported!");
            }
        }else if(stereo.equals(Stereo.MIXED)){
        	if (optical == Optical.NONE) {
                return;
            }

            final List<Integer> undefs = new ArrayList<Integer>();
            for (Map.Entry<Integer, Integer> me : stereocenters.entrySet()) {
                if (MolAtom.CHIRALITY_R != me.getValue() 
                    && MolAtom.CHIRALITY_S != me.getValue()) {
                    undefs.add(me.getKey());
                }
            }
            
            GrayCode gc = GrayCode.createBinaryGrayCode(undefs.size());
            gc.addObserver(new Observer () {
                    public void update (Observable o, Object arg) {
                        int[] c = (int[])arg;

                        Molecule clone = mol.cloneMolecule();
                        for (int i = 0; i < c.length; ++i) {
                            int chirality = c[i] == 0 ? MolAtom.CHIRALITY_R
                                : MolAtom.CHIRALITY_S;
                            Integer atom = undefs.get(i);
                            clone.setChirality(atom, chirality);
                        }
                        callback.generated
                            (StructureProcessor.instrument(clone));
                    }
                });
            gc.generate();

            if (optical == Optical.PLUS_MINUS && stereodefined > 0) {
                // flip 
                final Molecule flip = flip (mol);
                gc = GrayCode.createBinaryGrayCode(undefs.size());
                gc.addObserver(new Observer () {
                        public void update (Observable o, Object arg) {
                            int[] c = (int[])arg;
                            
                            Molecule clone = flip.cloneMolecule();
                            for (int i = 0; i < c.length; ++i) {
                                int chirality = c[i] == 0 ? MolAtom.CHIRALITY_R
                                    : MolAtom.CHIRALITY_S;
                                Integer atom = undefs.get(i);
                                clone.setChirality(atom, chirality);
                            }
                            callback.generated
                                (StructureProcessor.instrument(clone));
                        }
                    });
                gc.generate();
            }
        }else{
            callback.generated(StructureProcessor.instrument(mol));
        }
    }

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
}