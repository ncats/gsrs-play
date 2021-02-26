package ix.ginas.utils;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import ix.core.utils.NamedIdGenerator;
import ix.core.util.ConfigHelper;
import ix.ginas.models.v1.Substance;

public class CodeSystemGenerator implements NamedIdGenerator<Substance, String> {


    private final String name;
    private final String codeSystem;
    @JsonCreator
    public CodeSystemGenerator(@JsonProperty("name") String name,
                                @JsonProperty("codeSystem") String codeSystem){
        this.name = name;
        this.codeSystem = codeSystem;
    }
    @Override
    public synchronized String generateId(Substance s) {
        String code = s.codes.stream().filter(c -> (codeSystem.equals(c.codeSystem) && "PRIMARY".equals(c.type))).findFirst()
            .map(c -> c.getCode()).orElse("");
        return code;
    }


    @Override
    public boolean isValidId(String id) {
        if (id.endsWith(id)) {
            return true;
        }
        return false;
    }

    @Override
    public String getName() {
        return name;
    }
}
