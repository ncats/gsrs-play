package ix.core.chem;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import gov.nih.ncats.molwitch.Atom;
import gov.nih.ncats.molwitch.Chemical;
import ix.core.models.Structure;


import ix.core.util.LogUtil;
import play.Logger;
/**
 * Chemistry utilities
 */
public class Chem {
    private Chem () {}

    public static void setFormula (Structure struc) {
        try {
            struc.formula = formula (struc.toChemical(false));
        }
        catch (Exception ex) {
            LogUtil.error(()->"error computing formula for structure", ex);
        }
    }
    
    public static Chemical RemoveQueryAtomsForPseudoInChI(Chemical c) {
        Chemical chemicalToUse = c;
        if(c.hasQueryAtoms()){
            chemicalToUse = c.copy();
            chemicalToUse.atoms()
                    .filter(Atom::isQueryAtom)
                    .forEach(a->{
                        a.setAtomicNumber(2);
                        a.setMassNumber(6);
                    });
        }
        return chemicalToUse;
    }


    public static Chemical fixMetals(Chemical chemical){
        for(Atom atom : chemical.getAtoms()){
            if(!atom.isQueryAtom() && !atom.isPseudoAtom() && atom.isMetal()){
                atom.setImplicitHCount(0);
            }
    	}

        return chemical;
    }
    
    /**
     * Generate chemical formula by treating disconnected components
     * separately.
     */
//    public static String formula (Molecule g) {
//        Molecule[] frags = g.cloneMolecule().convertToFrags();
//        final Map<String, Integer> formula = new HashMap<String, Integer>();
//        for (Molecule m : frags) {
//        	fixMetals(m);
//
//            String f = m.getFormula();
//            Integer c = formula.get(f);
//            formula.put(f, c==null ? 1 : (c+1));
//        }
//        List<String> order = new ArrayList<String>(formula.keySet());
//        StringBuilder sb = new StringBuilder ();
//        for (String f : order) {
//            Integer c = formula.get(f);
//            if (sb.length() > 0) sb.append(".");
//            if (c > 1) sb.append(c);
//            sb.append(f);
//        }
//        return FormulaInfo.toCanonicalString(sb.toString());
//    }

    /**
     * Generate chemical formula by treating disconnected components
     * separately.
     */
    public static String formula (Chemical g) {
        Iterator<Chemical> iter = g.copy().connectedComponents();
        final Map<String, AtomicInteger> formula = new HashMap<>();
        while(iter.hasNext()){
            Chemical m = iter.next();
            if(m.hasPseudoAtoms() || m.hasQueryAtoms()){
                continue;
            }
        	fixMetals(m);
            String f = m.getFormula();
            formula.computeIfAbsent(f, new Function<String, AtomicInteger>() {
                @Override
                public AtomicInteger apply(String s) {
                    return new AtomicInteger();
        }
            }).incrementAndGet();
        }
        StringBuilder sb = new StringBuilder ();
        for(Map.Entry<String, AtomicInteger> entry : formula.entrySet()){
            int c =entry.getValue().get();
            if(sb.length() > 0){
                sb.append('.');
        }
            if(c > 1) {
                sb.append(c);
        }
            sb.append(entry.getKey());
        }

        return FormulaInfo.toCanonicalString(sb.toString());
    }
    
    /**
     * Returns true if there is a problem exporting this chemical object
     * @param c
     * @return
     */
	public static boolean isProblem(Chemical c){
		boolean problem = false;
		
		try{
            String o=c.toSd();
		}catch(Exception e){
			Logger.warn("Error exporting molecule", e);
			problem=true;
		}
		return problem;
	}

}
