package ix.test.query.builder;

public class AndCondition extends BooleanCondition {
	public AndCondition(Condition c1, Condition c2) {
		super(c1, c2);
	}

	@Override
	public String asQueryString() {
		return "(" + c1.asQueryString() + " AND " + c2.asQueryString() + ")";
	}
}