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

/**
 * Tests the TextIndexer on specific tasks, like indexing
 * strings, facets, etc. Specifically focuses on idiosyncratic
 * behaviors that have been observed in the past.
 * @author peryeata
 *
 */
public class TextIndexerTests {
	@Rule
    public GinasTestServer ts = new GinasTestServer(9001);

	public class MySpecialTestClass{
		@Indexable(facet=true)
		public String stringField="";
		
		@Indexable(facet=true, indexEmpty=true,emptyString="EMPTYONE" )
		public String emptyIndexable="";
		
		public String normalField;
		
		public MySpecialTestClass(String searchValue){
			normalField=searchValue;
		}
		
	}

	@Test
	public void testCreateTextIndexerTemporary() throws IOException {
		try(RestSession session = ts.newRestSession(ts.getFakeUser1())){
		File newTempFile = new File("./test-index");
		IOUtil.deleteRecursivelyQuitely(newTempFile);
		newTempFile.mkdirs();
		TextIndexer ti = TextIndexer.getInstance(newTempFile);

		IOUtil.deleteRecursivelyQuitely(newTempFile);
		}
	}
	
	@Test
	public void testIndexEmptyStringField() throws IOException {
		try(RestSession session = ts.newRestSession(ts.getFakeUser1())){
			File newTempFile = new File("./test-index" + UUID.randomUUID());
			String value="VALUEIWILLSEARCHFOR";
			IOUtil.deleteRecursivelyQuitely(newTempFile);
			newTempFile.mkdirs();
			TextIndexer ti = TextIndexer.getInstance(newTempFile);
			ti.add(EntityWrapper.of(new MySpecialTestClass(value)));
			
			SearchResult sr= ti.search(value, 2);
			assertEquals(1,sr.count());

			IOUtil.deleteRecursivelyQuitely(newTempFile);
		}
	}
	
	@Test
	public void testIndexEmptyStringWithAllowedEmptyShouldBeSearchable() throws IOException {
		try(RestSession session = ts.newRestSession(ts.getFakeUser1())){
			File newTempFile = new File("./test-index" + UUID.randomUUID());
			String value="VALUEIWILLSEARCHFOR";
			IOUtil.deleteRecursivelyQuitely(newTempFile);
			newTempFile.mkdirs();
			
			TextIndexer ti = TextIndexer.getInstance(newTempFile);
			EntityWrapper<MySpecialTestClass> ew = EntityWrapper.of(new MySpecialTestClass(value));
			ti.add(ew);
			SearchResult sr= ti.search("EMPTYONE", 2);
			assertEquals(1,sr.count());
			IOUtil.deleteRecursivelyQuitely(newTempFile);
		}
	}
}
