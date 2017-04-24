package ix.ginas.indexers;

import ix.core.search.text.IndexValueMaker;
import ix.core.search.text.IndexableValue;
import ix.ginas.models.v1.Substance;

import java.util.function.Consumer;

/**
 * Created by VenkataSaiRa.Chavali on 4/20/2017.
 */
public class DeprecatedIndexValueMaker implements IndexValueMaker<Substance> {

    @Override
    public void createIndexableValues(Substance substance, Consumer<IndexableValue> consumer) {
        if (substance.deprecated)
            consumer.accept(IndexableValue.simpleFacetStringValue("Deprecated","Deprecated"));
    }
}