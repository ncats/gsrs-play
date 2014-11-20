package ix.core.models;

public class MixedCaseStandardizer 
    implements Standardizer<String, String> {

    public MixedCaseStandardizer () {
    }

    public String standardize (String input) {
        return input;
    }
}
