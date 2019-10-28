package ix.test.processor;

import ix.AbstractGinasServerTest;
import ix.ginas.modelBuilders.SubstanceBuilder;
import ix.ginas.models.v1.Substance;
import ix.test.server.RestSession;
import ix.test.server.SubstanceAPI;
import org.junit.Test;

import static org.junit.Assert.*;
public class SubstanceChangeReasonProcessorTest extends AbstractGinasServerTest{

    @Test
    public void newRecordShouldHaveChangeReasonSetToNull(){
        try(RestSession rs = ts.newRestSession(ts.getAdmin())){

            SubstanceAPI api = rs.newSubstanceAPI();

            Substance sub = new SubstanceBuilder()
                    .generateNewUUID()
                    .addName("myName")

                    .build();

            sub.changeReason = "A Change Reason";
            api.submitSubstance(sub);

            Substance fetched = SubstanceBuilder.from(api.fetchSubstanceJsonByUuid(sub.uuid)).build();

            assertNull(fetched.changeReason);
        }
    }

    @Test
    public void updatedRecordShouldHaveChangeReason(){
        try(RestSession rs = ts.newRestSession(ts.getAdmin())){

            SubstanceAPI api = rs.newSubstanceAPI();

            Substance sub = new SubstanceBuilder()
                    .generateNewUUID()
                    .addName("myName")

                    .build();

            api.submitSubstance(sub);

            Substance fetched = SubstanceBuilder.from(api.fetchSubstanceJsonByUuid(sub.uuid)).build();

            assertNull(fetched.changeReason);

            fetched.changeReason = "testing change";

            api.updateSubstance(fetched.toFullJsonNode());

            Substance fetched2 = SubstanceBuilder.from(api.fetchSubstanceJsonByUuid(sub.uuid)).build();

            assertEquals("testing change", fetched2.changeReason);
        }
    }
}
