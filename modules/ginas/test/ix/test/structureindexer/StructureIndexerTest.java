package ix.test.structureindexer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

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

import gov.nih.ncats.molwitch.Chemical;
import gov.nih.ncats.molwitch.search.MolSearcher;
import gov.nih.ncats.molwitch.search.MolSearcherFactory;

import java.util.Arrays;
import java.util.Optional;

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
	public void ensureBasicSSSCanGiveNegativeResult() throws Exception{
		
		
		Chemical p=Chemical.parseMol("\n" + 
				"   JSDraw209162010482D\n" + 
				"\n" + 
				"  1  0  0  0  0  0            999 V2000\n" + 
				"   15.8080   -7.0436    0.0000 P   0  0  0  0  0  0  0  0  0  0  0  0\n" + 
				"M  END");
		Chemical t=Chemical.parseMol("\n" + 
				"   JSDraw209162010482D\n" + 
				"\n" + 
				"  1  0  0  0  0  0            999 V2000\n" + 
				"   15.8080   -7.0436    0.0000 O   0  0  0  0  0  0  0  0  0  0  0  0\n" + 
				"M  END");
		
		
		
		Optional<int[]> hit = MolSearcherFactory.create(p).search(t);
		
		assertEquals("false",""+hit.isPresent());
		
		
	}

	@Test
	public void ensureSubstructureSearchHasBasicSmartsSupportForAnyBond() throws Exception{

		String structure="[#7,#8]~C1=c2c3c(OC([#6])(O)C3=O)cc(O)c2=C(O)\\C=C/1";
		structureIndexer.add("1", "COC1=CC=C(O)C2=C(O)C(C)=C3OC(C)(O)C(=O)C3=C12");
		structureIndexer.add("2", "CC1=C2OC(C)(O)C(=O)C2=C3C4=C(C=C(O)C3=C1O)N5C=CC=CC5=N4");
		assertEquals(2,StreamUtil.forEnumeration(structureIndexer.substructure(structure, 10)).count());
	}
	
	@Test
	public void ensureSearchForPhosphorousInNonPhosphorousStructureReturnsNothing() throws Exception{

		String structure="P";
		structureIndexer.add("1", "\n"
				+ "  Symyx   08281518352D 1   1.00000     0.00000     0\n"
				+ "\n" + 
				" 13 13  0  0  0  0            999 V2000\n" + 
				"   -1.5207    0.5690    0.0000 C   0  0  0  0  0  0           0  0  0\n" + 
				"    0.1103   -0.3621    0.0000 C   0  0  0  0  0  0           0  0  0\n" + 
				"   -3.1655   -0.3621    0.0000 C   0  0  0  0  0  0           0  0  0\n" + 
				"   -1.5207    2.4828    0.0000 C   0  0  0  0  0  0           0  0  0\n" + 
				"    0.1103   -2.2690    0.0000 C   0  0  0  0  0  0           0  0  0\n" + 
				"    1.7793    0.6069    0.0000 Se  0  0  0  0  0  0           0  0  0\n" + 
				"   -3.1655   -2.2690    0.0000 C   0  0  0  0  0  0           0  0  0\n" + 
				"    0.1103    3.4069    0.0000 O   0  0  0  0  0  0           0  0  0\n" + 
				"   -3.1655    3.4069    0.0000 O   0  0  0  0  0  0           0  0  0\n" + 
				"   -1.5207   -3.2414    0.0000 C   0  0  0  0  0  0           0  0  0\n" + 
				"    3.4345   -0.3621    0.0000 C   0  0  0  0  0  0           0  0  0\n" + 
				"    5.0793    0.6069    0.0000 C   0  0  0  0  0  0           0  0  0\n" + 
				"    3.4345   -2.2172    0.0000 O   0  0  0  0  0  0           0  0  0\n" + 
				"  1  2  2  0     0  0\n" + 
				"  1  3  1  0     0  0\n" + 
				"  1  4  1  0     0  0\n" + 
				"  2  5  1  0     0  0\n" + 
				"  2  6  1  0     0  0\n" + 
				"  3  7  2  0     0  0\n" + 
				"  4  8  1  0     0  0\n" + 
				"  4  9  2  0     0  0\n" + 
				"  5 10  2  0     0  0\n" + 
				"  6 11  1  0     0  0\n" + 
				" 11 12  1  0     0  0\n" + 
				" 11 13  2  0     0  0\n" + 
				"  7 10  1  0     0  0\n" + 
				"M  END");
		assertEquals(0,StreamUtil.forEnumeration(structureIndexer.substructure(structure, 10)).count());
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
