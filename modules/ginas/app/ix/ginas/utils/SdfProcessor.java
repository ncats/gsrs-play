package ix.ginas.utils;

import gov.nih.ncgc.chemical.Chemical;
import ix.ginas.modelBuilders.ChemicalSubstanceBuilder;
import ix.ginas.modelBuilders.SubstanceBuilder;

import java.util.Arrays;
import java.util.Collections;
import java.util.function.Consumer;
import java.util.regex.Pattern;

public class SdfProcessor implements SubstanceProcessor<Chemical, ChemicalSubstanceBuilder>{


    private static Pattern SPLIT_PATTERN = Pattern.compile("\n");
    @Override
    public ChemicalSubstanceBuilder process(Chemical c, ChemicalSubstanceBuilder builder) {

        ifPresent(c.getProperty("CAS") , v ->builder.addCode("CAS", v));
        ifPresent(c.getProperty("INN") , v ->builder.addCode("INN", v));
//        ifPresent(c.getProperty("APPROVAL_ID") , v ->builder.ap("CAS", v));
        ifPresent(c.getProperty("NAME") , v ->builder.setName(v));
        ifPresent(c.getProperty("NAMES") , v -> asList(v).forEach(n ->builder.addName(n)));
        return builder;
    }
    private static Iterable<String> asList(String s){
        String[] array = SPLIT_PATTERN.split(s);
        if(array ==null){
            return Collections.emptyList();
        }
        return Arrays.asList(array);
    }
    private static void ifPresent(String value , Consumer<String> consumer){
        if(value ==null || value.equals("") | value.equals(" ")){
            return;
        }

        consumer.accept(value);
    }
}
