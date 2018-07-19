package ix.ginas.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import chemaxon.struc.MolAtom;
import chemaxon.struc.Molecule;
import gov.nih.ncgc.chemical.Chemical;
import gov.nih.ncgc.jchemical.Jchemical;
import ix.core.validator.GinasProcessingMessage;
import ix.core.models.Structure;
import ix.core.models.Structure.Optical;
import ix.core.models.Structure.Stereo;
import ix.core.validator.ValidatorCallback;
import ix.utils.FortranLikeParserHelper.LineParser;


//All of the code below should be changed to use
//chemkit eventually

public class ChemUtils {

	private static LineParser MOLFILE_COUNT_LINE_PARSER=new LineParser("aaabbblllfffcccsssxxxrrrpppiiimmmvvvvvv");
	private static Pattern NEW_LINE_PATTERN = Pattern.compile("\n");

    /**
     * Checks for basic valence problems on structure, adding warnings to the
     * supplied list
     *
     * @param newstr the new Structure to check; can not be null.
     * @param callback the validation callback to add the warning messages to.
     */
    public static void checkValance(Structure newstr, ValidatorCallback callback){
        List<GinasProcessingMessage> list = new ArrayList<>();
        //we can get away with delegating because there are no
        //apply changes invocations
        checkValance(newstr, list);
        for(GinasProcessingMessage m : list){
            callback.addMessage(m);
        }
    }
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
     * @param callback
     */
    public static void fixChiralFlag(Structure newstr,  ValidatorCallback callback) {
        List<GinasProcessingMessage> gpm = new ArrayList<>();
        fixChiralFlag(newstr, gpm);
        for(GinasProcessingMessage m : gpm){
            callback.addMessage(m);
        }
    }
	/**
	 * Checks for basic chiral flag problems on structure, adding warnings to the
	 * supplied list, and fix the flag.
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
	
		String[] lines=NEW_LINE_PATTERN.split(newstr.molfile);
		if(lines.length < 3){
			//guess it's a smiles? ignore
			return;
		}

		String chiralFlag=lines[3].substring(12, 15);
		
		if(!newChiralFlag.equals(chiralFlag)){
			lines[3]=lines[3].substring(0, 12) + newChiralFlag +lines[3].substring(15);
			if(newChiralFlag.equals(chiralFlagOn)){
				gpm.add(GinasProcessingMessage.INFO_MESSAGE("Adding chiral flag based on structure information"));
			}else{
				gpm.add(GinasProcessingMessage.INFO_MESSAGE("Removing chiral flag based on structure information"));
			}
		}
		lines[3]=MOLFILE_COUNT_LINE_PARSER.standardize(lines[3]);
			newstr.molfile=Arrays.stream(lines).collect(Collectors.joining("\n"));
		
	}

}
