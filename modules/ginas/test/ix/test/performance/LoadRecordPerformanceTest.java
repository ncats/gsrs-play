package ix.test.performance;

import static ix.test.SubstanceJsonUtil.ensurePass;

import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.junit.Test;

import ix.AbstractGinasServerTest;
import ix.core.util.StopWatch;
import ix.ginas.modelBuilders.SubstanceBuilder;
import ix.test.server.RestSession;
import ix.test.server.SubstanceAPI;

public class LoadRecordPerformanceTest extends AbstractGinasServerTest{
	    
    @Test    
   	public void add2000SimpleConcepts() throws Exception {
        //JsonNode entered = parseJsonFile(resource);
        try( RestSession session = ts.newRestSession(ts.getFakeUser1())) {
        	String theName = "Simple Named Concept";
            SubstanceAPI api = new SubstanceAPI(session);
            long time1 = StopWatch.timeElapsed(()->{
	            for(int i=0;i<200;i++){
					new SubstanceBuilder()
						.addName(theName + i)
						.buildJsonAnd(j -> ensurePass(api.submitSubstance(j)));
	            }
            });
            System.out.println("Adding 2000 simple names:" + time1);
        }
   	}
 
    @Test    
   	public void add2000SimpleLinearChemicals() throws Exception {
        //JsonNode entered = parseJsonFile(resource);
        try( RestSession session = ts.newRestSession(ts.getFakeUser1())) {
        	String theName = "Simple Named Chemical";
        	String atoms = "PSCON";
        	
        	
        	
            SubstanceAPI api = new SubstanceAPI(session);
            long time1 = StopWatch.timeElapsed(()->{
	            for(int i=0;i<200;i++){
					new SubstanceBuilder()
						.asChemical()
						.setStructure(toAlphabet(i,atoms))
						.addName(theName + i)
						.buildJsonAnd(j -> ensurePass(api.submitSubstance(j)));
	            }
            });
            System.out.println("Adding 2000 simple chemicals:" + time1);
        }
   	}
    
    /**
     * Just encodes the number "i" in "excel"-like notation,
     * where each char in "alphabet" is cycled through in order.
     * 
     * <p>
     * For example, with alphabet <code>ABCDEFGHIJKLMNOPQRSTUVWXYZ</code>
     * the value at 0 would be "A", and the value at 25 would be "Z". 26
     * would give "AA", and 27 "AB". 
     * </p>
     * 
     * 
     * @param i
     * @param alphabet
     * @return
     */
    public static String toAlphabet(int i,String alphabet){
        return toAlphabet(i, alphabet.toCharArray());
    }
    
    
    public static String toAlphabet(int i,char[] alph){
        int j=i;
        String ret="";
        char c2;
        while(j>=alph.length){
            c2=(alph[(j) % alph.length]);
            ret=c2 + ret;
            j=j/(alph.length)-1;
        }
        return alph[j] + ret;
    
    }
    
    
    /**
     * Creates an infinite stream of all unique strings that can 
     * be created from the chars in the provided alphabet. "Unique"
     * here includes a check that the string is not the reverse
     * of a string previously seen in the stream. In other words,
     * this is an ordered stream of all possible strings,
     * with the later value of non-palindromic strings removed.
     * @param alphabet
     * @return
     */
    public static Stream<String> uniqueReversable(String alphabet){
        char[] abet = alphabet.toCharArray();
        return IntStream.iterate(0, i->i+1)
            .mapToObj(i->toAlphabet(i,abet))
            .filter(s->isLowerOrder(s));
    }
    
    
    private static boolean isLowerOrder(String s){
        StringBuilder sb= new StringBuilder(s);
        return sb.reverse().toString().compareTo(s)>=0;
    }
}
