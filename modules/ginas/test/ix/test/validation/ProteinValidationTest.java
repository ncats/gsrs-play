package ix.test.validation;

import static org.junit.Assert.*;
import static org.junit.Assert.assertTrue;

import java.util.Optional;

import com.fasterxml.jackson.databind.JsonNode;
import ix.core.util.RunOnly;
import ix.ginas.modelBuilders.ProteinSubstanceBuilder;
import ix.ginas.models.v1.Subunit;
import org.junit.Test;

import ix.AbstractGinasServerTest;
import ix.core.validator.ValidationMessage;
import ix.core.validator.ValidationMessage.MESSAGE_TYPE;
import ix.ginas.models.v1.Protein;
import ix.ginas.models.v1.Substance.SubstanceDefinitionLevel;
import ix.ginas.models.v1.Substance.SubstanceDefinitionType;
import ix.test.SubstanceJsonUtil;
import ix.ginas.modelBuilders.SubstanceBuilder;
import ix.test.server.RestSession;
import ix.test.server.SubstanceAPI;
import ix.test.server.SubstanceAPI.ValidationResponse;

public class ProteinValidationTest extends AbstractGinasServerTest{

    
    @Test   
   	public void testProteinWithNoSubunitsShouldFailValidation() throws Exception {
        try( RestSession session = ts.newRestSession(ts.getFakeUser1())) {
           SubstanceAPI api = new SubstanceAPI(session);
           new SubstanceBuilder()
        			.asProtein()
        			.setProtein(new Protein())
        			.addName("Just a test")
        			.buildJsonAnd(js->{
        				ValidationResponse vr=api.validateSubstance(js);
        				assertFalse(vr.isValid());
        				
        				Optional<ValidationMessage> ovm=vr.getMessages().stream()
        					.filter(v->v.getMessage().contains("ubunit"))
        					.filter(v->v.isError())
        					.findAny();
        				assertTrue("Should have validation message rejecting 0 subunits",ovm.isPresent());
        				
        				SubstanceJsonUtil.ensureFailure( api.submitSubstance(js));
        			});
        }
   	}
    
    @Test   
   	public void testIncompleteProteinWithNoSubunitsShouldPassValidation() throws Exception {
        try( RestSession session = ts.newRestSession(ts.getFakeUser1())) {
           SubstanceAPI api = new SubstanceAPI(session);
           new SubstanceBuilder()
        			.asProtein()
        			.setDefinition(SubstanceDefinitionType.PRIMARY, SubstanceDefinitionLevel.INCOMPLETE)
        			.setProtein(new Protein())
        			.addName("Just a test")
        			.buildJsonAnd(js->{
        				ValidationResponse vr=api.validateSubstance(js);
						assertTrue(vr.getMessages().toString(), vr.isValid());
        				
        				Optional<ValidationMessage> ovm=vr.getMessages().stream()
        					.filter(v->v.getMessage().contains("ubunit"))
        					.filter(v->v.getMessageType()==MESSAGE_TYPE.WARNING)
        					.findAny();
        				assertTrue("Should have validation message rejecting 0 subunits",ovm.isPresent());
        				
        				SubstanceJsonUtil.ensurePass( api.submitSubstance(js));
        			});
        }
   	}

   	@Test
	public void exactDuplicateSequenceShouldWarn() {
		try (RestSession session = ts.newRestSession(ts.getFakeUser1())) {
			SubstanceAPI api = new SubstanceAPI(session);


			new ProteinSubstanceBuilder()
					.addName("aName")
					.addSubUnit("AQPAMAQMQLVQSGAEVKKPGASVKLSCKASGYTFSSYWMHWVRQAPGQRLEWMGEINPGNGHTNYNEKFKSRV" +
							"TITVDKSASTAYMELSSLRSEDTAVYYCAKIWGPSLTSPFDYWGQGTLVTVSSGLGGLASTKGPSVFPLAPSSKSTSG" +
							"GTAALGCLVKDYFPEPVTVSWNSGALTSGVHTFPAVLQSSGLYSLSSVVTVPSSSLGTQTYICNVNHKPSNTKVDKRV" +
							"EPKSCDKTHTCPPCPAPELLGGPSVFLFPPKPKDTLMISRTPEVTCVVVDVSHEDPEVKFNWYVDGVEVHNAKTKPRE" +
							"EQYNSTYRVVSVLTVLHQDWLNGKEYKCKVSNKALPAPIEKTISKAKGQPREPQVYTLPPSREEMTKNQVSLTCLVKG" +
							"FYPSDIAVEWESNGQPENNYKTTPPVLDSDGSFFLYSKLTVDKSRWQQGNVFSCSVMHEALHNHYTQKSLSLSPGK")
					.buildJsonAnd(js -> SubstanceJsonUtil.ensurePass(api.submitSubstance(js)));


			new ProteinSubstanceBuilder()
					.addName("duplicateName")
					.addSubUnit("AQPAMAQMQLVQSGAEVKKPGASVKLSCKASGYTFSSYWMHWVRQAPGQRLEWMGEINPGNGHTNYNEKFKSRV" +
							"TITVDKSASTAYMELSSLRSEDTAVYYCAKIWGPSLTSPFDYWGQGTLVTVSSGLGGLASTKGPSVFPLAPSSKSTSG" +
							"GTAALGCLVKDYFPEPVTVSWNSGALTSGVHTFPAVLQSSGLYSLSSVVTVPSSSLGTQTYICNVNHKPSNTKVDKRV" +
							"EPKSCDKTHTCPPCPAPELLGGPSVFLFPPKPKDTLMISRTPEVTCVVVDVSHEDPEVKFNWYVDGVEVHNAKTKPRE" +
							"EQYNSTYRVVSVLTVLHQDWLNGKEYKCKVSNKALPAPIEKTISKAKGQPREPQVYTLPPSREEMTKNQVSLTCLVKG" +
							"FYPSDIAVEWESNGQPENNYKTTPPVLDSDGSFFLYSKLTVDKSRWQQGNVFSCSVMHEALHNHYTQKSLSLSPGK")
					.buildJsonAnd(js -> {
						ValidationResponse vr = api.validateSubstance(js);

						assertEquals(1, vr.getMessages().stream()
										.filter(m-> m.getMessageType() == MESSAGE_TYPE.WARNING && m.getMessage().contains("similar sequence"))
								.count());

						assertTrue(vr.getMessages().toString(), vr.isValid());


					});
		}
	}
    
	@Test
	public void subseq100PercentIdentitySequenceShouldWarn() {
		try (RestSession session = ts.newRestSession(ts.getFakeUser1())) {
			SubstanceAPI api = new SubstanceAPI(session);


			new ProteinSubstanceBuilder()
					.addName("aName")
					.addSubUnit("AQPAMAQMQLVQSGAEVKKPGASVKLSCKASGYTFSSYWMHWVRQAPGQRLEWMGEINPGNGHTNYNEKFKSRV" +
							"TITVDKSASTAYMELSSLRSEDTAVYYCAKIWGPSLTSPFDYWGQGTLVTVSSGLGGLASTKGPSVFPLAPSSKSTSG" +
							"GTAALGCLVKDYFPEPVTVSWNSGALTSGVHTFPAVLQSSGLYSLSSVVTVPSSSLGTQTYICNVNHKPSNTKVDKRV" +
							"EPKSCDKTHTCPPCPAPELLGGPSVFLFPPKPKDTLMISRTPEVTCVVVDVSHEDPEVKFNWYVDGVEVHNAKTKPRE" +
							"EQYNSTYRVVSVLTVLHQDWLNGKEYKCKVSNKALPAPIEKTISKAKGQPREPQVYTLPPSREEMTKNQVSLTCLVKG" +
							"FYPSDIAVEWESNGQPENNYKTTPPVLDSDGSFFLYSKLTVDKSRWQQGNVFSCSVMHEALHNHYTQKSLSLSPGK")
					.buildJsonAnd(js -> SubstanceJsonUtil.ensurePass(api.submitSubstance(js)));


			new ProteinSubstanceBuilder()
					.addName("duplicateName")
					.addSubUnit("AQPAMAQMQLVQSGAEVKKPGASVKLSCKASGYTFSSYWMHWVRQAPGQRLEWMGEINPGNGHTNYNEKFKSRV" +
							"TITVDKSASTAYMELSSLRSEDTAVYYCAKIWGPSLTSPFDYWGQGTLVTVSSGLGGLASTKGPSVFPLAPSSKSTSG" +
							"GTAALGCLVKDYFPEPVTVSWNSGALTSGVHTFPAVLQSSGLYSLSSVVTVPSSSLGTQTYICNVNHKPSNTKVDKRV" +
							"EPKSCDKTHTCPPCPAPELLGGPSVFLFPPKPKDTLMISRTPEVTCVVVDVSHEDPEVKFNWYVDGVEVHNAKTKPRE" +
							"EQYNSTYRVVSVLTVLHQDWLNGKEYKCKVSNKALPAPIEKTISKAKGQPREPQVYTLPPSREEMTKNQVSLTCLVKG" +
							"FYPSDIAVEWESNGQPENNYKTTPPVLDSDGSFFLYSKLTVDKSRWQQGNVFSCSVMHEALH")
					.buildJsonAnd(js -> {
						ValidationResponse vr = api.validateSubstance(js);

						assertEquals(1, vr.getMessages().stream()
								.filter(m-> m.getMessageType() == MESSAGE_TYPE.WARNING && m.getMessage().contains("similar sequence"))
								.count());

						assertTrue(vr.getMessages().toString(), vr.isValid());


					});
		}
	}

	@Test
	public void subseq50PercentIdentitySequenceShouleBeOK() {
		try (RestSession session = ts.newRestSession(ts.getFakeUser1())) {
			SubstanceAPI api = new SubstanceAPI(session);


			new ProteinSubstanceBuilder()
					.addName("aName")
					.addSubUnit("AQPAMAQMQLVQSGAEVKKPGASVKLSCKASGYTFSSYWMHWVRQAPGQRLEWMGEINPGNGHTNYNEKFKSRV" +
							"TITVDKSASTAYMELSSLRSEDTAVYYCAKIWGPSLTSPFDYWGQGTLVTVSSGLGGLASTKGPSVFPLAPSSKSTSG" +
							"GTAALGCLVKDYFPEPVTVSWNSGALTSGVHTFPAVLQSSGLYSLSSVVTVPSSSLGTQTYICNVNHKPSNTKVDKRV" +
							"EPKSCDKTHTCPPCPAPELLGGPSVFLFPPKPKDTLMISRTPEVTCVVVDVSHEDPEVKFNWYVDGVEVHNAKTKPRE" +
							"EQYNSTYRVVSVLTVLHQDWLNGKEYKCKVSNKALPAPIEKTISKAKGQPREPQVYTLPPSREEMTKNQVSLTCLVKG" +
							"FYPSDIAVEWESNGQPENNYKTTPPVLDSDGSFFLYSKLTVDKSRWQQGNVFSCSVMHEALHNHYTQKSLSLSPGK")
					.buildJsonAnd(js -> SubstanceJsonUtil.ensurePass(api.submitSubstance(js)));


			new ProteinSubstanceBuilder()
					.addName("duplicateName")
					.addSubUnit("AQPAMAQMQLVQSGAEVKKPGASVKLSCKASGYTFSSYWMHWVRQAPGQRLEWMGEINPGNGHTNYNEKFKSRV" +
							"TITVDKSASTAYMELSSLRSEDTAVYYCAKIWGPSLTSPFDYWGQGTLVTVSSGLGGLASTKGPSVFPLAPSSKSTSG" +
							"GTAALGCLVKDYFPEPVTVSWNSGALTSGVHTFPAVLQSSGLYSLSSVVTVPSSSLGTQTYICNVNHKPSNTKVDKRV")
					.buildJsonAnd(js -> {
						ValidationResponse vr = api.validateSubstance(js);

						assertEquals(0, vr.getMessages().stream()
								.filter(m-> m.getMessageType() == MESSAGE_TYPE.WARNING && m.getMessage().contains("similar sequence"))
								.count());

						assertTrue(vr.getMessages().toString(), vr.isValid());


					});
		}
	}

	@Test
	public void veryDifferentSequenceShouldBeOK() {
		try (RestSession session = ts.newRestSession(ts.getFakeUser1())) {
			SubstanceAPI api = new SubstanceAPI(session);


			new ProteinSubstanceBuilder()
					.addName("aName")
					.addSubUnit("AQPAMAQMQLVQSGAEVKKPGASVKLSCKASGYTFSSYWMHWVRQAPGQRLEWMGEINPGNGHTNYNEKFKSRV" +
							"TITVDKSASTAYMELSSLRSEDTAVYYCAKIWGPSLTSPFDYWGQGTLVTVSSGLGGLASTKGPSVFPLAPSSKSTSG" +
							"GTAALGCLVKDYFPEPVTVSWNSGALTSGVHTFPAVLQSSGLYSLSSVVTVPSSSLGTQTYICNVNHKPSNTKVDKRV" +
							"EPKSCDKTHTCPPCPAPELLGGPSVFLFPPKPKDTLMISRTPEVTCVVVDVSHEDPEVKFNWYVDGVEVHNAKTKPRE" +
							"EQYNSTYRVVSVLTVLHQDWLNGKEYKCKVSNKALPAPIEKTISKAKGQPREPQVYTLPPSREEMTKNQVSLTCLVKG" +
							"FYPSDIAVEWESNGQPENNYKTTPPVLDSDGSFFLYSKLTVDKSRWQQGNVFSCSVMHEALHNHYTQKSLSLSPGK")
					.buildJsonAnd(js -> SubstanceJsonUtil.ensurePass(api.submitSubstance(js)));


			new ProteinSubstanceBuilder()
					.addName("somethingCompletelyDiffewrent")
					.addSubUnit("AAAAAAAAAAAAAAAAAAAAAAAA")
					.buildJsonAnd(js -> {
						ValidationResponse vr = api.validateSubstance(js);

						assertEquals(0, vr.getMessages().stream()
								.filter(m-> m.getMessageType() == MESSAGE_TYPE.WARNING && m.getMessage().contains("similar sequence"))
								.count());

						assertTrue(vr.getMessages().toString(), vr.isValid());


					});
		}
	}

	@Test
	public void subsitutionsChangesDuplicateSequenceShouldWarn() {
		try (RestSession session = ts.newRestSession(ts.getFakeUser1())) {
			SubstanceAPI api = new SubstanceAPI(session);


			new ProteinSubstanceBuilder()
					.addName("aName")
					.addSubUnit("AQPAMAQMQLVQSGAEVKKPGASVKLSCKASGYTFSSYWMHWVRQAPGQRLEWMGEINPGNGHTNYNEKFKSRV" +
							"TITVDKSASTAYMELSSLRSEDTAVYYCAKIWGPSLTSPFDYWGQGTLVTVSSGLGGLASTKGPSVFPLAPSSKSTSG" +
							"GTAALGCLVKDYFPEPVTVSWNSGALTSGVHTFPAVLQSSGLYSLSSVVTVPSSSLGTQTYICNVNHKPSNTKVDKRV" +
							"EPKSCDKTHTCPPCPAPELLGGPSVFLFPPKPKDTLMISRTPEVTCVVVDVSHEDPEVKFNWYVDGVEVHNAKTKPRE" +
							"EQYNSTYRVVSVLTVLHQDWLNGKEYKCKVSNKALPAPIEKTISKAKGQPREPQVYTLPPSREEMTKNQVSLTCLVKG" +
							"FYPSDIAVEWESNGQPENNYKTTPPVLDSDGSFFLYSKLTVDKSRWQQGNVFSCSVMHEALHNHYTQKSLSLSPGK")
					.buildJsonAnd(js -> SubstanceJsonUtil.ensurePass(api.submitSubstance(js)));


			new ProteinSubstanceBuilder()
					.addName("duplicateName")
					.addSubUnit("AQPAMAQMQLVQSGAEVKKPGASVKLSCKASGYTFSSYWMHWVRQAPGQRLEWMGEINPGNGHTNYNEKFKSRV" +
							"TITVDKSASTAYMELSSLRSEDTAVYYCAKIWGPSLTSPFDYWGQGTLVTVSSGLGGLASTKGPSVFPLAPSSKSTSG" +
							"GTAALGCLVKDYFPEPVTVSWNSGALTSGVHTFPAVLQSSGLYSLSSVVTVPSSSLGTQTYICNVNHKPSNTKVDKRV" +
							"EPKSCDKTHTCPPCPAPELLGGPSVFLFPPKPKDTLMISRTPEVTCVVVDVSHEDPEVKFNWYVDGVEVHNAKTKPRE" +
							"EQYNSTYRVVSVLTVLHQDWLNXXXXXXXXXXXXXXXXXEKTISKAKGQPREPQVYTLPPSREEMTKNQVSLTCLVKG" +
							"FYPSDIAVEWESNGQPENNYKTTPPVLDSDGSFFLYSKLTVDKSRWQQGNVFSCSVMHEALHNHYTQKSLSLSPGK")
					.buildJsonAnd(js -> {
						ValidationResponse vr = api.validateSubstance(js);

						assertEquals(vr.getMessages().toString(), 1, vr.getMessages().stream()
								.filter(m-> m.getMessageType() == MESSAGE_TYPE.WARNING && m.getMessage().contains("similar sequence"))
								.count());

						assertTrue(vr.getMessages().toString(), vr.isValid());


					});
		}
	}

	@Test
	public void subunitWithoutReferenceShouldFail() {
		try (RestSession session = ts.newRestSession(ts.getFakeUser1())) {
			SubstanceAPI api = new SubstanceAPI(session);


			Subunit su = new Subunit();
			su.subunitIndex = 1;
			su.sequence = "ACGTACT";

			JsonNode json = new ProteinSubstanceBuilder()
					.addName("aName")
					.addSubUnit(su)
					.buildJson();

			ValidationResponse response = api.validateSubstance(json);
			assertFalse(response.isValid());

			assertTrue(response.getMessages().toString(), response.getMessages()
					.stream().filter(m-> m.isError())
					.filter(m-> m.getMessage().contains("Protein needs at least 1 reference"))
					.findAny()
					.isPresent());

		}

	}
}
