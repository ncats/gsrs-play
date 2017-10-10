package ix.ginas.indexers;

import ix.core.search.text.IndexValueMaker;
import ix.core.search.text.IndexableValue;
import ix.ginas.models.v1.Substance;

import java.util.function.Consumer;

/**
 * Created by VenkataSaiRa.Chavali on 4/20/2017.
 */
public class MolecularWeightPropertyIndexValueMaker implements IndexValueMaker<Substance> {

    @Override
    public void createIndexableValues(Substance substance, Consumer<IndexableValue> consumer) {
        if (substance.properties != null) {
            substance.properties.stream()
                    .filter(a -> a.getName().toUpperCase().contains("MOL_WEIGHT"))
                    .forEach(p -> {
                        Double avg = p.getValue().average;
                        if (avg != null) {
                            consumer.accept(IndexableValue.simpleFacetLongValue("Molecular Weight", Math.round(avg), new long[]{0, 200, 400, 600, 800, 1000}));
                        }
                    });
        }
    }
}