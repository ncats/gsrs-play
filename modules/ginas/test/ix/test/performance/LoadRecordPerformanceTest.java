package ix.test.performance;

import static ix.test.SubstanceJsonUtil.ensurePass;

import org.junit.Test;

import ix.core.util.StopWatch;
import ix.test.AbstractGinasServerTest;
import ix.test.builder.SubstanceBuilder;
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
    
    
    //Just convert integer to base {alphabet.length()},
  	//using the chars in alphabet as the digits
  	public static String toAlphabet(int i,String alphabet){
  			int j=i;
  			char[] alph=alphabet.toCharArray();
  			String ret="";
  			while(j>0){
  				ret=(alph[j % alph.length]) + ret;
  				j/=alph.length;
  			}
  			if(ret.length()==0){
  				return ""+alph[0];
  			}
  			return ret;
  	}
}
