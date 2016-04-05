package ix.ginas.models.converters;

import ix.core.Converter;
import ix.ginas.models.GinasReferenceContainer;

@Converter
public class GinasReferenceConverter extends EntityJsonClobConverter<GinasReferenceContainer> {
	public GinasReferenceConverter() {
		super(GinasReferenceContainer.class);
	}
}
