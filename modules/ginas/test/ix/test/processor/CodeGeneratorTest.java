package ix.test.processor;

import static ix.test.SubstanceJsonUtil.ensurePass;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import org.junit.Rule;
import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import ix.core.util.ExpectFailureChecker.ExpectedToFail;
import ix.core.util.StopWatch;
import ix.test.builder.SubstanceBuilder;
import ix.test.ix.test.server.GinasTestServer;
import ix.test.ix.test.server.RestSession;
import ix.test.ix.test.server.SubstanceAPI;
import play.Configuration;

public class CodeGeneratorTest {
	
	 @Rule
	 public GinasTestServer ts = new GinasTestServer(()->{
		 	Config additionalConfig = ConfigFactory.parseFile(new File("conf/ginas-test.conf"))
		 				.resolve()
		 				.withOnlyPath("ix.core.entityprocessors");
		 	return new Configuration(additionalConfig).asMap();
	 });
	 
	 
	 
	 @Test
	 public void generatedUniqueCodesUniquefor400Straight() {
		 Set<String> codes = new HashSet<String>();
		 Runnable r = ()->{
		 	try( RestSession session = ts.newRestSession(ts.getFakeUser1())) {
	        	String theName = "Simple Named Concept";
	        	
	            SubstanceAPI api = new SubstanceAPI(session);
	            long time1 = StopWatch.timeElapsed(()->{
		            for(int i=0;i<200;i++){
						JsonNode jsn = new SubstanceBuilder()
							.addName(theName + i + Math.random())
							.generateNewUUID()
							.buildJson();
						
						ensurePass(api.submitSubstance(jsn));
						String code=api.fetchSubstanceJsonByUuid(jsn.at("/uuid").asText())
										.at("/codes/0/code").asText();
						//System.out.println("AND ..." + code);
						assertTrue(!codes.contains(code));
						codes.add(code);
		            }
	            });
	        }catch(Throwable t){
	        	t.printStackTrace();
	        	throw t;
	        }
		 };
		 r.run();
		 ts.stop(true);
		 ts.start();
		 r.run();
	 }
	 
	 @Test
	 public void generateUniqueCodeWithDifferentLengthSeedGivesUniqueCodesAfterRestart() {
		 Set<String> codes = new HashSet<String>();
			try( RestSession session = ts.newRestSession(ts.getFakeUser1())) {
	        	
	            SubstanceAPI api = new SubstanceAPI(session);
	            
						JsonNode jsn = new SubstanceBuilder()
							.addName("seed start")
							.addCode("BDNUM","1232AB")
							.generateNewUUID()
							.buildJson();
						
						ensurePass(api.submitSubstance(jsn));
						String code=api.fetchSubstanceJsonByUuid(jsn.at("/uuid").asText())
										.at("/codes/0/code").asText();
						//System.out.println("AND ..." + code);
						assertTrue(!codes.contains(code));
						codes.add(code);
	            
	        }catch(Throwable t){
	        	t.printStackTrace();
	        	throw t;
	        }
		 Runnable r = ()->{
		 	try( RestSession session = ts.newRestSession(ts.getFakeUser1())) {
	        	String theName = "Simple Named Concept";
	        	
	            SubstanceAPI api = new SubstanceAPI(session);
	            long time1 = StopWatch.timeElapsed(()->{
		            for(int i=0;i<200;i++){
						JsonNode jsn = new SubstanceBuilder()
							.addName(theName + i + Math.random())
							.generateNewUUID()
							.buildJson();
						
						ensurePass(api.submitSubstance(jsn));
						String code=api.fetchSubstanceJsonByUuid(jsn.at("/uuid").asText())
										.at("/codes/0/code").asText();
						assertTrue(!codes.contains(code));
						codes.add(code);
		            }
	            });
	        }catch(Throwable t){
	        	t.printStackTrace();
	        	throw t;
	        }
		 };
		 r.run();
		 ts.stop(true);
		 ts.start();
		 r.run();
	 }
	 
	 
}
