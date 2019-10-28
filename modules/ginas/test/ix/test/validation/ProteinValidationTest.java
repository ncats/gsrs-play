package ix.test.validation;

import static org.junit.Assert.*;

import java.util.Optional;

import com.fasterxml.jackson.databind.JsonNode;
import ix.core.util.RunOnly;
import ix.ginas.modelBuilders.ProteinSubstanceBuilder;
import ix.ginas.models.v1.Glycosylation;
import ix.ginas.models.v1.ProteinSubstance;
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
	public void changingSequenceChangesDefinitionalHash(){
	String sequence = "AQPAMAQMQLVQSGAEVKKPGASVKLSCKASGYTFSSYWMHWVRQAPGQRLEWMGEINPGNGHTNYNEKFKSRV" +
			"TITVDKSASTAYMELSSLRSEDTAVYYCAKIWGPSLTSPFDYWGQGTLVTVSSGLGGLASTKGPSVFPLAPSSKSTSG" +
			"GTAALGCLVKDYFPEPVTVSWNSGALTSGVHTFPAVLQSSGLYSLSSVVTVPSSSLGTQTYICNVNHKPSNTKVDKRV" +
			"EPKSCDKTHTCPPCPAPELLGGPSVFLFPPKPKDTLMISRTPEVTCVVVDVSHEDPEVKFNWYVDGVEVHNAKTKPRE" +
			"EQYNSTYRVVSVLTVLHQDWLNGKEYKCKVSNKALPAPIEKTISKAKGQPREPQVYTLPPSREEMTKNQVSLTCLVKG" +
			"FYPSDIAVEWESNGQPENNYKTTPPVLDSDGSFFLYSKLTVDKSRWQQGNVFSCSVMHEALHNHYTQKSLSLSPGK";

		ProteinSubstance sub = new ProteinSubstanceBuilder()
				.addName("aName")
				.addSubUnit(sequence)
				.build();

		byte[] oldHash = sub.getDefinitionalElements().getDefinitionalHash();

		sub.protein.getSubunits().get(0).sequence = sequence + "XX";

		byte[] newHash = sub.getDefinitionalElements().getDefinitionalHash();

		assertArrayNotEquals(oldHash, newHash);
	}
	@Test
	public void changingGlycosCSitesChangesDefinitionalHash(){
		String sequence = "AQPAMAQMQLVQSGAEVKKPGASVKLSCKASGYTFSSYWMHWVRQAPGQRLEWMGEINPGNGHTNYNEKFKSRV" +
				"TITVDKSASTAYMELSSLRSEDTAVYYCAKIWGPSLTSPFDYWGQGTLVTVSSGLGGLASTKGPSVFPLAPSSKSTSG" +
				"GTAALGCLVKDYFPEPVTVSWNSGALTSGVHTFPAVLQSSGLYSLSSVVTVPSSSLGTQTYICNVNHKPSNTKVDKRV" +
				"EPKSCDKTHTCPPCPAPELLGGPSVFLFPPKPKDTLMISRTPEVTCVVVDVSHEDPEVKFNWYVDGVEVHNAKTKPRE" +
				"EQYNSTYRVVSVLTVLHQDWLNGKEYKCKVSNKALPAPIEKTISKAKGQPREPQVYTLPPSREEMTKNQVSLTCLVKG" +
				"FYPSDIAVEWESNGQPENNYKTTPPVLDSDGSFFLYSKLTVDKSRWQQGNVFSCSVMHEALHNHYTQKSLSLSPGK";

		ProteinSubstance sub = new ProteinSubstanceBuilder()
				.addName("aName")
				.addSubUnit(sequence)
				.setGlycosylationCSites("1_1-1_" +sequence.length())
				.build();


		byte[] oldHash = sub.getDefinitionalElements().getDefinitionalHash();

		ProteinSubstance sub2 = new ProteinSubstanceBuilder(sub)
									.setGlycosylationCSites("1_1-1_20;1_25-1_100")
				.build();

		byte[] newHash = sub2.getDefinitionalElements().getDefinitionalHash();

		assertArrayNotEquals(oldHash, newHash);
	}

	@Test
	public void changingGlycosOSitesChangesDefinitionalHash(){
		String sequence = "AQPAMAQMQLVQSGAEVKKPGASVKLSCKASGYTFSSYWMHWVRQAPGQRLEWMGEINPGNGHTNYNEKFKSRV" +
				"TITVDKSASTAYMELSSLRSEDTAVYYCAKIWGPSLTSPFDYWGQGTLVTVSSGLGGLASTKGPSVFPLAPSSKSTSG" +
				"GTAALGCLVKDYFPEPVTVSWNSGALTSGVHTFPAVLQSSGLYSLSSVVTVPSSSLGTQTYICNVNHKPSNTKVDKRV" +
				"EPKSCDKTHTCPPCPAPELLGGPSVFLFPPKPKDTLMISRTPEVTCVVVDVSHEDPEVKFNWYVDGVEVHNAKTKPRE" +
				"EQYNSTYRVVSVLTVLHQDWLNGKEYKCKVSNKALPAPIEKTISKAKGQPREPQVYTLPPSREEMTKNQVSLTCLVKG" +
				"FYPSDIAVEWESNGQPENNYKTTPPVLDSDGSFFLYSKLTVDKSRWQQGNVFSCSVMHEALHNHYTQKSLSLSPGK";

		ProteinSubstance sub = new ProteinSubstanceBuilder()
				.addName("aName")
				.addSubUnit(sequence)
				.setGlycosylationOSites("1_1-1_" +sequence.length())
				.build();


		byte[] oldHash = sub.getDefinitionalElements().getDefinitionalHash();

		ProteinSubstance sub2 = new ProteinSubstanceBuilder(sub)
				.setGlycosylationOSites("1_1-1_20;1_25-1_100")
				.build();

		byte[] newHash = sub2.getDefinitionalElements().getDefinitionalHash();

		assertArrayNotEquals(oldHash, newHash);
	}

	@Test
	public void changingGlycosNSitesChangesDefinitionalHash(){
		String sequence = "AQPAMAQMQLVQSGAEVKKPGASVKLSCKASGYTFSSYWMHWVRQAPGQRLEWMGEINPGNGHTNYNEKFKSRV" +
				"TITVDKSASTAYMELSSLRSEDTAVYYCAKIWGPSLTSPFDYWGQGTLVTVSSGLGGLASTKGPSVFPLAPSSKSTSG" +
				"GTAALGCLVKDYFPEPVTVSWNSGALTSGVHTFPAVLQSSGLYSLSSVVTVPSSSLGTQTYICNVNHKPSNTKVDKRV" +
				"EPKSCDKTHTCPPCPAPELLGGPSVFLFPPKPKDTLMISRTPEVTCVVVDVSHEDPEVKFNWYVDGVEVHNAKTKPRE" +
				"EQYNSTYRVVSVLTVLHQDWLNGKEYKCKVSNKALPAPIEKTISKAKGQPREPQVYTLPPSREEMTKNQVSLTCLVKG" +
				"FYPSDIAVEWESNGQPENNYKTTPPVLDSDGSFFLYSKLTVDKSRWQQGNVFSCSVMHEALHNHYTQKSLSLSPGK";

		ProteinSubstance sub = new ProteinSubstanceBuilder()
				.addName("aName")
				.addSubUnit(sequence)
				.setGlycosylationNSites("1_1-1_" +sequence.length())
				.build();


		byte[] oldHash = sub.getDefinitionalElements().getDefinitionalHash();

		ProteinSubstance sub2 = new ProteinSubstanceBuilder(sub)
				.setGlycosylationNSites("1_1-1_20;1_25-1_100")
				.build();

		byte[] newHash = sub2.getDefinitionalElements().getDefinitionalHash();

		assertArrayNotEquals(oldHash, newHash);
	}

	@Test
	public void addingFirstDisulfideLinksChangesDefinitionalHash(){
		String sequence = "AQPAMAQMQLVQSGAEVKKPGASVKLSCKASGYTFSSYWMHWVRQAPGQRLEWMGEINPGNGHTNYNEKFKSRV" +
				"TITVDKSASTAYMELSSLRSEDTAVYYCAKIWGPSLTSPFDYWGQGTLVTVSSGLGGLASTKGPSVFPLAPSSKSTSG" +
				"GTAALGCLVKDYFPEPVTVSWNSGALTSGVHTFPAVLQSSGLYSLSSVVTVPSSSLGTQTYICNVNHKPSNTKVDKRV" +
				"EPKSCDKTHTCPPCPAPELLGGPSVFLFPPKPKDTLMISRTPEVTCVVVDVSHEDPEVKFNWYVDGVEVHNAKTKPRE" +
				"EQYNSTYRVVSVLTVLHQDWLNGKEYKCKVSNKALPAPIEKTISKAKGQPREPQVYTLPPSREEMTKNQVSLTCLVKG" +
				"FYPSDIAVEWESNGQPENNYKTTPPVLDSDGSFFLYSKLTVDKSRWQQGNVFSCSVMHEALHNHYTQKSLSLSPGK";

		ProteinSubstance sub = new ProteinSubstanceBuilder()
				.addName("aName")
				.addSubUnit(sequence)
				.setGlycosylationNSites("1_1-1_" +sequence.length())
				.build();


		byte[] oldHash = sub.getDefinitionalElements().getDefinitionalHash();

		ProteinSubstance sub2 = new ProteinSubstanceBuilder(sub)
				.addDisulfideLink("1_1-1_20;1_25-1_100")
				.build();

		byte[] newHash = sub2.getDefinitionalElements().getDefinitionalHash();

		assertArrayNotEquals(oldHash, newHash);
	}
	@Test
	public void addingSecondDisulfideLinksChangesDefinitionalHash(){
		String sequence = "AQPAMAQMQLVQSGAEVKKPGASVKLSCKASGYTFSSYWMHWVRQAPGQRLEWMGEINPGNGHTNYNEKFKSRV" +
				"TITVDKSASTAYMELSSLRSEDTAVYYCAKIWGPSLTSPFDYWGQGTLVTVSSGLGGLASTKGPSVFPLAPSSKSTSG" +
				"GTAALGCLVKDYFPEPVTVSWNSGALTSGVHTFPAVLQSSGLYSLSSVVTVPSSSLGTQTYICNVNHKPSNTKVDKRV" +
				"EPKSCDKTHTCPPCPAPELLGGPSVFLFPPKPKDTLMISRTPEVTCVVVDVSHEDPEVKFNWYVDGVEVHNAKTKPRE" +
				"EQYNSTYRVVSVLTVLHQDWLNGKEYKCKVSNKALPAPIEKTISKAKGQPREPQVYTLPPSREEMTKNQVSLTCLVKG" +
				"FYPSDIAVEWESNGQPENNYKTTPPVLDSDGSFFLYSKLTVDKSRWQQGNVFSCSVMHEALHNHYTQKSLSLSPGK";

		ProteinSubstance sub = new ProteinSubstanceBuilder()
				.addName("aName")
				.addSubUnit(sequence)
				.addDisulfideLink("1_1-1_20;1_25-1_100")
				.build();


		byte[] oldHash = sub.getDefinitionalElements().getDefinitionalHash();

		ProteinSubstance sub2 = new ProteinSubstanceBuilder(sub)
				.addDisulfideLink("1_1-1_"+sequence.length()) //adding 2nd one not getting rid of first
				.build();

		byte[] newHash = sub2.getDefinitionalElements().getDefinitionalHash();

		assertArrayNotEquals(oldHash, newHash);
	}

	@Test
	public void addingFirstOtherLinksChangesDefinitionalHash(){
		String sequence = "AQPAMAQMQLVQSGAEVKKPGASVKLSCKASGYTFSSYWMHWVRQAPGQRLEWMGEINPGNGHTNYNEKFKSRV" +
				"TITVDKSASTAYMELSSLRSEDTAVYYCAKIWGPSLTSPFDYWGQGTLVTVSSGLGGLASTKGPSVFPLAPSSKSTSG" +
				"GTAALGCLVKDYFPEPVTVSWNSGALTSGVHTFPAVLQSSGLYSLSSVVTVPSSSLGTQTYICNVNHKPSNTKVDKRV" +
				"EPKSCDKTHTCPPCPAPELLGGPSVFLFPPKPKDTLMISRTPEVTCVVVDVSHEDPEVKFNWYVDGVEVHNAKTKPRE" +
				"EQYNSTYRVVSVLTVLHQDWLNGKEYKCKVSNKALPAPIEKTISKAKGQPREPQVYTLPPSREEMTKNQVSLTCLVKG" +
				"FYPSDIAVEWESNGQPENNYKTTPPVLDSDGSFFLYSKLTVDKSRWQQGNVFSCSVMHEALHNHYTQKSLSLSPGK";

		ProteinSubstance sub = new ProteinSubstanceBuilder()
				.addName("aName")
				.addSubUnit(sequence)

				.build();


		byte[] oldHash = sub.getDefinitionalElements().getDefinitionalHash();

		ProteinSubstance sub2 = new ProteinSubstanceBuilder(sub)
				.addOtherLink("linkTypeA", "1_1-1_" +sequence.length())
				.build();

		byte[] newHash = sub2.getDefinitionalElements().getDefinitionalHash();

		assertArrayNotEquals(oldHash, newHash);
	}

	@Test
	public void addingSecondOtherLinksChangesDefinitionalHash(){
		String sequence = "AQPAMAQMQLVQSGAEVKKPGASVKLSCKASGYTFSSYWMHWVRQAPGQRLEWMGEINPGNGHTNYNEKFKSRV" +
				"TITVDKSASTAYMELSSLRSEDTAVYYCAKIWGPSLTSPFDYWGQGTLVTVSSGLGGLASTKGPSVFPLAPSSKSTSG" +
				"GTAALGCLVKDYFPEPVTVSWNSGALTSGVHTFPAVLQSSGLYSLSSVVTVPSSSLGTQTYICNVNHKPSNTKVDKRV" +
				"EPKSCDKTHTCPPCPAPELLGGPSVFLFPPKPKDTLMISRTPEVTCVVVDVSHEDPEVKFNWYVDGVEVHNAKTKPRE" +
				"EQYNSTYRVVSVLTVLHQDWLNGKEYKCKVSNKALPAPIEKTISKAKGQPREPQVYTLPPSREEMTKNQVSLTCLVKG" +
				"FYPSDIAVEWESNGQPENNYKTTPPVLDSDGSFFLYSKLTVDKSRWQQGNVFSCSVMHEALHNHYTQKSLSLSPGK";

		ProteinSubstance sub = new ProteinSubstanceBuilder()
				.addName("aName")
				.addSubUnit(sequence)
				.addOtherLink("linkTypeA", "1_1-1_" +sequence.length())
				.build();


		byte[] oldHash = sub.getDefinitionalElements().getDefinitionalHash();

		ProteinSubstance sub2 = new ProteinSubstanceBuilder(sub)
				.addOtherLink("linkTypeA", "1_1-1_20")
				.build();

		byte[] newHash = sub2.getDefinitionalElements().getDefinitionalHash();

		assertArrayNotEquals(oldHash, newHash);
	}

	private void assertArrayNotEquals(byte[] expecteds, byte[] actuals) {
		try {
			assertArrayEquals(expecteds, actuals);
		} catch (AssertionError e) {
			return;
		}
		fail("The arrays are equal");
	}

	@Test
	public void addingSubunitChangesDefinitionalHash(){
		String sequence = "AQPAMAQMQLVQSGAEVKKPGASVKLSCKASGYTFSSYWMHWVRQAPGQRLEWMGEINPGNGHTNYNEKFKSRV" +
				"TITVDKSASTAYMELSSLRSEDTAVYYCAKIWGPSLTSPFDYWGQGTLVTVSSGLGGLASTKGPSVFPLAPSSKSTSG" +
				"GTAALGCLVKDYFPEPVTVSWNSGALTSGVHTFPAVLQSSGLYSLSSVVTVPSSSLGTQTYICNVNHKPSNTKVDKRV" +
				"EPKSCDKTHTCPPCPAPELLGGPSVFLFPPKPKDTLMISRTPEVTCVVVDVSHEDPEVKFNWYVDGVEVHNAKTKPRE" +
				"EQYNSTYRVVSVLTVLHQDWLNGKEYKCKVSNKALPAPIEKTISKAKGQPREPQVYTLPPSREEMTKNQVSLTCLVKG" +
				"FYPSDIAVEWESNGQPENNYKTTPPVLDSDGSFFLYSKLTVDKSRWQQGNVFSCSVMHEALHNHYTQKSLSLSPGK";

		ProteinSubstance sub = new ProteinSubstanceBuilder()
				.addName("aName")
				.addSubUnit(sequence)
				.build();

		byte[] oldHash = sub.getDefinitionalElements().getDefinitionalHash();



		byte[] newHash = new ProteinSubstanceBuilder(sub).addSubUnit("AAAAAAA").build().getDefinitionalElements().getDefinitionalHash();

		assertArrayNotEquals(oldHash, newHash);
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
