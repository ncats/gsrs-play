package ix.core.search;

public class SuggestResult {
    CharSequence key, highlight;
    long weight=0;
    
    
    public SuggestResult (CharSequence key, CharSequence highlight) {
        this.key = key;
        this.highlight = highlight;
    }
    
    public SuggestResult (CharSequence key, CharSequence highlight, long weight) {
        this.key = key;
        this.highlight = highlight;
        this.weight=weight;
    }

    
    public CharSequence getKey () { return key; }
    public CharSequence getHighlight () { return highlight; }
    public Long getWeight () { return weight; }
}