package ix.test.processor;

import static ix.test.SubstanceJsonUtil.ensurePass;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import ix.ginas.models.v1.Substance;
import ix.ginas.processors.UniqueCodeGenerator;
import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import ix.AbstractGinasServerTest;
import ix.core.util.StopWatch;
import ix.ginas.modelBuilders.SubstanceBuilder;
import ix.test.server.GinasTestServer;
import ix.test.server.RestSession;
import ix.test.server.SubstanceAPI;
import play.Configuration;

public class CodeGeneratorTest extends AbstractGinasServerTest{

	@Override
	public GinasTestServer createGinasTestServer(){

		GinasTestServer ts = super.createGinasTestServer();
		ts.addEntityProcessor(GinasTestServer.ConfigOptions.ALL_TESTS, Substance.class, UniqueCodeGenerator.class, "{\n" +
				"               \"codesystem\":\"BDNUM\",\n" +
						"                       \"suffix\":\"AB\",\n" +
						"                       \"length\":10,\n" +
						"                       \"padding\":true\n" +
						"               }");
//		return new GinasTestServer(()->{
//			String addconf="include \"ginas.conf\"\n" +
//					"\n" +
//					"ix.core.entityprocessors +={\n" +
//					"               \"class\":\"ix.ginas.models.v1.Substance\",\n" +
//					"               \"processor\":\"ix.ginas.processors.UniqueCodeGenerator\",\n" +
//					"               \"with\":{\n" +
//					"               \"codesystem\":\"BDNUM\",\n" +
//					"                       \"suffix\":\"AB\",\n" +
//					"                       \"length\":10,\n" +
//					"                       \"padding\":true\n" +
//					"               }\n" +
//					"        }";
//			Config additionalConfig = ConfigFactory.parseString(addconf)
//					.resolve()
//					.withOnlyPath("ix.core.entityprocessors");
//			Map hm = new Configuration(additionalConfig).asMap();
//			System.out.println(hm.toString());
//
//
//			return hm;
//		});

		return ts;
	}




	@Test
	public void generatedUniqueCodesUniquefor400Straight() {
		Set<String> codes = new HashSet<String>();
		Runnable r = ()->{
			try( RestSession session = ts.newRestSession(ts.getFakeUser1())) {
				String theName = "Simple Named Concept";

				SubstanceAPI api = new SubstanceAPI(session);

					for(int i=0;i<200;i++){
						JsonNode jsn = new SubstanceBuilder()
								.addName(theName + i + Math.random())
								.generateNewUUID()
								.buildJson();

						ensurePass(api.submitSubstance(jsn));
						String code=api.fetchSubstanceJsonByUuid(jsn.at("/uuid").asText())
								.at("/codes/0/code").asText();
//						System.out.println("AND ..." + code);
						assertTrue(!codes.contains(code));
						codes.add(code);
					}
			}catch(Throwable t){
				System.out.println("seen codes are " + codes);
				//System.out.println(ts.getApplication().configuration());
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
