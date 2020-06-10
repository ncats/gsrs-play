package ix.ginas.utils;

import ix.ginas.controllers.v1.SubstanceFactory;
import ix.ginas.models.v1.Substance;

import java.util.regex.Pattern;

public class UNIIGenerator extends RandomAlphaNumericIDGenerator<Substance>{

	public static char[] alphabet = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();
	public static int idLen=9;

	private static BadWordChecker badWordChecker = new BadWordChecker();

	private static Pattern _4_LETTER_WORD = Pattern.compile("[A-Z]{4}");

	public UNIIGenerator(){
		super(alphabet,idLen,true);
	}


	@Override
	public boolean allowID(String s) {
		Substance dupe=SubstanceFactory.getSubstanceByApprovalID(s);
		if(dupe!=null){
			//already exists in database
			return false;
		}
		if(has4consecutiveLetters(s)){
			return false;
		}
		return badWordChecker.isClean(s);
	}



	private boolean has4consecutiveLetters(String s) {
		return _4_LETTER_WORD.matcher(s).find();
	}




	@Override
	public boolean isValidId(String id) {
		return this.valid(id) && this.allowID(id);
	}


}