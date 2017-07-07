package ix.test.seqaln;

import ix.AbstractGinasServerTest;
import ix.ginas.models.v1.*;
import ix.ncats.controllers.App;
import ix.seqaln.SequenceIndexer;
import ix.test.builder.SubstanceBuilder;
import ix.test.server.BrowserSession;
import ix.test.server.RestSession;
import ix.test.server.SubstanceAPI;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
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

            NucleicAcid na = new NucleicAcid();
            Subunit seq = new Subunit();

            seq.sequence = "ACGTACGT";
            na.setSubunits(Arrays.asList(seq));


//            Sets all sugars to ribose (should be simpler)
            Sugar sug= new Sugar();
            sug.setSugar("R");
            sug.setSitesShorthand("1_1-1_" + seq.sequence.length());
            na.setSugars(Collections.singletonList(sug));

//            Sets all Linkages to phosphate (should be simpler)
            Linkage lin= new Linkage();
            lin.setLinkage("P");
            lin.setSitesShorthand("1_1-1_" + ( seq.sequence.length() -1));
            na.setLinkages(Collections.singletonList(lin));


            api.submitSubstance(new SubstanceBuilder().asNucleicAcid().addName("foo").setSubstanceClass(Substance.SubstanceClass.nucleicAcid).setNucleicAcid(na).build());


            try(BrowserSession browserSession = restSession.newBrowserSession()){
                SequenceSearchAPI searchAPI = new SequenceSearchAPI(browserSession);
                List<SequenceSearchAPI.SearchResult> result =searchAPI.searchNucleicAcids("ACGTACGT", 0.5);

                assertEquals(1, result.size());
                assertEquals("foo", result.get(0).getName());
            }
        }
    }
}