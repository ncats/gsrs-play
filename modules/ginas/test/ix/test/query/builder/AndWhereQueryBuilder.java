package ix.test.query.builder;

public class AndWhereQueryBuilder extends WhereQueryBuilder {
	private Condition cb;

	public AndWhereQueryBuilder(Condition before) {
		this.cb = before;
	}

	@Override
	public SuppliedQueryBuilder condition(Condition c1) {
		return new SuppliedQueryBuilder(cb.and(c1));
	}

}