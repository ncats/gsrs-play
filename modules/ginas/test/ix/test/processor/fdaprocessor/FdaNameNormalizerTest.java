package ix.test.processor.fdaprocessor;

import com.fasterxml.jackson.databind.JsonNode;
import ix.AbstractGinasServerTest;
import ix.core.util.EntityUtils;
import ix.ginas.controllers.v1.SubstanceFactory;
import ix.ginas.modelBuilders.SubstanceBuilder;
import ix.ginas.models.v1.Name;
import ix.ginas.models.v1.Substance;
import ix.ginas.processors.FDANameNormalizer;
import ix.test.SubstanceJsonUtil;
import ix.test.server.GinasTestServer;
import ix.test.server.RestSession;
import ix.test.server.SubstanceAPI;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;
/**
 * Created by katzelda on 6/28/18.
 */
@RunWith(Parameterized.class)
public class FdaNameNormalizerTest {

    @Parameterized.Parameters(name = "{0}")
    public static List<Object[]> getData(){
        List<Object[]> data = new ArrayList<>();

        data.add(new Object[]{"nothingSpecial", "foo", "foo"});
        data.add(new Object[]{"alpha", "bar .ALPHA.baz", "bar \u03b1baz"});
        data.add(new Object[]{"beta", "bar .BETA.baz", "bar \u03b2baz"});
        data.add(new Object[]{"gamma", "bar .GAMMA.baz", "bar \u03b3baz"});
        data.add(new Object[]{"delta", "bar .DELTA.baz", "bar \u03b4baz"});
        data.add(new Object[]{"EPSILON", "bar .EPSILON.baz", "bar \u03b5baz"});
        data.add(new Object[]{"ZETA", "bar .ZETA.baz", "bar \u03b6baz"});
        data.add(new Object[]{"ETA", "bar .ETA.baz", "bar \u03b7baz"});
        data.add(new Object[]{"THETA", "bar .THETA.baz", "bar \u03b8baz"});
        data.add(new Object[]{"IOTA", "bar .IOTA.baz", "bar \u03b9baz"});
        data.add(new Object[]{"KAPPA", "bar .KAPPA.baz", "bar \u03babaz"});
        data.add(new Object[]{"LAMBDA", "bar .LAMBDA.baz", "bar \u03bbbaz"});
        data.add(new Object[]{"MU", "bar .MU.baz", "bar \u03bcbaz"});
        data.add(new Object[]{"NU", "bar .NU.baz", "bar \u03bdbaz"});
        data.add(new Object[]{"XI", "bar .XI.baz", "bar \u03bebaz"});
        data.add(new Object[]{"OMICRON", "bar .OMICRON.baz", "bar \u03bfbaz"});

        data.add(new Object[]{"PI", "bar .PI.baz", "bar \u03c0baz"});
        data.add(new Object[]{"RHO", "bar .RHO.baz", "bar \u03c1baz"});
        data.add(new Object[]{"SIGMA", "bar .SIGMA.baz", "bar \u03c3baz"});
        //skip final sigma
        data.add(new Object[]{"TAU", "bar .TAU.baz", "bar \u03c4baz"});
        data.add(new Object[]{"UPSILON", "bar .UPSILON.baz", "bar \u03c5baz"});
        data.add(new Object[]{"PHI", "bar .PHI.baz", "bar \u03c6baz"});
        data.add(new Object[]{"CHI", "bar .CHI.baz", "bar \u03c7baz"});
        data.add(new Object[]{"PSI", "bar .PSI.baz", "bar \u03c8baz"});
        data.add(new Object[]{"OMEGA", "bar .OMEGA.baz", "bar \u03c9baz"});
        data.add(new Object[]{"+/-", "bar +/-baz", "bar \u00b1baz"});

        return data;
        /*

		".PI.	\u03c0\n" +
		".RHO.	\u03c1\n" +
		".SIGMA.	\u03c3\n" +
		".TAU.	\u03c4\n" +
		".UPSILON.	\u03c5\n" +
		".PHI.	\u03c6\n" +
		".CHI.	\u03c7\n" +
		".PSI.	\u03c8\n" +
		".OMEGA.	\u03c9\n" +
		"+/-	\u00b1\n";

         */
    }

    private final String inputName;
    private final String stdName;

    @ClassRule
    public static GinasTestServer ts = createTestServerWithProcessor();

    public static GinasTestServer createTestServerWithProcessor(){
        GinasTestServer ts = new GinasTestServer();
        ts.addEntityProcessor(GinasTestServer.ConfigOptions.ALL_TESTS, Name.class, FDANameNormalizer.class);
        return ts;
    }

    public FdaNameNormalizerTest( String ignored, String inputName, String stdName){
        this.inputName = inputName;
        this.stdName = stdName;
    }

    @Test
    public void stdNameAddedOnPersistViaPlugin() {
        try (RestSession session = ts.newRestSession(ts.getAdmin())) {
            SubstanceAPI api = new SubstanceAPI(session);

            Substance substance = new SubstanceBuilder()
                    .addName(inputName)
                    .generateNewUUID()
                    .build();

            api.submitSubstance(substance);

            //can't rely on api to fetch json out because it hides the stdName

           assertEquals(stdName,
                   SubstanceFactory.getSubstance(substance.getUuid()).names.get(0).stdName);

        }
    }

    @Test
    public void toFda(){
        assertEquals( inputName.toUpperCase(), FDANameNormalizer.toFDA(stdName));
    }

    @Test
    public void fromFda(){
        assertEquals( stdName, FDANameNormalizer.fromFDA(inputName));
    }
}
