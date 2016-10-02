package ix.test.entitywrapper;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import javax.persistence.Entity;

import org.junit.Rule;
import org.junit.Test;

import ix.core.models.Indexable;
import ix.core.models.Role;
import ix.core.util.EntityUtils.EntityWrapper;
import ix.core.util.EntityUtils.FieldMeta;
import ix.test.ix.test.server.GinasTestServer;
import ix.test.ix.test.server.RestSession;

public class EntityWrapperTest {
	@Rule
	public GinasTestServer ts = new GinasTestServer(9001);
	
	public static class TestIndexed{
		
		@Indexable(taxonomy=true)
		public String path;
		
		public TestIndexed(String path){
			this.path=path;
		}
	}
	@Test 
	public void testPathSplitOnEntityWrapperSameAsManualSplit() throws Exception {
		try (RestSession session = ts.newRestSession(ts.createUser(Role.Admin))) {
			TestIndexed testIndexed = new TestIndexed("this/is/a/path");
			EntityWrapper<TestIndexed> wrapped=EntityWrapper.of(testIndexed);
			Optional<FieldMeta> fieldMeta=wrapped.getFieldInfo()
				.stream()
				.filter(f->f.getName().equals("path"))
				.findFirst();
			assertTrue(fieldMeta.isPresent());
			FieldMeta fm = fieldMeta.get();
			String[] path=fm.getIndexable().splitPath(fm.getValue(testIndexed).get().toString());
			assertEquals(
					Arrays.asList(testIndexed.path.split("/")),
					Arrays.asList(path));
		}catch(Throwable e){
			e.printStackTrace();
			throw e;
		}
	}
	
	@Entity
	public static class Inception{
		@Indexable(indexed=true)
		public String lookHere="Look here";
		public Inception inc=this;
		
		public Child realChild = new Child();
		public Child nullChild = null;
		
	}
	
	@Entity
	public static class Child{
		
	}
	
	@Test
	public void testRecurseDoesNotGetIntoInfiniteLoop() throws Exception {
		try {
			Inception inc = new Inception();
			Set<String> expectedVisited = new HashSet<String>();
			expectedVisited.add("root_");
			expectedVisited.add("root_realChild");
			Set<String> pathsVisited = new HashSet<String>();

			EntityWrapper.of(inc).traverse().execute((p, ew) -> {
				assertTrue(expectedVisited.contains(p.toPath()));
				pathsVisited.add(p.toPath());
			});

			assertEquals(expectedVisited, pathsVisited);
		} catch (Throwable t) {
			t.printStackTrace();
			throw t;
		}
	}
}
