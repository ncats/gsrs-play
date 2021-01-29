package ix.ginas.comparators;

import ix.core.models.Edit;
import java.util.Comparator;
import play.Logger;

public class EditComparator implements Comparator<Edit> {

    public int compare(Edit o1, Edit o2) {
        try {
            int i1 = Integer.parseInt(o1.version) ;
            int i2 = Integer.parseInt(o2.version);
            return i2 - i1;
        } catch (Exception e){
            return o2.version.compareTo(o1.version);
        }
    }
}
