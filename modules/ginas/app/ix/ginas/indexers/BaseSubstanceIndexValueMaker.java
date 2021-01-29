package ix.ginas.indexers;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import ix.core.search.text.IndexValueMaker;
import ix.core.search.text.IndexableValue;
import ix.ginas.controllers.v1.SubstanceFactory;
import ix.ginas.models.v1.Name;
import ix.ginas.models.v1.Substance;


/**
 * Adds base substance values to index for the substance
 *
 * @author epuzanov
 *
 */
public class BaseSubstanceIndexValueMaker implements IndexValueMaker<Substance>{

	private static List<String> baseRelTypes = Arrays.asList("ACTIVE MOIETY","BASIS OF STRENGTH","ACTIVE CONSTITUENT ALWAYS PRESENT","SUBSTANCE PART");

	@Override
	public void createIndexableValues(Substance s, Consumer<IndexableValue> consumer) {
		s.relationships
			.stream()
			.forEach(r->{
				if (baseRelTypes.contains(r.type) && r.relatedSubstance != null){
					Substance relatedSubstance = SubstanceFactory.getFullSubstance(r.relatedSubstance);
					if (relatedSubstance != null) {
						Optional<Name> aName = relatedSubstance.getDisplayName();
						if(aName.isPresent()) {
							String refPname = aName.get().stdName;
							if (refPname != null && !refPname.isEmpty() && !refPname.equals(r.relatedSubstance.refPname)){
								consumer.accept(IndexableValue.simpleStringValue("root_relationships_baseSubstance_refPname", refPname));
							}
						}
					}
					if (r.relatedSubstance.refPname != null && !r.relatedSubstance.refPname.isEmpty()) {
						consumer.accept(IndexableValue.simpleStringValue("root_relationships_baseSubstance_refPname", r.relatedSubstance.refPname));
					}
					if (r.relatedSubstance.approvalID != null && !r.relatedSubstance.approvalID.isEmpty()) {
						consumer.accept(IndexableValue.simpleStringValue("root_relationships_baseSubstance_approvalID", r.relatedSubstance.approvalID));
					}
				}
			}
		);
	}
}
