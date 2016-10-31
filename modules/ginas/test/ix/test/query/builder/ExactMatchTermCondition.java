package ix.test.query.builder;

public class ExactMatchTermCondition extends PhraseTermCondition {
	public ExactMatchTermCondition(String f, String v) {
		super(f, "^" + v + "$");
	}
}