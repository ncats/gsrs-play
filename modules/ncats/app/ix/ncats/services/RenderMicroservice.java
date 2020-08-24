package ix.ncats.services;

import ix.core.models.Structure;
import ix.core.plugins.MicroServicePlugin;
import ix.core.services.RendererService;
import ix.core.util.CachedSupplier;
import play.Play;

public class RenderMicroservice implements RendererService {
    public static CachedSupplier<MicroServicePlugin> _microservice =
            CachedSupplier.of(()-> Play.application().plugin(MicroServicePlugin.class)) ;

    @Override
    public byte[] render(Structure struc, String format, int size, int[] atomMap, Boolean showStereo) throws Exception {
        if(_microservice.get().shouldUse()){
            return callMicroservice(struc,format,size,atomMap, showStereo);
        }
        return DefaultRenderer.INSTANCE.render(struc,format, size, atomMap,showStereo );
    }

    private byte[] callMicroservice(Structure struc, String format, int size, int[] atomMap, Boolean showStereo) throws Exception {
        return ix.core.controllers.services.RendererServiceRequest.createFor(struc, size)
                .format(format)
                .atomMap(atomMap)
                .markStereo(showStereo)
                .execute().get(5_000);
    }
}
