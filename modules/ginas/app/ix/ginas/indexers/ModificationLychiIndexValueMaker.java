package ix.ginas.indexers;

import java.util.function.Consumer;

import ix.core.search.text.IndexValueMaker;
import ix.core.search.text.IndexableValue;
import ix.ginas.controllers.v1.SubstanceFactory;
import ix.ginas.models.v1.ChemicalSubstance;
import ix.ginas.models.v1.MixtureSubstance;
import ix.ginas.models.v1.Substance;
import ix.utils.UUIDUtil;


/**
 * Adds lychi index values to index for the chemical modifications of a substance
 *
 * @author peryeata
 *
 */
public class ModificationLychiIndexValueMaker implements IndexValueMaker<Substance>{

    //This is the method which does the work
    @Override
    public void createIndexableValues(Substance s, Consumer<IndexableValue> consumer) {
        if(s.modifications!=null){
            if(!s.modifications.structuralModifications.isEmpty()){
                createModificationLychis(s, consumer);
            }
        }
    }


    public void createModificationLychis(Substance s, Consumer<IndexableValue> consumer) {
        s.modifications.structuralModifications
                .stream()
                .forEach(mc->{
                    String refuuid=mc.molecularFragment.refuuid;
                    if(UUIDUtil.isUUID(refuuid)){
                        Substance component = SubstanceFactory.getFullSubstance(mc.molecularFragment);
                        if(component instanceof ChemicalSubstance){
                            extractLychis((ChemicalSubstance)component, consumer);
                        }
                    }
                });

    }

    public void extractLychis(ChemicalSubstance s, Consumer<IndexableValue> consumer) {

        //consumer.accept(IndexableValue.simpleStringValue("root_structure_properties_term", lychi3));
        consumer.accept(IndexableValue.simpleStringValue("root_structure_properties_term", s.structure.getStereoInsensitiveHash()));
        consumer.accept(IndexableValue.simpleStringValue("root_structure_properties_term", s.structure.getExactHash()));

        s.moieties.stream().forEach(m->{
            consumer.accept(IndexableValue.simpleStringValue("root_moieties_structure_properties_term", m.structure.getStereoInsensitiveHash()));
            consumer.accept(IndexableValue.simpleStringValue("root_moieties_structure_properties_term", m.structure.getExactHash()));
        });

    }

}
