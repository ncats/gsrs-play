package ix.ginas.indexers;

import ix.core.search.text.IndexValueMaker;
import ix.core.search.text.IndexableValue;
import ix.ginas.models.v1.Substance;

import java.util.function.Consumer;

public class USANDateIndexValueMaker implements IndexValueMaker<Substance> {
    private final String DOCUMENT_TYPE = "USAN DATE";
    private final String FACET_NAME = "USAN DATE";

    @Override
    public void createIndexableValues(Substance substance, Consumer<IndexableValue> consumer) {
        play.Logger.trace("starting USANDateIndexValueMaker.createIndexableValues");
        substance.references.forEach(r -> {
            if (r.citation!= null && r.docType.equals(DOCUMENT_TYPE)) {
                play.Logger.trace("found a value");
                consumer.accept(IndexableValue
                        .simpleFacetStringValue(FACET_NAME, r.citation));
            }
        });
    }
}
