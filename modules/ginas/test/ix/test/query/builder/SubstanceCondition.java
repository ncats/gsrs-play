package ix.test.query.builder;

public interface SubstanceCondition extends Condition {

	public static AbstractTermCondition name(String value) {
		return AbstractTermCondition.of("root_names_name", value);
	}

	public static AbstractTermCondition code(String value) {
		return AbstractTermCondition.of("root_codes_code", value);
	}

	public static AbstractTermCondition approvaID(String value) {
		return AbstractTermCondition.of("root_approvalID", value);
	}
}