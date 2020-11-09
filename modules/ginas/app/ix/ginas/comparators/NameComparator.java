package ix.ginas.comparators;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.ObjectUtils;

import ix.ginas.models.v1.Name;

import play.Logger;

public class NameComparator implements Comparator<Name> {

    private static List<String> languages;

    public NameComparator(Map m) {
        languages = (List<String>) m.get("language_order");
    }

    /**
     * Utility function to sort names in nice display order.
     * <p>
     * Sort criteria: </p>
     * <ol>
     * <li> Display Name </li>
     * <li> Preferred status</li>
     * <li> Official status</li>
     * <li> Language order</li>
     * <li> Alphabetical</li>
     * <li> Name Type</li>
     * <li> Number of References</li>
     *
     *
     * </ol>
     *
     * Note that this sort order was changed in September 2018
     * for v2.3.1 so sorting with older versions might
     * be slightly different.
     */
    public int compare(Name o1, Name o2) {
        if(o1.isDisplayName() != o2.isDisplayName()){
            if(o1.isDisplayName())return -1;
            return 1;
        }
        if(o1.preferred != o2.preferred){
            if(o1.preferred)return -1;
            return 1;
        }
        if(o1.isOfficial() != o2.isOfficial()){
            if(o1.isOfficial())return -1;
            return 1;
        }
        for (String lang : languages) {
            if(o1.isLanguage(lang) != o2.isLanguage(lang)){
                if(o1.isLanguage(lang))return -1;
                return 1;
            }
        }

        int nameCompare = ObjectUtils.compare(o2.name, o1.name);
        if(nameCompare !=0){
            return nameCompare;
        }

        int nameType = ObjectUtils.compare(o2.type, o1.type);
        if(nameType !=0){
            return nameType;
        }
        return o1.getReferences().size()-o2.getReferences().size();
    }
}
