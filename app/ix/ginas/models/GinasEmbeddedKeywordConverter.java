package ix.ginas.models;

import ix.core.Converter;

@Converter
public class GinasEmbeddedKeywordConverter extends EntityJsonBlobConverter<EmbeddedKeywordList> {
	public GinasEmbeddedKeywordConverter() {
		super(EmbeddedKeywordList.class);
	}
}
