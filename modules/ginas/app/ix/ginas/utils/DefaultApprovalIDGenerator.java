package ix.ginas.utils;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class DefaultApprovalIDGenerator extends UniiLikeGenerator{
    private String name;

    private String prefix;

    @JsonCreator
    public DefaultApprovalIDGenerator( @JsonProperty("name") String name,
                                       @JsonProperty("numRandomChars")int numRandomChars,
                                       @JsonProperty("addCheckDigit")boolean addCheckDigit,
                                       @JsonProperty("prefix") String prefix) {
        super(numRandomChars, addCheckDigit);
        this.name = name;
        this.prefix = prefix;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    protected String getRandomPartOf(String id) {
        return id.substring(prefix.length());
    }

    @Override
    protected String decorateRandomID(String randomId) {
        if(prefix ==null){
            return randomId;
        }
        return prefix + randomId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }
}
