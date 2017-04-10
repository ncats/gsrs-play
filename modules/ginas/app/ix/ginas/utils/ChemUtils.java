package ix.ginas.utils;

import java.util.List;

import chemaxon.struc.MolAtom;
import chemaxon.struc.Molecule;
import gov.nih.ncgc.chemical.Chemical;
import gov.nih.ncgc.jchemical.Jchemical;
import ix.core.GinasProcessingMessage;
import ix.core.models.Structure;

import lychi.util.ChemUtil;

//All of the code below should be changed to use
//chemkit eventually

public class ChemUtils {

	/**
	 * Checks for basic valence problems on structure, adding warnings to the
	 * supplied list
	 * 
	 * @param newstr
	 * @param gpm
	 */
	public static void checkValance(Structure newstr, List<GinasProcessingMessage> gpm) {
		Chemical c = newstr.toChemical();
		Molecule m = Jchemical.makeJchemical(c).getMol();
		m.valenceCheck();
		MolAtom[] mas = m.getAtomArray();

		for (int i = 0; i < mas.length; i++) {
			MolAtom ma = mas[i];
			if (ma.hasValenceError()) {
				GinasProcessingMessage mes = GinasProcessingMessage
						.WARNING_MESSAGE("Valence Error on " + ma.getSymbol() + " atom (" + (i + 1) + ") ");
				gpm.add(mes);
			}
		}
	}

	/**
	 * Checks for basic charge balance of structure, warn if not 0
	 * 
	 * @param newstr
	 * @param gpm
	 */
	public static void checkChargeBalance(Structure newstr, List<GinasProcessingMessage> gpm) {
		if (newstr.charge != 0) {
			GinasProcessingMessage mes = GinasProcessingMessage
					.WARNING_MESSAGE("Structure is not charged balanced, net charge of: " + newstr.charge);
			gpm.add(mes);
		}
	}

}
