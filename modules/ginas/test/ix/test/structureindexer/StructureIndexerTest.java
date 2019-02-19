package ix.test.structureindexer;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import ix.AbstractGinasServerTest;
import ix.core.adapters.EntityPersistAdapter;
import ix.core.java8Util.Java8ForOldEbeanHelper;
import ix.core.plugins.StructureIndexerPlugin;
import ix.core.util.EntityUtils.EntityWrapper;
import ix.core.util.RunOnly;
import ix.core.util.StreamUtil;
import ix.ginas.modelBuilders.SubstanceBuilder;
import ix.ginas.models.v1.ChemicalSubstance;

public class StructureIndexerTest extends AbstractGinasServerTest{
	
	StructureIndexerPlugin.StandardizedStructureIndexer structureIndexer;
	
	@Before
	public void getIndexer(){
		structureIndexer=EntityPersistAdapter.getInstance().getStructureIndexer();
	}
	
	
	@Test
	public void ensureIndexing2StructuresWithSameIdReturnsTheIdTwiceWhenSearchMatches() throws Exception{
		String id="1234567";
		String structure="C1CCCCC1";
		structureIndexer.add(id, structure);
		structureIndexer.add(id, structure);
		
		assertEquals(2,StreamUtil.forEnumeration(structureIndexer.substructure(structure, 10)).count());		
	}
	
	@Test
	public void ensureIndexing2StructuresWithSameIdThenDeletingTheIDReturnsNothing() throws Exception{
		String id="1234567";
		String structure="C1CCCCC1";
		structureIndexer.add(id, structure);
		structureIndexer.add(id, structure);
		structureIndexer.remove(null,id);
		assertEquals(0,StreamUtil.forEnumeration(structureIndexer.substructure(structure, 10)).count());		
	}
	
	@Test
	public void ensureSubstructureSearchHasBasicSmartsSupport() throws Exception{

		String structure="[#7,#8]c1ccc(O)c2c(O)c([#6])c3OC([#6])(O)C(=O)c3c12";
		structureIndexer.add("1", "COC1=CC=C(O)C2=C(O)C(C)=C3OC(C)(O)C(=O)C3=C12");
		structureIndexer.add("2", "CC1=C2OC(C)(O)C(=O)C2=C3C4=C(C=C(O)C3=C1O)N5C=CC=CC5=N4");
		assertEquals(2,StreamUtil.forEnumeration(structureIndexer.substructure(structure, 10)).count());
	}

	@Test
	public void ensureSubstructureSearchHasBasicSmartsSupportForAnyBond() throws Exception{

		String structure="[#7,#8]~C1=c2c3c(OC([#6])(O)C3=O)cc(O)c2=C(O)\\C=C/1";
		structureIndexer.add("1", "COC1=CC=C(O)C2=C(O)C(C)=C3OC(C)(O)C(=O)C3=C12");
		structureIndexer.add("2", "CC1=C2OC(C)(O)C(=O)C2=C3C4=C(C=C(O)C3=C1O)N5C=CC=CC5=N4");
		assertEquals(2,StreamUtil.forEnumeration(structureIndexer.substructure(structure, 10)).count());
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
		assertEquals(1,StreamUtil.forEnumeration(structureIndexer.substructure(structure, 10)).count());		
		Java8ForOldEbeanHelper.removeStructureIndexesForBean(EntityPersistAdapter.getInstance(), EntityWrapper.of(cs));
		assertEquals(0,StreamUtil.forEnumeration(structureIndexer.substructure(structure, 10)).count());
		
		
	}
	
	

}
