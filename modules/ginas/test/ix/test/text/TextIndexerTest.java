package ix.test.text;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import ix.AbstractGinasServerTest;
import ix.core.models.Indexable;
import ix.core.search.SearchResult;
import ix.core.search.text.TextIndexer;
import ix.core.util.EntityUtils.EntityWrapper;
import org.junit.rules.TemporaryFolder;

import javax.persistence.Id;

/**
 * Tests the TextIndexer on specific tasks, like indexing
 * strings, facets, etc. Specifically focuses on idiosyncratic
 * behaviors that have been observed in the past.
 * @author peryeata
 *
 */
public class TextIndexerTest extends AbstractGinasServerTest{
	
	@Rule
	public TemporaryFolder tmpDir = new TemporaryFolder();

	public class MySpecialTestClass{
		@Indexable(facet=true)
		public String stringField="";
		
		@Indexable(facet=true, indexEmpty=true,emptyString="EMPTYONE" )
		public String emptyIndexable="";

        @Id
        public String id;
		public String normalField;
		
		public MySpecialTestClass(String id, String searchValue){

            this.id = id;
            normalField=searchValue;
		}
		
	}

	private TextIndexer ti;

	@Before
	public void createTextIndexer() throws IOException{
		ti = TextIndexer.getInstance(tmpDir.getRoot());
	}


    @Test
    public void stringWithStopWord() throws IOException {
        String value="VITAMIN";


        ti.add(EntityWrapper.of(new MySpecialTestClass("id", value+" A")));
        ti.add(EntityWrapper.of(new MySpecialTestClass("id", value+" B")));
        ti.add(EntityWrapper.of(new MySpecialTestClass("id", value+" C")));

        SearchResult sr= ti.search("\""+ value+" A\"", 2);
        assertEquals(1,sr.getCount());

    }
	@Test
	public void testIndexEmptyStringField() throws IOException {
			String value="VALUEIWILLSEARCHFOR";


			ti.add(EntityWrapper.of(new MySpecialTestClass("id", value)));
			
			SearchResult sr= ti.search(value, 2);
			assertEquals(1,sr.getCount());

	}
	
	@Test
	public void testIndexEmptyStringWithAllowedEmptyShouldBeSearchable() throws IOException {
					String value="VALUEIWILLSEARCHFOR";

			EntityWrapper<MySpecialTestClass> ew = EntityWrapper.of(new MySpecialTestClass("id2", value));
			ti.add(ew);
			SearchResult sr= ti.search("EMPTYONE", 2);
			assertEquals(1,sr.getCount());
    }

    @Test
    public void noResults() throws IOException {
        String value="VALUEIWILLSEARCHFOR";

        EntityWrapper<MySpecialTestClass> ew = EntityWrapper.of(new MySpecialTestClass("id2", value));
        ti.add(ew);
        SearchResult sr= ti.search("something completly different", 2);
        assertEquals(0,sr.getCount());
    }

    @Test
    public void prefixSearch() throws IOException {
        String value="VALUEIWILLSEARCHFOR";

        EntityWrapper<MySpecialTestClass> ew = EntityWrapper.of(new MySpecialTestClass("id2", value));
        ti.add(ew);
        SearchResult sr= ti.search("VALUE*", 2);
        assertEquals(1,sr.getCount());
    }

    @Test
    public void suffixSearch() throws IOException {
        String value="VALUEIWILLSEARCHFOR";

        EntityWrapper<MySpecialTestClass> ew = EntityWrapper.of(new MySpecialTestClass("id2", value));
        ti.add(ew);
        SearchResult sr= ti.search("*FOR", 2);
        assertEquals(1,sr.getCount());
    }

    @Test
    public void middleSearchWithWildCards() throws IOException {
        String value="VALUEIWILLSEARCHFOR";

        EntityWrapper<MySpecialTestClass> ew = EntityWrapper.of(new MySpecialTestClass("id2", value));
        ti.add(ew);
        SearchResult sr= ti.search("*WILL*", 2);
        assertEquals(1,sr.getCount());
    }
    @Test
    public void middleSearchWithoutWildCardsWillNotFindAnything() throws IOException {
        String value="VALUEIWILLSEARCHFOR";

        EntityWrapper<MySpecialTestClass> ew = EntityWrapper.of(new MySpecialTestClass("id2", value));
        ti.add(ew);
        SearchResult sr= ti.search("WILL", 2);
        assertEquals(0,sr.getCount());
    }
    @Test
    public void middleSearchWithoutDoubleSidedAstrixShouldNotFindAnything() throws IOException {
        String value="VALUEIWILLSEARCHFOR";

        EntityWrapper<MySpecialTestClass> ew = EntityWrapper.of(new MySpecialTestClass("id2", value));
        ti.add(ew);
        assertEquals(0,ti.search("*WILL", 2).getCount());
        assertEquals(0,ti.search("WILL*", 2).getCount());
    }

    @Test
    public void fieldSpecification() throws IOException {
        String value="VALUEIWILLSEARCHFOR";

        EntityWrapper<MySpecialTestClass> ew = EntityWrapper.of(new MySpecialTestClass("id2", value));
        ti.add(ew);
        ti.add(EntityWrapper.of(new MySpecialTestClass("id1", "somethingCompletelyDifferent")));
        SearchResult sr= ti.search("normalField:*WILL*", 2);
        assertEquals(1,sr.getCount());
    }
}
