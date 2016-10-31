package ix.ginas.models.converters;

import ix.core.Converter;
import ix.ginas.converters.EntityJsonClobConverter;
import ix.ginas.models.EmbeddedKeywordList;

@Converter
public class GinasEmbeddedKeywordConverter extends EntityJsonClobConverter<EmbeddedKeywordList> {
	public GinasEmbeddedKeywordConverter() {
		super(EmbeddedKeywordList.class);
	}
}
