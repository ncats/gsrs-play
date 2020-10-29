package ix.ginas.indexers;

import java.util.function.Consumer;

import ix.core.search.text.IndexValueMaker;
import ix.core.search.text.IndexableValue;
import ix.ginas.models.v1.Substance;


/**
 * Adds Normalized Name values to text index for {@link Substance} objects.
 *
 *
 * @author epuzanov
 *
 */
public class NormalizedNameIndexValueMaker implements IndexValueMaker<Substance>{

    @Override
    public void createIndexableValues(Substance s, Consumer<IndexableValue> consumer) {
        try {
            s.names.stream().forEach(n->{
                consumer.accept(IndexableValue.simpleStringValue("root_names_normName", n.getNormName()));
            });
        }catch(Exception e) {
            e.printStackTrace();
        }
    }
}
