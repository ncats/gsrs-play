package ix.core.models;

public interface Standardizer<I, O> {
    O standardize (I input);
}
