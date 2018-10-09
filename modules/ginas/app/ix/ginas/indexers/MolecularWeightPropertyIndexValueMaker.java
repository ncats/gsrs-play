package ix.ginas.indexers;

import java.util.function.Consumer;

import ix.core.search.text.IndexValueMaker;
import ix.core.search.text.IndexableValue;
import ix.ginas.models.v1.ChemicalSubstance;
import ix.ginas.models.v1.Substance;

/**
 * Created by VenkataSaiRa.Chavali on 4/20/2017.
 */
public class MolecularWeightPropertyIndexValueMaker implements IndexValueMaker<Substance> {

    @Override
    public void createIndexableValues(Substance substance, Consumer<IndexableValue> consumer) {
        boolean[] added= new boolean[]{false};
        
    	if (substance.properties != null) {
    		
            substance.properties.stream()
                    .filter(a -> a.getName().toUpperCase().contains("MOL_WEIGHT"))
                    .forEach(p -> {
                        Double avg = p.getValue().average;
                        if (avg != null) {
                            consumer.accept(IndexableValue.simpleFacetLongValue("Molecular Weight", Math.round(avg), new long[]{0, 200, 400, 600, 800, 1000}));
                            added[0]=true;
                        }
                    });
        }
        if(!added[0]){
        	if(substance instanceof ChemicalSubstance){
        		ChemicalSubstance cs = (ChemicalSubstance)substance;
        		double avg=cs.structure.mwt;
        		consumer.accept(IndexableValue.simpleFacetLongValue("Molecular Weight", Math.round(avg), new long[]{0, 200, 400, 600, 800, 1000}));
        	}
        }
    }
}