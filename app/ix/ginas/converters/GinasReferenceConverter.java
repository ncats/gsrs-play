package ix.ginas.converters;

import ix.core.Converter;
import ix.ginas.models.GinasReferenceContainer;
import ix.ginas.models.converters.EntityJsonClobConverter;

@Converter
public class GinasReferenceConverter extends EntityJsonClobConverter<GinasReferenceContainer> {
	public GinasReferenceConverter() {
		super(GinasReferenceContainer.class);
	}
}
