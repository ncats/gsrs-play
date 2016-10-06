package ix.test.structureindexer;

import static org.junit.Assert.assertEquals;

import java.util.Iterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import ix.core.adapters.EntityPersistAdapter;
import ix.core.java8Util.Java8ForOldEbeanHelper;
import ix.core.util.EntityUtils.EntityWrapper;
import ix.ginas.models.v1.ChemicalSubstance;
import ix.test.builder.SubstanceBuilder;
import ix.test.server.GinasTestServer;
import tripod.chem.indexer.StructureIndexer;
import tripod.chem.indexer.StructureIndexer.Result;
import tripod.chem.indexer.StructureIndexer.ResultEnumeration;

public class StructureIndexerTest {
	@Rule
    public GinasTestServer ts = new GinasTestServer();
	StructureIndexer structureIndexer;
	
	@Before
	public void getIndexer(){
		structureIndexer=EntityPersistAdapter.getInstance().getStructureIndexer();
	}
	
	public static Stream<Result> asStream(ResultEnumeration rn){
		//CollectionUtils.toIterator(enumeration)
		Iterator<Result> ir;
		ir=new Iterator<Result>(){
			@Override
			public boolean hasNext() {
				return rn.hasMoreElements();
			}

			@Override
			public Result next() {
				return rn.nextElement();
			}
		};
		return StreamSupport.stream(Spliterators.spliteratorUnknownSize(ir, Spliterator.IMMUTABLE), false);
		
	}
	
	@Test
	public void ensureIndexing2StructuresWithSameIdReturnsTheIdTwiceWhenSearchMatches() throws Exception{
		String id="1234567";
		String structure="C1CCCCC1";
		structureIndexer.add(id, structure);
		structureIndexer.add(id, structure);
		
		assertEquals(2,asStream(structureIndexer.substructure(structure, 10)).count());		
	}
	
	@Test
	public void ensureIndexing2StructuresWithSameIdThenDeletingTheIDReturnsNothing() throws Exception{
		String id="1234567";
		String structure="C1CCCCC1";
		structureIndexer.add(id, structure);
		structureIndexer.add(id, structure);
		structureIndexer.remove(null,id);
		assertEquals(0,asStream(structureIndexer.substructure(structure, 10)).count());		
	}
	
	@Test
	public void ensureAddingSubstanceAndRemovingGives0ResultsOnSearch() throws Exception{

		String structure="C1CCCCC1";
		ChemicalSubstance cs=new SubstanceBuilder()
				.asChemical()
				.setStructure(structure)
				.addName("Test")
				.generateNewUUID()
				.build();
		
		Java8ForOldEbeanHelper.makeStructureIndexesForBean(EntityPersistAdapter.getInstance(), EntityWrapper.of(cs));
		assertEquals(1,asStream(structureIndexer.substructure(structure, 10)).count());		
		Java8ForOldEbeanHelper.removeStructureIndexesForBean(EntityPersistAdapter.getInstance(), EntityWrapper.of(cs));
		assertEquals(0,asStream(structureIndexer.substructure(structure, 10)).count());
		
		
	}
	
	

}
