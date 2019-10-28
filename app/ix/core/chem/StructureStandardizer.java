package ix.core.chem;

import gov.nih.ncats.molwitch.Chemical;
import ix.core.models.Structure;
import ix.core.models.Value;

import java.io.IOException;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Created by katzelda on 7/28/19.
 */
public interface StructureStandardizer {

    String canonicalSmiles(Structure s, String mol);
    Chemical standardize(Chemical orig, Supplier<String> molSupplier, Consumer<Value> valueConsumer) throws IOException;
}
