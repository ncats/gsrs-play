package ix.test.query.builder;

public class SuppliedQueryBuilder extends SimpleQueryBuilder {
	private Condition c;

	public SuppliedQueryBuilder(Condition c) {
		this.c = c;
	}

	public SuppliedQueryBuilder and(Condition c) {
		
		this.c = this.c.and(c);
		return this;
	}

	public SuppliedQueryBuilder or(Condition c) {
		this.c = this.c.or(c);
		return this;
	}

	public WhereQueryBuilder or() {
		return new OrWhereQueryBuilder(c);
	}

	public WhereQueryBuilder and() {
		return new AndWhereQueryBuilder(c);
	}

	public String build() {
		return c.asQueryString();
	}

	public String toString() {
		return build();
	}
}