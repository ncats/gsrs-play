package ix.test.query.builder;

public class GlobalTermCondition implements Condition {
	private String value;

	public GlobalTermCondition(String val) {
		this.value = val;
	}

	@Override
	public String asQueryString() {
		return value;
	}
}