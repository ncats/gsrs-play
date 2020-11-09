package ix.ginas.comparators;

import java.util.Comparator;

import ix.ginas.models.v1.Moiety;

import play.Logger;

public class MoietyComparator implements Comparator<Moiety> {

    public int compare(Moiety o1, Moiety o2) {
        if(o1.innerUuid == null){
            if(o2.innerUuid == null) {
                return 0;
            }
            return 1;
        }
        if(o2.innerUuid == null){
            return -1;
        }
        return o1.innerUuid.compareTo(o2.innerUuid);
    }
}
