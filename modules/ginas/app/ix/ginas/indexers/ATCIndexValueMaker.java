package ix.ginas.indexers;

import ix.core.search.text.IndexValueMaker;
import ix.core.search.text.IndexableValue;
import ix.ginas.models.v1.Substance;

import play.Logger;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Created by VenkataSaiRa.Chavali on 4/7/2017.
 */
public class ATCIndexValueMaker implements IndexValueMaker<Substance> {
    @Override
    public void createIndexableValues(Substance substance, Consumer<IndexableValue> consumer) {
        List<String> acts = substance.getCodes().stream()
                .filter(r -> "WHO-ATC".equals(r.codeSystem))
                .map(r->r.comments).collect(Collectors.toList());

        try{
	        for(String act : acts){
	        	if(act ==null){
	        		continue;
				}
	            String[] parts = act.split("\\|");
	            if(parts.length>0){
	            	consumer.accept(IndexableValue.simpleFacetStringValue("ATC Level 1", parts[1].trim()).suggestable());
	            }
	            if(parts.length>1){
	            	consumer.accept(IndexableValue.simpleFacetStringValue("ATC Level 2", parts[2].trim()).suggestable());
	            }
	            if(parts.length>2){
	            	consumer.accept(IndexableValue.simpleFacetStringValue("ATC Level 3", parts[3].trim()).suggestable());
	            }
	            if(parts.length>3){
	            	consumer.accept(IndexableValue.simpleFacetStringValue("ATC Level 4", parts[4].trim()).suggestable());
	            }
	        }
        }catch(Exception e){
        	e.printStackTrace();
        	Logger.error("Trouble making ATC facet", e);
        }
    }
}
