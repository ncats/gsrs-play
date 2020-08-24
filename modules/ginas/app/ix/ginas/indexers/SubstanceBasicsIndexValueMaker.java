package ix.ginas.indexers;

import java.util.function.Consumer;

import ix.ginas.models.v1.Substance;
import ix.core.search.text.IndexValueMaker;
import ix.core.search.text.IndexableValue;
import ix.core.search.text.IndexableValueFromRaw;

/**
 * Facet (categorization for query results) based on top-level fields in a
 * Substance
 *
 * @author mitch miller
 */
public class SubstanceBasicsIndexValueMaker implements IndexValueMaker<Substance>
{

	private final String SUBSTANCE_CREATED_BY_FACET = "Record Created By";

	@Override
	public void createIndexableValues(Substance substance, Consumer<IndexableValue> consumer) {

		makeRecordBasicsValues(substance, consumer);
	}

	private void makeRecordBasicsValues(Substance s, Consumer<IndexableValue> consumer) {
		consumer.accept(new IndexableValueFromRaw(SUBSTANCE_CREATED_BY_FACET, s.createdBy.username).dynamic());
	}
}
