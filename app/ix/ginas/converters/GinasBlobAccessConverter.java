package ix.ginas.converters;

import ix.ginas.models.GinasAccessContainer;
import ix.ginas.models.converters.EntityJsonClobConverter;


public class GinasBlobAccessConverter extends EntityJsonClobConverter<GinasAccessContainer> {
	public GinasBlobAccessConverter() {
		super(GinasAccessContainer.class);
	}
}
