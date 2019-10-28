package ix.ginas.utils;

import gov.nih.ncats.molwitch.Chemical;
import ix.ginas.modelBuilders.ChemicalSubstanceBuilder;
import ix.ginas.modelBuilders.SubstanceBuilder;

import java.util.Arrays;
import java.util.Collections;
import java.util.Locale;
import java.util.function.Consumer;
import java.util.regex.Pattern;

public class SdfProcessor implements SubstanceProcessor<Chemical, ChemicalSubstanceBuilder>{


    private static Pattern SPLIT_PATTERN = Pattern.compile("\n");

    private static Pattern NAME_SPLIT_PATTERN = Pattern.compile("\\|");
    private static Pattern NAME_LANG_SPLIT_PATTERN = Pattern.compile(",");
    @Override
    public ChemicalSubstanceBuilder process(Chemical c, ChemicalSubstanceBuilder builder) {

        ifPresent(c.getProperty("CAS") , v ->builder.addCode("CAS", v));
        ifPresent(c.getProperty("INN") , v ->builder.addCode("INN", v));

        ifPresent(c.getProperty("NAMES") , v -> asList(v).forEach(n ->{
            //this will avoid duplicates if our display name is also listed under "names"

            String[] props = NAME_SPLIT_PATTERN.split(n);
            //GSRS-917 names property doesn't always have all fields so check for nulls/ length
            if(props ==null || props[0] ==null || props[0].trim().isEmpty()){
                return;
            }
//            System.out.println("name = \""+n+"\" props = " + Arrays.toString(props));
            builder.addName(props[0].trim(), name -> {
                if(props.length >=2) {
                for(String lang : NAME_LANG_SPLIT_PATTERN.split(props[1])) {
                        String trimmedLanguage = lang.trim();
                        if(!trimmedLanguage.isEmpty()) {
                            name.addLanguage(lang.trim());
                        }
                }
                if(props.length >=3){
                        name.displayName = Boolean.parseBoolean(props[2].trim());

                    }
                }
                //default to english if nothing set?
                //TODO should we default to the default locale language ?
                if(name.languages.isEmpty()){
                    name.addLanguage(Locale.getDefault().getLanguage());
                }
            });
        }));

//        ifPresent(c.getProperty("NAME") , v ->builder.setDisplayName(v));
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
