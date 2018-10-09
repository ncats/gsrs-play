package ix.test.query.builder;

public class SimpleQueryBuilder {

	public static  SuppliedQueryBuilder searchAll(){
		return new SimpleQueryBuilder()
				.where()
				.allDocsQuery();
	}

	public static  SuppliedQueryBuilder exactSearch(String query){
		return new SimpleQueryBuilder()
				.where()
				.globalMatchesExact(query);
	}

	public SuppliedQueryBuilder where(Condition c) {
		return new SuppliedQueryBuilder(c);
	}

	public WhereQueryBuilder where() {
		return new WhereQueryBuilder();
	}
}