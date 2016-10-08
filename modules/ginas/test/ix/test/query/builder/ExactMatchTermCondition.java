package ix.test.query.builder;

public class ExactMatchTermCondition extends OrderedTermCondition {
	public ExactMatchTermCondition(String f, String v) {
		super(f, "^" + v + "$");
	}
}