package ix.test.query.builder;

public class WhereQueryBuilder {

	public SuppliedQueryBuilder condition(Condition c1) {
		return new SuppliedQueryBuilder(c1);
	}
	
	// TODO: Is this the way it works?
	// for raw? I think it might not be parsed by lucene correctly
	public SuppliedQueryBuilder fieldMatchesAny(String field, String value) {
		return condition(Condition.rawFieldQuery(field, value));
	}

	public SuppliedQueryBuilder fieldMatchesExactly(String field, String value) {
		return condition(Condition.exactFieldQuery(field, value));
	}

	public SuppliedQueryBuilder fieldMatchesPhrase(String field, String value) {
		return condition(Condition.phraseFieldQuery(field, value));
	}

	public SuppliedQueryBuilder globalMatchesAny(String value) {
		return condition(Condition.rawGlobalQuery(value));
	}
	
	public SuppliedQueryBuilder allDocsQuery() {
        return condition(Condition.rawFieldQuery("*", "*"));
    }

	public SuppliedQueryBuilder globalMatchesPhrase(String value) {
		return condition(Condition.orderedGlobalQuery(value));
	}

	public SuppliedQueryBuilder globalMatchesExact(String value) {
		return condition(Condition.exactGlobalQuery(value));
	}
}