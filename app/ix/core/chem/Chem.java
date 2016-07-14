package ix.core.chem;

import java.util.*;

import chemaxon.util.MolHandler;
import chemaxon.struc.Molecule;
import chemaxon.struc.MolAtom;
import ix.core.models.Structure;

/**
 * Chemistry utilities
 */
public class Chem {
    private Chem () {}

    public static void setFormula (Structure struc) {
        try {
            MolHandler mh = new MolHandler ();
            if (struc.molfile != null) {
                mh.setMolecule(struc.molfile);
            }
            else if (struc.smiles != null) {
                mh.setMolecule(struc.smiles);
            }
            else {
                throw new IllegalArgumentException
                    ("Structure contains neither molfile nor smiles!");
            }
            
            struc.formula = formula (mh.getMolecule());
        }
        catch (Exception ex) {
            throw new IllegalArgumentException ("Invalid structure");
        }
    }
    
    /**
     * Generate chemical formula by treating disconnected components
     * seperately.
     */
    public static String formula (Molecule g) {
        Molecule[] frags = g.cloneMolecule().convertToFrags();
        final Map<String, Integer> formula = new HashMap<String, Integer>();
        for (Molecule m : frags) {
            for (MolAtom a : m.getAtomArray()) {
                // get the right H count
                int c = a.getCharge();
                if (c < 0) {
                    a.setCharge(0);
                    a.setImplicitHcount(-c);
                }
            }
            String f = m.getFormula();
            Integer c = formula.get(f);
            formula.put(f, c==null ? 1 : (c+1));
        }
        List<String> order = new ArrayList<String>(formula.keySet());
        Collections.sort(order, new Comparator<String>() {
                public int compare (String s1, String s2) {
                	int d=s2.length() - s1.length();
                    
                    if (d == 0) {
                    	Integer c1 = formula.get(s1);
                        Integer c2 = formula.get(s2);
                        d = c1 - c2;
                        if (d == 0)
                            d = s1.compareTo(s2);
                    }
                    return d;
                }
            });
        StringBuilder sb = new StringBuilder ();
        for (String f : order) {
            Integer c = formula.get(f);
            if (sb.length() > 0) sb.append(".");
            if (c > 1) sb.append(c);
            sb.append(f);
        }
        return sb.toString();
    }
}
