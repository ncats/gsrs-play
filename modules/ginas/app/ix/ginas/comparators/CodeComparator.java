package ix.ginas.comparators;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ix.core.util.ConfigHelper;
import ix.ginas.models.v1.Code;

import play.Logger;

public class CodeComparator implements Comparator<Code> {

    private static Map<String, Integer> codeSystemOrder = new HashMap<String, Integer>();

    public CodeComparator(Map m) {
        List<String> codeSystems = (List<String>) m.get("codesystem_order");
        int i = 0;
        for (String s : codeSystems) {
            codeSystemOrder.put(s, i++);
        }
    }

    public CodeComparator() {
        List<String> codeSystems = (List<String>) ConfigHelper.getOrDefault("ix.ginas.codes.order",
                new ArrayList<String>());
        int i = 0;
        for (String s : codeSystems) {
            codeSystemOrder.put(s, i++);
        }
    }

    public int compare(Code c1, Code c2) {
        if(c1.codeSystem==null){
            if(c2.codeSystem==null){
                return 0;
            }
            return 1;
        }
        if(c2.codeSystem==null)return -1;
        Integer i1=codeSystemOrder.get(c1.codeSystem);
        Integer i2=codeSystemOrder.get(c2.codeSystem);

        if(i1!=null && i2!=null){
            return i1-i2;
        }
        if(i1!=null && i2==null)return -1;
        if(i1==null && i2!=null)return 1;
        return c1.codeSystem.compareTo(c2.codeSystem);
    }
}
