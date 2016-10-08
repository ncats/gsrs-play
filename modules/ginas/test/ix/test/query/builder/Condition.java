package ix.test.query.builder;

public interface Condition {
	public String asQueryString();

	default OrCondition or(Condition c2) {
		return new OrCondition(this, c2);
	}

	default AndCondition and(Condition c2) {
		return new AndCondition(this, c2);
	}

	public static TermCondition rawFieldQuery(String field, String value) {
		return new TermCondition(field, value);
	}

	public static ExactMatchTermCondition exactFieldQuery(String field, String value) {
		return new ExactMatchTermCondition(field, value);
	}

	public static OrderedTermCondition orderedFieldQuery(String field, String value) {
		return new OrderedTermCondition(field, value);
	}

	public static GlobalTermCondition rawGlobalQuery(String value) {
		return rawFieldQuery(null, value).asGlobal();
	}

	public static GlobalTermCondition exactGlobalQuery(String value) {
		return exactFieldQuery(null, value).asGlobal();
	}

	public static GlobalTermCondition orderedGlobalQuery(String value) {
		return orderedFieldQuery(null, value).asGlobal();
	}

}