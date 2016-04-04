package ix.ginas.models;

import ix.core.Converter;

@Converter
public class GinasAccessConverter extends EntityJsonBlobConverter<GinasAccessContainer> {
	public GinasAccessConverter() {
		super(GinasAccessContainer.class);
	}
}
