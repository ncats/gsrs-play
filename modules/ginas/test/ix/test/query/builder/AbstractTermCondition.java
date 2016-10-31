package ix.test.query.builder;

public class AbstractTermCondition {
	private String field;
	private String value;

	public AbstractTermCondition(String field, String value) {
		this.field = field;
		this.value = value;
	}

	public ExactMatchTermCondition exact() {
		return Condition.exactFieldQuery(field, value);
	}

	public PhraseTermCondition phrase() {
		return Condition.phraseFieldQuery(field, value);
	}

	public TermCondition raw() {
		return Condition.rawFieldQuery(field, value);
	}
	
	public TermCondition prefix() {
		return Condition.rawFieldQuery(field, value + "*");
	}
	

	public static AbstractTermCondition of(String field, String value) {
		return new AbstractTermCondition(field, value);
	}
}