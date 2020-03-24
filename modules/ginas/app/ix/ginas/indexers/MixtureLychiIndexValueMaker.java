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
 * Adds lychi index values to index for the chemical components of a mixture
 * 
 * @author peryeata
 *
 */
public class MixtureLychiIndexValueMaker implements IndexValueMaker<Substance>{

	//This is the method which does the work
	@Override
	public void createIndexableValues(Substance s, Consumer<IndexableValue> consumer) {
		
		if(s instanceof MixtureSubstance){
			createMixtureLychis((MixtureSubstance)s, consumer);
		}
	}
	
	
	public void createMixtureLychis(MixtureSubstance s, Consumer<IndexableValue> consumer) {
		s.mixture.components
		         .stream()
		         .forEach(mc->{
		        	 String refuuid=mc.substance.refuuid;
		        	 if(UUIDUtil.isUUID(refuuid)){
		        		 Substance component = SubstanceFactory.getFullSubstance(mc.substance);
		        		 if(component instanceof ChemicalSubstance){
		        			 extractLychis((ChemicalSubstance)component, consumer);
		        		 }
		        	 }
		         });
		         
	}
	
	public void extractLychis(ChemicalSubstance s, Consumer<IndexableValue> consumer) {
		
		//consumer.accept(IndexableValue.simpleStringValue("root_structure_properties_term", lychi3));
		if( s.structure.getStereoInsensitiveHash() != null && s.structure.getStereoInsensitiveHash().length() > 0)
		{
		consumer.accept(IndexableValue.simpleStringValue("root_structure_properties_term", s.structure.getStereoInsensitiveHash()));
		}
		if(s.structure.getExactHash() != null && s.structure.getExactHash().length() >0 )
		{
		consumer.accept(IndexableValue.simpleStringValue("root_structure_properties_term", s.structure.getExactHash()));
		}

		s.moieties.stream().forEach(m->{
			consumer.accept(IndexableValue.simpleStringValue("root_moieties_structure_properties_term", m.structure.getStereoInsensitiveHash()));
			consumer.accept(IndexableValue.simpleStringValue("root_moieties_structure_properties_term", m.structure.getExactHash()));
		});
		
	}
	
}
