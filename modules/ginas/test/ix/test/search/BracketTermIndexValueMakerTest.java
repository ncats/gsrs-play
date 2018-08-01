package ix.test.search;

import ix.ginas.indexers.BracketTermIndexValueMaker;
import ix.ginas.modelBuilders.SubstanceBuilder;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.*;
/**
 * Created by VenkataSaiRa.Chavali on 6/30/2017.
 */
public class BracketTermIndexValueMakerTest {
    @Test
    public void noTerms() {
        SubstanceBuilder sb = new SubstanceBuilder();
        sb.addName("gibber");
        BracketTermIndexValueMaker maker = new BracketTermIndexValueMaker();
        Map<String,List<String>> map = new HashMap<>();
        maker.createIndexableValues(sb.build(),
                indexableValue -> map.computeIfAbsent(indexableValue.name(), k-> new ArrayList<String>())
                                                    .add((String)indexableValue.value()));

        assertTrue(map.isEmpty());
    }


    @Test
    public void oneTerms() {
        SubstanceBuilder sb = new SubstanceBuilder();
        sb.addName("gibber [ISH]");
        BracketTermIndexValueMaker maker = new BracketTermIndexValueMaker();
        Map<String,List<String>> map = new HashMap<>();
        maker.createIndexableValues(sb.build(),
                indexableValue -> map.computeIfAbsent(indexableValue.name(), k-> new ArrayList<String>())
                        .add((String)indexableValue.value()));

        Map<String, List<String>> expected = new HashMap<>();
        expected.put("GInAS Tag", Arrays.asList("ISH"));

        assertEquals(expected, map);
    }

    @Test
    public void twoTerms() {
        SubstanceBuilder sb = new SubstanceBuilder();
        sb.addName("gibber [ISH]");
        sb.addName("gibber [ESH] [MESH]");
        BracketTermIndexValueMaker maker = new BracketTermIndexValueMaker();
        Map<String,List<String>> map = new HashMap<>();
        maker.createIndexableValues(sb.build(),
                indexableValue -> map.computeIfAbsent(indexableValue.name(), k-> new ArrayList<String>())
                        .add((String)indexableValue.value()));

        Map<String, List<String>> expected = new HashMap<>();
        expected.put("GInAS Tag", Arrays.asList("ISH","ESH","MESH"));

        assertEquals(expected, map);
    }
}
