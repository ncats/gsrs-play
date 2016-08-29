package ix.ncats.controllers;

public abstract class DefaultResultRenderer<T>
    implements ResultRenderer<T> {
    public int getFacetDim () { return App.FACET_DIM; }
}