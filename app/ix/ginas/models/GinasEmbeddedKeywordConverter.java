package ix.ginas.models;

public class GinasEmbeddedKeywordConverter extends EntityJsonBlobConverter<EmbeddedKeywordList> {
	public GinasEmbeddedKeywordConverter() {
		super(EmbeddedKeywordList.class);
	}
}
