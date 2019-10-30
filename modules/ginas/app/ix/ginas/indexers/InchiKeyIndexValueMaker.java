package ix.ginas.indexers;

import java.util.function.Consumer;

import ix.core.search.text.IndexValueMaker;
import ix.core.search.text.IndexableValue;
import ix.ginas.models.v1.ChemicalSubstance;
import ix.ginas.models.v1.Substance;


/**
 * Adds inchikey values to text index for {@link ChemicalSubstance} objects.
 *
 *
 * @author peryeata
 *
 */
public class InchiKeyIndexValueMaker implements IndexValueMaker<Substance>{

    @Override
    public void createIndexableValues(Substance s, Consumer<IndexableValue> consumer) {
        if(s instanceof ChemicalSubstance) {
            try {
                extractInchiKeys((ChemicalSubstance)s, consumer);
            }catch(Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static void extractInchiKeys(ChemicalSubstance s, Consumer<IndexableValue> consumer) {
        consumer.accept(IndexableValue.simpleStringValue("root_structure_inchikey", s.structure.getInChIKey()));
        s.moieties.stream().forEach(m->{
            consumer.accept(IndexableValue.simpleStringValue("root_moieties_structure_inchikey", m.structure.getInChIKey()));
        });
    }

}
