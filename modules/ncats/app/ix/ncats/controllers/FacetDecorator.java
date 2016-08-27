package ix.ncats.controllers;

import ix.core.search.TextIndexer.Facet;

public class FacetDecorator {
    final public Facet facet;
    public int max;
    public boolean raw;
    public boolean hidden;
    public Integer[] total;
    public boolean[] selection;
    
    public FacetDecorator (Facet facet) {
        this (facet, false, 6);
    }
    public FacetDecorator (Facet facet, boolean raw, int max) {
        this.facet = facet;
        this.raw = raw;
        this.max = max;
        total = new Integer[facet.size()];
        selection = new boolean[facet.size()];
    }

    public String name () { return facet.getName(); }
    public int size () { return facet.getValues().size(); }
    public String label (int i) {
        return facet.getLabel(i);
    }
    public String value (int i) {
        Integer total = this.total[i];
        Integer count = facet.getCount(i);
        if (total != null) {
            return count+" | "+total;
        }
        return count.toString();
    }
}