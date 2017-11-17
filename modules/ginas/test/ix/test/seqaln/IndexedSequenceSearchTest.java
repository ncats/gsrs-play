package ix.test.seqaln;

import ix.AbstractGinasServerTest;
import ix.ginas.models.v1.*;
import ix.ginas.modelBuilders.SubstanceBuilder;
import ix.test.server.BrowserSession;
import ix.test.server.RestSession;
import ix.test.server.SubstanceAPI;
import org.junit.Test;

import java.util.List;
import static org.junit.Assert.*;
/**
 * Created by katzelda on 6/30/17.
 */
public class IndexedSequenceSearchTest extends AbstractGinasServerTest{



    @Test
    public void savingSequenceShouldIndexItsSequence(){
        try(RestSession restSession = ts.newRestSession(ts.getAdmin());

        ){
            SubstanceAPI api = restSession.newSubstanceAPI();



            api.submitSubstance(new SubstanceBuilder().asNucleicAcid().addName("foo")
                    .setSubstanceClass(Substance.SubstanceClass.nucleicAcid)
                    .addDnaSubunit("ACGTACGT")
                    .build());


            try(BrowserSession browserSession = restSession.newBrowserSession()){
                SequenceSearchAPI searchAPI = new SequenceSearchAPI(browserSession);
                List<SequenceSearchAPI.SearchResult> result =searchAPI.searchNucleicAcids("ACGTACGT", 0.5);

                assertEquals(1, result.size());
                assertEquals("foo", result.get(0).getName());
            }
        }
    }


}