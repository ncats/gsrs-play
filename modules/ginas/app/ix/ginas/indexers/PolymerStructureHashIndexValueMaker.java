package ix.ginas.indexers;

import java.util.function.Consumer;

import ix.core.chem.StructureProcessorTask;
import ix.core.search.text.IndexValueMaker;
import ix.core.search.text.IndexableValue;
import ix.ginas.models.v1.PolymerSubstance;
import ix.ginas.models.v1.Substance;
/**
 * Adds structure hash index values to index for the polymer substances. Note that this is a pretty
 * hacky way to make these work.
 *
 * @author peryeata
 *
 */
public class PolymerStructureHashIndexValueMaker implements IndexValueMaker<Substance>{

    //This is the method which does the work
    @Override
    public void createIndexableValues(Substance s, Consumer<IndexableValue> consumer) {
        if(s instanceof PolymerSubstance){
            createPolymerStructureHashes((PolymerSubstance)s, consumer);
        }
    }


    public void createPolymerStructureHashes(PolymerSubstance s, Consumer<IndexableValue> consumer) {
        try{

            StructureProcessorTask sst=new StructureProcessorTask.Builder()
                    .mol(s.polymer.displayStructure.molfile)
                    .build();

            sst.instrument();


            consumer.accept(IndexableValue.simpleStringValue("root_structure_properties_term", sst.getStructure().getStereoInsensitiveHash()));
            consumer.accept(IndexableValue.simpleStringValue("root_structure_properties_term", sst.getStructure().getExactHash()));


            sst.getComponents().forEach(m->{
                consumer.accept(IndexableValue.simpleStringValue("root_moieties_structure_properties_term", m.getStereoInsensitiveHash()));
                consumer.accept(IndexableValue.simpleStringValue("root_moieties_structure_properties_term", m.getExactHash()));
            });

        }catch(Exception e){
            e.printStackTrace();
        }

    }

}
