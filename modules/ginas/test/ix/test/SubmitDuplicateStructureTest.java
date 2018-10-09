package ix.test;

import com.fasterxml.jackson.databind.JsonNode;

import ix.AbstractGinasServerTest;
import ix.core.validator.ValidationMessage;
import ix.core.models.Group;
import ix.core.models.Role;
import ix.ginas.models.v1.ChemicalSubstance;
import ix.ginas.modelBuilders.ChemicalSubstanceBuilder;
import ix.ginas.modelBuilders.SubstanceBuilder;
import ix.ginas.models.v1.Name;
import ix.test.server.GinasTestServer;
import ix.test.server.RestSession;
import ix.test.server.SubstanceAPI;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import util.json.JsonUtil;

import java.io.File;
import java.util.*;

import static org.junit.Assert.*;

import static ix.test.SubstanceJsonUtil.ensurePass;

/**
 * Test of FDA GSRS-86:
 * SuperDataEntry and SuperUpdater roles should be the only ones to be able to override a duplicate warning.
 *
 * This really only applies to duplicate structures.
 *
 * The way we solve this requirement is only users with those Roles will get duplicate structures
 * as a Warning, which they will be able to dismiss/ allow through the UI. While other users will
 * get Errors instead which can't be dismissed.
 *
 * Created by katzelda on 9/9/16.
 */
@RunWith(Parameterized.class)
public class SubmitDuplicateStructureTest extends AbstractGinasServerTest {

    @Parameterized.Parameters(name = "{0}")
    public static List<Object[]> getData(){
        List<Object[]> list = new ArrayList<>();

        for(Role role : Role.values()){
            Set<Role> roles = EnumSet.of(Role.DataEntry);
            ValidationMessage.MESSAGE_TYPE messageType = null;
            if(role == Role.SuperDataEntry || role == Role.SuperUpdate){
                messageType = ValidationMessage.MESSAGE_TYPE.WARNING;
                roles.add(role);
            }else{
                messageType = ValidationMessage.MESSAGE_TYPE.ERROR;
            }
            if(messageType !=null) {
                list.add(new Object[]{roles, messageType});
            }
        }
        return list;
    }

    private SubstanceAPI api;
    private RestSession session;


    GinasTestServer.User admin, peon;


    private final ValidationMessage.MESSAGE_TYPE expectedType;
    private final Set<Role> roleUnderTest;

    private String smiles;


    public SubmitDuplicateStructureTest(Set<Role> role, ValidationMessage.MESSAGE_TYPE type){
        this.roleUnderTest = role;
        this.expectedType = type;
    }
    @Before
    public void login(){

        admin = ts.createAdmin("vader", "password");

        session = ts.newRestSession(admin);

        api = new SubstanceAPI(session);

        smiles = submitStructure();
    }

    private String submitStructure(){
        File f = new File("test/testJSON/pass/aspirin1.json");

        JsonNode inputJs = SubstanceJsonUtil.prepareUnapprovedPublic(JsonUtil.parseJsonFile(f));

        ChemicalSubstanceBuilder builder = SubstanceBuilder.from(inputJs);

        Set<Group> groups = new HashSet<>();
        groups.add( new Group("fake"));
        builder.setAccess(groups);

//        builder.modifyNames(names -> names.forEach( n-> n.setAccess(Collections.emptySet())));
        JsonNode js = builder.buildJson();
        SubstanceAPI.ValidationResponse validationResponse = api.validateSubstance(js);


        assertTrue(validationResponse.getMessages().toString(), validationResponse.isValid());

        ensurePass(api.submitSubstance(js));

        ChemicalSubstance submittedSubstance = api.fetchSubstanceObjectByUuid("8798e4b8-223c-4d24-aeeb-1f3ca2914328", ChemicalSubstance.class);



        return submittedSubstance.structure.smiles;

    }





    @Test
    public void submitSameStructureTwiceAsPeonShouldBeError() throws Throwable{



        ChemicalSubstanceBuilder builder = new SubstanceBuilder().asChemical()
                .setStructure(smiles);

        //  builder.setName("differentName");
        builder.generateNewUUID();
        JsonNode js = builder.buildJson();

        GinasTestServer.User newUser = ts.createUser(roleUnderTest);
        try(RestSession peonSession = ts.newRestSession(newUser)) {
            SubstanceAPI peonApi = new SubstanceAPI(peonSession);


            SubstanceAPI.ValidationResponse validationResponse = peonApi.validateSubstance(js);
            ValidationMessage duplicateMessage = getDuplicateSubstructureMessage(validationResponse);


            assertEquals(expectedType, duplicateMessage.getMessageType());
        }

    }

    private ValidationMessage getDuplicateSubstructureMessage(SubstanceAPI.ValidationResponse validationResponse) throws Throwable{
        return validationResponse.getMessages().stream()
                        .filter(m -> m.getMessage().contains("Structure has 1 possible duplicate"))
                        .findAny()
                        .orElseThrow(() -> {
                            throw new AssertionError("no duplicate structure message in " + validationResponse.getMessages());
                        });
    }
}
