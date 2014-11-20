package ix.core.models;

public class IdentityStandardizer implements Standardizer<String, String> {
    public IdentityStandardizer () {
    }

    public String standardize (String input) {
        return input;
    }
}
