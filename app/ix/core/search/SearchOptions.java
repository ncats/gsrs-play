package ix.core.search;

import java.util.List;
import java.util.ArrayList;

public class SearchOptions {
    public Class<?> kind; // filter by type

    public int top;
    public int skip;
    public int fdim; // facet dimension

    public List<String> drilldown = new ArrayList<String>();
    public List<String> order = new ArrayList<String>();
    public List<String> expand = new ArrayList<String>();

    public SearchOptions () { }
    public SearchOptions (Class<?> kind, int top, int skip, int fdim) {
        this.kind = kind;
        this.top = Math.max(1, top);
        this.skip = Math.max(0, skip);
        this.fdim = Math.max(1, fdim);
    }

    public int max () { return skip+top; }
}
