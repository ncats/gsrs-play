package ix.ginas.indexers;

import ix.core.search.text.IndexValueMaker;
import ix.core.search.text.IndexableValue;
import ix.ginas.models.v1.Substance;

import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by VenkataSaiRa.Chavali on 6/23/2017.
 */
public class BracketTermIndexValueMaker implements IndexValueMaker<Substance> {
    @Override
    public void createIndexableValues(Substance substance, Consumer<IndexableValue> consumer) {
        
        Pattern p = Pattern.compile("(?:[ \\]])\\[([ \\-A-Za-z0-9]+)\\]");
        if (substance.names != null) {
            substance.names.stream()
                    .filter(a -> a.getName().trim().endsWith("]"))
                    .forEach(n -> {
                        //ASPIRIN1,23[asguyasgda]asgduytqwqd [INN][USAN]
                        Matcher m = p.matcher(n.getName());
                        while (m.find()) {
                            String loc = m.group(1);
                            consumer.accept(IndexableValue.simpleFacetStringValue("GInAS Tag",loc));
        
                        }
                    });
        }
        }
}
