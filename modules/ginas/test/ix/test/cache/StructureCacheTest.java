package ix.test.cache;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import ix.AbstractGinasClassServerTest;
import ix.core.controllers.StructureFactory;
import ix.core.models.Structure;
import ix.core.plugins.IxCache;
import ix.ginas.models.v1.GinasChemicalStructure;
import ix.ginas.modelBuilders.SubstanceBuilder;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.ArrayList;
import java.util.List;

@RunWith(Parameterized.class)
public class StructureCacheTest extends AbstractGinasClassServerTest{


	@Parameterized.Parameters
	public static List<Object[]> data(){
		List<Object[]> list = new ArrayList<>();
		list.add(new Object[]{Boolean.TRUE});
		list.add(new Object[]{Boolean.FALSE});
		//ix.cache.useFileDb=false
		return list;
	}

	private boolean usePersistenceCache;

	public StructureCacheTest(boolean usePersistenceCache) {
		this.usePersistenceCache = usePersistenceCache;
	}

	@Before
	public void setPersistenceCache(){
		ts.modifyConfig("ix.cache.useFileDb", usePersistenceCache);
		ts.restart();
	}

	@Test
	public void testStructureCacheStoresAndRetrieves(){
		GinasChemicalStructure s=new SubstanceBuilder()
					.asChemical()
					.setStructure("C1CCCCC1")
					.build()
					.structure;
		StructureFactory.saveTempStructure(s);
		Structure s2=StructureFactory.getTempStructure(s.id.toString());
		
		assertStructureEquals(s,s2);
		
	}
	
	@Test
	public void testStructureCacheStoresAndRetrievesAfterCacheCleared(){
		GinasChemicalStructure s=new SubstanceBuilder()
					.asChemical()
					.setStructure("C1CCCCC1")
					.build()
					.structure;
		StructureFactory.saveTempStructure(s);
		
		IxCache.clearCache();
		
		Structure s2=StructureFactory.getTempStructure(s.id.toString());
		if(usePersistenceCache) {
			assertStructureEquals(s, s2);
		}else{
			assertNull(s2);
		}
		
	}
	@Test
	public void testStructureCacheStoresAndRetrievesAfterServerRestart(){
		GinasChemicalStructure s=new SubstanceBuilder()
					.asChemical()
					.setStructure("C1CCCCC1")
					.build()
					.structure;
		StructureFactory.saveTempStructure(s);
		
		ts.restart();
		
		Structure s2=StructureFactory.getTempStructure(s.id.toString());

		if(usePersistenceCache) {
			assertStructureEquals(s, s2);
		}else{
			assertNull(s2);
		}
		
	}
	
	public void assertStructureEquals(Structure s1, Structure s2){
		assertNotNull(s1);
		assertNotNull(s2);
		assertEquals(s1.molfile,s2.molfile);
		assertEquals(s1.smiles,s2.smiles);
		
		assertEquals(s1.id,s2.id);
		assertEquals(s1.created,s2.created);
		
		
	}
}
