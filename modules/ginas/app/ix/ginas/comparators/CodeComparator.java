package ix.ginas.comparators;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ix.core.util.CachedSupplier;
import ix.core.util.ConfigHelper;
import ix.ginas.models.v1.Code;

import play.Logger;

public class CodeComparator implements Comparator<Code> {

    private static CachedSupplier<Map<String, Integer>> DEFAULT_ORDER = CachedSupplier.of(()->populateCodeOrderMap(ConfigHelper.getOrDefault("ix.ginas.codes.order", new ArrayList<>())));

    private Map<String, Integer> codeSystemOrder = DEFAULT_ORDER.get();


    public Map<String, Integer> getCodeSystemOrder() {
        return codeSystemOrder;
    }

    public void setCodeSystemOrder(List<String> codeSystemOrder) {
        this.codeSystemOrder = populateCodeOrderMap(codeSystemOrder);
    }

    private static Map<String, Integer> populateCodeOrderMap(List<String> list){
        Map<String,Integer> map = new HashMap<>();
        int i = 0;
        for (String s : list) {
            map.put(s, i++);
        }
        return map;
    }


    public int compare(Code c1, Code c2) {
        if(c1.codeSystem == null){
            if(c2.codeSystem == null){
                return 0;
            }
            return 1;
        }
        if(c2.codeSystem == null)return -1;
        Integer i1=codeSystemOrder.get(c1.codeSystem);
        Integer i2=codeSystemOrder.get(c2.codeSystem);

        if(i1 != null && i2 != null){
            return i1-i2;
        }
        if(i1 != null && i2 == null)return -1;
        if(i1 == null && i2 != null)return 1;
        int c = c1.codeSystem.compareTo(c2.codeSystem);
        if(c != 0)return c;
        if(c1.code == null && c2.code == null)return 0;
        if(c1.code != null && c2.code != null){
            return c1.code.compareTo(c2.code);
        }
        if(c1.code == null)return 1;
        return -1;
    }
}
