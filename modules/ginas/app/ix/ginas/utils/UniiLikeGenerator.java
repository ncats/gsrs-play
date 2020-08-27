package ix.ginas.utils;

import ix.ginas.controllers.v1.SubstanceFactory;
import ix.ginas.models.v1.Substance;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class UniiLikeGenerator extends RandomAlphaNumericIDGenerator<Substance> implements NamedIdGenerator<Substance,String>{

    public static char[] alphabet = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();
    //GSRS-1658 can not contain only digit characters and "E" characters (`^[0-9E]*$`)
    private static Pattern SCI_NOTATION_PATTERN = Pattern.compile("^[0-9E]+$");
    //GSRS-1658 can not contain month abbreviations which cause problems in excel
    private static Set<String> MONTHS = new HashSet<>(Arrays.asList("JAN", "FEB", "MAR", "APR", "MAY", "JUN", "JUL", "AUG", "SEP", "OCT", "NOV", "DEC"));
    private static List<Character> CONFUSING_CHARS_LIST = Arrays.asList('0', 'O', '1', 'I');

    private static char[] CONFUSING_CHARS_ARRAY;
    static{
        CONFUSING_CHARS_ARRAY = new char[CONFUSING_CHARS_LIST.size()];
        int i=0;
        for(Character c : CONFUSING_CHARS_LIST){
            CONFUSING_CHARS_ARRAY[i++] = c.charValue();
        }
    }


    private static BadWordChecker badWordChecker = new BadWordChecker();

    private static Pattern _4_LETTER_WORD = Pattern.compile("[A-Z]{4}");

    public UniiLikeGenerator(int numRandomChars, boolean addCheckDigit){

        super(alphabet,numRandomChars,addCheckDigit);
    }

    public UniiLikeGenerator(Random random, int numRandomChars, boolean addCheckDigit){

        super(random, alphabet,numRandomChars,addCheckDigit);
    }


    @Override
    public boolean allowID(String randomPart, String fullId) {

        //GSRS-1658 can not contain a "0", "O", "1" or "I" character
        for(int i=0; i< CONFUSING_CHARS_ARRAY.length; i++){
            if(randomPart.indexOf(CONFUSING_CHARS_ARRAY[i])>=0){
                return false;
            }
        }

        if(has4consecutiveLetters(randomPart)){
            return false;
        }
        Matcher sciNotationPattern = SCI_NOTATION_PATTERN.matcher(randomPart);
        if(sciNotationPattern.find()){
            return false;
        }
        for(String m: MONTHS){
            if(randomPart.contains(m)){
                return false;
            }
        }
        if(! badWordChecker.isClean(randomPart)){
            return false;
        }

        Substance dupe= SubstanceFactory.getSubstanceByApprovalID(fullId);
        return dupe==null;

    }



    private boolean has4consecutiveLetters(String s) {
        return _4_LETTER_WORD.matcher(s).find();
    }



    protected abstract String getRandomPartOf(String id);
    @Override
    public boolean isValidId(String id) {
        return this.valid(id) && this.allowID(id, getRandomPartOf(id));
    }

}
