package ix.test.query.builder;

public class SimpleQueryBuilder {
	public SuppliedQueryBuilder where(Condition c) {
		return new SuppliedQueryBuilder(c);
	}

	public WhereQueryBuilder where() {
		return new WhereQueryBuilder();
	}
}