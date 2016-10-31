package ix.test.query.builder;

public class OrCondition extends BooleanCondition {
	public OrCondition(Condition c1, Condition c2) {
		super(c1, c2);
	}

	@Override
	public String asQueryString() {
		return "(" + c1.asQueryString() + " OR " + c2.asQueryString() + ")";
	}
}