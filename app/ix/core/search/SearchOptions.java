package ix.core.search;

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

public class SearchOptions {
    public static final int DEFAULT_TOP = 10;
    public static final int DEFAULT_FDIM = 10;
    
    public Class<?> kind; // filter by type

    public int top = DEFAULT_TOP;
    public int skip;
    public int fdim = DEFAULT_FDIM; // facet dimension
    // whether drilldown (false) or sideway (true)
    public boolean sideway = true;
    public String filter;
    
    /**
     * Facet is of the form: DIMENSION/VALUE...
     */
    public List<String> facets = new ArrayList<String>();
    public List<String> order = new ArrayList<String>();
    public List<String> expand = new ArrayList<String>();

    public SearchOptions () { }
    public SearchOptions (Class<?> kind, int top, int skip, int fdim) {
        this.kind = kind;
        this.top = Math.max(1, top);
        this.skip = Math.max(0, skip);
        this.fdim = Math.max(1, fdim);
    }

    public void setFacet (String facet, String value) {
        facets.clear();
        addFacet (facet, value);
    }
    public void addFacet (String facet, String value) {
        facets.add(facet+"/"+value);
    }

    public int max () { return skip+top; }
    public String toString () {
        StringBuilder sb = new StringBuilder
            ("SearchOptions{kind="+(kind!=null ? kind.getName():"")
             +",top="+top+",skip="+skip+",fdim="+fdim+",sideway="+sideway
             +",filter="+filter+",facets={");
        for (Iterator<String> it = facets.iterator(); it.hasNext(); ) {
            sb.append(it.next());
            if (it.hasNext()) sb.append(",");
        }
        sb.append("},order={");
        for (Iterator<String> it = order.iterator(); it.hasNext(); ) {
            sb.append(it.next());
            if (it.hasNext()) sb.append(",");
        }
        sb.append("},expand={");
        for (Iterator<String> it = expand.iterator(); it.hasNext(); ) {
            sb.append(it.next());
            if (it.hasNext()) sb.append(",");
        }
        sb.append("}}");
        return sb.toString();
    }
}
