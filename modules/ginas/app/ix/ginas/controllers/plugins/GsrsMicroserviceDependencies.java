package ix.ginas.controllers.plugins;

import ix.core.search.text.IndexListener;
import ix.core.services.RendererService;
import ix.ncats.services.RenderMicroservice;

import java.util.Collections;
import java.util.List;

public class GsrsMicroserviceDependencies extends GsrsDependencyPlugin.Dependencies {

    @Override
    public RendererService getRendererService() {
        return new RenderMicroservice();
    }

}
