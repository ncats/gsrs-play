package ix.test.indexer;

import ix.core.search.text.IndexValueMaker;
import ix.ginas.models.v1.Substance;

public abstract class SubstanceIndexerValueMakerTest<U extends IndexValueMaker<Substance>> extends AbstractIndexerValueMakerTest<Substance, U>{

	@Override
	public Class<Substance> getEntityClass() {
		return Substance.class;
	}
	
	
}
