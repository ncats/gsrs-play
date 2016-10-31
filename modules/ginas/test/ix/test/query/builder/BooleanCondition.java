package ix.test.query.builder;

public abstract class BooleanCondition implements Condition {
	protected Condition c1;
	protected Condition c2;

	public BooleanCondition(Condition c1, Condition c2) {
		this.c1 = c1;
		this.c2 = c2;
	}
}