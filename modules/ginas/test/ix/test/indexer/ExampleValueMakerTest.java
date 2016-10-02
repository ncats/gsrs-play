package ix.test.indexer;

import org.junit.Test;

import ix.ginas.indexers.ExampleValueMaker;
import ix.ginas.models.v1.Relationship;
import ix.ginas.models.v1.Substance;
import ix.test.builder.SubstanceBuilder;

/**
 * Test for ExampleValueMaker. The purpose of this test is to ensure
 * that the ExampleValueMaker is properly registered, and that it is
 * called and computes the facets / values as expected.
 * @author peryeata
 *
 */
public class ExampleValueMakerTest extends SubstanceIndexerValueMakerTest<ExampleValueMaker> {

	@Override
	public Class<ExampleValueMaker> getIndexMakerClass() {
		return ExampleValueMaker.class;
	}
	

	@Test
	public void addActiveMoietyFacetTest(){
		
		Substance s=new SubstanceBuilder()
			.asChemical()
			.setStructure("CCC1CCCC1")
			.addName("Test Guy")
			.addReflexiveActiveMoietyRelationship()
			.build();
		this.testIndexableValuesHasFacet(s, ExampleValueMaker.MOIETY_TYPE_FACET, Relationship.ACTIVE_MOIETY_RELATIONSHIP_TYPE);
	}
	

}
