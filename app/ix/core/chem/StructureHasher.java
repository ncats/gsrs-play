package ix.core.chem;

import gov.nih.ncats.molwitch.Chemical;

import java.util.function.BiConsumer;

public interface StructureHasher {

    //TODO: I don't know why we need both the Chemical and the Mol string. That seems redundant
    void hash(Chemical chem, String mol, BiConsumer<String, String> keyValueConsumer);
}
