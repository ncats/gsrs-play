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
	public void ensureBenzeneSearchWorks() throws Exception{

		String structure="C1=CC=CC=C1";
		structureIndexer.add("1", "C1=CC=CC=C1");
		assertEquals(1,StreamUtil.forEnumeration(structureIndexer.substructure(structure, 10)).count());
	}
	
	@Test
	public void ensureBenzeneAromaticSearchWorks() throws Exception{

		String structure="c1ccccc1";
		structureIndexer.add("1", "c1ccccc1");
		assertEquals(1,StreamUtil.forEnumeration(structureIndexer.substructure(structure, 10)).count());
	}
	
	@Test
	public void ensureSingleDoubleBondSearchDoNotReturnAromaticBenzene() throws Exception{

		String structure="CC=C";
		structureIndexer.add("1", "c1ccccc1");
		assertEquals(0,StreamUtil.forEnumeration(structureIndexer.substructure(structure, 10)).count());
	}
	
	@Test
	public void ensureSingleDoubleBondSearchDoNotReturnKekulizeBenzene() throws Exception{

		String structure="CC=C";
		structureIndexer.add("1", "C1=CC=CC=C1");
		assertEquals(0,StreamUtil.forEnumeration(structureIndexer.substructure(structure, 10)).count());
	}
	
	@Test
	public void ensureSingleDoubleBondSearchDoesReturnSingelDoubleBondStructure() throws Exception{

		String structure="CC=C";
		structureIndexer.add("1", "CC=CC=CC");
		assertEquals(0,StreamUtil.forEnumeration(structureIndexer.substructure(structure, 10)).count());
	}
	
	@Test
	public void ensureBasicSingleDoubleBondSearchDoesReturnSingelDoubleBondStructure() throws Exception{


		Chemical p=Chemical.parseMol("\n" + 
				"   JSDraw209162018192D\n" + 
				"\n" + 
				"  3  2  0  0  0  0            999 V2000\n" + 
				"   18.0440   -9.4640    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n" + 
				"   19.3950   -8.6840    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n" + 
				"   20.7460   -9.4640    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n" + 
				"  1  2  1  0  0  0  0\n" + 
				"  2  3  2  0  0  0  0\n" + 
				"M  END");
		
		Chemical t=Chemical.parseMol("\n" + 
				"   JSDraw209162018202D\n" + 
				"\n" + 
				"  6  5  0  0  0  0            999 V2000\n" + 
				"   19.1880  -14.0400    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n" + 
				"   17.8370  -13.2600    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n" + 
				"   17.8370  -11.7000    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n" + 
				"   20.5390  -13.2600    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n" + 
				"   20.5390  -11.7000    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n" + 
				"   19.1880  -10.9200    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n" + 
				"  2  3  1  0  0  0  0\n" + 
				"  1  4  1  0  0  0  0\n" + 
				"  4  5  2  0  0  0  0\n" + 
				"  5  6  1  0  0  0  0\n" + 
				"  6  3  2  0  0  0  0\n" + 
				"M  END");
		
		
		
		Optional<int[]> hit = MolSearcherFactory.create(p).search(t);
		
		assertEquals("true",""+hit.isPresent());
	}
	
	@Test
	public void ensureBasicBenzeneSearchWorksKekule() throws Exception{


		
		Chemical p=Chemical.parseMol("\n" + 
				"   JSDraw209162017232D\n" + 
				"\n" + 
				"  6  6  0  0  0  0            999 V2000\n" + 
				"   26.4680  -12.3240    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n" + 
				"   25.1170  -11.5440    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n" + 
				"   25.1170   -9.9840    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n" + 
				"   27.8190  -11.5440    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n" + 
				"   27.8190   -9.9840    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n" + 
				"   26.4680   -9.2040    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n" + 
				"  1  2  2  0  0  0  0\n" + 
				"  2  3  1  0  0  0  0\n" + 
				"  1  4  1  0  0  0  0\n" + 
				"  4  5  2  0  0  0  0\n" + 
				"  5  6  1  0  0  0  0\n" + 
				"  6  3  2  0  0  0  0\n" + 
				"M  END");
		Chemical t=Chemical.parseMol("\n" + 
				"   JSDraw209162017232D\n" + 
				"\n" + 
				"  6  6  0  0  0  0            999 V2000\n" + 
				"   26.4680  -12.3240    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n" + 
				"   25.1170  -11.5440    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n" + 
				"   25.1170   -9.9840    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n" + 
				"   27.8190  -11.5440    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n" + 
				"   27.8190   -9.9840    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n" + 
				"   26.4680   -9.2040    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n" + 
				"  1  2  2  0  0  0  0\n" + 
				"  2  3  1  0  0  0  0\n" + 
				"  1  4  1  0  0  0  0\n" + 
				"  4  5  2  0  0  0  0\n" + 
				"  5  6  1  0  0  0  0\n" + 
				"  6  3  2  0  0  0  0\n" + 
				"M  END");
		
		
		
		Optional<int[]> hit = MolSearcherFactory.create(p).search(t);
		
		assertEquals("true",""+hit.isPresent());
	}
	
	@Test
	public void ensureBasicBenzeneSearchWorksAromatic() throws Exception{


		
		Chemical p=Chemical.parseMol("\n" + 
				"   JSDraw209162017232D\n" + 
				"\n" + 
				"  6  6  0  0  0  0            999 V2000\n" + 
				"   26.4680  -12.3240    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n" + 
				"   25.1170  -11.5440    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n" + 
				"   25.1170   -9.9840    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n" + 
				"   27.8190  -11.5440    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n" + 
				"   27.8190   -9.9840    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n" + 
				"   26.4680   -9.2040    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n" + 
				"  1  2  4  0  0  0  0\n" + 
				"  2  3  4  0  0  0  0\n" + 
				"  1  4  4  0  0  0  0\n" + 
				"  4  5  4  0  0  0  0\n" + 
				"  5  6  4  0  0  0  0\n" + 
				"  6  3  4  0  0  0  0\n" + 
				"M  END");
		Chemical t=Chemical.parseMol("\n" + 
				"   JSDraw209162017232D\n" + 
				"\n" + 
				"  6  6  0  0  0  0            999 V2000\n" + 
				"   26.4680  -12.3240    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n" + 
				"   25.1170  -11.5440    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n" + 
				"   25.1170   -9.9840    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n" + 
				"   27.8190  -11.5440    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n" + 
				"   27.8190   -9.9840    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n" + 
				"   26.4680   -9.2040    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n" + 
				"  1  2  4  0  0  0  0\n" + 
				"  2  3  4  0  0  0  0\n" + 
				"  1  4  4  0  0  0  0\n" + 
				"  4  5  4  0  0  0  0\n" + 
				"  5  6  4  0  0  0  0\n" + 
				"  6  3  4  0  0  0  0\n" + 
				"M  END");
		
		
		
		Optional<int[]> hit = MolSearcherFactory.create(p).search(t);
		
		assertEquals("true",""+hit.isPresent());
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
	public void ensureBasicSSSCanGivePositiveResult() throws Exception{
		
		
		Chemical p=Chemical.parseMol("\n" + 
				"   JSDraw209162010482D\n" + 
				"\n" + 
				"  1  0  0  0  0  0            999 V2000\n" + 
				"   15.8080   -7.0436    0.0000 O   0  0  0  0  0  0  0  0  0  0  0  0\n" + 
				"M  END");
		Chemical t=Chemical.parseMol("\n" + 
				"   JSDraw209162010482D\n" + 
				"\n" + 
				"  1  0  0  0  0  0            999 V2000\n" + 
				"   15.8080   -7.0436    0.0000 O   0  0  0  0  0  0  0  0  0  0  0  0\n" + 
				"M  END");
		
		
		
		Optional<int[]> hit = MolSearcherFactory.create(p).search(t);
		
		assertEquals("true",""+hit.isPresent());
		
		
	}
	
	@Test
	public void ensureBasicSSSCanGivePositiveResultOnStrictSubstructure() throws Exception{
		
		
		Chemical p=Chemical.parseMol("\n" + 
				"   JSDraw209162010482D\n" + 
				"\n" + 
				"  1  0  0  0  0  0            999 V2000\n" + 
				"   15.8080   -7.0436    0.0000 O   0  0  0  0  0  0  0  0  0  0  0  0\n" + 
				"M  END");
		Chemical t=Chemical.parseMol("\n" + 
				"   JSDraw209162016432D\n" + 
				"\n" + 
				"  2  0  0  0  0  0            999 V2000\n" + 
				"   19.0840   -8.3720    0.0000 O   0  0  0  0  0  0  0  0  0  0  0  0\n" + 
				"   22.7760   -9.9320    0.0000 O   0  0  0  0  0  0  0  0  0  0  0  0\n" + 
				"M  END");
		
		
		
		Optional<int[]> hit = MolSearcherFactory.create(p).search(t);
		
		assertEquals("true",""+hit.isPresent());
		
		
	}
	
	@Test
	public void ensureBasicSSSCanGivePositiveResultOnStrictSubstructureConnected() throws Exception{
		
		
		Chemical p=Chemical.parseMol("\n" + 
				"   JSDraw209162010482D\n" + 
				"\n" + 
				"  1  0  0  0  0  0            999 V2000\n" + 
				"   15.8080   -7.0436    0.0000 O   0  0  0  0  0  0  0  0  0  0  0  0\n" + 
				"M  END");
		Chemical t=Chemical.parseMol("\n" + 
				"   JSDraw209162016482D\n" + 
				"\n" + 
				"  2  1  0  0  0  0            999 V2000\n" + 
				"   19.0840   -8.3720    0.0000 O   0  0  0  0  0  0  0  0  0  0  0  0\n" + 
				"   22.7760   -9.9320    0.0000 O   0  0  0  0  0  0  0  0  0  0  0  0\n" + 
				"  1  2  1  0  0  0  0\n" + 
				"M  END");
		
		
		
		Optional<int[]> hit = MolSearcherFactory.create(p).search(t);
		
		assertEquals("true",""+hit.isPresent());		
	}
	
	@Test
	public void ensureBasicSSSCanGivePositiveResultOnStrictSubstructureConnectedQueryConnectedTarget() throws Exception{
		
		
		Chemical p=Chemical.parseMol("\n" + 
				"   JSDraw209162016482D\n" + 
				"\n" + 
				"  2  1  0  0  0  0            999 V2000\n" + 
				"   19.0840   -8.3720    0.0000 O   0  0  0  0  0  0  0  0  0  0  0  0\n" + 
				"   22.7760   -9.9320    0.0000 O   0  0  0  0  0  0  0  0  0  0  0  0\n" + 
				"  1  2  1  0  0  0  0\n" + 
				"M  END");
		Chemical t=Chemical.parseMol("\n" + 
				"   JSDraw209162016482D\n" + 
				"\n" + 
				"  2  1  0  0  0  0            999 V2000\n" + 
				"   19.0840   -8.3720    0.0000 O   0  0  0  0  0  0  0  0  0  0  0  0\n" + 
				"   22.7760   -9.9320    0.0000 O   0  0  0  0  0  0  0  0  0  0  0  0\n" + 
				"  1  2  1  0  0  0  0\n" + 
				"M  END");
		
		
		
		Optional<int[]> hit = MolSearcherFactory.create(p).search(t);
		
		assertEquals("true",""+hit.isPresent());		
	}
	
	@Test
	public void ensureBasicSSSCanGivePositiveResultOnStrictSubstructureDisconnectedQueryConnectedTarget() throws Exception{
		
		
		Chemical p=Chemical.parseMol("\n" + 
				"   JSDraw209162016482D\n" + 
				"\n" + 
				"  2  0  0  0  0  0            999 V2000\n" + 
				"   19.0840   -8.3720    0.0000 O   0  0  0  0  0  0  0  0  0  0  0  0\n" + 
				"   22.7760   -9.9320    0.0000 O   0  0  0  0  0  0  0  0  0  0  0  0\n" + 
				"M  END");
		Chemical t=Chemical.parseMol("\n" + 
				"   JSDraw209162016482D\n" + 
				"\n" + 
				"  2  1  0  0  0  0            999 V2000\n" + 
				"   19.0840   -8.3720    0.0000 O   0  0  0  0  0  0  0  0  0  0  0  0\n" + 
				"   22.7760   -9.9320    0.0000 O   0  0  0  0  0  0  0  0  0  0  0  0\n" + 
				"  1  2  1  0  0  0  0\n" + 
				"M  END");
		
		
		
		Optional<int[]> hit = MolSearcherFactory.create(p).search(t);
		
		assertEquals("true",""+hit.isPresent());		
	}
	
	@Test
	public void ensureBasicSSSCanGiveNegativeResultOnCyclohexaneCase() throws Exception{
		
		
		Chemical p=Chemical.parseMol("\n" + 
				"   JSDraw209162014542D\n" + 
				"\n" + 
				"  6  6  0  0  0  0              0 V2000\n" + 
				"   23.1920   -9.2560    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n" + 
				"   21.8410   -8.4760    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n" + 
				"   21.8410   -6.9160    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n" + 
				"   23.1920   -6.1360    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n" + 
				"   24.5430   -6.9160    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n" + 
				"   24.5430   -8.4760    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n" + 
				"  1  2  1  0  0  0  0\n" + 
				"  2  3  1  0  0  0  0\n" + 
				"  3  4  1  0  0  0  0\n" + 
				"  4  5  1  0  0  0  0\n" + 
				"  5  6  1  0  0  0  0\n" + 
				"  6  1  1  0  0  0  0\n" + 
				"M  END");
		Chemical t=Chemical.parseMol("\n" + 
				"  Symyx   08281518122D 1   1.00000     0.00000     0\n" + 
				"\n" + 
				" 27 29  0  0  1  0            999 V2000\n" + 
				"    7.1375   -2.2042    0.0000 N   0  0  3  0  0  0           0  0  0\n" + 
				"    6.0500   -1.8583    0.0000 C   0  0  0  0  0  0           0  0  0\n" + 
				"    6.0500   -0.7125    0.0000 C   0  0  0  0  0  0           0  0  0\n" + 
				"    7.1292   -0.3542    0.0000 N   0  0  0  0  0  0           0  0  0\n" + 
				"    7.8083   -1.2792    0.0000 C   0  0  0  0  0  0           0  0  0\n" + 
				"    5.0042   -0.0375    0.0000 C   0  0  0  0  0  0           0  0  0\n" + 
				"    5.0042   -2.4667    0.0000 N   0  0  0  0  0  0           0  0  0\n" + 
				"    3.9333   -0.6458    0.0000 N   0  0  0  0  0  0           0  0  0\n" + 
				"    3.9333   -1.8583    0.0000 C   0  0  0  0  0  0           0  0  0\n" + 
				"    5.0042    1.1417    0.0000 N   0  0  0  0  0  0           0  0  0\n" + 
				"    7.1042   -3.9750    0.0000 C   0  0  1  0  0  0           0  0  0\n" + 
				"    6.7583   -5.0833    0.0000 C   0  0  1  0  0  0           0  0  0\n" + 
				"    6.1333   -3.2625    0.0000 O   0  0  0  0  0  0           0  0  0\n" + 
				"    5.1750   -3.9625    0.0000 C   0  0  1  0  0  0           0  0  0\n" + 
				"    5.5583   -5.0833    0.0000 C   0  0  1  0  0  0           0  0  0\n" + 
				"    4.1500   -3.3708    0.0000 C   0  0  0  0  0  0           0  0  0\n" + 
				"    3.1250   -3.9625    0.0000 S   0  3  2  0  0  0           0  0  0\n" + 
				"    2.1000   -3.3708    0.0000 C   0  0  0  0  0  0           0  0  0\n" + 
				"    1.0750   -3.9625    0.0000 C   0  0  0  0  0  0           0  0  0\n" + 
				"    0.0500   -3.3708    0.0000 C   0  0  1  0  0  0           0  0  0\n" + 
				"   -0.9750   -3.9625    0.0000 C   0  0  0  0  0  0           0  0  0\n" + 
				"   -2.0000   -3.3708    0.0000 O   0  5  0  0  0  0           0  0  0\n" + 
				"    5.2458   -6.2250    0.0000 O   0  0  0  0  0  0           0  0  0\n" + 
				"    7.3458   -6.1083    0.0000 O   0  0  0  0  0  0           0  0  0\n" + 
				"    3.1208   -5.1417    0.0000 C   0  0  0  0  0  0           0  0  0\n" + 
				"    0.0458   -2.1917    0.0000 N   0  0  0  0  0  0           0  0  0\n" + 
				"   -0.9792   -5.1417    0.0000 O   0  0  0  0  0  0           0  0  0\n" + 
				" 11  1  1  1     0  0\n" + 
				" 12 11  1  0     0  0\n" + 
				" 13 11  1  0     0  0\n" + 
				" 14 13  1  0     0  0\n" + 
				" 15 12  1  0     0  0\n" + 
				" 15 14  1  0     0  0\n" + 
				"  6  3  1  0     0  0\n" + 
				" 14 16  1  1     0  0\n" + 
				"  7  2  1  0     0  0\n" + 
				" 16 17  1  0     0  0\n" + 
				"  8  9  1  0     0  0\n" + 
				" 17 18  1  0     0  0\n" + 
				"  9  7  2  0     0  0\n" + 
				" 18 19  1  0     0  0\n" + 
				" 10  6  1  0     0  0\n" + 
				" 19 20  1  0     0  0\n" + 
				"  3  4  1  0     0  0\n" + 
				" 20 21  1  0     0  0\n" + 
				"  8  6  2  0     0  0\n" + 
				" 21 22  1  0     0  0\n" + 
				" 15 23  1  6     0  0\n" + 
				"  2  1  1  0     0  0\n" + 
				" 12 24  1  6     0  0\n" + 
				"  3  2  2  0     0  0\n" + 
				" 17 25  1  1     0  0\n" + 
				"  4  5  2  0     0  0\n" + 
				" 20 26  1  1     0  0\n" + 
				"  5  1  1  0     0  0\n" + 
				" 21 27  2  0     0  0\n" + 
				"M  CHG  2  17   1  22  -1\n" + 
				"M  END");
		
		
		
		Optional<int[]> hit = MolSearcherFactory.create(p).search(t);
		
		if(hit.isPresent()){
			System.out.println(Arrays.toString(hit.get()));
		}
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
	public void ensureCyclohexaneSearchInNonCyclohexaneStructureReturnsNothing() throws Exception{

		String structure="C1CCCCC1";
		structureIndexer.add("1", "\n" + 
				"  Symyx   08281518122D 1   1.00000     0.00000     0\n" + 
				"\n" + 
				" 27 29  0  0  1  0            999 V2000\n" + 
				"    7.1375   -2.2042    0.0000 N   0  0  3  0  0  0           0  0  0\n" + 
				"    6.0500   -1.8583    0.0000 C   0  0  0  0  0  0           0  0  0\n" + 
				"    6.0500   -0.7125    0.0000 C   0  0  0  0  0  0           0  0  0\n" + 
				"    7.1292   -0.3542    0.0000 N   0  0  0  0  0  0           0  0  0\n" + 
				"    7.8083   -1.2792    0.0000 C   0  0  0  0  0  0           0  0  0\n" + 
				"    5.0042   -0.0375    0.0000 C   0  0  0  0  0  0           0  0  0\n" + 
				"    5.0042   -2.4667    0.0000 N   0  0  0  0  0  0           0  0  0\n" + 
				"    3.9333   -0.6458    0.0000 N   0  0  0  0  0  0           0  0  0\n" + 
				"    3.9333   -1.8583    0.0000 C   0  0  0  0  0  0           0  0  0\n" + 
				"    5.0042    1.1417    0.0000 N   0  0  0  0  0  0           0  0  0\n" + 
				"    7.1042   -3.9750    0.0000 C   0  0  1  0  0  0           0  0  0\n" + 
				"    6.7583   -5.0833    0.0000 C   0  0  1  0  0  0           0  0  0\n" + 
				"    6.1333   -3.2625    0.0000 O   0  0  0  0  0  0           0  0  0\n" + 
				"    5.1750   -3.9625    0.0000 C   0  0  1  0  0  0           0  0  0\n" + 
				"    5.5583   -5.0833    0.0000 C   0  0  1  0  0  0           0  0  0\n" + 
				"    4.1500   -3.3708    0.0000 C   0  0  0  0  0  0           0  0  0\n" + 
				"    3.1250   -3.9625    0.0000 S   0  3  2  0  0  0           0  0  0\n" + 
				"    2.1000   -3.3708    0.0000 C   0  0  0  0  0  0           0  0  0\n" + 
				"    1.0750   -3.9625    0.0000 C   0  0  0  0  0  0           0  0  0\n" + 
				"    0.0500   -3.3708    0.0000 C   0  0  1  0  0  0           0  0  0\n" + 
				"   -0.9750   -3.9625    0.0000 C   0  0  0  0  0  0           0  0  0\n" + 
				"   -2.0000   -3.3708    0.0000 O   0  5  0  0  0  0           0  0  0\n" + 
				"    5.2458   -6.2250    0.0000 O   0  0  0  0  0  0           0  0  0\n" + 
				"    7.3458   -6.1083    0.0000 O   0  0  0  0  0  0           0  0  0\n" + 
				"    3.1208   -5.1417    0.0000 C   0  0  0  0  0  0           0  0  0\n" + 
				"    0.0458   -2.1917    0.0000 N   0  0  0  0  0  0           0  0  0\n" + 
				"   -0.9792   -5.1417    0.0000 O   0  0  0  0  0  0           0  0  0\n" + 
				" 11  1  1  1     0  0\n" + 
				" 12 11  1  0     0  0\n" + 
				" 13 11  1  0     0  0\n" + 
				" 14 13  1  0     0  0\n" + 
				" 15 12  1  0     0  0\n" + 
				" 15 14  1  0     0  0\n" + 
				"  6  3  1  0     0  0\n" + 
				" 14 16  1  1     0  0\n" + 
				"  7  2  1  0     0  0\n" + 
				" 16 17  1  0     0  0\n" + 
				"  8  9  1  0     0  0\n" + 
				" 17 18  1  0     0  0\n" + 
				"  9  7  2  0     0  0\n" + 
				" 18 19  1  0     0  0\n" + 
				" 10  6  1  0     0  0\n" + 
				" 19 20  1  0     0  0\n" + 
				"  3  4  1  0     0  0\n" + 
				" 20 21  1  0     0  0\n" + 
				"  8  6  2  0     0  0\n" + 
				" 21 22  1  0     0  0\n" + 
				" 15 23  1  6     0  0\n" + 
				"  2  1  1  0     0  0\n" + 
				" 12 24  1  6     0  0\n" + 
				"  3  2  2  0     0  0\n" + 
				" 17 25  1  1     0  0\n" + 
				"  4  5  2  0     0  0\n" + 
				" 20 26  1  1     0  0\n" + 
				"  5  1  1  0     0  0\n" + 
				" 21 27  2  0     0  0\n" + 
				"M  CHG  2  17   1  22  -1\n" + 
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
