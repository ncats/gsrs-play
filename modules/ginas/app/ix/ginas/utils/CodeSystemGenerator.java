package ix.ginas.utils;

import ix.core.util.ConfigHelper;
import ix.ginas.models.v1.Substance;

public class CodeSystemGenerator implements IdGeneratorForType<Substance, String> {

    private static String codeSystem = ConfigHelper.getOrDefault("ix.ginas.approvalIDGenerator.name", "UNII");

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
}
