package ix.test.validation;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Optional;

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
        				assertTrue(vr.isValid());
        				
        				Optional<ValidationMessage> ovm=vr.getMessages().stream()
        					.filter(v->v.getMessage().contains("ubunit"))
        					.filter(v->v.getMessageType()==MESSAGE_TYPE.WARNING)
        					.findAny();
        				assertTrue("Should have validation message rejecting 0 subunits",ovm.isPresent());
        				
        				SubstanceJsonUtil.ensurePass( api.submitSubstance(js));
        			});
        }
   	}
    
   
    
}
