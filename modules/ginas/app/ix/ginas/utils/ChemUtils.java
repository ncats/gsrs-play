package ix.ginas.utils;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import chemaxon.struc.MolAtom;
import chemaxon.struc.Molecule;
import gov.nih.ncgc.chemical.Chemical;
import gov.nih.ncgc.jchemical.Jchemical;
import ix.core.GinasProcessingMessage;
import ix.core.models.Structure;
import ix.core.models.Structure.Optical;
import ix.core.models.Structure.Stereo;


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
	
	/**
	 * Checks for basic valence problems on structure, adding warnings to the
	 * supplied list
	 * 
	 * @param newstr
	 * @param gpm
	 */
	public static void fixChiralFlag(Structure newstr, List<GinasProcessingMessage> gpm) {
		//Chiral flag should be on if:
		//1. There are defined centers
		//2. It's not explicitly racemic
		//3. It's not explicitly unknown
		//4. The optical activity is not (+/-)
		
		final String chiralFlagOn = "  1";
		final String chiralFlagOff = "  0";
		
		String newChiralFlag= chiralFlagOff;
		
		if(newstr.definedStereo>0){
			if(!Stereo.RACEMIC.equals(newstr.stereoChemistry) && !Stereo.UNKNOWN.equals(newstr.stereoChemistry) ){
				if(!Optical.PLUS_MINUS.equals(newstr.opticalActivity)){
					newChiralFlag=chiralFlagOn;
				}
			}
		}
	
		String[] lines=newstr.molfile.split("\n");
		String chiralFlag=lines[3].substring(12, 14);
		
		if(!newChiralFlag.equals(chiralFlag)){
			lines[3]=lines[3].substring(0, 11) + newChiralFlag +lines[3].substring(15);
			if(newChiralFlag.equals(chiralFlagOn)){
				gpm.add(GinasProcessingMessage.INFO_MESSAGE("Adding chiral flag based on structure information"));
			}else{
				gpm.add(GinasProcessingMessage.INFO_MESSAGE("Removing chiral flag based on structure information"));
			}
			newstr.molfile=Arrays.stream(lines).collect(Collectors.joining("\n"));
		}
		
	}

}
