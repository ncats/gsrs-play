package ix.test.query.builder;

public class PhraseTermCondition extends TermCondition {
	public PhraseTermCondition(String f, String v) {
		super(f, "\"" + v + "\"");
	}
}