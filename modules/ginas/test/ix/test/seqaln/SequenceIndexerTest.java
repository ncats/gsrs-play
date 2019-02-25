package ix.test.seqaln;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.junit.*;
import org.junit.rules.TemporaryFolder;
import org.junit.rules.TestRule;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;

import ix.AbstractGinasTest;
import ix.core.util.RunOnly;
import ix.core.util.StopWatch;
import ix.core.util.StreamUtil;
import ix.seqaln.SequenceIndexer;
import ix.seqaln.SequenceIndexer.CachedSup;
import ix.seqaln.SequenceIndexer.CutoffType;
import ix.seqaln.SequenceIndexer.Result;
import ix.utils.Tuple;
import net.sf.ehcache.CacheManager;

/**
 * Created by katzelda on 3/30/16.
 */
public class SequenceIndexerTest extends AbstractGinasTest{


    private static final double DELTA = 0.001D;

    @Rule
    public TestRule watcher = new TestWatcher() {
        protected void starting(Description description) {
            System.out.println("Starting test: " + getClass().getCanonicalName() + " . " + description.getMethodName());
        }

        @Override
        protected void finished(Description description) {
            System.out.println("Ending test: " + getClass().getCanonicalName() + " . " + description.getMethodName());
        }
    };

    @Rule
    public TemporaryFolder tmpDir = new TemporaryFolder();


    private SequenceIndexer indexer;

    @Before
    public void setUp() throws IOException{
        clearCache();
        CachedSup.resetAllCaches();

        indexer = SequenceIndexer.open(tmpDir.newFolder());
    }

    @After
    public void tearDown(){
        indexer.shutdown();
        clearCache();
    }

    private void clearCache(){
        CacheManager.getInstance().clearAll();
    }


    @Test
    public void noRecordsInIndex(){
        SequenceIndexer.ResultEnumeration results = indexer.search("ATCGATCG", 1,CutoffType.GLOBAL, "NucleicAcid");
        assertFalse(results.hasMoreElements());
    }

    @Test
    public void aFewSimpleTestsForContains() throws IOException{
        String q = "ABCDEFG";

        indexer.addAminoAcidSequence("1", "ABCDEFG");
        indexer.addAminoAcidSequence("2", "ABCDEFGX");
        indexer.addAminoAcidSequence("3", "ABCDEFGXX");
        indexer.addAminoAcidSequence("4", "ABCDEFGXXXXXXXXXXXXXXXXXXXXXXXXX");
        indexer.addAminoAcidSequence("5", "XXXXXXXABCDEFGXXXXXXXXXXXXXXXXXX");
        //indexer.setUseFingerprint(false);
        SequenceIndexer.ResultEnumeration results = indexer.search(q, 1, CutoffType.SUB, "Protein");
        List<Result> res=StreamUtil.forEnumeration(results).collect(Collectors.toList());

        assertEquals(5,res.size());

    }



    @Test
    public void aFewMoreSimpleTestsForContains() throws IOException{
        String q = "ABCDEFGHINKLMNOP";

        indexer.addAminoAcidSequence("2", "ABCDEFGXHINKLMNOP");
        indexer.addAminoAcidSequence("3", "ABCDEFGXHINKLMNOPXXXXXXX");
        indexer.addAminoAcidSequence("4", "XXXXXXABCDEFGXHINKLMNOPXXXXXXX");
        indexer.addAminoAcidSequence("5", "ABCDEFGHINKXLMNOP");
        indexer.addAminoAcidSequence("6", "ABCDEFGHINKXXXXXXXXXXXXXXXXXXXXXXXXXXXXLMNOP");


        //indexer.setUseFingerprint(false);
        SequenceIndexer.ResultEnumeration results = indexer.search(q, 1, CutoffType.SUB, "Protein");
        List<Result> res=StreamUtil.forEnumeration(results).collect(Collectors.toList());

        assertEquals(0,res.size());

        results = indexer.search(q, .9, CutoffType.SUB, "Protein");
        res=StreamUtil.forEnumeration(results).collect(Collectors.toList());

        assertEquals(4,res.size());

    }

    @Test
    public void lowIdentityShouldNotGlobalHaveHit() throws IOException {
        String seq = "ACGTTTGCCG";
        String rev  = "TGCAAACGGA"; // rev comp
        indexer.addNucleicAcidSequence("foo", seq);

        SequenceIndexer.ResultEnumeration results = indexer.search(rev, .9, CutoffType.GLOBAL, "NucleicAcid");
        assertFalse(results.hasMoreElements());
    }
    @Test
    public void commonSubsequenceShouldHaveLocalHit() throws IOException {
        String seq =  "KKKKKKKKACGTACGTKSSSSSSSSS";
        String rev  = "ACGTACGTWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWW";
        indexer.addNucleicAcidSequence("foo", seq);

        SequenceIndexer.ResultEnumeration results = indexer.search(rev, .9, CutoffType.LOCAL, "NucleicAcid");
        assertTrue(results.hasMoreElements());
    }
    @Test
    public void commonSubsequenceShouldHaveNoSubHit() throws IOException {
        String seq =  "KKKKKKKKACGTACGTKSSSSSSSSS";
        String rev  = "ACGTACGTWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWW";
        indexer.addAminoAcidSequence("foo", seq);

        SequenceIndexer.ResultEnumeration results = indexer.search(rev, .9, CutoffType.SUB, "Protein");
        assertFalse(results.hasMoreElements());
    }

    @Test
    public void perfectSubQueryShouldHavePerfectSubHit() throws IOException {
        String q =  "ACGTACGT"; //contained below
        String t  = "WWW"+
                "WWW"+
                "WWW"+
                "WWW"+
                "WWW"+
                "WWW"+
                "WWW"+
                "WWW"+
                "WWW"+
                "ACGTACGT" + //There it is!
                "WWW"+
                "WWW"+
                "WWW"+
                "WWW"+
                "WWW";
        indexer.addNucleicAcidSequence("foo", t);

        SequenceIndexer.ResultEnumeration results = indexer.search(q, 1, CutoffType.SUB, "NucleicAcid");
        assertTrue(results.hasMoreElements());
    }

    @Test
    public void perfectSuperQueryShouldHavePerfectLocalHit() throws IOException {
        String q = "MRLAVGALLVCAVLGLCLAVPDKTVRWCAVSEHEATKCQSFRDHMKSVIPSDGP"
                +"SVACVKKASYLDCIRAIAANEADAVTLDAGLVYDAYLAPNNLKPVVAEFYGSKEDPQT"
                +"FYYAVAVVKKDSGFQMNQLRGKKSCHTGLGRSAGWNIPIGLLYCDLPEPRKPLEKAVA"
                +"NFFSGSCAPCADGTDFPQLCQLCPGCGCSTLNQYFGYSGAFKCLKDGAGDVAFVKHST"
                +"IFENLANKADRDQYELLCLDNTRKPVDEYKDCHLAQVPSHTVVARSMGGKEDLIWELL"
                +"NQAQEHFGKDKSKEFQLFSSPHGKDLLFKDSAHGFLKVPPRMDAKMYLGYEYVTAIRN"
                +"LREGTCPEAPTDECKPVKWCALSHHERLKCDEWSVNSVGKIECVSAETTEDCIAKIMN"
                +"GEADAMSLDGGFVYIAGKCGLVPVLAENYNKSDNCEDTPEAGYFAVAVVKKSASDLTW"
                +"DNLKGKKSCHTAVGRTAGWNIPMGLLYNKINHCRFDEFFSEGCAPGSKKDSSLCKLCM"
                +"GSGLNLCEPNNKEGYYGYTGAFRCLVEKGDVAFVKHQTVPQNTGGKNPDPWAKNLNEK"
                +"DYELLCLDGTRKPVEEYANCHLARAPNHAVVTRKDKEACVHKILRQQQHLFGSNVTDC"
                +"SGNFCLFRSETKDLLFRDDTVCLAKLHDRNTYEKYLGEEYVKAVGNLRKCSTSSLLEA"
                +"CTFRRP";
        String t  = "RDQYELLCLDNTRKPVDEYKDCH"; //contained above
        indexer.addAminoAcidSequence("foo", t);

        SequenceIndexer.ResultEnumeration results = indexer.search(q, 1, CutoffType.LOCAL, "Protein");
        assertTrue(results.hasMoreElements());
    }
    @Test
    public void perfectSuperQueryShouldHaveNoSubHit() throws IOException {
        String q = "MRLAVGALLVCAVLGLCLAVPDKTVRWCAVSEHEATKCQSFRDHMKSVIPSDGP"
                +"SVACVKKASYLDCIRAIAANEADAVTLDAGLVYDAYLAPNNLKPVVAEFYGSKEDPQT"
                +"FYYAVAVVKKDSGFQMNQLRGKKSCHTGLGRSAGWNIPIGLLYCDLPEPRKPLEKAVA"
                +"NFFSGSCAPCADGTDFPQLCQLCPGCGCSTLNQYFGYSGAFKCLKDGAGDVAFVKHST"
                +"IFENLANKADRDQYELLCLDNTRKPVDEYKDCHLAQVPSHTVVARSMGGKEDLIWELL"
                +"NQAQEHFGKDKSKEFQLFSSPHGKDLLFKDSAHGFLKVPPRMDAKMYLGYEYVTAIRN"
                +"LREGTCPEAPTDECKPVKWCALSHHERLKCDEWSVNSVGKIECVSAETTEDCIAKIMN"
                +"GEADAMSLDGGFVYIAGKCGLVPVLAENYNKSDNCEDTPEAGYFAVAVVKKSASDLTW"
                +"DNLKGKKSCHTAVGRTAGWNIPMGLLYNKINHCRFDEFFSEGCAPGSKKDSSLCKLCM"
                +"GSGLNLCEPNNKEGYYGYTGAFRCLVEKGDVAFVKHQTVPQNTGGKNPDPWAKNLNEK"
                +"DYELLCLDGTRKPVEEYANCHLARAPNHAVVTRKDKEACVHKILRQQQHLFGSNVTDC"
                +"SGNFCLFRSETKDLLFRDDTVCLAKLHDRNTYEKYLGEEYVKAVGNLRKCSTSSLLEA"
                +"CTFRRP";
        String t  = "RDQYELLCLDNTRKPVDEYKDCH"; //contained above
        indexer.addAminoAcidSequence("foo", t);

        SequenceIndexer.ResultEnumeration results = indexer.search(q, 1, CutoffType.SUB, "Protein");
        assertFalse(results.hasMoreElements());
    }

    @Test
    public void oneRecord100Identity() throws IOException{
        String seq = "ACGTTTGC";

        indexer.addNucleicAcidSequence("foo", seq);

        SequenceIndexer.ResultEnumeration results = indexer.search(seq, 1, CutoffType.GLOBAL, "NucleicAcid");
        assertTrue(results.hasMoreElements());
        SequenceIndexer.Result result = results.nextElement();

        assertEquals("foo", result.id);
        assertEquals(seq, result.query);
        assertEquals(seq, result.target);
        List<SequenceIndexer.Alignment> alignments = result.alignments;

        assertEquals( 1, alignments.size());

        SequenceIndexer.Alignment alignment = alignments.get(0);

        assertEquals( 1, alignment.iden, DELTA);


        assertFalse(results.hasMoreElements());
    }

    @Test
    public void longerLocalSequnce100Identity() throws IOException{
        String seq = "CCTCCGGTTCTGAAGGTGTTC";

        //CCTCCGGTTCTGAAGGTGTTC
        //           XXXXXXXTTC
        //           XXXXXXGTTCT

        indexer.addNucleicAcidSequence("bar", "GGGGGGGGGGGGG");
        indexer.addNucleicAcidSequence("foo", seq);

        SequenceIndexer.ResultEnumeration results = indexer.search(seq,1, CutoffType.LOCAL, "NucleicAcid");
        assertTrue(results.hasMoreElements());
        SequenceIndexer.Result result = results.nextElement();

        assertEquals("foo", result.id);
        assertEquals(seq, result.query);
        assertEquals(seq, result.target);
        List<SequenceIndexer.Alignment> alignments = result.alignments;

        System.out.println("alignments = " + alignments);
        assertEquals(1, alignments.size());

        assert100PercentIdentity(alignments.get(0), seq);

        assertFalse(results.hasMoreElements());
    }

    @Test
    public void twoRecords100Identity() throws IOException{
        String seq = "ACGTTTGC";

        indexer.addNucleicAcidSequence("bar", "GGGGGGGGGGGGG");
        indexer.addNucleicAcidSequence("foo", seq);

        SequenceIndexer.ResultEnumeration results = indexer.search(seq, .4,CutoffType.LOCAL, "NucleicAcid");
        assertTrue(results.hasMoreElements());
        SequenceIndexer.Result result = results.nextElement();

        assertEquals("foo", result.id);
        assertEquals(seq, result.query);
        assertEquals(seq, result.target);
        List<SequenceIndexer.Alignment> alignments = result.alignments;

        assertEquals(1, alignments.size());

        assert100PercentIdentity(alignments.get(0), seq);

        assertFalse(results.hasMoreElements());
    }

    private void assert100PercentIdentity(SequenceIndexer.Alignment alignment, String seq){

        assertEquals(1D, alignment.iden, 0.001D);
//        assertEquals( 1, alignment.iden, DELTA);
//
//
//        SequenceIndexer.SEG expected = new  SequenceIndexer.SEG(0,seq.length(), 0, seq.length());
//        assertEquals(expected, alignment.getSegment());
    }

    @Test
    public void repeatedSeqShouldntCauseRepeats() throws IOException{

        String seq =  "ACGTACGT";
        indexer.addNucleicAcidSequence("foo",seq);

        SequenceIndexer.ResultEnumeration results = indexer.search(seq, .5, CutoffType.GLOBAL, "NucleicAcid");


        assertTrue(results.hasMoreElements());

        SequenceIndexer.Result result = results.nextElement();
        assertEquals("foo", result.id);
        assertEquals(seq, result.query);
        assertEquals(seq, result.target);

        assertFalse(results.hasMoreElements());


    }

    private Set<SequenceIndexer.SEG> getSegmentsFrom( SequenceIndexer.Result result) {
        Set<SequenceIndexer.SEG> set = new HashSet<>();
        for(SequenceIndexer.Alignment alignment : result.alignments){
            set.add(alignment.getSegment());
        }
        return set;
    }


    @Test
    public void longContainsMatch() throws IOException {
        String fullSequence = "GGGGGGGGGGGGGGGTTGGCCACTCCCTCTCTGCGCGCTCGCTCGCTCACTGAGGCCGGGCGACCAAAGGTCGCCCGACGCCCGGGCTT" +
                "TGCCCGGGCGGCCTCAGTGAGCGAGCGAGCGCGCAGAGAGGGAGTGGCCAACTCCATCACTAGGGGTTCCTCAGATCTGAATTCGGTACCCGTTACATAACTT" +
                "ACGGTAAATGGCCCGCCTGGCTGACCGCCCAACGACCCCCGCCCATTGACGTCAATAGTAACGCCAATAGGGACTTTCCATTGACGTCAATGGGTGGAGTATTT" +
                "ACGGTAAACTGCCCACTTGGCAGTACATCAAGTGTATCATATGCCAAGTACGCCCCCTATTGACGTCAATGACGGTAAATGGCCCGCCTGGCATTGTGCCCAGT" +
                "ACATGACCTTATGGGACTTTCCTACTTGGCAGTACATCTACGTATTAGTCATCGCTATTACCATGGTGATGCGGTTTTGGCAGTACATCAATGGGCGTGGATAG" +
                "CGGTTTGACTCACGGGGATTTCCAAGTCTCCACCCCATTGACGTCAATGGGAGTTTGTTTTGGCACCAAAATCAACGGGACTTTCCAAAATGTCGTAACAACTC" +
                "CGCCCCATTGACGCAAATGGGCGGTAGGCGTGTACGGTGGGAGGTCTATATAAGCAGAGCTCGTTTAGTGAACCGTCAGATCGCCTGGAGACGCCATCCACGCT" +
                "GTTTTGACCTCCATAGAAGACACCGGGACCGATCCAGCCTCCGGACTCTAGAGGATCCGGTACTCGATAATACGACTCACTATAGGGAGACCCAAGCTTGATCC" +
                "CCCCTCTTCCTCCTCCTCAAGGGAAAGCTGCCCACTTCTAGCTGCCCTGCCATCCCCTTTAAAGGGCGACTTGCTCAGCGCCAAACCGCGGCTCCAGCCCTCTC" +
                "CAGCCTCCGGCTCAGCCGGCTCATCAGTCGGTCCGCGCCTTGCAGCTCCTCCAGAGGGACGCGCCCCGAGATGGAGAGCAAAGCCCTGCTCGTGCTGACTCTGG" +
                "CCGTGTGGCTCCAGAGTCTGACCGCCTCCCGCGGAGGGGTGGCCGCCGCCGACCAAAGAAGAGATTTTATCGACATCGAAAGTAAATTTGCCCTAAGGACCCCT" +
                "GAAGACACAGCTGAGGACACTTGCCACCTCATTCCCGGAGTAGCAGAGTCCGTGGCTACCTGTCATTTCAATCACAGCAGCAAAACCTTCATGGTGATCCATGG" +
                "CTGGACGGTAACAGGAATGTATGAGAGTTGGGTGCCAAAACTTGTGGCCGCCCTGTACAAGAGAGAACCAGACTCCAATGTCATTGTGGTGGACTGGCTGTCAC" +
                "GGGCTCAGGAGCATTACCCAGTGTCCGCGGGCTACACCAAACTGGTGGGACAGGATGTGGCCCGGTTTATCAACTGGATGGAGGAGGAGTTTAACTACCCTCTG" +
                "GACAATGTCCATCTCTTGGGATACAGCCTTGGAGCCCATGCTGCTGGCATTGCAGGAAGTCTGACCAATAAGAAAGTCAACAGAATTACTGGCCTCGATCCAGC" +
                "TGGACCTAACTTTGAGTATGCAGAAGCCCCGAGTCGTCTTTCTCCTGATGATGCAGATTTTGTAGACGTCTTACACACATTCACCAGAGGGTCCCCTGGTCGAA" +
                "GCATTGGAATCCAGAAACCAGTTGGGCATGTTGACATTTACCCGAATGGAGGTACTTTTCAGCCAGGATGTAACATTGGAGAAGCTATCCGCGTGATTGCAGAGA" +
                "GAGGACTTGGAGATGTGGACCAGCTAGTGAAGTGCTCCCACGAGCGCTCCATTCATCTCTTCATCGACTCTCTGTTGAATGAAGAAAATCCAAGTAAGGCCTACA" +
                "GGTGCAGTTCCAAGGAAGCCTTTGAGAAAGGGCTCTGCTTGAGTTGTAGAAAGAACCGCTGCAACAATCTGGGCTATGAGATCAATAAAGTCAGAGCCAAAAGAA" +
                "GCAGCAAAATGTACCTGAAGACTCGTTCTCAGATGCCCTACAAAGTCTTCCATTACCAAGTAAAGATTCATTTTTCTGGGACTGAGAGTGAAACCCATACCAATC" +
                "AGGCCTTTGAGATTTCTCTGTATGGCACCGTGGCCGAGAGTGAGAACATCCCATTCACTCTGCCTGAAGTTTCCACAAATAAGACCTACTCCTTCCTAATTTACA" +
                "CAGAGGTAGATATTGGAGAACTACTCATGTTGAAGCTCAAATGGAAGAGTGATTCATACTTTAGCTGGTCAGACTGGTGGAGCAGTCCCGGCTTCGCCATTCAGA" +
                "AGATCAGAGTAAAAGCAGGAGAGACTCAGAAAAAGGTGATCTTCTGTTCTAGGGAGAAAGTGTCTCATTTGCAGAAAGGAAAGGCACCTGCGGTATTTGTGAAAT" +
                "GCCATGACAAGTCTCTGAATAAGAAGTGAGGCTGAAACTGGGCGAATCTACAGAACAAAGAACGGCATGTGAATTCCTGCAGGTCGCGGCCGCGACTCTAGAGCTA" +
                "GTTCAGGTGTATTGCCACAAGACAAACATGTTAAGAAAATTTCCCGTTATTTGCACTCTGTTCCTGTTAATCAACCTCTGGATTACAAAATTTGTGAAAGATTGA" +
                "CTGGTATTCTTAACTATGTTGCTCCTTTTACGCTATGTGGATACGCTGCTTTAATGCCTTTGTATCATGCTATTGCTTCCCGTATGGCTTTCATTTTCTCCTCCT" +
                "TGTATAAATCCTGGTTGCTGTCTCTTTATGAGGAGTTGTGGCCCGTTGTCAGGCAACGTGGCGTGGTGTGCACTGTGTTTGCTGACGCAACCCCCACTGGTTGGGG" +
                "CATTGCCACCACCTGTCAGCTCCTTTCCGGGACTTTCGCTTTCCCCCTCCCTATTGCCACGGCGGAACTCATCGCCGCCTGCCTTGCCCGCTGCTGGACAGGGGC" +
                "TCGGCTGTTGGGCACTGACAATTCCGTGGTGTTGTCGGGGAAGCTGACGTCCTTTCCATGGCTGCTCGCCTGTGTTGCCACCTGGATTCTGCGCGGGACGTCCTTC" +
                "TGCTACGTCCCTTCGGCCCTCAATCCAGCGGACCTTCCTTCCCGCGGCCTGCTGCCGGCTCTGCGGCCTCTTCCGCGTCTTCGCCTTCGCCCTCAGACGAGTCGGA" +
                "TCTCCCTTTGGGCCGCCTCCCCGCCTGTTTCGCCTCGGCGTCCGGTCCGTGTTGCTTGGTCTTCACCTGTGCAGAATTGCGAACCATGGATTCATCGACGGTACCG" +
                "CGGGCCCTCGACTAGAGCTCGCTGATCAGCCTCGACTGTGCCTTCTAGTTGCCAGCCATCTGTTGTTTGCCCCTCCCCCGTGCCTTCCTTGACCCTGGAAGGTGCC" +
                "ACTCCCACTGTCCTTTCCTAATAAAATGAGGAAATTGCATCGCATTGTCTGAGTAGGTGTCATTCTATTCTGGGGGGTGGGGTGGGGCAGGACAGCAAGGGGGAGG" +
                "ATTGGGAAGACAATAGCAGGCATGCTGGGGAGAGATCTGAGGAACCCCTAGTGATGGAGTTGGCCACTCCCTCTCTGCGCGCTCGCTCGCTCACTGAGGCCGCCCG" +
                "GGCAAAGCCCGGGCGTCGGGCGACCTTTGGTCGCCCGGCCTCAGTGAGCGAGCGAGCGCGCAGAGAGGGAGTGGCCAACTCCATCACTAGGGGTTCCCC";

        String subSequence = "ACATGACCTTATGGGACTTTCCTACTTGGCAGTACATCTACGTATTAGTCATCGCTATTACCATGGTGATGCGGTTTTGGCAGTACATCAATG" +
                "GGCGTGGATAGCGGTTTGACTCACGGGGATTTCCAAGTCTCCACCCCATTGACGTCAATGGGAGTTTGTTTTGGCACCAAAATCAACGGGACTTTCCAAAATGTCG" +
                "TAACAACTCCGCCCCATTGACGCAAATGGGCGGTAGGCGTGTACGGTGGGAGGTCTATATAAGCAGAGCTCGTTTAGTGAACCGTCAGATCGCCTGGAGACGCCAT" +
                "CCACGCTGTTTTGACCTCCATAGAAGACACCGGGACCGATCCAGCCTCCGGACTCTAGAGGATCCGGTACTCGATAATACGACTCACTATAGGGAGACCCAAGCTT" +
                "GATCCCCCCTCTTCCTCCTCCTCAAGGGAAAGCTGCCCACTTCTAGCTGCCCTGCCATCCCCTTTAAAGGGCGACTTGCTCAGCGCCAAACCGCGGCTCCAGCCCT" +
                "CTCCAGCCTCCGGCTCAGCCGGCTCATCAGTCGGTCCGCGCCTTGCAGCTCCTCCAGAGGGACGCGCCCCGAGATGGAGAGCAAAGCCCTGCTCGTGCTGACTCTG" +
                "GCCGTGTGGCTCCAGAGTCTGACCGCCTCCCGCGGAGGGGTGGCCGCCGCCGACCAAAGAAGAGATTTTATCGACATCGAAAGTAAATTTGCCCTAAGGACCCCTG" +
                "AAGACACAGCTGAGGACACTTGCCACCTCATTCCCGGAGTAGCAGAGTCCGTGGCTACCTGTCATTTCAATCACAGCAGCAAAACCTTCATGGTGATCCATGGCTG" +
                "GACGGTAACAGGAATGTATGAGAGTTGGGTGCCAAAACTTGTGGCCGCCCTGTACAAGAGAGAACCAGACTCCAATGTCATTGTGGTGGACTGGCTGTCACGGGCT" +
                "CAGGAGCATTACCCAGTGTCCGCGGGCTACACCAAACTGGTGGGACAGGATGTGGCCCGGTTTATCAACTGGATGGAGGAGGAGTTTAACTACCCTCTGGACAATG" +
                "TCCATCTCTTGGGATACAGCCTTGGAGCCCATGCTGCTGGCATTGCAGGAAGTCTGACCAATAAGAAAGTCAACAGAATTACTGGCCTCGATCCAGCTGGACCTAA" +
                "CTTTGAGTATGCAGAAGCCCCGAGTCGTCTTTCTCCTGATGATGCAGATTTTGTAGACGTCTTACACACATTCACCAGAGGGTCCCCTGGTCGAAGCATTGGAATC" +
                "CAGAAACCAGTTGGGCATGTTGACATTTACCCGAATGGAGGTACTTTTCAGCCAGGATGTAACATTGGAGAAGCTATCCGCGTGATTGCAGAGAGAGGACTTGGAG" +
                "ATGTGGACCAGCTAGTGAAGTGCTCCCACGAGCGCTCCATTCATCTCTTCATCGACTCTCTGTTGAATGAAGAAAATCCAAGTAAGGCCTACAGGTGCAGTTCCAAG" +
                "GAAGCCTTTGAGAAAGGGCTCTGCTTGAGTTGTAGAAAGAACCGCTGCAACAATCTGGGCTATGAGATCAATAAAGTCAGAGCCAAAAGAAGCAGCAAAATGTACCT" +
                "GAAGACTCGTTCTCAGAT";

        indexer.addNucleicAcidSequence("foo", fullSequence);
        SequenceIndexer.ResultEnumeration results = indexer.search(subSequence, .8, CutoffType.SUB, "NucleicAcid");
        assertTrue(results.hasMoreElements());
    }


    @Test
    public void searchingLargeSetOfSequencesThatHaveMultiSubunits() throws IOException {
        indexer.setProteinKmer(3);
        indexer.setKmerSize(3);

        SequenceIndexerBatchTest.getLargeSetOfSequences((t)->{
            long timee = StopWatch.timeElapsed(()->{
                try
                {
                    indexer.addAminoAcidSequence(t.k(), t.v());
                    indexer.addAminoAcidSequence(t.k()+"_d", t.v());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        });

        Map<String,Integer> whiteList =
                Arrays.stream((
                        "0e26ba1a-db56-4823-b9b3-0b6f81a55522_1	14	4896058168\n" +
                                "0e26ba1a-db56-4823-b9b3-0b6f81a55522_2	3	1110518963\n" +
                                "23559f52-21c8-4b43-92f1-981b36cdfe1d_3	1	3736874972\n" +
                                "bdd095d5-0abb-4ef0-9a02-f517e915cbc2_4	1	5774902198\n" +
                                "5491b265-a9cb-44be-aa35-5a47d787db5b_5	1	417252287\n" +
                                "9a363179-e4d5-4759-aea8-3e9b6d613731_6	2	2501258026\n" +
                                "66c94b1a-d703-4233-b798-fa613b3e5dbe_7	2	3479402818\n" +
                                "0e60e83e-9768-4d80-9ae9-db3653efa9ca_8	21	1278625537\n" +
                                "0e60e83e-9768-4d80-9ae9-db3653efa9ca_9	114	83550399\n" +
                                "85a53fca-7ab8-465e-8a2d-8be91e359a46_16	10	5135281027\n" +
                                "85a53fca-7ab8-465e-8a2d-8be91e359a46_17	1	2958264074\n" +
                                "541523f0-7fd2-428f-9e0f-0d879c77115b_18	125	239518682\n" +
                                "e47d4347-337e-49ea-96e2-cb073655c96a_25	1	3375455\n" +
                                "3a25d346-062e-4cf4-a56f-65e2455f2114_29	1	436253081").split("\n"))
                        .map(l->l.split("\t"))
                        .map(t->Tuple.of(t[0],t[1]))
                        .map(Tuple.vmap(l->Integer.parseInt(l)))
                        .collect(Tuple.toMap());

        SequenceIndexerBatchTest.getLargeSetOfSequences((t)->{
            if(!whiteList.containsKey(t.k()))return;
            int count=whiteList.get(t.k());
            long start=System.nanoTime();
            SequenceIndexer.ResultEnumeration results = indexer.search(t.v(), .9, CutoffType.GLOBAL, "Protein");

//        	System.out.println("matches = ");
//        	StreamUtil.forEnumeration(results).map(r-> r.id).forEach(System.out::println);
            List<Result> res=StreamUtil.forEnumeration(results).collect(Collectors.toList());
//        	System.out.println(res);
            long end = System.nanoTime();

            //assertEquals(count,res.size()/2);

            System.out.println(t.k()+"\t" +res.size() + "\t" + (end-start));
        });

    }
}
