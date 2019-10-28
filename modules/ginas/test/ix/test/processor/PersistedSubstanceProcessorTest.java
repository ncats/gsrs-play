package ix.test.processor;

import com.fasterxml.jackson.databind.JsonNode;
import ix.AbstractGinasServerTest;
import ix.core.EntityProcessor;
import ix.core.util.RunOnly;
import ix.ginas.modelBuilders.SubstanceBuilder;
import ix.ginas.models.v1.Name;
import ix.ginas.models.v1.Substance;
import ix.ginas.processors.PersistedSubstanceProcessor;
import ix.test.SubstanceJsonUtil;
import ix.test.server.GinasTestServer;
import ix.test.server.RestSession;
import ix.test.server.SubstanceAPI;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.HashSet;
import java.util.stream.Collectors;

/**
 * Created by katzelda on 12/19/17.
 */
public class PersistedSubstanceProcessorTest extends AbstractGinasServerTest{




    @Test
    public void newSubstanceShouldCallNew() {

        try(RestSession session = ts.newRestSession(ts.getAdmin())){
            SubstanceAPI api = session.newSubstanceAPI();

            Substance s = new SubstanceBuilder()
                    .addName("aName")
                    .build();
            api.submitSubstance(s);
            assertEquals(1, PersistedSubstanceProcessorTestDouble.timesNewCalled);
            assertEquals(0, PersistedSubstanceProcessorTestDouble.timesUpdatedCalled);
            assertNotNull(PersistedSubstanceProcessorTestDouble.lastNewSubstance);
        }



    }


    @Override
    public GinasTestServer createGinasTestServer() {
        GinasTestServer ts = super.createGinasTestServer();
        ts.addEntityProcessor(GinasTestServer.ConfigOptions.ALL_TESTS,
                new GinasTestServer.EntityProcessorConfig.Builder(Substance.class, PersistedSubstanceProcessorTestDouble.class)
                        .build());

        return ts;
    }

    @Test
    public void update() {

//        ts.stop(true);
//        ts.addEntityProcessor(Substance.class, PersistedSubstanceProcessorTestDouble.class);
//
//        ts.start();

        try(RestSession session = ts.newRestSession(ts.getAdmin())){
            SubstanceAPI api = session.newSubstanceAPI();

            Substance s = new SubstanceBuilder()
                    .addName("aName")
                    .generateNewUUID()
                    .build();
            api.submitSubstance(s);

            System.out.println("=======================================");
            JsonNode js =api.fetchSubstanceJsonByUuid(s.getUuid().toString());

            SubstanceBuilder.from(js)
                    .addName("secondNme")
//                            .setVersion(2)
                    .buildJsonAnd(js2-> SubstanceJsonUtil.ensurePass(api.updateSubstance(js2)));




            assertEquals(new HashSet<>(Arrays.asList("aName", "secondNme")),
                    SubstanceBuilder.from(api.fetchSubstanceJsonByUuid(s.getUuid().toString()))
                            .build()
                            .names.stream()
                            .map(Name::getName).collect(Collectors.toSet()));


//            js =api.fetchSubstanceJsonByUuid(s.getUuid().toString());
//
//            SubstanceBuilder.from(js)
//                    .addName("thirdName")
////                            .setVersion(2)
//                    .buildJsonAnd(js2-> SubstanceJsonUtil.ensurePass(api.updateSubstance(js2)));

            assertEquals(1, PersistedSubstanceProcessorTestDouble.timesNewCalled);
            assertEquals(1, PersistedSubstanceProcessorTestDouble.timesUpdatedCalled);

            assertEquals(new HashSet<>(Arrays.asList("aName", "secondNme")),
                    PersistedSubstanceProcessorTestDouble.lastUpdatedNew.names.stream()
                            .map(Name::getName).collect(Collectors.toSet()));

            assertEquals(new HashSet<>(Arrays.asList("aName")),
                    PersistedSubstanceProcessorTestDouble.lastUpdatedOld.names.stream()
                            .map(Name::getName).collect(Collectors.toSet()));

        }


    }
    public static class PersistedSubstanceProcessorTestDouble extends PersistedSubstanceProcessor {

        public static int timesNewCalled=0;
        public static int timesUpdatedCalled=0;
        public static Substance lastNewSubstance=null;
        public static Substance lastUpdatedOld, lastUpdatedNew;

        public PersistedSubstanceProcessorTestDouble(){
            timesNewCalled=0;
            timesUpdatedCalled=0;
            lastNewSubstance=null;
            lastUpdatedOld=null;
            lastUpdatedNew=null;

        }

        @Override
        protected void handleNewSubstance(Substance substance) {
            timesNewCalled++;
            lastNewSubstance = substance;
        }

        @Override
        protected void handleUpdatedSubstance(Substance oldSubstance, Substance newSubstance) {
            timesUpdatedCalled++;
            lastUpdatedOld=oldSubstance;
            lastUpdatedNew = newSubstance;
        }
    }
}

