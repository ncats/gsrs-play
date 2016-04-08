package ix.ginas.converters;

import ix.core.Converter;
import ix.ginas.models.GinasReferenceContainer;
import ix.ginas.models.converters.EntityJsonClobConverter;

@Converter
public class GinasBlobReferenceConverter extends EntityJsonClobConverter<GinasReferenceContainer> {
	public GinasBlobReferenceConverter() {
		super(GinasReferenceContainer.class);
	}
}
