package ix.ginas.models.converters;

import ix.core.Converter;
import ix.ginas.models.GinasAccessContainer;

@Converter
public class GinasAccessConverter extends EntityJsonClobConverter<GinasAccessContainer> {
	public GinasAccessConverter() {
		super(GinasAccessContainer.class);
	}
}
