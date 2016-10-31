package ix.test.query.builder;

public class OrWhereQueryBuilder extends WhereQueryBuilder {
	private Condition cb;

	public OrWhereQueryBuilder(Condition before) {
		this.cb = before;
	}

	@Override
	public SuppliedQueryBuilder condition(Condition c1) {
		return new SuppliedQueryBuilder(cb.or(c1));
	}

}