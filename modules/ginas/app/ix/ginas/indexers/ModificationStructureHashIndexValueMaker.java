package ix.ginas.indexers;

import java.util.Objects;
import java.util.function.Consumer;

import ix.core.search.text.IndexValueMaker;
import ix.core.search.text.IndexableValue;
import ix.ginas.controllers.v1.SubstanceFactory;
import ix.ginas.models.v1.ChemicalSubstance;
import ix.ginas.models.v1.Substance;
import ix.utils.UUIDUtil;


/**
 * Adds lychi index values to index for the chemical modifications of a substance
 *
 * @author peryeata
 *
 */
public class ModificationStructureHashIndexValueMaker implements IndexValueMaker<Substance>{

    //This is the method which does the work
    @Override
    public void createIndexableValues(Substance s, Consumer<IndexableValue> consumer) {
        if(s.modifications!=null){
            if(!s.modifications.structuralModifications.isEmpty()){
                createModificationHashes(s, consumer);
            }
        }
    }


    public void createModificationHashes(Substance s, Consumer<IndexableValue> consumer) {
        s.modifications.structuralModifications
                .stream()
				 .filter(Objects::nonNull)
                .forEach(mc->{
                    String refuuid=mc.molecularFragment.refuuid;
                    if(UUIDUtil.isUUID(refuuid)){
                        Substance component = SubstanceFactory.getFullSubstance(mc.molecularFragment);
                        if(component instanceof ChemicalSubstance){
                            extractHashes((ChemicalSubstance)component, consumer);
                        }
                    }
                });

    }

    public void extractHashes(ChemicalSubstance s, Consumer<IndexableValue> consumer) {

        //consumer.accept(IndexableValue.simpleStringValue("root_structure_properties_term", lychi3));
		String stereoInsensitive=s.structure.getStereoInsensitiveHash();
		String exact=s.structure.getExactHash();
		if(stereoInsensitive!=null){
			consumer.accept(IndexableValue.simpleStringValue("root_structure_properties_term", stereoInsensitive));
		}
		if(exact!=null){
			consumer.accept(IndexableValue.simpleStringValue("root_structure_properties_term", exact));
		}
        s.moieties.stream().forEach(m->{
			String sins=m.structure.getStereoInsensitiveHash();
			String exa=m.structure.getExactHash();
			if(sins!=null){
				consumer.accept(IndexableValue.simpleStringValue("root_moieties_structure_properties_term", sins));
			}
			if(exa!=null){
				consumer.accept(IndexableValue.simpleStringValue("root_moieties_structure_properties_term", exa));
			}
        });

    }

}
