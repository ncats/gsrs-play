package ix.core.chem;

import java.util.*;

import chemaxon.util.MolHandler;
import chemaxon.struc.Molecule;
import chemaxon.struc.MolAtom;
import ix.core.models.Structure;

import gov.nih.ncgc.chemical.Chemical;
import gov.nih.ncgc.chemical.ChemicalFactory;

import play.Logger;
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
    
    
    public static Molecule fixMetals(Molecule m){
    	for(MolAtom atom:m.getAtomArray()){
    		if(lychi.ElementData.isMetal(atom.getAtno())){
            	atom.setImplicitHcount(0);	
            }
    	}
    	return m;
    }
    
    /**
     * Generate chemical formula by treating disconnected components
     * separately.
     */
    public static String formula (Molecule g) {
        Molecule[] frags = g.cloneMolecule().convertToFrags();
        final Map<String, Integer> formula = new HashMap<String, Integer>();
        for (Molecule m : frags) {
        	fixMetals(m);
        	
            String f = m.getFormula();
            Integer c = formula.get(f);
            formula.put(f, c==null ? 1 : (c+1));
        }
        List<String> order = new ArrayList<String>(formula.keySet());
        StringBuilder sb = new StringBuilder ();
        for (String f : order) {
            Integer c = formula.get(f);
            if (sb.length() > 0) sb.append(".");
            if (c > 1) sb.append(c);
            sb.append(f);
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
			String o=c.export(Chemical.FORMAT_SDF);
		}catch(Exception e){
			Logger.warn("Error exporting molecule", e);
			problem=true;
		}
		return problem;
	}
	
	
	/**
     * Returns true if there is a problem importing or 
     * exporting this chemical object
     * @param c
     * @return
     */
	public static boolean isProblem(String molfile){
		boolean problem = false;
		
		try{
			Chemical c = ChemicalFactory.DEFAULT_CHEMICAL_FACTORY()
					                    .createChemical(molfile, Chemical.FORMAT_AUTO);
			String o=c.export(Chemical.FORMAT_SDF);
		}catch(Exception e){
			Logger.warn("Error exporting molecule", e);
			problem=true;
		}
		return problem;
	}
}
