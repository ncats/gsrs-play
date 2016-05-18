package ix.ginas.utils;

import java.util.List;

import chemaxon.struc.MolAtom;
import chemaxon.struc.Molecule;
import gov.nih.ncgc.chemical.Chemical;
import gov.nih.ncgc.jchemical.Jchemical;
import ix.core.models.Structure;

//All of the code below should be changed to use
//chemkit eventually


public class ChemUtils {
	
	
	public static void CheckValanace(Structure newstr, List<GinasProcessingMessage> gpm) {
		Chemical c = GinasUtils.structureToChemical(newstr, null);
		Molecule m = Jchemical.makeJchemical(c).getMol();
		m.valenceCheck();
		MolAtom[] mas = m.getAtomArray();

		for (int i = 0; i < mas.length; i++) {
			MolAtom ma = mas[i];
			if (ma.hasValenceError()) {
				GinasProcessingMessage mes = GinasProcessingMessage
						.WARNING_MESSAGE("Valance Error on " + ma.getSymbol() + " atom (" + (i + 1) + ") ");
				gpm.add(mes);
			}
		}
	}
}
