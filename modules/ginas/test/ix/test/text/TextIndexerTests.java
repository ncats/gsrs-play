package ix.test.text;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

import org.junit.Rule;
import org.junit.Test;

import ix.core.models.Indexable;
import ix.core.search.SearchResult;
import ix.core.search.text.TextIndexer;
import ix.core.util.EntityUtils.EntityWrapper;
import ix.core.util.IOUtil;
import ix.test.ix.test.server.GinasTestServer;
import ix.test.ix.test.server.RestSession;
import org.junit.rules.TemporaryFolder;

import javax.persistence.Id;

/**
 * Tests the TextIndexer on specific tasks, like indexing
 * strings, facets, etc. Specifically focuses on idiosyncratic
 * behaviors that have been observed in the past.
 * @author peryeata
 *
 */
public class TextIndexerTests {
	@Rule
    public GinasTestServer ts = new GinasTestServer();

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

	@Test
	public void testCreateTextIndexerTemporary() throws IOException {
			TextIndexer.getInstance(tmpDir.getRoot());


	}
	
	@Test
	public void testIndexEmptyStringField() throws IOException {
			String value="VALUEIWILLSEARCHFOR";

			TextIndexer ti = TextIndexer.getInstance(tmpDir.getRoot());
			ti.add(EntityWrapper.of(new MySpecialTestClass("id", value)));
			
			SearchResult sr= ti.search(value, 2);
			assertEquals(1,sr.count());

	}
	
	@Test
	public void testIndexEmptyStringWithAllowedEmptyShouldBeSearchable() throws IOException {
					String value="VALUEIWILLSEARCHFOR";

			
			TextIndexer ti = TextIndexer.getInstance(tmpDir.getRoot());
			EntityWrapper<MySpecialTestClass> ew = EntityWrapper.of(new MySpecialTestClass("id2", value));
			ti.add(ew);
			SearchResult sr= ti.search("EMPTYONE", 2);
			assertEquals(1,sr.count());
    }

	
}
