package ix.test.query.builder;

public class TermCondition implements Condition {
	private String field;
	private String value;

	@Override
	public String asQueryString() {
		return field + ":" + value;
	}

	public TermCondition(String f, String v) {
		this.field = f;
		this.value = v;
	}

	public GlobalTermCondition asGlobal() {
		return new GlobalTermCondition(value);
	}
}