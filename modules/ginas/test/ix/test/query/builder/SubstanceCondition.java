package ix.test.query.builder;

public interface SubstanceCondition extends Condition {

	public static AbstractTermCondition name(String value) {
		return AbstractTermCondition.of("root_names_name", value);
	}

	public static AbstractTermCondition code(String value) {
		return AbstractTermCondition.of("root_codes_code", value);
	}
	
	public static AbstractTermCondition codeSystem(String codeSystem) {
        return AbstractTermCondition.of("root_codes_codeSystem", codeSystem);
    }
	
	public static AbstractTermCondition codeAndCodeSystem(String code, String codeSystem) {
        return AbstractTermCondition.of("root_codes_" + codeSystem, code);
    }
	
	public static AbstractTermCondition uuid(String uuid) {
        return AbstractTermCondition.of("root_uuid", uuid);
    }

	public static AbstractTermCondition approvaID(String value) {
		return AbstractTermCondition.of("root_approvalID", value);
	}
}