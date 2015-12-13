package ix.ginas.utils;

import ix.ginas.controllers.v1.SubstanceFactory;
import ix.ginas.models.v1.Substance;

public class UNIIGenerator extends RandomAlphaNumericIDGenerator{

	public static char[] alphabet = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();
	public static int idLen=9;
	
	public UNIIGenerator(){
		super(alphabet,idLen,true);
	}


	@Override
	public boolean allowID(String s) {
		Substance dupe=SubstanceFactory.getSubstanceByApprovalID(s);
		if(dupe==null)return true;
		return false;
	}
}