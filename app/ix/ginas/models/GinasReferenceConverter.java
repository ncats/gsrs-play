package ix.ginas.models;

import ix.core.Converter;

@Converter
public class GinasReferenceConverter extends EntityJsonBlobConverter<GinasReferenceContainer> {
	public GinasReferenceConverter() {
		super(GinasReferenceContainer.class);
	}
}
