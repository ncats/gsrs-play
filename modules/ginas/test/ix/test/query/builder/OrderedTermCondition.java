package ix.test.query.builder;

public class OrderedTermCondition extends TermCondition {
	public OrderedTermCondition(String f, String v) {
		super(f, "\"" + v + "\"");
	}
}